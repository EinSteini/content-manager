package de.busesteinkamp.plugins.content

import de.busesteinkamp.adapters.content.ImageContent
import de.busesteinkamp.adapters.content.TxtContent
import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentProvider
import de.busesteinkamp.domain.content.ContentType
import java.io.IOException

class ContentFileReader(path: String) : ContentProvider {
    private val file: java.io.File = java.io.File(path)

    init{
        if (!file.exists()) {
            throw IOException("File does not exist: $path")
        }
        if (!file.isFile) {
            throw IOException("Not a valid file: $path")
        }
        if (!file.canRead()) {
            throw IOException("Cannot read file: $path")
        }
    }

    private val content: Content = when (file.extension) {
        "txt" -> getTxtContent()
        "jpg", "jpeg", "png" -> getImageContent()
        else -> throw IllegalArgumentException("Only txt files are supported")
    }

    private fun getTxtContent(): TxtContent {
        val textContent = file.readText()
        return TxtContent(content = textContent)
    }

    private fun getImageContent(): ImageContent {
        val fileContent = file.readBytes()
        val contentType = when (file.extension) {
            "jpg", "jpeg" -> ContentType.IMAGE_JPEG
            "png" -> ContentType.IMAGE_PNG
            else -> throw IllegalArgumentException("Only jpg and png files are supported")
        }
        return ImageContent(fileContent = fileContent, contentType = contentType)
    }

    override fun getContent(): Content {
        return content
    }
}