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
import java.util.concurrent.atomic.AtomicBoolean

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.gui2.Interactable.Result
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction

/**
 * This abstract implementation of `BasePane` has the common code shared by all different concrete
 * implementations.
 */
abstract class AbstractBasePane<T : BasePane?> protected constructor():BasePane {
protected val contentHolder:ContentHolder?
private val listeners:CopyOnWriteArrayList<BasePaneListener<T?>?>?
protected var interactableLookupMap:InteractableLookupMap? = null
private var focusedInteractable:Interactable? = null
private var invalid:Boolean = false
private var strictFocusChange:Boolean = false
private var enableDirectionBasedMovements:Boolean = false
private var theme:Theme? = null

private var mouseDownForDrag:Interactable? = null

 val isInvalid:Boolean
@Override
get() {
return invalid || contentHolder!!.isInvalid
}

 var component:Component?
@Override
get() {
return contentHolder!!.getComponent()
}
@Override
set(component) {
contentHolder!!.setComponent(component)
}

 //Don't allow the component to set the cursor outside of its own boundaries
 val cursorPosition:TerminalPosition?
@Override
get() {
if (focusedInteractable == null)
{
return null
}
val position = focusedInteractable!!.getCursorLocation()
if (position == null)
{
return null
}
if ((position!!.column < 0 || 
position!!.row < 0 || 
position!!.column >= focusedInteractable!!.getSize().getColumns() || 
position!!.row >= focusedInteractable!!.getSize().getRows()))
{
return null
}
return focusedInteractable!!.toBasePane(position)
}

