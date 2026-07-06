@file:Suppress("HardCodedStringLiteral")
package net.calvuz.qreport.photo.data.local.repository

import android.annotation.SuppressLint
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.file.domain.model.DirectorySpec
import net.calvuz.qreport.app.file.domain.repository.*
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.app.util.DateTimeUtils.toFilenameSafeTime
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemDao
import net.calvuz.qreport.photo.domain.repository.PhotoFileInfo
import net.calvuz.qreport.photo.domain.repository.PhotoFileRepository
import net.calvuz.qreport.photo.domain.repository.StorageInfo
import net.calvuz.qreport.settings.domain.repository.TechnicianSettingsRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PhotoFileRepositoryImpl - File operations specifiche per foto
 *
 * SI APPOGGIA a CoreFileRepository per operazioni base
 */
@Singleton
class PhotoFileRepositoryImpl @Inject constructor(
    private val coreFileRepo: CoreFileRepository,
    private val checkItemDao: CheckItemDao,
    private val technicianSettingsRepository: TechnicianSettingsRepository
) : PhotoFileRepository {

    companion object {
        private const val THUMBS_SUBDIR = "thumbs"
        private const val CHECKUP_FOLDER_PREFIX = "checkup_"
        private const val USER_PREFIX_LENGTH = 3
        private const val USER_PREFIX_FALLBACK = "usr"
    }

    // ===== DIRECTORY MANAGEMENT =====

    override suspend fun getPhotosDirectory(): QrResult<String, QrError> {
        return coreFileRepo.getOrCreateDirectory(DirectorySpec.PHOTOS)
    }

    /**
     * Resolves the `checkup_{checkUpId}` folder name that owns [checkItemId]'s photos.
     * Falls back to the raw checkItemId (pre-migration convention) if the checkItem's
     * checkup can't be resolved, so a save never hard-fails on a lookup miss.
     */
    private suspend fun resolveCheckUpFolderName(checkItemId: String): String {
        val checkUpId = checkItemDao.getCheckUpIdForCheckItem(checkItemId)
        if (checkUpId == null) {
            Timber.w("No checkup found for checkItem $checkItemId, falling back to legacy folder name")
            return checkItemId
        }
        return "$CHECKUP_FOLDER_PREFIX$checkUpId"
    }

    /**
     * First [USER_PREFIX_LENGTH] letters of the current technician's name (Settings),
     * lowercased, for a human-readable filename hint. Falls back to
     * [USER_PREFIX_FALLBACK] if no name is configured.
     */
    private suspend fun resolveUserNamePrefix(): String {
        val technicianName = try {
            technicianSettingsRepository.getTechnicianInfo().first().name
        } catch (e: Exception) {
            Timber.w(e, "Failed to read technician name, using fallback prefix")
            ""
        }

        val prefix = technicianName.filter { it.isLetter() }.take(USER_PREFIX_LENGTH).lowercase()
        return prefix.ifBlank { USER_PREFIX_FALLBACK }
    }

    override suspend fun getThumbnailsDirectory(checkItemId: String): QrResult<String, QrError> {
        return try {
            val folderName = resolveCheckUpFolderName(checkItemId)
            coreFileRepo.createSubDirectory(DirectorySpec.PHOTOS, "$folderName/$THUMBS_SUBDIR")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create thumbnails directory")
            QrResult.Error(QrError.PhotoError.DirectoryCreate())
        }
    }

    override suspend fun createCheckItemPhotoDirectory(checkItemId: String): QrResult<String, QrError> {
        return try {
            val folderName = resolveCheckUpFolderName(checkItemId)
            coreFileRepo.createSubDirectory(DirectorySpec.PHOTOS, folderName)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create checkup photo directory for checkItem: $checkItemId")
            QrResult.Error(QrError.PhotoError.DirectoryCreate())
        }
    }

    // ===== PHOTO FILE OPERATIONS =====

    override suspend fun createPhotoFilePath(checkItemId: String): QrResult<String, QrError> {
        return try {
            // Create checkItem directory if needed
            when (val dirResult = createCheckItemPhotoDirectory(checkItemId)) {
                is QrResult.Error -> dirResult
                is QrResult.Success -> {
                    val userPrefix = resolveUserNamePrefix()
                    val timeSuffix = Clock.System.now().toFilenameSafeTime()
                    val fileName = "photo_${checkItemId}_${userPrefix}_${timeSuffix}.jpg"
                    val photoPath = File(dirResult.data, fileName).absolutePath

                    Timber.d("Generated photo path: $photoPath")
                    QrResult.Success(photoPath)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create photo file path")
            QrResult.Error(QrError.PhotoError.FileCreate())
        }
    }

    override suspend fun createThumbnailFilePath(checkItemId: String, photoFileName: String): QrResult<String, QrError> {
        return try {
            when (val thumbDirResult = getThumbnailsDirectory(checkItemId)) {
                is QrResult.Error -> thumbDirResult
                is QrResult.Success -> {
                    val thumbnailFileName = "thumb_$photoFileName"
                    val thumbnailPath = File(thumbDirResult.data, thumbnailFileName).absolutePath

                    QrResult.Success(thumbnailPath)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create thumbnail path")
            QrResult.Error(QrError.PhotoError.ThumbnailCreate())
        }
    }

    override suspend fun getPhotoFileSize(photoFilePath: String): QrResult<Long, QrError> {
        return try {
            when (val sizeResult = coreFileRepo.getFileSize(photoFilePath)) {
                is QrResult.Success -> QrResult.Success(sizeResult.data)
                is QrResult.Error -> QrResult.Error(QrError.PhotoError.FileAccess())
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get photo file size")
            QrResult.Error(QrError.PhotoError.FileAccess())
        }
    }

    // ===== PHOTO VALIDATION =====

    override suspend fun validatePhotoFile(photoFilePath: String): QrResult<Boolean, QrError> {
        return try {
            val exists = coreFileRepo.fileExists(photoFilePath)
            QrResult.Success(exists)
        } catch (e: Exception) {
            Timber.e(e, "Failed to validate photo file")
            QrResult.Error(QrError.PhotoError.Validation())
        }
    }

    override suspend fun getAvailablePhotoStorage(): QrResult<StorageInfo, QrError> {
        return try {
            when (val photosDirResult = getPhotosDirectory()) {
                is QrResult.Error -> QrResult.Error(photosDirResult.error)
                is QrResult.Success -> {
                    val photosDirSize = coreFileRepo.getDirectorySize(photosDirResult.data)
                    val thumbnailsSize = sumThumbsFolders(photosDirResult.data)

                    when (photosDirSize) {
                        is QrResult.Success -> {
                            val listResult = coreFileRepo.listFiles(photosDirResult.data)
                            val photoCount = when (listResult) {
                                is QrResult.Success -> listResult.data.count { !it.isDirectory }
                                is QrResult.Error -> 0
                            }

                            val storageInfo = StorageInfo(
                                totalSize = photosDirSize.data + thumbnailsSize,
                                photosSize = photosDirSize.data,
                                thumbnailsSize = thumbnailsSize,
                                photoCount = photoCount,
                                availableSpace = getAvailableSpace() // File system available space
                            )

                            QrResult.Success(storageInfo)
                        }
                        is QrResult.Error -> QrResult.Error(QrError.PhotoError.StorageAccess())
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get storage info")
            QrResult.Error(QrError.PhotoError.StorageAccess())
        }
    }

    // ===== PHOTO CLEANUP =====

    override suspend fun deletePhotoFiles(photoFilePath: String, thumbnailPath: String?): QrResult<Unit, QrError> {
        return try {
            var hasErrors = false

            // Delete main photo
            when (coreFileRepo.deleteFile(photoFilePath)) {
                is QrResult.Error -> {
                    Timber.e("Failed to delete photo: $photoFilePath")
                    hasErrors = true
                }
                is QrResult.Success -> Timber.d("Deleted photo: $photoFilePath")
            }

            // Delete thumbnail if exists
            thumbnailPath?.let { thumbPath ->
                when (coreFileRepo.deleteFile(thumbPath)) {
                    is QrResult.Error -> {
                        Timber.w("Failed to delete thumbnail: $thumbPath")
                        // Thumbnail deletion failure is not critical
                    }
                    is QrResult.Success -> Timber.d("Deleted thumbnail: $thumbPath")
                }
            }

            if (hasErrors) {
                QrResult.Error(QrError.PhotoError.Delete())
            } else {
                QrResult.Success(Unit)
            }

        } catch (e: Exception) {
            Timber.e(e, "Error deleting photo files")
            QrResult.Error(QrError.PhotoError.Delete())
        }
    }

    override suspend fun cleanupOrphanedPhotos(): QrResult<Int, QrError> {
        return try {
            // TODO: Implement orphaned photos cleanup
            // Would need database repository to check which photos are referenced
            Timber.d("Orphaned photos cleanup not implemented yet")
            QrResult.Success(0)
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup orphaned photos")
            QrResult.Error(QrError.PhotoError.Cleanup())
        }
    }

    override suspend fun listCheckItemPhotos(checkItemId: String): QrResult<List<PhotoFileInfo>, QrError> {
        return try {
            when (val photosDirResult = getPhotosDirectory()) {
                is QrResult.Error -> QrResult.Error(photosDirResult.error)
                is QrResult.Success -> {
                    val folderName = resolveCheckUpFolderName(checkItemId)
                    val checkupDir = File(photosDirResult.data, folderName)

                    if (!checkupDir.exists()) {
                        return QrResult.Success(emptyList())
                    }

                    when (val listResult = coreFileRepo.listFiles(checkupDir.absolutePath)) {
                        is QrResult.Error ->  QrResult.Error(listResult.error)
                        is QrResult.Success -> {
                            // The checkup folder is shared by all its checkItems, so filter
                            // down to files belonging to this one (photo_<checkItemId>_...).
                            val photoFiles = listResult.data
                                .filter {
                                    !it.isDirectory && it.extension in listOf("jpg", "jpeg", "png") &&
                                        it.name.startsWith("photo_${checkItemId}_")
                                }
                                .map { fileInfo ->
                                    PhotoFileInfo(
                                        fileName = fileInfo.name,
                                        filePath = fileInfo.path,
                                        fileSize = fileInfo.size,
                                        lastModified = fileInfo.lastModified,
                                        hasThumbnail = hasThumbnailFile(checkItemId, fileInfo.name)
                                    )
                                }

                            QrResult.Success(photoFiles)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to list checkItem photos")
            QrResult.Error(QrError.PhotoError.List())
        }
    }

    // ===== HELPER METHODS =====

    @SuppressLint("UsableSpace")
    private fun getAvailableSpace(): Long {
        return try {
            // Get available space on internal storage
            val internalDir = File("/data/data")
            internalDir.usableSpace
        } catch (e: Exception) {
            Timber.w(e, "Failed to get available space")
            0L
        }
    }

    /** Sums the size of every checkup's `thumbs` subfolder under the photos root. */
    private fun sumThumbsFolders(photosDirPath: String): Long {
        val checkupDirs = File(photosDirPath).listFiles { file ->
            file.isDirectory && file.name.startsWith(CHECKUP_FOLDER_PREFIX)
        } ?: return 0L

        return checkupDirs.sumOf { checkupDir ->
            File(checkupDir, THUMBS_SUBDIR).walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }
    }

    private suspend fun hasThumbnailFile(checkItemId: String, photoFileName: String): Boolean {
        return try {
            when (val thumbDirResult = getThumbnailsDirectory(checkItemId)) {
                is QrResult.Success -> {
                    val thumbnailPath = File(thumbDirResult.data, "thumb_$photoFileName").absolutePath
                    coreFileRepo.fileExists(thumbnailPath)
                }
                is QrResult.Error -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check for thumbnail")
            false
        }
    }
}