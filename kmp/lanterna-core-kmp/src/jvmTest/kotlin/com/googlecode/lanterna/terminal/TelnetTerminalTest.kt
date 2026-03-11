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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Random

/**
 * 
 * @author martin
 */
 object TelnetTerminalTest {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val server = TelnetTerminalServer(1024, StandardCharsets.UTF_8)

        while (true)
{
val telnetTerminal = server.acceptConnection()
if (telnetTerminal != null)
{
spawnColorTest(telnetTerminal)
}
}
}

private fun spawnColorTest(terminal:TelnetTerminal?) {
object:Thread() {

@Volatile private var terminalSizeCurrent:TerminalSize? = null

  override fun run() {
try
{
val string = "Hello!"
val random = Random()
terminal!!.enterPrivateMode()
terminal!!.clearScreen()
terminal!!.addResizeListener(object : TerminalResizeListener {
override fun onResized(terminal1: Terminal?, newSize: TerminalSize?) {
System.err.println("Resized to " + newSize)
terminalSizeCurrent = newSize
}
})
terminalSizeCurrent = terminal!!.getTerminalSize()

terminal!!.setCursorPosition(3, 3)
printString(terminal, "Press any key to start")
terminal!!.readInput() // Test blocking input

while (true)
{
val key = terminal!!.pollInput()
if (key != null)
{
System.out.println(key)
if (key!!.getKeyType() == KeyType.ESCAPE)
{
terminal!!.exitPrivateMode()
return 
}
}

val foregroundIndex = TextColor.Indexed.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255))
val backgroundIndex = TextColor.Indexed.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255))

terminal!!.setForegroundColor(foregroundIndex)
terminal!!.setBackgroundColor(backgroundIndex)
terminal!!.setCursorPosition(
random.nextInt(terminalSizeCurrent!!.getColumns() - string.length),
random.nextInt(terminalSizeCurrent!!.getRows())
)
printString(terminal, string)

try
{
Thread.sleep(200)
}
catch (e:InterruptedException) {}

}
}
catch (e:IOException) {
e!!.printStackTrace()
}
finally
{
try
{
terminal!!.close()
}
catch (e:IOException) {
e!!.printStackTrace()
}

}
}
}.start()
}

@Throws(IOException::class)
private fun printString(terminal:Terminal?, string:String) {
for (i in 0 until string.length)
terminal!!.putCharacter(string.charAt(i))
terminal!!.flush()
}
}
