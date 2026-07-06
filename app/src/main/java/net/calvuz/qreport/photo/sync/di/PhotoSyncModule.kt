package net.calvuz.qreport.photo.sync.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.calvuz.qreport.photo.sync.PhotoTransferProvider
import net.calvuz.qreport.photo.sync.remote.KtorPhotoTransferProvider
import net.calvuz.qreport.photo.sync.remote.PhotoFileApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for photo sync. Mirror of
 * [net.calvuz.qreport.client.document.sync.di.DocumentSyncModule].
 *
 * Reuses the same `@Named("fileTransferClient")` [OkHttpClient] already
 * provided by `DocumentSyncModule` (extended read/write timeouts for large
 * file transfers) — no need for a second client, just a second Retrofit
 * instance/API interface pointed at the /photos endpoints.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoSyncModule {

    @Binds
    @Singleton
    abstract fun bindPhotoTransferProvider(
        impl: KtorPhotoTransferProvider
    ): PhotoTransferProvider

    companion object {

        private const val PLACEHOLDER_URL = "https://placeholder.qreport.local/"

        @Provides
        @Singleton
        fun providePhotoFileApi(
            @Named("fileTransferClient") client: OkHttpClient
        ): PhotoFileApi {
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(PLACEHOLDER_URL)
                .client(client)
                .addConverterFactory(
                    json.asConverterFactory("application/json".toMediaType())
                )
                .build()
                .create(PhotoFileApi::class.java)
        }
    }
}
