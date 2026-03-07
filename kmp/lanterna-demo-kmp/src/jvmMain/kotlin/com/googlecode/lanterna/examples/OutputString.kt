package com.googlecode.lanterna.examples

import java.io.IOException

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal


 object OutputString {

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)

val s = "Hello World!"
val tGraphics = screen.newTextGraphics()

screen.startScreen()
screen.clear()

tGraphics!!.putString(10, 10, s)
screen.refresh()

screen.readInput()
screen.stopScreen()
}

}
