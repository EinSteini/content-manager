package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.Platform
import de.busesteinkamp.domain.media.PlatformRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class JpaPlatformRepository : PlatformRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findById(id: UUID): Platform? {
        return entityManager.find(Platform::class.java, id)
    }

    override fun findByName(name: String): Platform? {
        val query = entityManager.createQuery("SELECT p FROM Platform p WHERE p.name = :name", Platform::class.java)
        query.setParameter("name", name)
        return query.resultList.firstOrNull()
    }

    override fun findAll(): List<Platform> {
        return entityManager.createQuery("SELECT p FROM Platform p", Platform::class.java).resultList
    }
}