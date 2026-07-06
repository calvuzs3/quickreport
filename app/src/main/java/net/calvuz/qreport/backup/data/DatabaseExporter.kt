package net.calvuz.qreport.backup.data

import androidx.room.withTransaction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.calvuz.qreport.backup.domain.model.BackupValidationResult
import net.calvuz.qreport.backup.domain.model.backup.CheckItemBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpAssociationBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpMaintenanceLogAssociationBackup
import net.calvuz.qreport.backup.domain.model.backup.ClientBackup
import net.calvuz.qreport.backup.domain.model.backup.ContactBackup
import net.calvuz.qreport.backup.domain.model.backup.DatabaseBackup
import net.calvuz.qreport.backup.domain.model.backup.FacilityBackup
import net.calvuz.qreport.backup.domain.model.backup.FacilityIslandBackup
import net.calvuz.qreport.backup.domain.model.backup.PhotoBackup
import net.calvuz.qreport.backup.domain.model.backup.TiAssociationBackup
import net.calvuz.qreport.backup.domain.model.backup.TiMaintenanceLogAssociationBackup
import net.calvuz.qreport.app.database.data.local.QReportDatabase
import net.calvuz.qreport.backup.domain.model.backup.ContractBackup
import net.calvuz.qreport.backup.domain.model.backup.MechanicalUnitBackup
import net.calvuz.qreport.backup.domain.model.backup.TechnicalInterventionBackup
import net.calvuz.qreport.backup.domain.model.backup.MaintenanceLogBackup
import net.calvuz.qreport.backup.domain.model.backup.DocumentBackup
import net.calvuz.qreport.backup.domain.model.backup.IslandTypeBackup
import net.calvuz.qreport.backup.domain.model.backup.ModuleTypeBackup
import net.calvuz.qreport.backup.domain.model.backup.ModuleTypeIslandTypeLinkBackup
import net.calvuz.qreport.backup.domain.model.backup.CriticalityBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckItemTemplateBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpStatusBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpStatusTransitionBackup
import net.calvuz.qreport.backup.domain.model.backup.CheckUpSparePartBackup
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpMaintenanceLogAssociationDao
import net.calvuz.qreport.client.island.maintenance.data.local.dao.MaintenanceLogDao
import net.calvuz.qreport.client.island.maintenance.data.local.entity.MaintenanceLogEntity
import net.calvuz.qreport.client.document.data.local.dao.DocumentDao
import net.calvuz.qreport.client.document.data.local.entity.DocumentEntity
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemDao
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpAssociationDao
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpDao
import net.calvuz.qreport.client.client.data.local.dao.ClientDao
import net.calvuz.qreport.client.contact.data.local.dao.ContactDao
import net.calvuz.qreport.client.facility.data.local.dao.FacilityDao
import net.calvuz.qreport.client.island.data.local.dao.IslandDao
import net.calvuz.qreport.checkup.items.data.local.entity.CheckItemEntity
import net.calvuz.qreport.checkup.checkup.data.local.entity.CheckUpEntity
import net.calvuz.qreport.checkup.checkup.data.local.entity.CheckUpIslandAssociationEntity
import net.calvuz.qreport.checkup.checkup.data.local.entity.CheckUpMaintenanceLogAssociationEntity
import net.calvuz.qreport.client.client.data.local.entity.ClientEntity
import net.calvuz.qreport.client.contact.data.local.entity.ContactEntity
import net.calvuz.qreport.client.facility.data.local.entity.FacilityEntity
import net.calvuz.qreport.client.island.data.local.entity.IslandEntity
import net.calvuz.qreport.photo.data.local.entity.PhotoEntity
import net.calvuz.qreport.client.contract.data.local.dao.ContractDao
import net.calvuz.qreport.client.contract.data.local.entity.ContractEntity
import net.calvuz.qreport.client.unit.data.local.dao.MechanicalUnitDao
import net.calvuz.qreport.client.unit.data.local.entity.MechanicalUnitEntity
import net.calvuz.qreport.photo.data.local.dao.PhotoDao
import net.calvuz.qreport.ti.data.local.dao.TiAssociationDao
import net.calvuz.qreport.ti.data.local.dao.TiMaintenanceLogAssociationDao
import net.calvuz.qreport.ti.data.local.dao.TechnicalInterventionDao
import net.calvuz.qreport.ti.data.local.entity.TechnicalInterventionEntity
import net.calvuz.qreport.ti.data.local.entity.TiIslandAssociationEntity
import net.calvuz.qreport.ti.data.local.entity.TiMaintenanceLogAssociationEntity
import net.calvuz.qreport.client.island.data.local.dao.IslandTypeDao
import net.calvuz.qreport.client.island.data.local.entity.IslandTypeEntity
import net.calvuz.qreport.checkup.modules.data.local.dao.ModuleTypeDao
import net.calvuz.qreport.checkup.modules.data.local.entity.ModuleTypeEntity
import net.calvuz.qreport.checkup.modules.data.local.entity.ModuleTypeIslandTypeCrossRef
import net.calvuz.qreport.checkup.criticality.data.local.dao.CriticalityDao
import net.calvuz.qreport.checkup.criticality.data.local.entity.CriticalityEntity
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemTemplateDao
import net.calvuz.qreport.checkup.items.data.local.entity.CheckItemTemplateEntity
import net.calvuz.qreport.checkup.status.data.local.dao.CheckUpStatusDao
import net.calvuz.qreport.checkup.status.data.local.entity.CheckUpStatusEntity
import net.calvuz.qreport.checkup.status.data.local.entity.CheckUpStatusTransitionCrossRef
import net.calvuz.qreport.checkup.spareparts.data.local.dao.CheckUpSparePartDao
import net.calvuz.qreport.checkup.spareparts.data.local.entity.CheckUpSparePartEntity
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.String

