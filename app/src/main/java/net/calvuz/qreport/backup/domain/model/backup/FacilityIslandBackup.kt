package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class FacilityIslandBackup(
    val id: String,
    val facilityId: String,
    val commissioningNumber: String? = null,
    val islandType: String,
    val islandTypeId: String? = null,
    val serialNumber: String,
    val modelNumber: String? = null,
    val model: String?,
    @Contextual val installationDate: Instant?,
    @Contextual val warrantyExpiration: Instant?,
    val isActive: Boolean,
    val operatingHours: Int,
    val cycleCount: Long,
    @Contextual val lastMaintenanceDate: Instant?,
    @Contextual val nextScheduledMaintenance: Instant?,
    val customName: String?,
    val location: String?,
    val notes: String?,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)