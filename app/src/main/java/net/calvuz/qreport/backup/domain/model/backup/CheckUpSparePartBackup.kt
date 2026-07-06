package net.calvuz.qreport.backup.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * CheckUpSparePartBackup - Backup dei ricambi selezionati in un checkup
 */
@Serializable
data class CheckUpSparePartBackup(
    val id: String,
    val checkupId: String,
    val articleUuid: String,
    val name: String,
    val codeOem: String = "",
    val codeErp: String = "",
    val codeBm: String = "",
    val unit: String = "pz",
    val quantity: Double? = null,
    val notes: String = "",
    val addedAt: Long
)
