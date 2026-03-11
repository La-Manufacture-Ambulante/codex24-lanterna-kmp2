/*
 * Author Rajatt, modified by Andreas
 */
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import java.io.IOException
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.Terminal

 object Issue274 {

@Throws(IOException::class)
 fun main(args:Array<String?>?) {

val ter = DefaultTerminalFactory()
.setForceTextTerminal(true)
.setMouseCaptureMode(MouseCaptureMode.CLICK)
.setTelnetPort(1024)
.createTerminal()!!

val screen = TerminalScreen(ter)
screen.startScreen()
val gui = MultiWindowTextGUI(screen)

val menubar = Panel()
menubar.setLayoutManager(LinearLayout(Direction.HORIZONTAL).setSpacing(1))

val text = TextBox(TerminalSize(10, 10), TextBox.Style.MULTI_LINE)
menubar.addComponent(text)

menubar.addComponent(Button("Open", {
val op = BasicWindow("Select file")
op.component = Button("Close", Runnable { op.close() })
gui.addWindow(op)
}))

menubar.addComponent(Button("Save"))

menubar.addComponent(Button("Exit", { gui.activeWindow?.close() }))

val main = BasicWindow("Test")
main.component = menubar
try
{
gui.addWindowAndWait(main)
}

finally
{
screen.stopScreen()
}
}
}
