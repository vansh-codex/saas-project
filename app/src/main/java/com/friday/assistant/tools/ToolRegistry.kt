package com.friday.assistant.tools

/**
 * Holds every [Tool] Friday can call, in the order they should be advertised to the model.
 */
class ToolRegistry(private val tools: List<Tool>) {

    fun all(): List<Tool> = tools

    fun find(name: String): Tool? = tools.firstOrNull { it.name.equals(name, ignoreCase = true) }

    /** Human-readable listing injected into the model's system instruction. */
    fun describe(): String = tools.joinToString(separator = "\n") { "- ${it.name}: ${it.description}" }

    companion object {
        fun builtIn(): ToolRegistry = ToolRegistry(
            tools = listOf(
                TimeTool(),
            ),
        )
    }
}
