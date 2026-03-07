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
package com.googlecode.lanterna.gui2

import java.util.concurrent.CopyOnWriteArrayList

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke

/**
 * Simple labeled button that the user can trigger by pressing the Enter or the Spacebar key on the keyboard when the
 * component is in focus. You can specify an initial action through one of the constructors and you can also add
 * additional actions to the button using [.addListener]. To remove a previously attached action, use
 * [.removeListener].
 * @author Martin
 */
 class Button/**
 * Creates a new button with a specific label and no initially attached action.
 * @param label Label to put on the button
 */
    (label:String?):AbstractInteractableComponent<Button?>() {

private val listeners:List<Listener?>?
/**
 * Returns the label current assigned to the button
 * @return Label currently used by the button
 */
    /**
 * Updates the label on the button to the specified string
 * @param label New label to use on the button
 */
     var label:String? = null
@Synchronized set(label) {
var label = label
if (label == null)
{
throw IllegalArgumentException("null label to a button is not allowed")
}
if (label!!.isEmpty())
{
label = " "
}
field = label
invalidate()
}

 val cursorLocation:TerminalPosition?
@Override
@Synchronized get() {
return getRenderer().getCursorLocation(this)
}

 val accelerator:KeyStroke?
get() {
return super.getAccelerator()
}

/**
 * Listener interface that can be used to catch user events on the button
 */
     interface Listener {
/**
 * This is called when the user has triggered the button
 * @param button Button which was triggered
 */
         fun onTriggered(button:Button?) 
}

init{
this.listeners = CopyOnWriteArrayList()
label = label
}

/**
 * Creates a new button with a label and an associated action to fire when triggered by the user
 * @param label Label to put on the button
 * @param action Action to fire when the user triggers the button by pressing the enter or the space key
 */
     constructor(label:String?, action:Runnable?) : this(label) {
listeners!!.add({ button-> action!!.run() })
}

@Override
protected fun createDefaultRenderer():ButtonRenderer {
return DefaultButtonRenderer()
}

 fun setAccelerator(keyStroke:KeyStroke?):Button? {
return super.setAccelerator(keyStroke)
}

@Override
@Synchronized  fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isActivationStroke(keyStroke) || isKeyboardAcceleratorStroke(keyStroke))
{
getBasePane().setFocusedInteractable(this)
triggerActions()
return Result.HANDLED
}
return super.handleKeyStroke(keyStroke)
}

@Synchronized protected fun triggerActions() {
for (listener in listeners!!)
{
listener!!.onTriggered(this)
}
}

/**
 * Adds a listener to notify when the button is triggered; the listeners will be called serially in the order they
 * were added
 * @param listener Listener to call when the button is triggered
 */
     fun addListener(listener:Listener?) {
if (listener == null)
{
throw IllegalArgumentException("null listener to a button is not allowed")
}
listeners!!.add(listener)
}

/**
 * Removes a listener from the button's list of listeners to call when the button is triggered. If the listener list
 * doesn't contain the listener specified, this call do with do nothing.
 * @param listener Listener to remove from this button's listener list
 * @return `true` if this button contained the specified listener
 */
     fun removeListener(listener:Listener?):Boolean {
return listeners!!.remove(listener)
}

@Override
 fun toString():String? {
return "Button{" + this.label + "}"
}

/**
 * Helper interface that doesn't add any new methods but makes coding new button renderers a little bit more clear
 */
     interface ButtonRenderer:InteractableRenderer<Button?>

