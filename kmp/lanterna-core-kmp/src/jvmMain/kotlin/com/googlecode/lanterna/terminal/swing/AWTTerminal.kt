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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.TerminalResizeListener
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Panel
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import java.awt.im.InputMethodRequests
import java.text.AttributedCharacterIterator
import com.googlecode.lanterna.internal.compat.TimeUnit

@Suppress("serial")
class AWTTerminal : Panel, IOSafeTerminal {
    private val terminalImplementation: AWTTerminalImplementation
    private val inputMethodRequests: TerminalInputMethodRequests

    constructor() : this(TerminalScrollController.Null())

    @Suppress("WeakerAccess")
    constructor(scrollController: TerminalScrollController) : this(
        TerminalEmulatorDeviceConfiguration.default,
        AWTTerminalFontConfiguration.default,
        TerminalEmulatorColorConfiguration.default,
        scrollController,
    )

    constructor(
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
    ) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration)

    constructor(
        initialTerminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
    ) : this(
        initialTerminalSize,
        deviceConfiguration,
        fontConfiguration,
        colorConfiguration,
        TerminalScrollController.Null(),
    )

    constructor(
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        scrollController: TerminalScrollController,
    ) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration, scrollController)

    constructor(
        initialTerminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        scrollController: TerminalScrollController,
    ) {
        var resolvedDeviceConfiguration = deviceConfiguration
        var resolvedFontConfiguration = fontConfiguration
        var resolvedColorConfiguration = colorConfiguration

        if (resolvedDeviceConfiguration == null) {
            resolvedDeviceConfiguration = TerminalEmulatorDeviceConfiguration.default
        }
        if (resolvedFontConfiguration == null) {
            resolvedFontConfiguration = AWTTerminalFontConfiguration.default
        }
        if (resolvedColorConfiguration == null) {
            resolvedColorConfiguration = TerminalEmulatorColorConfiguration.default
        }

        enableInputMethods(true)
        addInputMethodListener(object : InputMethodListener {
            override fun inputMethodTextChanged(event: InputMethodEvent) = Unit

            override fun caretPositionChanged(event: InputMethodEvent) = Unit
        })

        terminalImplementation = AWTTerminalImplementation(
            this,
            resolvedFontConfiguration,
            initialTerminalSize,
            resolvedDeviceConfiguration,
            resolvedColorConfiguration,
            scrollController,
        )
        inputMethodRequests = TerminalInputMethodRequests(this, terminalImplementation)
    }

    val fontConfiguration: AWTTerminalFontConfiguration
        get() = terminalImplementation.fontConfiguration ?: AWTTerminalFontConfiguration.default

    val colorConfiguration: TerminalEmulatorColorConfiguration
        get() = terminalImplementation.colorConfiguration ?: TerminalEmulatorColorConfiguration.default

    val deviceConfiguration: TerminalEmulatorDeviceConfiguration
        get() = terminalImplementation.deviceConfiguration ?: TerminalEmulatorDeviceConfiguration.default

    @Synchronized
    override fun getPreferredSize(): Dimension {
        return requireNotNull(terminalImplementation.preferredSize)
    }

    @Synchronized
    override fun paint(componentGraphics: Graphics) {
        terminalImplementation.paintComponent(componentGraphics)
    }

    @Synchronized
    override fun update(componentGraphics: Graphics) {
        terminalImplementation.paintComponent(componentGraphics)
    }

    fun addInput(keyStroke: KeyStroke?) {
        terminalImplementation.addInput(keyStroke)
    }

    fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        terminalImplementation.setMouseCaptureMode(mouseCaptureMode)
    }

    override fun getInputMethodRequests(): InputMethodRequests {
        return inputMethodRequests
    }

    override fun processInputMethodEvent(e: InputMethodEvent) {
        val iterator: AttributedCharacterIterator = e.text ?: return
        repeat(e.committedCharacterCount) {
            terminalImplementation.addInput(KeyStroke(iterator.current(), false, false))
            iterator.next()
        }
    }

    override fun enterPrivateMode() {
        terminalImplementation.enterPrivateMode()
    }

    override fun exitPrivateMode() {
        terminalImplementation.exitPrivateMode()
    }

    override fun clearScreen() {
        terminalImplementation.clearScreen()
    }

    override fun setCursorPosition(x: Int, y: Int) {
        terminalImplementation.setCursorPosition(x, y)
    }

    override var cursorPosition: TerminalPosition?
        get() = terminalImplementation.cursorPosition
        set(position) {
            terminalImplementation.cursorPosition = position
        }

    override fun setCursorVisible(visible: Boolean) {
        terminalImplementation.setCursorVisible(visible)
    }

    override fun putCharacter(c: Char) {
        terminalImplementation.putCharacter(c)
    }

    override fun putString(string: String?) {
        terminalImplementation.putString(string)
    }

    override fun enableSGR(sgr: SGR?) {
        terminalImplementation.enableSGR(sgr)
    }

    override fun disableSGR(sgr: SGR?) {
        terminalImplementation.disableSGR(sgr)
    }

    override fun resetColorAndSGR() {
        terminalImplementation.resetColorAndSGR()
    }

    override fun setForegroundColor(color: TextColor?) {
        terminalImplementation.setForegroundColor(color)
    }

    override fun setBackgroundColor(color: TextColor?) {
        terminalImplementation.setBackgroundColor(color)
    }

    override val terminalSize: TerminalSize?
        get() = terminalImplementation.terminalSize

    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray? {
        return terminalImplementation.enquireTerminal(timeout, timeoutUnit)
    }

    override fun bell() {
        terminalImplementation.bell()
    }

    override fun flush() {
        terminalImplementation.flush()
    }

    override fun close() {
        terminalImplementation.close()
    }

    override fun pollInput(): KeyStroke? {
        return terminalImplementation.pollInput()
    }

    override fun readInput(): KeyStroke? {
        return terminalImplementation.readInput()
    }

    override fun newTextGraphics(): TextGraphics? {
        return terminalImplementation.newTextGraphics()
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        terminalImplementation.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        terminalImplementation.removeResizeListener(listener)
    }
}
