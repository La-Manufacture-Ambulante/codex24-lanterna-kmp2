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

import java.util.concurrent.CopyOnWriteArrayList

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * The list box will display a number of items, of which one and only one can be marked as selected.
 * The user can select an item in the list box by pressing the return key or space bar key. If you
 * select one item when another item is already selected, the previously selected item will be
 * deselected and the highlighted item will be the selected one instead.
 * @author Martin
 */
 class RadioBoxList<V>/**
 * Creates a new RadioCheckBoxList with a specified size. If the items in the `RadioBoxList` cannot fit in the
 * size specified, scrollbars will be used
 * @param preferredSize Size of the `RadioBoxList` or `null` to have it try to be as big as necessary to
 * be able to draw all items
 */
     @JvmOverloads  constructor(preferredSize:TerminalSize? = null):AbstractListBox<V?, RadioBoxList<V?>?>(preferredSize) {

private val listeners:List<Listener?>?
private var checkedIndex:Int = 0

/**
 * @return The index of the item which is currently selected, or -1 if there is no selection
 */
    /**
 * Sets the currently selected item by index. If the index is out of range, it does nothing.
 * @param index Index of the item to be selected
 */
     var checkedItemIndex:Int
get() {
return checkedIndex
}
@Synchronized set(index) {
if (index < -1 || index >= getItemCount())
return 

setCheckedIndex(index)
}

/**
 * @return The object currently selected, or null if there is no selection
 */
    /**
 * Sets the currently checked item by the value itself. If null, the selection is cleared. When changing selection,
 * any previously selected item is deselected.
 * @param item Item to be checked
 */
     var checkedItem:V?
@Synchronized get() {
if (checkedIndex == -1 || checkedIndex >= getItemCount())
return null

return getItemAt(checkedIndex)
}
@Synchronized set(item) {
if (item == null)
{
setCheckedIndex(-1)
}
else
{
checkedItemIndex = indexOf(item)
}
}
/**
 * Listener interface that can be attached to the `RadioBoxList` in order to be notified on user actions
 */
     interface Listener {
/**
 * Called by the `RadioBoxList` when the user changes which item is selected
 * @param selectedIndex Index of the newly selected item, or -1 if the selection has been cleared (can only be
 * done programmatically)
 * @param previousSelection The index of the previously selected item which is now no longer selected, or -1 if
 * nothing was previously selected
 */
         fun onSelectionChanged(selectedIndex:Int, previousSelection:Int) 
}

init{
this.listeners = CopyOnWriteArrayList()
this.checkedIndex = -1
}

@Override
protected fun createDefaultListItemRenderer():ListItemRenderer<V?, RadioBoxList<V?>?>? {
return RadioBoxListItemRenderer<Object?>()
}

@Override
@Synchronized  fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isKeyboardActivationStroke(keyStroke))
{
setCheckedIndex(getSelectedIndex())
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

 // includes mouse drag
            val existingIndex = getSelectedIndex()
val newIndex = getIndexByMouseAction(mouseAction)
if (existingIndex != newIndex || !isFocused())
{
val result = super.handleKeyStroke(keyStroke)
setCheckedIndex(getSelectedIndex())
return result
}
setCheckedIndex(getSelectedIndex())
return Result.HANDLED
}
return super.handleKeyStroke(keyStroke)
}

@Override
@Synchronized  fun removeItem(index:Int):V? {
val item = super.removeItem(index)
if (index < checkedIndex)
{
checkedIndex--
}
while (checkedIndex >= getItemCount())
{
checkedIndex--
}
return item
}

@Override
@Synchronized  fun clearItems():RadioBoxList<V?>? {
setCheckedIndex(-1)
return super.clearItems()
}

/**
 * This method will see if an object is the currently selected item in this RadioCheckBoxList
 * @param object Object to test if it's the selected one
 * @return `true` if the supplied object is what's currently selected in the list box,
 * `false` otherwise. Returns null if the supplied object is not an item in the list box.
 */
    @Synchronized  fun isChecked(`object`:V?):Boolean? {
if (`object` == null)
return null

if (indexOf(`object`) === -1)
return null

return checkedIndex == indexOf(`object`)
}

