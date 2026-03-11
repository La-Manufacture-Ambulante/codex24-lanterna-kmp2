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
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.TreeMap

/**
 * 
 * @author martin
 */
 object PseudoTerminal {

@Throws(InterruptedException::class, IOException::class)
 fun main(args:Array<String?>?) {
val rawTerminal = TestTerminalFactory(args).createTerminal()

 //assume bash is available
        val bashProcess = Runtime.getRuntime().exec("bash", makeEnvironmentVariables())
val stdout = ProcessOutputReader(bashProcess!!.getInputStream(), rawTerminal)
val stderr = ProcessOutputReader(bashProcess!!.getErrorStream(), rawTerminal)
val stdin = ProcessInputWriter(bashProcess!!.getOutputStream(), rawTerminal)
stdout.start()
stderr.start()
stdin.start()
val returnCode = bashProcess!!.waitFor()
stdout.stop()
stderr.stop()
stdin.stop()
System.exit(returnCode)
}

private fun makeEnvironmentVariables():Array<String?>? {
	val environment = ArrayList<String>()
	val env = TreeMap<String, String>(System.getenv())
env.put("TERM", "xterm")   //Will this make bash detect us as a proper terminal??
for (key in env.keys)
{
environment.add(key + "=" + env.get(key))
}
return environment.toArray(arrayOfNulls<String?>(0))
}

private class ProcessOutputReader(inputStream:InputStream?, private val terminalEmulator:Terminal?) {

private val inputStreamReader:InputStreamReader?
private var stop:Boolean = false

init{
this.inputStreamReader = InputStreamReader(inputStream, Charset.defaultCharset())
this.stop = false
}

fun start() {
object:Thread("OutputReader") {
  override fun run() {
try
{
val buffer = CharArray(1024)
var readCharacters = inputStreamReader!!.read(buffer)
while (readCharacters != -1 && !stop)
{
if (readCharacters > 0)
{
for (i in 0 until readCharacters)
{
terminalEmulator!!.putCharacter(buffer[i])
}
terminalEmulator!!.flush()
}
else
{
try
{
Thread.sleep(1)
}
catch (e:InterruptedException) {}

}
readCharacters = inputStreamReader!!.read(buffer)
}
}
catch (e:IOException) {
e!!.printStackTrace()
}
finally
{
try
{
inputStreamReader!!.close()
}
catch (e:IOException) {}

}
}
}.start()
}

fun stop() {
stop = true
}
}

private class ProcessInputWriter(private val outputStream:OutputStream?, private val terminalEmulator:Terminal?) {
private var stop:Boolean = false

init{
this.stop = false
}

fun start() {
object:Thread("InputWriter") {
  override fun run() {
try
{
while (!stop)
{
val keyStroke = terminalEmulator!!.pollInput()
if (keyStroke == null)
{
Thread.sleep(1)
}
else
{
when (keyStroke!!.keyType) {
KeyType.CHARACTER -> writeCharacter(keyStroke!!.character!!)
KeyType.ENTER -> writeCharacter('\n')
KeyType.BACKSPACE -> writeCharacter('\b')
KeyType.TAB -> writeCharacter('\t')
else -> {}
}
flush()
}
}
}
catch (e:IOException) {}
catch (e:InterruptedException) {}
finally
{
try
{
outputStream!!.close()
}
catch (e:IOException) {}

}
}
}.start()
}

@Throws(IOException::class)
private fun writeCharacter(character:Char) {
	outputStream!!.write(character.code)
	terminalEmulator!!.putCharacter(character)
}

@Throws(IOException::class)
private fun flush() {
outputStream!!.flush()
terminalEmulator!!.flush()
}

fun stop() {
stop = true
}
}
}
