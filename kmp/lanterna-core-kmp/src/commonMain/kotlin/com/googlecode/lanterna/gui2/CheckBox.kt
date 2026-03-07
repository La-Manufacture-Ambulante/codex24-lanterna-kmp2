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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType


/**
 * The checkbox component looks like a regular checkbox that you can find in modern graphics user interfaces, a label
 * and a space that the user can toggle on and off by using enter or space keys.
 * 
 * @author Martin
 */
 class CheckBox/**
 * Creates a new checkbox with a specific label, initially set to un-checked
 * @param label Label to assign to the check box
 */
     @JvmOverloads  constructor(private var label:String? = ""):AbstractInteractableComponent<CheckBox?>() {

private val listeners:List<Listener?>?
private var checked:Boolean = false

/**
 * Listener interface that can be used to catch user events on the check box
 */
     interface Listener {
/**
 * This is fired when the user has altered the checked state of this `CheckBox`
 * @param checked If the `CheckBox` is now toggled on, this is set to `true`, otherwise
 * `false`
 */
         fun onStatusChanged(checked:Boolean) 
}

init{
if (label == null)
{
throw IllegalArgumentException("Cannot create a CheckBox with null label")
}
else if (label!!.contains("\n") || label!!.contains("\r"))
{
throw IllegalArgumentException("Multiline checkbox labels are not supported")
}
this.listeners = CopyOnWriteArrayList()
this.checked = false
}

/**
 * Programmatically updated the check box to a particular checked state
 * @param checked If `true`, the check box will be set to toggled on, otherwise `false`
 * @return Itself
 */
    @Synchronized  fun setChecked(checked:Boolean):CheckBox {
this.checked = checked
runOnGUIThreadIfExistsOtherwiseRunDirect({ for (listener in listeners!!)
{
listener!!.onStatusChanged(checked)
} })
invalidate()
return this
}

/**
 * Returns the checked state of this check box
 * @return `true` if the check box is toggled on, otherwise `false`
 */
     fun isChecked():Boolean {
return checked
}

@Override
 fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isKeyboardActivationStroke(keyStroke))
{
setChecked(!isChecked())
return Result.HANDLED
}
else if (isMouseActivationStroke(keyStroke))
{
getBasePane().setFocusedInteractable(this)
setChecked(!isChecked())
return Result.HANDLED
}
return super.handleKeyStroke(keyStroke)
}

/**
 * Updates the label of the checkbox
 * @param label New label to assign to the check box
 * @return Itself
 */
    @Synchronized  fun setLabel(label:String?):CheckBox {
if (label == null)
{
throw IllegalArgumentException("Cannot set CheckBox label to null")
}
this.label = label
invalidate()
return this
}

/**
 * Returns the label of check box
 * @return Label currently assigned to the check box
 */
     fun getLabel():String? {
return label
}

/**
 * Adds a listener to this check box so that it will be notificed on certain user actions
 * @param listener Listener to fire events on
 * @return Itself
 */
     fun addListener(listener:Listener?):CheckBox {
if (listener != null && !listeners!!.contains(listener))
{
listeners!!.add(listener)
}
return this
}

/**
 * Removes a listener from this check box so that, if it was previously added, it will no long receive any events
 * @param listener Listener to remove from the check box
 * @return Itself
 */
     fun removeListener(listener:Listener?):CheckBox {
listeners!!.remove(listener)
return this
}

@Override
protected fun createDefaultRenderer():CheckBoxRenderer {
return DefaultCheckBoxRenderer()
}

/**
 * Helper interface that doesn't add any new methods but makes coding new check box renderers a little bit more clear
 */
    abstract class CheckBoxRenderer:InteractableRenderer<CheckBox?>

/**
 * The default renderer that is used unless overridden. This renderer will draw the checkbox label on the right side
 * of a "[ ]" block which will contain a "X" inside it if the check box has toggle status on
 */
     class DefaultCheckBoxRenderer:CheckBoxRenderer() {
@Override
 fun getCursorLocation(component:CheckBox):TerminalPosition? {
if (component.getThemeDefinition().isCursorVisible())
{
return CURSOR_LOCATION
}
else
{
return null
}
}

@Override
 fun getPreferredSize(component:CheckBox):TerminalSize {
var width = 3
if (!component.label!!.isEmpty())
{
width += 1 + TerminalTextUtils.getColumnWidth(component.label)
}
return TerminalSize(width, 1)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:CheckBox) {
val themeDefinition = component.getThemeDefinition()
if (component.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}

graphics!!.fill(' ')
graphics!!.putString(4, 0, component.label)

if (component.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getInsensitive())
}
graphics!!.setCharacter(0, 0, themeDefinition!!.getCharacter("LEFT_BRACKET", '['))
graphics!!.setCharacter(2, 0, themeDefinition!!.getCharacter("RIGHT_BRACKET", ']'))
graphics!!.setCharacter(3, 0, ' ')

if (component.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics!!.setCharacter(1, 0, (if (component.isChecked()) themeDefinition!!.getCharacter("MARKER", 'x') else ' '))
}

companion object {
private val CURSOR_LOCATION = TerminalPosition(1, 0)
}
}
}/**
 * Creates a new checkbox with no label, initially set to un-checked
 */
