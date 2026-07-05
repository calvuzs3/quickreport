package net.calvuz.qreport.app.database.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Default `checkup_statuses` + `checkup_status_transitions` content, shared between
 * [MIGRATION_7_8] (real upgrade from an existing v7 database) and
 * [net.calvuz.qreport.app.database.data.local.QReportDatabase.CALLBACK] (a genuinely
 * fresh install, where Room creates the current schema directly and never runs any
 * `Migration.migrate()` body — so without this, `checkup_status_transitions` stays
 * permanently empty on any device that hasn't gone through the 7→8 upgrade).
 *
 * Two seed entry points because `checkup_statuses` grew columns after
 * `MIGRATION_7_8`: [MIGRATION_8_9] adds `synced_at`/`is_deleted`. At v8 (inside
 * `MIGRATION_7_8` itself) those columns don't exist yet; on a fresh install the
 * table is already at the final schema, where `is_deleted` is `NOT NULL` with no
 * SQL-level `DEFAULT`, so it must be inserted explicitly.
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

    // ARCHIVED gets no rows (terminal state) — same hardcoded matrix this replaced.
    private val transitions = listOf(
        "DRAFT" to "IN_PROGRESS",
        "DRAFT" to "COMPLETED",
        "IN_PROGRESS" to "DRAFT",
        "IN_PROGRESS" to "COMPLETED",
        "COMPLETED" to "EXPORTED",
        "EXPORTED" to "ARCHIVED"
    )

    /** For [MIGRATION_7_8]: schema at v8, before [MIGRATION_8_9] adds sync columns. */
    fun seedLegacySchema(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        statuses.forEach { seed ->
            db.execSQL(
                """
                INSERT INTO `checkup_statuses`
                (`id`, `code`, `label`, `color_hex`, `icon_emoji`, `sort_order`, `is_active`, `blocks_deletion`, `marks_completion`, `created_at`, `updated_at`)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    seed.code, seed.code, seed.label, seed.colorHex, seed.iconEmoji, seed.sortOrder,
                    if (seed.blocksDeletion) 1 else 0, if (seed.marksCompletion) 1 else 0, now, now
                )
            )
        }

        insertTransitions(db)
    }

    /** For [net.calvuz.qreport.app.database.data.local.QReportDatabase.CALLBACK]: current full schema. */
    fun seedCurrentSchema(db: SupportSQLiteDatabase) {
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

        insertTransitions(db)
    }

    private fun insertTransitions(db: SupportSQLiteDatabase) {
        transitions.forEach { (from, to) ->
            db.execSQL(
                "INSERT INTO `checkup_status_transitions` (`from_status_id`, `to_status_id`) VALUES (?, ?)",
                arrayOf<Any>(from, to)
            )
        }
    }
}
