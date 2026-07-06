package net.calvuz.qreport.checkup.items.domain.usecase

import net.calvuz.qreport.checkup.checkup.domain.usecase.TouchCheckUpForCheckItemUseCase
import net.calvuz.qreport.checkup.items.domain.model.CheckItemStatus
import net.calvuz.qreport.checkup.items.domain.repository.CheckItemRepository
import javax.inject.Inject

/**
 * Use Case per aggiornare lo status di un check item
 *
 * Gestisce:
 * - Aggiornamento status con timestamp
 * - Validazione del nuovo status
 * - Persistenza nel database
 */
class UpdateCheckItemStatusUseCase @Inject constructor(
    private val checkItemRepository: CheckItemRepository,
    private val touchCheckUpForCheckItem: TouchCheckUpForCheckItemUseCase
) {
    suspend operator fun invoke(
        itemId: String,
        newStatus: CheckItemStatus
    ): Result<Unit> {
        return try {
            // Aggiorna lo status del check item
            checkItemRepository.updateCheckItemStatus(itemId, newStatus)
            // check_items non è sincronizzato per-riga: il checkup proprietario
            // va marcato pending-sync a mano, altrimenti SyncUseCase non
            // includerà mai questa modifica nel prossimo push.
            touchCheckUpForCheckItem(itemId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}