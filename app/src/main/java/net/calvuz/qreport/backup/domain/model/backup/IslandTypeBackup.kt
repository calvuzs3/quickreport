package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * IslandTypeBackup - Backup dei tipi isola (master data)
 */
@Serializable
data class IslandTypeBackup(
    val id: String,
    val code: String,
    val label: String,
    val description: String? = null,
    val iconName: String? = null,
    val maintenanceIntervalDays: Int = 180,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)
