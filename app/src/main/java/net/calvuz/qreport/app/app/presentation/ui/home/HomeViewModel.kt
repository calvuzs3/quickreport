@file:Suppress("HardCodedStringLiteral")
package net.calvuz.qreport.app.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.calvuz.qreport.R
import net.calvuz.qreport.app.error.presentation.UiText
import net.calvuz.qreport.checkup.checkup.domain.model.CheckUp
import net.calvuz.qreport.shared.codes.CheckUpStatusCodes
import net.calvuz.qreport.checkup.checkup.domain.usecase.CreateCheckUpUseCase
import net.calvuz.qreport.checkup.checkup.domain.usecase.GetCheckUpStatsUseCase
import net.calvuz.qreport.checkup.checkup.domain.usecase.GetCheckUpsUseCase
import net.calvuz.qreport.checkup.checkup.domain.usecase.ObserveCriticalCheckUpsCountUseCase
import net.calvuz.qreport.checkup.status.domain.model.CheckUpStatusMaster
import net.calvuz.qreport.checkup.status.domain.usecase.ObserveActiveCheckUpStatusesUseCase
import net.calvuz.qreport.client.client.domain.model.Client
import net.calvuz.qreport.client.client.domain.usecase.ObserveClientsUseCase
import net.calvuz.qreport.client.island.domain.model.Island
import net.calvuz.qreport.client.island.domain.model.IslandTypeMaster
import net.calvuz.qreport.client.island.domain.usecase.ObserveIslandTypesUseCase
import net.calvuz.qreport.client.island.domain.usecase.ObserveIslandsUseCase
import net.calvuz.qreport.settings.domain.model.HomePreferences
import net.calvuz.qreport.settings.domain.repository.AppSettingsRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.days


// =============================================================================
// CHECKUP SUMMARY — computed from the checkup list in the ViewModel
// =============================================================================

data class DashboardCheckupStatistics(
    val totalCheckUps: Int,
    val activeCheckUps: Int,
    val completedThisWeek: Int,
    val criticalCheckUps: Int,
    val averageCompletionTime: Int // in hours
)

data class DashboardCheckupData(
    val recentCheckUps: List<CheckUp>,
    val recentCheckUpsCriticalCounts: Map<String, Int>,
    val inProgressCheckUps: List<CheckUp>,
    val draftCheckUps: List<CheckUp>,
    val completedCheckUps: List<CheckUp>,
    val criticalCheckUps: Int
)

// =============================================================================
// CLIENT SUMMARY — computed from the client list in the ViewModel
// =============================================================================

data class DashboardClientStatistics(
    val totalClient: Int,
    val activeClient: Int
)

// =============================================================================
// ISLAND SUMMARY — computed from the island list in the ViewModel
// =============================================================================

data class DashboardIslandStatistics(
    val total: Int = 0,
    val operational: Int = 0,
    val maintenanceSoon: Int = 0   // needs maintenance within 30 days
)

// =============================================================================
// UI STATE
// =============================================================================

data class HomeUiState(
    val isLoading: Boolean = false,
    val checkupStats: DashboardCheckupStatistics? = null,
    val recentCheckUps: List<CheckUp> = emptyList(),
    val recentCheckUpsCriticalCounts: Map<String, Int> = emptyMap(),
    val inProgressCheckUps: List<CheckUp> = emptyList(),
    val selectedCheckUpId: String? = null,
    val quickCreatedCheckUpId: String? = null,

    // Clients
    val clientStats: DashboardClientStatistics? = null,
    val recentClients: List<Client> = emptyList(),

    // Islands
    val islandStats: DashboardIslandStatistics = DashboardIslandStatistics(),
    val recentIslands: List<Island> = emptyList(),
    val islandTypes: List<IslandTypeMaster> = emptyList(),
    val statusMasters: List<CheckUpStatusMaster> = emptyList(),

    // Dashboard visibility preferences
    val homePreferences: HomePreferences = HomePreferences(),

    val error: UiText? = null
)

