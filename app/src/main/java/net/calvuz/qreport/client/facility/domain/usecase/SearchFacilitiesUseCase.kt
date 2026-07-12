package net.calvuz.qreport.client.facility.domain.usecase

import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.client.facility.domain.model.Facility
import net.calvuz.qreport.client.facility.domain.repository.FacilityRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Searches facilities by text query across all clients.
 *
 * Minimum query length: 2 characters.
 * Results are sorted by match quality: exact → starts-with → alphabetical.
 */
class SearchFacilitiesUseCase @Inject constructor(
    private val facilityRepository: FacilityRepository
) {
    suspend operator fun invoke(query: String): QrResult<List<Facility>, QrError.FacilityError> {

        Timber.v("Searching facilities by $query")

        val trimmed = query.trim()

        if (trimmed.length < 2) {
            Timber.w("Search query too short")
            return QrResult.Success(emptyList())
        }

        return facilityRepository.searchFacilities(trimmed).fold(onSuccess = { facilities ->
            Timber.d("Found ${facilities.size} facilities")
            QrResult.Success(facilities.sortedByRelevance(trimmed))
        }, onFailure = {
            Timber.e(it, "Failed to search facilities by $query")
            QrResult.Error(QrError.FacilityError.LoadError(it.message))
        })
    }

    private fun List<Facility>.sortedByRelevance(query: String): List<Facility> =
        sortedWith(compareBy<Facility> {
            !it.name.equals(query, ignoreCase = true)
        }.thenBy { !it.name.startsWith(query, ignoreCase = true) }
            .thenBy { it.name.lowercase() })
}
