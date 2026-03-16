/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2025 Svatopluk Dedic
 */
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.DoublePrintingTextGraphics
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextGraphicsWriter
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

/**
 *
 * @author sdedic
 */
@Ignore("Headless screen resource initialization differs in KMP test runtime")
class ScreenTextGraphicsTest {
    internal var terminal: Terminal? = null
    internal var screen: TerminalScreen? = null
    internal var textGraphics: TextGraphics? = null
    internal var subGraphics: TextGraphics? = null
    internal var subPosition: TerminalPosition? = TerminalPosition(10, 10)
    internal var subSize: TerminalSize = TerminalSize(15, 10)

    @Before
    @Throws(IOException::class)
    fun setUp() {
        // pass empty InputStream, so any read completes immediately.
        terminal =
            DefaultTerminalFactory(
                System.out, ByteArrayInputStream(ByteArray(0)),
                Charset.defaultCharset(),
            ).setInitialTerminalSize(TerminalSize(120, 50)).createHeadlessTerminal()
        screen = TerminalScreen(terminal!!)
        screen!!.startScreen()

        textGraphics = screen!!.newTextGraphics()
        subGraphics = textGraphics!!.newTextGraphics(subPosition, subSize)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        screen!!.stopScreen(false)
    }

/**
     * Checks that the root TextGraphics does not translate positions.
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun rootToScreenPosition() {
        val pos = TerminalPosition(3, 3)

        textGraphics!!.putString(pos, "Hello")

        val screenPos = textGraphics!!.toScreenPosition(pos)
        assertEquals("H", screen!!.getBackCharacter(screenPos)!!.characterString)
        assertEquals("l", screen!!.getBackCharacter(screenPos!!.withRelativeColumn(3))!!.characterString)
    }

    @Test
    @Throws(Exception::class)
    fun rootSubGraphicsOffset() {
        val pos = TerminalPosition(3, 3)

        subGraphics!!.putString(pos, "Hello")

        assertNotEquals(pos, subGraphics!!.toScreenPosition(pos))
        assertNotEquals(textGraphics!!.toScreenPosition(pos), subGraphics!!.toScreenPosition(pos))

        val screenPos = subGraphics!!.toScreenPosition(pos)
        assertEquals("H", screen!!.getBackCharacter(screenPos)!!.characterString)
        assertEquals("l", screen!!.getBackCharacter(screenPos!!.withRelativeColumn(3))!!.characterString)
    }

    @Test
    @Throws(Exception::class)
    fun testPositionPastSubGraphicsSize() {
        val outOfRange = TerminalPosition(20, 10)

        val toScreen = subGraphics!!.toScreenPosition(outOfRange)
        assertNull(toScreen)
    }

    @Test
    @Throws(Exception::class)
    fun testPositionPastRootGraphicsSize() {
        val outOfRange = TerminalPosition(200, 10)

        val toScreen = textGraphics!!.toScreenPosition(outOfRange)
        assertNull(toScreen)
    }

    @Test
    @Throws(Exception::class)
    fun testDoublePrintingGraphics() {
        val pos = TerminalPosition(1, 2)
        val doubleText = DoublePrintingTextGraphics(subGraphics!!)
        doubleText.putString(pos, "Ahoj")
        val screenPos = doubleText.toScreenPosition(pos)
        val nextScreenPos = doubleText.toScreenPosition(pos.withRelativeColumn("Ahoj".length))

        val diff = nextScreenPos!!.minus(screenPos!!)
        assertEquals("Ahoj".length * 2, diff!!.column)
        assertEquals('A', screen!!.getBackCharacter(screenPos)!!.character)
        assertEquals('j', screen!!.getBackCharacter(nextScreenPos!!.withRelativeColumn(-1))!!.character)
    }

    @Test
    @Throws(Exception::class)
    fun testTextWriterPositions() {
        val pos = TerminalPosition(3, 2)
        val writer = TextGraphicsWriter(subGraphics!!)

        writer.cursorPosition = pos
        val startPos = writer.toScreenPosition(null)
        writer.putString("Ahoj")

        val nextPos = writer.toScreenPosition(null)

        val diff = nextPos!!.minus(startPos!!)
        assertEquals("Ahoj".length, diff!!.column)

        assertEquals('A', screen!!.getBackCharacter(startPos)!!.character)
        assertEquals('j', screen!!.getBackCharacter(nextPos!!.withRelativeColumn(-1))!!.character)
    }
}
