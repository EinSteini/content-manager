package de.busesteinkamp.plugins.content

import de.busesteinkamp.adapters.content.ImageContent
import de.busesteinkamp.adapters.content.TextContent
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
        FileExtension.TXT.extension -> getTextContent()
        FileExtension.JPG.extension, FileExtension.JPEG.extension, FileExtension.PNG.extension -> getImageContent()
        else -> throw IllegalArgumentException("Only txt, jpg and png files are supported")
    }

    private fun getTextContent(): TextContent {
        val textContent = file.readText()
        return TextContent(content = textContent)
    }

    private fun getImageContent(): ImageContent {
        val fileContent = file.readBytes()
        val contentType = when (file.extension) {
            FileExtension.JPG.extension, FileExtension.JPEG.extension -> ContentType.IMAGE_JPEG
            FileExtension.PNG.extension -> ContentType.IMAGE_PNG
            else -> throw IllegalArgumentException("Only jpg and png files are supported")
        }
        return ImageContent(fileContent = fileContent, contentType = contentType)
    }

    override fun getContent(): Content {
        return content
    }
}