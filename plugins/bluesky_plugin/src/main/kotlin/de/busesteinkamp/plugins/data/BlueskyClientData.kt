package de.busesteinkamp.plugins.data

import kotlinx.serialization.Serializable

@Serializable
data class BlueskyAuthResponse(val accessJwt: String, val refreshJwt: String)

@Serializable
data class BlueskyUploadResponse(val blob: BlueskyBlob)

@Serializable
data class BlueskyAuthRequest(val identifier: String, val password: String)

@Serializable
data class BlueskyPostRecord(val text: String, val createdAt: String, val embed: BlueskyPostEmbedImages? = null, val `$type`: String = "app.bsky.feed.post", val facets: List<BlueskyFacet> = emptyList())

@Serializable
data class BlueskyCreatePostRequest(
    val collection: String,
    val repo: String,
    val record: BlueskyPostRecord
)