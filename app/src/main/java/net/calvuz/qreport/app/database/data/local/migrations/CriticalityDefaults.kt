package net.calvuz.qreport.app.database.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Default `criticality_levels` content, seeded via
 * [net.calvuz.qreport.app.database.data.local.QReportDatabase.CALLBACK] on every
 * fresh install — mirrors the known [net.calvuz.qreport.checkup.criticality.domain.model.CriticalityCodes]
 * values so the table isn't empty before the first server sync pull.
 */
internal object CriticalityDefaults {

    private data class CriticalitySeed(
        val code: String,
        val label: String,
        val priority: Int,
        val colorHex: String,
        val iconEmoji: String,
        val sortOrder: Int
    )

    private val levels = listOf(
        CriticalitySeed("NA", "N/A", priority = 0, colorHex = "#9E9E9E", iconEmoji = "➖", sortOrder = 0),
        CriticalitySeed("ROUTINE", "Routine", priority = 1, colorHex = "#2196F3", iconEmoji = "🟢", sortOrder = 1),
        CriticalitySeed("IMPORTANT", "Importante", priority = 2, colorHex = "#FF9800", iconEmoji = "🟡", sortOrder = 2),
        CriticalitySeed("CRITICAL", "Critico", priority = 3, colorHex = "#F44336", iconEmoji = "🔴", sortOrder = 3)
    )

    fun seed(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        levels.forEach { seed ->
            db.execSQL(
                """
                INSERT INTO `criticality_levels`
                (`id`, `code`, `label`, `priority`, `color_hex`, `icon_emoji`, `sort_order`, `is_active`, `created_at`, `updated_at`, `synced_at`, `is_deleted`)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?, NULL, 0)
                """.trimIndent(),
                arrayOf<Any>(
                    seed.code, seed.code, seed.label, seed.priority, seed.colorHex, seed.iconEmoji,
                    seed.sortOrder, now, now
                )
            )
        }
    }
}
