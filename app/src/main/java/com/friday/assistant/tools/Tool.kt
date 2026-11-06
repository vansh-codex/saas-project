package com.friday.assistant.tools

/**
 * A capability Friday can perform locally on the device.
 *
 * Implementations must be side-effect explicit and return a short, model-readable result.
 */
interface Tool {

    /** Stable identifier used when the model asks for a tool by name. */
    val name: String

    /** One-line description surfaced to the model. */
    val description: String

    /** Runs the tool and returns its textual result. */
    suspend fun execute(arguments: Map<String, String> = emptyMap()): String
}
