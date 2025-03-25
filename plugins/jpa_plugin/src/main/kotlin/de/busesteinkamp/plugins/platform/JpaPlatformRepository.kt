package de.busesteinkamp.plugins.platform

import de.busesteinkamp.adapters.platform.PlatformDto
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PlatformRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class JpaPlatformRepository(
    private val platformMapper: PlatformMapper
) : PlatformRepository {
    private var entityManager: EntityManager
    private var entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("platform-unit")

    init {
        entityManager = entityManagerFactory.createEntityManager()
    }

    override fun findById(id: UUID): Platform? {
        return entityManager.find(PlatformDto::class.java, id)?.let { platformMapper.toDomain(it) }
    }

    override fun findByName(name: String): Platform? {
        val query = entityManager.createQuery("SELECT p FROM PlatformDto p WHERE p.name = :name", PlatformDto::class.java)
        query.setParameter("name", name)
        return query.resultList.firstOrNull()?.let { platformMapper.toDomain(it) }
    }

    override fun findAll(): List<Platform> {
        return entityManager.createQuery("SELECT p FROM PlatformDto p", PlatformDto::class.java)
            .resultList
            .map { platformMapper.toDomain(it) }
    }

    override fun save(platform: Platform): Platform {
        val dto = platformMapper.toDto(platform)
        entityManager.transaction.begin()
        entityManager.persist(dto)
        entityManager.transaction.commit()
        return platformMapper.toDomain(dto)
    }
}