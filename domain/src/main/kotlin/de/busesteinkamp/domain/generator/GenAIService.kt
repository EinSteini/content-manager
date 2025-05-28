package de.busesteinkamp.domain.generator

/**
 * Domain Service interface for AI-powered text generation.
 * This service encapsulates the domain logic for generating content using AI,
 * while abstracting away the specific AI implementation details.
 * Domain Services contain domain logic that doesn't naturally fit within an Entity or Value Object.
 */
interface GenAIService {
    /**
     * Generates text content based on system prompt and user input.
     * 
     * @param systemPrompt The system-level instructions for the AI
     * @param userInput The user's input or request
     * @return Generated text content
     */
    suspend fun sendMessage(systemPrompt: String, userInput: String): String
}