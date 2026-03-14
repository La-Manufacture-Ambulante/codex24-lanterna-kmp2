package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TextColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InteractiveWidgetDemoTest {
    @Test
    fun summaryReflectsInteractiveState() {
        val summary =
            renderInteractiveWidgetSummary(
                InteractiveWidgetDemoState(
                    title = "Widget",
                    notes = "one\ntwo",
                    notificationsEnabled = false,
                    theme = "Amber",
                    clicks = 3,
                    lastAction = "Primary action ran (3)",
                ),
            )

        assertTrue(summary.contains("Input: Widget"))
        assertTrue(summary.contains("Theme: Amber"))
        assertTrue(summary.contains("Notifications: muted"))
        assertTrue(summary.contains("Primary clicks: 3"))
        assertTrue(summary.contains("Notes lines: 2"))
    }

    @Test
    fun metricsStayCompactAndReadable() {
        val metrics =
            renderInteractiveWidgetMetrics(
                InteractiveWidgetDemoState(
                    title = "",
                    notes = "",
                    notificationsEnabled = true,
                    theme = "Graphite",
                    clicks = 0,
                ),
            )

        assertEquals("Theme Graphite | Dialog on | Clicks 0 | Notes 1", metrics)
    }

    @Test
    fun themesMapToDistinctPaletteDefinitions() {
        val ocean = createInteractiveWidgetTheme("Ocean").defaultDefinition
        val amber = createInteractiveWidgetTheme("Amber").defaultDefinition
        val graphite = createInteractiveWidgetTheme("Graphite").defaultDefinition

        assertEquals(TextColor.ANSI.BLUE, ocean?.normal?.background)
        assertEquals(TextColor.ANSI.YELLOW, amber?.normal?.background)
        assertEquals(TextColor.ANSI.BLACK_BRIGHT, graphite?.normal?.background)
        assertNotEquals(ocean?.normal?.background, amber?.normal?.background)
        assertNotEquals(amber?.normal?.background, graphite?.normal?.background)
    }
}
