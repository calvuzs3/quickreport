package net.calvuz.qreport.photo.sync.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Retrofit interface for photo file transfer endpoints. Mirror of
 * [net.calvuz.qreport.client.document.sync.remote.DocumentFileApi].
 *
 * Separate from [net.calvuz.qreport.sync.data.remote.QReportApi] so that
 * file transfer configuration (timeouts, streaming) can be tuned independently
 * from the entity sync configuration.
 *
 * All endpoints require the Authorization: Bearer header injected by
 * the existing [net.calvuz.qreport.sync.data.remote.DynamicUrlInterceptor].
 */
interface PhotoFileApi {

    /**
     * Returns {id, fileHash, fileSize} for all photos with an uploaded file
     * on the server. Used to compute the diff before transferring files.
     */
    @GET("photos/manifest")
    suspend fun getManifest(): Response<List<PhotoManifestEntryDto>>

    /**
     * Uploads a photo's file bytes as multipart/form-data.
     * The server computes the authoritative SHA-256 hash on the fly.
     *
     * Max size: 50MB (enforced by server).
     */
    @Multipart
    @POST("photos/upload/{id}")
    suspend fun uploadPhoto(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    /**
     * Downloads a photo's file bytes.
     *
     * @Streaming prevents OkHttp from loading the entire response body
     * into memory.
     */
    @GET("photos/download/{id}")
    @Streaming
    suspend fun downloadPhoto(
        @Path("id") id: String
    ): Response<ResponseBody>
}

// ── DTO ───────────────────────────────────────────────────────────────────────

@Serializable
data class PhotoManifestEntryDto(
    val id: String,
    @SerialName("file_hash") val fileHash: String,
    @SerialName("file_size") val fileSize: Long
)
