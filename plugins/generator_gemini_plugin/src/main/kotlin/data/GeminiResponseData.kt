package data

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata,
    val modelVersion: String
)

@Serializable
data class Candidate(
    val content: ResponseContent,
    val finishReason: String,
    val citationMetadata: CitationMetadata? = null,
    val avgLogprobs: Double
)

@Serializable
data class ResponseContent(
    val parts: List<Part>,
    val role: String
)

@Serializable
data class CitationMetadata(
    val citationSources: List<CitationSource>
)

@Serializable
data class CitationSource(
    val startIndex: Int,
    val endIndex: Int,
    val uri: String? = null // uri ist optional
)

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int,
    val promptTokensDetails: List<TokenDetails>,
    val candidatesTokensDetails: List<TokenDetails>
)

@Serializable
data class TokenDetails(
    val modality: String,
    val tokenCount: Int
)