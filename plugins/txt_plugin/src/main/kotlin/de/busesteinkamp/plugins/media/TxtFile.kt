package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaType
import de.busesteinkamp.domain.process.UploadStatus
import java.util.*

class TxtFile(id: UUID?, filename: String, fileSize: Long) : MediaFile(
    id,
    filename,
    filetype = MediaType.TEXT_PLAIN,
    fileSize
) {
    var textContent: String = ""

    init {
        this.loadFile()
    }

    override fun loadFile() {
        textContent = java.io.File(filename).readText()
        fileSize = textContent.length.toLong()
    }
}