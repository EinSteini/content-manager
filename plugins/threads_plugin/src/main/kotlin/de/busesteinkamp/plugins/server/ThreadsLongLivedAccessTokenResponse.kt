package de.busesteinkamp.plugins.server

import kotlinx.serialization.Serializable

@Serializable
data class ThreadsLongLivedAccessTokenResponse(val access_token: String, val token_type: String, val expires_in: Int) {
}
