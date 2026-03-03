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
import com.googlecode.lanterna.terminal.ExtendedTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Class containing graphics code for ANSI compliant text terminals and terminal emulators. All the methods inside of
 * this class uses ANSI escape codes written to the underlying output stream.
 *
 * @see <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">Wikipedia</a>
 * @author Martin
 */
abstract class ANSITerminal protected constructor(
    terminalInput: InputStream,
    terminalOutput: OutputStream,
    terminalCharset: Charset
) : StreamBasedTerminal(terminalInput, terminalOutput, terminalCharset), ExtendedTerminal {

    private var requestedMouseCaptureMode: MouseCaptureMode? = null
    private var mouseCaptureMode: MouseCaptureMode? = null
    private var inPrivateMode: Boolean = false

    init {
        getInputDecoder().addProfile(getDefaultKeyDecodingProfile())
    }

    /**
     * This method can be overridden in a custom terminal implementation to change the default key decoders.
     * @return The KeyDecodingProfile used by the terminal when translating character sequences to keystrokes
     */
    protected open fun getDefaultKeyDecodingProfile(): KeyDecodingProfile {
        return DefaultKeyDecodingProfile()
    }

    @Throws(IOException::class)
    private fun writeCSISequenceToTerminal(tail: ByteArray?) {
        val tailArray = tail ?: throw NullPointerException()
        val completeSequence = ByteArray(tailArray.size + 2)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = '['.code.toByte()
        System.arraycopy(tailArray, 0, completeSequence, 2, tailArray.size)
        writeToTerminal(completeSequence)
    }

    @Throws(IOException::class)
    private fun writeSGRSequenceToTerminal(sgrParameters: ByteArray?) {
        val sgrArray = sgrParameters ?: throw NullPointerException()
        val completeSequence = ByteArray(sgrArray.size + 3)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = '['.code.toByte()
        completeSequence[completeSequence.size - 1] = 'm'.code.toByte()
        System.arraycopy(sgrArray, 0, completeSequence, 2, sgrArray.size)
        writeToTerminal(completeSequence)
    }

    @Throws(IOException::class)
    private fun writeOSCSequenceToTerminal(tail: ByteArray?) {
        val tailArray = tail ?: throw NullPointerException()
        val completeSequence = ByteArray(tailArray.size + 2)
        completeSequence[0] = 0x1b.toByte()
        completeSequence[1] = ']'.code.toByte()
        System.arraycopy(tailArray, 0, completeSequence, 2, tailArray.size)
        writeToTerminal(completeSequence)
    }

    // Final because we handle the onResized logic here; extending classes should override #findTerminalSize instead
    @Synchronized
    @Throws(IOException::class)
    final override fun getTerminalSize(): TerminalSize {
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
        var terminalPosition: TerminalPosition? = waitForCursorPositionReport()
        if (terminalPosition == null) {
            terminalPosition = TerminalPosition(80, 24)
        }
        return TerminalSize(terminalPosition.getColumn(), terminalPosition.getRow())
    }

    @Throws(IOException::class)
    open override fun setTerminalSize(columns: Int, rows: Int) {
        writeCSISequenceToTerminal(("8;$rows;${columns}t").toByteArray(Charset.defaultCharset()))

        //We can't trust that the previous call was honoured by the terminal so force a re-query here, which will
        //trigger a resize event if one actually took place
        getTerminalSize()
    }

    @Throws(IOException::class)
    open override fun setTitle(title: String) {
        //The bell character is our 'null terminator', make sure there's none in the title
        val cleanTitle = title.replace("\u0007", "")
        writeOSCSequenceToTerminal(("2;$cleanTitle\u0007").toByteArray(Charset.defaultCharset()))
    }

    @Throws(IOException::class)
    open override fun setForegroundColor(color: TextColor) {
        writeSGRSequenceToTerminal(color.foregroundSGRSequence)
    }

    @Throws(IOException::class)
    open override fun setBackgroundColor(color: TextColor) {
        writeSGRSequenceToTerminal(color.backgroundSGRSequence)
    }

    @Throws(IOException::class)
    open override fun enableSGR(sgr: SGR) {
        when (sgr) {
            SGR.BLINK -> writeCSISequenceToTerminal(byteArrayOf('5'.code.toByte(), 'm'.code.toByte()))
            SGR.BOLD -> writeCSISequenceToTerminal(byteArrayOf('1'.code.toByte(), 'm'.code.toByte()))
            SGR.BORDERED -> writeCSISequenceToTerminal(byteArrayOf('5'.code.toByte(), '1'.code.toByte(), 'm'.code.toByte()))
            SGR.CIRCLED -> writeCSISequenceToTerminal(byteArrayOf('5'.code.toByte(), '2'.code.toByte(), 'm'.code.toByte()))
            SGR.CROSSED_OUT -> writeCSISequenceToTerminal(byteArrayOf('9'.code.toByte(), 'm'.code.toByte()))
            SGR.FRAKTUR -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '0'.code.toByte(), 'm'.code.toByte()))
            SGR.REVERSE -> writeCSISequenceToTerminal(byteArrayOf('7'.code.toByte(), 'm'.code.toByte()))
            SGR.UNDERLINE -> writeCSISequenceToTerminal(byteArrayOf('4'.code.toByte(), 'm'.code.toByte()))
            SGR.ITALIC -> writeCSISequenceToTerminal(byteArrayOf('3'.code.toByte(), 'm'.code.toByte()))
        }
    }

    @Throws(IOException::class)
    open override fun disableSGR(sgr: SGR) {
        when (sgr) {
            SGR.BLINK -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '5'.code.toByte(), 'm'.code.toByte()))
            SGR.BOLD -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '2'.code.toByte(), 'm'.code.toByte()))
            SGR.BORDERED -> writeCSISequenceToTerminal(byteArrayOf('5'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte()))
            SGR.CIRCLED -> writeCSISequenceToTerminal(byteArrayOf('5'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte()))
            SGR.CROSSED_OUT -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '9'.code.toByte(), 'm'.code.toByte()))
            SGR.FRAKTUR -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '3'.code.toByte(), 'm'.code.toByte()))
            SGR.REVERSE -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '7'.code.toByte(), 'm'.code.toByte()))
            SGR.UNDERLINE -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '4'.code.toByte(), 'm'.code.toByte()))
            SGR.ITALIC -> writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), '3'.code.toByte(), 'm'.code.toByte()))
        }
    }

    @Throws(IOException::class)
    open override fun resetColorAndSGR() {
        writeCSISequenceToTerminal(byteArrayOf('0'.code.toByte(), 'm'.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun clearScreen() {
        writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), 'J'.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun enterPrivateMode() {
        if (inPrivateMode) {
            throw IllegalStateException("Cannot call enterPrivateMode() when already in private mode")
        }
        writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '4'.code.toByte(), '9'.code.toByte(), 'h'.code.toByte()))
        if (requestedMouseCaptureMode != null) {
            this.mouseCaptureMode = requestedMouseCaptureMode
            updateMouseCaptureMode(this.mouseCaptureMode, 'h')
        }
        flush()
        inPrivateMode = true
    }

    @Throws(IOException::class)
    open override fun exitPrivateMode() {
        if (!inPrivateMode) {
            throw IllegalStateException("Cannot call exitPrivateMode() when not in private mode")
        }
        resetColorAndSGR()
        setCursorVisible(true)
        writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '4'.code.toByte(), '9'.code.toByte(), 'l'.code.toByte()))
        if (mouseCaptureMode != null) {
            updateMouseCaptureMode(this.mouseCaptureMode, 'l')
            this.mouseCaptureMode = null
        }
        flush()
        inPrivateMode = false
    }

    @Throws(IOException::class)
    open override fun close() {
        if (isInPrivateMode()) {
            exitPrivateMode()
        }
        super.close()
    }

    @Throws(IOException::class)
    open override fun setCursorPosition(x: Int, y: Int) {
        writeCSISequenceToTerminal("${y + 1};${x + 1}H".toByteArray(Charset.defaultCharset()))
    }

    @Throws(IOException::class)
    open override fun setCursorPosition(position: TerminalPosition) {
        setCursorPosition(position.column, position.row)
    }

    @Synchronized
    @Throws(IOException::class)
    open override fun getCursorPosition(): TerminalPosition {
        resetMemorizedCursorPosition()
        reportPosition()

        // ANSI terminal positions are 1-indexed so top-left corner is 1x1 instead of 0x0, that's why we need to adjust it here
        val terminalPosition = waitForCursorPositionReport() ?: TerminalPosition.OFFSET_1x1
        return terminalPosition.withRelative(-1, -1)
    }

    @Throws(IOException::class)
    open override fun setCursorVisible(visible: Boolean) {
        writeCSISequenceToTerminal(("?25" + if (visible) "h" else "l").toByteArray(Charset.defaultCharset()))
    }

    @Throws(IOException::class)
    open override fun readInput(): KeyStroke {
        var keyStroke: KeyStroke?
        do {
            // KeyStroke may because null by filterMouseEvents, so that's why we have the while(true) loop here
            keyStroke = filterMouseEvents(super.readInput())
        } while (keyStroke == null)
        return keyStroke
    }

    @Throws(IOException::class)
    open override fun pollInput(): KeyStroke? {
        return filterMouseEvents(super.pollInput())
    }

    private fun filterMouseEvents(keyStroke: KeyStroke?): KeyStroke? {
        //Remove bad input events from terminals that are not following the xterm protocol properly
        if (keyStroke == null || keyStroke.keyType != KeyType.MOUSE_EVENT) {
            return keyStroke
        }

        val mouseAction = keyStroke as MouseAction
        when (mouseAction.actionType) {
            MouseAction.Type.CLICK_RELEASE -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK) {
                    return null
                }
            }
            MouseAction.Type.DRAG -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE
                ) {
                    return null
                }
            }
            MouseAction.Type.MOVE -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG
                ) {
                    return null
                }
            }
            else -> {}
        }
        return mouseAction
    }

    open override fun pushTitle() {
        throw UnsupportedOperationException("Not implemented yet")
    }

    open override fun popTitle() {
        throw UnsupportedOperationException("Not implemented yet")
    }

    @Throws(IOException::class)
    open override fun iconify() {
        writeCSISequenceToTerminal(byteArrayOf('2'.code.toByte(), 't'.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun deiconify() {
        writeCSISequenceToTerminal(byteArrayOf('1'.code.toByte(), 't'.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun maximize() {
        writeCSISequenceToTerminal(byteArrayOf('9'.code.toByte(), ';'.code.toByte(), '1'.code.toByte(), 't'.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun unmaximize() {
        writeCSISequenceToTerminal(byteArrayOf('9'.code.toByte(), ';'.code.toByte(), '0'.code.toByte(), 't'.code.toByte()))
    }

    @Throws(IOException::class)
    private fun updateMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?, lOrH: Char) {
        if (mouseCaptureMode == null) {
            return
        }

        when (mouseCaptureMode) {
            MouseCaptureMode.CLICK -> {
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '9'.code.toByte(), lOrH.code.toByte()))
            }
            MouseCaptureMode.CLICK_RELEASE -> {
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), lOrH.code.toByte()))
            }
            MouseCaptureMode.CLICK_RELEASE_DRAG -> {
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '2'.code.toByte(), lOrH.code.toByte()))
            }
            MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE -> {
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '3'.code.toByte(), lOrH.code.toByte()))
            }
            MouseCaptureMode.CLICK_AUTODETECT -> {
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '9'.code.toByte(), lOrH.code.toByte()))
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), lOrH.code.toByte()))
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '2'.code.toByte(), lOrH.code.toByte()))
                writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '3'.code.toByte(), lOrH.code.toByte()))
            }
        }
        writeCSISequenceToTerminal(byteArrayOf('?'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '6'.code.toByte(), lOrH.code.toByte()))
    }

    @Throws(IOException::class)
    open override fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        requestedMouseCaptureMode = mouseCaptureMode
        if (inPrivateMode && requestedMouseCaptureMode != this.mouseCaptureMode) {
            updateMouseCaptureMode(this.mouseCaptureMode, 'l')
            this.mouseCaptureMode = requestedMouseCaptureMode
            updateMouseCaptureMode(this.mouseCaptureMode, 'h')
        }
    }

    @Throws(IOException::class)
    open override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        val CSI = "\u001B["

        var localFirstLine = firstLine
        var localLastLine = lastLine

        // some sanity checks:
        if (distance == 0) {
            return
        }
        if (localFirstLine < 0) {
            localFirstLine = 0
        }
        if (localLastLine < localFirstLine) {
            return
        }
        val sb = StringBuilder()

        // define range:
        sb.append(CSI).append(localFirstLine + 1)
            .append(';').append(localLastLine + 1).append('r')

        // place cursor on line to scroll away from:
        val target = if (distance > 0) localLastLine else localFirstLine
        sb.append(CSI).append(target + 1).append(";1H")

        // do scroll:
        if (distance > 0) {
            val num = Math.min(distance, localLastLine - localFirstLine + 1)
            for (i in 0 until num) {
                sb.append('\n')
            }
        } else { // distance < 0
            val num = Math.min(-distance, localLastLine - localFirstLine + 1)
            for (i in 0 until num) {
                sb.append("\u001BM")
            }
        }

        // reset range:
        sb.append(CSI).append('r')

        // off we go!
        writeToTerminal(sb.toString().toByteArray(Charset.defaultCharset()))
    }

    /**
     * Method to test if the terminal (as far as the library knows) is in private mode.
     *
     * @return True if there has been a call to enterPrivateMode() but not yet exitPrivateMode()
     */
    internal open fun isInPrivateMode(): Boolean {
        return inPrivateMode
    }

    @Throws(IOException::class)
    internal open fun reportPosition() {
        writeCSISequenceToTerminal("6n".toByteArray(Charset.defaultCharset()))
    }

    @Throws(IOException::class)
    internal open fun restoreCursorPosition() {
        writeCSISequenceToTerminal("u".toByteArray(Charset.defaultCharset()))
    }

    @Throws(IOException::class)
    internal open fun saveCursorPosition() {
        writeCSISequenceToTerminal("s".toByteArray(Charset.defaultCharset()))
    }
}
