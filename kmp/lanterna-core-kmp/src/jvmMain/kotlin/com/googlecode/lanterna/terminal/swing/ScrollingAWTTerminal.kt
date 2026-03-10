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
import com.googlecode.lanterna.terminal.TerminalResizeListener
import java.awt.BorderLayout
import java.awt.Container
import java.awt.EventQueue
import java.awt.Scrollbar
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import com.googlecode.lanterna.internal.compat.TimeUnit

@Suppress("serial")
class ScrollingAWTTerminal(
    deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    fontConfiguration: SwingTerminalFontConfiguration?,
    colorConfiguration: TerminalEmulatorColorConfiguration?,
) : Container(), IOSafeTerminal {
    private val awtTerminal: AWTTerminal
    private val scrollBar: Scrollbar

    @Volatile
    private var scrollModelUpdateBySystem: Boolean = false

    constructor() : this(
        TerminalEmulatorDeviceConfiguration.default,
        SwingTerminalFontConfiguration.default,
        TerminalEmulatorColorConfiguration.default,
    )

    init {
        scrollBar = Scrollbar(Scrollbar.VERTICAL)
        awtTerminal = AWTTerminal(deviceConfiguration, fontConfiguration, colorConfiguration, ScrollController())
        layout = BorderLayout()
        add(awtTerminal, BorderLayout.CENTER)
        add(scrollBar, BorderLayout.EAST)
        scrollBar.minimum = 0
        scrollBar.maximum = 20
        scrollBar.value = 0
        scrollBar.visibleAmount = 20
        scrollBar.addAdjustmentListener(ScrollbarListener())
    }

    override var cursorPosition: TerminalPosition?
        get() = awtTerminal.cursorPosition
        set(position) {
            awtTerminal.cursorPosition = position
        }

    override val terminalSize: TerminalSize?
        get() = awtTerminal.terminalSize

    private inner class ScrollController : TerminalScrollController {
        override var scrollingOffset: Int = 0
            private set

        override fun updateModel(totalSize: Int, screenSize: Int) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater { updateModel(totalSize, screenSize) }
                return
            }
            try {
                scrollModelUpdateBySystem = true
                var value = scrollBar.value
                var maximum = scrollBar.maximum
                var visibleAmount = scrollBar.visibleAmount

                if (maximum != totalSize) {
                    val lastMaximum = maximum
                    maximum = if (totalSize > screenSize) totalSize else screenSize
                    if (lastMaximum < maximum && lastMaximum - visibleAmount - value == 0) {
                        value = scrollBar.value + (maximum - lastMaximum)
                    }
                }
                if (value + screenSize > maximum) {
                    value = maximum - screenSize
                }
                if (visibleAmount != screenSize) {
                    if (visibleAmount > screenSize) {
                        value += visibleAmount - screenSize
                    }
                    visibleAmount = screenSize
                }
                if (value > maximum - visibleAmount) {
                    value = maximum - visibleAmount
                }
                if (value < 0) {
                    value = 0
                }

                scrollingOffset = value

                if (scrollBar.maximum != maximum) {
                    scrollBar.maximum = maximum
                }
                if (scrollBar.visibleAmount != visibleAmount) {
                    scrollBar.visibleAmount = visibleAmount
                }
                if (scrollBar.value != value) {
                    scrollBar.value = value
                }
            } finally {
                scrollModelUpdateBySystem = false
            }
        }
    }

    private inner class ScrollbarListener : AdjustmentListener {
        @Synchronized
        override fun adjustmentValueChanged(e: AdjustmentEvent) {
            if (!scrollModelUpdateBySystem) {
                awtTerminal.repaint()
            }
        }
    }

    fun addInput(keyStroke: KeyStroke?) {
        awtTerminal.addInput(keyStroke)
    }

    override fun pollInput(): KeyStroke? = awtTerminal.pollInput()

    override fun readInput(): KeyStroke? = awtTerminal.readInput()

    override fun enterPrivateMode() {
        awtTerminal.enterPrivateMode()
    }

    override fun exitPrivateMode() {
        awtTerminal.exitPrivateMode()
    }

    override fun clearScreen() {
        awtTerminal.clearScreen()
    }

    override fun setCursorPosition(x: Int, y: Int) {
        awtTerminal.setCursorPosition(x, y)
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

    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray? {
        return awtTerminal.enquireTerminal(timeout, timeoutUnit)
    }

    override fun bell() {
        awtTerminal.bell()
    }

    override fun flush() {
        awtTerminal.flush()
    }

    override fun close() {
        awtTerminal.close()
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        awtTerminal.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        awtTerminal.removeResizeListener(listener)
    }
}
