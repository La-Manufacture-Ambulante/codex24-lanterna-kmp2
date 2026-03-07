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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.CygwinTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer
import com.googlecode.lanterna.terminal.ansi.UnixLikeTTYTerminal
import com.googlecode.lanterna.terminal.ansi.UnixTerminal
import com.googlecode.lanterna.terminal.swing.*

import java.io.Console
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.util.EnumSet

/**
 * This TerminalFactory implementation uses a simple auto-detection mechanism for figuring out which terminal
 * implementation to create based on characteristics of the system the program is running on.
 * 
 * 
 * Note that for all systems with a graphical environment present, the SwingTerminalFrame will be chosen. You can
 * suppress this by calling setForceTextTerminal(true) on this factory.
 * @author martin
 */
 class DefaultTerminalFactory/**
 * Creates a new DefaultTerminalFactory with I/O and character set options customisable.
 * @param outputStream Output stream to use for text-based Terminal implementations
 * @param inputStream Input stream to use for text-based Terminal implementations
 * @param charset Character set to assume the client is using
 */
     @SuppressWarnings("SameParameterValue", "WeakerAccess")
 constructor(private val outputStream:OutputStream?, private val inputStream:InputStream?, private val charset:Charset?):TerminalFactory {

private var initialTerminalSize:TerminalSize? = null
private var forceTextTerminal:Boolean = false
private var preferTerminalEmulator:Boolean = false
private var forceAWTOverSwing:Boolean = false
private var telnetPort:Int = 0
private var inputTimeout:Int = 0
private var title:String? = null
private var autoOpenTerminalFrame:Boolean = false
private val autoCloseTriggers:EnumSet<TerminalEmulatorAutoCloseTrigger?>?
private var colorConfiguration:TerminalEmulatorColorConfiguration? = null
private var deviceConfiguration:TerminalEmulatorDeviceConfiguration? = null
private var fontConfiguration:AWTTerminalFontConfiguration? = null
private var mouseCaptureMode:MouseCaptureMode? = null
private var unixTerminalCtrlCBehaviour:UnixTerminal.CtrlCBehaviour? = null

private// Most likely cause is that the java.desktop module is not available in the runtime image.
 val isAwtHeadless:Boolean
get() {
try
{
val cls = Class.forName("java.awt.GraphicsEnvironment")
val method = cls!!.getDeclaredMethod("isHeadless")
return method!!.invoke(null) as Boolean
}
catch (ignore:Exception) {
return true
}

}

/**
 * Creates a new DefaultTerminalFactory with all properties set to their defaults
 */
     constructor() : this(DEFAULT_OUTPUT_STREAM, DEFAULT_INPUT_STREAM, DEFAULT_CHARSET) {}

init{

this.forceTextTerminal = false
this.preferTerminalEmulator = false
this.forceAWTOverSwing = false

this.telnetPort = -1
this.inputTimeout = -1
this.autoOpenTerminalFrame = true
this.title = null
this.autoCloseTriggers = EnumSet.of(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE)
this.mouseCaptureMode = null
this.unixTerminalCtrlCBehaviour = UnixTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION

 //SwingTerminal will replace these null values for the default implementation if they are unchanged
        this.colorConfiguration = null
this.deviceConfiguration = null
this.fontConfiguration = null
}

@Override
@Throws(IOException::class)
 fun createTerminal():Terminal? {
 // 3 different reasons for tty-based terminal:
        //   "explicit preference", "no alternative",
        //       ("because we can" - unless "rather not")
        if ((forceTextTerminal || isAwtHeadless || 
(hasTerminal() && !preferTerminalEmulator)))
{
return createHeadlessTerminal()
}
else
{
 // while Lanterna's TerminalEmulator lacks mouse support:
            // if user wanted mouse AND set a telnetPort, and didn't
            //   explicitly ask for a graphical Terminal, then go telnet:
            if (!preferTerminalEmulator && mouseCaptureMode != null && telnetPort > 0)
{
return createTelnetTerminal()
}
else
{
return createTerminalEmulator()
}
}
}

/**
 * Instantiates a Terminal according to the factory implementation with the exception that
 * [DefaultTerminalFactory.preferTerminalEmulator] is always ignored. You may want to use this method when
 * using tools that rely on AOT compilation such as Graal native-image to ensure AWT/Swing code paths are not hit.
 * @return Terminal implementation
 * @throws IOException If there was an I/O error with the underlying input/output system
 */
    @Throws(IOException::class)
 fun createHeadlessTerminal():Terminal? {
 // if tty but have no tty, but do have a port, then go telnet:
        if (telnetPort > 0 && System.console() == null)
{
return createTelnetTerminal()
}
if (isOperatingSystemWindows)
{
return createWindowsTerminal()
}

return createUnixTerminal(outputStream, inputStream, charset)
}

/**
 * Creates a new terminal emulator window which will be either Swing-based or AWT-based depending on what is
 * available on the system
 * @return New terminal emulator exposed as a [Terminal] interface
 */
     fun createTerminalEmulator():Terminal? {
val terminal:Terminal?
if (!forceAWTOverSwing && hasSwing())
{
val stf = createSwingTerminal()
if (mouseCaptureMode != null)
{
stf!!.getSwingTerminal().setMouseCaptureMode(mouseCaptureMode)
}
terminal = stf
}
else
{
val atf = createAWTTerminal()
if (mouseCaptureMode != null)
{
atf!!.getAWTTerminal().setMouseCaptureMode(mouseCaptureMode)
}
terminal = atf
}

if (autoOpenTerminalFrame)
{
makeWindowVisible(terminal)
}
return terminal
}

 fun createAWTTerminal():AWTTerminalFrame? {
return AWTTerminalFrame(
title, 
initialTerminalSize, 
deviceConfiguration, 
fontConfiguration, 
colorConfiguration, 
autoCloseTriggers!!.toArray(arrayOfNulls<TerminalEmulatorAutoCloseTrigger?>(0)))
}

 fun createSwingTerminal():SwingTerminalFrame? {
return SwingTerminalFrame(
title, 
initialTerminalSize, 
deviceConfiguration, 
if (fontConfiguration is SwingTerminalFontConfiguration) fontConfiguration as SwingTerminalFontConfiguration? else null, 
colorConfiguration, 
autoCloseTriggers!!.toArray(arrayOfNulls<TerminalEmulatorAutoCloseTrigger?>(0)))
}

/**
 * Creates a new TelnetTerminal
 * 
 * Note: a telnetPort should have been set with setTelnetPort(),
 * otherwise creation of TelnetTerminal will most likely fail.
 * 
 * @return New terminal emulator exposed as a [Terminal] interface
 */
     fun createTelnetTerminal():TelnetTerminal? {
try
{
System.err.print("Waiting for incoming telnet connection on port " + telnetPort + " ... ")
System.err.flush()

val tts = TelnetTerminalServer(telnetPort)
val rawTerminal = tts.acceptConnection()
tts.close() // Just for single-shot: free up the port!

System.err.println("Ok, got it!")

if (mouseCaptureMode != null)
{
rawTerminal!!.setMouseCaptureMode(mouseCaptureMode)
}
if (inputTimeout >= 0)
{
rawTerminal!!.getInputDecoder().setTimeoutUnits(inputTimeout)
}
return rawTerminal
}
catch (ioe:IOException) {
throw RuntimeException(ioe)
}

}

private fun hasSwing():Boolean {
try
{
Class.forName("javax.swing.JComponent")
return true
}
catch (ignore:Exception) {
return false
}

}

private fun makeWindowVisible(terminal:Terminal?) {
try
{
val cls = Class.forName("java.awt.Window")
val method = cls!!.getDeclaredMethod("setVisible", Boolean::class.javaPrimitiveType)
method!!.invoke(terminal, true)
}
catch (ex:Exception) {
throw RuntimeException("Failed to make terminal emulator window visible.", ex)
}

}

/**
 * Sets a hint to the TerminalFactory of what size to use when creating the terminal. Most terminals are not created
 * on request but for example the SwingTerminal and SwingTerminalFrame are and this value will be passed down on
 * creation.
 * @param initialTerminalSize Size (in rows and columns) of the newly created terminal
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setInitialTerminalSize(initialTerminalSize:TerminalSize?):DefaultTerminalFactory {
this.initialTerminalSize = initialTerminalSize
return this
}

/**
 * Controls whether a text-based Terminal shall be created even if the system
 * supports a graphical environment
 * @param forceTextTerminal If true, will always create a text-based Terminal
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setForceTextTerminal(forceTextTerminal:Boolean):DefaultTerminalFactory {
this.forceTextTerminal = forceTextTerminal
return this
}

/**
 * Controls whether a Swing or AWT TerminalFrame shall be preferred if the system
 * has both a Console and a graphical environment
 * @param preferTerminalEmulator If true, will prefer creating a graphical terminal emulator
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setPreferTerminalEmulator(preferTerminalEmulator:Boolean):DefaultTerminalFactory {
this.preferTerminalEmulator = preferTerminalEmulator
return this
}

/**
 * Sets the default CTRL-C behavior to use for all [UnixTerminal] objects created by this factory. You can
 * use this to tell Lanterna to trap CTRL-C instead of exiting the application. Non-UNIX terminals are not affected
 * by this.
 * @param unixTerminalCtrlCBehaviour CTRL-C behavior to use for [UnixTerminal]:s
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setUnixTerminalCtrlCBehaviour(unixTerminalCtrlCBehaviour:UnixTerminal.CtrlCBehaviour?):DefaultTerminalFactory {
this.unixTerminalCtrlCBehaviour = unixTerminalCtrlCBehaviour
return this
}

/**
 * Primarily for debugging applications with mouse interactions:
 * If no Console is available (e.g. from within an IDE), then fall
 * back to TelnetTerminal on specified port.
 * 
 * If both a non-null mouseCapture mode and a positive telnetPort
 * are specified, then as long as Swing/AWT Terminal emulators do
 * not support MouseCapturing, a TelnetTerminal will be preferred
 * over the graphical Emulators.
 * 
 * @param telnetPort the TCP/IP port on which to eventually wait for a connection.
 * A value less or equal 0 disables creation of a TelnetTerminal.
 * Note, that ports less than 1024 typically require system
 * privileges to listen on.
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTelnetPort(telnetPort:Int):DefaultTerminalFactory {
this.telnetPort = telnetPort
return this
}

/**
 * Only for StreamBasedTerminals: After seeing e.g. an Escape (but nothing
 * else yet), wait up to the specified number of time units for more
 * bytes to make up a complete sequence. This may be necessary on
 * slow channels, or if some client terminal sends each byte of a
 * sequence in its own TCP packet.
 * 
 * @param inputTimeout how long to wait for possible completions of sequences.
 * units are of a 1/4 second, so e.g. 12 would wait up to 3 seconds.
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setInputTimeout(inputTimeout:Int):DefaultTerminalFactory {
this.inputTimeout = inputTimeout
return this
}

/**
 * Normally when a graphical terminal emulator is created by the factory, it will create a
 * [SwingTerminalFrame] unless Swing is not present in the system. Setting this property to `true` will
 * make it create an [AWTTerminalFrame] even if Swing is present
 * @param forceAWTOverSwing If `true`, will always create an [AWTTerminalFrame] over a
 * [SwingTerminalFrame] if asked to create a graphical terminal emulator
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setForceAWTOverSwing(forceAWTOverSwing:Boolean):DefaultTerminalFactory {
this.forceAWTOverSwing = forceAWTOverSwing
return this
}

/**
 * Controls whether a SwingTerminalFrame shall be automatically shown (.setVisible(true)) immediately after
 * creation. If `false`, you will manually need to call `.setVisible(true)` on the JFrame to actually
 * see the terminal window. Default for this value is `true`.
 * @param autoOpenTerminalFrame Automatically open SwingTerminalFrame after creation
 * @return Itself
 */
     fun setAutoOpenTerminalEmulatorWindow(autoOpenTerminalFrame:Boolean):DefaultTerminalFactory {
this.autoOpenTerminalFrame = autoOpenTerminalFrame
return this
}

/**
 * Sets the title to use on created SwingTerminalFrames created by this factory
 * @param title Title to use on created SwingTerminalFrames created by this factory
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTerminalEmulatorTitle(title:String?):DefaultTerminalFactory {
this.title = title
return this
}

/**
 * Sets the auto-close trigger to use on created SwingTerminalFrames created by this factory. This will reset any
 * previous triggers. If called with `null`, all triggers are cleared.
 * @param autoCloseTrigger Auto-close trigger to use on created SwingTerminalFrames created by this factory, or `null` to clear all existing triggers
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTerminalEmulatorFrameAutoCloseTrigger(autoCloseTrigger:TerminalEmulatorAutoCloseTrigger?):DefaultTerminalFactory {
this.autoCloseTriggers!!.clear()
if (autoCloseTrigger != null)
{
this.autoCloseTriggers!!.add(autoCloseTrigger)
}
return this
}

/**
 * Adds an auto-close trigger to use on created SwingTerminalFrames created by this factory
 * @param autoCloseTrigger Auto-close trigger to add to the created SwingTerminalFrames created by this factory
 * @return Reference to itself, so multiple calls can be chained
 */
     fun addTerminalEmulatorFrameAutoCloseTrigger(autoCloseTrigger:TerminalEmulatorAutoCloseTrigger?):DefaultTerminalFactory {
if (autoCloseTrigger != null)
{
this.autoCloseTriggers!!.add(autoCloseTrigger)
}
return this
}

/**
 * Sets the color configuration to use on created SwingTerminalFrames created by this factory
 * @param colorConfiguration Color configuration to use on created SwingTerminalFrames created by this factory
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTerminalEmulatorColorConfiguration(colorConfiguration:TerminalEmulatorColorConfiguration?):DefaultTerminalFactory {
this.colorConfiguration = colorConfiguration
return this
}

/**
 * Sets the device configuration to use on created SwingTerminalFrames created by this factory
 * @param deviceConfiguration Device configuration to use on created SwingTerminalFrames created by this factory
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTerminalEmulatorDeviceConfiguration(deviceConfiguration:TerminalEmulatorDeviceConfiguration?):DefaultTerminalFactory {
this.deviceConfiguration = deviceConfiguration
return this
}

/**
 * Sets the font configuration to use on created SwingTerminalFrames created by this factory
 * @param fontConfiguration Font configuration to use on created SwingTerminalFrames created by this factory
 * @return Reference to itself, so multiple .set-calls can be chained
 */
     fun setTerminalEmulatorFontConfiguration(fontConfiguration:AWTTerminalFontConfiguration?):DefaultTerminalFactory {
this.fontConfiguration = fontConfiguration
return this
}

/**
 * Sets the mouse capture mode the terminal should use. Please note that this is an extension which isn't widely
 * supported!
 * 
 * If both a non-null mouseCapture mode and a positive telnetPort
 * are specified, then as long as Swing/AWT Terminal emulators do
 * not support MouseCapturing, a TelnetTerminal will be preferred
 * over the graphical Emulators.
 * 
 * @param mouseCaptureMode Capture mode for mouse interactions
 * @return Itself
 */
     fun setMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?):DefaultTerminalFactory {
this.mouseCaptureMode = mouseCaptureMode
return this
}

