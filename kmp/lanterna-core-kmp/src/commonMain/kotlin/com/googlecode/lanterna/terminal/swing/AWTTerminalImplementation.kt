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

import java.awt.*
import java.awt.event.*
import java.util.Collections

/**
 * AWT implementation of [GraphicalTerminalImplementation] that contains all the overrides for AWT
 * Created by martin on 08/02/16.
 */
internal class AWTTerminalImplementation/**
 * Creates a new `AWTTerminalImplementation`
 * @param component Component that is the AWT terminal surface
 * @param fontConfiguration Font configuration to use
 * @param initialTerminalSize Initial size of the terminal
 * @param deviceConfiguration Device configuration
 * @param colorConfiguration Color configuration
 * @param scrollController Controller to be used when inspecting scroll status
 */
    (
private val component:Component?, 
 val fontConfiguration:AWTTerminalFontConfiguration?, 
initialTerminalSize:TerminalSize?, 
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?, 
scrollController:TerminalScrollController?):GraphicalTerminalImplementation(initialTerminalSize, deviceConfiguration, colorConfiguration, scrollController) {
private var mouseListener:MouseAdapter? = null

protected val fontHeight:Int
@Override
get() {
return fontConfiguration!!.getFontHeight()
}

protected val fontWidth:Int
@Override
get() {
return fontConfiguration!!.getFontWidth()
}

protected val height:Int
@Override
get() {
return component!!.getHeight()
}

protected val width:Int
@Override
get() {
return component!!.getWidth()
}

protected val isTextAntiAliased:Boolean
@Override
get() {
return fontConfiguration!!.isAntiAliased()
}

init{

 //Prevent us from shrinking beyond one character
        component.setMinimumSize(Dimension(fontConfiguration.getFontWidth(), fontConfiguration.getFontHeight()))

component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet<AWTKeyStroke?>())
component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet<AWTKeyStroke?>())

component.addKeyListener(TerminalInputListener())

 //Mouse support
        updateMouseCaptureMode(this.mouseCaptureMode)

component.addHierarchyListener({ e-> if (e!!.getChangeFlags() === HierarchyEvent.DISPLAYABILITY_CHANGED)
{
if (e!!.getChanged().isDisplayable())
{
onCreated()
}
else
{
onDestroyed()
}
} })
}

@Override
protected fun updateMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?) {
if (this.mouseListener != null)
{
component!!.removeMouseListener(this.mouseListener)
component!!.removeMouseWheelListener(this.mouseListener)
component!!.removeMouseMotionListener(this.mouseListener)
}
this.mouseListener = object:TerminalMouseListener(this.mouseCaptureMode) {
@Override
 fun mouseClicked(e:MouseEvent?) {
super.mouseClicked(e)
this@AWTTerminalImplementation.component!!.requestFocusInWindow()
}
}
component!!.addMouseListener(this.mouseListener)
component!!.addMouseWheelListener(this.mouseListener)
component!!.addMouseMotionListener(this.mouseListener)
}

@Override
protected fun getFontForCharacter(character:TextCharacter?):Font? {
return fontConfiguration!!.getFontForCharacter(character)
}

@Override
protected fun repaint() {
if (EventQueue.isDispatchThread())
{
component!!.repaint()
}
else
{
EventQueue.invokeLater(???({ component!!.repaint() }))
}
}

@Override
 fun readInput():KeyStroke? {
if (EventQueue.isDispatchThread())
{
throw UnsupportedOperationException("Cannot call SwingTerminal.readInput() on the AWT thread")
}
return super.readInput()
}
}
