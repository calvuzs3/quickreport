package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * CriticalityBackup - Backup dei livelli di criticità checklist (master data)
 */
@Serializable
data class CriticalityBackup(
    val id: String,
    val code: String,
    val label: String,
    val priority: Int,
    val colorHex: String,
    val iconEmoji: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)
