package net.calvuz.qreport.sync.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import net.calvuz.qreport.client.client.data.local.entity.ClientEntity
import net.calvuz.qreport.client.contact.data.local.entity.ContactEntity
import net.calvuz.qreport.client.contract.data.local.entity.ContractEntity
import net.calvuz.qreport.client.facility.data.local.entity.FacilityEntity
import net.calvuz.qreport.client.island.data.local.entity.IslandEntity
import net.calvuz.qreport.client.island.maintenance.data.local.entity.MaintenanceLogEntity
import net.calvuz.qreport.client.unit.data.local.entity.MechanicalUnitEntity

/**
 * DAO for sync operations on the client management entity group.
 *
 * Covers: clients, contacts, contracts, facilities, facility_islands, mechanical_units.
 *
 * Two query types are used throughout:
 *  - "pending push": records modified locally after the last sync
 *    (updated_at > synced_at OR synced_at IS NULL), including soft-deleted ones
 *  - "upsert from server": insert or replace incoming records from the server
 */
@Dao
interface SyncDao {

    // ===== PENDING PUSH — records to send to the server =====

    /**
     * Returns clients that have local changes not yet pushed to the server.
     * Includes soft-deleted records so deletions propagate to other devices.
     */
    @Query("""
        SELECT * FROM clients
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getClientsPendingSync(): List<ClientEntity>

    @Query("""
        SELECT * FROM contacts
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getContactsPendingSync(): List<ContactEntity>

    @Query("""
        SELECT * FROM contracts
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getContractsPendingSync(): List<ContractEntity>

    @Query("""
        SELECT * FROM facilities
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getFacilitiesPendingSync(): List<FacilityEntity>

    @Query("""
        SELECT * FROM facility_islands
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getFacilityIslandsPendingSync(): List<IslandEntity>

    @Query("""
        SELECT * FROM mechanical_units
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getMechanicalUnitsPendingSync(): List<MechanicalUnitEntity>

    @Query("""
        SELECT * FROM maintenance_logs
        WHERE updated_at > COALESCE(synced_at, 0)
        ORDER BY updated_at ASC
    """)
    suspend fun getMaintenanceLogsPendingSync(): List<MaintenanceLogEntity>

    // ===== MARK AS SYNCED — after a successful push =====

    /**
     * Stamps synced_at = [now] on records that were successfully pushed.
     * Uses the id list returned by the server acknowledgement.
     */
    @Query("UPDATE clients SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markClientsSynced(ids: List<String>, now: Long)

    @Query("UPDATE contacts SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markContactsSynced(ids: List<String>, now: Long)

    @Query("UPDATE contracts SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markContractsSynced(ids: List<String>, now: Long)

    @Query("UPDATE facilities SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markFacilitiesSynced(ids: List<String>, now: Long)

    @Query("UPDATE facility_islands SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markFacilityIslandsSynced(ids: List<String>, now: Long)

    @Query("UPDATE mechanical_units SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markMechanicalUnitsSynced(ids: List<String>, now: Long)

    @Query("UPDATE maintenance_logs SET synced_at = :now WHERE id IN (:ids)")
    suspend fun markMaintenanceLogsSynced(ids: List<String>, now: Long)

    // ===== UPSERT FROM SERVER — apply incoming records =====

    /**
     * Insert or update records received from the server (last-write-wins).
     *
     * Uses `@Upsert`, NOT `@Insert(onConflict = REPLACE)`: on a primary-key
     * conflict, REPLACE tells SQLite to DELETE the existing row and INSERT the
     * new one — and several of these entities are the parent side of an
     * `onDelete = CASCADE` foreign key (facility_islands -> maintenance_logs /
     * mechanical_units, clients -> contacts/contracts/facilities, facilities ->
     * facility_islands). Every sync round that echoes back an already-existing
     * parent record (which happens on essentially every sync right after that
     * record was pushed) would silently wipe all of its children. `@Upsert`
     * performs a real UPDATE on conflict instead, never triggering the cascade.
     */
    @Upsert
    suspend fun upsertClients(clients: List<ClientEntity>)

    @Upsert
    suspend fun upsertContacts(contacts: List<ContactEntity>)

    @Upsert
    suspend fun upsertContracts(contracts: List<ContractEntity>)

    @Upsert
    suspend fun upsertFacilities(facilities: List<FacilityEntity>)

    @Upsert
    suspend fun upsertFacilityIslands(islands: List<IslandEntity>)

    @Upsert
    suspend fun upsertMechanicalUnits(units: List<MechanicalUnitEntity>)

    @Upsert
    suspend fun upsertMaintenanceLogs(logs: List<MaintenanceLogEntity>)

    // ===== ID LOOKUPS — used by SyncUseCase to validate FK references before upsert =====

    @Query("SELECT id FROM facility_islands")
    suspend fun getAllFacilityIslandIds(): List<String>

    // ===== DIAGNOSTICS =====

    /** Total number of records pending push across all client-management tables. */
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM clients WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM contacts WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM contracts WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM facilities WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM facility_islands WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM mechanical_units WHERE updated_at > COALESCE(synced_at, 0)) +
            (SELECT COUNT(*) FROM maintenance_logs WHERE updated_at > COALESCE(synced_at, 0))
    """)
    suspend fun countPendingSync(): Int
}