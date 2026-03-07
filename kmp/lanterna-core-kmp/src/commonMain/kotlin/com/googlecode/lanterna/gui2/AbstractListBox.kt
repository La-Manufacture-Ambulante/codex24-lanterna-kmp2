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

import java.util.ArrayList

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * Base class for several list box implementations, this will handle things like list of items and the scrollbar.
 * @param <T> Should always be itself, see `AbstractComponent`
 * @param <V> Type of items this list box contains
 * @author Martin
</V></T> */
abstract class AbstractListBox<V, T : AbstractListBox<V?, T?>?>/**
 * This constructor sets up the component with a preferred size that is will always request, no matter what items
 * are in the list box. If there are more items than the size can contain, scrolling and a vertical scrollbar will
 * be used. Calling this constructor with a `null` value has the same effect as calling the default
 * constructor.
 * 
 * @param size Preferred size that the list should be asking for instead of invoking the preferred size calculation,
 * or if set to `null` will ask to be big enough to display all items.
 */
     @JvmOverloads  protected constructor(size:TerminalSize? = null):AbstractInteractableComponent<T?>() {
private val items:List<V?>?
private var selectedIndex:Int = 0
private var listItemRenderer:ListItemRenderer<V?, T?>? = null
protected var scrollOffset:TerminalPosition? = TerminalPosition(0, 0)

 // These dialog boxes are quite weird when they are empty and receive input focus, so try to avoid that
 val isFocusable:Boolean
@Override
get() {
if (isEmpty)
{
return false
}
return super.isFocusable()
}

/**
 * Checks if the list box has no items
 * @return `true` if the list box has no items, `false` otherwise
 */
     val isEmpty:Boolean
@Synchronized get() {
return items!!.isEmpty()
}

/**
 * Returns the number of items currently in the list box
 * @return Number of items in the list box
 */
     val itemCount:Int
@Synchronized get() {
return items!!.size()
}

/**
 * Returns the currently selected item in the list box. Please note that in this context, selected
 * simply means it is the item that currently has input focus. This is not to be confused with list box
 * implementations such as `CheckBoxList` where individual items have a certain checked/unchecked state.
 * @return The currently selected item in the list box, or `null` if there are no items
 */
     val selectedItem:V?
@Synchronized get() {
if (selectedIndex == -1)
{
return null
}
else
{
return items!!.get(selectedIndex)
}
}

init{
this.items = ArrayList()
this.selectedIndex = -1
setPreferredSize(size)
setListItemRenderer(createDefaultListItemRenderer())
}

@Override
protected fun createDefaultRenderer():InteractableRenderer<T?>? {
return DefaultListBoxRenderer<Object?, com.googlecode.lanterna.gui2.AbstractListBox<V?, T?>?>()
}

/**
 * Method that constructs the `ListItemRenderer` that this list box should use to draw the elements of the
 * list box. This can be overridden to supply a custom renderer. Note that this is not the renderer used for the
 * entire list box but for each item, called one by one.
 * @return `ListItemRenderer` to use when drawing the items in the list
 */
    protected fun createDefaultListItemRenderer():ListItemRenderer<V?, T?> {
return ListItemRenderer<V?, T?>()
}

internal fun getListItemRenderer():ListItemRenderer<V?, T?>? {
return listItemRenderer
}

/**
 * This method overrides the `ListItemRenderer` that is used to draw each element in the list box. Note that
 * this is not the renderer used for the entire list box but for each item, called one by one.
 * @param listItemRenderer New renderer to use when drawing the items in the list box
 * @return Itself
 */
    @Synchronized  fun setListItemRenderer(listItemRenderer:ListItemRenderer<V?, T?>?):T? {
var listItemRenderer = listItemRenderer
if (listItemRenderer == null)
{
listItemRenderer = createDefaultListItemRenderer()
if (listItemRenderer == null)
{
throw IllegalStateException("createDefaultListItemRenderer returned null")
}
}
this.listItemRenderer = listItemRenderer
return self()
}

@Override
@Synchronized  fun handleKeyStroke(keyStroke:KeyStroke):Result? {
try
{
when (keyStroke.getKeyType()) {
TAB -> return Result.MOVE_FOCUS_NEXT

REVERSE_TAB -> return Result.MOVE_FOCUS_PREVIOUS

ARROW_RIGHT -> return Result.MOVE_FOCUS_RIGHT

ARROW_LEFT -> return Result.MOVE_FOCUS_LEFT

ARROW_DOWN -> {
if (items!!.isEmpty() || selectedIndex == items!!.size() - 1)
{
return Result.MOVE_FOCUS_DOWN
}
selectedIndex++
return Result.HANDLED
}

ARROW_UP -> {
if (items!!.isEmpty() || selectedIndex == 0)
{
return Result.MOVE_FOCUS_UP
}
selectedIndex--
return Result.HANDLED
}

HOME -> {
selectedIndex = 0
return Result.HANDLED
}

END -> {
selectedIndex = items!!.size() - 1
return Result.HANDLED
}

PAGE_UP -> {
if (getSize() != null)
{
setSelectedIndex(getSelectedIndex() - getSize().getRows())
}
return Result.HANDLED
}

PAGE_DOWN -> {
if (getSize() != null)
{
setSelectedIndex(getSelectedIndex() + getSize().getRows())
}
return Result.HANDLED
}

CHARACTER -> {
 // Only check is Alt and Ctrl keys are not pressed signaling a possible accelerator key
                    if (!keyStroke.isAltDown() && !keyStroke.isCtrlDown() && selectByCharacter(keyStroke.getCharacter()))
{
return Result.HANDLED
}
return Result.UNHANDLED
}
MOUSE_EVENT -> {
val mouseAction = keyStroke as MouseAction
val actionType = mouseAction!!.getActionType()
if (isMouseMove(keyStroke))
{
takeFocus()
selectedIndex = getIndexByMouseAction(mouseAction!!)
return Result.HANDLED
}

if (actionType === MouseActionType.CLICK_RELEASE)
{
 // do nothing, desired actioning has been performed already on CLICK_DOWN and DRAG
                        return Result.HANDLED
}
else if (actionType === MouseActionType.SCROLL_UP)
{
 // relying on setSelectedIndex(index) to clip the index to valid values within range
                        setSelectedIndex(getSelectedIndex() - 1)
return Result.HANDLED
}
else if (actionType === MouseActionType.SCROLL_DOWN)
{
 // relying on setSelectedIndex(index) to clip the index to valid values within range
                        setSelectedIndex(getSelectedIndex() + 1)
return Result.HANDLED
}

selectedIndex = getIndexByMouseAction(mouseAction!!)
return super.handleKeyStroke(keyStroke)
}
}
return Result.UNHANDLED
}

finally
{
invalidate()
}
}

/**
 * By converting [TerminalPosition]s to
 * [.toGlobal] gets index clicked on by mouse action.
 * 
 * @return index of a item that was clicked on with [MouseAction]
 */
    protected fun getIndexByMouseAction(click:MouseAction):Int {
val index = click.getPosition().getRow() - getGlobalPosition().getRow() - scrollOffset!!.row

return Math.min(index, items!!.size() - 1)
}

private fun selectByCharacter(character:Character?):Boolean {
var character = character
character = Character.toLowerCase(character)

val selectedIndex = getSelectedIndex()
for (i in 0 until itemCount)
{
val index = (selectedIndex + i + 1) % itemCount
val item = getItemAt(index)
val label = if (item != null) item!!.toString() else null
if (label != null && label!!.length() > 0)
{
val firstChar = Character.toLowerCase(label!!.charAt(0))
if (firstChar == character)
{
setSelectedIndex(index)
return true
}
}
}

return false
}

@Override
@Synchronized protected fun afterEnterFocus(direction:FocusChangeDirection?, previouslyInFocus:Interactable?) {
if (items!!.isEmpty())
{
return 
}

if (direction === FocusChangeDirection.DOWN)
{
selectedIndex = 0
}
else if (direction === FocusChangeDirection.UP)
{
selectedIndex = items!!.size() - 1
}
}

/**
 * Adds one more item to the list box, at the end.
 * @param item Item to add to the list box
 * @return Itself
 */
    @Synchronized  fun addItem(item:V?):T? {
if (item == null)
{
return self()
}

items!!.add(item)
if (selectedIndex == -1)
{
selectedIndex = 0
}
invalidate()
return self()
}

/**
 * Removes an item from the list box by its index. The current selection in the list box will be adjusted
 * accordingly.
 * @param index Index of the item to remove
 * @return The item that was removed
 * @throws IndexOutOfBoundsException if the index is out of bounds in regards to the list of items
 */
    @Synchronized  fun removeItem(index:Int):V? {
val existing = items!!.remove(index)
if (index < selectedIndex)
{
selectedIndex--
}
while (selectedIndex >= items!!.size())
{
selectedIndex--
}
invalidate()
return existing
}

/**
 * Removes all items from the list box
 * @return Itself
 */
    @Synchronized  fun clearItems():T? {
items!!.clear()
selectedIndex = -1
invalidate()
return self()
}

/**
 * Looks for the particular item in the list and returns the index within the list (starting from zero) of that item
 * if it is found, or -1 otherwise
 * @param item What item to search for in the list box
 * @return Index of the item in the list box or -1 if the list box does not contain the item
 */
    @Synchronized  fun indexOf(item:V?):Int {
return items!!.indexOf(item)
}

/**
 * Retrieves the item at the specified index in the list box
 * @param index Index of the item to fetch
 * @return The item at the specified index
 * @throws IndexOutOfBoundsException If the index is less than zero or equals/greater than the number of items in
 * the list box
 */
    @Synchronized  fun getItemAt(index:Int):V? {
return items!!.get(index)
}

/**
 * Returns a copy of the items in the list box as a `List`
 * @return Copy of all the items in this list box
 */
    @Synchronized  fun getItems():List<V?>? {
return ArrayList(items)
}

/**
 * Sets which item in the list box that is currently selected. Please note that in this context, selected simply
 * means it is the item that currently has input focus. This is not to be confused with list box implementations
 * such as `CheckBoxList` where individual items have a certain checked/unchecked state.
 * This method will clip the supplied index to within 0 to items.size() -1.
 * @param index Index of the item that should be currently selected
 * @return Itself
 */
    @Synchronized  fun setSelectedIndex(index:Int):T? {
selectedIndex = Math.max(0, Math.min(index, items!!.size() - 1))

invalidate()
return self()
}

/**
 * Returns the index of the currently selected item in the list box. Please note that in this context, selected
 * simply means it is the item that currently has input focus. This is not to be confused with list box
 * implementations such as `CheckBoxList` where individual items have a certain checked/unchecked state.
 * @return The index of the currently selected row in the list box, or -1 if there are no items
 */
     fun getSelectedIndex():Int {
return selectedIndex
}

/**
 * The default renderer for `AbstractListBox` and all its subclasses.
 * @param <V> Type of the items the list box this renderer is for
 * @param <T> Type of list box
</T></V> */
     class DefaultListBoxRenderer<V, T : AbstractListBox<V?, T?>?>:InteractableRenderer<T?> {
private val verticalScrollBar:ScrollBar?
private var scrollTopIndex:Int = 0
/**
 * Default constructor
 */
        init{
this.verticalScrollBar = ScrollBar(Direction.VERTICAL)
this.scrollTopIndex = 0
}

@Override
 fun getCursorLocation(listBox:T):TerminalPosition? {
if (!listBox.getThemeDefinition().isCursorVisible())
{
return null
}
val selectedIndex = listBox.getSelectedIndex()
val columnAccordingToRenderer = listBox.getListItemRenderer()!!.getHotSpotPositionOnLine(selectedIndex)
if (columnAccordingToRenderer == -1)
{
return null
}
return TerminalPosition(columnAccordingToRenderer, selectedIndex - scrollTopIndex)
}

@Override
 fun getPreferredSize(listBox:T):TerminalSize {
var maxWidth = 5   //Set it to something...
var index = 0
for (item in listBox.getItems()!!)
{
val itemString = listBox.getListItemRenderer()!!.getLabel(listBox, index++, item)
val stringLengthInColumns = TerminalTextUtils.getColumnWidth(itemString)
if (stringLengthInColumns > maxWidth)
{
maxWidth = stringLengthInColumns
}
}
return TerminalSize(maxWidth + 1, listBox.itemCount)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, listBox:T) {
 //update the page size, used for page up and page down keys
            val themeDefinition = listBox.getTheme().getDefinition(AbstractListBox<*, *>::class.java)
val componentHeight = graphics.getSize().getRows()
 //int componentWidth = graphics.getSize().getColumns();
            val selectedIndex = listBox.getSelectedIndex()
val items = listBox.getItems()
val listItemRenderer = listBox.getListItemRenderer()

if (selectedIndex != -1)
{
if (selectedIndex < scrollTopIndex)
scrollTopIndex = selectedIndex
else if (selectedIndex >= componentHeight + scrollTopIndex)
scrollTopIndex = selectedIndex - componentHeight + 1
}

 //Do we need to recalculate the scroll position?
            //This code would be triggered by resizing the window when the scroll
            //position is at the bottom
            if ((items!!.size() > componentHeight && items!!.size() - scrollTopIndex < componentHeight))
{
scrollTopIndex = items!!.size() - componentHeight
}

listBox.scrollOffset = TerminalPosition(0, -scrollTopIndex)

graphics.applyThemeStyle(themeDefinition!!.getNormal())
graphics.fill(' ')

val itemSize = graphics.getSize().withRows(1)
for (i in scrollTopIndex until items!!.size())
{
if (i - scrollTopIndex >= componentHeight)
{
break
}
listItemRenderer!!.drawItem(
graphics.newTextGraphics(TerminalPosition(0, i - scrollTopIndex), itemSize), 
listBox, 
i, 
items!!.get(i), 
selectedIndex == i, 
listBox.isFocused())
}

graphics.applyThemeStyle(themeDefinition!!.getNormal())
if (items!!.size() > componentHeight)
{
verticalScrollBar!!.onAdded(listBox.getParent())
verticalScrollBar!!.setViewSize(componentHeight)
verticalScrollBar!!.setScrollMaximum(items!!.size())
verticalScrollBar!!.setScrollPosition(scrollTopIndex)
verticalScrollBar!!.draw(graphics.newTextGraphics(
TerminalPosition(graphics.getSize().getColumns() - 1, 0), 
TerminalSize(1, graphics.getSize().getRows())))
}
}
}

/**
 * The default list item renderer class, this can be extended and customized it needed. The instance which is
 * assigned to the list box will be called once per item in the list when the list box is drawn.
 * @param <V> Type of the items in the list box
 * @param <T> Type of the list box class itself
</T></V> */
     class ListItemRenderer<V, T : AbstractListBox<V?, T?>?> {
/**
 * Returns where on the line to place the text terminal cursor for a currently selected item. By default this
 * will return 0, meaning the first character of the selected line. If you extend `ListItemRenderer` you
 * can change this by returning a different number. Returning -1 will cause lanterna to hide the cursor.
 * @param selectedIndex Which item is currently selected
 * @return Index of the character in the string we want to place the terminal cursor on, or -1 to hide it
 */
         fun getHotSpotPositionOnLine(selectedIndex:Int):Int {
return 0
}

/**
 * Given a list box, an index of an item within that list box and what the item is, this method should return
 * what to draw for that item. The default implementation is to return whatever `toString()` returns when
 * called on the item.
 * @param listBox List box the item belongs to
 * @param index Index of the item
 * @param item The item itself
 * @return String to draw for this item
 */
         fun getLabel(listBox:T?, index:Int, item:V?):String? {
return if (item != null) item!!.toString() else "<null>"
}

/**
 * This is the main drawing method for a single list box item, it applies the current theme to setup the colors
 * and then calls `getLabel(..)` and draws the result using the supplied `TextGUIGraphics`. The
 * graphics object is created just for this item and is restricted so that it can only draw on the area this
 * item is occupying. The top-left corner (0x0) should be the starting point when drawing the item.
 * @param graphics Graphics object to draw with
 * @param listBox List box we are drawing an item from
 * @param index Index of the item we are drawing
 * @param item The item we are drawing
 * @param selected Will be set to `true` if the item is currently selected, otherwise `false`, but
 * please notice what context 'selected' refers to here (see `setSelectedIndex`)
 * @param focused Will be set to `true` if the list box currently has input focus, otherwise `false`
 */
         fun drawItem(graphics:TextGUIGraphics?, listBox:T, index:Int, item:V?, selected:Boolean, focused:Boolean) {
val themeDefinition = listBox.getTheme().getDefinition(AbstractListBox<*, *>::class.java)
if (selected && focused)
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
var label = getLabel(listBox, index, item)
label = TerminalTextUtils.fitString(label, graphics!!.getSize().getColumns())
while (TerminalTextUtils.getColumnWidth(label) < graphics!!.getSize().getColumns())
{
label += " "
}
graphics!!.putString(0, 0, label)
}
}
}/**
 * This constructor sets up the component so it has no preferred size but will ask to be as big as the list is. If
 * the GUI cannot accommodate this size, scrolling and a vertical scrollbar will be used.
 */
