package net.calvuz.qreport.sync.data.remote.dto

import kotlinx.serialization.Serializable

// ===== VERSION =====

/**
 * No matching server-side model was found for this response shape (likely an
 * inline route response) — kept client-local rather than moved to `:shared`.
 * Revisit if a real server-side counterpart turns up.
 */
@Serializable
data class ServerVersionDto(
    val name: String,
    val version: String
)