// =============================================================================
// VIEWMODEL
// =============================================================================

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCheckUpsUseCase: GetCheckUpsUseCase,
    private val createCheckUpUseCase: CreateCheckUpUseCase,
    private val observeClientsUseCase: ObserveClientsUseCase,
    private val observeIslandsUseCase: ObserveIslandsUseCase,
    private val observeIslandTypesUseCase: ObserveIslandTypesUseCase,
    private val observeActiveCheckUpStatusesUseCase: ObserveActiveCheckUpStatusesUseCase,
    private val observeCriticalCheckUpsCountUseCase: ObserveCriticalCheckUpsCountUseCase,
    private val getCheckUpStatsUseCase: GetCheckUpStatsUseCase,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Timber.d("HomeViewModel initialized")
        loadCheckupData()
        observeClients()
        observeIslands()
        observeIslandTypes()
        observeCheckUpStatuses()
        observeHomePreferences()
    }

    // =========================================================================
    // PUBLIC
    // =========================================================================

    fun refresh() {
        loadCheckupData()
    }

    fun navigateToCheckUp(checkUpId: String) {
        _uiState.update { it.copy(selectedCheckUpId = checkUpId) }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
    fun clearSelectedCheckUp() = _uiState.update { it.copy(selectedCheckUpId = null) }

    // =========================================================================
    // CHECKUPS
    // =========================================================================

    private fun loadCheckupData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                combine(
                    loadRecentCheckUps(),
                    loadInProgressCheckUps(),
                    loadDraftCheckUps(),
                    loadCompletedCheckUps(),
                    observeCriticalCheckUpsCountUseCase()
                ) { recent, inProgress, drafts, completed, criticalCount ->
                    val recentCriticalCounts = recent.take(3).associate { checkUp ->
                        val count = getCheckUpStatsUseCase(checkUp.id).getOrNull()?.criticalIssues ?: 0
                        checkUp.id to count
                    }
                    DashboardCheckupData(
                        recentCheckUps = recent,
                        recentCheckUpsCriticalCounts = recentCriticalCounts,
                        inProgressCheckUps = inProgress,
                        draftCheckUps = drafts,
                        completedCheckUps = completed,
                        criticalCheckUps = criticalCount
                    )
                }.collect { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkupStats = DashboardCheckupStatistics(
                                totalCheckUps = data.recentCheckUps.size,
                                activeCheckUps = data.inProgressCheckUps.size + data.draftCheckUps.size,
                                completedThisWeek = data.completedCheckUps.take(10).size,
                                criticalCheckUps = data.criticalCheckUps,
                                averageCompletionTime = 0
                            ),
                            recentCheckUps = data.recentCheckUps,
                            recentCheckUpsCriticalCounts = data.recentCheckUpsCriticalCounts,
                            inProgressCheckUps = data.inProgressCheckUps,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load checkup data")
                _uiState.update {
                    it.copy(isLoading = false, error = UiText.StringResource(R.string.home_error_load))
                }
            }
        }
    }

    private fun loadRecentCheckUps(): Flow<List<CheckUp>> =
        getCheckUpsUseCase().map { it.take(5) }.catch { e -> Timber.e(e); emit(emptyList()) }

    private fun loadInProgressCheckUps(): Flow<List<CheckUp>> =
        getCheckUpsUseCase(status = CheckUpStatusCodes.IN_PROGRESS).catch { e -> Timber.e(e); emit(emptyList()) }

    private fun loadDraftCheckUps(): Flow<List<CheckUp>> =
        getCheckUpsUseCase(status = CheckUpStatusCodes.DRAFT).catch { e -> Timber.e(e); emit(emptyList()) }

    private fun loadCompletedCheckUps(): Flow<List<CheckUp>> =
        getCheckUpsUseCase(status = CheckUpStatusCodes.COMPLETED).catch { e -> Timber.e(e); emit(emptyList()) }

    // =========================================================================
    // CLIENTS
    // =========================================================================

    private fun observeClients() {
        viewModelScope.launch {
            observeClientsUseCase()
                .catch { e -> Timber.e(e, "Failed to observe clients") }
                .collect { clients ->
                    _uiState.update {
                        it.copy(
                            clientStats = DashboardClientStatistics(
                                totalClient = clients.size,
                                activeClient = clients.count { c -> c.isActive }
                            ),
                            recentClients = clients.sortedByDescending { c -> c.updatedAt }.take(3)
                        )
                    }
                }
        }
    }

    // =========================================================================
    // PREFERENCES
    // =========================================================================

    private fun observeHomePreferences() {
        viewModelScope.launch {
            appSettingsRepository.getHomePreferences()
                .catch { e -> Timber.e(e, "Failed to observe home preferences") }
                .collect { preferences ->
                    _uiState.update { it.copy(homePreferences = preferences) }
                }
        }
    }

    // =========================================================================
    // ISLANDS
    // =========================================================================

    private fun observeIslands() {
        viewModelScope.launch {
            observeIslandsUseCase()
                .catch { e -> Timber.e(e, "Failed to observe islands") }
                .collect { islands ->
                    val now = Clock.System.now()
                    val thirtyDaysFromNow = now + 30.days

                    val stats = DashboardIslandStatistics(
                        total = islands.size,
                        operational = islands.count { it.isActive && !it.needsMaintenance() },
                        maintenanceSoon = islands.count { island ->
                            // Overdue OR scheduled within 30 days
                            island.needsMaintenance() ||
                                    island.nextScheduledMaintenance?.let { it <= thirtyDaysFromNow } == true
                        }
                    )

                    val recent = islands.sortedByDescending { it.updatedAt }.take(3)

                    _uiState.update { it.copy(islandStats = stats, recentIslands = recent) }
                }
        }
    }

    private fun observeIslandTypes() {
        viewModelScope.launch {
            observeIslandTypesUseCase()
                .catch { e -> Timber.e(e, "Failed to observe island types") }
                .collect { types -> _uiState.update { it.copy(islandTypes = types) } }
        }
    }

    private fun observeCheckUpStatuses() {
        viewModelScope.launch {
            observeActiveCheckUpStatusesUseCase()
                .catch { e -> Timber.e(e, "Failed to observe checkup statuses") }
                .collect { statuses -> _uiState.update { it.copy(statusMasters = statuses) } }
        }
    }
}