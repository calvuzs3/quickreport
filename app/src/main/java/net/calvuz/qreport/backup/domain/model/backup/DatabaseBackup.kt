package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * DatabaseBackup - Contenitore per tutti i dati del database
 *
 * ===== CHECKLIST: aggiungere una nuova tabella al backup =====
 * Non esiste un registry: ogni tabella va wired a mano nei seguenti punti:
 * 1. DTO `XxxBackup.kt` in `backup/domain/model/backup/`
 * 2. Campo qui in [DatabaseBackup] + [getTotalRecordCount] + [empty] + [TABLE_NAMES]
 * 3. `DatabaseExporter`: DAO nel costruttore, export + mapper `toBackup()`
 * 4. `DatabaseImporter`: DAO nel costruttore, `clearAllTablesInOrder()` (ordine FK
 *    inverso), `importAllTablesInOrder()` (ordine FK), `validateImportedData()`,
 *    `logImportStatistics()`, mapper `toEntity()`
 * 5. Il DAO deve esporre `getAllForBackup/insertAllFromBackup/deleteAll/count`
 * 6. `DatabaseExportRepositoryImpl.clearAllTables()` - empty literal
 * ================================================================
 */
@Serializable
data class DatabaseBackup(
    // ===== CORE CHECKUP =====
    val checkUps: List<CheckUpBackup>,
    val checkItems: List<CheckItemBackup>,
    val photos: List<PhotoBackup>,

    // ===== CLIENT MANAGEMENT =====
    val clients: List<ClientBackup>,
    val contacts: List<ContactBackup>,
    val contracts: List<ContractBackup>,
    val facilities: List<FacilityBackup>,
    val facilityIslands: List<FacilityIslandBackup>,
    val mechanicalUnits: List<MechanicalUnitBackup>,

    // ===== ASSOCIATIONS =====
    val checkUpAssociations: List<CheckUpAssociationBackup>,
    val tiIslandAssociations: List<TiAssociationBackup> = emptyList(),
    val checkUpMaintenanceLogAssociations: List<CheckUpMaintenanceLogAssociationBackup> = emptyList(),
    val tiMaintenanceLogAssociations: List<TiMaintenanceLogAssociationBackup> = emptyList(),

    // ===== TECHNICAL INTERVENTIONS =====
    val technicalInterventions: List<TechnicalInterventionBackup> = emptyList(),

    // ===== ISLAND MAINTENANCE =====
    val maintenanceLogs: List<MaintenanceLogBackup> = emptyList(),

    // ===== DOCUMENTS =====
    val documents: List<DocumentBackup> = emptyList(),

    // ===== CHECKUP MASTER DATA =====
    val islandTypes: List<IslandTypeBackup> = emptyList(),
    val moduleTypes: List<ModuleTypeBackup> = emptyList(),
    val moduleTypeIslandTypeLinks: List<ModuleTypeIslandTypeLinkBackup> = emptyList(),
    val criticalityLevels: List<CriticalityBackup> = emptyList(),
    val checkItemTemplates: List<CheckItemTemplateBackup> = emptyList(),
    val checkUpStatuses: List<CheckUpStatusBackup> = emptyList(),
    val checkUpStatusTransitions: List<CheckUpStatusTransitionBackup> = emptyList(),
    val checkUpSpareParts: List<CheckUpSparePartBackup> = emptyList(),

    // ===== METADATA =====
    @Contextual val exportedAt: Instant
) {
    /**
     * Conta totale record per validazione
     */
    fun getTotalRecordCount(): Int {
        return checkUps.size +
                checkItems.size +
                photos.size +
                clients.size +
                contacts.size +
                contracts.size +
                facilities.size +
                facilityIslands.size +
                mechanicalUnits.size +
                checkUpAssociations.size +
                tiIslandAssociations.size +
                checkUpMaintenanceLogAssociations.size +
                tiMaintenanceLogAssociations.size +
                technicalInterventions.size +
                maintenanceLogs.size +
                documents.size +
                islandTypes.size +
                moduleTypes.size +
                moduleTypeIslandTypeLinks.size +
                criticalityLevels.size +
                checkItemTemplates.size +
                checkUpStatuses.size +
                checkUpStatusTransitions.size +
                checkUpSpareParts.size
    }

    /**
     * Verifica se il backup è vuoto
     */
    fun isEmpty(): Boolean {
        return getTotalRecordCount() == 0
    }

    companion object {
        /** Nomi dei campi/tabelle del backup, usati per validazione JSON e conteggi UI. */
        val TABLE_NAMES = listOf(
            "checkUps", "checkItems", "photos",
            "clients", "contacts", "contracts", "facilities", "facilityIslands",
            "mechanicalUnits", "checkUpAssociations",
            "tiIslandAssociations", "checkUpMaintenanceLogAssociations", "tiMaintenanceLogAssociations",
            "technicalInterventions", "maintenanceLogs", "documents",
            "islandTypes", "moduleTypes", "moduleTypeIslandTypeLinks", "criticalityLevels",
            "checkItemTemplates", "checkUpStatuses", "checkUpStatusTransitions", "checkUpSpareParts"
        )

        fun empty(): DatabaseBackup {
            return DatabaseBackup(
                checkUps = emptyList(),
                checkItems = emptyList(),
                photos = emptyList(),
                clients = emptyList(),
                contacts = emptyList(),
                contracts = emptyList(),
                facilities = emptyList(),
                facilityIslands = emptyList(),
                mechanicalUnits = emptyList(),
                checkUpAssociations = emptyList(),
                tiIslandAssociations = emptyList(),
                checkUpMaintenanceLogAssociations = emptyList(),
                tiMaintenanceLogAssociations = emptyList(),
                technicalInterventions = emptyList(),
                maintenanceLogs = emptyList(),
                documents = emptyList(),
                islandTypes = emptyList(),
                moduleTypes = emptyList(),
                moduleTypeIslandTypeLinks = emptyList(),
                criticalityLevels = emptyList(),
                checkItemTemplates = emptyList(),
                checkUpStatuses = emptyList(),
                checkUpStatusTransitions = emptyList(),
                checkUpSpareParts = emptyList(),
                exportedAt = Instant.fromEpochMilliseconds(0)
            )
        }
    }
}
