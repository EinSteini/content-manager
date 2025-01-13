package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaMediaFileRepository : MediaFileRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

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