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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.AbstractTextGraphics
import com.googlecode.lanterna.graphics.TextGraphics
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is the terminal's implementation of TextGraphics. Upon creation it takes a snapshot for the terminal's size, so
 * that it won't require to do an expensive lookup on every call to {@code getSize()}, but this also means that it can
 * go stale quickly if the terminal is resized. You should try to use the object quickly and then let it be GC:ed. It
 * will not pick up on terminal resize! Also, the state of the Terminal after an operation performed by this
 * TextGraphics implementation is undefined and you should probably re-initialize colors and modifiers.
 * <p/>
 * Any write operation that results in an IOException will be wrapped by a RuntimeException since the TextGraphics
 * interface doesn't allow throwing IOException
 */
internal class TerminalTextGraphics @Throws(IOException::class) constructor(private val terminal: Terminal) : AbstractTextGraphics() {

    private val terminalSize: TerminalSize = terminal.getTerminalSize()
    private val writeHistory: MutableMap<TerminalPosition?, TextCharacter?> = HashMap()

    private val manageCallStackSize: AtomicInteger = AtomicInteger(0)
    private var lastCharacter: TextCharacter? = null
    private var lastPosition: TerminalPosition? = null

    override fun setCharacter(columnIndex: Int, rowIndex: Int, textCharacter: TextCharacter?): TextGraphics {
        return setCharacter(TerminalPosition(columnIndex, rowIndex), textCharacter)
    }

    @Synchronized
    override fun setCharacter(position: TerminalPosition?, textCharacter: TextCharacter?): TextGraphics {
        try {
            if (manageCallStackSize.get() > 0) {
                val previousCharacter = lastCharacter
                if (previousCharacter == null || !previousCharacter.equals(textCharacter)) {
                    applyGraphicState(textCharacter)
                    lastCharacter = textCharacter
                }
                val previousPosition = lastPosition
                if (previousPosition == null || !previousPosition.equals(position)) {
                    val nonNullPosition = position ?: throw NullPointerException()
                    terminal.setCursorPosition(nonNullPosition.getColumn(), nonNullPosition.getRow())
                    lastPosition = position
                }
            } else {
                val nonNullPosition = position ?: throw NullPointerException()
                terminal.setCursorPosition(nonNullPosition.getColumn(), nonNullPosition.getRow())
                applyGraphicState(textCharacter)
            }
            val nonNullTextCharacter = textCharacter ?: throw NullPointerException()
            terminal.putString(nonNullTextCharacter.getCharacterString())
            if (manageCallStackSize.get() > 0) {
                val nonNullPosition = position ?: throw NullPointerException()
                lastPosition = nonNullPosition.withRelativeColumn(1)
            }
            writeHistory[position] = textCharacter
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this
    }

    override fun getCharacter(column: Int, row: Int): TextCharacter? {
        return getCharacter(TerminalPosition(column, row))
    }

    @Synchronized
    override fun getCharacter(position: TerminalPosition?): TextCharacter? {
        return writeHistory[position]
    }

    @Throws(IOException::class)
    private fun applyGraphicState(textCharacter: TextCharacter?) {
        terminal.resetColorAndSGR()
        val nonNullTextCharacter = textCharacter ?: throw NullPointerException()
        terminal.setForegroundColor(nonNullTextCharacter.getForegroundColor())
        terminal.setBackgroundColor(nonNullTextCharacter.getBackgroundColor())
        for (sgr: SGR in nonNullTextCharacter.getModifiers()) {
            terminal.enableSGR(sgr)
        }
    }

    override fun getSize(): TerminalSize {
        return terminalSize
    }

    @Synchronized
    override fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: Char): TextGraphics {
        try {
            enterAtomic()
            super.drawLine(fromPoint, toPoint, character)
            return this
        } finally {
            leaveAtomic()
        }
    }

    @Synchronized
    override fun drawTriangle(p1: TerminalPosition?, p2: TerminalPosition?, p3: TerminalPosition?, character: Char): TextGraphics {
        try {
            enterAtomic()
            super.drawTriangle(p1, p2, p3, character)
            return this
        } finally {
            leaveAtomic()
        }
    }

    @Synchronized
    override fun fillTriangle(p1: TerminalPosition?, p2: TerminalPosition?, p3: TerminalPosition?, character: Char): TextGraphics {
        try {
            enterAtomic()
            super.fillTriangle(p1, p2, p3, character)
            return this
        } finally {
            leaveAtomic()
        }
    }

    @Synchronized
    override fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics {
        try {
            enterAtomic()
            super.fillRectangle(topLeft, size, character)
            return this
        } finally {
            leaveAtomic()
        }
    }

    @Synchronized
    override fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics {
        try {
            enterAtomic()
            super.drawRectangle(topLeft, size, character)
            return this
        } finally {
            leaveAtomic()
        }
    }

    @Synchronized
    override fun putString(column: Int, row: Int, string: String?): TextGraphics {
        try {
            enterAtomic()
            return super.putString(column, row, string)
        } finally {
            leaveAtomic()
        }
    }

    /**
     * It's tricky with this implementation because we can't rely on any state in between two calls to setCharacter
     * since the caller might modify the terminal's state outside of this writer. However, many calls inside
     * TextGraphics will indeed make multiple calls in setCharacter where we know that the state won't change (actually,
     * we can't be 100% sure since the caller might create a separate thread and maliciously write directly to the
     * terminal while call one of the draw/fill/put methods in here). We could just set the state before writing every
     * single character but that would be inefficient. Rather, we keep a counter of if we are inside an 'atomic'
     * (meaning we know multiple calls to setCharacter will have the same state). Some drawing methods call other
     * drawing methods internally for their implementation so that's why this is implemented with an integer value
     * instead of a boolean; when the counter reaches zero we remove the memory of what state the terminal is in.
     */
    private fun enterAtomic() {
        manageCallStackSize.incrementAndGet()
    }

    private fun leaveAtomic() {
        if (manageCallStackSize.decrementAndGet() == 0) {
            lastPosition = null
            lastCharacter = null
        }
    }
}
