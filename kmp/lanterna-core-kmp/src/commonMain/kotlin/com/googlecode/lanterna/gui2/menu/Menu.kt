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
import java.util.concurrent.atomic.AtomicBoolean

import com.googlecode.lanterna.gui2.MenuPopupWindow
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.WindowListenerAdapter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * Implementation of a drop-down menu contained in a [MenuBar] and also a sub-menu inside another [Menu].
 */
 class Menu/**
 * Creates a menu with the specified label
 * @param label Label to use for the menu item that will trigger this menu to pop up
 */
    (label:String?):MenuItem(label) {
private val subItems:List<MenuItem?>?

 val subMenus:List<MenuItem?>?
get() {
return ArrayList(subItems)
}

init{
this.subItems = ArrayList()
}

/**
 * Adds a new menu item to this menu, this can be either a regular [MenuItem] or another [Menu]
 * @param menuItem The item to add to this menu
 * @return Itself
 */
     fun add(menuItem:MenuItem?):Menu {
synchronized (subItems) {
subItems!!.add(menuItem)
}
return this
}

 fun setAccelerator(keyStroke:KeyStroke?):Menu {
super.setAccelerator(keyStroke)
return this
}

@Override
protected fun onActivated():Boolean {
var result = true
if (subItems!!.isEmpty())
{
return result
}
val popupMenu = MenuPopupWindow(this)
val popupCancelled = AtomicBoolean(false)
for (menuItem in subItems!!)
{
popupMenu.addMenuItem(menuItem)
}
if (getParent() is MenuBar)
{
val menuBar = getParent() as MenuBar
popupMenu.addWindowListener(object:WindowListenerAdapter() {
@Override
 fun onUnhandledInput(basePane:Window?, keyStroke:KeyStroke?, hasBeenHandled:AtomicBoolean?) {
if (keyStroke!!.getKeyType() === KeyType.ARROW_LEFT)
{
val thisMenuIndex = menuBar!!.getChildrenList().indexOf(this@Menu)
if (thisMenuIndex > 0)
{
popupMenu.close()
val nextSelectedMenu = menuBar!!.getMenu(thisMenuIndex - 1)
nextSelectedMenu!!.takeFocus()
nextSelectedMenu!!.onActivated()
}
}
else if (keyStroke!!.getKeyType() === KeyType.ARROW_RIGHT)
{
val thisMenuIndex = menuBar!!.getChildrenList().indexOf(this@Menu)
if (thisMenuIndex >= 0 && thisMenuIndex < menuBar!!.getMenuCount() - 1)
{
popupMenu.close()
val nextSelectedMenu = menuBar!!.getMenu(thisMenuIndex + 1)
nextSelectedMenu!!.takeFocus()
nextSelectedMenu!!.onActivated()
}
}

 // Check if accelerator for c
                    for (menuItem in subItems!!)
{
if (menuItem!!.isEnabled() && menuItem!!.isKeyboardAcceleratorStroke(keyStroke))
{
val result = menuItem!!.handleKeyStroke(keyStroke)
if (result === Result.HANDLED) break
}
}
}
})
}
popupMenu.addWindowListener(object:WindowListenerAdapter() {
@Override
 fun onUnhandledInput(basePane:Window?, keyStroke:KeyStroke?, hasBeenHandled:AtomicBoolean?) {
if (keyStroke!!.getKeyType() === KeyType.ESCAPE)
{
popupCancelled.set(true)
popupMenu.close()
}
}
})
(getTextGUI() as WindowBasedTextGUI).addWindowAndWait(popupMenu)
result = !popupCancelled.get()

return result
}
}
