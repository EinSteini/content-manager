package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.media.MediaType
import java.util.*

class ImageFile(id: UUID?, filename: String, val altText: String) : MediaFile(
    id,
    filename,
    filetype = MediaType.IMAGE_JPEG,
    fileSize = 0
){
    var fileContent: ByteArray = byteArrayOf()

    init {
        val fileEnding: String = filename.split(".").last()
        if(fileEnding == "png"){
            filetype = MediaType.IMAGE_PNG
        }else if(fileEnding != "jpg" && fileEnding != "jpeg"){
            throw IllegalArgumentException("Only jpg and png files are supported")
        }
        this.loadFile()
    }

    override fun loadFile() {
        fileContent = java.io.File(filename).readBytes()
        fileSize = fileContent.size.toLong()
    }
}