package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * CheckItemTemplateBackup - Backup dei template voce checklist (master data)
 */
@Serializable
data class CheckItemTemplateBackup(
    val id: String,
    val moduleTypeId: String,
    val category: String,
    val description: String,
    val criticalityId: String,
    val orderIndex: Int,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)
