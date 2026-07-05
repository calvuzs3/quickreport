package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * ModuleTypeBackup - Backup dei tipi modulo checklist (master data)
 */
@Serializable
data class ModuleTypeBackup(
    val id: String,
    val code: String,
    val label: String,
    val description: String? = null,
    val iconName: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)

/**
 * ModuleTypeIslandTypeLinkBackup - Backup del link many-to-many modulo↔tipo isola
 */
@Serializable
data class ModuleTypeIslandTypeLinkBackup(
    val islandTypeId: String,
    val moduleTypeId: String
)
