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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * Default implementation of Interactable that extends from AbstractComponent. If you want to write your own component
 * that is interactable, i.e. can receive keyboard (and mouse) input, you probably want to extend from this class as
 * it contains some common implementations of the methods from `Interactable` interface
 * @param <T> Should always be itself, see `AbstractComponent`
 * @author Martin
</T> */
abstract class AbstractInteractableComponent<T : AbstractInteractableComponent<T?>?>/**
 * Default constructor
 */
     protected constructor():AbstractComponent<T?>(), Interactable {

private var inputFilter:InputFilter? = null
@get:Override
 var isFocused:Boolean = false
private set
private var enabled:Boolean = false
private var accelerator:KeyStroke? = null

 val renderer:InteractableRenderer<T?>?
@Override
get() {
return super.getRenderer() as InteractableRenderer<T?>
}

 val isFocusable:Boolean
@Override
get() {
return true
}

 val cursorLocation:TerminalPosition?
@Override
get() {
return renderer!!.getCursorLocation(self())
}

init{
inputFilter = null
isFocused = false
enabled = true
}

@Override
 fun takeFocus():T? {
if (!isEnabled())
{
return self()
}
val basePane = getBasePane()
if (basePane != null)
{
basePane!!.setFocusedInteractable(this)
}
return self()
}

/**
 * {@inheritDoc}
 * 
 * 
 * This method is final in `AbstractInteractableComponent`, please override `afterEnterFocus` instead
 */
    @Override
 fun onEnterFocus(direction:FocusChangeDirection?, previouslyInFocus:Interactable?) {
isFocused = true
afterEnterFocus(direction, previouslyInFocus)
}

/**
 * Called by `AbstractInteractableComponent` automatically after this component has received input focus. You
 * can override this method if you need to trigger some action based on this.
 * @param direction How focus was transferred, keep in mind this is from the previous component's point of view so
 * if this parameter has value DOWN, focus came in from above
 * @param previouslyInFocus Which interactable component had focus previously
 */
    @SuppressWarnings("EmptyMethod")
protected fun afterEnterFocus(direction:FocusChangeDirection?, previouslyInFocus:Interactable?) {
 //By default no action
    }

@Synchronized protected fun setAccelerator(keyStroke:KeyStroke?):T? {
this.accelerator = keyStroke
return self()
}

@Synchronized protected fun getAccelerator():KeyStroke? {
return this.accelerator
}

/**
 * {@inheritDoc}
 * 
 * 
 * This method is final in `AbstractInteractableComponent`, please override `afterLeaveFocus` instead
 */
    @Override
 fun onLeaveFocus(direction:FocusChangeDirection?, nextInFocus:Interactable?) {
isFocused = false
afterLeaveFocus(direction, nextInFocus)
}

/**
 * Called by `AbstractInteractableComponent` automatically after this component has lost input focus. You
 * can override this method if you need to trigger some action based on this.
 * @param direction How focus was transferred, keep in mind this is from the this component's point of view so
 * if this parameter has value DOWN, focus is moving down to a component below
 * @param nextInFocus Which interactable component is going to receive focus
 */
    @SuppressWarnings("EmptyMethod")
protected fun afterLeaveFocus(direction:FocusChangeDirection?, nextInFocus:Interactable?) {
 //By default no action
    }

@Override
protected abstract fun createDefaultRenderer():InteractableRenderer<T?>? 

@Override
@Synchronized  fun setEnabled(enabled:Boolean):T? {
this.enabled = enabled
if (!enabled && isFocused)
{
val basePane = getBasePane()
if (basePane != null)
{
basePane!!.setFocusedInteractable(null)
}
}
return self()
}

@Override
 fun isEnabled():Boolean {
return enabled
}

@Override
@Synchronized  fun handleInput(keyStroke:KeyStroke?):Result? {
if (inputFilter == null || inputFilter!!.onInput(this, keyStroke))
{
return handleKeyStroke(keyStroke!!)
}
else
{
return Result.UNHANDLED
}
}

/**
 * This method can be overridden to handle various user input (mostly from the keyboard) when this component is in
 * focus. The input method from the interface, `handleInput(..)` is final in
 * `AbstractInteractableComponent` to ensure the input filter is properly handled. If the filter decides that
 * this event should be processed, it will call this method.
 * @param keyStroke What input was entered by the user
 * @return Result of processing the key-stroke
 */
    protected fun handleKeyStroke(keyStroke:KeyStroke):Result? {
 // Skip the keystroke if ctrl, alt or shift was down
        if (!keyStroke.isAltDown() && !keyStroke.isCtrlDown() && !keyStroke.isShiftDown())
{
when (keyStroke.getKeyType()) {
ARROW_DOWN -> return Result.MOVE_FOCUS_DOWN
ARROW_LEFT -> return Result.MOVE_FOCUS_LEFT
ARROW_RIGHT -> return Result.MOVE_FOCUS_RIGHT
ARROW_UP -> return Result.MOVE_FOCUS_UP
TAB -> return Result.MOVE_FOCUS_NEXT
REVERSE_TAB -> return Result.MOVE_FOCUS_PREVIOUS
MOUSE_EVENT -> {
if (isMouseMove(keyStroke))
{
 // do nothing
                        return Result.UNHANDLED
}
getBasePane().setFocusedInteractable(this)
return Result.HANDLED
}
}
}
return Result.UNHANDLED
}

@Override
 fun getInputFilter():InputFilter? {
return inputFilter
}

@Override
@Synchronized  fun setInputFilter(inputFilter:InputFilter?):T? {
this.inputFilter = inputFilter
return self()
}

 fun isKeyboardActivationStroke(keyStroke:KeyStroke):Boolean {
val isKeyboardActivation = (keyStroke.getKeyType() === KeyType.CHARACTER && keyStroke.getCharacter() === ' ') || keyStroke.getKeyType() === KeyType.ENTER

return isFocused && isKeyboardActivation
}

 fun isKeyboardAcceleratorStroke(keyStroke:KeyStroke?):Boolean {
if (accelerator == null) return false

return isEnabled() && accelerator!!.equals(keyStroke)
}

 fun isMouseActivationStroke(keyStroke:KeyStroke?):Boolean {
var isMouseActivation = false
if (keyStroke is MouseAction)
{
val action = keyStroke as MouseAction?
isMouseActivation = action!!.getActionType() === MouseActionType.CLICK_DOWN
}

return isFocused && isMouseActivation
}

 fun isActivationStroke(keyStroke:KeyStroke?):Boolean {
val isKeyboardActivationStroke = isKeyboardActivationStroke(keyStroke!!)
val isMouseActivationStroke = isMouseActivationStroke(keyStroke)

return isKeyboardActivationStroke || isMouseActivationStroke
}

 fun isMouseDown(keyStroke:KeyStroke):Boolean {
return keyStroke.getKeyType() === KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseDown()
}

 fun isMouseDrag(keyStroke:KeyStroke):Boolean {
return keyStroke.getKeyType() === KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseDrag()
}

 fun isMouseMove(keyStroke:KeyStroke):Boolean {
return keyStroke.getKeyType() === KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseMove()
}

 fun isMouseUp(keyStroke:KeyStroke):Boolean {
return keyStroke.getKeyType() === KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseUp()
}


}
