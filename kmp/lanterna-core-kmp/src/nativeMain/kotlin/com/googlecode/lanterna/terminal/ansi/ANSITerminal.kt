package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.DefaultKeyDecodingProfile
import com.googlecode.lanterna.input.InputDecoder
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import com.googlecode.lanterna.input.ScreenInfoCharacterPattern
import com.googlecode.lanterna.internal.compat.LambdaReader
import com.googlecode.lanterna.internal.compat.TimeUnit
import com.googlecode.lanterna.internal.concurrency.sleepCurrentThread
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.terminal.AbstractTerminal
import com.googlecode.lanterna.terminal.ExtendedTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.nativeposix.PosixTerminalIO
import com.googlecode.lanterna.terminal.nativeposix.PosixTerminalRuntime
import com.googlecode.lanterna.internal.compat.System as CompatSystem

open class ANSITerminal : AbstractTerminal(), ExtendedTerminal {
    private val keyQueue = ArrayDeque<KeyStroke>()
    private val inputDecoder =
        InputDecoder(
            LambdaReader(
                onRead = { PosixTerminalIO.readByte() ?: -1 },
                onReady = { PosixTerminalIO.hasInput() },
            ),
        )

    private var inPrivateMode = false
    private var rawModeEnabled = false
    private var lastKnownCursorPosition: TerminalPosition? = TerminalPosition.TOP_LEFT_CORNER
    private var lastReportedCursorPosition: TerminalPosition? = null
    private var requestedMouseCaptureMode: MouseCaptureMode? = null
    private var mouseCaptureMode: MouseCaptureMode? = null

    init {
        inputDecoder.addProfile(DefaultKeyDecodingProfile())
    }

    @Throws(IOException::class)
    override fun enterPrivateMode() {
        if (inPrivateMode) {
            throw IllegalStateException("Cannot call enterPrivateMode() when already in private mode")
        }
        writeCSI("?1049h")
        rawModeEnabled = PosixTerminalRuntime.configureRawModeNoEcho()
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
        writeCSI("?1049l")
        if (mouseCaptureMode != null) {
            updateMouseCaptureMode(mouseCaptureMode, 'l')
            mouseCaptureMode = null
        }
        flush()
        inPrivateMode = false
        restoreCookedModeIfNeeded()
    }

    @Throws(IOException::class)
    override fun clearScreen() {
        writeCSI("2J")
    }

