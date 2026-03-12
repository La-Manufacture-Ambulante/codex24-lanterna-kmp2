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
import com.googlecode.lanterna.graphics.BasicTextImage
import java.io.IOException

/**
 * Test to try out drawImage in TextGraphics
 */
object DrawImageTest {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        // Setup a standard Screen
        val screen = TestTerminalFactory(args).createScreen()
        screen!!.startScreen()
        screen!!.cursorPosition = null

        // Create an 'image' that we fill with recognizable characters
        val image = BasicTextImage(5, 5)
        val imageCharacter = TextCharacter('X')
        val textGraphics = image.newTextGraphics()
        textGraphics!!.drawRectangle(
            TerminalPosition.TOP_LEFT_CORNER,
            TerminalSize(5, 5),
            imageCharacter.withBackgroundColor(TextColor.ANSI.RED),
        )
        textGraphics!!.drawRectangle(
            TerminalPosition.OFFSET_1x1,
            TerminalSize(3, 3),
            imageCharacter.withBackgroundColor(TextColor.ANSI.MAGENTA),
        )
        textGraphics!!.setCharacter(
            2, 2,
            imageCharacter.withBackgroundColor(TextColor.ANSI.CYAN),
        )

        val screenGraphics = screen!!.newTextGraphics()
        screenGraphics!!.setBackgroundColor(TextColor.Indexed.fromRGB(50, 50, 50))
        screenGraphics!!.fill(' ')
        screenGraphics!!.drawImage(TerminalPosition.OFFSET_1x1, image)
        screenGraphics!!.drawImage(TerminalPosition(8, 1), image, TerminalPosition.TOP_LEFT_CORNER, image.size.withRelativeColumns(-4))
        screenGraphics!!.drawImage(TerminalPosition(10, 1), image, TerminalPosition.TOP_LEFT_CORNER, image.size.withRelativeColumns(-3))
        screenGraphics!!.drawImage(TerminalPosition(13, 1), image, TerminalPosition.TOP_LEFT_CORNER, image.size.withRelativeColumns(-2))
        screenGraphics!!.drawImage(TerminalPosition(17, 1), image, TerminalPosition.TOP_LEFT_CORNER, image.size.withRelativeColumns(-1))
        screenGraphics!!.drawImage(TerminalPosition(22, 1), image)
        screenGraphics!!.drawImage(TerminalPosition(28, 1), image, TerminalPosition(1, 0), image.size)
        screenGraphics!!.drawImage(TerminalPosition(33, 1), image, TerminalPosition(2, 0), image.size)
        screenGraphics!!.drawImage(TerminalPosition(37, 1), image, TerminalPosition(3, 0), image.size)
        screenGraphics!!.drawImage(TerminalPosition(40, 1), image, TerminalPosition(4, 0), image.size)

        // Try to draw bigger than the image size, this should ignore the extra size
        screenGraphics!!.drawImage(TerminalPosition(1, 7), image, TerminalPosition.TOP_LEFT_CORNER, image.size.withRelativeColumns(10))

        // 0 size should draw nothing
        screenGraphics!!.drawImage(TerminalPosition(8, 7), image, TerminalPosition.TOP_LEFT_CORNER, TerminalSize.ZERO)

        // Drawing with a negative source image offset will move the target position
        screenGraphics!!.drawImage(TerminalPosition(8, 7), image, TerminalPosition(-2, -2), image.size)

        screen!!.refresh()
        screen!!.readInput()
        screen!!.stopScreen()
    }
}
