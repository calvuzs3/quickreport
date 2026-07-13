@file:Suppress("HardCodedStringLiteral")
package net.calvuz.qreport.client.document.sync.remote

import net.calvuz.qreport.client.document.domain.model.Document
import net.calvuz.qreport.client.document.domain.model.DocumentCategory
import net.calvuz.qreport.client.document.domain.model.DocumentScope
import net.calvuz.qreport.shared.dto.DocumentDto

/**
 * Maps a domain [Document] to its wire representation for the entity sync
 * JSON channel — see [net.calvuz.qreport.sync.domain.usecase.SyncUseCase].
 * The DTO itself now lives in `:shared` (`net.calvuz.qreport.shared.dto.DocumentDto`),
 * consumed identically by the Ktor server.
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