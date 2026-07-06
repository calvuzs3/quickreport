@file:Suppress("HardCodedStringLiteral")
package net.calvuz.qreport.client.document.sync.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.calvuz.qreport.client.document.domain.model.Document
import net.calvuz.qreport.client.document.domain.model.DocumentCategory
import net.calvuz.qreport.client.document.domain.model.DocumentScope

/**
 * DTO for [net.calvuz.qreport.client.document.domain.model.Document]
 * in the entity sync JSON payload.
 *
 * Note: filePath is intentionally absent — paths are local to each device.
 * Each device resolves its own path via [DocumentDirectories.forScope()] after
 * receiving the metadata. The bytes travel through the separate file sync channel.
 *
 * [fileHash] is included so the receiving device knows whether it needs to
 * download the file or already has an identical copy.
 */
@Serializable
data class DocumentDto(
    val id: String,
    val scope: String,

    @SerialName("island_id")   val islandId: String?   = null,
    @SerialName("facility_id") val facilityId: String? = null,
    @SerialName("client_id")   val clientId: String?   = null,

    @SerialName("file_name")   val fileName: String,
    @SerialName("file_size")   val fileSize: Long,
    @SerialName("mime_type")   val mimeType: String,
    @SerialName("file_hash")   val fileHash: String?   = null,

    val title: String,
    val category: String,
    val notes: String?         = null,

    @SerialName("created_at")  val createdAt: Long,
    @SerialName("updated_at")  val updatedAt: Long,
    @SerialName("is_active")   val isActive: Boolean,
    @SerialName("is_deleted")  val isDeleted: Boolean,
    @SerialName("synced_at")   val syncedAt: Long?     = null
)

/**
 * Maps a domain [Document] to its wire representation for the entity sync
 * JSON channel — see [net.calvuz.qreport.sync.domain.usecase.SyncUseCase].
 */
fun Document.toDto() = DocumentDto(
    id = id,
    scope = scope.name,
    islandId = islandId,
    facilityId = facilityId,
    clientId = clientId,
    fileName = fileName,
    fileSize = fileSize,
    mimeType = mimeType,
    fileHash = fileHash,
    title = title,
    category = category.name,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDeleted = isDeleted,
    syncedAt = syncedAt
)

/**
 * Maps an incoming [DocumentDto] back to the domain model.
 *
 * [filePath] isn't part of the DTO (device-local — see the class KDoc): the
 * caller resolves it — pass the existing local record's filePath if this
 * document is already known on this device, or blank for a document arriving
 * for the first time. A blank filePath is a valid, expected sentinel:
 * [net.calvuz.qreport.client.document.sync.DocumentSyncUseCase] resolves a
 * staging path for it once the file bytes are downloaded.
 */
fun DocumentDto.toDomain(filePath: String) = Document(
    id = id,
    scope = DocumentScope.valueOf(scope),
    islandId = islandId,
    facilityId = facilityId,
    clientId = clientId,
    fileName = fileName,
    filePath = filePath,
    fileSize = fileSize,
    mimeType = mimeType,
    fileHash = fileHash,
    title = title,
    category = DocumentCategory.valueOf(category),
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDeleted = isDeleted,
    syncedAt = syncedAt
)