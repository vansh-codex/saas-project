package com.friday.assistant.tools

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reports the device's current local date and time.
 *
 * `java.time` is used directly because `minSdk` is 29.
 */
class TimeTool(
    private val now: () -> Instant = { Instant.now() },
    private val zone: () -> ZoneId = { ZoneId.systemDefault() },
) : Tool {

    override val name: String = "current_time"

    override val description: String =
        "Returns the current local date and time on the user's device."

    override suspend fun execute(arguments: Map<String, String>): String =
        FORMATTER.format(now().atZone(zone()))

    private companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm:ss")
    }
}
