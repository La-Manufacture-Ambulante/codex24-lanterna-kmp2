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
import java.io.IOException

/**
 *
 * @author martin
 */
class ScreenTabTest
    @Throws(InterruptedException::class, IOException::class)
    constructor(args: Array<String?>?) {
        private val screen: Screen?

        init {
            screen = TestTerminalFactory(args).createScreen()
            screen!!.startScreen()
            screen!!.cursorPosition = TerminalPosition(0, 0)
            putStrings("Trying out some tabs!")

            val now = System.currentTimeMillis()
            while (System.currentTimeMillis() - now < 20 * 1000) {
                Thread.sleep(1)
            }
            screen!!.stopScreen()
        }

        @Throws(IOException::class)
        private fun putStrings(topTitle: String?) {
            val writer = ScreenTextGraphics(screen!!)
            writer.setForegroundColor(TextColor.ANSI.DEFAULT)
            writer.setBackgroundColor(TextColor.ANSI.DEFAULT)
            writer.fill(' ')

            writer.setForegroundColor(TextColor.ANSI.DEFAULT)
            writer.setBackgroundColor(TextColor.ANSI.DEFAULT)
            writer.putString(0, 0, topTitle, SGR.BLINK)
            writer.setTabBehaviour(TabBehaviour.CONVERT_TO_ONE_SPACE)
            writer.putString(10, 1, "TabBehaviour.CONVERT_TO_ONE_SPACE:    |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.CONVERT_TO_TWO_SPACES)
            writer.putString(10, 2, "TabBehaviour.CONVERT_TO_TWO_SPACES:   |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.CONVERT_TO_THREE_SPACES)
            writer.putString(10, 3, "TabBehaviour.CONVERT_TO_THREE_SPACES: |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.CONVERT_TO_FOUR_SPACES)
            writer.putString(10, 4, "TabBehaviour.CONVERT_TO_FOUR_SPACES:  |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.CONVERT_TO_EIGHT_SPACES)
            writer.putString(10, 5, "TabBehaviour.CONVERT_TO_EIGHT_SPACES: |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4)
            writer.putString(10, 6, "TabBehaviour.ALIGN_TO_COLUMN_4:       |\t|\t|\t|\t|")
            writer.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_8)
            writer.putString(10, 7, "TabBehaviour.ALIGN_TO_COLUMN_8:       |\t|\t|\t|\t|")
            writer.putString(10, 9, "Default behaviour is: " + screen!!.tabBehaviour!!)
            writer.putString(10, 10, "Testing Screen's tab replacement:")
            writer.putString(10, 11, "XXXXXXXXXXXXXXXX")
            screen!!.setCharacter(12, 11, TextCharacter('\t'))
            screen!!.tabBehaviour = TabBehaviour.CONVERT_TO_ONE_SPACE
            screen!!.setCharacter(20, 11, TextCharacter('\t'))
            screen!!.refresh()

            // Verify
            if (!screen!!.getBackCharacter(TerminalPosition(20, 11))!!.`is`(' ')) {
                throw IllegalStateException("Expected tab to be replaced with space")
            }
            if (!screen!!.getBackCharacter(TerminalPosition(21, 11))!!.`is`('X')) {
                throw IllegalStateException("Expected X in back buffer")
            }
            if (!screen!!.getFrontCharacter(TerminalPosition(20, 11))!!.`is`(' ')) {
                throw IllegalStateException("Expected tab to be replaced with space")
            }
            if (!screen!!.getFrontCharacter(TerminalPosition(21, 11))!!.`is`('X')) {
                throw IllegalStateException("Expected X in front buffer")
            }
        }

        companion object {
            @Throws(InterruptedException::class, IOException::class)
            fun main(args: Array<String?>?) {
                ScreenTabTest(args)
            }
        }
    }
