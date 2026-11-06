package com.friday.assistant.ai

import com.friday.assistant.tools.ToolRegistry

/**
 * Turns a user utterance into a reply. Today it delegates straight to [GeminiClient] while
 * advertising the locally available tools in the system instruction; tool execution is the
 * natural extension point for later work.
 */
class AssistantEngine(
    private val geminiClient: GeminiClient,
    private val toolRegistry: ToolRegistry,
) {

    suspend fun ask(
        prompt: String,
        history: List<AiMessage> = emptyList(),
    ): Result<String> {
        if (prompt.isBlank()) {
            return Result.failure(AiException.EmptyResponse)
        }
        return geminiClient.generate(
            prompt = prompt,
            history = history,
            systemInstruction = buildSystemInstruction(),
        )
    }

    private fun buildSystemInstruction(): String = buildString {
        append(SYSTEM_PERSONA)
        val tools = toolRegistry.all()
        if (tools.isNotEmpty()) {
            append("\n\nLocal tools available on this device:\n")
            append(toolRegistry.describe())
        }
    }

    private companion object {
        const val SYSTEM_PERSONA =
            "You are Friday, a concise personal voice assistant running on the user's Android phone. " +
                "Answer briefly and conversationally, in the language the user speaks. " +
                "Never invent device capabilities you were not given."
    }
}
