package net.calvuz.qreport.sync.domain.usecase

import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.backup.domain.repository.DatabaseExportRepository
import net.calvuz.qreport.sync.domain.model.SyncResult
import net.calvuz.qreport.sync.domain.repository.SyncRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Wipes all local data and re-downloads everything from the server.
 *
 * Unlike a plain full sync (which only resets the sync timestamp and pulls
 * on top of whatever is already on the device), this clears every local
 * table first — so stale or corrupted local records can't survive the pull
 * and the device ends up an exact mirror of the server. Local changes never
 * pushed to the server are lost; the caller is responsible for confirming
 * this with the user first.
 *
 * Note: like [SyncUseCase], this only syncs entity data — the caller is
 * responsible for triggering file transfer afterwards (see
 * [net.calvuz.qreport.sync.app.FileSyncCoordinator]).
 */
class ResetAndResyncUseCase @Inject constructor(
    private val databaseExportRepository: DatabaseExportRepository,
    private val syncRepository: SyncRepository,
    private val syncUseCase: SyncUseCase
) {
    suspend operator fun invoke(): QrResult<SyncResult, QrError> {
        Timber.w("ResetAndResyncUseCase: wiping all local data before full resync")

        databaseExportRepository.clearAllTables().onFailure { e ->
            Timber.e(e, "ResetAndResyncUseCase: failed to clear local data")
            return QrResult.Error(QrError.SystemError.UnknownError(e as? Exception))
        }

        syncRepository.resetLastSyncTimestamp()

        val result = syncUseCase()
        if (result is QrResult.Error) {
            Timber.e("ResetAndResyncUseCase: resync after wipe failed: ${result.error}")
        } else {
            Timber.d("ResetAndResyncUseCase: reset + resync completed")
        }
        return result
    }
}
