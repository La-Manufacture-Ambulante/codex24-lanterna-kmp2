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
 * Copyright (C) 2017 Bruno Eberhard
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */
package com.googlecode.lanterna.gui2.menu

import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Container
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.InteractableLookupMap
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyStroke

/**
 * A menu bar offering drop-down menus. You can attach a menu bar to a [Window] by using the
 * [Window.setMenuBar] method, then use [MenuBar.add] to add sub-menus to the menu bar.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @author Bruno Eberhard
 * @author Martin Berglund
 */
@SuppressWarnings("SuspiciousMethodCalls")
 class MenuBar:AbstractComponent<MenuBar?>(), Container {
private val menus:List<Menu?>?

 val childCount:Int
@Override
get() {
return menuCount
}

 val childrenList:List<Component?>?
@Override
get() {
return ArrayList(menus)
}

 val children:Collection<Component?>?
@Override
get() {
return childrenList
}

/**
 * Returns the number of menus this menu bar currently has
 * @return The number of menus this menu bar currently has
 */
     val menuCount:Int
get() {
return menus!!.size()
}

 val isEmptyMenuBar:Boolean
get() {
return false
}
/**
 * Creates a new menu bar
 */
    init{
this.menus = CopyOnWriteArrayList()
}

/**
 * Adds a new drop-down menu to the menu bar, at the end
 * @param menu Menu to add to the menu bar
 * @return Itself
 */
     fun add(menu:Menu?):MenuBar {
menus!!.add(menu)
menu!!.onAdded(this)
return this
}

@Override
 fun containsComponent(component:Component?):Boolean {
return menus!!.contains(component)
}

@Override
@Synchronized  fun removeComponent(component:Component?):Boolean {
val hadMenu = menus!!.remove(component)
if (hadMenu)
{
component!!.onRemoved(this)
}
return hadMenu
}

@Override
@Synchronized  fun nextFocus(fromThis:Interactable?):Interactable? {
if (menus!!.isEmpty())
{
return null
}
else if (fromThis == null)
{
return menus!!.get(0)
}
else if (!menus!!.contains(fromThis) || menus!!.indexOf(fromThis) === menus!!.size() - 1)
{
return null
}
else
{
return menus!!.get(menus!!.indexOf(fromThis) + 1)
}
}

@Override
 fun previousFocus(fromThis:Interactable?):Interactable? {
if (menus!!.isEmpty())
{
return null
}
else if (fromThis == null)
{
return menus!!.get(menus!!.size() - 1)
}
else if (!menus!!.contains(fromThis) || menus!!.indexOf(fromThis) === 0)
{
return null
}
else
{
return menus!!.get(menus!!.indexOf(fromThis) - 1)
}
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
 // Process top level menus
    	for (mnu in menus!!)
{
 // Check to see if handled by accelerator 
    		if (mnu!!.isKeyboardAcceleratorStroke(key))
{
mnu!!.handleKeyStroke(key)
return true
}
}
return false
}

/**
 * Returns the drop-down menu at the specified index. This method will throw an Array
 * @param index Index of the menu to return
 * @return The drop-down menu at the specified index
 * @throws IndexOutOfBoundsException if the index is out of range
 */
     fun getMenu(index:Int):Menu? {
return menus!!.get(index)
}

@Override
protected fun createDefaultRenderer():ComponentRenderer<MenuBar?>? {
return DefaultMenuBarRenderer()
}

@Override
@Synchronized  fun updateLookupMap(interactableLookupMap:InteractableLookupMap?) {
for (menu in menus!!)
{
interactableLookupMap!!.add(menu)
}
}

@Override
 fun toBasePane(position:TerminalPosition?):TerminalPosition? {
 // Assume the menu is always at the top of the content panel
        return position
}

/**
 * The default implementation for rendering a [MenuBar]
 */
    inner class DefaultMenuBarRenderer:ComponentRenderer<MenuBar?> {
@Override
 fun getPreferredSize(menuBar:MenuBar):TerminalSize {
var maxHeight = 1
var totalWidth = EXTRA_PADDING
for (i in 0 until menuBar.menuCount)
{
val menu = menuBar.getMenu(i)
val preferredSize = menu!!.getPreferredSize()
maxHeight = Math.max(maxHeight, preferredSize!!.rows)
totalWidth += preferredSize!!.columns
}
totalWidth += EXTRA_PADDING
return TerminalSize(totalWidth, maxHeight)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, menuBar:MenuBar) {
 // Reset the area
            graphics.applyThemeStyle(getThemeDefinition().getNormal())
graphics.fill(' ')

var leftPosition = EXTRA_PADDING
val size = graphics.getSize()
var remainingSpace = size!!.columns - EXTRA_PADDING
var i = 0
while (i < menuBar.menuCount && remainingSpace > 0)
{
val menu = menuBar.getMenu(i)
val preferredSize = menu!!.getPreferredSize()
menu!!.setPosition(menu!!.getPosition()
.withColumn(leftPosition)
.withRow(0))
val finalWidth = Math.min(preferredSize!!.columns, remainingSpace)
menu!!.setSize(menu!!.getSize()
.withColumns(finalWidth)
.withRows(size!!.rows))
remainingSpace -= finalWidth + EXTRA_PADDING
leftPosition += finalWidth + EXTRA_PADDING
val componentGraphics = graphics.newTextGraphics(menu!!.getPosition(), menu!!.getSize())
menu!!.draw(componentGraphics)
i++
}
}
}

companion object {
private val EXTRA_PADDING = 0
}
}
