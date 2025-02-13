package de.busesteinkamp.de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.Platform
import de.busesteinkamp.domain.media.PlatformRepository
import java.util.UUID

class InMemoryPlatformRepository : PlatformRepository {

    private val platforms: MutableList<Platform> = mutableListOf()

    init {
        // Füge hier deine Plattformen hinzu
        platforms.add(Platform(UUID.randomUUID(), "YouTube", "https://www.youtube.com/"))
        platforms.add(Platform(UUID.randomUUID(), "Instagram", "https://www.instagram.com/"))
        // ...
    }

    override fun findById(id: UUID): Platform? {
        return platforms.find { it.id == id }
    }

    override fun findByName(name: String): Platform? {
        return platforms.find { it.name == name }
    }

    override fun findAll(): List<Platform> {
        return platforms
    }
}