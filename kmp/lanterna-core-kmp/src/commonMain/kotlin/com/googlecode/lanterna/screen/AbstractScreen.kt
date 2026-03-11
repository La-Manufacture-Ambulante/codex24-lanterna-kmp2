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

abstract class AbstractScreen(
    initialSize: TerminalSize?,
    private val defaultCharacter: TextCharacter? = Screen.DEFAULT_CHARACTER,
) : Screen {
    override var cursorPosition: TerminalPosition? = TerminalPosition(0, 0)
        set(value) {
            if (value == null) {
                field = null
                return
            }

            var position = value
            if (position.column < 0) {
                position = position.withColumn(0) ?: position
            }
            if (position.row < 0) {
                position = position.withRow(0) ?: position
            }
            val currentSize = terminalSize
            if (currentSize != null) {
                if (position.column >= currentSize.columns) {
                    position = position.withColumn(currentSize.columns - 1) ?: position
                }
                if (position.row >= currentSize.rows) {
                    position = position.withRow(currentSize.rows - 1) ?: position
                }
            }
            field = position
        }

    protected var backBuffer: ScreenBuffer = ScreenBuffer(initialSize, defaultCharacter)
        protected set
    protected var frontBuffer: ScreenBuffer = ScreenBuffer(initialSize, defaultCharacter)
        protected set

    override var tabBehaviour: TabBehaviour? = TabBehaviour.ALIGN_TO_COLUMN_4
        set(value) {
            if (value != null) {
                field = value
            }
        }

    private var terminalSizeBacking: TerminalSize? = initialSize
    override val terminalSize: TerminalSize?
        get() = terminalSizeBacking

    private var latestResizeRequest: TerminalSize? = null

    override fun setCharacter(
        position: TerminalPosition?,
        screenCharacter: TextCharacter?,
    ) {
        setCharacter(position!!.column, position.row, screenCharacter!!)
    }

    override fun newTextGraphics(): TextGraphics {
        return object : ScreenTextGraphics(this) {
            override fun drawImage(
                topLeft: TerminalPosition?,
                image: TextImage?,
                sourceImageTopLeft: TerminalPosition?,
                sourceImageSize: TerminalSize?,
            ): TextGraphics? {
                if (topLeft != null && image != null && sourceImageTopLeft != null && sourceImageSize != null) {
                    backBuffer.copyFrom(
                        image,
                        sourceImageTopLeft.row,
                        sourceImageSize.rows,
                        sourceImageTopLeft.column,
                        sourceImageSize.columns,
                        topLeft.row,
                        topLeft.column,
                    )
                }
                return this
            }
        }
    }

    @Synchronized
    override fun setCharacter(
        column: Int,
        row: Int,
        screenCharacter: TextCharacter?,
    ) {
        var character = screenCharacter!!
        if (character.`is`('\t')) {
            character = character.withCharacter(' ')
            val replacementLength = tabBehaviour?.replaceTabs("\t", column)?.length ?: 1
            for (i in 0 until replacementLength) {
                backBuffer.setCharacterAt(column + i, row, character)
            }
        } else {
            backBuffer.setCharacterAt(column, row, character)
        }
    }

    override fun getFrontCharacter(
        column: Int,
        row: Int,
    ): TextCharacter? = getCharacterFromBuffer(frontBuffer, column, row)

    override fun getFrontCharacter(position: TerminalPosition?): TextCharacter? = getFrontCharacter(position!!.column, position.row)

    override fun getBackCharacter(
        column: Int,
        row: Int,
    ): TextCharacter? = getCharacterFromBuffer(backBuffer, column, row)

    override fun getBackCharacter(position: TerminalPosition?): TextCharacter? = getBackCharacter(position!!.column, position.row)

    @Throws(IOException::class)
    override fun refresh() {
        refresh(Screen.RefreshType.AUTOMATIC)
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
        val pendingResize = getAndClearPendingResize() ?: return null
        backBuffer = backBuffer.resize(pendingResize, defaultCharacter)
        frontBuffer = frontBuffer.resize(pendingResize, defaultCharacter)
        return pendingResize
    }

    protected fun addResizeRequest(newSize: TerminalSize?) {
        latestResizeRequest = newSize
    }

    @Synchronized
    private fun getAndClearPendingResize(): TerminalSize? {
        if (latestResizeRequest != null) {
            terminalSizeBacking = latestResizeRequest
            latestResizeRequest = null
            return terminalSizeBacking
        }
        return null
    }

    private fun getCharacterFromBuffer(
        buffer: ScreenBuffer,
        column: Int,
        row: Int,
    ): TextCharacter? {
        return buffer.getCharacterAt(column, row)
    }

    override fun toString(): String = backBuffer.toString()

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        backBuffer.scrollLines(firstLine, lastLine, distance)
    }
}
