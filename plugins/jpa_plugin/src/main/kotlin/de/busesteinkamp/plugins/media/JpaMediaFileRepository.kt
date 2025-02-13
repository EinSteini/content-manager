package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.UUID

class JpaMediaFileRepository : MediaFileRepository {

    private lateinit var entityManager: EntityManager
    private var entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("media-unit")

    init {
        entityManager = entityManagerFactory.createEntityManager()
    }

    override fun findById(id: UUID): MediaFile? {
        return entityManager.find(MediaFile::class.java, id)
    }

    override fun save(mediaFile: MediaFile): MediaFile {
        entityManager.persist(mediaFile)
        return mediaFile
    }

    override fun update(mediaFile: MediaFile) {
        entityManager.merge(mediaFile)
    }
}