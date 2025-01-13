package de.busesteinkamp.domain.media

import java.util.UUID

interface PlatformRepository {
    fun findById(id: UUID): Platform?
    fun findByName(name: String): Platform?
    fun findAll(): List<Platform>
}