/**
 * Transaction export of all db tables
 * - conversion Entity → Backup for JSON serialization
 */

@Singleton
class DatabaseExporter @Inject constructor(
    private val database: QReportDatabase,
    private val checkUpDao: CheckUpDao,
    private val checkItemDao: CheckItemDao,
    private val photoDao: PhotoDao,
    private val clientDao: ClientDao,
    private val contractDao: ContractDao,
    private val contactDao: ContactDao,
    private val facilityDao: FacilityDao,
    private val islandDao: IslandDao,
    private val mechanicalUnitDao: MechanicalUnitDao,
    private val checkUpAssociationDao: CheckUpAssociationDao,
    private val tiAssociationDao: TiAssociationDao,
    private val checkUpMaintenanceLogAssociationDao: CheckUpMaintenanceLogAssociationDao,
    private val tiMaintenanceLogAssociationDao: TiMaintenanceLogAssociationDao,
    private val technicalInterventionDao: TechnicalInterventionDao,
    private val maintenanceLogDao: MaintenanceLogDao,
    private val documentDao: DocumentDao,
    private val islandTypeDao: IslandTypeDao,
    private val moduleTypeDao: ModuleTypeDao,
    private val criticalityDao: CriticalityDao,
    private val checkItemTemplateDao: CheckItemTemplateDao,
    private val checkUpStatusDao: CheckUpStatusDao,
    private val checkUpSparePartDao: CheckUpSparePartDao

) {

    /**
     * Export of all tables in a single transaction
     *
     * @return DatabaseBackup with all data converted
     */
    suspend fun exportAllTables(): DatabaseBackup = database.withTransaction {

        Timber.v("Database export begin")
        val startTime = System.currentTimeMillis()

        try {
            // Export with optimized order
            // less FK dependencies first to reduce lock time

            val clients = clientDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${clients.size} clients")

            val facilities = facilityDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${facilities.size} facilities")

            val contacts = contactDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${contacts.size} contacts")

            val contracts = contractDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${contracts.size} contracts")

            val facilityIslands = islandDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${facilityIslands.size} facility islands")

            val mechanicalUnits = mechanicalUnitDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${mechanicalUnits.size} mechanical units")

            val checkUps = checkUpDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkUps.size} checkups")

            val checkItems = checkItemDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkItems.size} check items")

            val photos = photoDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${photos.size} photos")

            val associations = checkUpAssociationDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${associations.size} checkup associations")

            val tiIslandAssociations = tiAssociationDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${tiIslandAssociations.size} ti island associations")

            val checkUpMaintenanceLogAssociations = checkUpMaintenanceLogAssociationDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkUpMaintenanceLogAssociations.size} checkup maintenance log associations")

            val tiMaintenanceLogAssociations = tiMaintenanceLogAssociationDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${tiMaintenanceLogAssociations.size} ti maintenance log associations")

            val technicalInterventions =
                technicalInterventionDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${technicalInterventions.size} technical interventions")

            val maintenanceLogs = maintenanceLogDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${maintenanceLogs.size} maintenance logs")

            val documents = documentDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${documents.size} documents")

            val islandTypes = islandTypeDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${islandTypes.size} island types")

            val moduleTypes = moduleTypeDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${moduleTypes.size} module types")

            val moduleTypeIslandTypeLinks = moduleTypeDao.getAllModuleIslandLinksOnce().map { it.toBackup() }
            Timber.v("Exported ${moduleTypeIslandTypeLinks.size} module type island type links")

            val criticalityLevels = criticalityDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${criticalityLevels.size} criticality levels")

            val checkItemTemplates = checkItemTemplateDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkItemTemplates.size} check item templates")

            val checkUpStatuses = checkUpStatusDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkUpStatuses.size} checkup statuses")

            val checkUpStatusTransitions = checkUpStatusDao.getAllTransitionsOnce().map { it.toBackup() }
            Timber.v("Exported ${checkUpStatusTransitions.size} checkup status transitions")

            val checkUpSpareParts = checkUpSparePartDao.getAllForBackup().map { it.toBackup() }
            Timber.v("Exported ${checkUpSpareParts.size} checkup spare parts")

            val databaseBackup = DatabaseBackup(
                // Core entities
                checkUps = checkUps,
                checkItems = checkItems,
                photos = photos,

                // Client entities
                clients = clients,
                contacts = contacts,
                contracts = contracts,
                facilities = facilities,
                facilityIslands = facilityIslands,
                mechanicalUnits = mechanicalUnits,

                // Associations
                checkUpAssociations = associations,
                tiIslandAssociations = tiIslandAssociations,
                checkUpMaintenanceLogAssociations = checkUpMaintenanceLogAssociations,
                tiMaintenanceLogAssociations = tiMaintenanceLogAssociations,

                // Technical Intervention
                technicalInterventions = technicalInterventions,

                // Island maintenance
                maintenanceLogs = maintenanceLogs,

                // Documents
                documents = documents,

                // Checkup master data
                islandTypes = islandTypes,
                moduleTypes = moduleTypes,
                moduleTypeIslandTypeLinks = moduleTypeIslandTypeLinks,
                criticalityLevels = criticalityLevels,
                checkItemTemplates = checkItemTemplates,
                checkUpStatuses = checkUpStatuses,
                checkUpStatusTransitions = checkUpStatusTransitions,
                checkUpSpareParts = checkUpSpareParts,

                // Metadata
                exportedAt = Clock.System.now()
            )

            val duration = System.currentTimeMillis() - startTime
            val totalRecords = databaseBackup.getTotalRecordCount()

            Timber.v("Export completed in ${duration} ms - $totalRecords records")
            Timber.i(buildString {
                append("Breakdown: CheckUps=${checkUps.size}, CheckItems=${checkItems.size}, ")
                append("Photos=${photos.size}, ")
                append("Clients=${clients.size}, ContactsError=${contacts.size}, ")
                append("ContractsError=${contracts.size}, ")
                append("FacilityError=${facilities.size}, Islands=${facilityIslands.size}, ")
                append("Associations=${associations.size}")
                append("TechnicalInterventions=${technicalInterventions.size}, ")
                append("MaintenanceLogs=${maintenanceLogs.size}, ")
                append("Documents=${documents.size}, ")
                append("IslandTypes=${islandTypes.size}, ModuleTypes=${moduleTypes.size}, ")
                append("CriticalityLevels=${criticalityLevels.size}, CheckItemTemplates=${checkItemTemplates.size}, ")
                append("CheckUpStatuses=${checkUpStatuses.size}, CheckUpSpareParts=${checkUpSpareParts.size}")
            })

            databaseBackup

        } catch (e: Exception) {
            Timber.e(e, "Database export failed")
            throw DatabaseExportException("Export fallito: ${e.message}", e)
        }
    }

    /**
     * Count total records in db (estimation)
     */
    suspend fun getEstimatedRecordCount(): Int = database.withTransaction {
        try {
            val counts = listOf(
                checkUpDao.count(),
                checkItemDao.count(),
                photoDao.count(),
                clientDao.count(),
                contactDao.count(),
                contractDao.count(),
                facilityDao.count(),
                islandDao.count(),
                mechanicalUnitDao.count(),
                checkUpAssociationDao.count(),
                tiAssociationDao.count(),
                checkUpMaintenanceLogAssociationDao.count(),
                tiMaintenanceLogAssociationDao.count(),
                technicalInterventionDao.count(),
                maintenanceLogDao.count(),
                documentDao.count(),
                islandTypeDao.count(),
                moduleTypeDao.count(),
                criticalityDao.count(),
                checkItemTemplateDao.count(),
                checkUpStatusDao.count(),
                checkUpSparePartDao.count()
            )

            val total = counts.sum()
            Timber.v("Database count estimate: $total records (${counts.joinToString()})")
            total

        } catch (e: Exception) {
            Timber.w(e, "Database count estimation failed")
            0
        }
    }

    /**
     * Database integrity check before expor
     */
    suspend fun validateDatabaseIntegrity(): BackupValidationResult {
        return try {
            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            // Verifica FK consistency
            val orphanedCheckItems = checkItemDao.count() -
                    database.query(
                        "SELECT COUNT(*) FROM check_items ci WHERE EXISTS (SELECT 1 FROM checkups c WHERE c.id = ci.checkup_id)",
                        null
                    ).use {
                        it.moveToFirst()
                        it.getInt(0)
                    }

            if (orphanedCheckItems > 0) {
                warnings.add("$orphanedCheckItems possibly orphaned check items")
            }

            // Check for photo existance on filesystem
            val photosWithMissingFiles = photoDao.getAllForBackup().count { photo ->
                !File(photo.filePath).exists()
            }

            if (photosWithMissingFiles > 0) {
                warnings.add("$photosWithMissingFiles photos have missing files")
            }

            // Check fr db size
            val totalRecords = getEstimatedRecordCount()
            if (totalRecords > 100000) {
                warnings.add("Database is big ($totalRecords record) - the export could be slow")
            }

            BackupValidationResult(
                isValid = true,
                errors = errors,
                warnings = warnings
            )

        } catch (e: Exception) {
            Timber.e(e, "Database integrity validation failed")
            BackupValidationResult.invalid(listOf("Errore validazione: ${e.message}"))
        }
    }
}

