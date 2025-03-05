package de.busesteinkamp.domain.generator

interface Generator {
    suspend fun generateText(input: String, parameters: Map<String, String>): String
}
