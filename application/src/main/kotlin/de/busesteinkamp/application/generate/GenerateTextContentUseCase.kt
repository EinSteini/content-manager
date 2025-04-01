package de.busesteinkamp.application.generate

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.generator.Generator

class GenerateTextContentUseCase(private val generator: Generator) {
    suspend fun execute(input: String): Content {
        generator.generateText(input, emptyMap())
        if(generator.getContent().get() !is String) {
            throw IllegalStateException("Generator did not produce any text content")
        }else{
            return generator.getContent()
        }
    }
}