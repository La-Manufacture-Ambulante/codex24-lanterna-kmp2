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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import java.io.IOException
import java.util.Random

/**
 *
 * @author martin
 */
object ScreenLineTest {
    private var CIRCLE_LAST_POSITION: TerminalPosition? = null

    @Throws(IOException::class, InterruptedException::class)
    fun main(args: Array<String?>) {
        var useAnsiColors = false
        var slow = false
        var circle = false
        for (arg in args) {
            if (arg!!.equals("--ansi-colors")) {
                useAnsiColors = true
            }
            if (arg!!.equals("--slow")) {
                slow = true
            }
            if (arg!!.equals("--circle")) {
                circle = true
            }
        }
        val screen = TestTerminalFactory(args).createScreen()
        screen!!.startScreen()

        val textGraphics = ScreenTextGraphics(screen)
        val random = Random()
        while (true) {
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

            val p1: TerminalPosition?
            val p2: TerminalPosition?
            if (circle) {
                p1 = TerminalPosition(size!!.columns / 2, size!!.rows / 2)
                if (CIRCLE_LAST_POSITION == null) {
                    CIRCLE_LAST_POSITION = TerminalPosition(0, 0)
                } else if (CIRCLE_LAST_POSITION!!.row == 0) {
                    if (CIRCLE_LAST_POSITION!!.column < size!!.columns - 1) {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeColumn(1)
                    } else {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeRow(1)
                    }
                } else if (CIRCLE_LAST_POSITION!!.row < size!!.rows - 1) {
                    if (CIRCLE_LAST_POSITION!!.column == 0) {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeRow(-1)
                    } else {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeRow(1)
                    }
                } else {
                    if (CIRCLE_LAST_POSITION!!.column > 0) {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeColumn(-1)
                    } else {
                        CIRCLE_LAST_POSITION = CIRCLE_LAST_POSITION!!.withRelativeRow(-1)
                    }
                }
                p2 = CIRCLE_LAST_POSITION
            } else {
                p1 = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
                p2 = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
            }
            textGraphics.setBackgroundColor(color)
            textGraphics.drawLine(p1, p2, ' ')
            textGraphics.setBackgroundColor(TextColor.ANSI.BLACK)
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
            textGraphics.putString(4, size!!.rows - 1, "P1 " + p1 + " -> P2 " + p2)
            screen!!.refresh(Screen.RefreshType.DELTA)
            if (slow) {
                Thread.sleep(500)
            }
        }
        screen!!.stopScreen()
    }
}
