package de.busesteinkamp.plugins.data

import kotlinx.serialization.Serializable

@Serializable
data class TwitterApiTweetResponse(
    val data: TwitterApiTweetData,
    val errors: List<TwitterApiError>? = null
)

@Serializable
data class TwitterApiTweetData(
    val id: String,
    val text: String,
)

@Serializable
data class TwitterApiError(
    val detail: String,
    val status: Int,
    val title: String,
    val type: String
)

@Serializable
data class LongLivedAccessTokenResponse(val access_token: String, val token_type: String, val expires_in: Int)

@Serializable
data class ShortLivedAccessTokenResponse(val access_token: String, val refresh_token: String, val expires_in: Int)
