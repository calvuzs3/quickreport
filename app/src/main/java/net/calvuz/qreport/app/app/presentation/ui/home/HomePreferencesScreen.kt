@file:OptIn(ExperimentalMaterial3Api::class)

package net.calvuz.qreport.app.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.SettingsSection
import net.calvuz.qreport.app.app.presentation.components.SettingsSwitchItem

/**
 * Home dashboard visibility preferences — reachable from Settings and from the
 * Home header. All toggles default to false (see [net.calvuz.qreport.settings.domain.model.HomePreferences]).
 */
@Composable
fun HomePreferencesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomePreferencesViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.home_preferences_screen_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_close))
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = stringResource(R.string.home_preferences_section_clients)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.home_preferences_show_client_stats_title),
                    subtitle = stringResource(R.string.home_preferences_show_client_stats_subtitle),
                    icon = Icons.Default.QueryStats,
                    checked = preferences.showClientStats,
                    onCheckedChange = viewModel::setShowClientStats
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.home_preferences_show_recent_clients_title),
                    subtitle = stringResource(R.string.home_preferences_show_recent_clients_subtitle),
                    icon = Icons.Default.PersonSearch,
                    checked = preferences.showRecentClients,
                    onCheckedChange = viewModel::setShowRecentClients
                )
            }

            SettingsSection(title = stringResource(R.string.home_preferences_section_islands)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.home_preferences_show_island_stats_title),
                    subtitle = stringResource(R.string.home_preferences_show_island_stats_subtitle),
                    icon = Icons.Default.Widgets,
                    checked = preferences.showIslandStats,
                    onCheckedChange = viewModel::setShowIslandStats
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.home_preferences_show_recent_islands_title),
                    subtitle = stringResource(R.string.home_preferences_show_recent_islands_subtitle),
                    icon = Icons.Default.HistoryToggleOff,
                    checked = preferences.showRecentIslands,
                    onCheckedChange = viewModel::setShowRecentIslands
                )
            }
        }
    }
}
