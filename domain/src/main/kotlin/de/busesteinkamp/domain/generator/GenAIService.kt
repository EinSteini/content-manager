package de.busesteinkamp.domain.generator

interface GenAIService {
    suspend fun sendMessage(systemPrompt: String, userInput: String): String
}