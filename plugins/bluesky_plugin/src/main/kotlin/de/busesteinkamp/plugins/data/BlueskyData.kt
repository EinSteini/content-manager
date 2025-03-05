package de.busesteinkamp.plugins.data

import kotlinx.serialization.Serializable

@Serializable
data class BlueskyBlob(val ref: BlueskyBlobRef, val mimeType: String, val `$type`: String, val size: Int)

@Serializable
data class BlueskyBlobRef(val `$link`: String)

@Serializable
data class BlueskyPostEmbedImages(
    val images: List<BlueskyBlobImage>,
    val `$type`: String
)

@Serializable
data class BlueskyBlobImage(val alt: String, val image: BlueskyBlob) // todo: aspect ratio missing

@Serializable
data class BlueskyFacetIndex(val byteStart: Int, val byteEnd: Int)

@Serializable
data class BlueskyFacetFeature(val `$type`: String, val did: String = "", val uri: String = "", val tag: String = "")

@Serializable
data class BlueskyFacet(val index: BlueskyFacetIndex, val features: List<BlueskyFacetFeature>)

@Serializable
data class BlueskyHandleResolve(val did: String)

data class MentionSpan(val start: Int, val end: Int, val handle: String)
data class UrlSpan(val start: Int, val end: Int, val url: String)
data class HashtagSpan(val start: Int, val end: Int, val hashtag: String)