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
package com.googlecode.lanterna.terminal.virtual

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.terminal.AbstractTerminal
import java.util.ArrayList
import java.util.EnumSet
import java.util.TreeSet
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class DefaultVirtualTerminal
@JvmOverloads
constructor(private var internalTerminalSize: TerminalSize? = TerminalSize(80, 24)) : AbstractTerminal(), VirtualTerminal {
    private val regularTextBuffer = TextBuffer()
    private val privateModeTextBuffer = TextBuffer()
    private val dirtyTerminalCells = TreeSet<TerminalPosition>()
    private val listeners: MutableList<VirtualTerminalListener> = ArrayList()

    private var currentTextBuffer: TextBuffer = regularTextBuffer
    private var wholeBufferDirty: Boolean = false
    private var backlogSize: Int = 1000

    private val inputQueue: BlockingQueue<KeyStroke> = LinkedBlockingQueue()
    private val activeModifiers: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)
    private var activeForegroundColor: TextColor = TextColor.ANSI.DEFAULT
    private var activeBackgroundColor: TextColor = TextColor.ANSI.DEFAULT

    override var isCursorVisible: Boolean = true
        @Synchronized get
        @Synchronized private set

    override var cursorBufferPosition: TerminalPosition? = TerminalPosition.TOP_LEFT_CORNER
        @Synchronized get
        private set

    private var savedCursorPosition: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER

    override var cursorPosition: TerminalPosition?
        @Synchronized get() {
            val terminalSize = requireNotNull(internalTerminalSize)
            return if (bufferLineCount <= terminalSize.rows) {
                cursorBufferPosition
            } else {
                requireNotNull(cursorBufferPosition).withRelativeRow(-(bufferLineCount - terminalSize.rows))
            }
        }
        @Synchronized set(cursorPosition) {
            var adjustedPosition = requireNotNull(cursorPosition)
            val terminalSize = requireNotNull(internalTerminalSize)
            if (terminalSize.rows < bufferLineCount) {
                adjustedPosition = requireNotNull(adjustedPosition.withRelativeRow(bufferLineCount - terminalSize.rows))
            }
            this.cursorBufferPosition = adjustedPosition
            correctCursor()
        }

    val dirtyCells: TreeSet<TerminalPosition>
        @Synchronized get() = TreeSet(dirtyTerminalCells)

    val andResetDirtyCells: TreeSet<TerminalPosition>
        @Synchronized get() {
            val copy = TreeSet(dirtyTerminalCells)
            dirtyTerminalCells.clear()
            return copy
        }

    val isWholeBufferDirtyThenReset: Boolean
        @Synchronized get() {
            val copy = wholeBufferDirty
            wholeBufferDirty = false
            return copy
        }

    override val bufferLineCount: Int
        @Synchronized get() = currentTextBuffer.lineCount

    override val terminalSize: TerminalSize?
        @Synchronized get() = internalTerminalSize

    @Synchronized
    override fun setTerminalSize(newSize: TerminalSize?) {
        internalTerminalSize = newSize
        trimBufferBacklog()
        correctCursor()
        for (listener in listeners) {
            listener.onResized(this, internalTerminalSize)
        }
        val size = requireNotNull(newSize)
        onResized(size.columns, size.rows)
    }

    @Synchronized
    override fun enterPrivateMode() {
        currentTextBuffer = privateModeTextBuffer
        savedCursorPosition = requireNotNull(cursorBufferPosition)
        cursorPosition = TerminalPosition.TOP_LEFT_CORNER
        setWholeBufferDirty()
    }

    @Synchronized
    override fun exitPrivateMode() {
        currentTextBuffer = regularTextBuffer
        cursorBufferPosition = savedCursorPosition
        setWholeBufferDirty()
    }

    @Synchronized
    override fun clearScreen() {
        currentTextBuffer.clear()
        setWholeBufferDirty()
        cursorPosition = TerminalPosition.TOP_LEFT_CORNER
    }

    @Synchronized
    override fun setCursorPosition(x: Int, y: Int) {
        cursorPosition = requireNotNull(requireNotNull(requireNotNull(cursorBufferPosition).withColumn(x)).withRow(y))
    }

    @Synchronized
    override fun setCursorVisible(visible: Boolean) {
        isCursorVisible = visible
    }

    @Synchronized
    override fun putCharacter(c: Char) {
        if (c == '\n') {
            moveCursorToNextLine()
        } else if (TerminalTextUtils.isPrintableCharacter(c)) {
            putCharacter(TextCharacter(c, activeForegroundColor, activeBackgroundColor, activeModifiers))
        }
    }

    @Synchronized
    override fun putString(string: String?) {
        val textCharacters = TextCharacter.fromString(string, activeForegroundColor, activeBackgroundColor, *activeModifiers.toTypedArray())
        if (textCharacters != null) {
            for (textCharacter in textCharacters) {
                if (textCharacter != null) {
                    putCharacter(textCharacter)
                }
            }
        }
    }

    @Synchronized
    override fun enableSGR(sgr: SGR?) {
        if (sgr != null) {
            activeModifiers.add(sgr)
        }
    }

    @Synchronized
    override fun disableSGR(sgr: SGR?) {
        if (sgr != null) {
            activeModifiers.remove(sgr)
        }
    }

    @Synchronized
    override fun resetColorAndSGR() {
        activeModifiers.clear()
        activeForegroundColor = TextColor.ANSI.DEFAULT
        activeBackgroundColor = TextColor.ANSI.DEFAULT
    }

    @Synchronized
    override fun setForegroundColor(color: TextColor?) {
        activeForegroundColor = color ?: TextColor.ANSI.DEFAULT
    }

    @Synchronized
    override fun setBackgroundColor(color: TextColor?) {
        activeBackgroundColor = color ?: TextColor.ANSI.DEFAULT
    }

    @Synchronized
    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray = javaClass.name.toByteArray()

    @Synchronized
    override fun bell() {
        for (listener in listeners) {
            listener.onBell()
        }
    }

    @Synchronized
    override fun flush() {
        for (listener in listeners) {
            listener.onFlush()
        }
    }

    override fun close() {
        for (listener in listeners) {
            listener.onClose()
        }
    }

    @Synchronized
    override fun pollInput(): KeyStroke? = inputQueue.poll()

    @Synchronized
    override fun readInput(): KeyStroke {
        try {
            return inputQueue.take()
        } catch (e: InterruptedException) {
            throw RuntimeException("Unexpected interrupt", e)
        }
    }

    override fun newTextGraphics(): TextGraphics = VirtualTerminalTextGraphics(this)

    @Synchronized
    override fun addVirtualTerminalListener(listener: VirtualTerminalListener?) {
        if (listener != null) {
            listeners.add(listener)
        }
    }

    @Synchronized
    override fun removeVirtualTerminalListener(listener: VirtualTerminalListener?) {
        if (listener != null) {
            listeners.remove(listener)
        }
    }

    @Synchronized
    override fun setBacklogSize(backlogSize: Int) {
        this.backlogSize = backlogSize
    }

    override fun addInput(keyStroke: KeyStroke?) {
        if (keyStroke != null) {
            inputQueue.add(keyStroke)
        }
    }

    @Synchronized
    override fun getCharacter(position: TerminalPosition?): TextCharacter? {
        return getCharacter(requireNotNull(position).column, position.row)
    }

    @Synchronized
    override fun getCharacter(column: Int, row: Int): TextCharacter? {
        var adjustedRow = row
        val terminalSize = requireNotNull(internalTerminalSize)
        if (terminalSize.rows < currentTextBuffer.lineCount) {
            adjustedRow += currentTextBuffer.lineCount - terminalSize.rows
        }
        return getBufferCharacter(column, adjustedRow)
    }

    override fun getBufferCharacter(column: Int, row: Int): TextCharacter? {
        return currentTextBuffer.getCharacter(row, column)
    }

    override fun getBufferCharacter(position: TerminalPosition?): TextCharacter? {
        return getBufferCharacter(requireNotNull(position).column, position.row)
    }

    @Synchronized
    override fun forEachLine(startRow: Int, endRow: Int, bufferWalker: VirtualTerminal.BufferWalker?) {
        val emptyLine = object : VirtualTerminal.BufferLine {
            override fun getCharacterAt(column: Int): TextCharacter = TextCharacter.DEFAULT_CHARACTER
        }
        val iterator = currentTextBuffer.getLinesFrom(startRow)
        for (row in startRow..endRow) {
            var bufferLine: VirtualTerminal.BufferLine = emptyLine
            if (iterator.hasNext()) {
                val list = iterator.next()
                bufferLine = object : VirtualTerminal.BufferLine {
                    override fun getCharacterAt(column: Int): TextCharacter {
                        if (column >= list.size) {
                            return TextCharacter.DEFAULT_CHARACTER
                        }
                        return requireNotNull(list[column])
                    }
                }
            }
            bufferWalker?.onLine(row, bufferLine)
        }
    }

    @Synchronized
    internal fun putCharacter(terminalCharacter: TextCharacter) {
        val terminalSize = requireNotNull(internalTerminalSize)
        if (terminalCharacter.`is`('\t')) {
            val nrOfSpaces = TabBehaviour.ALIGN_TO_COLUMN_4.getTabReplacement(requireNotNull(cursorBufferPosition).column).length
            var i = 0
            while (i < nrOfSpaces && requireNotNull(cursorBufferPosition).column < terminalSize.columns - 1) {
                putCharacter(terminalCharacter.withCharacter(' '))
                i++
            }
        } else {
            val doubleWidth = terminalCharacter.isDoubleWidth
            if (requireNotNull(cursorBufferPosition).column == terminalSize.columns - 1 && doubleWidth) {
                currentTextBuffer.setCharacter(cursorBufferPosition!!.row, cursorBufferPosition!!.column, TextCharacter.DEFAULT_CHARACTER)
                moveCursorToNextLine()
            }
            if (requireNotNull(cursorBufferPosition).column == terminalSize.columns) {
                moveCursorToNextLine()
            }

            val style = currentTextBuffer.setCharacter(cursorBufferPosition!!.row, cursorBufferPosition!!.column, terminalCharacter)
            if (!wholeBufferDirty) {
                dirtyTerminalCells.add(TerminalPosition(cursorBufferPosition!!.column, cursorBufferPosition!!.row))
                if (style == 1) {
                    dirtyTerminalCells.add(TerminalPosition(cursorBufferPosition!!.column + 1, cursorBufferPosition!!.row))
                } else if (style == 2) {
                    dirtyTerminalCells.add(TerminalPosition(cursorBufferPosition!!.column - 1, cursorBufferPosition!!.row))
                }
                if (dirtyTerminalCells.size > terminalSize.columns * terminalSize.rows * 0.9) {
                    setWholeBufferDirty()
                }
            }

            cursorBufferPosition = requireNotNull(cursorBufferPosition).withRelativeColumn(if (doubleWidth) 2 else 1)
            if (requireNotNull(cursorBufferPosition).column > terminalSize.columns) {
                moveCursorToNextLine()
            }
        }
    }

    private fun moveCursorToNextLine() {
        cursorBufferPosition = requireNotNull(requireNotNull(requireNotNull(cursorBufferPosition).withColumn(0)).withRelativeRow(1))
        if (requireNotNull(cursorBufferPosition).row >= currentTextBuffer.lineCount) {
            currentTextBuffer.newLine()
        }
        trimBufferBacklog()
        correctCursor()
    }

    private fun setWholeBufferDirty() {
        wholeBufferDirty = true
        dirtyTerminalCells.clear()
    }

    private fun trimBufferBacklog() {
        var bufferBacklogSize = backlogSize
        if (currentTextBuffer === privateModeTextBuffer) {
            bufferBacklogSize = 0
        }
        val terminalSize = requireNotNull(internalTerminalSize)
        val trimBacklogRows = currentTextBuffer.lineCount - (bufferBacklogSize + terminalSize.rows)
        if (trimBacklogRows > 0) {
            currentTextBuffer.removeTopLines(trimBacklogRows)
            cursorBufferPosition = requireNotNull(cursorBufferPosition).withRelativeRow(-trimBacklogRows)
            correctCursor()
            if (!wholeBufferDirty) {
                val newDirtySet = TreeSet<TerminalPosition>()
                for (dirtyPosition in dirtyTerminalCells) {
                    val adjustedPosition = requireNotNull(dirtyPosition.withRelativeRow(-trimBacklogRows))
                    if (adjustedPosition.row >= 0) {
                        newDirtySet.add(adjustedPosition)
                    }
                }
                dirtyTerminalCells.clear()
                dirtyTerminalCells.addAll(newDirtySet)
            }
        }
    }

    private fun correctCursor() {
        val terminalSize = requireNotNull(internalTerminalSize)
        cursorBufferPosition = requireNotNull(cursorBufferPosition).withColumn(
            minOf(cursorBufferPosition!!.column, terminalSize.columns - 1),
        )
        cursorBufferPosition = requireNotNull(cursorBufferPosition).withRow(
            minOf(cursorBufferPosition!!.row, maxOf(terminalSize.rows, bufferLineCount) - 1),
        )
        cursorBufferPosition = TerminalPosition(
            maxOf(cursorBufferPosition!!.column, 0),
            maxOf(cursorBufferPosition!!.row, 0),
        )
    }

    override fun toString(): String = currentTextBuffer.toString()
}
