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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

import java.io.IOException

@SuppressWarnings("rawtypes")
 class InputUITest:TestBase() {

@Override
 fun init(textGUI:WindowBasedTextGUI) {
val window = BasicWindow("Input test")

val interactable = object:AbstractInteractableComponent() {
private var lastKey:String? = null

@Override
protected fun handleKeyStroke(keyStroke:KeyStroke):Result? {
if (keyStroke!!.getKeyType() === KeyType.TAB)
{
return super.handleKeyStroke(keyStroke)
}
if (keyStroke!!.getKeyType() === KeyType.CHARACTER)
{
lastKey = keyStroke!!.getCharacter() + ""
}
else
{
lastKey = keyStroke!!.getKeyType().toString()
}
if (keyStroke!!.isCtrlDown())
{
lastKey += " + CTRL"
}
if (keyStroke!!.isAltDown())
{
lastKey += " + ALT"
}
if (keyStroke!!.isShiftDown())
{
lastKey += " + SHIFT"
}
return Result.HANDLED
}

@Override
protected fun createDefaultRenderer():InteractableRenderer? {
return object:InteractableRenderer() {
@Override
 fun getCursorLocation(component:Component?):TerminalPosition? {
val adjustedSize = component!!.getSize().withRelative(-1, -1)
return TerminalPosition(adjustedSize!!.getColumns(), adjustedSize!!.getRows())
}

@Override
 fun getPreferredSize(component:Component?):TerminalSize {
return TerminalSize(70, 5)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, component:Component?) {
graphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
graphics!!.setForegroundColor(TextColor.ANSI.WHITE)
graphics!!.fill(' ')
if (lastKey != null)
{
val leftPosition = 35 - (lastKey!!.length / 2)
graphics!!.putString(leftPosition, 2, lastKey)
}
}
}
}
}

window.setComponent(
Panels.vertical(
interactable.withBorder(Borders.doubleLineBevel("Press any key to test capturing the KeyStroke")), 
Label("Use the TAB key to shift focus"), 
Button("Close", Runnable({ window.close() }))))
textGUI.addWindow(window)
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
InputUITest().run(args)
}
}
}