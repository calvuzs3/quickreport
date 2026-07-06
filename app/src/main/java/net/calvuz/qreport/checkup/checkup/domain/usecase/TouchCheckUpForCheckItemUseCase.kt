package net.calvuz.qreport.checkup.checkup.domain.usecase

import kotlinx.datetime.Clock
import net.calvuz.qreport.checkup.checkup.data.local.dao.CheckUpDao
import net.calvuz.qreport.checkup.items.data.local.dao.CheckItemDao
import timber.log.Timber
import javax.inject.Inject

/**
 * Marks the checkup owning [checkItemId] as pending-sync (bumps `updated_at`).
 *
 * Neither `check_items` nor `photos` are synced per-row — both are
 * wholesale-replaced alongside the check items of their owning checkup
 * whenever that checkup itself is pending-sync (see `SyncUseCase`). Their own
 * DAO updates (status, notes, photo insert/delete) never touch
 * `checkups.updated_at` on their own, so without this call
 * `CheckUpDao.getPendingSync()` would never notice the change and the sync
 * push would never include it.
 *
 * Called from [net.calvuz.qreport.checkup.items.domain.usecase.UpdateCheckItemStatusUseCase],
 * [net.calvuz.qreport.checkup.items.domain.usecase.UpdateCheckItemNotesUseCase], and the
 * photo capture/import/delete use cases.
 */
class TouchCheckUpForCheckItemUseCase @Inject constructor(
    private val checkItemDao: CheckItemDao,
    private val checkUpDao: CheckUpDao
) {
    suspend operator fun invoke(checkItemId: String) {
        val checkUpId = checkItemDao.getCheckUpIdForCheckItem(checkItemId)
        if (checkUpId == null) {
            Timber.w("TouchCheckUpForCheckItemUseCase: no checkup found for checkItem $checkItemId")
            return
        }
        checkUpDao.touchUpdatedAt(checkUpId, Clock.System.now())
    }
}
