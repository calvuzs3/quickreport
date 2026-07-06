package net.calvuz.qreport.checkup.criticality.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Domain model for a user-managed checklist criticality level master record
 * (the `criticality_levels` table) — the source of truth for display
 * (label/color/icon), and supports custom criticality levels created from
 * Settings. See [CriticalityCodes] for well-known-code comparisons.
 */
@Serializable
data class CriticalityMaster(
    val id: String,
    val code: String,
    val label: String,
    val priority: Int,
    val colorHex: String,
    val iconEmoji: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
)
