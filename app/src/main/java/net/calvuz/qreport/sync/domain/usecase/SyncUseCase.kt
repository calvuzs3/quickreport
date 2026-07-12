package net.calvuz.qreport.sync.domain.usecase

import kotlinx.coroutines.flow.first
import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.file.domain.model.DirectorySpec
import net.calvuz.qreport.app.file.domain.repository.CoreFileRepository
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.client.document.domain.repository.DocumentRepository
import net.calvuz.qreport.client.document.sync.remote.toDomain
import net.calvuz.qreport.client.document.sync.remote.toDto
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpAssociationDao
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpDao
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemDao
import net.calvuz.qreport.checkup.criticality.data.local.dao.CriticalityDao
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemTemplateDao
import net.calvuz.qreport.checkup.modules.data.local.dao.ModuleTypeDao
import net.calvuz.qreport.checkup.status.data.local.dao.CheckUpStatusDao
import net.calvuz.qreport.client.island.data.local.dao.IslandTypeDao
import net.calvuz.qreport.photo.data.local.dao.PhotoDao
import net.calvuz.qreport.sync.data.local.SyncSettingsDataStore
import net.calvuz.qreport.sync.data.local.TokenStorage
import net.calvuz.qreport.sync.data.local.dao.SyncDao
import net.calvuz.qreport.sync.data.remote.RemoteDataSource
import net.calvuz.qreport.sync.data.remote.dto.SyncPayloadDto
import net.calvuz.qreport.sync.domain.model.SyncMode
import net.calvuz.qreport.sync.domain.model.SyncResult
import net.calvuz.qreport.sync.mapper.SyncMapper
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Orchestrates a full bidirectional sync session:
 * 1. Check sync is enabled and token is available
 * 2. Build push payload from local pending changes
 * 3. Push to server — server responds with pull payload in the same call
 * 4. Apply pulled records to local Room DB
 * 5. Mark pushed records as synced
 * 6. Save sync timestamp
 */
private const val MIN_SERVER_VERSION = "1.0.0"
private const val MAX_SERVER_VERSION_EXCLUSIVE = "1.1.0"

private fun compareVersions(a: String, b: String): Int {
    val partsA = a.split(".").map { it.toIntOrNull() ?: 0 }
    val partsB = b.split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0..2) {
        val x = partsA.getOrElse(i) { 0 }
        val y = partsB.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    return 0
}

private fun isVersionCompatible(serverVersion: String): Boolean {
    return compareVersions(serverVersion, MIN_SERVER_VERSION) >= 0 &&
        compareVersions(serverVersion, MAX_SERVER_VERSION_EXCLUSIVE) < 0
}

