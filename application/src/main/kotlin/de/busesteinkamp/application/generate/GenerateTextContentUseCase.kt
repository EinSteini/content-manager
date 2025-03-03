package de.busesteinkamp.application.generate

import de.busesteinkamp.domain.generator.Generator

class GenerateTextPostUseCase(private val generator: Generator) {
    suspend fun execute(input: String): String {
        val content = generator.generateText(input, emptyMap())
        return content
    }
}