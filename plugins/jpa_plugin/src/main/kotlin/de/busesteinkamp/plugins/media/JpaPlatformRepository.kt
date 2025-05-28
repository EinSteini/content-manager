package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.platform.SocialMediaPlatform
import de.busesteinkamp.domain.platform.SocialMediaPlatformRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class JpaPlatformRepository : SocialMediaPlatformRepository {

    private lateinit var entityManager: EntityManager
    private var entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("platform-unit")

    init {
        entityManager = entityManagerFactory.createEntityManager()
    }

    override fun findById(id: UUID): SocialMediaPlatform? {
        return entityManager.find(SocialMediaPlatform::class.java, id)
    }

    override fun findByName(name: String): SocialMediaPlatform? {
        val query = entityManager.createQuery("SELECT p FROM SocialMediaPlatform p WHERE p.name = :name", SocialMediaPlatform::class.java)
        query.setParameter("name", name)
        return query.resultList.firstOrNull()
    }

    override fun findAll(): List<SocialMediaPlatform> {
        return entityManager.createQuery("SELECT p FROM SocialMediaPlatform p", SocialMediaPlatform::class.java).resultList
    }

    override fun save(platform: SocialMediaPlatform): SocialMediaPlatform {
        TODO("Not yet implemented")
    }
}