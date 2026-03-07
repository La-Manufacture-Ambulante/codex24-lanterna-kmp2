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
package com.googlecode.lanterna.terminal.ansi

import java.io.*
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

/**
 * UnixLikeTerminal extends from ANSITerminal and defines functionality that is common to
 * `UnixTerminal` and `CygwinTerminal`, like setting tty modes; echo, cbreak
 * and minimum characters for reading as well as a shutdown hook to set the tty back to
 * original state at the end.
 * 
 * 
 * If requested, it handles Control-C input to terminate the program, and hooks
 * into Unix WINCH signal to detect when the user has resized the terminal,
 * if supported by the JVM.
 * 
 * @author Andreas
 * @author Martin
 */
abstract class UnixLikeTTYTerminal/**
 * Creates a UnixTerminal using a specified input stream, output stream and character set, with a custom size
 * querier instead of using the default one. This way you can override size detection (if you want to force the
 * terminal to a fixed size, for example). You also choose how you want ctrl+c key strokes to be handled.
 * 
 * @param ttyDev TTY device file that is representing this terminal session, will be used when calling stty to make
 * it operate on this session
 * @param terminalInput Input stream to read terminal input from
 * @param terminalOutput Output stream to write terminal output to
 * @param terminalCharset Character set to use when converting characters to bytes
 * @param terminalCtrlCBehaviour Special settings on how the terminal will behave, see `UnixTerminalMode` for
 * more details
 * @throws IOException If there was an I/O error while setting up the terminal
 */
     @Throws(IOException::class)
 protected constructor(
private val ttyDev:File?, 
terminalInput:InputStream?, 
terminalOutput:OutputStream?, 
terminalCharset:Charset?, 
terminalCtrlCBehaviour:CtrlCBehaviour?):UnixLikeTerminal(terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {
private var sttyStatusToRestore:String? = null

protected// Issue #519: this will hopefully be more portable across linux distributions
 // Previously we hard-coded "/bin/stty" here
 val sttyCommand:Array<String?>?
get() {
val sttyOverride = System.getProperty("com.googlecode.lanterna.terminal.UnixTerminal.sttyCommand")
if (sttyOverride != null)
{
return arrayOf<String?>(sttyOverride)
}
else
{
return arrayOf<String?>("/usr/bin/env", "stty")
}
}

init{

 // Take ownership of the terminal
        realAcquire()
}

@Override
@Throws(IOException::class)
protected fun acquire() {
 // Hack!
    }

@Throws(IOException::class)
private fun realAcquire() {
super.acquire()
}

@Override
@Throws(IOException::class)
protected fun registerTerminalResizeListener(onResize:Runnable?) {
try
{
val signalClass = Class.forName("sun.misc.Signal")
for (m in signalClass!!.getDeclaredMethods())
{
if ("handle".equals(m!!.getName()))
{
val windowResizeHandler = Proxy.newProxyInstance(getClass().getClassLoader(), arrayOf<Class?>(Class.forName("sun.misc.SignalHandler")), { proxy, method, args->
if ("handle".equals(method!!.getName()))
{
onResize!!.run()
}
null })
m!!.invoke(null, signalClass!!.getConstructor(String::class.java).newInstance("WINCH"), windowResizeHandler)
}
}
}
catch (ignore:Throwable) {
 // We're probably running on a non-Sun JVM and there's no way to catch signals without resorting to native
            // code integration
        }

}

@Override
@Throws(IOException::class)
protected fun saveTerminalSettings() {
sttyStatusToRestore = runSTTYCommand("-g")!!.trim()
}

@Override
@Throws(IOException::class)
protected fun restoreTerminalSettings() {
if (sttyStatusToRestore != null)
{
runSTTYCommand(sttyStatusToRestore)
}
}

@Override
@Throws(IOException::class)
protected fun keyEchoEnabled(enabled:Boolean) {
runSTTYCommand(if (enabled) "echo" else "-echo")
}

@Override
@Throws(IOException::class)
protected fun canonicalMode(enabled:Boolean) {
runSTTYCommand(if (enabled) "icanon" else "-icanon")
if (!enabled)
{
runSTTYCommand("min", "1")
}
}

@Override
@Throws(IOException::class)
protected fun keyStrokeSignalsEnabled(enabled:Boolean) {
if (enabled)
{
runSTTYCommand("intr", "^C")
}
else
{
runSTTYCommand("intr", "undef")
}
}

@Throws(IOException::class)
protected fun runSTTYCommand(vararg parameters:String?):String? {
val commandLine = ArrayList(Arrays.asList(
sttyCommand))
commandLine.addAll(Arrays.asList(parameters))
return exec(commandLine.toArray(arrayOfNulls<String?>(0)))
}

@Throws(IOException::class)
protected fun exec(vararg cmd:String?):String? {
val pb = ProcessBuilder(cmd)
if (ttyDev != null)
{
pb.redirectInput(ProcessBuilder.Redirect.from(ttyDev))
}
val process = pb.start()
val stdoutBuffer = ByteArrayOutputStream()
val stdout = process!!.getInputStream()
var readByte = stdout!!.read()
while (readByte >= 0)
{
stdoutBuffer.write(readByte)
readByte = stdout!!.read()
}
val stdoutBufferInputStream = ByteArrayInputStream(stdoutBuffer.toByteArray())
val reader = BufferedReader(InputStreamReader(stdoutBufferInputStream))
val builder = StringBuilder()
val line:String?
while ((line = reader.readLine()) != null)
{
builder.append(line)
}
reader.close()
return builder.toString()
}
}