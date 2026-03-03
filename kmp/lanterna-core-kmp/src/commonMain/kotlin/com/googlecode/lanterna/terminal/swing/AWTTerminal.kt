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
import java.util.concurrent.TimeUnit

@Suppress("serial")
open class AWTTerminal(
    initialTerminalSize: TerminalSize?,
    deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    fontConfiguration: AWTTerminalFontConfiguration?,
    colorConfiguration: TerminalEmulatorColorConfiguration?,
    scrollController: TerminalScrollController?
) : Panel(), IOSafeTerminal {

    private val terminalImplementation: AWTTerminalImplementation
    private val inputMethodRequests: TerminalInputMethodRequests

    constructor() : this(TerminalScrollController.Null())

    constructor(scrollController: TerminalScrollController?) : this(
        TerminalEmulatorDeviceConfiguration.getDefault(),
        AWTTerminalFontConfiguration.getDefault(),
        TerminalEmulatorColorConfiguration.getDefault(),
        scrollController
    )

    constructor(
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?
    ) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration)

    constructor(
        initialTerminalSize: TerminalSize?,
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?
    ) : this(
        initialTerminalSize,
        deviceConfiguration,
        fontConfiguration,
        colorConfiguration,
        TerminalScrollController.Null()
    )

    constructor(
        deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
        fontConfiguration: AWTTerminalFontConfiguration?,
        colorConfiguration: TerminalEmulatorColorConfiguration?,
        scrollController: TerminalScrollController?
    ) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration, scrollController)

    init {
        val resolvedDeviceConfiguration =
            deviceConfiguration ?: TerminalEmulatorDeviceConfiguration.getDefault()
        val resolvedFontConfiguration =
            fontConfiguration ?: SwingTerminalFontConfiguration.getDefault()
        val resolvedColorConfiguration =
            colorConfiguration ?: TerminalEmulatorColorConfiguration.getDefault()

        enableInputMethods(true)

        addInputMethodListener(object : InputMethodListener {
            override fun inputMethodTextChanged(event: InputMethodEvent) {
            }

            override fun caretPositionChanged(event: InputMethodEvent) {
            }
        })

        terminalImplementation = AWTTerminalImplementation(
            this,
            resolvedFontConfiguration,
            initialTerminalSize,
            resolvedDeviceConfiguration,
            resolvedColorConfiguration,
            scrollController
        )

        inputMethodRequests = TerminalInputMethodRequests(this, terminalImplementation)
    }

    open fun getFontConfiguration(): AWTTerminalFontConfiguration? {
        return terminalImplementation.getFontConfiguration()
    }

    open fun getColorConfiguration(): TerminalEmulatorColorConfiguration? {
        return terminalImplementation.getColorConfiguration()
    }

    open fun getDeviceConfiguration(): TerminalEmulatorDeviceConfiguration? {
        return terminalImplementation.getDeviceConfiguration()
    }

    @Synchronized
    override fun getPreferredSize(): Dimension {
        return terminalImplementation.getPreferredSize()
    }

    @Synchronized
    override fun paint(componentGraphics: Graphics) {
        terminalImplementation.paintComponent(componentGraphics)
    }

    @Synchronized
    override fun update(componentGraphics: Graphics) {
        terminalImplementation.paintComponent(componentGraphics)
    }

    open fun addInput(keyStroke: KeyStroke?) {
        terminalImplementation.addInput(keyStroke)
    }

    open fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        terminalImplementation.setMouseCaptureMode(mouseCaptureMode)
    }

    override fun getInputMethodRequests(): InputMethodRequests {
        return inputMethodRequests
    }

    override fun processInputMethodEvent(e: InputMethodEvent) {
        val iterator: AttributedCharacterIterator? = e.text
        for (i in 0 until e.committedCharacterCount) {
            val nonNullIterator = iterator ?: throw NullPointerException()
            terminalImplementation.addInput(KeyStroke(nonNullIterator.current(), false, false))
            nonNullIterator.next()
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

    override fun setCursorPosition(position: TerminalPosition?) {
        terminalImplementation.setCursorPosition(position)
    }

    override fun getCursorPosition(): TerminalPosition? {
        return terminalImplementation.getCursorPosition()
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

    override fun getTerminalSize(): TerminalSize? {
        return terminalImplementation.getTerminalSize()
    }

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
