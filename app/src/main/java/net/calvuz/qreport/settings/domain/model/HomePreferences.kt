package net.calvuz.qreport.settings.domain.model

/**
 * User-controlled visibility toggles for the Home dashboard sections.
 * All default to false — the Home starts minimal (Check-up only) until the
 * user opts in from the Home Preferences screen.
 */
data class HomePreferences(
    val showCheckUpStats: Boolean = false,
    val showClientStats: Boolean = false,
    val showIslandStats: Boolean = false,
    val showRecentCheckUps: Boolean = false,
    val showRecentClients: Boolean = false,
    val showRecentIslands: Boolean = false
)
