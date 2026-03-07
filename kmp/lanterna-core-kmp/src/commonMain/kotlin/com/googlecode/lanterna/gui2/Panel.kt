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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import java.util.ArrayList
import java.util.Collections

/**
 * This class is the basic building block for creating user interfaces, being the standard implementation of
 * `Container` that supports multiple children. A `Panel` is a component that can contain one or more
 * other components, including nested panels. The panel itself doesn't have any particular appearance and isn't
 * interactable by itself, although you can set a border for the panel and interactable components inside the panel will
 * receive input focus as expected.
 * 
 * @author Martin
 */
 class Panel @JvmOverloads  constructor(layoutManager:LayoutManager? = LinearLayout()):AbstractComponent<Panel?>(), Container {
private val components:List<Component?>?
private var layoutManager:LayoutManager? = null
private var cachedPreferredSize:TerminalSize? = null
/**
 * Returns the color used to override the default background color from the theme, if set. Otherwise `null` is
 * returned and whatever theme is assigned will be used to derive the fill color.
 * @return The color, if any, used to fill the panel's unused space instead of the theme's color
 */
    /**
 * Sets an override color to be used instead of the theme's color for Panels when drawing unused space. If called
 * with `null`, it will reset back to the theme's color.
 * @param fillColor Color to draw the unused space with instead of what the theme definition says, no `null`
 * to go back to the theme definition
 */
     var fillColorOverride:TextColor? = null

 val childCount:Int
@Override
get() {
synchronized (components) {
return components!!.size()
}
}

 val children:Collection<Component?>?
@Override
get() {
return childrenList
}

 val childrenList:List<Component?>?
@Override
get() {
synchronized (components) {
return ArrayList(components)
}
}

 val isInvalid:Boolean
@Override
get() {
synchronized (components) {
for (component in components!!)
{
if (component!!.isVisible() && component!!.isInvalid())
{
return true
}
}
}
return super.isInvalid() || layoutManager!!.hasChanged()
}

init{
var layoutManager = layoutManager
if (layoutManager == null)
{
layoutManager = AbsoluteLayout()
}
this.components = ArrayList()
this.layoutManager = layoutManager
this.cachedPreferredSize = null
}

/**
 * Adds a new child component to the panel. Where within the panel the child will be displayed is up to the layout
 * manager assigned to this panel. If the component has already been added to another panel, it will first be
 * removed from that panel before added to this one.
 * @param component Child component to add to this panel
 * @return Itself
 */
     fun addComponent(component:Component?):Panel? {
return addComponent(Integer.MAX_VALUE, component)
}

/**
 * Adds a new child component to the panel. Where within the panel the child will be displayed is up to the layout
 * manager assigned to this panel. If the component has already been added to another panel, it will first be
 * removed from that panel before added to this one.
 * @param component Child component to add to this panel
 * @param index At what index to add the component among the existing components
 * @return Itself
 */
     fun addComponent(index:Int, component:Component?):Panel {
var index = index
if (component == null)
{
throw IllegalArgumentException("Cannot add null component")
}
synchronized (components) {
if (components!!.contains(component))
{
return this
}
if (component!!.getParent() != null)
{
component!!.getParent().removeComponent(component)
}
if (index > components!!.size())
{
index = components!!.size()
}
else if (index < 0)
{
index = 0
}
components!!.add(index, component)
}
component!!.onAdded(this)
invalidate()
return this
}

/**
 * This method is a shortcut for calling:
 * <pre>
 * `component.setLayoutData(layoutData);
 * panel.addComponent(component);
` * 
</pre> * 
 * @param component Component to add to the panel
 * @param layoutData Layout data to assign to the component
 * @return Itself
 */
     fun addComponent(component:Component?, layoutData:LayoutData?):Panel {
if (component != null)
{
component!!.setLayoutData(layoutData)
addComponent(component)
}
return this
}

@Override
 fun containsComponent(component:Component?):Boolean {
return component != null && component!!.hasParent(this)
}

@Override
 fun removeComponent(component:Component?):Boolean {
if (component == null)
{
throw IllegalArgumentException("Cannot remove null component")
}
synchronized (components) {
val index = components!!.indexOf(component)
if (index == -1)
{
return false
}
if (getBasePane() != null && getBasePane().getFocusedInteractable() === component)
{
getBasePane().setFocusedInteractable(null)
}
components!!.remove(index)
}
component!!.onRemoved(this)
invalidate()
return true
}

/**
 * Removes all child components from this panel
 * @return Itself
 */
     fun removeAllComponents():Panel {
synchronized (components) {
for (component in ArrayList(components))
{
removeComponent(component)
}
}
return this
}

/**
 * Assigns a new layout manager to this panel, replacing the previous layout manager assigned. Please note that if
 * the panel is not empty at the time you assign a new layout manager, the existing components might not show up
 * where you expect them and their layout data property might need to be re-assigned.
 * @param layoutManager New layout manager this panel should be using
 * @return Itself
 */
    @Synchronized  fun setLayoutManager(layoutManager:LayoutManager?):Panel {
var layoutManager = layoutManager
if (layoutManager == null)
{
layoutManager = AbsoluteLayout()
}
this.layoutManager = layoutManager
invalidate()
return this
}

/**
 * Returns the layout manager assigned to this panel
 * @return Layout manager assigned to this panel
 */
     fun getLayoutManager():LayoutManager? {
return layoutManager
}

@Override
protected fun createDefaultRenderer():ComponentRenderer<Panel?>? {
return DefaultPanelRenderer()
}

inner class DefaultPanelRenderer:ComponentRenderer<Panel?> {
private var fillAreaBeforeDrawingComponents = true

/**
 * If setting this to `false` (default is `true`), the [Panel] will not reset it's drawable
 * area with the space character ' ' before drawing all the components. Usually you **do** want to reset this
 * area before drawing but you might have a custom renderer that has prepared the area already and just want the
 * panel renderer to layout and draw the components in the panel without touching the existing content. One such
 * example is the `FullScreenTextGUITest`.
 * @param fillAreaBeforeDrawingComponents Should the panels area be cleared before drawing components?
 */
         fun setFillAreaBeforeDrawingComponents(fillAreaBeforeDrawingComponents:Boolean) {
this.fillAreaBeforeDrawingComponents = fillAreaBeforeDrawingComponents
}

@Override
 fun getPreferredSize(component:Panel?):TerminalSize? {
synchronized (components) {
cachedPreferredSize = layoutManager!!.getPreferredSize(components)
}
return cachedPreferredSize
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, panel:Panel?) {
if (isInvalid)
{
layout(graphics!!.getSize())
}

if (fillAreaBeforeDrawingComponents)
{
 // Reset the area
                graphics!!.applyThemeStyle(getThemeDefinition().getNormal())
if (fillColorOverride != null)
{
graphics!!.setBackgroundColor(fillColorOverride)
}
graphics!!.fill(' ')
}

synchronized (components) {
for (child in components!!)
{
if (!child!!.isVisible())
{
continue
}
val componentGraphics = graphics!!.newTextGraphics(child!!.getPosition(), child!!.getSize())
child!!.draw(componentGraphics)
}
}
}
}

@Override
 fun calculatePreferredSize():TerminalSize? {
if (cachedPreferredSize != null && !isInvalid)
{
return cachedPreferredSize
}
return super.calculatePreferredSize()
}

@Override
 fun nextFocus(fromThis:Interactable?):Interactable? {
var chooseNextAvailable = (fromThis == null)

synchronized (components) {
for (component in components!!)
{
if (!component!!.isVisible())
{
continue
}
if (chooseNextAvailable)
{
if (component is Interactable && (component as Interactable).isEnabled() && (component as Interactable).isFocusable())
{
return component as Interactable?
}
else if (component is Container)
{
val firstInteractable = ((component) as Container).nextFocus(null)
if (firstInteractable != null)
{
return firstInteractable
}
}
continue
}

if (component === fromThis)
{
chooseNextAvailable = true
continue
}

if (component is Container)
{
val container = component as Container?
if (fromThis!!.isInside(container))
{
val next = container!!.nextFocus(fromThis)
if (next == null)
{
chooseNextAvailable = true
}
else
{
return next
}
}
}
}
return null
}
}

@Override
 fun previousFocus(fromThis:Interactable?):Interactable? {
var chooseNextAvailable = (fromThis == null)

val reversedComponentList:List<Component?>?
synchronized (components) {
reversedComponentList = ArrayList(components)
}
Collections.reverse(reversedComponentList)

for (component in reversedComponentList!!)
{
if (!component!!.isVisible())
{
continue
}
if (chooseNextAvailable)
{
if (component is Interactable && (component as Interactable).isEnabled() && (component as Interactable).isFocusable())
{
return component as Interactable?
}
if (component is Container)
{
val lastInteractable = ((component) as Container).previousFocus(null)
if (lastInteractable != null)
{
return lastInteractable
}
}
continue
}

if (component === fromThis)
{
chooseNextAvailable = true
continue
}

if (component is Container)
{
val container = component as Container?
if (fromThis!!.isInside(container))
{
val next = container!!.previousFocus(fromThis)
if (next == null)
{
chooseNextAvailable = true
}
else
{
return next
}
}
}
}
return null
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
return false
}

@Override
 fun updateLookupMap(interactableLookupMap:InteractableLookupMap?) {
synchronized (components) {
for (component in components!!)
{
if (!component!!.isVisible())
{
continue
}
if (component is Container)
{
(component as Container).updateLookupMap(interactableLookupMap)
}
else if (component is Interactable && (component as Interactable).isEnabled() && (component as Interactable).isFocusable())
{
interactableLookupMap!!.add(component as Interactable?)
}
}
}
}

@Override
@JvmStatic  fun invalidate() {
super.invalidate()

synchronized (components) {
 //Propagate
            for (component in components!!)
{
component!!.invalidate()
}
}
}

private fun layout(size:TerminalSize?) {
synchronized (components) {
layoutManager!!.doLayout(size, components)
}
}
}/**
 * Default constructor, creates a new panel with no child components and by default set to a vertical
 * `LinearLayout` layout manager.
 */
