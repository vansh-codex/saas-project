package com.friday.assistant.tools

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ToolRegistryTest {

    @Test
    fun `built in registry exposes the time tool`() {
        val registry = ToolRegistry.builtIn()

        assertNotNull(registry.find("current_time"))
        assertTrue(registry.describe().contains("current_time"))
    }

    @Test
    fun `lookup ignores case and returns null for unknown tools`() {
        val registry = ToolRegistry.builtIn()

        assertNotNull(registry.find("CURRENT_TIME"))
        assertNull(registry.find("does_not_exist"))
    }

    @Test
    fun `time tool reports its injected instant`() = runTest {
        val fixedInstant = Instant.parse("2026-11-06T12:00:00Z")
        val tool = TimeTool(now = { fixedInstant }, zone = { ZoneId.of("UTC") })

        val result = tool.execute()

        assertTrue(result.contains("2026"))
    }
}