/**
 * Create a [Terminal] and immediately wrap it up in a [TerminalScreen]
 * @return New [TerminalScreen] created with a terminal from [.createTerminal]
 * @throws IOException In case there was an I/O error
 */
    @Throws(IOException::class)
 fun createScreen():TerminalScreen? {
return TerminalScreen(createTerminal())
}

@Throws(IOException::class)
private fun createWindowsTerminal():Terminal? {
try
{
val nativeImplementation = Class.forName("com.googlecode.lanterna.terminal.win32.WindowsTerminal")
val constructor = nativeImplementation!!.getConstructor(InputStream::class.java, OutputStream::class.java, Charset::class.java, UnixLikeTTYTerminal.CtrlCBehaviour::class.java)
return constructor!!.newInstance(inputStream, outputStream, charset, UnixLikeTTYTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION) as Terminal
}
catch (ignore:Exception) {
try
{
return createCygwinTerminal(outputStream, inputStream, charset)
}
catch (e:IOException) {
throw IOException("To use Lanterna on Windows, either add JNA (and jna-platform) to the classpath or use javaw! (see https://github.com/mabe02/lanterna/issues/335)", e)
}

}
catch (ignore:NoClassDefFoundError) {
try
{
return createCygwinTerminal(outputStream, inputStream, charset)
}
catch (e:IOException) {
throw IOException("To use Lanterna on Windows, either add JNA (and jna-platform) to the classpath or use javaw! (see https://github.com/mabe02/lanterna/issues/335)", e)
}

}

}

