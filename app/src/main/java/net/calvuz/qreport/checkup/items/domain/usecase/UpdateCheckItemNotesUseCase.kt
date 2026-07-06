package net.calvuz.qreport.checkup.items.domain.usecase

import net.calvuz.qreport.checkup.checkup.domain.usecase.TouchCheckUpForCheckItemUseCase
import net.calvuz.qreport.checkup.items.domain.repository.CheckItemRepository
import javax.inject.Inject

/**
 * Use Case per aggiornare le note di un check item
 *
 * Gestisce:
 * - Aggiornamento note con validazione
 * - Persistenza nel database
 * - Gestione errori
 */
class UpdateCheckItemNotesUseCase @Inject constructor(
    private val checkItemRepository: CheckItemRepository,
    private val touchCheckUpForCheckItem: TouchCheckUpForCheckItemUseCase
) {
    suspend operator fun invoke(
        itemId: String,
        notes: String
    ): Result<Unit> {
        return try {
            // Trim delle note per rimuovere spazi vuoti
            val cleanedNotes = notes.trim()

            // Aggiorna le note del check item
            checkItemRepository.updateCheckItemNotes(itemId, cleanedNotes)
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