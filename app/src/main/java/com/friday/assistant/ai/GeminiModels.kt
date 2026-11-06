package com.friday.assistant.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Wire models for the Gemini `generateContent` REST API.
 * Only the fields Friday actually uses are modelled; unknown keys are ignored by the parser.
 */

@Serializable
data class GeminiPart(
    val text: String,
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart> = emptyList(),
)

@Serializable
data class GeminiSystemInstruction(
    val parts: List<GeminiPart> = emptyList(),
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int? = null,
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig? = null,
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
    @SerialName("promptFeedback") val promptFeedback: GeminiPromptFeedback? = null,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String? = null,
)

@Serializable
data class GeminiPromptFeedback(
    @SerialName("blockReason") val blockReason: String? = null,
)

@Serializable
data class GeminiErrorEnvelope(
    val error: GeminiError? = null,
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
)
