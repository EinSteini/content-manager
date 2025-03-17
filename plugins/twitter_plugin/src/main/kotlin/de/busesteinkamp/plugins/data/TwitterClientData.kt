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