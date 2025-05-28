package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.platform.SocialMediaPlatformRepository
import java.util.UUID

class InMemorySocialMediaPlatformRepository : SocialMediaPlatformRepository {

    private val platforms: MutableList<SocialMediaPlatform> = mutableListOf()

    override fun findById(id: UUID): SocialMediaPlatform? {
        return platforms.find { it.id == id }
    }

    override fun findByName(name: String): SocialMediaPlatform? {
        return platforms.find { it.name == name }
    }

    override fun findAll(): List<SocialMediaPlatform> {
        return platforms
    }

    override fun save(platform: SocialMediaPlatform): SocialMediaPlatform {
        val id = platform.id ?: UUID.randomUUID()
        platform.id = id
        platforms.add(platform)
        return platform
    }
}