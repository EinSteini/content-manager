package de.busesteinkamp.plugins.client

import de.busesteinkamp.domain.generator.GenAIService
import de.busesteinkamp.plugins.data.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

class GeminiClient : GenAIService {

    private val apiKey: String
    private val baseUrl: String

    init {
        val dotenv = dotenv()
        apiKey = dotenv["GEMINI_API_KEY"]
        baseUrl = dotenv["GEMINI_BASE_URL"]
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    override suspend fun sendMessage(systemPrompt: String, userInput: String): String {
        val generationConfig = GenerationConfig(
            temperature = 1f,
            topK = 64,
            topP = 0.95f,
            maxOutputTokens = 8192,
            responseMimeType = "application/json"
        )

        val body = RequestContent(
            generation_config = generationConfig,
            system_instruction = SystemInstruct(parts = Part(text = systemPrompt)),
            contents = listOf(
                Contents(
                    role = "user",
                    parts = listOf(Part(text = userInput))
                )
            )
        )

        val response = client.post(baseUrl) {
            headers {
                append("Content-Type", "application/json")
                append("x-goog-api-key", apiKey)
            }
            setBody(body)
        }

        val res = response.body<GeminiResponse>()
        val rawResponse = res.candidates[0].content.parts[0].text
        val cleanedResponse = cleanResponse(rawResponse)
        return cleanedResponse
    }
}

private fun cleanResponse(response: String): String {
    return response.replace(Regex("[\\[\\]\\n\"]"), "").trim()
}