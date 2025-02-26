package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import java.util.UUID

class InMemoryPlatformRepository : PlatformRepository {

    private val platforms: MutableList<Platform> = mutableListOf()

    override fun findById(id: UUID): Platform? {
        return platforms.find { it.id == id }
    }

    override fun findByName(name: String): Platform? {
        return platforms.find { it.name == name }
    }

    override fun findAll(): List<Platform> {
        return platforms
    }

    override fun save(platform: Platform): Platform {
        val id = platform.id ?: UUID.randomUUID()
        platform.id = id
        platforms.add(platform)
        return platform
    }
}