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
import com.googlecode.lanterna.internal.compat.TimeUnit
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.TerminalResizeListener
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Frame
import java.awt.HeadlessException
import java.util.EnumSet

open class AWTTerminalFrame : Frame, IOSafeTerminal {
    val awtTerminal: AWTTerminal
    private val autoCloseTriggers: EnumSet<TerminalEmulatorAutoCloseTrigger>
    private var disposed: Boolean = false

    constructor(vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger) : this("AwtTerminalFrame", *autoCloseTriggers)

    @Throws(HeadlessException::class)
    constructor(title: String?, vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger) :
        this(title, AWTTerminal(), *autoCloseTriggers)

    constructor(
        title: String?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger,
    ) : this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, *autoCloseTriggers)

    constructor(
        title: String?,
        terminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        vararg autoCloseTriggers: TerminalEmulatorAutoCloseTrigger,
    ) : this(
        title,
        AWTTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration),
        *autoCloseTriggers,
    )

    private constructor(
        title: String?,
        awtTerminal: AWTTerminal,
        vararg autoCloseTrigger: TerminalEmulatorAutoCloseTrigger,
    ) : super(title ?: "AWTTerminalFrame") {
        this.awtTerminal = awtTerminal
        this.autoCloseTriggers =
            if (autoCloseTrigger.isEmpty()) {
                EnumSet.noneOf(TerminalEmulatorAutoCloseTrigger::class.java)
            } else {
                EnumSet.copyOf(autoCloseTrigger.asList())
            }

        layout = BorderLayout()
        add(awtTerminal, BorderLayout.CENTER)
        background = Color.BLACK
        pack()
        awtTerminal.requestFocusInWindow()
    }

    val fontConfiguration: AWTTerminalFontConfiguration
        get() = awtTerminal.fontConfiguration

    val colorConfiguration: TerminalEmulatorColorConfiguration
        get() = awtTerminal.colorConfiguration

    val deviceConfiguration: TerminalEmulatorDeviceConfiguration
        get() = awtTerminal.deviceConfiguration

    val autoCloseTrigger: Set<TerminalEmulatorAutoCloseTrigger>
        get() = EnumSet.copyOf(autoCloseTriggers)

    fun addAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger) {
        autoCloseTriggers.add(autoCloseTrigger)
    }

    override fun dispose() {
        super.dispose()
        disposed = true
    }

    override fun close() {
        dispose()
    }

    fun addInput(keyStroke: KeyStroke?) {
        awtTerminal.addInput(keyStroke)
    }

    override fun pollInput(): KeyStroke? {
        if (disposed) {
            return KeyStroke(KeyType.EOF)
        }
        val keyStroke = awtTerminal.pollInput()
        if (
            autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_ESCAPE) &&
            keyStroke != null &&
            keyStroke.keyType == KeyType.ESCAPE
        ) {
            dispose()
        }
        return keyStroke
    }

    override fun readInput(): KeyStroke? = awtTerminal.readInput()

    override fun enterPrivateMode() {
        awtTerminal.enterPrivateMode()
    }

    override fun exitPrivateMode() {
        awtTerminal.exitPrivateMode()
        if (autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE)) {
            dispose()
        }
    }

    override fun clearScreen() {
        awtTerminal.clearScreen()
    }

    override fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        awtTerminal.setCursorPosition(x, y)
    }

    override var cursorPosition: TerminalPosition?
        get() = awtTerminal.cursorPosition
        set(position) {
            awtTerminal.cursorPosition = position
        }

    override fun setCursorVisible(visible: Boolean) {
        awtTerminal.setCursorVisible(visible)
    }

    override fun putCharacter(c: Char) {
        awtTerminal.putCharacter(c)
    }

    override fun putString(string: String?) {
        awtTerminal.putString(string)
    }

    override fun newTextGraphics(): TextGraphics? = awtTerminal.newTextGraphics()

    override fun enableSGR(sgr: SGR?) {
        awtTerminal.enableSGR(sgr)
    }

    override fun disableSGR(sgr: SGR?) {
        awtTerminal.disableSGR(sgr)
    }

    override fun resetColorAndSGR() {
        awtTerminal.resetColorAndSGR()
    }

    override fun setForegroundColor(color: TextColor?) {
        awtTerminal.setForegroundColor(color)
    }

    override fun setBackgroundColor(color: TextColor?) {
        awtTerminal.setBackgroundColor(color)
    }

    override val terminalSize: TerminalSize?
        get() = awtTerminal.terminalSize

    override fun enquireTerminal(
        timeout: Int,
        timeoutUnit: TimeUnit?,
    ): ByteArray? {
        return awtTerminal.enquireTerminal(timeout, timeoutUnit)
    }

    override fun bell() {
        awtTerminal.bell()
    }

    override fun flush() {
        awtTerminal.flush()
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        awtTerminal.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        awtTerminal.removeResizeListener(listener)
    }
}
