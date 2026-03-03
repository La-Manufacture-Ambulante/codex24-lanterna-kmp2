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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.terminal.MouseCaptureMode
import java.awt.AWTKeyStroke
import java.awt.Dimension
import java.awt.Font
import java.awt.KeyboardFocusManager
import java.awt.event.HierarchyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Collections
import javax.swing.JComponent
import javax.swing.SwingUtilities

/**
 * Concrete implementation of [GraphicalTerminalImplementation] that adapts it to Swing
 */
internal class SwingTerminalImplementation internal constructor(
    private val component: JComponent,
    private val fontConfiguration: SwingTerminalFontConfiguration,
    initialTerminalSize: TerminalSize?,
    deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    colorConfiguration: TerminalEmulatorColorConfiguration?,
    scrollController: TerminalScrollController?
) : GraphicalTerminalImplementation(
    initialTerminalSize,
    deviceConfiguration,
    colorConfiguration,
    scrollController
) {
    private var mouseListener: MouseAdapter? = null

    init {
        // Prevent us from shrinking beyond one character
        component.minimumSize = Dimension(fontConfiguration.fontWidth, fontConfiguration.fontHeight)

        component.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.emptySet<AWTKeyStroke>()
        )
        component.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.emptySet<AWTKeyStroke>()
        )

        // Make sure the component is double-buffered to prevent flickering
        component.isDoubleBuffered = true

        component.addKeyListener(TerminalInputListener())

        // Mouse support
        updateMouseCaptureMode(this.mouseCaptureMode)

        component.addHierarchyListener { e ->
            if (e.changeFlags == HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) {
                if (e.changed.isDisplayable) {
                    onCreated()
                } else {
                    onDestroyed()
                }
            }
        }
    }

    override protected fun updateMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        if (this.mouseListener != null) {
            component.removeMouseListener(this.mouseListener)
            component.removeMouseWheelListener(this.mouseListener)
            component.removeMouseMotionListener(this.mouseListener)
        }
        this.mouseListener = object : TerminalMouseListener(this.mouseCaptureMode) {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)
                this@SwingTerminalImplementation.component.requestFocusInWindow()
            }
        }
        component.addMouseListener(this.mouseListener)
        component.addMouseWheelListener(this.mouseListener)
        component.addMouseMotionListener(this.mouseListener)
    }

    /**
     * Returns the current font configuration. Note that it is immutable and cannot be changed.
     * @return This SwingTerminal's current font configuration
     */
    fun getFontConfiguration(): SwingTerminalFontConfiguration {
        return fontConfiguration
    }

    override protected fun getFontHeight(): Int {
        return fontConfiguration.fontHeight
    }

    override protected fun getFontWidth(): Int {
        return fontConfiguration.fontWidth
    }

    override protected fun getHeight(): Int {
        return component.height
    }

    override protected fun getWidth(): Int {
        return component.width
    }

    override protected fun getFontForCharacter(character: TextCharacter?): Font {
        return fontConfiguration.getFontForCharacter(character)
    }

    override protected fun isTextAntiAliased(): Boolean {
        return fontConfiguration.isAntiAliased
    }

    override protected fun repaint() {
        if (SwingUtilities.isEventDispatchThread()) {
            component.repaint()
        } else {
            SwingUtilities.invokeLater(component::repaint)
        }
    }

    override fun readInput(): com.googlecode.lanterna.input.KeyStroke? {
        if (SwingUtilities.isEventDispatchThread()) {
            throw UnsupportedOperationException("Cannot call SwingTerminal.readInput() on the AWT thread")
        }
        return super.readInput()
    }
}
