package net.calvuz.qreport.checkup.items.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.calvuz.qreport.photo.domain.model.Photo

@Serializable
data class CheckItem(
    val id: String,
    val checkUpId: String,
    val moduleTypeId: String,
    val itemCode: String,
    val description: String,
    val status: CheckItemStatus,
    val criticalityId: String,
    val notes: String = "",
    val photos: List<Photo> = emptyList(),
    val checkedAt: Instant? = null,
    val orderIndex: Int
)