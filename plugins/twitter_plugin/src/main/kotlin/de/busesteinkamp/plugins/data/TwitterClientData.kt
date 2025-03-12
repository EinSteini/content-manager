package de.busesteinkamp.plugins.data

data class TwitterApiTweetResponse(
    val data: TwitterApiTweetData,
    val errors: List<TwitterApiError>? = null
)

data class TwitterApiTweetData(
    val id: String,
    val text: String,
)

data class TwitterApiError(
    val detail: String,
    val status: Int,
    val title: String,
    val type: String
)