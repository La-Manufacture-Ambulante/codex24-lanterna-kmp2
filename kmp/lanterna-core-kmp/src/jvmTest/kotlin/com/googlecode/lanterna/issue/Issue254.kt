package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object Issue254 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)
screen.startScreen()

 // Now resize the window to the smallest possible size allowed by the window manager
        // Then quickly make it bigger by grabbing the bottom right corner
    }
}
