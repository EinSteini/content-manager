package de.busesteinkamp.domain.content

enum class ContentType(val text: String) {
    TEXT_PLAIN("text/plain"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_MULTIPLE("image/multiple"),
    VIDEO_MP4("video/mp4"),
}