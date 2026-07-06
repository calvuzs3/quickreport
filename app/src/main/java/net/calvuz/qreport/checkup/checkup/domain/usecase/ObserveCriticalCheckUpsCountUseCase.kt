package net.calvuz.qreport.checkup.checkup.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.calvuz.qreport.checkup.checkup.domain.repository.CheckUpRepository
import javax.inject.Inject

/**
 * Numero di check-up distinti con almeno una voce critica non risolta
 * (criticality=CRITICAL, status=NOK) — usato dalla dashboard Home.
 */
class ObserveCriticalCheckUpsCountUseCase @Inject constructor(
    private val repository: CheckUpRepository
) {
    operator fun invoke(): Flow<Int> = repository.observeCriticalCheckUpsCount()
}