@Throws(IOException::class)
private fun createCygwinTerminal(outputStream:OutputStream?, inputStream:InputStream?, charset:Charset?):Terminal {
val cygTerminal = CygwinTerminal(inputStream, outputStream, charset)
if (inputTimeout >= 0)
{
cygTerminal.getInputDecoder().setTimeoutUnits(inputTimeout)
}
return cygTerminal
}

@Throws(IOException::class)
private fun createUnixTerminal(outputStream:OutputStream?, inputStream:InputStream?, charset:Charset?):Terminal? {
val unixTerminal:UnixTerminal?
try
{
val nativeImplementation = Class.forName("com.googlecode.lanterna.terminal.NativeGNULinuxTerminal")
val constructor = nativeImplementation!!.getConstructor(InputStream::class.java, OutputStream::class.java, Charset::class.java, UnixLikeTTYTerminal.CtrlCBehaviour::class.java)
unixTerminal = constructor!!.newInstance(inputStream, outputStream, charset, unixTerminalCtrlCBehaviour) as UnixTerminal
}
catch (ignore:Exception) {
unixTerminal = UnixTerminal(inputStream, outputStream, charset, unixTerminalCtrlCBehaviour)
}

if (mouseCaptureMode != null)
{
unixTerminal!!.setMouseCaptureMode(mouseCaptureMode)
}
if (inputTimeout >= 0)
{
unixTerminal!!.getInputDecoder().setTimeoutUnits(inputTimeout)
}
return unixTerminal
}

