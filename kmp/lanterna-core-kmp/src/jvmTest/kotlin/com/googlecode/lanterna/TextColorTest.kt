package com.googlecode.lanterna

import org.junit.Assert.assertEquals
import org.junit.Test
import java.awt.Color

class TextColorTest {
    @Test
    fun testFromAWTColor() {
        var rgb = TextColor.RGB(Color.BLUE.red, Color.BLUE.green, Color.BLUE.blue)
        assertEquals(0, rgb.red)
        assertEquals(0, rgb.green)
        assertEquals(255, rgb.blue)
        rgb = TextColor.RGB(Color.RED.red, Color.RED.green, Color.RED.blue)
        assertEquals(255, rgb.red)
        assertEquals(0, rgb.green)
        assertEquals(0, rgb.blue)
        rgb = TextColor.RGB(Color.GREEN.red, Color.GREEN.green, Color.GREEN.blue)
        assertEquals(0, rgb.red)
        assertEquals(255, rgb.green)
        assertEquals(0, rgb.blue)
    }
}
