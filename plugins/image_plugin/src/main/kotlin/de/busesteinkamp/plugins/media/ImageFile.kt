package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import java.util.*

class ImageFile(id: UUID?, filename: String, fileSize: Long, val altText: String) : MediaFile(
    id,
    filename,
    filetype = "image/jpeg",
    fileSize
){
    var fileContent: ByteArray = byteArrayOf()

    init {
        val fileEnding: String = filename.split(".").last()
        if(fileEnding == "png"){
            filetype = "image/png"
        }else if(fileEnding != "jpg" && fileEnding != "jpeg"){
            throw IllegalArgumentException("Only jpg and png files are supported")
        }
    }

    override fun loadFile() {
        fileContent = java.io.File(filename).readBytes()
    }
}