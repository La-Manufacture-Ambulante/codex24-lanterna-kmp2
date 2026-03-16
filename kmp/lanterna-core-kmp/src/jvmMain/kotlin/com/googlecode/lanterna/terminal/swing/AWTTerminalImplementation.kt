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
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.MouseCaptureMode
import java.awt.AWTKeyStroke
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.awt.KeyboardFocusManager
import java.awt.event.HierarchyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Collections

/**
 * AWT implementation of [GraphicalTerminalImplementation] that contains all the overrides for AWT
 * Created by martin on 08/02/16.
 */
internal class AWTTerminalImplementation(
    private val component: Component,
    val fontConfiguration: AWTTerminalFontConfiguration,
    initialTerminalSize: TerminalSize?,
    deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    colorConfiguration: TerminalEmulatorColorConfiguration?,
    scrollController: TerminalScrollController?,
) : GraphicalTerminalImplementation(initialTerminalSize, deviceConfiguration, colorConfiguration, scrollController) {
    private var mouseListener: MouseAdapter? = null

    init {
        component.minimumSize = Dimension(fontConfiguration.fontWidth, fontConfiguration.fontHeight)
        component.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.emptySet<AWTKeyStroke>(),
        )
        component.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.emptySet<AWTKeyStroke>(),
        )
        component.addKeyListener(TerminalInputListener())
        updateMouseCaptureMode(activeMouseCaptureMode)
        component.addHierarchyListener { event ->
            if (event.changeFlags == HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) {
                if (event.changed.isDisplayable) {
                    onCreated()
                } else {
                    onDestroyed()
                }
            }
        }
    }

    override val fontHeight: Int
        get() = fontConfiguration.fontHeight

    override val fontWidth: Int
        get() = fontConfiguration.fontWidth

    override val height: Int
        get() = component.height

    override val width: Int
        get() = component.width

    override val isTextAntiAliased: Boolean
        get() = fontConfiguration.isAntiAliased()

    override fun updateMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        mouseListener?.let {
            component.removeMouseListener(it)
            component.removeMouseWheelListener(it)
            component.removeMouseMotionListener(it)
        }
        mouseListener =
            object : TerminalMouseListener(this.activeMouseCaptureMode) {
                override fun mouseClicked(e: MouseEvent) {
                    super.mouseClicked(e)
                    component.requestFocusInWindow()
                }
            }
        component.addMouseListener(mouseListener)
        component.addMouseWheelListener(mouseListener)
        component.addMouseMotionListener(mouseListener)
    }

    override fun getFontForCharacter(character: TextCharacter): Font {
        return fontConfiguration.getFontForCharacter(character)
    }

    override fun repaint() {
        if (EventQueue.isDispatchThread()) {
            component.repaint()
        } else {
            EventQueue.invokeLater(component::repaint)
        }
    }

    override fun readInput(): KeyStroke {
        if (EventQueue.isDispatchThread()) {
            throw UnsupportedOperationException("Cannot call SwingTerminal.readInput() on the AWT thread")
        }
        return super.readInput()
    }
}
