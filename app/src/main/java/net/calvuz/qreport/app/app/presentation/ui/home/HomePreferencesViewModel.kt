package net.calvuz.qreport.app.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.calvuz.qreport.settings.domain.model.HomePreferences
import net.calvuz.qreport.settings.domain.repository.AppSettingsRepository
import javax.inject.Inject

@HiltViewModel
class HomePreferencesViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val preferences: StateFlow<HomePreferences> = appSettingsRepository.getHomePreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomePreferences()
        )

    fun setShowClientStats(value: Boolean) {
        viewModelScope.launch { appSettingsRepository.setHomeShowClientStats(value) }
    }

    fun setShowIslandStats(value: Boolean) {
        viewModelScope.launch { appSettingsRepository.setHomeShowIslandStats(value) }
    }

    fun setShowRecentClients(value: Boolean) {
        viewModelScope.launch { appSettingsRepository.setHomeShowRecentClients(value) }
    }

    fun setShowRecentIslands(value: Boolean) {
        viewModelScope.launch { appSettingsRepository.setHomeShowRecentIslands(value) }
    }
}
