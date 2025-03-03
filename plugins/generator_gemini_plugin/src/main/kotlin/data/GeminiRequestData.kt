package data

import kotlinx.serialization.Serializable

@Serializable
data class Contents(
    val role: String,
    val parts: List<Part>
)

@Serializable
data class SystemInstruct(
    val parts: Part
)

@Serializable
data class GenerationConfig(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxOutputTokens: Int,
    val responseMimeType: String
)

@Serializable
data class RequestContent(
    val system_instruction: SystemInstruct,
    val contents: List<Contents>,
    val generation_config: GenerationConfig
)
