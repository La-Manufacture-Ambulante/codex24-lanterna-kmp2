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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import java.io.IOException
import java.util.Random

/**
 *
 * @author martin
 */
object ScreenRectangleTest {
    @Throws(IOException::class, InterruptedException::class)
    fun main(args: Array<String?>) {
        var useAnsiColors = false
        var useFilled = false
        var slow = false
        for (arg in args) {
            if (arg!!.equals("--ansi-colors")) {
                useAnsiColors = true
            }
            if (arg!!.equals("--filled")) {
                useFilled = true
            }
            if (arg!!.equals("--slow")) {
                slow = true
            }
        }
        val screen = TestTerminalFactory(args).createScreen()
        screen!!.startScreen()

        val textGraphics = ScreenTextGraphics(screen)
        val random = Random()

        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 1000 * 20) {
            val keyStroke = screen!!.pollInput()
            if ((keyStroke != null && (keyStroke!!.keyType == KeyType.ESCAPE || keyStroke!!.keyType == KeyType.EOF))) {
                break
            }
            screen!!.doResizeIfNecessary()
            val size = textGraphics.size
            val color: TextColor?
            if (useAnsiColors) {
                color = TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().size)]
            } else {
                // Draw a rectangle in random indexed color
                color = TextColor.Indexed(random.nextInt(256))
            }

            val topLeft = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
            val rectangleSize = TerminalSize(random.nextInt(size!!.columns - topLeft.column), random.nextInt(size!!.rows - topLeft.row))

            textGraphics.setBackgroundColor(color)
            if (useFilled) {
                textGraphics.fillRectangle(topLeft, rectangleSize, ' ')
            } else {
                textGraphics.drawRectangle(topLeft, rectangleSize, ' ')
            }
            screen!!.refresh(Screen.RefreshType.DELTA)
            if (slow) {
                Thread.sleep(500)
            }
        }
        screen!!.stopScreen()
    }
}
