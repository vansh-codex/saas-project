package com.friday.assistant.ai

/** Speaker of a turn in the conversation. */
enum class AiRole(val wireName: String) {
    USER("user"),
    ASSISTANT("model"),
}

/** A single turn of conversation handed to the model. */
data class AiMessage(
    val role: AiRole,
    val text: String,
)

/** Errors surfaced by the AI layer, already phrased for display. */
sealed class AiException(message: String) : Exception(message) {

    data object ApiKeyMissing : AiException(
        "No Gemini API key configured. Add one in Settings.",
    )

    data class RequestRejected(
        val code: Int,
        val detail: String,
    ) : AiException("The model rejected the request ($code): $detail")

    data class Network(val error: Throwable) : AiException(
        "Could not reach the model: ${error.message ?: error::class.java.simpleName}",
    )

    data object EmptyResponse : AiException("The model returned an empty response.")
}
