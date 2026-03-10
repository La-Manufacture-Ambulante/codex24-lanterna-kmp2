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
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

 class MultiWindowManagerTest:TestBase() {

private var virtualScreenEnabled = true
private var buttonToggleVirtualScreen:Button? = null

@Override
 fun init(textGUI:WindowBasedTextGUI) {
textGUI.getBackgroundPane().setComponent(BackgroundComponent())
val mainWindow = BasicWindow("Multi Window Test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(Button("Add new window", { onNewWindow(textGUI) }))
buttonToggleVirtualScreen = Button("Virtual Screen: Enabled", { virtualScreenEnabled = !virtualScreenEnabled
textGUI.setVirtualScreenEnabled(virtualScreenEnabled)
buttonToggleVirtualScreen!!.setLabel("Virtual Screen: " + (if (virtualScreenEnabled) "Enabled" else "Disabled")) })
contentArea.addComponent(buttonToggleVirtualScreen)
contentArea.addComponent(EmptySpace(TerminalSize.ONE))
contentArea.addComponent(Button("Close", ???({ mainWindow.close() })))
mainWindow.setComponent(contentArea)
textGUI.addListener({ textGUI1, keyStroke->
if (((keyStroke!!.isCtrlDown() && keyStroke!!.getKeyType() === KeyType.TAB) || keyStroke!!.getKeyType() === KeyType.F6))
{
(textGUI1 as WindowBasedTextGUI).cycleActiveWindow(false)
}
else if (((keyStroke!!.isCtrlDown() && keyStroke!!.getKeyType() === KeyType.REVERSE_TAB) || keyStroke!!.getKeyType() === KeyType.F7))
{
(textGUI1 as WindowBasedTextGUI).cycleActiveWindow(true)
}
else
{
return@textGUI.addListener false
}
true })
textGUI.addWindow(mainWindow)
}

private fun onNewWindow(textGUI:WindowBasedTextGUI?) {
val window = DynamicWindow()
val availableThemes = ArrayList(LanternaThemes.getRegisteredThemes())
val themeName = availableThemes.get(nextTheme++)
if (nextTheme == availableThemes.size())
{
nextTheme = 0
}
window.setTheme(LanternaThemes.getRegisteredTheme(themeName))
textGUI!!.addWindow(window)
}

private class DynamicWindow:BasicWindow("Window #" + WINDOW_COUNTER.incrementAndGet()) {

private val labelWindowSize:Label?
private val labelWindowPosition:Label?
private val labelUnlockWindow:Label?
init{

val statsTableContainer = Panel()
statsTableContainer.setLayoutManager(GridLayout(2))
statsTableContainer.addComponent(Label("Position:"))
this.labelWindowPosition = Label("")
statsTableContainer.addComponent(labelWindowPosition)
statsTableContainer.addComponent(Label("Size:"))
this.labelWindowSize = Label("")
statsTableContainer.addComponent(labelWindowSize)
statsTableContainer.addComponent(Label("Auto-sized:"))
this.labelUnlockWindow = Label("true")
statsTableContainer.addComponent(labelUnlockWindow)

addWindowListener(object:WindowListenerAdapter() {
@Override
 fun onResized(window:Window?, oldSize:TerminalSize?, newSize:TerminalSize?) {
labelWindowSize!!.setText(newSize!!.toString())
}

@Override
 fun onMoved(window:Window?, oldPosition:TerminalPosition?, newPosition:TerminalPosition?) {
labelWindowPosition!!.setText(newPosition!!.toString())
}
})

val contentArea = Panel()
contentArea.setLayoutManager(GridLayout(1))
contentArea.addComponent(statsTableContainer)
contentArea.addComponent(EmptySpace(TerminalSize.ONE))
contentArea.addComponent(
Label(
("Move window with ALT+Arrow\n" + "Resize window with CTRL+Arrow")))
contentArea.addComponent(EmptySpace(TerminalSize.ONE).setLayoutData(
GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true)))
contentArea.addComponent(
Panels.horizontal(
Button("Toggle auto-sized", ???({ this.toggleManaged() })), 
Button("Close", ???({ this.close() }))))
setComponent(contentArea)
}

private fun toggleManaged() {
var isManaged = !getHints().contains(Hint.FIXED_SIZE)
isManaged = !isManaged
if (isManaged)
{
setHints(Collections.emptyList<Hint?>())
}
else
{
setHints(Collections.singletonList(Hint.FIXED_SIZE))
}
labelUnlockWindow!!.setText(Boolean.toString(isManaged))
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
var handled = super.handleInput(key)
if (!handled)
{
when (key!!.getKeyType()) {
ARROW_DOWN -> {
if (key!!.isAltDown())
{
setPosition(getPosition().withRelativeRow(1))
}
else if (key!!.isCtrlDown())
{
setFixedSize(getSize().withRelativeRows(1))
labelUnlockWindow!!.setText("false")
}
handled = true
}
ARROW_LEFT -> {
if (key!!.isAltDown())
{
setPosition(getPosition().withRelativeColumn(-1))
}
else if (key!!.isCtrlDown() && getSize().getColumns() > 1)
{
setFixedSize(getSize().withRelativeColumns(-1))
labelUnlockWindow!!.setText("false")
}
handled = true
}
ARROW_RIGHT -> {
if (key!!.isAltDown())
{
setPosition(getPosition().withRelativeColumn(1))
}
else if (key!!.isCtrlDown())
{
setFixedSize(getSize().withRelativeColumns(1))
labelUnlockWindow!!.setText("false")
}
handled = true
}
ARROW_UP -> {
if (key!!.isAltDown())
{
setPosition(getPosition().withRelativeRow(-1))
}
else if (key!!.isCtrlDown() && getSize().getRows() > 1)
{
setFixedSize(getSize().withRelativeRows(-1))
labelUnlockWindow!!.setText("false")
}
handled = true
}
}
}
return handled
}
}

private class BackgroundComponent:GUIBackdrop() {
@Override
protected fun createDefaultRenderer():ComponentRenderer<EmptySpace?> {
return object:ComponentRenderer<EmptySpace?>() {
@Override
 fun getPreferredSize(component:EmptySpace?):TerminalSize {
return TerminalSize.ONE
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:EmptySpace?) {
graphics!!.applyThemeStyle(component!!.getTheme().getDefinition(GUIBackdrop::class.java).getNormal())
graphics!!.fill('・')
val text = "Press <CTRL+Tab>/F6 and <CTRL+Shift+Tab>/F7 to cycle active window"
graphics!!.putString(graphics!!.getSize().getColumns() - text.length() - 4, graphics!!.getSize().getRows() - 1, text)
}
}
}


}

companion object {

private val WINDOW_COUNTER = AtomicInteger(0)

@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
MultiWindowManagerTest().run(args)
}

private var nextTheme = 0
}
}
