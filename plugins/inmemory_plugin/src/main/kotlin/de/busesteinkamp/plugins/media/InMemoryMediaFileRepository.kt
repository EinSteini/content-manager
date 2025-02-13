package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository
import java.util.UUID

class InMemoryMediaFileRepository : MediaFileRepository {

    private val mediaFiles: MutableList<MediaFile> = mutableListOf()

    override fun findById(id: UUID): MediaFile? {
        return mediaFiles.find { it.id == id }
    }

    override fun save(mediaFile: MediaFile): MediaFile {
        val id = mediaFile.id ?: UUID.randomUUID()
        mediaFile.id = id
        mediaFiles.add(mediaFile)
        return mediaFile
    }

    override fun update(mediaFile: MediaFile) {
        val index = mediaFiles.indexOfFirst { it.id == mediaFile.id }
        if (index != -1) {
            mediaFiles[index] = mediaFile
        }
    }
}