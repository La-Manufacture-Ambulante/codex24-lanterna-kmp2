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

import java.util.Collections

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke

/**
 * This abstract implementation contains common code for the different `Composite` implementations. A
 * `Composite` component is one that encapsulates a single component, like borders. Because of this, a
 * `Composite` can be seen as a special case of a `Container` and indeed this abstract class does in fact
 * implement the `Container` interface as well, to make the composites easier to work with internally.
 * @author martin
 * @param <T> Should always be itself, see `AbstractComponent`
</T> */
abstract class AbstractComposite<T : Container?>:AbstractComponent<T?>(), Composite, Container {

private var component:Component? = null

 val childCount:Int
@Override
get() {
return if (component != null) 1 else 0
}

 val childrenList:List<Component?>?
@Override
get() {
if (component != null)
{
return Collections.singletonList(component)
}
else
{
return Collections.emptyList()
}
}

 val children:Collection<Component?>?
@Override
get() {
return childrenList
}

 val isInvalid:Boolean
@Override
get() {
return component != null && component!!.isInvalid()
}
/**
 * Default constructor
 */
    init{
component = null
}

@Override
 fun setComponent(component:Component?) {
val oldComponent = this.component
if (oldComponent === component)
{
return 
}
if (oldComponent != null)
{
removeComponent(oldComponent)
}
if (component != null)
{
this.component = component
component!!.onAdded(this)
if (getBasePane() != null)
{
val menuBar = getBasePane().getMenuBar()
if (menuBar == null || menuBar!!.isEmptyMenuBar())
{
component!!.setPosition(TerminalPosition.TOP_LEFT_CORNER)
}
else
{
component!!.setPosition(TerminalPosition.TOP_LEFT_CORNER.withRelativeRow(1))
}
}
invalidate()
}
}

@Override
 fun getComponent():Component? {
return component
}

@Override
 fun containsComponent(component:Component?):Boolean {
return component != null && component!!.hasParent(this)
}

@Override
 fun removeComponent(component:Component?):Boolean {
if (this.component === component)
{
this.component = null
component!!.onRemoved(this)
invalidate()
return true
}
return false
}

@Override
@JvmStatic  fun invalidate() {
super.invalidate()

 //Propagate
        if (component != null)
{
component!!.invalidate()
}
}

@Override
 fun nextFocus(fromThis:Interactable?):Interactable? {
if (fromThis == null && getComponent() is Interactable)
{
val interactable = getComponent() as Interactable?
if (interactable!!.isEnabled())
{
return interactable
}
}
else if (getComponent() is Container)
{
return (getComponent() as Container).nextFocus(fromThis)
}
return null
}

@Override
 fun previousFocus(fromThis:Interactable?):Interactable? {
if (fromThis == null && getComponent() is Interactable)
{
val interactable = getComponent() as Interactable?
if (interactable!!.isEnabled())
{
return interactable
}
}
else if (getComponent() is Container)
{
return (getComponent() as Container).previousFocus(fromThis)
}
return null
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
return false
}

@Override
 fun updateLookupMap(interactableLookupMap:InteractableLookupMap?) {
if (getComponent() is Container)
{
(getComponent() as Container).updateLookupMap(interactableLookupMap)
}
else if (getComponent() is Interactable)
{
interactableLookupMap!!.add(getComponent() as Interactable?)
}
}
}
