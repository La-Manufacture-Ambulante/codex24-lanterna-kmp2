package com.googlecode.lanterna

import org.junit.Assert.assertEquals
import org.junit.Test
import java.awt.Color

class TextColorTest {
    @Test
    fun testFromAWTColor() {
        var rgb = TextColor.RGB.fromAWTColor(Color.BLUE)
        assertEquals(0, rgb!!.red)
        assertEquals(0, rgb!!.green)
        assertEquals(255, rgb!!.blue)
        rgb = TextColor.RGB.fromAWTColor(Color.RED)
        assertEquals(255, rgb!!.red)
        assertEquals(0, rgb!!.green)
        assertEquals(0, rgb!!.blue)
        rgb = TextColor.RGB.fromAWTColor(Color.GREEN)
        assertEquals(0, rgb!!.red)
        assertEquals(255, rgb!!.green)
        assertEquals(0, rgb!!.blue)
    }
}
