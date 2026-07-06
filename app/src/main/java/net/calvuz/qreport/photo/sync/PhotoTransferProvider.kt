package net.calvuz.qreport.photo.sync

import net.calvuz.qreport.photo.data.local.entity.PhotoEntity

/**
 * A single entry in the server's photo manifest.
 * Contains only the identity and content fingerprint — no metadata.
 */
data class PhotoManifestEntry(
    val id: String,
    val fileHash: String,
    val fileSize: Long
)

/**
 * Abstracts the photo file transfer mechanism. Mirror of
 * [net.calvuz.qreport.client.document.sync.DocumentTransferProvider].
 *
 * Today: [net.calvuz.qreport.photo.sync.remote.KtorPhotoTransferProvider] —
 * calls the /photos endpoints on the Ktor server.
 *
 * [PhotoSyncUseCase] depends only on this interface.
 */
interface PhotoTransferProvider {

    /**
     * Returns the server manifest: list of {id, fileHash, fileSize} for
     * all photos with an uploaded file. Used to compute the diff before
     * transferring files.
     */
    suspend fun getManifest(): Result<List<PhotoManifestEntry>>

    /**
     * Uploads the file bytes of [photo] to the server.
     * The server computes the authoritative hash on the fly.
     */
    suspend fun upload(photo: PhotoEntity): Result<Unit>

    /**
     * Downloads the file bytes for [id] and writes them to [targetPath].
     * Returns the SHA-256 hash of the downloaded bytes for post-download
     * verification.
     */
    suspend fun download(id: String, targetPath: String): Result<String>
}
