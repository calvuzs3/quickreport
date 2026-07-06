package net.calvuz.qreport.photo.sync.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.calvuz.qreport.photo.data.local.entity.PhotoEntity
import net.calvuz.qreport.photo.sync.PhotoHash
import net.calvuz.qreport.photo.sync.PhotoManifestEntry
import net.calvuz.qreport.photo.sync.PhotoTransferProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PhotoTransferProvider] implementation backed by the Ktor server. Mirror of
 * [net.calvuz.qreport.client.document.sync.remote.KtorDocumentTransferProvider].
 *
 * Uses the [PhotoFileApi] Retrofit interface. Inherits the Authorization
 * header and dynamic URL rewriting from the OkHttp interceptor chain already
 * configured in NetworkModule.
 *
 * All I/O runs on [Dispatchers.IO].
 */
@Singleton
class KtorPhotoTransferProvider @Inject constructor(
    private val api: PhotoFileApi
) : PhotoTransferProvider {

    override suspend fun getManifest(): Result<List<PhotoManifestEntry>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.getManifest()
                if (!response.isSuccessful) {
                    error("Manifest fetch failed: HTTP ${response.code()}")
                }
                response.body()?.map {
                    PhotoManifestEntry(
                        id       = it.id,
                        fileHash = it.fileHash,
                        fileSize = it.fileSize
                    )
                } ?: emptyList()
            }.onFailure { Timber.e(it, "KtorPhotoTransferProvider: getManifest failed") }
        }

    override suspend fun upload(photo: PhotoEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(photo.filePath)
                if (!file.exists()) error("File not found for upload: ${photo.filePath}")

                val bytes = file.readBytes()
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData(
                    name     = "file",
                    filename = photo.fileName,
                    body     = requestBody
                )

                val response = api.uploadPhoto(photo.id, part)
                if (!response.isSuccessful) {
                    error("Upload failed for ${photo.id}: HTTP ${response.code()}")
                }

                Timber.d("KtorPhotoTransferProvider: uploaded ${photo.fileName} (${bytes.size} bytes)")
            }.onFailure { Timber.e(it, "KtorPhotoTransferProvider: upload failed id=${photo.id}") }
        }

    override suspend fun download(id: String, targetPath: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.downloadPhoto(id)
                if (!response.isSuccessful) {
                    error("Download failed for $id: HTTP ${response.code()}")
                }

                val body = response.body()
                    ?: error("Empty response body for photo $id")

                val targetFile = File(targetPath)
                targetFile.parentFile?.mkdirs()

                body.byteStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }

                Timber.d("KtorPhotoTransferProvider: downloaded photo $id to $targetPath")

                PhotoHash.compute(targetPath)

            }.onFailure { Timber.e(it, "KtorPhotoTransferProvider: download failed id=$id") }
        }
}
