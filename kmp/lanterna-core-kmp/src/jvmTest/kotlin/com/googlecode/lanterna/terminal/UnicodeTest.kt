package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen

import java.io.IOException

 object UnicodeTest {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
val terminal = TestTerminalFactory(args).createTerminal()
val screen = TerminalScreen(terminal)
screen.startScreen()
screen.newTextGraphics().putString(4, 2, "Hello!")
screen.setCharacter(4, 2, TextCharacter.fromString("·")[0])
screen.refresh()
Thread.sleep(5000)
screen.stopScreen()
}
}
