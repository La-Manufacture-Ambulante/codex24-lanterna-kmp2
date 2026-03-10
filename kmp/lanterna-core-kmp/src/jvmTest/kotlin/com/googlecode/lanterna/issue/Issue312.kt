package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

/**
 * Created by Martin on 2017-07-09.
 */
 object Issue312 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()!!
val textGraphics = terminal!!.newTextGraphics()
var row = 0
while (true)
{
var keyStroke = terminal!!.pollInput()
if (keyStroke == null)
{
terminal!!.setCursorPosition(0, 0)
textGraphics!!.putString(0, terminal!!.getCursorPosition().getRow(), " > ")
terminal!!.flush()
keyStroke = terminal!!.readInput()
row = 1
terminal!!.clearScreen()
}
if (keyStroke!!.getKeyType() === KeyType.ESCAPE || keyStroke!!.getKeyType() === KeyType.EOF)
{
break
}
textGraphics!!.putString(0, row++, "Read KeyStroke: " + keyStroke + "\n")
terminal!!.flush()
}
terminal!!.close()
}
}
