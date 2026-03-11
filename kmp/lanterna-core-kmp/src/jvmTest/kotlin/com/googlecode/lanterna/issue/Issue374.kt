package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.util.Collections

 object Issue374 {

@Throws(Exception::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()

val window = BasicWindow("FocusTraversalTest")
window.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN))
val gui = MultiWindowTextGUI(screen)

val mainPanel = Panel(LinearLayout())
window.component = mainPanel

val disabledInBorder1 = Button("disabledB1")
disabledInBorder1.setEnabled(false)
mainPanel.addComponent(disabledInBorder1.withBorder(Borders.singleLine("border")))

val first = Button("enabled")
mainPanel.addComponent(first)

val disabled = Button("disabled")
disabled.setEnabled(false)
mainPanel.addComponent(disabled)

val disabledInBorder2 = Button("disabledB2")
disabledInBorder2.setEnabled(false)
mainPanel.addComponent(disabledInBorder2.withBorder(Borders.singleLine("border")))

val anotherFocusable = Button("focusable")
mainPanel.addComponent(anotherFocusable)

mainPanel.addComponent(Button("Button"))

first.takeFocus()
gui.addWindowAndWait(window)
}

}
