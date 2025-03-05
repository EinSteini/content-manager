package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import java.util.*

class MultipleImageFiles : MediaFile {

    constructor(id: UUID?, imagePaths: List<String>, altTexts: List<String>) : this(id) {
        if(imagePaths.isEmpty() || altTexts.isEmpty()){
            throw IllegalArgumentException("No image paths or alt texts provided")
        }
        if(imagePaths.size != altTexts.size){
            throw IllegalArgumentException("Number of image paths and alt texts must be equal")
        }
        this.imagePaths = imagePaths
        this.altTexts = altTexts

        this.loadFile()
    }

    private constructor(id: UUID?) : super(id, "multiple_images", "image/multiple", 0)

    private var imagePaths: List<String> = emptyList()
    private var altTexts: List<String> = emptyList()
    var imageFiles: List<ImageFile> = emptyList()

    override fun loadFile() {
        imageFiles = imagePaths.mapIndexed { index, path ->
            ImageFile(UUID.randomUUID(), path, altTexts[index])
        }
        imageFiles.forEach { it.loadFile() }
        fileSize = imageFiles.sumOf { it.fileSize }
    }
}