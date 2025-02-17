package de.busesteinkamp.domain.platform

import java.util.UUID

interface PlatformRepository {
    fun findById(id: UUID): Platform?
    fun findByName(name: String): Platform?
    fun findAll(): List<Platform>
}