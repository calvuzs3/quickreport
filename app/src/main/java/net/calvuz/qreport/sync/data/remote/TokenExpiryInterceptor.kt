package net.calvuz.qreport.sync.data.remote

import net.calvuz.qreport.sync.data.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that:
 *  - Auto-attaches `Authorization: Bearer <token>` to requests that don't
 *    already carry one — the entity sync API (QReportApi.pull/push) sets it
 *    manually per-call via a Retrofit @Header param, so this is a no-op for
 *    those; the file-transfer APIs (DocumentFileApi/PhotoFileApi) have no
 *    such param and rely entirely on this to authenticate.
 *  - Detects 401 Unauthorized responses and clears the stored JWT token,
 *    forcing the user to re-login on the next sync attempt.
 *
 * This centralizes token handling at the network layer so that every API
 * call benefits from it automatically.
 */
@Singleton
class TokenExpiryInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.header("Authorization") == null) {
            tokenStorage.getToken()?.let { token ->
                request = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            Timber.w("TokenExpiryInterceptor: 401 received — clearing token, re-login required")
            tokenStorage.clearToken()
        }

        return response
    }
}