package net.calvuz.qreport.app.voice.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.calvuz.qreport.R
import net.calvuz.qreport.app.error.presentation.UiText
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.client.client.domain.model.Client
import net.calvuz.qreport.client.client.domain.usecase.SearchClientsUseCase
import net.calvuz.qreport.client.facility.domain.model.Facility
import net.calvuz.qreport.client.facility.domain.usecase.SearchFacilitiesUseCase
import net.calvuz.qreport.client.island.domain.model.Island
import net.calvuz.qreport.client.island.domain.usecase.SearchIslandsUseCase
import timber.log.Timber
import javax.inject.Inject

/** Which voice command triggered this search — determines which repository is queried. */
enum class VoiceSearchMode {
    CONTACT, FACILITY, ISLAND
}

data class VoiceSearchUiState(
    val isLoading: Boolean = false,
    val clientResults: List<Client> = emptyList(),
    val facilityResults: List<Facility> = emptyList(),
    val islandResults: List<Island> = emptyList(),
    val error: UiText? = null
)

/**
 * Landing/resolver ViewModel for App Actions voice deep links.
 *
 * Google Assistant only passes free-text spoken by the user (e.g. a company,
 * facility or island name) — it has no knowledge of our internal IDs. This ViewModel
 * resolves that text against [SearchClientsUseCase]/[SearchFacilitiesUseCase]/
 * [SearchIslandsUseCase]; the screen then auto-navigates on a single unambiguous
 * match or shows a disambiguation list.
 */
@HiltViewModel
class VoiceSearchViewModel @Inject constructor(
    private val searchClientsUseCase: SearchClientsUseCase,
    private val searchFacilitiesUseCase: SearchFacilitiesUseCase,
    private val searchIslandsUseCase: SearchIslandsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceSearchUiState())
    val uiState: StateFlow<VoiceSearchUiState> = _uiState.asStateFlow()

    fun search(mode: VoiceSearchMode, query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (mode) {
                VoiceSearchMode.CONTACT -> when (val result = searchClientsUseCase(query)) {
                    is QrResult.Success -> _uiState.update {
                        it.copy(isLoading = false, clientResults = result.data)
                    }

                    is QrResult.Error -> {
                        Timber.e("Voice contact search failed: ${result.error}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = UiText.StringResource(R.string.voice_search_error)
                            )
                        }
                    }
                }

                VoiceSearchMode.FACILITY -> when (val result = searchFacilitiesUseCase(query)) {
                    is QrResult.Success -> _uiState.update {
                        it.copy(isLoading = false, facilityResults = result.data)
                    }

                    is QrResult.Error -> {
                        Timber.e("Voice facility search failed: ${result.error}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = UiText.StringResource(R.string.voice_search_error)
                            )
                        }
                    }
                }

                VoiceSearchMode.ISLAND -> when (val result = searchIslandsUseCase(query)) {
                    is QrResult.Success -> _uiState.update {
                        it.copy(isLoading = false, islandResults = result.data)
                    }

                    is QrResult.Error -> {
                        Timber.e("Voice island search failed: ${result.error}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = UiText.StringResource(R.string.voice_search_error)
                            )
                        }
                    }
                }
            }
        }
    }
}
