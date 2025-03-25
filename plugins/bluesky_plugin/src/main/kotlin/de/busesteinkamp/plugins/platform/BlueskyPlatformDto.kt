package de.busesteinkamp.plugins.platform

import de.busesteinkamp.adapters.platform.PlatformDto
import java.util.UUID
import jakarta.persistence.*

@Entity
@DiscriminatorValue("bluesky")
class BlueskyPlatformDto (
    id: UUID? = null,
    name: String,
) : PlatformDto(id, name) {
    constructor() : this(null, "") {}
}