package de.busesteinkamp.domain.process

import de.busesteinkamp.domain.user.User
import java.util.*

interface DistributionRepository {
    fun findById(id: UUID): Distribution?
    fun findAll(): List<Distribution>
    fun save(distribution: Distribution): Distribution
    fun update(distribution: Distribution)
    fun delete(distribution: Distribution)
}