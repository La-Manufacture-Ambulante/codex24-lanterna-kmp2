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
package com.googlecode.lanterna.gui2.menu

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.gui2.BasePane
import com.googlecode.lanterna.gui2.InteractableRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyStroke

/**
 * This class is a single item that appears in a [Menu] with an optional action attached to it
 */
 class MenuItem/**
 * Creates a new [MenuItem] with a label and an action that will run on the GUI thread when activated. When
 * the action has finished, the [Menu] containing this item will close.
 * @param label Label of the new [MenuItem]
 * @param action Action to invoke on the GUI thread when the menu item is activated
 */
     @JvmOverloads  constructor(label:String?, private val action:Runnable? = {  }):AbstractInteractableComponent<MenuItem?>() {
/**
 * Returns the label of this menu item
 * @return Label of this menu item
 */
     val label:String?

 val accelerator:KeyStroke?
get() {
return super.getAccelerator()
}

init{
if (label == null || label!!.trim().isEmpty())
{
throw IllegalArgumentException("Menu label is not allowed to be null or empty")
}
this.label = label!!.trim()
}

 fun setAccelerator(keyStroke:KeyStroke?):MenuItem? {
return super.setAccelerator(keyStroke)
}

@Override
protected fun createDefaultRenderer():InteractableRenderer<MenuItem?>? {
return DefaultMenuItemRenderer()
}

/**
 * Method to invoke when a menu item is "activated" by pressing the Enter key.
 * @return Returns `true` if the action was performed successfully, otherwise `false`, which will not
 * automatically close the popup window itself.
 */
    protected fun onActivated():Boolean {
action!!.run()
return true
}

@Override
protected fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isActivationStroke(keyStroke) || isKeyboardAcceleratorStroke(keyStroke))
{
takeFocus()

if (onActivated())
{
val basePane = getBasePane()
if (basePane is Window && (basePane as Window).getHints().contains(Window.Hint.MENU_POPUP))
{
(basePane as Window).close()
}
}
return Result.HANDLED
}
else if (isMouseMove(keyStroke))
{
takeFocus()
return Result.HANDLED
}

return super.handleKeyStroke(keyStroke)
}

/**
 * Helper interface that doesn't add any new methods but makes coding new menu renderers a little bit more clear
 */
    abstract class MenuItemRenderer:InteractableRenderer<MenuItem?>

/**
 * Default renderer for menu items (both sub-menus and regular items)
 */
     class DefaultMenuItemRenderer:MenuItemRenderer() {
@Override
 fun getCursorLocation(component:MenuItem?):TerminalPosition? {
return null
}

@Override
 fun getPreferredSize(component:MenuItem):TerminalSize? {
var preferredWidth = TerminalTextUtils.getColumnWidth(component.label) + 2
if (component is Menu && !(component.getParent() is MenuBar))
{
preferredWidth += 2
}
return TerminalSize.ONE.withColumns(preferredWidth)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, menuItem:MenuItem) {
val themeDefinition = menuItem.getThemeDefinition()
if (menuItem.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}

val label = menuItem.label
val leadingCharacter = label!!.substring(0, 1)

graphics!!.fill(' ')
graphics!!.putString(1, 0, label)
if (menuItem is Menu && !(menuItem.getParent() is MenuBar))
{
graphics!!.putString(graphics!!.getSize().getColumns() - 2, 0, String.valueOf(Symbols.TRIANGLE_RIGHT_POINTING_BLACK))
}
if (!label!!.isEmpty())
{
if (menuItem.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
graphics!!.putString(1, 0, leadingCharacter)
}
}
}
}/**
 * Creates a [MenuItem] with a label that does nothing when activated
 * @param label Label of the new [MenuItem]
 */