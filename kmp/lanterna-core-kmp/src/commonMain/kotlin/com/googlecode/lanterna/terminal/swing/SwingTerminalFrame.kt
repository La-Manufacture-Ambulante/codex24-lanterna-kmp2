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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.TerminalResizeListener
import java.awt.BorderLayout
import java.awt.Color
import java.awt.HeadlessException
import java.util.EnumSet
import java.util.concurrent.TimeUnit
import javax.swing.JFrame

@Suppress("serial")
class SwingTerminalFrame : JFrame, IOSafeTerminal {
    val swingTerminal: SwingTerminal
    private val autoCloseTriggers: EnumSet<TerminalEmulatorAutoCloseTrigger>
    private var disposed: Boolean = false

    constructor(vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger) : this("SwingTerminalFrame", *autoCloseTriggers)

    @Throws(HeadlessException::class)
    constructor(title: String?, vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger) :
        this(title, SwingTerminal(), *autoCloseTriggers)

    constructor(
        title: String?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: SwingTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger,
    ) : this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, *autoCloseTriggers)

    constructor(
        title: String?,
        terminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: SwingTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger,
    ) : this(
        title,
        SwingTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration),
        *autoCloseTriggers,
    )

    private constructor(
        title: String?,
        swingTerminal: SwingTerminal,
        vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger,
    ) : super(title ?: "SwingTerminalFrame") {
        this.swingTerminal = swingTerminal
        this.autoCloseTriggers = if (autoCloseTriggers.isEmpty()) {
            EnumSet.noneOf(TerminalEmulatorAutoCloseTrigger::class.java)
        } else {
            EnumSet.copyOf(autoCloseTriggers.asList())
        }

        contentPane.layout = BorderLayout()
        contentPane.add(swingTerminal, BorderLayout.CENTER)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        background = Color.BLACK
    }

    val fontConfiguration: SwingTerminalFontConfiguration
        get() = swingTerminal.fontConfiguration

    val colorConfiguration: TerminalEmulatorColorConfiguration?
        get() = swingTerminal.colorConfiguration

    val deviceConfiguration: TerminalEmulatorDeviceConfiguration?
        get() = swingTerminal.deviceConfiguration

    val autoCloseTrigger: Set<TerminalEmulatorAutoCloseTrigger>
        get() = EnumSet.copyOf(autoCloseTriggers)

    fun setAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): SwingTerminalFrame {
        autoCloseTriggers.clear()
        if (autoCloseTrigger != null) {
            autoCloseTriggers.add(autoCloseTrigger)
        }
        return this
    }

    fun addAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): SwingTerminalFrame {
        if (autoCloseTrigger != null) {
            autoCloseTriggers.add(autoCloseTrigger)
        }
        return this
    }

    override fun dispose() {
        super.dispose()
        disposed = true
    }

    override fun pack() {
        super.pack()
        disposed = false
    }

    override fun setVisible(visible: Boolean) {
        if (visible) {
            pack()
            swingTerminal.requestFocusInWindow()
        }
        super.setVisible(visible)
    }

    override fun close() {
        dispose()
    }

    fun addInput(keyStroke: KeyStroke?) {
        swingTerminal.addInput(keyStroke)
    }

    override fun pollInput(): KeyStroke? {
        if (disposed) {
            return KeyStroke(KeyType.EOF)
        }
        val keyStroke = swingTerminal.pollInput()
        if (
            autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_ESCAPE) &&
            keyStroke != null &&
            keyStroke.keyType == KeyType.ESCAPE
        ) {
            dispose()
        }
        return keyStroke
    }

    override fun readInput(): KeyStroke? = swingTerminal.readInput()

    override fun enterPrivateMode() {
        swingTerminal.enterPrivateMode()
    }

    override fun exitPrivateMode() {
        swingTerminal.exitPrivateMode()
        if (autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE)) {
            dispose()
        }
    }

    override fun clearScreen() {
        swingTerminal.clearScreen()
    }

    override fun setCursorPosition(x: Int, y: Int) {
        swingTerminal.setCursorPosition(x, y)
    }

    override var cursorPosition: TerminalPosition?
        get() = swingTerminal.cursorPosition
        set(position) {
            swingTerminal.cursorPosition = position
        }

    override fun setCursorVisible(visible: Boolean) {
        swingTerminal.setCursorVisible(visible)
    }

    override fun putCharacter(c: Char) {
        swingTerminal.putCharacter(c)
    }

    override fun putString(string: String?) {
        swingTerminal.putString(string)
    }

    override fun newTextGraphics(): TextGraphics? = swingTerminal.newTextGraphics()

    override fun enableSGR(sgr: SGR?) {
        swingTerminal.enableSGR(sgr)
    }

    override fun disableSGR(sgr: SGR?) {
        swingTerminal.disableSGR(sgr)
    }

    override fun resetColorAndSGR() {
        swingTerminal.resetColorAndSGR()
    }

    override fun setForegroundColor(color: TextColor?) {
        swingTerminal.setForegroundColor(color)
    }

    override fun setBackgroundColor(color: TextColor?) {
        swingTerminal.setBackgroundColor(color)
    }

    override val terminalSize: TerminalSize?
        get() = swingTerminal.terminalSize

    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray? {
        return swingTerminal.enquireTerminal(timeout, timeoutUnit)
    }

    override fun bell() {
        swingTerminal.bell()
    }

    override fun flush() {
        swingTerminal.flush()
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.removeResizeListener(listener)
    }
}