class SyncUseCase @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val syncDao: SyncDao,
    private val islandTypeDao: IslandTypeDao,
    private val moduleTypeDao: ModuleTypeDao,
    private val criticalityDao: CriticalityDao,
    private val checkUpStatusDao: CheckUpStatusDao,
    private val checkItemTemplateDao: CheckItemTemplateDao,
    private val checkUpDao: CheckUpDao,
    private val checkUpAssociationDao: CheckUpAssociationDao,
    private val checkItemDao: CheckItemDao,
    private val photoDao: PhotoDao,
    private val coreFileRepo: CoreFileRepository,
    private val documentRepository: DocumentRepository,
    private val syncSettingsDataStore: SyncSettingsDataStore,
    private val tokenStorage: TokenStorage,
    private val syncMapper: SyncMapper
) {

    @Suppress("HardCodedStringLiteral")
    suspend operator fun invoke(): QrResult<SyncResult, QrError> {
        return try {

            // 0. Check server version compatibility
            when (val versionResult = remoteDataSource.getServerVersion()) {
                is QrResult.Error -> {
                    Timber.e("SyncUseCase: cannot reach server or parse version: ${versionResult.error}")
                    return QrResult.Error(versionResult.error)
                }
                is QrResult.Success -> {
                    val serverVersion = versionResult.data
                    if (!isVersionCompatible(serverVersion)) {
                        Timber.e("SyncUseCase: server version $serverVersion outside allowed range [$MIN_SERVER_VERSION, $MAX_SERVER_VERSION_EXCLUSIVE)")
                        return QrResult.Error(
                            QrError.NetworkError.ServerVersionIncompatible(
                                serverVersion = serverVersion,
                                minVersion = MIN_SERVER_VERSION,
                                maxVersionExclusive = MAX_SERVER_VERSION_EXCLUSIVE
                            )
                        )
                    }
                    Timber.d("SyncUseCase: server version $serverVersion OK (range=[$MIN_SERVER_VERSION, $MAX_SERVER_VERSION_EXCLUSIVE))")
                }
            }

            // 1. Check sync mode
            val mode = syncSettingsDataStore.getSyncMode().first()
//            val mode = syncSettingsDataStore.getSyncMode()
//                .let { flow -> kotlinx.coroutines.flow.first(flow) }

            if (mode == SyncMode.LOCAL_ONLY) {
                Timber.w("SyncUseCase: sync is disabled (LOCAL_ONLY mode)")
                return QrResult.Error(QrError.NetworkError.SyncDisabled())
            }

            // 2. Check token
            val token = tokenStorage.getToken()
            if (token == null) {
                Timber.w("SyncUseCase: no auth token, user must login first")
                return QrResult.Error(QrError.NetworkError.Unauthorized())
            }

            val lastSync = syncSettingsDataStore.getLastSyncTimestampOnce()
            val now = System.currentTimeMillis()
            val deviceId = syncSettingsDataStore.getDeviceId()

            Timber.d("SyncUseCase: starting sync, last sync: $lastSync, device: $deviceId")

            // 3. Build push payload from local pending changes
            val payload = buildPushPayload(deviceId, now)
            Timber.d(buildString {
                append("SyncUseCase: pushing ${payload.islandTypes.size} island types, ")
                append("${payload.clients.size} clients, ")
                append("${payload.contacts.size} contacts, ${payload.contracts.size} contracts, ")
                append("${payload.facilities.size} facilities, ")
                append("${payload.facilityIslands.size} islands, ${payload.mechanicalUnits.size} units, ")
                append("${payload.maintenanceLogs.size} logs, ${payload.documents.size} documents | ")
                append("master data: ${payload.moduleTypes.size} modules, ")
                append("${payload.criticalityLevels.size} criticalities, ")
                append("${payload.checkupStatuses.size} statuses, ")
                append("${payload.checkItemTemplates.size} templates | ")
                append("checkups: ${payload.checkups.size}, assoc: ${payload.checkupIslandAssociations.size}, ")
                append("photos: ${payload.photos.size}")
            })

            // 4. Push — server responds with pull payload in the same round-trip
            when (val pushResult = remoteDataSource.push(token, payload, lastSync)) {
                is QrResult.Error -> {
                    Timber.e("SyncUseCase: push failed: ${pushResult.error}")
                    return QrResult.Error(pushResult.error)
                }

                is QrResult.Success -> {
                    val response = pushResult.data
                    Timber.d("SyncUseCase: push accepted ${response.acceptedIds.size} records")

                    // 5. Apply pulled records to local DB
                    applyRemoteChanges(response.pulledPayload)

                    // 6. Mark pushed records as synced
                    markPushedAsSynced(payload, now)

                    // 7. Save sync timestamp
                    syncSettingsDataStore.setLastSyncTimestamp(now)

                    val result = SyncResult(
                        syncedAt = now,
                        pushedCount = response.acceptedIds.size,
                        pulledCount = countPulled(response.pulledPayload)
                    )

                    Timber.d("SyncUseCase: sync completed — pushed: ${result.pushedCount}, pulled: ${result.pulledCount}")
                    QrResult.Success(result)
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "SyncUseCase: unexpected exception during sync")
            QrResult.Error(QrError.SystemError.UnknownError(e))
        }
    }

    // ===== PRIVATE HELPERS =====

    private suspend fun buildPushPayload(deviceId: String, now: Long): SyncPayloadDto {
        val isAdmin = tokenStorage.canPushMasterData()
        Timber.d("SyncUseCase: role=${tokenStorage.getRole()}, isAdmin=$isAdmin")
        val pendingCheckups = checkUpDao.getPendingSync()
        val deletedCheckups = checkUpDao.getDeletedPendingSync()
        val pendingCheckupIds = pendingCheckups.map { it.id }
        val pendingCheckItems = if (pendingCheckupIds.isNotEmpty())
            checkItemDao.getCheckItemsByCheckUpIds(pendingCheckupIds)
        else emptyList()
        val pendingCheckItemIds = pendingCheckItems.map { it.id }
        val pendingPhotos = if (pendingCheckItemIds.isNotEmpty())
            photoDao.getPhotosByCheckItemIds(pendingCheckItemIds)
        else emptyList()
        val pendingDocuments = documentRepository.getPendingSync().getOrElse {
            Timber.e(it, "SyncUseCase: failed to load pending documents")
            emptyList()
        }
        return SyncPayloadDto(
            deviceId = deviceId,
            syncTimestamp = now,
            islandTypes = islandTypeDao.getPendingSync().map { syncMapper.islandTypeToDto(it) },
            clients = syncDao.getClientsPendingSync().map { syncMapper.clientToDto(it) },
            contacts = syncDao.getContactsPendingSync().map { syncMapper.contactToDto(it) },
            contracts = syncDao.getContractsPendingSync().map { syncMapper.contractToDto(it) },
            facilities = syncDao.getFacilitiesPendingSync().map { syncMapper.facilityToDto(it) },
            facilityIslands = syncDao.getFacilityIslandsPendingSync()
                .map { syncMapper.facilityIslandToDto(it) },
            mechanicalUnits = syncDao.getMechanicalUnitsPendingSync()
                .map { syncMapper.mechanicalUnitToDto(it) },
            maintenanceLogs = syncDao.getMaintenanceLogsPendingSync()
                .map { syncMapper.maintenanceLogToDto(it) },
            documents = pendingDocuments.map { it.toDto() },
            moduleTypes = if (isAdmin) moduleTypeDao.getPendingSync().map { syncMapper.moduleTypeToDto(it) } else emptyList(),
            criticalityLevels = if (isAdmin) criticalityDao.getPendingSync().map { syncMapper.criticalityLevelToDto(it) } else emptyList(),
            checkupStatuses = if (isAdmin) checkUpStatusDao.getPendingSync().map { syncMapper.checkUpStatusToDto(it) } else emptyList(),
            checkItemTemplates = if (isAdmin) checkItemTemplateDao.getPendingSync().map { syncMapper.checkItemTemplateToDto(it) } else emptyList(),
            moduleTypeIslandTypeLinks = if (isAdmin) moduleTypeDao.getAllModuleIslandLinksOnce().map { syncMapper.moduleIslandLinkToDto(it) } else emptyList(),
            checkUpStatusTransitions = if (isAdmin) checkUpStatusDao.getAllTransitionsOnce().map { syncMapper.checkUpStatusTransitionToDto(it) } else emptyList(),
            checkups = (pendingCheckups + deletedCheckups).map { syncMapper.checkUpToDto(it) },
            checkupIslandAssociations = checkUpAssociationDao.getPendingSync().map { syncMapper.checkUpIslandAssociationToDto(it) },
            checkupItems = pendingCheckItems.map { syncMapper.checkItemToDto(it) },
            photos = pendingPhotos.map { syncMapper.photoToDto(it) }
        )
    }

    private suspend fun applyRemoteChanges(payload: SyncPayloadDto) {
        if (payload.islandTypes.isNotEmpty()) {
            islandTypeDao.upsertAll(payload.islandTypes.map { syncMapper.islandTypeToEntity(it) })
        }
        if (payload.clients.isNotEmpty()) syncDao.upsertClients(payload.clients.map {
            syncMapper.clientToEntity(
                it
            )
        })
        if (payload.contacts.isNotEmpty()) syncDao.upsertContacts(payload.contacts.map {
            syncMapper.contactToEntity(
                it
            )
        })
        if (payload.contracts.isNotEmpty()) syncDao.upsertContracts(payload.contracts.map {
            syncMapper.contractToEntity(
                it
            )
        })
        if (payload.facilities.isNotEmpty()) {
            // Guard against FK violations: the server computes each entity's delta
            // independently by its own updated_at, so a pull batch can contain a
            // facility whose client wasn't itself modified recently enough to be
            // included — or wasn't ever synced to this device. Same pattern as the
            // checkupIslandAssociations guard below.
            val knownClientIds = payload.clients.map { it.id }.toHashSet()
                .also { it.addAll(syncDao.getAllClientIds()) }

            val validFacilities = payload.facilities
                .filter { it.clientId in knownClientIds }
                .map { syncMapper.facilityToEntity(it) }

            val skippedFacilities = payload.facilities.size - validFacilities.size
            if (skippedFacilities > 0) {
                Timber.w("SyncUseCase: skipped $skippedFacilities facilities with unresolvable client FK — will retry on next sync")
            }
            if (validFacilities.isNotEmpty()) syncDao.upsertFacilities(validFacilities)
        }
        if (payload.facilityIslands.isNotEmpty()) {
            val knownFacilityIds = payload.facilities.map { it.id }.toHashSet()
                .also { it.addAll(syncDao.getAllFacilityIds()) }

            val validIslands = payload.facilityIslands
                .filter { it.facilityId in knownFacilityIds }
                .map { syncMapper.facilityIslandToEntity(it) }

            val skippedIslands = payload.facilityIslands.size - validIslands.size
            if (skippedIslands > 0) {
                Timber.w("SyncUseCase: skipped $skippedIslands facility islands with unresolvable facility FK — will retry on next sync")
            }
            if (validIslands.isNotEmpty()) syncDao.upsertFacilityIslands(validIslands)
        }
        if (payload.mechanicalUnits.isNotEmpty() || payload.maintenanceLogs.isNotEmpty()) {
            val knownIslandIds = payload.facilityIslands.map { it.id }.toHashSet()
                .also { it.addAll(syncDao.getAllFacilityIslandIds()) }

            if (payload.mechanicalUnits.isNotEmpty()) {
                val validUnits = payload.mechanicalUnits
                    .filter { it.islandId in knownIslandIds }
                    .map { syncMapper.mechanicalUnitToEntity(it) }

                val skippedUnits = payload.mechanicalUnits.size - validUnits.size
                if (skippedUnits > 0) {
                    Timber.w("SyncUseCase: skipped $skippedUnits mechanical units with unresolvable island FK — will retry on next sync")
                }
                if (validUnits.isNotEmpty()) syncDao.upsertMechanicalUnits(validUnits)
            }
            if (payload.maintenanceLogs.isNotEmpty()) {
                val validLogs = payload.maintenanceLogs
                    .filter { it.islandId in knownIslandIds }
                    .map { syncMapper.maintenanceLogToEntity(it) }

                val skippedLogs = payload.maintenanceLogs.size - validLogs.size
                if (skippedLogs > 0) {
                    Timber.w("SyncUseCase: skipped $skippedLogs maintenance logs with unresolvable island FK — will retry on next sync")
                }
                if (validLogs.isNotEmpty()) syncDao.upsertMaintenanceLogs(validLogs)
            }
        }
        if (payload.documents.isNotEmpty()) {
            // filePath isn't in the DTO (device-local) — preserve the existing
            // local path for already-known documents; blank for new ones,
            // resolved to a staging path once DocumentSyncUseCase downloads them.
            val documents = payload.documents.map { dto ->
                val existingFilePath = documentRepository.getDocumentById(dto.id).getOrNull()?.filePath ?: ""
                dto.toDomain(filePath = existingFilePath)
            }
            documentRepository.upsertDocuments(documents).onFailure {
                Timber.e(it, "SyncUseCase: failed to apply pulled documents")
            }
        }
        if (payload.moduleTypes.isNotEmpty()) moduleTypeDao.upsertAll(payload.moduleTypes.map {
            syncMapper.moduleTypeToEntity(it)
        })
        if (payload.criticalityLevels.isNotEmpty()) criticalityDao.upsertAll(payload.criticalityLevels.map {
            syncMapper.criticalityLevelToEntity(it)
        })
        if (payload.checkupStatuses.isNotEmpty()) checkUpStatusDao.upsertAll(payload.checkupStatuses.map {
            syncMapper.checkUpStatusToEntity(it)
        })
        if (payload.checkItemTemplates.isNotEmpty()) checkItemTemplateDao.upsertAll(payload.checkItemTemplates.map {
            syncMapper.checkItemTemplateToEntity(it)
        })
        if (payload.moduleTypeIslandTypeLinks.isNotEmpty()) {
            moduleTypeDao.deleteAllModuleIslandLinks()
            moduleTypeDao.insertModuleIslandLinks(payload.moduleTypeIslandTypeLinks.map {
                syncMapper.moduleIslandLinkToEntity(it)
            })
        }
        if (payload.checkUpStatusTransitions.isNotEmpty()) {
            checkUpStatusDao.deleteAllTransitions()
            checkUpStatusDao.insertTransitions(payload.checkUpStatusTransitions.map {
                syncMapper.checkUpStatusTransitionToEntity(it)
            })
        }
        if (payload.checkups.isNotEmpty()) checkUpDao.upsertAll(payload.checkups.map {
            syncMapper.checkUpToEntity(it)
        })
        if (payload.checkupIslandAssociations.isNotEmpty()) {
            // Guard against FK violations: the server may send associations that reference
            // checkups soft-deleted on the server (not included in payload.checkups) or
            // islands absent from the payload. Build the full set of known IDs from both
            // the current payload and whatever is already in the local DB.
            val knownCheckupIds = payload.checkups.map { it.id }.toHashSet()
                .also { it.addAll(checkUpDao.getAllIds()) }
            val knownIslandIds = payload.facilityIslands.map { it.id }.toHashSet()
                .also { it.addAll(syncDao.getAllFacilityIslandIds()) }

            val validAssociations = payload.checkupIslandAssociations
                .filter { it.checkupId in knownCheckupIds && it.islandId in knownIslandIds }
                .map { syncMapper.checkUpIslandAssociationToEntity(it) }

            val skipped = payload.checkupIslandAssociations.size - validAssociations.size
            if (skipped > 0) {
                Timber.w("SyncUseCase: skipped $skipped associations with unresolvable FK — will retry on next sync")
            }
            if (validAssociations.isNotEmpty()) checkUpAssociationDao.upsertAll(validAssociations)
        }
        if (payload.checkupItems.isNotEmpty()) {
            payload.checkupItems.groupBy { it.checkupId }.forEach { (checkupId, items) ->
                checkItemDao.deleteCheckItemsByCheckUpId(checkupId)
                checkItemDao.insertCheckItems(items.map { syncMapper.checkItemToEntity(it) })
            }

            // Photos are wholesale-replaced for every check item just replaced
            // above — this also clears local photos whose check item now has
            // none (nothing to re-insert for it).
            val checkItemIds = payload.checkupItems.map { it.id }
            photoDao.deletePhotosByCheckItemIds(checkItemIds)
            if (payload.photos.isNotEmpty()) {
                val checkupIdByCheckItemId = payload.checkupItems.associate { it.id to it.checkupId }
                val photosRootDir = when (val result = coreFileRepo.getOrCreateDirectory(DirectorySpec.PHOTOS)) {
                    is QrResult.Success -> result.data
                    is QrResult.Error -> {
                        Timber.e("SyncUseCase: failed to resolve photos directory, skipping photo metadata apply")
                        null
                    }
                }
                if (photosRootDir != null) {
                    val photoEntities = payload.photos.map { dto ->
                        val checkupId = checkupIdByCheckItemId[dto.checkItemId] ?: dto.checkItemId
                        val filePath = File(File(photosRootDir, "checkup_$checkupId"), dto.fileName).absolutePath
                        syncMapper.photoToEntity(dto, filePath)
                    }
                    photoDao.insertPhotos(photoEntities)
                }
            }
        }

        Timber.d("SyncUseCase: applied remote changes to local DB")
    }

    private suspend fun markPushedAsSynced(payload: SyncPayloadDto, now: Long) {
        payload.islandTypes.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { islandTypeDao.markSynced(it, now) }
        payload.clients.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markClientsSynced(it, now) }
        payload.contacts.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markContactsSynced(it, now) }
        payload.contracts.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markContractsSynced(it, now) }
        payload.facilities.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markFacilitiesSynced(it, now) }
        payload.facilityIslands.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markFacilityIslandsSynced(it, now) }
        payload.mechanicalUnits.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markMechanicalUnitsSynced(it, now) }
        payload.maintenanceLogs.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { syncDao.markMaintenanceLogsSynced(it, now) }
        payload.documents.forEach { dto ->
            documentRepository.markSynced(dto.id, now)
        }
        payload.moduleTypes.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { moduleTypeDao.markSynced(it, now) }
        payload.criticalityLevels.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { criticalityDao.markSynced(it, now) }
        payload.checkupStatuses.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { checkUpStatusDao.markSynced(it, now) }
        payload.checkItemTemplates.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { checkItemTemplateDao.markSynced(it, now) }
        payload.checkups.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { checkUpDao.markSynced(it, now) }
        payload.checkupIslandAssociations.map { it.id }.takeIf { it.isNotEmpty() }
            ?.let { checkUpAssociationDao.markSynced(it, now) }
    }

    private fun countPulled(payload: SyncPayloadDto): Int =
        payload.islandTypes.size + payload.clients.size + payload.contacts.size +
        payload.contracts.size + payload.facilities.size + payload.facilityIslands.size +
        payload.mechanicalUnits.size + payload.maintenanceLogs.size + payload.documents.size +
        payload.moduleTypes.size + payload.criticalityLevels.size +
        payload.checkupStatuses.size + payload.checkItemTemplates.size +
        payload.moduleTypeIslandTypeLinks.size +
        payload.checkups.size + payload.checkupIslandAssociations.size +
        payload.checkupItems.size + payload.photos.size
}