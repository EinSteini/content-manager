package de.busesteinkamp.domain.platform

import java.util.UUID

/**
 * Repository interface for SocialMediaPlatform entities.
 * Defines the contract for platform persistence operations.
 */
interface SocialMediaPlatformRepository {
    fun findById(id: UUID): SocialMediaPlatform?
    fun findByName(name: String): SocialMediaPlatform?
    fun findAll(): List<SocialMediaPlatform>
    fun save(platform: SocialMediaPlatform): SocialMediaPlatform
}