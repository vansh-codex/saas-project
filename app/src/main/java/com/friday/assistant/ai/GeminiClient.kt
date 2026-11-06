package com.friday.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Minimal client for the Gemini `generateContent` endpoint.
 *
 * The API key is supplied by [apiKeyProvider] on every call rather than held as a field, so
 * the client never caches a secret and always reflects the latest value from Settings.
 */
class GeminiClient(
    private val apiKeyProvider: () -> String?,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val model: String = DEFAULT_MODEL,
    private val httpClient: OkHttpClient = defaultHttpClient(),
    private val json: Json = defaultJson(),
) {

    val isConfigured: Boolean get() = !apiKeyProvider().isNullOrBlank()

    suspend fun generate(
        prompt: String,
        history: List<AiMessage> = emptyList(),
        systemInstruction: String? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(AiException.ApiKeyMissing)
        }

        val payload = GeminiRequest(
            contents = buildContents(history, prompt),
            systemInstruction = systemInstruction
                ?.takeIf { it.isNotBlank() }
                ?.let { GeminiSystemInstruction(parts = listOf(GeminiPart(text = it))) },
            generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 1024),
        )

        val request = Request.Builder()
            .url("$baseUrl/$model:generateContent")
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json")
            .post(json.encodeToString(GeminiRequest.serializer(), payload).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(AiException.RequestRejected(response.code, describeError(body)))
                }
                val parsed = json.decodeFromString(GeminiResponse.serializer(), body)
                val text = parsed.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString(separator = "")
                    ?.trim()

                when {
                    !text.isNullOrBlank() -> Result.success(text)
                    parsed.promptFeedback?.blockReason != null ->
                        Result.failure(AiException.RequestRejected(response.code, "blocked: ${parsed.promptFeedback.blockReason}"))
                    else -> Result.failure(AiException.EmptyResponse)
                }
            }
        } catch (io: IOException) {
            Result.failure(AiException.Network(io))
        }
    }

    private fun buildContents(history: List<AiMessage>, prompt: String): List<GeminiContent> {
        val turns = history.map { message ->
            GeminiContent(
                role = message.role.wireName,
                parts = listOf(GeminiPart(text = message.text)),
            )
        }
        return turns + GeminiContent(
            role = AiRole.USER.wireName,
            parts = listOf(GeminiPart(text = prompt)),
        )
    }

    private fun describeError(body: String): String =
        runCatching { json.decodeFromString(GeminiErrorEnvelope.serializer(), body).error?.message }
            .getOrNull()
            ?: body.take(MAX_ERROR_LENGTH)

    companion object {
        const val DEFAULT_BASE_URL: String = "https://generativelanguage.googleapis.com/v1beta/models"
        const val DEFAULT_MODEL: String = "gemini-2.0-flash"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MAX_ERROR_LENGTH = 300

        private fun defaultJson(): Json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        private fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
