package de.busesteinkamp.domain.platform

import java.time.Instant
import java.util.*

class PublishParameters(
    var id: UUID? = null,
) {
    var publishDate: Date = Date.from(Instant.now())
    var title: String = ""
    var description: String = ""
}