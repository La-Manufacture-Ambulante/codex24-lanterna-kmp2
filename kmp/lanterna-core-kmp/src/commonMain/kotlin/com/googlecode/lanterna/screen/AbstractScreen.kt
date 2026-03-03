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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextImage
import java.io.IOException

/**
 * This class implements some of the Screen logic that is not directly tied to the actual implementation of how the
 * Screen translate to the terminal. It keeps data structures for the front- and back buffers, the cursor location and
 * some other simpler states.
 * @author martin
 */
abstract class AbstractScreen : Screen {
    private var cursorPosition: TerminalPosition?
    private var backBuffer: ScreenBuffer
    private var frontBuffer: ScreenBuffer
    private val defaultCharacter: TextCharacter

    //How to deal with \t characters
    private var tabBehaviour: TabBehaviour

    //Current size of the screen
    private var terminalSize: TerminalSize

    //Pending resize of the screen
    private var latestResizeRequest: TerminalSize?

    constructor(initialSize: TerminalSize) : this(initialSize, Screen.DEFAULT_CHARACTER)

    /**
     * Creates a new Screen on top of a supplied terminal, will query the terminal for its size. The screen is initially
     * blank. You can specify which character you wish to be used to fill the screen initially; this will also be the
     * character used if the terminal is enlarged and you don't set anything on the new areas.
     *
     * @param initialSize Size to initially create the Screen with (can be resized later)
     * @param defaultCharacter What character to use for the initial state of the screen and expanded areas
     */
    @Suppress("SameParameterValue", "WeakerAccess")
    constructor(initialSize: TerminalSize, defaultCharacter: TextCharacter) {
        this.frontBuffer = ScreenBuffer(initialSize, defaultCharacter)
        this.backBuffer = ScreenBuffer(initialSize, defaultCharacter)
        this.defaultCharacter = defaultCharacter
        this.cursorPosition = TerminalPosition(0, 0)
        this.tabBehaviour = TabBehaviour.ALIGN_TO_COLUMN_4
        this.terminalSize = initialSize
        this.latestResizeRequest = null
    }

    /**
     * @return Position where the cursor will be located after the screen has been refreshed or `null` if the
     * cursor is not visible
     */
    override fun getCursorPosition(): TerminalPosition? {
        return cursorPosition
    }

    /**
     * Moves the current cursor position or hides it. If the cursor is hidden and given a new position, it will be
     * visible after this method call.
     *
     * @param position 0-indexed column and row numbers of the new position, or if `null`, hides the cursor
     */
    override fun setCursorPosition(position: TerminalPosition?) {
        if (position == null) {
            //Skip any validation checks if we just want to hide the cursor
            this.cursorPosition = null
            return
        }
        var adjusted = position
        if (adjusted.column < 0) {
            adjusted = adjusted.withColumn(0)
        }
        if (adjusted.row < 0) {
            adjusted = adjusted.withRow(0)
        }
        if (adjusted.column >= terminalSize.columns) {
            adjusted = adjusted.withColumn(terminalSize.columns - 1)
        }
        if (adjusted.row >= terminalSize.rows) {
            adjusted = adjusted.withRow(terminalSize.rows - 1)
        }
        this.cursorPosition = adjusted
    }

    override fun setTabBehaviour(tabBehaviour: TabBehaviour?) {
        if (tabBehaviour != null) {
            this.tabBehaviour = tabBehaviour
        }
    }

    override fun getTabBehaviour(): TabBehaviour {
        return tabBehaviour
    }

    override fun setCharacter(position: TerminalPosition, screenCharacter: TextCharacter) {
        setCharacter(position.column, position.row, screenCharacter)
    }

    override fun newTextGraphics(): TextGraphics {
        return object : ScreenTextGraphics(this) {
            override fun drawImage(
                topLeft: TerminalPosition,
                image: TextImage,
                sourceImageTopLeft: TerminalPosition,
                sourceImageSize: TerminalSize
            ): TextGraphics {
                backBuffer.copyFrom(
                    image,
                    sourceImageTopLeft.row,
                    sourceImageSize.rows,
                    sourceImageTopLeft.column,
                    sourceImageSize.columns,
                    topLeft.row,
                    topLeft.column
                )
                return this
            }
        }
    }

    @Synchronized
    override fun setCharacter(column: Int, row: Int, screenCharacter: TextCharacter) {
        //It would be nice if we didn't have to care about tabs at this level, but we have no such luxury
        if (screenCharacter.`is`('\t')) {
            //Swap out the tab for a space
            val expandedCharacter = screenCharacter.withCharacter(' ')

            //Now see how many times we have to put spaces...
            for (i in 0 until tabBehaviour.replaceTabs("\t", column).length) {
                backBuffer.setCharacterAt(column + i, row, expandedCharacter)
            }
        } else {
            //This is the normal case, no special character
            backBuffer.setCharacterAt(column, row, screenCharacter)
        }
    }

    @Synchronized
    override fun getFrontCharacter(position: TerminalPosition): TextCharacter {
        return getFrontCharacter(position.column, position.row)
    }

    override fun getFrontCharacter(column: Int, row: Int): TextCharacter {
        return getCharacterFromBuffer(frontBuffer, column, row)
    }

    @Synchronized
    override fun getBackCharacter(position: TerminalPosition): TextCharacter {
        return getBackCharacter(position.column, position.row)
    }

    override fun getBackCharacter(column: Int, row: Int): TextCharacter {
        return getCharacterFromBuffer(backBuffer, column, row)
    }

    @Throws(IOException::class)
    override fun refresh() {
        refresh(RefreshType.AUTOMATIC)
    }

    @Throws(IOException::class)
    override fun close() {
        stopScreen()
    }

    @Synchronized
    override fun clear() {
        backBuffer.setAll(defaultCharacter)
    }

    @Synchronized
    override fun doResizeIfNecessary(): TerminalSize? {
        val pendingResize = getAndClearPendingResize()
        if (pendingResize == null) {
            return null
        }

        backBuffer = backBuffer.resize(pendingResize, defaultCharacter)
        frontBuffer = frontBuffer.resize(pendingResize, defaultCharacter)
        return pendingResize
    }

    override fun getTerminalSize(): TerminalSize {
        return terminalSize
    }

    /**
     * Returns the front buffer connected to this screen, don't use this unless you know what you are doing!
     * @return This Screen's front buffer
     */
    protected open fun getFrontBuffer(): ScreenBuffer {
        return frontBuffer
    }

    /**
     * Returns the back buffer connected to this screen, don't use this unless you know what you are doing!
     * @return This Screen's back buffer
     */
    protected open fun getBackBuffer(): ScreenBuffer {
        return backBuffer
    }

    @Synchronized
    private fun getAndClearPendingResize(): TerminalSize? {
        val pending = latestResizeRequest
        if (pending != null) {
            terminalSize = pending
            latestResizeRequest = null
            return terminalSize
        }
        return null
    }

    /**
     * Tells this screen that the size has changed and it should, at next opportunity, resize itself and its buffers
     * @param newSize New size the 'real' terminal now has
     */
    protected open fun addResizeRequest(newSize: TerminalSize) {
        latestResizeRequest = newSize
    }

    private fun getCharacterFromBuffer(buffer: ScreenBuffer, column: Int, row: Int): TextCharacter {
        return buffer.getCharacterAt(column, row)
    }

    override fun toString(): String {
        return getBackBuffer().toString()
    }

    /**
     * Performs the scrolling on its back-buffer.
     */
    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        getBackBuffer().scrollLines(firstLine, lastLine, distance)
    }
}