companion object {
private val DEFAULT_OUTPUT_STREAM = System.out
private val DEFAULT_INPUT_STREAM = System.`in`
private val DEFAULT_CHARSET = Charset.defaultCharset()

/**
 * Detects whether the running platform is Windows* by looking at the
 * operating system name system property
 */
    private val isOperatingSystemWindows:Boolean
get() {
return System.getProperty("os.name", "").toLowerCase().startsWith("windows")
}

private fun hasTerminal():Boolean {
 // Prior to Java 22, the test was System.console() != null but now things have changed:
        // https://www.oracle.com/java/technologies/javase/22-relnote-issues.html#JDK-8308591
        // We need to check if the Console.isTerminal() method is available and rely on that if so
        val console = System.console()
return console != null && isTerminalCheckJDK22(console)
}

private fun isTerminalCheckJDK22(console:Console?):Boolean {
try
{
 // Don't want to require Java 22 so we need to check this by reflection
            val isTerminal = Console::class.java!!.getMethod("isTerminal")
return isTerminal!!.invoke(console) as Boolean
}
catch (e:NoSuchMethodException) {
return true  // This is normal and expected for pre-22 JVM
}
catch (e:InvocationTargetException) {
return true  // This is unexpected, but return true here too, just in case
}
catch (e:IllegalAccessException) {
return true
}

}
}
}
