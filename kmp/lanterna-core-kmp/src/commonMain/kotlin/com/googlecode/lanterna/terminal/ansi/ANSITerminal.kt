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
package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.DefaultKeyDecodingProfile
import com.googlecode.lanterna.input.KeyDecodingProfile
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import com.googlecode.lanterna.terminal.ExtendedTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

abstract class ANSITerminal @Suppress("WeakerAccess") protected constructor(
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
) : StreamBasedTerminal(terminalInput, terminalOutput, terminalCharset), ExtendedTerminal {
    private var requestedMouseCaptureMode: MouseCaptureMode? = null
    private var mouseCaptureMode: MouseCaptureMode? = null
    private var inPrivateMode = false

    protected open fun getDefaultKeyDecodingProfile(): KeyDecodingProfile {
        return DefaultKeyDecodingProfile()
    }

    private fun writeCSISequenceToTerminal(vararg tail: Byte) {
        val completeSequence = ByteArray(tail.size + 2)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = '['.code.toByte()
        System.arraycopy(tail, 0, completeSequence, 2, tail.size)
        writeToTerminal(*completeSequence)
    }

    private fun writeSGRSequenceToTerminal(vararg sgrParameters: Byte) {
        val completeSequence = ByteArray(sgrParameters.size + 3)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = '['.code.toByte()
        completeSequence[completeSequence.size - 1] = 'm'.code.toByte()
        System.arraycopy(sgrParameters, 0, completeSequence, 2, sgrParameters.size)
        writeToTerminal(*completeSequence)
    }

    private fun writeOSCSequenceToTerminal(vararg tail: Byte) {
        val completeSequence = ByteArray(tail.size + 2)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = ']'.code.toByte()
        System.arraycopy(tail, 0, completeSequence, 2, tail.size)
        writeToTerminal(*completeSequence)
    }

    @get:Synchronized
    @get:Throws(IOException::class)
    final override val terminalSize: TerminalSize?
        get() {
            val size = findTerminalSize()
            onResized(size)
            return size
        }

    @Throws(IOException::class)
    protected open fun findTerminalSize(): TerminalSize {
        saveCursorPosition()
        setCursorPosition(5000, 5000)
        resetMemorizedCursorPosition()
        reportPosition()
        restoreCursorPosition()
        val terminalPosition = waitForCursorPositionReport() ?: TerminalPosition(80, 24)
        return TerminalSize(terminalPosition.column, terminalPosition.row)
    }

    @Throws(IOException::class)
    override fun setTerminalSize(columns: Int, rows: Int) {
        writeCSISequenceToTerminal(*("8;$rows;$columns" + "t").toByteArray())
        terminalSize
    }

    @Throws(IOException::class)
    override fun setTitle(title: String?) {
        val safeTitle = (title ?: "").replace("\u0007", "")
        writeOSCSequenceToTerminal(*("2;$safeTitle\u0007").toByteArray())
    }

    @Throws(IOException::class)
    override fun setForegroundColor(color: TextColor?) {
        val safeColor = color ?: TextColor.ANSI.DEFAULT
        writeSGRSequenceToTerminal(*requireNotNull(safeColor.foregroundSGRSequence))
    }

    @Throws(IOException::class)
    override fun setBackgroundColor(color: TextColor?) {
        val safeColor = color ?: TextColor.ANSI.DEFAULT
        writeSGRSequenceToTerminal(*requireNotNull(safeColor.backgroundSGRSequence))
    }

    @Throws(IOException::class)
    override fun enableSGR(sgr: SGR?) {
        when (sgr) {
            SGR.BLINK -> writeCSISequenceToTerminal('5'.code.toByte(), 'm'.code.toByte())
            SGR.BOLD -> writeCSISequenceToTerminal('1'.code.toByte(), 'm'.code.toByte())
            SGR.BORDERED -> writeCSISequenceToTerminal('5'.code.toByte(), '1'.code.toByte(), 'm'.code.toByte())
            SGR.CIRCLED -> writeCSISequenceToTerminal('5'.code.toByte(), '2'.code.toByte(), 'm'.code.toByte())
            SGR.CROSSED_OUT -> writeCSISequenceToTerminal('9'.code.toByte(), 'm'.code.toByte())
            SGR.FRAKTUR -> writeCSISequenceToTerminal('2'.code.toByte(), '0'.code.toByte(), 'm'.code.toByte())
            SGR.REVERSE -> writeCSISequenceToTerminal('7'.code.toByte(), 'm'.code.toByte())
            SGR.UNDERLINE -> writeCSISequenceToTerminal('4'.code.toByte(), 'm'.code.toByte())
            SGR.ITALIC -> writeCSISequenceToTerminal('3'.code.toByte(), 'm'.code.toByte())
            null -> Unit
        }
    }

    @Throws(IOException::class)
    override fun disableSGR(sgr: SGR?) {
        when (sgr) {
            SGR.BLINK -> writeCSISequenceToTerminal('2'.code.toByte(), '5'.code.toByte(), 'm'.code.toByte())
            SGR.BOLD -> writeCSISequenceToTerminal('2'.code.toByte(), '2'.code.toByte(), 'm'.code.toByte())
            SGR.BORDERED -> writeCSISequenceToTerminal('5'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte())
            SGR.CIRCLED -> writeCSISequenceToTerminal('5'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte())
            SGR.CROSSED_OUT -> writeCSISequenceToTerminal('2'.code.toByte(), '9'.code.toByte(), 'm'.code.toByte())
            SGR.FRAKTUR -> writeCSISequenceToTerminal('2'.code.toByte(), '3'.code.toByte(), 'm'.code.toByte())
            SGR.REVERSE -> writeCSISequenceToTerminal('2'.code.toByte(), '7'.code.toByte(), 'm'.code.toByte())
            SGR.UNDERLINE -> writeCSISequenceToTerminal('2'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte())
            SGR.ITALIC -> writeCSISequenceToTerminal('2'.code.toByte(), '3'.code.toByte(), 'm'.code.toByte())
            null -> Unit
        }
    }

    @Throws(IOException::class)
    override fun resetColorAndSGR() {
        writeCSISequenceToTerminal('0'.code.toByte(), 'm'.code.toByte())
    }

    @Throws(IOException::class)
    override fun clearScreen() {
        writeCSISequenceToTerminal('2'.code.toByte(), 'J'.code.toByte())
    }

    @Throws(IOException::class)
    override fun enterPrivateMode() {
        if (inPrivateMode) {
            throw IllegalStateException("Cannot call enterPrivateMode() when already in private mode")
        }
        writeCSISequenceToTerminal(
            '?'.code.toByte(),
            '1'.code.toByte(),
            '0'.code.toByte(),
            '4'.code.toByte(),
            '9'.code.toByte(),
            'h'.code.toByte(),
        )
        if (requestedMouseCaptureMode != null) {
            mouseCaptureMode = requestedMouseCaptureMode
            updateMouseCaptureMode(mouseCaptureMode, 'h')
        }
        flush()
        inPrivateMode = true
    }

    @Throws(IOException::class)
    override fun exitPrivateMode() {
        if (!inPrivateMode) {
            throw IllegalStateException("Cannot call exitPrivateMode() when not in private mode")
        }
        resetColorAndSGR()
        setCursorVisible(true)
        writeCSISequenceToTerminal(
            '?'.code.toByte(),
            '1'.code.toByte(),
            '0'.code.toByte(),
            '4'.code.toByte(),
            '9'.code.toByte(),
            'l'.code.toByte(),
        )
        if (mouseCaptureMode != null) {
            updateMouseCaptureMode(mouseCaptureMode, 'l')
            mouseCaptureMode = null
        }
        flush()
        inPrivateMode = false
    }

    @Throws(IOException::class)
    override fun close() {
        if (isInPrivateMode()) {
            exitPrivateMode()
        }
        super.close()
    }

    @Throws(IOException::class)
    override fun setCursorPosition(x: Int, y: Int) {
        writeCSISequenceToTerminal(*("${y + 1};${x + 1}H").toByteArray())
    }

    @get:Synchronized
    @get:Throws(IOException::class)
    override var cursorPosition: TerminalPosition?
        get() {
            resetMemorizedCursorPosition()
            reportPosition()
            val terminalPosition = waitForCursorPositionReport() ?: TerminalPosition.OFFSET_1x1
            return terminalPosition.withRelative(-1, -1)
        }
        @Throws(IOException::class)
        set(position) {
            if (position != null) {
                setCursorPosition(position.column, position.row)
            }
        }

    @Throws(IOException::class)
    override fun setCursorVisible(visible: Boolean) {
        writeCSISequenceToTerminal(*("?25" + if (visible) "h" else "l").toByteArray())
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke {
        var keyStroke: KeyStroke?
        do {
            keyStroke = filterMouseEvents(super.readInput())
        } while (keyStroke == null)
        return keyStroke
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        return filterMouseEvents(super.pollInput())
    }

    private fun filterMouseEvents(keyStroke: KeyStroke?): KeyStroke? {
        if (keyStroke == null || keyStroke.keyType != KeyType.MOUSE_EVENT) {
            return keyStroke
        }

        val mouseAction = keyStroke as MouseAction
        when (mouseAction.actionType) {
            MouseActionType.CLICK_RELEASE -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK) {
                    return null
                }
            }
            MouseActionType.DRAG -> {
                if (
                    mouseCaptureMode == MouseCaptureMode.CLICK ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE
                ) {
                    return null
                }
            }
            MouseActionType.MOVE -> {
                if (
                    mouseCaptureMode == MouseCaptureMode.CLICK ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG
                ) {
                    return null
                }
            }
            else -> Unit
        }
        return mouseAction
    }

    override fun pushTitle() {
        throw UnsupportedOperationException("Not implemented yet")
    }

    override fun popTitle() {
        throw UnsupportedOperationException("Not implemented yet")
    }

    @Throws(IOException::class)
    override fun iconify() {
        writeCSISequenceToTerminal('2'.code.toByte(), 't'.code.toByte())
    }

    @Throws(IOException::class)
    override fun deiconify() {
        writeCSISequenceToTerminal('1'.code.toByte(), 't'.code.toByte())
    }

    @Throws(IOException::class)
    override fun maximize() {
        writeCSISequenceToTerminal('9'.code.toByte(), ';'.code.toByte(), '1'.code.toByte(), 't'.code.toByte())
    }

    @Throws(IOException::class)
    override fun unmaximize() {
        writeCSISequenceToTerminal('9'.code.toByte(), ';'.code.toByte(), '0'.code.toByte(), 't'.code.toByte())
    }

    @Throws(IOException::class)
    private fun updateMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?, lOrH: Char) {
        when (mouseCaptureMode) {
            null -> return
            MouseCaptureMode.CLICK -> {
                writeCSISequenceToTerminal('?'.code.toByte(), '9'.code.toByte(), lOrH.code.toByte())
            }
            MouseCaptureMode.CLICK_RELEASE -> {
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), lOrH.code.toByte(),
                )
            }
            MouseCaptureMode.CLICK_RELEASE_DRAG -> {
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '2'.code.toByte(), lOrH.code.toByte(),
                )
            }
            MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE -> {
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '3'.code.toByte(), lOrH.code.toByte(),
                )
            }
            MouseCaptureMode.CLICK_AUTODETECT -> {
                writeCSISequenceToTerminal('?'.code.toByte(), '9'.code.toByte(), lOrH.code.toByte())
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), lOrH.code.toByte(),
                )
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '2'.code.toByte(), lOrH.code.toByte(),
                )
                writeCSISequenceToTerminal(
                    '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '3'.code.toByte(), lOrH.code.toByte(),
                )
            }
        }
        writeCSISequenceToTerminal(
            '?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '6'.code.toByte(), lOrH.code.toByte(),
        )
    }

    @Throws(IOException::class)
    override fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        requestedMouseCaptureMode = mouseCaptureMode
        if (inPrivateMode && requestedMouseCaptureMode != this.mouseCaptureMode) {
            updateMouseCaptureMode(this.mouseCaptureMode, 'l')
            this.mouseCaptureMode = requestedMouseCaptureMode
            updateMouseCaptureMode(this.mouseCaptureMode, 'h')
        }
    }

    @Throws(IOException::class)
    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        var effectiveFirstLine = firstLine
        val csi = "\u001b["
        if (distance == 0) {
            return
        }
        if (effectiveFirstLine < 0) {
            effectiveFirstLine = 0
        }
        if (lastLine < effectiveFirstLine) {
            return
        }
        val sb = StringBuilder()
        sb.append(csi).append(effectiveFirstLine + 1).append(';').append(lastLine + 1).append('r')
        val target = if (distance > 0) lastLine else effectiveFirstLine
        sb.append(csi).append(target + 1).append(";1H")
        if (distance > 0) {
            val num = minOf(distance, lastLine - effectiveFirstLine + 1)
            repeat(num) { sb.append('\n') }
        } else {
            val num = minOf(-distance, lastLine - effectiveFirstLine + 1)
            repeat(num) { sb.append("\u001bM") }
        }
        sb.append(csi).append('r')
        writeToTerminal(*sb.toString().toByteArray())
    }

    internal fun isInPrivateMode(): Boolean {
        return inPrivateMode
    }

    @Throws(IOException::class)
    internal fun reportPosition() {
        writeCSISequenceToTerminal(*"6n".toByteArray())
    }

    @Throws(IOException::class)
    internal fun restoreCursorPosition() {
        writeCSISequenceToTerminal(*"u".toByteArray())
    }

    @Throws(IOException::class)
    internal fun saveCursorPosition() {
        writeCSISequenceToTerminal(*"s".toByteArray())
    }

    init {
        inputDecoder.addProfile(getDefaultKeyDecodingProfile())
    }
}