/**
 * This is the default button renderer that is used if you don't override anything. With this renderer, buttons are
 * drawn on a single line, with the label inside of "&lt;" and "&gt;".
 */
     class DefaultButtonRenderer:ButtonRenderer {
@Override
 fun getCursorLocation(button:Button):TerminalPosition? {
if (button.getThemeDefinition().isCursorVisible())
{
return TerminalPosition(1 + getLabelShift(button, button.getSize()), 0)
}
else
{
return null
}
}

@Override
 fun getPreferredSize(button:Button):TerminalSize {
return TerminalSize(Math.max(8, TerminalTextUtils.getColumnWidth(button.label) + 2), 1)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, button:Button) {
val themeDefinition = button.getThemeDefinition()
if (button.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getInsensitive())
}
graphics!!.fill(' ')
graphics!!.setCharacter(0, 0, themeDefinition!!.getCharacter("LEFT_BORDER", '<'))
graphics!!.setCharacter(graphics!!.getSize().getColumns() - 1, 0, themeDefinition!!.getCharacter("RIGHT_BORDER", '>'))

if (button.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
val labelShift = getLabelShift(button, graphics!!.getSize())
graphics!!.setCharacter(1 + labelShift, 0, button.label!!.charAt(0))

if (TerminalTextUtils.getColumnWidth(button.label) === 1)
{
return 
}
if (button.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics!!.putString(1 + labelShift + 1, 0, button.label!!.substring(1))
}

private fun getLabelShift(button:Button?, size:TerminalSize):Int {
val availableSpace = size.columns - 2
if (availableSpace <= 0)
{
return 0
}
var labelShift = 0
val widthInColumns = TerminalTextUtils.getColumnWidth(button!!.label)
if (availableSpace > widthInColumns)
{
labelShift = (size.columns - 2 - widthInColumns) / 2
}
return labelShift
}
}

/**
 * Alternative button renderer that displays buttons with just the label and minimal decoration
 */
     class FlatButtonRenderer:ButtonRenderer {
@Override
 fun getCursorLocation(component:Button?):TerminalPosition? {
return null
}

@Override
 fun getPreferredSize(component:Button):TerminalSize {
return TerminalSize(TerminalTextUtils.getColumnWidth(component.label), 1)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, button:Button) {
val themeDefinition = button.getThemeDefinition()
if (button.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getInsensitive())
}
graphics!!.fill(' ')
if (button.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics!!.putString(0, 0, button.label)
}
}

 class BorderedButtonRenderer:ButtonRenderer {
@Override
 fun getCursorLocation(component:Button?):TerminalPosition? {
return null
}

@Override
 fun getPreferredSize(component:Button):TerminalSize {
return TerminalSize(TerminalTextUtils.getColumnWidth(component.label) + 5, 4)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, button:Button) {
val themeDefinition = button.getThemeDefinition()
graphics.applyThemeStyle(themeDefinition!!.getNormal())
val size = graphics.getSize()
graphics.drawLine(1, 0, size!!.columns - 3, 0, Symbols.SINGLE_LINE_HORIZONTAL)
graphics.drawLine(1, size!!.rows - 2, size!!.columns - 3, size!!.rows - 2, Symbols.SINGLE_LINE_HORIZONTAL)
graphics.drawLine(0, 1, 0, size!!.rows - 3, Symbols.SINGLE_LINE_VERTICAL)
graphics.drawLine(size!!.columns - 2, 1, size!!.columns - 2, size!!.rows - 3, Symbols.SINGLE_LINE_VERTICAL)
graphics.setCharacter(0, 0, Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
graphics.setCharacter(size!!.columns - 2, 0, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
graphics.setCharacter(size!!.columns - 2, size!!.rows - 2, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
graphics.setCharacter(0, size!!.rows - 2, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)

 // Fill the inner part of the box
            graphics.drawLine(1, 1, size!!.columns - 3, 1, ' ')

 // Draw the text inside the button
            if (button.isFocused())
{
graphics.applyThemeStyle(themeDefinition!!.getActive())
}
graphics.putString(2, 1, TerminalTextUtils.fitString(button.label, size!!.columns - 5))

 // Draw the shadow
            graphics.applyThemeStyle(themeDefinition!!.getInsensitive())
graphics.drawLine(1, size!!.rows - 1, size!!.columns - 1, size!!.rows - 1, ' ')
graphics.drawLine(size!!.columns - 1, 1, size!!.columns - 1, size!!.rows - 2, ' ')
}
}
}
