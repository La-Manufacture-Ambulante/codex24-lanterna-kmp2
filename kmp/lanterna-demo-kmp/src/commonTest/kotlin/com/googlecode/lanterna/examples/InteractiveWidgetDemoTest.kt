package com.googlecode.lanterna.examples

import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun tableRowsStayStable() {
        val rows =
            interactiveWidgetTableRows(
                InteractiveWidgetDemoState(
                    title = "",
                    notes = "",
                    notificationsEnabled = true,
                    theme = "Graphite",
                    clicks = 0,
                ),
            )

        assertEquals(5, rows.size)
        assertEquals("Input", rows[0].first)
        assertEquals("<empty>", rows[0].second)
        assertEquals("Theme", rows[1].first)
        assertEquals("Graphite", rows[1].second)
        assertEquals("Notifications", rows[2].first)
        assertEquals("enabled", rows[2].second)
        assertEquals("Primary clicks", rows[3].first)
        assertEquals("0", rows[3].second)
        assertEquals("Notes lines", rows[4].first)
        assertEquals("1", rows[4].second)
    }
}
