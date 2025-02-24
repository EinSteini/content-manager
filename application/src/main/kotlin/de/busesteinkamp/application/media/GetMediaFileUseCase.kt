package de.busesteinkamp.application.media

import java.util.UUID
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaFileRepository

class GetMediaFileUseCase(private val mediaFileRepository: MediaFileRepository) {
    fun execute(id: UUID): MediaFile? {
        return mediaFileRepository.findById(id)
    }
}