/**
 * Custom exception for export errors
 */
class DatabaseExportException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// ===== EXTENSION FUNCTIONS PER MAPPING ENTITY → BACKUP =====

/**
 * Mapping da CheckUpEntity a CheckUpBackup ✔️
 */
fun CheckUpEntity.toBackup(): CheckUpBackup {
    return CheckUpBackup(
        id = id,
        clientCompanyName = clientCompanyName,
        clientContactPerson = clientContactPerson,
        clientSite = clientSite,
        clientAddress = clientAddress,
        clientPhone = clientPhone,
        clientEmail = clientEmail,
        islandSerialNumber = islandSerialNumber,
        islandModel = islandModel,
        islandInstallationDate = islandInstallationDate,
        islandLastMaintenanceDate = islandLastMaintenanceDate,
        islandOperatingHours = islandOperatingHours,
        islandCycleCount = islandCycleCount,
        technicianName = technicianName,
        technicianCompany = technicianCompany,
        technicianCertification = technicianCertification,
        technicianPhone = technicianPhone,
        technicianEmail = technicianEmail,
        checkUpDate = checkUpDate,
        headerNotes = headerNotes,
        islandType = islandType,
        islandTypeId = islandTypeId,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da CheckItemEntity a CheckItemBackup ✔️
 */
fun CheckItemEntity.toBackup(): CheckItemBackup {
    return CheckItemBackup(
        id = id,
        checkUpId = checkUpId,
        moduleType = moduleType,
        moduleTypeId = moduleTypeId,
        itemCode = itemCode,
        description = description,
        status = status,
        criticality = criticality,
        criticalityId = criticalityId,
        notes = notes,
        checkedAt = checkedAt,
        orderIndex = orderIndex
    )
}

/**
 * Mapping da PhotoEntity a PhotoBackup ✔️
 */
fun PhotoEntity.toBackup(): PhotoBackup {
    return PhotoBackup(
        id = id,
        checkItemId = checkItemId,
        fileName = fileName,
        filePath = filePath,
        thumbnailPath = thumbnailPath,
        caption = caption,
        takenAt = takenAt,
        fileSize = fileSize,
        orderIndex = orderIndex,
        width = width,
        height = height,
        gpsLocation = gpsLocation,
        resolution = resolution,
        perspective = perspective,
        exifData = exifData,
        cameraSettings = cameraSettings
    )
}

/**
 * Mapping da ClientEntity a ClientBackup ✔️
 */
fun ClientEntity.toBackup(): ClientBackup {
    return ClientBackup(
        id = id,
        companyName = companyName,
        notes = notes,
        headquartersJson = headquartersJson,
        isActive = isActive,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da ContactEntity a ContactBackup ✔️
 */
fun ContactEntity.toBackup(): ContactBackup {
    return ContactBackup(
        id = id,
        clientId = clientId,
        firstName = firstName,
        lastName = lastName,
        title = title,
        role = role,
        department = department,
        phone = phone,
        mobilePhone = mobilePhone,
        email = email,
        alternativeEmail = alternativeEmail,
        isPrimary = isPrimary,
        isActive = isActive,
        preferredContactMethod = preferredContactMethod,
        notes = notes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

fun ContractEntity.toBackup(): ContractBackup {
    return ContractBackup(
        id = id,
        clientId = clientId,
        name = name,
        description = description,
        startDate = Instant.fromEpochMilliseconds(startDate),
        endDate = Instant.fromEpochMilliseconds(endDate),
        hasPriority = hasPriority,
        hasRemoteAssistance = hasRemoteAssistance,
        hasMaintenance = hasMaintenance,
        notes = notes,
        isActive = isActive,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da FacilityEntity a FacilityBackup ✔️
 */
fun FacilityEntity.toBackup(): FacilityBackup {
    return FacilityBackup(
        id = id,
        clientId = clientId,
        name = name,
        code = code,
        description = notes,
        facilityType = facilityType,
        addressJson = addressJson,
        isPrimary = isPrimary,
        isActive = isActive,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da IslandEntity a FacilityIslandBackup ✔️
 */
fun IslandEntity.toBackup(): FacilityIslandBackup {
    return FacilityIslandBackup(
        id = id,
        facilityId = facilityId,
        commissioningNumber = commissioningNumber,
        islandType = islandType,
        islandTypeId = islandTypeId,
        serialNumber = serialNumber,
        modelNumber = modelNumber,
        model = model,
        installationDate = installationDate?.let { Instant.fromEpochMilliseconds(it) },
        warrantyExpiration = warrantyExpiration?.let { Instant.fromEpochMilliseconds(it) },
        isActive = isActive,
        operatingHours = operatingHours,
        cycleCount = cycleCount,
        lastMaintenanceDate = lastMaintenanceDate?.let { Instant.fromEpochMilliseconds(it) },
        nextScheduledMaintenance = nextScheduledMaintenance?.let { Instant.fromEpochMilliseconds(it) },
        customName = customName,
        location = location,
        notes = notes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}


/**
 * Mapping da MechanicalUnitEntity a MechanicalUnitBackup ✔️
 */
fun MechanicalUnitEntity.toBackup(): MechanicalUnitBackup {
    return MechanicalUnitBackup(
        id = id,
        islandId = islandId,
        unitType = unitType,
        name = name,
        serialNumber = serialNumber,
        model = model,
        notes = notes,
        isActive = isActive,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da CheckUpIslandAssociationEntity a CheckUpAssociationBackup ✔️
 */
fun CheckUpIslandAssociationEntity.toBackup(): CheckUpAssociationBackup {
    return CheckUpAssociationBackup(
        id = id,
        checkupId = checkupId,
        islandId = islandId,
        associationType = associationType,
        notes = notes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}

fun TiIslandAssociationEntity.toBackup(): TiAssociationBackup {
    return TiAssociationBackup(
        id = id,
        interventionId = interventionId,
        islandId = islandId,
        associationType = associationType,
        notes = notes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

fun CheckUpMaintenanceLogAssociationEntity.toBackup(): CheckUpMaintenanceLogAssociationBackup {
    return CheckUpMaintenanceLogAssociationBackup(
        id = id,
        checkupId = checkupId,
        maintenanceLogId = maintenanceLogId,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

fun TiMaintenanceLogAssociationEntity.toBackup(): TiMaintenanceLogAssociationBackup {
    return TiMaintenanceLogAssociationBackup(
        id = id,
        interventionId = interventionId,
        maintenanceLogId = maintenanceLogId,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Extension function to convert TechnicalInterventionEntity to TechnicalInterventionBackup
 *
 * Follows the same pattern as other entity mappers in DatabaseExporter.kt
 */
fun TechnicalInterventionEntity.toBackup(): TechnicalInterventionBackup {
    return TechnicalInterventionBackup(
        id = id,
        interventionNumber = intervention_number,
        createdAt = created_at,
        updatedAt = updated_at,
        status = status.name,
        customerDataJson = customer_data,
        robotDataJson = robot_data,
        workLocationJson = work_location,
        techniciansJson = technicians,
        workDaysJson = work_days,
        interventionDescription = intervention_description,
        materialsUsedJson = materials_used,
        externalReportJson = external_report,
        isComplete = is_complete,
        technicianSignatureJson = technician_signature,
        customerSignatureJson = customer_signature,
        customerName = customer_name
    )
}

/**
 * Mapping da MaintenanceLogEntity a MaintenanceLogBackup
 */
fun MaintenanceLogEntity.toBackup(): MaintenanceLogBackup {
    return MaintenanceLogBackup(
        id = id,
        islandId = islandId,
        operationType = operationType,
        customOperationLabel = customOperationLabel,
        mechanicalUnitId = mechanicalUnitId,
        componentLabel = componentLabel,
        description = description,
        technicianName = technicianName,
        technicianCompany = technicianCompany,
        operatingHoursAtEvent = operatingHoursAtEvent,
        cycleCountAtEvent = cycleCountAtEvent,
        outcome = outcome,
        durationMinutes = durationMinutes,
        notes = notes,
        performedAt = Instant.fromEpochMilliseconds(performedAt),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        syncedAt = syncedAt,
        isActive = isActive,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da DocumentEntity a DocumentBackup
 */
fun DocumentEntity.toBackup(): DocumentBackup {
    return DocumentBackup(
        id = id,
        scope = scope,
        islandId = islandId,
        facilityId = facilityId,
        clientId = clientId,
        fileName = fileName,
        filePath = filePath,
        fileSize = fileSize,
        mimeType = mimeType,
        fileHash = fileHash,
        title = title,
        category = category,
        notes = notes,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        isActive = isActive,
        isDeleted = isDeleted,
        syncedAt = syncedAt
    )
}

/**
 * Mapping da IslandTypeEntity a IslandTypeBackup
 */
fun IslandTypeEntity.toBackup(): IslandTypeBackup {
    return IslandTypeBackup(
        id = id,
        code = code,
        label = label,
        description = description,
        iconName = iconName,
        maintenanceIntervalDays = maintenanceIntervalDays,
        sortOrder = sortOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da ModuleTypeEntity a ModuleTypeBackup
 */
fun ModuleTypeEntity.toBackup(): ModuleTypeBackup {
    return ModuleTypeBackup(
        id = id,
        code = code,
        label = label,
        description = description,
        iconName = iconName,
        sortOrder = sortOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da ModuleTypeIslandTypeCrossRef a ModuleTypeIslandTypeLinkBackup
 */
fun ModuleTypeIslandTypeCrossRef.toBackup(): ModuleTypeIslandTypeLinkBackup {
    return ModuleTypeIslandTypeLinkBackup(
        islandTypeId = islandTypeId,
        moduleTypeId = moduleTypeId
    )
}

/**
 * Mapping da CriticalityEntity a CriticalityBackup
 */
fun CriticalityEntity.toBackup(): CriticalityBackup {
    return CriticalityBackup(
        id = id,
        code = code,
        label = label,
        priority = priority,
        colorHex = colorHex,
        iconEmoji = iconEmoji,
        sortOrder = sortOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da CheckItemTemplateEntity a CheckItemTemplateBackup
 */
fun CheckItemTemplateEntity.toBackup(): CheckItemTemplateBackup {
    return CheckItemTemplateBackup(
        id = id,
        moduleTypeId = moduleTypeId,
        category = category,
        description = description,
        criticalityId = criticalityId,
        orderIndex = orderIndex,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da CheckUpStatusEntity a CheckUpStatusBackup
 */
fun CheckUpStatusEntity.toBackup(): CheckUpStatusBackup {
    return CheckUpStatusBackup(
        id = id,
        code = code,
        label = label,
        colorHex = colorHex,
        iconEmoji = iconEmoji,
        sortOrder = sortOrder,
        isActive = isActive,
        blocksDeletion = blocksDeletion,
        marksCompletion = marksCompletion,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt,
        isDeleted = isDeleted
    )
}

/**
 * Mapping da CheckUpStatusTransitionCrossRef a CheckUpStatusTransitionBackup
 */
fun CheckUpStatusTransitionCrossRef.toBackup(): CheckUpStatusTransitionBackup {
    return CheckUpStatusTransitionBackup(
        fromStatusId = fromStatusId,
        toStatusId = toStatusId
    )
}

/**
 * Mapping da CheckUpSparePartEntity a CheckUpSparePartBackup
 */
fun CheckUpSparePartEntity.toBackup(): CheckUpSparePartBackup {
    return CheckUpSparePartBackup(
        id = id,
        checkupId = checkupId,
        articleUuid = articleUuid,
        name = name,
        codeOem = codeOem,
        codeErp = codeErp,
        codeBm = codeBm,
        unit = unit,
        quantity = quantity,
        notes = notes,
        addedAt = addedAt
    )
}