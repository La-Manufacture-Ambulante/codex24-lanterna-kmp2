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

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer

import java.io.IOException
import java.net.SocketException
import java.util.ArrayList

 object GUIOverTelnet {

private val ALL_TEXTBOXES = ArrayList()
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val telnetTerminalServer = TelnetTerminalServer(1024)
System.out.println("Listening on port 1024, please connect to it with a separate telnet process")

        while (true)
{
val telnetTerminal = telnetTerminalServer.acceptConnection()
System.out.println("Accepted connection from " + telnetTerminal!!.getRemoteSocketAddress())
val thread = Thread({ try
{
runGUI(telnetTerminal)
}
catch (e:IOException) {
e!!.printStackTrace()
}

try
{
telnetTerminal!!.close()
}
catch (ignore:IOException) {}
 })
thread.start()
}
}

@SuppressWarnings("rawtypes")
@Throws(IOException::class)
private fun runGUI(telnetTerminal:TelnetTerminal?) {
val screen = TerminalScreen(telnetTerminal)
screen.startScreen()
val textGUI = MultiWindowTextGUI(screen)
textGUI.setBlockingIO(false)
textGUI.setEOFWhenNoWindows(true)
try
{
val window = BasicWindow("Text GUI over Telnet")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(Button("Button", { val messageBox = BasicWindow("Response")
messageBox.setComponent(Panels.vertical(
Label("Hello!"), 
Button("Close", Runnable({ messageBox.close() }))))
textGUI.addWindow(messageBox) }).withBorder(Borders.singleLine("This is a button")))


val textBox = object:TextBox(TerminalSize(20, 4)) {
@Override
 fun handleKeyStroke(keyStroke:KeyStroke):Result? {
try
{
return super.handleKeyStroke(keyStroke)
}

finally
{
for (box in ALL_TEXTBOXES)
{
if (this != box)
{
box!!.setText(getText())
}
}
}
}
}
ALL_TEXTBOXES.add(textBox)
contentArea.addComponent(textBox.withBorder(Borders.singleLine("Text editor")))
contentArea.addComponent(object:AbstractInteractableComponent() {
internal var text:String? = "Press any key"
@Override
protected fun createDefaultRenderer():InteractableRenderer? {
return object:InteractableRenderer() {
@Override
 fun getPreferredSize(component:Component?):TerminalSize {
return TerminalSize(30, 1)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, component:Component?) {
graphics!!.putString(0, 0, text)
}

@Override
 fun getCursorLocation(component:Component?):TerminalPosition? {
return TerminalPosition.TOP_LEFT_CORNER
}
}
}

@Override
 fun handleKeyStroke(keyStroke:KeyStroke):Result? {
if ((keyStroke!!.getKeyType() === KeyType.TAB || keyStroke!!.getKeyType() === KeyType.REVERSE_TAB))
{
return super.handleKeyStroke(keyStroke)
}
if (keyStroke!!.getKeyType() === KeyType.CHARACTER)
{
text = ("Character: " + keyStroke!!.getCharacter() + (if (keyStroke!!.isCtrlDown()) " (ctrl)" else "") + 
(if (keyStroke!!.isAltDown()) " (alt)" else ""))
}
else
{
text = ("Key: " + keyStroke!!.getKeyType() + (if (keyStroke!!.isCtrlDown()) " (ctrl)" else "") + 
(if (keyStroke!!.isAltDown()) " (alt)" else ""))
}
return Result.HANDLED
}
}.withBorder(Borders.singleLine("Custom component")))

contentArea.addComponent(Button("Close", Runnable({ window.close() })))
window.setComponent(contentArea)

textGUI.addWindowAndWait(window)
}

finally
{
try
{
screen.stopScreen()
}
catch (ignore:SocketException) {
 // If the telnet client suddenly quit, we'll get an exception when we try to get the client to exit
                // private mode, but that's fine, no need to report this
            }

}
}
}
