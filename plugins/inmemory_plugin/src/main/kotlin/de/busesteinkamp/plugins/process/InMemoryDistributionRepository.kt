package de.busesteinkamp.plugins.process

import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import java.util.*

class InMemoryDistributionRepository : DistributionRepository {
    private val distributions = mutableMapOf<UUID, Distribution>()

    override fun findById(id: UUID): Distribution? {
        return distributions[id]
    }

    override fun save(distribution: Distribution): Distribution {
        distributions[distribution.id] = distribution
        return distribution
    }

    override fun update(distribution: Distribution) {
        distributions[distribution.id] = distribution
    }

    override fun delete(distribution: Distribution) {
        distributions.remove(distribution.id)
    }
}