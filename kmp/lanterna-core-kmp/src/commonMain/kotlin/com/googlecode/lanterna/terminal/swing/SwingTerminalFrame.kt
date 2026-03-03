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
import java.util.Arrays
import java.util.EnumSet
import java.util.Set
import java.util.concurrent.TimeUnit
import javax.swing.JFrame

@Suppress("serial")
open class SwingTerminalFrame private constructor(
    title: String?,
    private val swingTerminal: SwingTerminal,
    autoCloseTriggers: Array<out TerminalEmulatorAutoCloseTrigger>?
) : JFrame(title ?: "SwingTerminalFrame"), IOSafeTerminal {

    private val autoCloseTriggers: EnumSet<TerminalEmulatorAutoCloseTrigger>?
    private var disposed: Boolean = false

    constructor(autoCloseTriggers: Array<out TerminalEmulatorAutoCloseTrigger>?) : this(
        "SwingTerminalFrame",
        autoCloseTriggers
    )

    @Throws(HeadlessException::class)
    constructor(
        title: String?,
        autoCloseTriggers: Array<out TerminalEmulatorAutoCloseTrigger>?
    ) : this(title, SwingTerminal(), autoCloseTriggers)

    constructor(
        title: String?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: SwingTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        autoCloseTriggers: Array<out TerminalEmulatorAutoCloseTrigger>?
    ) : this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, autoCloseTriggers)

    constructor(
        title: String?,
        terminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: SwingTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        autoCloseTriggers: Array<out TerminalEmulatorAutoCloseTrigger>?
    ) : this(
        title,
        SwingTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration),
        autoCloseTriggers
    )

    init {
        this.autoCloseTriggers = if (autoCloseTriggers != null && autoCloseTriggers.isNotEmpty()) {
            EnumSet.copyOf(Arrays.asList(*autoCloseTriggers))
        } else {
            null
        }

        contentPane.layout = BorderLayout()
        contentPane.add(swingTerminal, BorderLayout.CENTER)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        background = Color.BLACK
    }

    open fun getSwingTerminal(): SwingTerminal {
        return swingTerminal
    }

    open fun getFontConfiguration(): SwingTerminalFontConfiguration? {
        return swingTerminal.fontConfiguration
    }

    open fun getColorConfiguration(): TerminalEmulatorColorConfiguration? {
        return swingTerminal.colorConfiguration
    }

    open fun getDeviceConfiguration(): TerminalEmulatorDeviceConfiguration? {
        return swingTerminal.deviceConfiguration
    }

    open fun getAutoCloseTrigger(): Set<TerminalEmulatorAutoCloseTrigger> {
        val triggers = autoCloseTriggers ?: throw NullPointerException()
        return EnumSet.copyOf(triggers)
    }

    open fun setAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): SwingTerminalFrame {
        val triggers = autoCloseTriggers ?: throw NullPointerException()
        triggers.clear()
        if (autoCloseTrigger != null) {
            triggers.add(autoCloseTrigger)
        }
        return this
    }

    open fun addAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): SwingTerminalFrame {
        if (autoCloseTrigger != null) {
            val triggers = autoCloseTriggers ?: throw NullPointerException()
            triggers.add(autoCloseTrigger)
        }
        return this
    }

    open override fun dispose() {
        super.dispose()
        disposed = true
    }

    open override fun pack() {
        super.pack()
        disposed = false
    }

    open override fun setVisible(visible: Boolean) {
        if (visible) {
            pack()
            swingTerminal.requestFocusInWindow()
        }
        super.setVisible(visible)
    }

    open override fun close() {
        dispose()
    }

    open fun addInput(keyStroke: KeyStroke?) {
        swingTerminal.addInput(keyStroke)
    }

    open override fun pollInput(): KeyStroke? {
        if (disposed) {
            return KeyStroke(KeyType.EOF)
        }
        val keyStroke = swingTerminal.pollInput()
        if (autoCloseTriggers != null &&
            autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_ESCAPE) &&
            keyStroke != null &&
            keyStroke.keyType == KeyType.ESCAPE
        ) {
            dispose()
        }
        return keyStroke
    }

    open override fun readInput(): KeyStroke? {
        return swingTerminal.readInput()
    }

    open override fun enterPrivateMode() {
        swingTerminal.enterPrivateMode()
    }

    open override fun exitPrivateMode() {
        swingTerminal.exitPrivateMode()
        if (autoCloseTriggers != null &&
            autoCloseTriggers.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE)
        ) {
            dispose()
        }
    }

    open override fun clearScreen() {
        swingTerminal.clearScreen()
    }

    open override fun setCursorPosition(x: Int, y: Int) {
        swingTerminal.setCursorPosition(x, y)
    }

    open override fun setCursorPosition(position: TerminalPosition?) {
        swingTerminal.setCursorPosition(position)
    }

    open override fun getCursorPosition(): TerminalPosition? {
        return swingTerminal.cursorPosition
    }

    open override fun setCursorVisible(visible: Boolean) {
        swingTerminal.setCursorVisible(visible)
    }

    open override fun putCharacter(c: Char) {
        swingTerminal.putCharacter(c)
    }

    open override fun putString(string: String?) {
        swingTerminal.putString(string)
    }

    open override fun newTextGraphics(): TextGraphics? {
        return swingTerminal.newTextGraphics()
    }

    open override fun enableSGR(sgr: SGR?) {
        swingTerminal.enableSGR(sgr)
    }

    open override fun disableSGR(sgr: SGR?) {
        swingTerminal.disableSGR(sgr)
    }

    open override fun resetColorAndSGR() {
        swingTerminal.resetColorAndSGR()
    }

    open override fun setForegroundColor(color: TextColor?) {
        swingTerminal.setForegroundColor(color)
    }

    open override fun setBackgroundColor(color: TextColor?) {
        swingTerminal.setBackgroundColor(color)
    }

    open override fun getTerminalSize(): TerminalSize? {
        return swingTerminal.terminalSize
    }

    open override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray? {
        return swingTerminal.enquireTerminal(timeout, timeoutUnit)
    }

    open override fun bell() {
        swingTerminal.bell()
    }

    open override fun flush() {
        swingTerminal.flush()
    }

    open override fun addResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.addResizeListener(listener)
    }

    open override fun removeResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.removeResizeListener(listener)
    }
}
