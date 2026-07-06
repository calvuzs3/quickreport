package net.calvuz.qreport.settings.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import net.calvuz.qreport.settings.domain.model.HomePreferences
import net.calvuz.qreport.settings.domain.model.ListViewMode
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

/**
 * DataStore for general app UI preferences.
 *
 * Stores per-list view mode preferences using a key pattern:
 * "view_mode_{listKey}" -> ListViewMode name (e.g. "FULL", "COMPACT", "MINIMAL")
 *
 * List keys are defined as constants for type safety.
 */
@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // List screen keys
        const val LIST_KEY_CLIENTS = "clients"
        const val LIST_KEY_CONTACTS = "contacts"
        const val LIST_KEY_CONTRACTS = "contracts"
        const val LIST_KEY_FACILITIES = "facilities"
        const val LIST_KEY_ISLANDS = "islands"
        const val LIST_KEY_MECHANICAL_UNITS = "units"
        const val LIST_KEY_CHECKUPS = "checkups"
        const val LIST_KEY_TI = "technical_intervention"

        /** Builds the DataStore preference key for a given list. */
        private fun viewModeKey(listKey: String): Preferences.Key<String> {
            return stringPreferencesKey("view_mode_$listKey")
        }

        // Home dashboard visibility toggles
        private val HOME_SHOW_CHECK_UP_STATS = booleanPreferencesKey("home_show_check_up_stats")
        private val HOME_SHOW_CLIENT_STATS = booleanPreferencesKey("home_show_client_stats")
        private val HOME_SHOW_ISLAND_STATS = booleanPreferencesKey("home_show_island_stats")
        private val HOME_SHOW_RECENT_CHECK_UPS = booleanPreferencesKey("home_show_recent_check_ups")
        private val HOME_SHOW_RECENT_CLIENTS = booleanPreferencesKey("home_show_recent_clients")
        private val HOME_SHOW_RECENT_ISLANDS = booleanPreferencesKey("home_show_recent_islands")
    }

    /** Observe the Home dashboard visibility preferences (all default false). */
    fun getHomePreferences(): Flow<HomePreferences> {
        return context.appSettingsDataStore.data
            .catch { exception ->
                Timber.e(exception, "Error reading home preferences")
                emit(androidx.datastore.preferences.core.emptyPreferences())
            }
            .map { preferences ->
                HomePreferences(
                    showCheckUpStats = preferences[HOME_SHOW_CHECK_UP_STATS] ?: false,
                    showClientStats = preferences[HOME_SHOW_CLIENT_STATS] ?: false,
                    showIslandStats = preferences[HOME_SHOW_ISLAND_STATS] ?: false,
                    showRecentCheckUps = preferences[HOME_SHOW_RECENT_CHECK_UPS] ?: false,
                    showRecentClients = preferences[HOME_SHOW_RECENT_CLIENTS] ?: false,
                    showRecentIslands = preferences[HOME_SHOW_RECENT_ISLANDS] ?: false
                )
            }
    }
    
    suspend fun setHomeShowCheckUpStats(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_CHECK_UP_STATS] = value }
    }
    
    suspend fun setHomeShowClientStats(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_CLIENT_STATS] = value }
    }

    suspend fun setHomeShowIslandStats(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_ISLAND_STATS] = value }
    }
    
    suspend fun setHomeShowRecentCheckUps(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_RECENT_CHECK_UPS] = value }
    }
    
    suspend fun setHomeShowRecentClients(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_RECENT_CLIENTS] = value }
    }

    suspend fun setHomeShowRecentIslands(value: Boolean) {
        context.appSettingsDataStore.edit { it[HOME_SHOW_RECENT_ISLANDS] = value }
    }

    /**
     * Observe the [ListViewMode] for a specific list screen.
     *
     * @param listKey one of the LIST_KEY_* constants
     * @return Flow emitting the current view mode, defaults to [ListViewMode.DEFAULT]
     */
    fun getListViewMode(listKey: String): Flow<ListViewMode> {
        return context.appSettingsDataStore.data
            .catch { exception ->
                Timber.e(exception, "Error reading view mode for list: $listKey")
                emit(androidx.datastore.preferences.core.emptyPreferences())
            }
            .map { preferences ->
                val stored = preferences[viewModeKey(listKey)]
                ListViewMode.fromString(stored)
            }
    }

    /**
     * Update the [ListViewMode] for a specific list screen.
     *
     * @param listKey one of the LIST_KEY_* constants
     * @param mode the new view mode to persist
     */
    suspend fun setListViewMode(listKey: String, mode: ListViewMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[viewModeKey(listKey)] = mode.name
        }
        Timber.d("View mode for '$listKey' updated to ${mode.name}")
    }
}