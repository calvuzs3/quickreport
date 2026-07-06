package net.calvuz.qreport.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import net.calvuz.qreport.settings.domain.model.HomePreferences
import net.calvuz.qreport.settings.domain.model.ListViewMode

/**
 * Repository for general app UI preferences.
 *
 * Provides per-list view mode persistence and Home dashboard visibility toggles.
 */
interface AppSettingsRepository {

    /**
     * Observe the view mode for a specific list screen.
     *
     * @param listKey identifier for the list screen
     */
    fun getListViewMode(listKey: String): Flow<ListViewMode>

    /**
     * Update the view mode for a specific list screen.
     *
     * @param listKey identifier for the list screen
     * @param mode the new view mode
     */
    suspend fun setListViewMode(listKey: String, mode: ListViewMode)

    /** Observe the Home dashboard visibility preferences. */
    fun getHomePreferences(): Flow<HomePreferences>

    suspend fun setHomeShowCheckUpStats(value: Boolean)
    suspend fun setHomeShowClientStats(value: Boolean)
    suspend fun setHomeShowIslandStats(value: Boolean)
    suspend fun setHomeShowRecentCheckUps(value: Boolean)
    suspend fun setHomeShowRecentClients(value: Boolean)
    suspend fun setHomeShowRecentIslands(value: Boolean)
}