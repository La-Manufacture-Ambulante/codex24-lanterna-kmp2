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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This is a list box implementation where each item has its own checked state that can be toggled on and off
 * @author Martin
 */
 class CheckBoxList<V>/**
 * Creates a new `CheckBoxList` that is initially empty and has a pre-defined size that it will request. If
 * there are more items that can fit in this size, the list box will use scrollbars.
 * @param preferredSize Size the list box should request, no matter how many items it contains
 */
     @JvmOverloads  constructor(preferredSize:TerminalSize? = null):AbstractListBox<V?, CheckBoxList<V?>?>(preferredSize) {

private val listeners:List<Listener?>?
private val itemStatus:List<Boolean?>?

 // this is used during mouse dragged to assign all items to the same state
    private var stateForMouseDragged:Boolean = false
private var minIndexForMouseDragged:Int = 0
private var maxIndexForMouseDragged:Int = 0

/**
 * Returns all the items in the list box that have checked state, as a list
 * @return List of all items in the list box that has checked state on
 */
     val checkedItems:List<V?>?
@Synchronized get() {
val result = ArrayList()
for (i in 0 until itemStatus!!.size())
{
if (itemStatus!!.get(i))
{
result.add(getItemAt(i))
}
}
return result
}
/**
 * Listener interface that can be attached to the `CheckBoxList` in order to be notified on user actions
 */
     interface Listener {
/**
 * Called by the `CheckBoxList` when the user changes the toggle state of one item
 * @param itemIndex Index of the item that was toggled
 * @param checked If the state of the item is now checked, this will be `true`, otherwise `false`
 */
         fun onStatusChanged(itemIndex:Int, checked:Boolean) 
}

init{
this.listeners = CopyOnWriteArrayList()
this.itemStatus = ArrayList()
}

@Override
protected fun createDefaultListItemRenderer():ListItemRenderer<V?, CheckBoxList<V?>?>? {
return CheckBoxListItemRenderer<Object?>()
}

@Override
@Synchronized  fun clearItems():CheckBoxList<V?>? {
itemStatus!!.clear()
return super.clearItems()
}

@Override
 fun addItem(`object`:V?):CheckBoxList<V?>? {
return addItem(`object`, false)
}

@Override
@Synchronized  fun removeItem(index:Int):V? {
val item = super.removeItem(index)
itemStatus!!.remove(index)
return item
}

/**
 * Adds an item to the checkbox list with an explicit checked status
 * @param object Object to add to the list
 * @param checkedState If `true`, the new item will be initially checked
 * @return Itself
 */
    @Synchronized  fun addItem(`object`:V?, checkedState:Boolean):CheckBoxList<V?>? {
itemStatus!!.add(checkedState)
return super.addItem(`object`)
}

/**
 * Checks if a particular item is part of the check box list and returns a boolean value depending on the toggle
 * state of the item.
 * @param object Object to check the status of
 * @return If the item wasn't found in the list box, `null` is returned, otherwise `true` or
 * `false` depending on checked state of the item
 */
    @Synchronized  fun isChecked(`object`:V?):Boolean? {
if (indexOf(`object`) === -1)
return null

return itemStatus!!.get(indexOf(`object`))
}

/**
 * Checks if a particular item is part of the check box list and returns a boolean value depending on the toggle
 * state of the item.
 * @param index Index of the item to check the status of
 * @return If the index was not valid in the list box, `null` is returned, otherwise `true` or
 * `false` depending on checked state of the item at that index
 */
    @Synchronized  fun isChecked(index:Int):Boolean? {
if (index < 0 || index >= itemStatus!!.size())
return null

return itemStatus!!.get(index)
}

/**
 * Programmatically sets the checked state of an item in the list box.
 * If the state was already true, it is set to false, otherwise it is set to true.
 * @param index Index of the item to toggle the status of
 * @return Itself
 */
    @Synchronized  fun toggleChecked(index:Int):CheckBoxList<V?>? {
setChecked(index, (!isChecked(index))!!)
return self()
}

/**
 * Programmatically sets the checked state of an item in the list box
 * @param object Object to set the checked state of
 * @param checked If `true`, then the item is set to checked, otherwise not
 * @return Itself
 */
    @Synchronized  fun setChecked(`object`:V?, checked:Boolean):CheckBoxList<V?>? {
val index = indexOf(`object`)
if (index != -1)
{
setChecked(index, checked)
}
return self()
}

private fun setChecked(index:Int, checked:Boolean) {
if (!(0 <= index && index < itemStatus!!.size()))
{
return 
}
itemStatus!!.set(index, checked)
runOnGUIThreadIfExistsOtherwiseRunDirect({ for (listener in listeners!!)
{
listener!!.onStatusChanged(index, checked)
} })
}

/**
 * Adds a new listener to the `CheckBoxList` that will be called on certain user actions
 * @param listener Listener to attach to this `CheckBoxList`
 * @return Itself
 */
    @Synchronized  fun addListener(listener:Listener?):CheckBoxList<V?> {
if (listener != null && !listeners!!.contains(listener))
{
listeners!!.add(listener)
}
return this
}

/**
 * Removes a listener from this `CheckBoxList` so that if it had been added earlier, it will no longer be
 * called on user actions
 * @param listener Listener to remove from this `CheckBoxList`
 * @return Itself
 */
     fun removeListener(listener:Listener?):CheckBoxList<V?> {
listeners!!.remove(listener)
return this
}

@Override
@Synchronized  fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isKeyboardActivationStroke(keyStroke))
{
toggleChecked(getSelectedIndex())
return Result.HANDLED
}
else if (keyStroke!!.getKeyType() === KeyType.MOUSE_EVENT)
{
val mouseAction = keyStroke as MouseAction?
val actionType = mouseAction!!.getActionType()

if ((isMouseMove(keyStroke) 
|| actionType === MouseActionType.CLICK_RELEASE 
|| actionType === MouseActionType.SCROLL_UP 
|| actionType === MouseActionType.SCROLL_DOWN))
{
return super.handleKeyStroke(keyStroke)
}

val result = super.handleKeyStroke(keyStroke)
val newIndex = getIndexByMouseAction(mouseAction)
if (actionType === MouseActionType.CLICK_DOWN)
{
stateForMouseDragged = (!isChecked(newIndex))!!
setChecked(newIndex, stateForMouseDragged)
minIndexForMouseDragged = newIndex
maxIndexForMouseDragged = newIndex
}

minIndexForMouseDragged = Math.min(minIndexForMouseDragged, newIndex)
maxIndexForMouseDragged = Math.max(maxIndexForMouseDragged, newIndex)

if (actionType === MouseActionType.DRAG)
{
for (i in minIndexForMouseDragged..maxIndexForMouseDragged)
{
setChecked(i, stateForMouseDragged)
}
}
return result
}