    @Throws(IOException::class)
    override fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        writeCSI("${y + 1};${x + 1}H")
        lastKnownCursorPosition = TerminalPosition(x, y)
    }

    override var cursorPosition: TerminalPosition?
        get() {
            if (inPrivateMode) {
                lastReportedCursorPosition = null
                reportPosition()
                val reportedPosition = waitForCursorPositionReport(250)
                if (reportedPosition != null) {
                    lastKnownCursorPosition = reportedPosition.withRelative(-1, -1) ?: lastKnownCursorPosition
                }
            }
            return lastKnownCursorPosition
        }
        set(position) {
            if (position != null) {
                setCursorPosition(position.column, position.row)
            }
        }

    @Throws(IOException::class)
    override fun setCursorVisible(visible: Boolean) {
        writeCSI("?25" + if (visible) "h" else "l")
    }

    @Throws(IOException::class)
    override fun putCharacter(c: Char) {
        if (TerminalTextUtils.isPrintableCharacter(c)) {
            PosixTerminalIO.write(translateCharacter(c))
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

    @Throws(IOException::class)
    override fun enableSGR(sgr: SGR?) {
        when (sgr) {
            SGR.BLINK -> writeCSI("5m")
            SGR.BOLD -> writeCSI("1m")
            SGR.BORDERED -> writeCSI("51m")
            SGR.CIRCLED -> writeCSI("52m")
            SGR.CROSSED_OUT -> writeCSI("9m")
            SGR.FRAKTUR -> writeCSI("20m")
            SGR.REVERSE -> writeCSI("7m")
            SGR.UNDERLINE -> writeCSI("4m")
            SGR.ITALIC -> writeCSI("3m")
            null -> Unit
        }
    }

    @Throws(IOException::class)
    override fun disableSGR(sgr: SGR?) {
        when (sgr) {
            SGR.BLINK -> writeCSI("25m")
            SGR.BOLD -> writeCSI("22m")
            SGR.BORDERED -> writeCSI("54m")
            SGR.CIRCLED -> writeCSI("54m")
            SGR.CROSSED_OUT -> writeCSI("29m")
            SGR.FRAKTUR -> writeCSI("23m")
            SGR.REVERSE -> writeCSI("27m")
            SGR.UNDERLINE -> writeCSI("24m")
            SGR.ITALIC -> writeCSI("23m")
            null -> Unit
        }
    }

    @Throws(IOException::class)
    override fun resetColorAndSGR() {
        writeCSI("0m")
    }

    @Throws(IOException::class)
    override fun setForegroundColor(color: TextColor?) {
        val safeColor = color ?: TextColor.ANSI.DEFAULT
        val sequence = safeColor.foregroundSGRSequence?.decodeToString() ?: "39"
        writeCSI("${sequence}m")
    }

    @Throws(IOException::class)
    override fun setBackgroundColor(color: TextColor?) {
        val safeColor = color ?: TextColor.ANSI.DEFAULT
        val sequence = safeColor.backgroundSGRSequence?.decodeToString() ?: "49"
        writeCSI("${sequence}m")
    }

    override val terminalSize: TerminalSize?
        get() {
            val size = findTerminalSize()
            onResized(size)
            return size
        }

    @Throws(IOException::class)
    override fun enquireTerminal(
        timeout: Int,
        timeoutUnit: TimeUnit?,
    ): ByteArray {
        val effectiveTimeoutUnit = timeoutUnit ?: TimeUnit.MILLISECONDS
        PosixTerminalIO.writeByte(5)
        flush()

        val timeoutMillis = effectiveTimeoutUnit.toMillis(timeout.toLong()).coerceAtLeast(0L).toInt()
        if (!PosixTerminalIO.hasInput(timeoutMillis)) {
            return ByteArray(0)
        }

        val result = ArrayList<Byte>()
        while (PosixTerminalIO.hasInput(0)) {
            val value = PosixTerminalIO.readByte() ?: break
            result.add(value.toByte())
        }
        return result.toByteArray()
    }

    @Throws(IOException::class)
    override fun bell() {
        PosixTerminalIO.writeByte(7)
    }

    @Throws(IOException::class)
    override fun flush() {
        PosixTerminalIO.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        if (inPrivateMode) {
            try {
                exitPrivateMode()
            } catch (_: IllegalStateException) {
            }
        }
        restoreCookedModeIfNeeded()
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        return filterMouseEvents(readInputInternal(blocking = false, useKeyQueue = true))
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? {
        while (true) {
            val keyStroke = filterMouseEvents(readInputInternal(blocking = true, useKeyQueue = true))
            if (keyStroke != null) {
                return keyStroke
            }
        }
    }

    @Throws(IOException::class)
    override fun setTerminalSize(
        columns: Int,
        rows: Int,
    ) {
        writeCSI("8;$rows;$columns" + "t")
        onResized(TerminalSize(columns, rows))
    }

    @Throws(IOException::class)
    override fun setTitle(title: String?) {
        val safeTitle = (title ?: "").replace("\u0007", "")
        writeOSC("2;$safeTitle\u0007")
    }

    override fun pushTitle() {
        writeCSI("22;0t")
    }

    override fun popTitle() {
        writeCSI("23;0t")
    }

    @Throws(IOException::class)
    override fun iconify() {
        writeCSI("2t")
    }

    @Throws(IOException::class)
    override fun deiconify() {
        writeCSI("1t")
    }

    @Throws(IOException::class)
    override fun maximize() {
        writeCSI("9;1t")
    }

    @Throws(IOException::class)
    override fun unmaximize() {
        writeCSI("9;0t")
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
    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        var effectiveFirstLine = firstLine
        if (distance == 0) {
            return
        }
        if (effectiveFirstLine < 0) {
            effectiveFirstLine = 0
        }
        if (lastLine < effectiveFirstLine) {
            return
        }
        val sequenceBuilder = StringBuilder()
        sequenceBuilder.append("\u001b[").append(effectiveFirstLine + 1).append(';').append(lastLine + 1).append('r')
        val target = if (distance > 0) lastLine else effectiveFirstLine
        sequenceBuilder.append("\u001b[").append(target + 1).append(";1H")
        if (distance > 0) {
            repeat(minOf(distance, lastLine - effectiveFirstLine + 1)) { sequenceBuilder.append('\n') }
        } else {
            repeat(minOf(-distance, lastLine - effectiveFirstLine + 1)) { sequenceBuilder.append("\u001bM") }
        }
        sequenceBuilder.append("\u001b[r")
        PosixTerminalIO.write(sequenceBuilder.toString())
    }

    fun setInputTimeoutUnits(timeoutUnits: Int) {
        inputDecoder.setTimeoutUnits(timeoutUnits)
    }

    internal fun isInPrivateMode(): Boolean = inPrivateMode

    @Throws(IOException::class)
    internal fun reportPosition() {
        writeCSI("6n")
    }

    @Throws(IOException::class)
    internal fun saveCursorPosition() {
        writeCSI("s")
    }

    @Throws(IOException::class)
    internal fun restoreCursorPosition() {
        writeCSI("u")
    }

    @Throws(IOException::class)
    protected open fun findTerminalSize(): TerminalSize {
        val size = PosixTerminalRuntime.queryTerminalSize()
        return TerminalSize(size.columns, size.rows)
    }

    private fun writeCSI(sequence: String) {
        PosixTerminalIO.write("\u001b[$sequence")
    }

    private fun writeOSC(sequence: String) {
        PosixTerminalIO.write("\u001b]$sequence")
    }

    private fun restoreCookedModeIfNeeded() {
        if (rawModeEnabled) {
            PosixTerminalRuntime.restoreCookedMode()
            rawModeEnabled = false
        }
    }

    @Throws(IOException::class)
    private fun updateMouseCaptureMode(
        mouseCaptureMode: MouseCaptureMode?,
        mode: Char,
    ) {
        when (mouseCaptureMode) {
            null -> return
            MouseCaptureMode.CLICK -> writeCSI("?9$mode")
            MouseCaptureMode.CLICK_RELEASE -> writeCSI("?1000$mode")
            MouseCaptureMode.CLICK_RELEASE_DRAG -> writeCSI("?1002$mode")
            MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE -> writeCSI("?1003$mode")
            MouseCaptureMode.CLICK_AUTODETECT -> {
                writeCSI("?9$mode")
                writeCSI("?1000$mode")
                writeCSI("?1002$mode")
                writeCSI("?1003$mode")
            }
        }
        writeCSI("?1006$mode")
    }

    private fun filterMouseEvents(keyStroke: KeyStroke?): KeyStroke? {
        if (keyStroke == null || keyStroke.keyType != KeyType.MOUSE_EVENT) {
            return keyStroke
        }

        val mouseAction = keyStroke as MouseAction
        return when (mouseAction.actionType) {
            MouseActionType.CLICK_RELEASE -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK) null else mouseAction
            }
            MouseActionType.DRAG -> {
                if (mouseCaptureMode == MouseCaptureMode.CLICK || mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE) {
                    null
                } else {
                    mouseAction
                }
            }
            MouseActionType.MOVE -> {
                if (
                    mouseCaptureMode == MouseCaptureMode.CLICK ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE ||
                    mouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG
                ) {
                    null
                } else {
                    mouseAction
                }
            }
            else -> mouseAction
        }
    }

    @Throws(IOException::class)
    private fun readInputInternal(
        blocking: Boolean,
        useKeyQueue: Boolean,
    ): KeyStroke? {
        while (true) {
            if (useKeyQueue) {
                val queuedKey = keyQueue.removeFirstOrNull()
                if (queuedKey != null) {
                    return queuedKey
                }
            }

            val keyStroke = inputDecoder.getNextCharacter(blocking) ?: return null
            val report = ScreenInfoCharacterPattern.tryToAdopt(keyStroke)
            if (report != null) {
                lastReportedCursorPosition = report.position
                continue
            }
            return keyStroke
        }
    }

    private fun waitForCursorPositionReport(timeoutMillis: Long): TerminalPosition? {
        val deadline = CompatSystem.currentTimeMillis() + timeoutMillis
        while (CompatSystem.currentTimeMillis() < deadline) {
            if (lastReportedCursorPosition != null) {
                return lastReportedCursorPosition
            }
            val keyStroke = readInputInternal(blocking = false, useKeyQueue = false)
            if (keyStroke != null) {
                keyQueue.addLast(keyStroke)
            }
            if (lastReportedCursorPosition != null) {
                return lastReportedCursorPosition
            }
            sleepCurrentThread(1)
        }
        return null
    }

    private fun translateCharacter(input: Char): String {
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
            else -> input.toString()
        }
    }

    private fun convertToVT100(code: Char): String {
        return "\u001b(0$code\u001b(B"
    }
}
