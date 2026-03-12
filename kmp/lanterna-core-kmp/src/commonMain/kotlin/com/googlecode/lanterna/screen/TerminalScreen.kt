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
import com.googlecode.lanterna.internal.compat.EnumSet
import com.googlecode.lanterna.internal.compat.TreeMap
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.TerminalResizeListener
import kotlin.Comparator

class TerminalScreen constructor(
    val terminal: Terminal,
    defaultCharacter: TextCharacter? = Screen.DEFAULT_CHARACTER,
) : AbstractScreen(terminal.terminalSize, defaultCharacter) {
    private var isStarted = false
    private var fullRedrawHint = true
    private var scrollHint: ScrollHint? = null

    init {
        terminal.addResizeListener(TerminalScreenResizeListener())
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        terminal.close()
    }

    @Throws(IOException::class)
    override fun startScreen() {
        if (isStarted) {
            return
        }
        isStarted = true
        terminal.enterPrivateMode()
        terminal.terminalSize
        terminal.clearScreen()
        fullRedrawHint = true
        val currentCursorPosition = cursorPosition
        if (currentCursorPosition != null) {
            terminal.setCursorVisible(true)
            terminal.setCursorPosition(currentCursorPosition.column, currentCursorPosition.row)
        } else {
            terminal.setCursorVisible(false)
        }
    }

    @Throws(IOException::class)
    override fun stopScreen() {
        stopScreen(true)
    }

    @Throws(IOException::class)
    fun stopScreen(flushInput: Boolean) {
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

    @Throws(IOException::class)
    override fun refresh(refreshType: Screen.RefreshType?) {
        if (!isStarted) {
            return
        }
        if ((refreshType == Screen.RefreshType.AUTOMATIC && fullRedrawHint) || refreshType == Screen.RefreshType.COMPLETE) {
            refreshFull()
            fullRedrawHint = false
        } else if (
            refreshType == Screen.RefreshType.AUTOMATIC &&
            (scrollHint == null || scrollHint == ScrollHint.INVALID)
        ) {
            val currentTerminalSize = terminalSize ?: return
            val threshold = currentTerminalSize.rows * currentTerminalSize.columns * 0.75
            if (backBuffer.isVeryDifferent(frontBuffer, threshold.toInt())) {
                refreshFull()
            } else {
                refreshByDelta()
            }
        } else {
            refreshByDelta()
        }

        backBuffer.copyTo(frontBuffer)
        val currentCursorPosition = cursorPosition
        if (currentCursorPosition != null) {
            terminal.setCursorVisible(true)
            val previousPosition = currentCursorPosition.withRelativeColumn(-1)
            if (
                currentCursorPosition.column > 0 &&
                previousPosition != null &&
                (frontBuffer.getCharacterAt(previousPosition)?.isDoubleWidth == true)
            ) {
                terminal.setCursorPosition(currentCursorPosition.column - 1, currentCursorPosition.row)
            } else {
                terminal.setCursorPosition(currentCursorPosition.column, currentCursorPosition.row)
            }
        } else {
            terminal.setCursorVisible(false)
        }
        terminal.flush()
    }

    @Throws(IOException::class)
    private fun useScrollHint() {
        val currentScrollHint = scrollHint ?: return
        try {
            if (currentScrollHint == ScrollHint.INVALID) {
                return
            }
            val term = terminal
            if (term is Scrollable) {
                currentScrollHint.applyTo(term)
                currentScrollHint.applyTo(frontBuffer)
            }
        } catch (_: UnsupportedOperationException) {
        } finally {
            scrollHint = null
        }
    }

    @Throws(IOException::class)
    private fun refreshByDelta() {
        val updateMap = TreeMap<TerminalPosition, TextCharacter>(ScreenPointComparator())
        val currentTerminalSize = terminalSize ?: return

        useScrollHint()

        for (y in 0 until currentTerminalSize.rows) {
            var x = 0
            while (x < currentTerminalSize.columns) {
                val backBufferCharacter = backBuffer.getCharacterAt(x, y) ?: continue
                val frontBufferCharacter = frontBuffer.getCharacterAt(x, y) ?: continue
                if (backBufferCharacter != frontBufferCharacter) {
                    updateMap[TerminalPosition(x, y)] = backBufferCharacter
                }
                if (backBufferCharacter.isDoubleWidth) {
                    x++
                } else if (frontBufferCharacter.isDoubleWidth && x + 1 < currentTerminalSize.columns) {
                    updateMap[TerminalPosition(x + 1, y)] = frontBufferCharacter.withCharacter(' ')
                }
                x++
            }
        }

        if (updateMap.isEmpty()) {
            return
        }

        var currentPosition = updateMap.keys.first()
        terminal.setCursorPosition(currentPosition.column, currentPosition.row)

        val firstCharacter = updateMap.values.first()
        val currentSGR = firstCharacter.getModifiers()
        terminal.resetColorAndSGR()
        for (sgr in currentSGR) {
            terminal.enableSGR(sgr)
        }
        var currentForegroundColor = firstCharacter.foregroundColor
        var currentBackgroundColor = firstCharacter.backgroundColor
        terminal.setForegroundColor(currentForegroundColor)
        terminal.setBackgroundColor(currentBackgroundColor)

        for ((position, newCharacter) in updateMap) {
            if (position != currentPosition) {
                terminal.setCursorPosition(position.column, position.row)
                currentPosition = position
            }
            if (currentForegroundColor != newCharacter.foregroundColor) {
                terminal.setForegroundColor(newCharacter.foregroundColor)
                currentForegroundColor = newCharacter.foregroundColor
            }
            if (currentBackgroundColor != newCharacter.backgroundColor) {
                terminal.setBackgroundColor(newCharacter.backgroundColor)
                currentBackgroundColor = newCharacter.backgroundColor
            }
            for (sgr in SGR.values()) {
                if (currentSGR.contains(sgr) && !newCharacter.getModifiers().contains(sgr)) {
                    terminal.disableSGR(sgr)
                    currentSGR.remove(sgr)
                } else if (!currentSGR.contains(sgr) && newCharacter.getModifiers().contains(sgr)) {
                    terminal.enableSGR(sgr)
                    currentSGR.add(sgr)
                }
            }
            terminal.putString(newCharacter.characterString)
            currentPosition = currentPosition.withRelativeColumn(if (newCharacter.isDoubleWidth) 2 else 1) ?: currentPosition
        }
    }

    @Throws(IOException::class)
    private fun refreshFull() {
        terminal.setForegroundColor(TextColor.ANSI.DEFAULT)
        terminal.setBackgroundColor(TextColor.ANSI.DEFAULT)
        terminal.clearScreen()
        terminal.resetColorAndSGR()
        scrollHint = null

        val currentSGR = EnumSet.noneOf(SGR::class)
        var currentForegroundColor: TextColor? = TextColor.ANSI.DEFAULT
        var currentBackgroundColor: TextColor? = TextColor.ANSI.DEFAULT
        val currentTerminalSize = terminalSize ?: return

        for (y in 0 until currentTerminalSize.rows) {
            terminal.setCursorPosition(0, y)
            var currentColumn = 0
            for (x in 0 until currentTerminalSize.columns) {
                val newCharacter = backBuffer.getCharacterAt(x, y) ?: continue
                if (newCharacter == Screen.DEFAULT_CHARACTER) {
                    continue
                }
                if (currentForegroundColor != newCharacter.foregroundColor) {
                    terminal.setForegroundColor(newCharacter.foregroundColor)
                    currentForegroundColor = newCharacter.foregroundColor
                }
                if (currentBackgroundColor != newCharacter.backgroundColor) {
                    terminal.setBackgroundColor(newCharacter.backgroundColor)
                    currentBackgroundColor = newCharacter.backgroundColor
                }
                for (sgr in SGR.values()) {
                    if (currentSGR.contains(sgr) && !newCharacter.getModifiers().contains(sgr)) {
                        terminal.disableSGR(sgr)
                        currentSGR.remove(sgr)
                    } else if (!currentSGR.contains(sgr) && newCharacter.getModifiers().contains(sgr)) {
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
                } else {
                    currentColumn += 1
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? = terminal.readInput()

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? = terminal.pollInput()

    override fun clear() {
        super.clear()
        fullRedrawHint = true
        scrollHint = ScrollHint.INVALID
    }

    override fun doResizeIfNecessary(): TerminalSize? {
        val newSize = super.doResizeIfNecessary()
        if (newSize != null) {
            fullRedrawHint = true
        }
        return newSize
    }

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        if (distance == 0 || firstLine > lastLine) {
            return
        }
        super.scrollLines(firstLine, lastLine, distance)
        val newHint = ScrollHint(firstLine, lastLine, distance)
        scrollHint =
            when (val current = scrollHint) {
                null -> newHint
                ScrollHint.INVALID -> ScrollHint.INVALID
                else ->
                    if (current.matches(newHint)) {
                        current.distance += newHint.distance
                        current
                    } else {
                        ScrollHint.INVALID
                    }
            }
    }

    private inner class TerminalScreenResizeListener : TerminalResizeListener {
        override fun onResized(
            terminal: Terminal?,
            newSize: TerminalSize?,
        ) {
            addResizeRequest(newSize)
        }
    }

    private class ScreenPointComparator : Comparator<TerminalPosition> {
        override fun compare(
            o1: TerminalPosition,
            o2: TerminalPosition,
        ): Int {
            return if (o1.row == o2.row) {
                o1.column.compareTo(o2.column)
            } else {
                o1.row.compareTo(o2.row)
            }
        }
    }

    private class ScrollHint(
        val firstLine: Int,
        val lastLine: Int,
        var distance: Int,
    ) {
        fun matches(other: ScrollHint): Boolean {
            return firstLine == other.firstLine && lastLine == other.lastLine
        }

        @Throws(IOException::class)
        fun applyTo(scrollable: Scrollable) {
            scrollable.scrollLines(firstLine, lastLine, distance)
        }

        companion object {
            val INVALID = ScrollHint(-1, -1, 0)
        }
    }
}
