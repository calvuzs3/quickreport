package net.calvuz.qreport.checkup.criticality.domain.model

/**
 * `CheckItem.criticalityId` is a plain criticality code, validated against
 * [CriticalityMaster] (table `criticality_levels`, fully user-editable from
 * Settings) rather than a fixed enum — see
 * [net.calvuz.qreport.checkup.checkup.domain.model.CheckUpStatusCodes] for the
 * same pattern applied to checkup statuses.
 *
 * These constants are *not* a closed set used for branching: they only name the
 * handful of seeded default levels that a few call sites need to target
 * explicitly (e.g. counting critical/important issues). They're safe against
 * user edits because they reference the master row's `id` (the Room primary
 * key, never exposed as editable in the management screen) — not its
 * `code`/`label`, which the user can freely rename.
 */
object CriticalityCodes {
    const val NA = "NA"
    const val ROUTINE = "ROUTINE"
    const val IMPORTANT = "IMPORTANT"
    const val CRITICAL = "CRITICAL"
}
