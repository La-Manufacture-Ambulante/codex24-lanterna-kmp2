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
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.SwingUtilities

@Suppress("serial")
class ScrollingSwingTerminal(
    deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    fontConfiguration: SwingTerminalFontConfiguration?,
    colorConfiguration: TerminalEmulatorColorConfiguration?,
) : JComponent(), IOSafeTerminal {
    private val swingTerminal: SwingTerminal
    private val scrollBar: JScrollBar

    @Volatile
    private var scrollModelUpdateBySystem: Boolean = false

    constructor() : this(
        TerminalEmulatorDeviceConfiguration.default,
        SwingTerminalFontConfiguration.default,
        TerminalEmulatorColorConfiguration.default,
    )

    init {
        scrollBar = JScrollBar(JScrollBar.VERTICAL)
        swingTerminal = SwingTerminal(deviceConfiguration, fontConfiguration, colorConfiguration, ScrollController())
        layout = BorderLayout()
        add(swingTerminal, BorderLayout.CENTER)
        add(scrollBar, BorderLayout.EAST)
        scrollBar.minimum = 0
        scrollBar.maximum = 20
        scrollBar.value = 0
        scrollBar.visibleAmount = 20
        scrollBar.addAdjustmentListener(ScrollbarListener())
    }

    override var cursorPosition: TerminalPosition?
        get() = swingTerminal.cursorPosition
        set(position) {
            swingTerminal.cursorPosition = position
        }

    override val terminalSize: TerminalSize?
        get() = swingTerminal.terminalSize

    private inner class ScrollController : TerminalScrollController {
        override var scrollingOffset: Int = 0
            private set

        override fun updateModel(
            totalSize: Int,
            screenSize: Int,
        ) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater { updateModel(totalSize, screenSize) }
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
                swingTerminal.repaint()
            }
        }
    }

    fun addInput(keyStroke: KeyStroke?) {
        swingTerminal.addInput(keyStroke)
    }

    override fun pollInput(): KeyStroke? = swingTerminal.pollInput()

    override fun readInput(): KeyStroke? = swingTerminal.readInput()

    override fun enterPrivateMode() {
        swingTerminal.enterPrivateMode()
    }

    override fun exitPrivateMode() {
        swingTerminal.exitPrivateMode()
    }

    override fun clearScreen() {
        swingTerminal.clearScreen()
    }

    override fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        swingTerminal.setCursorPosition(x, y)
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

    override fun enquireTerminal(
        timeout: Int,
        timeoutUnit: TimeUnit?,
    ): ByteArray? {
        return swingTerminal.enquireTerminal(timeout, timeoutUnit)
    }

    override fun bell() {
        swingTerminal.bell()
    }

    override fun flush() {
        swingTerminal.flush()
    }

    override fun close() {
        swingTerminal.close()
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        swingTerminal.removeResizeListener(listener)
    }
}
