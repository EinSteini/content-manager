package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class JpaPlatformRepository : PlatformRepository {

    private lateinit var entityManager: EntityManager
    private var entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("platform-unit")

    init {
        entityManager = entityManagerFactory.createEntityManager()
    }

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