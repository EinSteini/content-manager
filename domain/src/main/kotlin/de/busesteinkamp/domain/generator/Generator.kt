package de.busesteinkamp.domain.generator

import de.busesteinkamp.domain.content.ContentProvider

interface Generator : ContentProvider {
    suspend fun generateText(input: String, parameters: Map<String, String>)
}