return super.handleKeyStroke(keyStroke)
}

/**
 * Default renderer for this component which is used unless overridden. The checked state is drawn on the left side
 * of the item label using a "[ ]" block filled with an X if the item has checked state on
 * @param <V> Type of items in the [CheckBoxList]
</V> */
     class CheckBoxListItemRenderer<V>:ListItemRenderer<V?, CheckBoxList<V?>?>() {
@Override
 fun getHotSpotPositionOnLine(selectedIndex:Int):Int {
return 1
}

@Override
 fun getLabel(listBox:CheckBoxList<V?>, index:Int, item:V?):String? {
var check:String? = " "
val itemStatus = listBox.itemStatus
if (itemStatus!!.get(index))
check = "x"

val text = item!!.toString()
return "[" + check + "] " + text
}

@Override
 fun drawItem(graphics:TextGUIGraphics?, listBox:CheckBoxList<V?>, index:Int, item:V?, selected:Boolean, focused:Boolean) {
val themeDefinition = listBox.getTheme().getDefinition(CheckBoxList<*>::class.java)
val itemStyle:ThemeStyle?
if (selected && !focused)
{
itemStyle = themeDefinition!!.getSelected()
}
else if (selected)
{
itemStyle = themeDefinition!!.getActive()
}
else if (focused)
{
itemStyle = themeDefinition!!.getInsensitive()
}
else
{
itemStyle = themeDefinition!!.getNormal()
}

if (themeDefinition!!.getBooleanProperty("CLEAR_WITH_NORMAL", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
graphics!!.fill(' ')
graphics!!.applyThemeStyle(itemStyle)
}
else
{
graphics!!.applyThemeStyle(itemStyle)
graphics!!.fill(' ')
}

val brackets = (themeDefinition!!.getCharacter("LEFT_BRACKET", '[') + 
" " + 
themeDefinition!!.getCharacter("RIGHT_BRACKET", ']'))
if (themeDefinition!!.getBooleanProperty("FIXED_BRACKET_COLOR", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
graphics!!.putString(0, 0, brackets)
graphics!!.applyThemeStyle(itemStyle)
}
else
{
graphics!!.putString(0, 0, brackets)
}

val text = (if (item != null) item else "<null>").toString()
graphics!!.putString(4, 0, text)

val itemChecked = listBox.isChecked(index)!!
val marker = themeDefinition!!.getCharacter("MARKER", 'x')
if (themeDefinition!!.getBooleanProperty("MARKER_WITH_NORMAL", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
if (selected && focused && themeDefinition!!.getBooleanProperty("HOTSPOT_PRELIGHT", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
graphics!!.setCharacter(1, 0, (if (itemChecked!!) marker else ' '))
}
}
}/**
 * Creates a new `CheckBoxList` that is initially empty and has no hardcoded preferred size, so it will
 * attempt to be as big as necessary to draw all items.
 */
