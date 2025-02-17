package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import de.busesteinkamp.plugins.platform.ThreadsConnector
import java.util.UUID

class InMemoryPlatformRepository : PlatformRepository {

    private val platforms: MutableList<Platform> = mutableListOf()

    init {
        // Füge hier deine Plattformen hinzu
        platforms.add(ThreadsConnector(UUID.randomUUID(), "threads"))
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