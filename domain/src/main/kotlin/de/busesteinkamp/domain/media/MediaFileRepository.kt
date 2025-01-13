package de.busesteinkamp.domain.media

import java.util.UUID

interface MediaFileRepository {
    fun findById(id: UUID): MediaFile?
    fun save(mediaFile: MediaFile): MediaFile
    fun update(mediaFile: MediaFile)
}