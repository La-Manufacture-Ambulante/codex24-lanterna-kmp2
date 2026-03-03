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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.Scrollable
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.TerminalResizeListener
import java.io.IOException
import java.util.Comparator
import java.util.EnumSet
import java.util.TreeMap

open class TerminalScreen : AbstractScreen {
    private val terminal: Terminal
    private var isStarted: Boolean
    private var fullRedrawHint: Boolean
    private var scrollHint: ScrollHint? = null

    @Throws(IOException::class)
    constructor(terminal: Terminal) : this(terminal, DEFAULT_CHARACTER)

    @Throws(IOException::class)
    constructor(terminal: Terminal, defaultCharacter: TextCharacter) : super(terminal.terminalSize, defaultCharacter) {
        this.terminal = terminal
        this.terminal.addResizeListener(TerminalScreenResizeListener())
        this.isStarted = false
        this.fullRedrawHint = true
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        terminal.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun startScreen() {
        if (isStarted) {
            return
        }

        isStarted = true
        terminal.enterPrivateMode()
        terminal.terminalSize
        terminal.clearScreen()
        this.fullRedrawHint = true
        val cursorPosition: TerminalPosition? = cursorPosition
        if (cursorPosition != null) {
            terminal.setCursorVisible(true)
            terminal.setCursorPosition(cursorPosition.column, cursorPosition.row)
        } else {
            terminal.setCursorVisible(false)
        }
    }

    @Throws(IOException::class)
    override fun stopScreen() {
        stopScreen(true)
    }

    @Synchronized
    @Throws(IOException::class)
    open fun stopScreen(flushInput: Boolean) {
        if (!isStarted) {
            return
        }

        if (flushInput) {
            var keyStroke: KeyStroke?
            do {
                keyStroke = pollInput()
            } while (keyStroke != null && keyStroke.keyType != KeyType.EOF)
        }

        terminal.exitPrivateMode()
        isStarted = false
    }

    @Synchronized
    @Throws(IOException::class)
    override fun refresh(refreshType: RefreshType) {
        if (!isStarted) {
            return
        }
        if ((refreshType == RefreshType.AUTOMATIC && fullRedrawHint) || refreshType == RefreshType.COMPLETE) {
            refreshFull()
            fullRedrawHint = false
        } else if (refreshType == RefreshType.AUTOMATIC &&
            (scrollHint == null || scrollHint == ScrollHint.INVALID)
        ) {
            val threshold = terminalSize.rows * terminalSize.columns * 0.75
            if (backBuffer.isVeryDifferent(frontBuffer, threshold.toInt())) {
                refreshFull()
            } else {
                refreshByDelta()
            }
        } else {
            refreshByDelta()
        }
        backBuffer.copyTo(frontBuffer)
        val cursorPosition: TerminalPosition? = cursorPosition
        if (cursorPosition != null) {
            terminal.setCursorVisible(true)
            if (cursorPosition.column > 0 &&
                frontBuffer.getCharacterAt(cursorPosition.withRelativeColumn(-1)).isDoubleWidth
            ) {
                terminal.setCursorPosition(cursorPosition.column - 1, cursorPosition.row)
            } else {
                terminal.setCursorPosition(cursorPosition.column, cursorPosition.row)
            }
        } else {
            terminal.setCursorVisible(false)
        }
        terminal.flush()
    }

    @Throws(IOException::class)
    private fun useScrollHint() {
        val hint = scrollHint ?: return

        try {
            if (hint == ScrollHint.INVALID) {
                return
            }
            val term = terminal
            if (term is Scrollable) {
                hint.applyTo(term)
                hint.applyTo(frontBuffer)
            }
        } catch (_: UnsupportedOperationException) {
        } finally {
            scrollHint = null
        }
    }

    @Throws(IOException::class)
    private fun refreshByDelta() {
        val updateMap = TreeMap<TerminalPosition, TextCharacter>(ScreenPointComparator())
        val terminalSize: TerminalSize = terminalSize

        useScrollHint()

        for (y in 0 until terminalSize.rows) {
            var x = 0
            while (x < terminalSize.columns) {
                val backBufferCharacter = backBuffer.getCharacterAt(x, y)
                val frontBufferCharacter = frontBuffer.getCharacterAt(x, y)
                if (!backBufferCharacter.equals(frontBufferCharacter)) {
                    updateMap[TerminalPosition(x, y)] = backBufferCharacter
                }
                if (backBufferCharacter.isDoubleWidth) {
                    x++
                } else if (frontBufferCharacter.isDoubleWidth) {
                    if (x + 1 < terminalSize.columns) {
                        updateMap[TerminalPosition(x + 1, y)] = frontBufferCharacter.withCharacter(' ')
                    }
                }
                x++
            }
        }

        if (updateMap.isEmpty()) {
            return
        }
        var currentPosition = updateMap.keys.iterator().next()
        terminal.setCursorPosition(currentPosition.column, currentPosition.row)

        val firstScreenCharacterToUpdate = updateMap.values.iterator().next()
        val currentSGR = firstScreenCharacterToUpdate.modifiers
        terminal.resetColorAndSGR()
        for (sgr in currentSGR) {
            terminal.enableSGR(sgr)
        }
        var currentForegroundColor = firstScreenCharacterToUpdate.foregroundColor
        var currentBackgroundColor = firstScreenCharacterToUpdate.backgroundColor
        terminal.setForegroundColor(currentForegroundColor)
        terminal.setBackgroundColor(currentBackgroundColor)
        for (position in updateMap.keys) {
            if (!position.equals(currentPosition)) {
                terminal.setCursorPosition(position.column, position.row)
                currentPosition = position
            }
            val newCharacter = updateMap[position] ?: throw NullPointerException()
            if (!currentForegroundColor.equals(newCharacter.foregroundColor)) {
                terminal.setForegroundColor(newCharacter.foregroundColor)
                currentForegroundColor = newCharacter.foregroundColor
            }
            if (!currentBackgroundColor.equals(newCharacter.backgroundColor)) {
                terminal.setBackgroundColor(newCharacter.backgroundColor)
                currentBackgroundColor = newCharacter.backgroundColor
            }
            for (sgr in SGR.values()) {
                if (currentSGR.contains(sgr) && !newCharacter.modifiers.contains(sgr)) {
                    terminal.disableSGR(sgr)
                    currentSGR.remove(sgr)
                } else if (!currentSGR.contains(sgr) && newCharacter.modifiers.contains(sgr)) {
                    terminal.enableSGR(sgr)
                    currentSGR.add(sgr)
                }
            }
            terminal.putString(newCharacter.characterString)
            currentPosition = if (newCharacter.isDoubleWidth) {
                currentPosition.withRelativeColumn(2)
            } else {
                currentPosition.withRelativeColumn(1)
            }
        }
    }

    @Throws(IOException::class)
    private fun refreshFull() {
        terminal.setForegroundColor(TextColor.ANSI.DEFAULT)
        terminal.setBackgroundColor(TextColor.ANSI.DEFAULT)
        terminal.clearScreen()
        terminal.resetColorAndSGR()
        scrollHint = null

        val currentSGR = EnumSet.noneOf(SGR::class.java)
        var currentForegroundColor: TextColor = TextColor.ANSI.DEFAULT
        var currentBackgroundColor: TextColor = TextColor.ANSI.DEFAULT
        for (y in 0 until terminalSize.rows) {
            terminal.setCursorPosition(0, y)
            var currentColumn = 0
            var x = 0
            while (x < terminalSize.columns) {
                val newCharacter = backBuffer.getCharacterAt(x, y)
                if (newCharacter.equals(DEFAULT_CHARACTER)) {
                    x++
                    continue
                }

                if (!currentForegroundColor.equals(newCharacter.foregroundColor)) {
                    terminal.setForegroundColor(newCharacter.foregroundColor)
                    currentForegroundColor = newCharacter.foregroundColor
                }
                if (!currentBackgroundColor.equals(newCharacter.backgroundColor)) {
                    terminal.setBackgroundColor(newCharacter.backgroundColor)
                    currentBackgroundColor = newCharacter.backgroundColor
                }
                for (sgr in SGR.values()) {
                    if (currentSGR.contains(sgr) && !newCharacter.modifiers.contains(sgr)) {
                        terminal.disableSGR(sgr)
                        currentSGR.remove(sgr)
                    } else if (!currentSGR.contains(sgr) && newCharacter.modifiers.contains(sgr)) {
                        terminal.enableSGR(sgr)
                        currentSGR.add(sgr)
                    }
                }
                if (currentColumn != x) {
                    terminal.setCursorPosition(x, y)
                    currentColumn = x
                }
                terminal.putString(newCharacter.characterString)
                if (newCharacter.isDoubleWidth) {
                    currentColumn += 2
                    x++
                } else {
                    currentColumn += 1
                }
                x++
            }
        }
    }

    @Suppress("WeakerAccess")
    open fun getTerminal(): Terminal {
        return terminal
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? {
        return terminal.readInput()
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        return terminal.pollInput()
    }

    @Synchronized
    override fun clear() {
        super.clear()
        fullRedrawHint = true
        scrollHint = ScrollHint.INVALID
    }

    @Synchronized
    override fun doResizeIfNecessary(): TerminalSize? {
        val newSize = super.doResizeIfNecessary()
        if (newSize != null) {
            fullRedrawHint = true
        }
        return newSize
    }

    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        if (distance == 0 || firstLine > lastLine) {
            return
        }

        super.scrollLines(firstLine, lastLine, distance)

        val newHint = ScrollHint(firstLine, lastLine, distance)
        val currentHint = scrollHint
        if (currentHint == null) {
            scrollHint = newHint
        } else if (currentHint == ScrollHint.INVALID) {
            // leave at INVALID
        } else if (currentHint.matches(newHint)) {
            currentHint.distance += newHint.distance
        } else {
            this.scrollHint = ScrollHint.INVALID
        }
    }

    private inner class TerminalScreenResizeListener : TerminalResizeListener {
        override fun onResized(terminal: Terminal, newSize: TerminalSize) {
            addResizeRequest(newSize)
        }
    }

    private class ScreenPointComparator : Comparator<TerminalPosition> {
        override fun compare(o1: TerminalPosition, o2: TerminalPosition): Int {
            return if (o1.row == o2.row) {
                if (o1.column == o2.column) {
                    0
                } else {
                    Integer.compare(o1.column, o2.column)
                }
            } else {
                Integer.compare(o1.row, o2.row)
            }
        }
    }

    private class ScrollHint(
        val firstLine: Int,
        val lastLine: Int,
        var distance: Int
    ) {
        fun matches(other: ScrollHint): Boolean {
            return this.firstLine == other.firstLine &&
                this.lastLine == other.lastLine
        }

        @Throws(IOException::class)
        fun applyTo(scr: Scrollable) {
            scr.scrollLines(firstLine, lastLine, distance)
        }

        companion object {
            @JvmField
            val INVALID = ScrollHint(-1, -1, 0)
        }
    }
}
