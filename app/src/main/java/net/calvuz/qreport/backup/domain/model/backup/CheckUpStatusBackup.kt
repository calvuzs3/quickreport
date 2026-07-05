package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * CheckUpStatusBackup - Backup degli stati workflow checkup (master data)
 */
@Serializable
data class CheckUpStatusBackup(
    val id: String,
    val code: String,
    val label: String,
    val colorHex: String,
    val iconEmoji: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val blocksDeletion: Boolean = false,
    val marksCompletion: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)

/**
 * CheckUpStatusTransitionBackup - Backup delle transizioni di stato consentite
 */
@Serializable
data class CheckUpStatusTransitionBackup(
    val fromStatusId: String,
    val toStatusId: String
)
