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
package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.input.InputDecoder
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.ScreenInfoCharacterPattern
import com.googlecode.lanterna.terminal.AbstractTerminal
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

abstract class StreamBasedTerminal @Suppress("WeakerAccess") constructor(
    private val terminalInput: InputStream?,
    private val terminalOutput: OutputStream?,
    terminalCharset: Charset?,
) : AbstractTerminal() {
    companion object {
        private val TERMINAL_SIZE_TIMEOUT =
            Integer.getInteger("com.googlecode.lanterna.streamTerminalSizeTimeout", 5000)
        private val UTF8_REFERENCE: Charset = StandardCharsets.UTF_8
    }

    private val terminalCharset: Charset = terminalCharset ?: Charset.defaultCharset()
    val inputDecoder: InputDecoder = InputDecoder(InputStreamReader(terminalInput, this.terminalCharset))
    private val keyQueue: Queue<KeyStroke> = LinkedList()
    private val readLock: Lock = ReentrantLock()

    @Volatile
    private var lastReportedCursorPosition: TerminalPosition? = null

    @Throws(IOException::class)
    override fun putCharacter(c: Char) {
        if (TerminalTextUtils.isPrintableCharacter(c)) {
            writeToTerminal(*translateCharacter(c))
        }
    }

    @Throws(IOException::class)
    override fun putString(string: String?) {
        if (string == null) {
            return
        }
        for (character in string) {
            putCharacter(character)
        }
    }

    @Suppress("WeakerAccess")
    @Throws(IOException::class)
    protected fun writeToTerminal(vararg bytes: Byte) {
        synchronized(terminalOutput as Any) {
            terminalOutput.write(bytes)
        }
    }

    @Throws(IOException::class)
    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray {
        val effectiveTimeoutUnit = timeoutUnit ?: TimeUnit.MILLISECONDS
        synchronized(terminalOutput as Any) {
            terminalOutput.write(5)
            flush()
        }

        val startTime = System.currentTimeMillis()
        while (terminalInput!!.available() == 0) {
            if (System.currentTimeMillis() - startTime > effectiveTimeoutUnit.toMillis(timeout.toLong())) {
                return ByteArray(0)
            }
            try {
                Thread.sleep(1)
            } catch (_: InterruptedException) {
                return ByteArray(0)
            }
        }

        val buffer = ByteArrayOutputStream()
        while (terminalInput.available() > 0) {
            buffer.write(terminalInput.read())
        }
        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    override fun bell() {
        terminalOutput!!.write(7.toByte().toInt())
        terminalOutput.flush()
    }

    internal fun resetMemorizedCursorPosition() {
        lastReportedCursorPosition = null
    }

    @Synchronized
    @Throws(IOException::class)
    internal fun waitForCursorPositionReport(): TerminalPosition? {
        val startTime = System.currentTimeMillis()
        var cursorPosition = lastReportedCursorPosition
        while (cursorPosition == null) {
            if (System.currentTimeMillis() - startTime > TERMINAL_SIZE_TIMEOUT.toLong()) {
                return null
            }
            val keyStroke = readInput(false, false)
            if (keyStroke != null) {
                keyQueue.add(keyStroke)
            } else {
                try {
                    Thread.sleep(1)
                } catch (_: InterruptedException) {
                }
            }
            cursorPosition = lastReportedCursorPosition
        }
        return cursorPosition
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        return readInput(false, true)
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? {
        return readInput(true, true)
    }

    @Throws(IOException::class)
    private fun readInput(blocking: Boolean, useKeyQueue: Boolean): KeyStroke? {
        while (true) {
            if (useKeyQueue) {
                val previouslyReadKey = keyQueue.poll()
                if (previouslyReadKey != null) {
                    return previouslyReadKey
                }
            }
            if (blocking) {
                readLock.lock()
            } else if (!readLock.tryLock()) {
                return null
            }
            try {
                val key = inputDecoder.getNextCharacter(blocking)
                val report = ScreenInfoCharacterPattern.tryToAdopt(key)
                if (lastReportedCursorPosition == null && report != null) {
                    lastReportedCursorPosition = report.position
                } else {
                    return key
                }
            } finally {
                readLock.unlock()
            }
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        synchronized(terminalOutput as Any) {
            terminalOutput.flush()
        }
    }

    @Throws(IOException::class)
    override fun close() {
    }

    protected fun getCharset(): Charset {
        return terminalCharset
    }

    @Suppress("WeakerAccess")
    protected fun translateCharacter(input: Char): ByteArray {
        if (UTF8_REFERENCE == terminalCharset) {
            return convertToCharset(input)
        }
        return when (input) {
            Symbols.ARROW_DOWN -> convertToVT100('v')
            Symbols.ARROW_LEFT -> convertToVT100('<')
            Symbols.ARROW_RIGHT -> convertToVT100('>')
            Symbols.ARROW_UP -> convertToVT100('^')
            Symbols.BLOCK_DENSE, Symbols.BLOCK_MIDDLE, Symbols.BLOCK_SOLID, Symbols.BLOCK_SPARSE -> convertToVT100('a')
            Symbols.HEART, Symbols.CLUB, Symbols.SPADES -> convertToVT100('?')
            Symbols.FACE_BLACK, Symbols.FACE_WHITE, Symbols.DIAMOND -> convertToVT100('`')
            Symbols.BULLET -> convertToVT100('f')
            Symbols.DOUBLE_LINE_CROSS, Symbols.SINGLE_LINE_CROSS -> convertToVT100('n')
            Symbols.DOUBLE_LINE_HORIZONTAL, Symbols.SINGLE_LINE_HORIZONTAL -> convertToVT100('q')
            Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER -> convertToVT100('m')
            Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER -> convertToVT100('j')
            Symbols.DOUBLE_LINE_T_DOWN,
            Symbols.SINGLE_LINE_T_DOWN,
            Symbols.DOUBLE_LINE_T_SINGLE_DOWN,
            Symbols.SINGLE_LINE_T_DOUBLE_DOWN,
            -> convertToVT100('w')
            Symbols.DOUBLE_LINE_T_LEFT,
            Symbols.SINGLE_LINE_T_LEFT,
            Symbols.DOUBLE_LINE_T_SINGLE_LEFT,
            Symbols.SINGLE_LINE_T_DOUBLE_LEFT,
            -> convertToVT100('u')
            Symbols.DOUBLE_LINE_T_RIGHT,
            Symbols.SINGLE_LINE_T_RIGHT,
            Symbols.DOUBLE_LINE_T_SINGLE_RIGHT,
            Symbols.SINGLE_LINE_T_DOUBLE_RIGHT,
            -> convertToVT100('t')
            Symbols.DOUBLE_LINE_T_UP,
            Symbols.SINGLE_LINE_T_UP,
            Symbols.DOUBLE_LINE_T_SINGLE_UP,
            Symbols.SINGLE_LINE_T_DOUBLE_UP,
            -> convertToVT100('v')
            Symbols.DOUBLE_LINE_TOP_LEFT_CORNER, Symbols.SINGLE_LINE_TOP_LEFT_CORNER -> convertToVT100('l')
            Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER -> convertToVT100('k')
            Symbols.DOUBLE_LINE_VERTICAL, Symbols.SINGLE_LINE_VERTICAL -> convertToVT100('x')
            else -> convertToCharset(input)
        }
    }

    private fun convertToVT100(code: Char): ByteArray {
        return byteArrayOf(27, 40, 48, code.code.toByte(), 27, 40, 66)
    }

    private fun convertToCharset(input: Char): ByteArray {
        return terminalCharset.encode(Character.toString(input)).array()
    }
}
