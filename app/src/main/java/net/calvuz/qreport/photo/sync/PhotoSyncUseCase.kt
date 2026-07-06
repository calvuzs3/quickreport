package net.calvuz.qreport.photo.sync

import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.calvuz.qreport.photo.data.local.dao.PhotoDao
import net.calvuz.qreport.photo.data.local.entity.PhotoEntity
import net.calvuz.qreport.photo.data.local.repository.PhotoStorageManager
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Orchestrates bidirectional photo file sync between the device and the
 * server. Mirror of
 * [net.calvuz.qreport.client.document.sync.DocumentSyncUseCase].
 *
 * Must run AFTER [net.calvuz.qreport.sync.domain.usecase.SyncUseCase] so that
 * photo metadata records (and their resolved `checkup_<id>/` filePath) are
 * already in Room before file bytes are transferred.
 *
 * Algorithm:
 *  1. GET /photos/manifest  →  server {id, fileHash, fileSize} list
 *  2. Load all local photos from Room
 *  3. Compute diff:
 *       local only                      →  upload
 *       server only / file not on disk  →  download (new photo, or metadata
 *                                            arrived via SyncUseCase but bytes
 *                                            not yet transferred)
 *       hash mismatch                   →  download (server wins)
 *       hash match                      →  skip
 *  4. After each successful download: regenerate the thumbnail locally
 *     (thumbnails are never transferred over the network — see
 *     [PhotoStorageManager.regenerateThumbnail]).
 *
 * File sync failures are non-fatal — they are logged and counted in
 * [PhotoSyncResult.errors]. The entity sync result is never affected.
 */
class PhotoSyncUseCase @Inject constructor(
    private val photoDao: PhotoDao,
    private val transferProvider: PhotoTransferProvider,
    private val photoStorageManager: PhotoStorageManager,
    @ApplicationContext private val context: Context
) {

    data class PhotoSyncResult(
        val uploaded: Int   = 0,
        val downloaded: Int = 0,
        val skipped: Int    = 0,
        val errors: Int     = 0
    ) {
        val hasErrors: Boolean get() = errors > 0
        val totalTransferred: Int get() = uploaded + downloaded

        override fun toString(): String =
            "PhotoSyncResult(↑$uploaded ↓$downloaded =$skipped ✗$errors)"
    }

    suspend operator fun invoke(): Result<PhotoSyncResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                var uploaded = 0; var downloaded = 0; var skipped = 0; var errors = 0

                // 1. Server manifest
                val serverManifest = transferProvider.getManifest().getOrElse { e ->
                    Timber.e(e, "PhotoSync: manifest fetch failed — aborting file sync")
                    throw e
                }
                val serverMap: Map<String, PhotoManifestEntry> =
                    serverManifest.associateBy { it.id }

                Timber.d("PhotoSync: server has ${serverManifest.size} photos")

                // 2. All local photos
                val localPhotos: List<PhotoEntity> = photoDao.getAllPhotos()
                val localMap: Map<String, PhotoEntity> = localPhotos.associateBy { it.id }

                Timber.d("PhotoSync: device has ${localPhotos.size} photos")

                // 3a. Upload: present locally but not on server
                for (photo in localPhotos) {
                    if (photo.id in serverMap) continue   // handled in 3b

                    Timber.d("PhotoSync: uploading ${photo.fileName} id=${photo.id}")
                    transferProvider.upload(photo).fold(
                        onSuccess = { uploaded++ },
                        onFailure = { e ->
                            Timber.e(e, "PhotoSync: upload failed id=${photo.id}")
                            errors++
                        }
                    )
                }

                // 3b. Download: server-only, file missing locally, OR hash mismatch
                for (entry in serverManifest) {
                    val local = localMap[entry.id]

                    val needsDownload = when {
                        local == null                       -> true   // new photo from another device
                        !File(local.filePath).exists()       -> true  // metadata exists but bytes not yet transferred
                        PhotoHash.compute(local.filePath) != entry.fileHash -> true  // content changed on server
                        else                                 -> false // identical — skip
                    }

                    if (!needsDownload) {
                        skipped++
                        continue
                    }

                    val targetPath = resolveTargetPath(entry.id, local)
                    Timber.d("PhotoSync: downloading id=${entry.id} to $targetPath")

                    transferProvider.download(entry.id, targetPath).fold(
                        onSuccess = { downloadedHash ->
                            if (downloadedHash == entry.fileHash) {
                                regenerateThumbnailAndPersist(entry.id, local, targetPath)
                                downloaded++
                            } else {
                                Timber.e(
                                    "PhotoSync: hash mismatch after download id=${entry.id} " +
                                            "expected=${entry.fileHash} got=$downloadedHash"
                                )
                                File(targetPath).delete()
                                errors++
                            }
                        },
                        onFailure = { e ->
                            Timber.e(e, "PhotoSync: download failed id=${entry.id}")
                            errors++
                        }
                    )
                }

                val result = PhotoSyncResult(uploaded, downloaded, skipped, errors)
                Timber.d("PhotoSync: complete — $result")
                result

            }.onFailure { Timber.e(it, "PhotoSync: sync session failed") }
        }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves where to save a downloaded file.
     *
     * Normally [local] is never null here: photo metadata (with a fully
     * resolved `checkup_<id>/` filePath) is always inserted by
     * [net.calvuz.qreport.sync.domain.usecase.SyncUseCase] before this use
     * case runs. The staging path is a defensive fallback only.
     */
    private fun resolveTargetPath(id: String, local: PhotoEntity?): String {
        if (local != null && local.filePath.isNotBlank()) {
            return local.filePath
        }
        val stagingDir = File(context.filesDir, "photos/incoming")
        stagingDir.mkdirs()
        return File(stagingDir, id).absolutePath
    }

    /**
     * Regenerates the thumbnail for a just-downloaded photo and stamps its
     * path in Room. [local] may be null if the metadata row wasn't already
     * loaded (defensive path, see [resolveTargetPath]) — reload it in that case.
     */
    private suspend fun regenerateThumbnailAndPersist(
        id: String,
        local: PhotoEntity?,
        filePath: String
    ) {
        val photo = local ?: photoDao.getPhotoById(id) ?: run {
            Timber.w("PhotoSync: cannot regenerate thumbnail — record not found id=$id")
            return
        }

        val thumbnailPath = photoStorageManager.regenerateThumbnail(
            checkItemId = photo.checkItemId,
            photoFilePath = filePath,
            fileName = photo.fileName
        )

        if (thumbnailPath == null) {
            Timber.w("PhotoSync: thumbnail regeneration failed id=$id")
            return
        }

        photoDao.updatePhoto(photo.copy(filePath = filePath, thumbnailPath = thumbnailPath))
    }
}