/**
 * This method will see if an item, addressed by index, is the currently selected item in this
 * RadioCheckBoxList
 * @param index Index of the item to check if it's currently selected
 * @return `true` if the currently selected object is at the supplied index,
 * `false` otherwise. Returns false if the index is out of range.
 */
    @SuppressWarnings("SimplifiableIfStatement")
@Synchronized  fun isChecked(index:Int):Boolean {
if (index < 0 || index >= getItemCount())
{
return false
}

return checkedIndex == index
}

/**
 * Un-checks the currently checked item (if any) and leaves the radio check box in a state where no item is checked.
 */
    @Synchronized @JvmStatic  fun clearSelection() {
setCheckedIndex(-1)
}

/**
 * Adds a new listener to the `RadioBoxList` that will be called on certain user actions
 * @param listener Listener to attach to this `RadioBoxList`
 * @return Itself
 */
     fun addListener(listener:Listener?):RadioBoxList<V?> {
if (listener != null && !listeners!!.contains(listener))
{
listeners!!.add(listener)
}
return this
}

/**
 * Removes a listener from this `RadioBoxList` so that if it had been added earlier, it will no longer be
 * called on user actions
 * @param listener Listener to remove from this `RadioBoxList`
 * @return Itself
 */
     fun removeListener(listener:Listener?):RadioBoxList<V?> {
listeners!!.remove(listener)
return this
}

private fun setCheckedIndex(index:Int) {
val previouslyChecked = checkedIndex
this.checkedIndex = index
invalidate()
runOnGUIThreadIfExistsOtherwiseRunDirect({ for (listener in listeners!!)
{
listener!!.onSelectionChanged(checkedIndex, previouslyChecked)
} })
}

/**
 * Default renderer for this component which is used unless overridden. The selected state is drawn on the left side
 * of the item label using a "&lt; &gt;" block filled with an "o" if the item is the selected one
 * @param <V> Type of items in the [RadioBoxList]
</V> */
     class RadioBoxListItemRenderer<V>:ListItemRenderer<V?, RadioBoxList<V?>?>() {
@Override
 fun getHotSpotPositionOnLine(selectedIndex:Int):Int {
return 1
}

protected fun getItemText(listBox:RadioBoxList<V?>?, index:Int, item:V?):String? {
return (if (item != null) item else "<null>").toString()
}

@Override
 fun getLabel(listBox:RadioBoxList<V?>, index:Int, item:V?):String? {
var check:String? = " "
if (listBox.checkedIndex == index)
check = "o"

val text = getItemText(listBox, index, item)
return "<" + check + "> " + text
}

@Override
 fun drawItem(graphics:TextGUIGraphics?, listBox:RadioBoxList<V?>, index:Int, item:V?, selected:Boolean, focused:Boolean) {
val themeDefinition = listBox.getTheme().getDefinition(RadioBoxList<*>::class.java)
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

val brackets = (themeDefinition!!.getCharacter("LEFT_BRACKET", '<') + 
" " + 
themeDefinition!!.getCharacter("RIGHT_BRACKET", '>'))
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

val text = getItemText(listBox, index, item)
graphics!!.putString(4, 0, text)

val itemChecked = listBox.checkedIndex == index
val marker = themeDefinition!!.getCharacter("MARKER", 'o')
if (themeDefinition!!.getBooleanProperty("MARKER_WITH_NORMAL", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
if (selected && focused && themeDefinition!!.getBooleanProperty("HOTSPOT_PRELIGHT", false))
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
graphics!!.setCharacter(1, 0, (if (itemChecked) marker else ' '))
}
}

}/**
 * Creates a new RadioCheckBoxList with no items. The size of the `RadioBoxList` will be as big as is required
 * to display all items.
 */
