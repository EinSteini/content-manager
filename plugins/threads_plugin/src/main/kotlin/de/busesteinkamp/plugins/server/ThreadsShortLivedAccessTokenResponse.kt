package de.busesteinkamp.plugins.server

import kotlinx.serialization.Serializable


@Serializable
data class ThreadsShortLivedAccessTokenResponse(val access_token: String, val user_id: String)