 var menuBar:MenuBar?
@Override
get() {
return contentHolder!!.getMenuBar()
}
@Override
set(menuBar) {
contentHolder!!.setMenuBar(menuBar)
}

protected val basePaneListeners:List<BasePaneListener<T?>?>?
get() {
return listeners
}

init{
this.contentHolder = ContentHolder()
this.listeners = CopyOnWriteArrayList()
this.interactableLookupMap = InteractableLookupMap(TerminalSize(80, 25))
this.invalid = false
this.strictFocusChange = false
this.enableDirectionBasedMovements = true
this.theme = null
}

@Override
@JvmStatic  fun invalidate() {
invalid = true

 //Propagate
        contentHolder!!.invalidate()
}

@Override
 fun draw(graphics:TextGUIGraphics) {
graphics.applyThemeStyle(getTheme()!!.getDefinition(Window::class.java).getNormal())
graphics.fill(' ')

if (!interactableLookupMap!!.getSize().equals(graphics.getSize()))
{
interactableLookupMap = InteractableLookupMap(graphics.getSize())
}
else
{
interactableLookupMap!!.reset()
}

contentHolder!!.draw(graphics)
contentHolder!!.updateLookupMap(interactableLookupMap)
 //interactableLookupMap.debug();
        invalid = false
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
 // Fire events first and decide if the event should be sent to the focused component or not
        val deliverEvent = AtomicBoolean(true)
for (listener in listeners!!)
{
listener!!.onInput(self(), key, deliverEvent)
}
if (!deliverEvent.get())
{
return true
}

 // Now try to deliver the event to the focused component
        var handled = doHandleInput(key!!)
if (!handled)
{
 // Now try to deliver the event as an accelerator to unfocused components
        	handled = doHandleAccelerator(key!!)
}

 // If it wasn't handled, fire the listeners and decide what to report to the TextGUI
        if (!handled)
{
val hasBeenHandled = AtomicBoolean(false)
for (listener in listeners!!)
{
listener!!.onUnhandledInput(self(), key, hasBeenHandled)
}
handled = hasBeenHandled.get()
}
return handled
}

internal abstract fun self():T? 

private fun doHandleAccelerator(key:KeyStroke):Boolean {
if (key.getKeyType() === KeyType.MOUSE_EVENT) return false

val menuBar = menuBar

 // Check menu accelerators
    	if (menuBar != null && menuBar!!.handleInput(key)) return true

 // Check on base component    	    	
    	return handleAccelerator(contentHolder!!, key)
}

private fun handleAccelerator(container:Container, key:KeyStroke?):Boolean {
 // Check unfocused buttons
    	for (child in container.getChildren())
{
if (child is Button)
{
val btn = child as Button?
if (btn!!.handleInput(key) === Result.HANDLED) return true
}

if (child is Container && (child as Container).getChildCount() > 0)
{
if (handleAccelerator((child as Container?)!!, key)) return true
}
}

return false
}

private fun doHandleInput(key:KeyStroke):Boolean {
var result = false
if (key.getKeyType() === KeyType.MOUSE_EVENT)
{
return handleMouseInput(key as MouseAction)
}
var direction = Interactable.FocusChangeDirection.TELEPORT // Default
var nextFocus:Interactable? = null
if (focusedInteractable == null)
{
 // If nothing is focused and the user presses certain navigation keys, try to find if there is an
            // Interactable component we can move focus to.
            val menuBar = menuBar
val baseComponent = component
when (key.getKeyType()) {
TAB, ARROW_RIGHT, ARROW_DOWN -> {
direction = Interactable.FocusChangeDirection.NEXT
 // First try the menu, then the actual component
                    nextFocus = menuBar!!.nextFocus(null)
if (nextFocus == null)
{
if (baseComponent is Container)
{
nextFocus = (baseComponent as Container).nextFocus(null)
}
else if (baseComponent is Interactable)
{
nextFocus = baseComponent as Interactable?
}
}
}

REVERSE_TAB, ARROW_UP, ARROW_LEFT -> {
direction = Interactable.FocusChangeDirection.PREVIOUS
if (baseComponent is Container)
{
nextFocus = (baseComponent as Container).previousFocus(null)
}
else if (baseComponent is Interactable)
{
nextFocus = baseComponent as Interactable?
}
 // If no component can take focus, try the menu
                    if (nextFocus == null)
{
nextFocus = menuBar!!.previousFocus(null)
}
}
}
if (nextFocus != null)
{
setFocusedInteractable(nextFocus, direction)
result = true
}
}
else
{
var handleResult = focusedInteractable!!.handleInput(key)
if (!enableDirectionBasedMovements)
{
if (handleResult === Interactable.Result.MOVE_FOCUS_DOWN || handleResult === Interactable.Result.MOVE_FOCUS_RIGHT)
{
handleResult = Interactable.Result.MOVE_FOCUS_NEXT
}
else if (handleResult === Interactable.Result.MOVE_FOCUS_UP || handleResult === Interactable.Result.MOVE_FOCUS_LEFT)
{
handleResult = Interactable.Result.MOVE_FOCUS_PREVIOUS
}
}
when (handleResult) {
HANDLED -> result = true
UNHANDLED -> {
 //Filter the event recursively through all parent containers until we hit null; give the containers
                    //a chance to absorb the event
                    var parent = focusedInteractable!!.getParent()
while (parent != null)
{
if (parent!!.handleInput(key))
{
return true
}
parent = parent!!.getParent()
}
result = false
}
MOVE_FOCUS_NEXT -> {
nextFocus = contentHolder!!.nextFocus(focusedInteractable)
if (nextFocus == null)
{
nextFocus = contentHolder!!.nextFocus(null)
}
direction = Interactable.FocusChangeDirection.NEXT
}
MOVE_FOCUS_PREVIOUS -> {
nextFocus = contentHolder!!.previousFocus(focusedInteractable)
if (nextFocus == null)
{
nextFocus = contentHolder!!.previousFocus(null)
}
direction = Interactable.FocusChangeDirection.PREVIOUS
}
MOVE_FOCUS_DOWN -> {
nextFocus = interactableLookupMap!!.findNextDown(focusedInteractable)
direction = Interactable.FocusChangeDirection.DOWN
if (nextFocus == null && !strictFocusChange)
{
nextFocus = contentHolder!!.nextFocus(focusedInteractable)
direction = Interactable.FocusChangeDirection.NEXT
}
}
MOVE_FOCUS_LEFT -> {
nextFocus = interactableLookupMap!!.findNextLeft(focusedInteractable)
direction = Interactable.FocusChangeDirection.LEFT
}
MOVE_FOCUS_RIGHT -> {
nextFocus = interactableLookupMap!!.findNextRight(focusedInteractable)
direction = Interactable.FocusChangeDirection.RIGHT
}
MOVE_FOCUS_UP -> {
nextFocus = interactableLookupMap!!.findNextUp(focusedInteractable)
direction = Interactable.FocusChangeDirection.UP
if (nextFocus == null && !strictFocusChange)
{
nextFocus = contentHolder!!.previousFocus(focusedInteractable)
direction = Interactable.FocusChangeDirection.PREVIOUS
}
}
}
}
if (nextFocus != null)
{
setFocusedInteractable(nextFocus, direction)
result = true
}
return result
}

private fun handleMouseInput(mouseAction:MouseAction):Boolean {
val localCoordinates = fromGlobal(mouseAction.getPosition())
if (localCoordinates == null)
{
return false
}
val interactable = interactableLookupMap!!.getInteractableAt(localCoordinates)
if (mouseAction.isMouseDown())
{
mouseDownForDrag = interactable
}
val wasMouseDownForDrag = mouseDownForDrag
if (mouseAction.isMouseUp())
{
mouseDownForDrag = null
}
if (mouseAction.isMouseDrag() && mouseDownForDrag != null)
{
return mouseDownForDrag!!.handleInput(mouseAction) === Result.HANDLED
}
if (interactable == null)
{
return false
}
if (mouseAction.isMouseUp())
{
 // MouseUp only handled by same interactable as MouseDown
            if (wasMouseDownForDrag === interactable)
{
return interactable!!.handleInput(mouseAction) === Result.HANDLED
}
 // did not handleInput because mouse up was not on component mouse down was on
            return false
}
return interactable!!.handleInput(mouseAction) === Result.HANDLED
}

@Override
 fun getFocusedInteractable():Interactable? {
return focusedInteractable
}

@Override
 fun setFocusedInteractable(toFocus:Interactable?) {
setFocusedInteractable(toFocus, 
if (toFocus != null)
Interactable.FocusChangeDirection.TELEPORT
else
Interactable.FocusChangeDirection.RESET)
}

protected fun setFocusedInteractable(toFocus:Interactable?, direction:Interactable.FocusChangeDirection?) {
if (focusedInteractable === toFocus)
{
return 
}
if (toFocus != null && !toFocus!!.isEnabled())
{
return 
}
if (focusedInteractable != null)
{
focusedInteractable!!.onLeaveFocus(direction, toFocus)
}
val previous = focusedInteractable
focusedInteractable = toFocus
if (toFocus != null)
{
toFocus!!.onEnterFocus(direction, previous)
}
invalidate()
}

@Override
 fun setStrictFocusChange(strictFocusChange:Boolean) {
this.strictFocusChange = strictFocusChange
}

@Override
 fun setEnableDirectionBasedMovements(enableDirectionBasedMovements:Boolean) {
this.enableDirectionBasedMovements = enableDirectionBasedMovements
}

@Override
@Synchronized  fun getTheme():Theme? {
if (theme != null)
{
return theme
}
else if (getTextGUI() != null)
{
return getTextGUI().getTheme()
}
return null
}

@Override
@Synchronized  fun setTheme(theme:Theme?) {
this.theme = theme
invalidate()
}

protected fun addBasePaneListener(basePaneListener:BasePaneListener<T?>?) {
listeners!!.addIfAbsent(basePaneListener)
}

protected fun removeBasePaneListener(basePaneListener:BasePaneListener<T?>?) {
listeners!!.remove(basePaneListener)
}

protected inner class ContentHolder internal constructor():AbstractComposite<Container?>() {
private var menuBar:MenuBar? = null

 val isInvalid:Boolean
@Override
get() {
return super.isInvalid() || menuBar!!.isInvalid()
}

 val textGUI:TextGUI?
@Override
get() {
return this@AbstractBasePane.getTextGUI()
}

 val basePane:BasePane?
@Override
get() {
return this@AbstractBasePane
}

init{
this.menuBar = EmptyMenuBar()
}

private fun setMenuBar(menuBar:MenuBar?) {
var menuBar = menuBar
if (menuBar == null)
{
menuBar = EmptyMenuBar()
}

if (this.menuBar !== menuBar)
{
menuBar!!.onAdded(this)
this.menuBar!!.onRemoved(this)
this.menuBar = menuBar
if (focusedInteractable == null)
{
setFocusedInteractable(menuBar!!.nextFocus(null))
}
invalidate()
}
}

private fun getMenuBar():MenuBar? {
return menuBar
}

@Override
 fun invalidate() {
super.invalidate()
menuBar!!.invalidate()
}

@Override
 fun updateLookupMap(interactableLookupMap:InteractableLookupMap?) {
super.updateLookupMap(interactableLookupMap)
menuBar!!.updateLookupMap(interactableLookupMap)
}

@Override
 fun setComponent(component:Component?) {
if (component === component)
{
return 
}
setFocusedInteractable(null)
super.setComponent(component)
if (focusedInteractable == null && component is Interactable)
{
setFocusedInteractable(component as Interactable?)
}
else if (focusedInteractable == null && component is Container)
{
setFocusedInteractable((component as Container).nextFocus(null))
}
}

 fun removeComponent(component:Component?):Boolean {
val removed = super.removeComponent(component)
if (removed)
{
focusedInteractable = null
}
return removed
}

@Override
protected fun createDefaultRenderer():ComponentRenderer<Container?>? {
return object:ComponentRenderer<Container?>() {
@Override
 fun getPreferredSize(component:Container?):TerminalSize? {
val subComponent = component
if (subComponent == null)
{
return TerminalSize.ZERO
}
return subComponent!!.getPreferredSize()
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:Container?) {
var graphics = graphics
if (!(menuBar is EmptyMenuBar))
{
val menuBarHeight = menuBar!!.getPreferredSize().getRows()
val menuGraphics = graphics!!.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, graphics!!.getSize().withRows(menuBarHeight))
menuBar!!.draw(menuGraphics)
graphics = graphics!!.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER.withRelativeRow(menuBarHeight), graphics!!.getSize().withRelativeRows(-menuBarHeight))
}

val subComponent = component
if (subComponent == null)
{
return 
}
subComponent!!.draw(graphics)
}
}
}

@Override
 fun toGlobal(position:TerminalPosition?):TerminalPosition? {
return this@AbstractBasePane.toGlobal(position)
}

@Override
 fun toBasePane(position:TerminalPosition?):TerminalPosition? {
return position
}
}

private class EmptyMenuBar:MenuBar() {
 val isInvalid:Boolean
@Override
get() {
return false
}

 val isEmptyMenuBar:Boolean
@Override
get() {
return true
}

@Override
@Synchronized  fun onAdded(container:Container?) {}

@Override
@Synchronized  fun onRemoved(container:Container?) {}
}
}
