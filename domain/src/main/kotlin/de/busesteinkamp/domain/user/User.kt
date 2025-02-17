package de.busesteinkamp.domain.user

import de.busesteinkamp.domain.platform.Platform
import java.util.*

class User(
    var id: UUID? = null,
    var name: String,
    var platforms: List<Platform> = emptyList(),
) {
    constructor() : this(null, "")
}