package net.calvuz.qreport.app.database.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Default `checkup_statuses` + `checkup_status_transitions` content, seeded via
 * [net.calvuz.qreport.app.database.data.local.QReportDatabase.CALLBACK] on every
 * fresh install — Room creates the schema straight from the current `@Entity`
 * annotations and never runs a `Migration.migrate()` body, so this is one of
 * the few places master data gets populated locally (most of the rest of the
 * checkup master data set relies on the server sync pull). See also
 * [CriticalityDefaults] for the `criticality_levels` table.
 */
internal object CheckUpStatusDefaults {

    private data class StatusSeed(
        val code: String,
        val label: String,
        val colorHex: String,
        val iconEmoji: String,
        val sortOrder: Int,
        val blocksDeletion: Boolean,
        val marksCompletion: Boolean
    )

    private val statuses = listOf(
        StatusSeed("DRAFT", "Bozza", "#EEEEEE", "📝", 0, blocksDeletion = false, marksCompletion = false),
        StatusSeed("IN_PROGRESS", "In corso", "#9E9E9E", "⏳", 1, blocksDeletion = false, marksCompletion = false),
        StatusSeed("COMPLETED", "Completato", "#1976D2", "✅", 2, blocksDeletion = true, marksCompletion = true),
        StatusSeed("EXPORTED", "Esportato", "#388E3C", "📤", 3, blocksDeletion = true, marksCompletion = false),
        StatusSeed("ARCHIVED", "Archiviato", "#C49000", "📦", 4, blocksDeletion = true, marksCompletion = false)
    )

    // ARCHIVED gets no rows (terminal state).
    private val transitions = listOf(
        "DRAFT" to "IN_PROGRESS",
        "DRAFT" to "COMPLETED",
        "IN_PROGRESS" to "DRAFT",
        "IN_PROGRESS" to "COMPLETED",
        "COMPLETED" to "EXPORTED",
        "EXPORTED" to "ARCHIVED"
    )

    fun seed(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        statuses.forEach { seed ->
            db.execSQL(
                """
                INSERT INTO `checkup_statuses`
                (`id`, `code`, `label`, `color_hex`, `icon_emoji`, `sort_order`, `is_active`, `blocks_deletion`, `marks_completion`, `created_at`, `updated_at`, `synced_at`, `is_deleted`)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, NULL, 0)
                """.trimIndent(),
                arrayOf<Any>(
                    seed.code, seed.code, seed.label, seed.colorHex, seed.iconEmoji, seed.sortOrder,
                    if (seed.blocksDeletion) 1 else 0, if (seed.marksCompletion) 1 else 0, now, now
                )
            )
        }

        transitions.forEach { (from, to) ->
            db.execSQL(
                "INSERT INTO `checkup_status_transitions` (`from_status_id`, `to_status_id`) VALUES (?, ?)",
                arrayOf<Any>(from, to)
            )
        }
    }
}
