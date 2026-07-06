package net.calvuz.qreport.sync.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.calvuz.qreport.client.document.sync.DocumentSyncUseCase
import net.calvuz.qreport.photo.sync.PhotoSyncUseCase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs document + photo file transfer on an app-lifetime scope, after a
 * successful entity sync.
 *
 * File transfer must NOT run on a caller-owned scope like `viewModelScope`:
 * the entity sync "completed" message appears before file transfer even
 * starts (see [net.calvuz.qreport.sync.domain.usecase.SyncUseCase] vs.
 * [DocumentSyncUseCase]/[PhotoSyncUseCase] ordering), so if the user
 * navigates away right after seeing that message, a `viewModelScope`-bound
 * launch would be cancelled mid-transfer with no error, no log, no upload —
 * exactly the silent "photos never reach the server" symptom this fixes.
 *
 * Used by both [net.calvuz.qreport.sync.presentation.ui.SyncSettingsViewModel]
 * (manual sync) and [SyncForegroundObserver] (automatic sync), so the two
 * call sites don't duplicate this orchestration.
 */
@Singleton
class FileSyncCoordinator @Inject constructor(
    private val documentSyncUseCase: DocumentSyncUseCase,
    private val photoSyncUseCase: PhotoSyncUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Fire-and-forget: returns immediately, transfer continues on [scope]
     * regardless of what happens to the caller afterward.
     */
    fun launchAfterEntitySync() {
        scope.launch {
            documentSyncUseCase().fold(
                onSuccess = { Timber.d("FileSyncCoordinator: document file sync — $it") },
                onFailure = { Timber.e(it, "FileSyncCoordinator: document file sync failed") }
            )
            photoSyncUseCase().fold(
                onSuccess = { Timber.d("FileSyncCoordinator: photo file sync — $it") },
                onFailure = { Timber.e(it, "FileSyncCoordinator: photo file sync failed") }
            )
        }
    }
}
