package com.googlecode.lanterna.terminal.win32

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.Arrays
import java.util.function.Consumer

import com.googlecode.lanterna.terminal.win32.WinDef.INPUT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.KEY_EVENT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.MOUSE_EVENT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.WINDOW_BUFFER_SIZE_RECORD
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference

 class WindowsConsoleInputStream( val handle:HANDLE?,  val encoderCharset:Charset?):InputStream() {
private var buffer = ByteBuffer.allocate(0)

private var keyEventHandler:Consumer<KEY_EVENT_RECORD?>? = null
private var mouseEventHandler:Consumer<MOUSE_EVENT_RECORD?>? = null
private var windowBufferSizeEventHandler:Consumer<WINDOW_BUFFER_SIZE_RECORD?>? = null

 constructor(encoderCharset:Charset?) : this(Wincon.INSTANCE.GetStdHandle(Wincon.STD_INPUT_HANDLE), encoderCharset) {}

@Throws(IOException::class)
private fun readConsoleInput():Array<INPUT_RECORD?>? {
val lpBuffer = arrayOfNulls<INPUT_RECORD?>(64)
val lpNumberOfEventsRead = IntByReference()
if (Wincon.INSTANCE.ReadConsoleInput(handle, lpBuffer, lpBuffer.size, lpNumberOfEventsRead))
{
val n = lpNumberOfEventsRead.getValue()
return Arrays.copyOfRange(lpBuffer, 0, n)
}
throw EOFException()
}

private fun availableConsoleInput():Int {
val lpcNumberOfEvents = IntByReference()
if (Wincon.INSTANCE.GetNumberOfConsoleInputEvents(handle, lpcNumberOfEvents))
{
return lpcNumberOfEvents.getValue()
}
return 0
}

@Override
@Synchronized @Throws(IOException::class)
 fun read():Int {
while (!buffer!!.hasRemaining())
{
buffer = readKeyEvents(true)
}

return buffer!!.get()
}

@Override
@Synchronized @Throws(IOException::class)
 fun read(b:ByteArray?, offset:Int, length:Int):Int {
while (length > 0 && !buffer!!.hasRemaining())
{
buffer = readKeyEvents(true)
}

val n = Math.min(buffer!!.remaining(), length)
buffer!!.get(b, offset, n)
return n
}

@Override
@Synchronized @Throws(IOException::class)
 fun available():Int {
if (buffer!!.hasRemaining())
{
return buffer!!.remaining()
}

buffer = readKeyEvents(false)
return buffer!!.remaining()
}

@Throws(IOException::class)
private fun readKeyEvents(blocking:Boolean):ByteBuffer? {
val keyEvents = StringBuilder()

if (blocking || availableConsoleInput() > 0)
{
for (i in readConsoleInput()!!)
{
filter(i!!, keyEvents)
}
}

return encoderCharset!!.encode(CharBuffer.wrap(keyEvents))
}

@Throws(IOException::class)
private fun filter(input:INPUT_RECORD, keyEvents:Appendable?) {
when (input.EventType) {
INPUT_RECORD.KEY_EVENT -> {
if (input.Event.KeyEvent.uChar !== 0 && input.Event.KeyEvent.bKeyDown)
{
keyEvents!!.append(input.Event.KeyEvent.uChar)
}
if (keyEventHandler != null)
{
keyEventHandler!!.accept(input.Event.KeyEvent)
}
}
INPUT_RECORD.MOUSE_EVENT -> if (mouseEventHandler != null)
{
mouseEventHandler!!.accept(input.Event.MouseEvent)
}
INPUT_RECORD.WINDOW_BUFFER_SIZE_EVENT -> if (windowBufferSizeEventHandler != null)
{
windowBufferSizeEventHandler!!.accept(input.Event.WindowBufferSizeEvent)
}
}
}

 fun onKeyEvent(handler:Consumer<KEY_EVENT_RECORD?>?) {
if (keyEventHandler == null)
{
keyEventHandler = handler
}
else
{
keyEventHandler = keyEventHandler!!.andThen(handler)
}
}

 fun onMouseEvent(handler:Consumer<MOUSE_EVENT_RECORD?>?) {
if (mouseEventHandler == null)
{
mouseEventHandler = handler
}
else
{
mouseEventHandler = mouseEventHandler!!.andThen(handler)
}
}

 fun onWindowBufferSizeEvent(handler:Consumer<WINDOW_BUFFER_SIZE_RECORD?>?) {
if (windowBufferSizeEventHandler == null)
{
windowBufferSizeEventHandler = handler
}
else
{
windowBufferSizeEventHandler = windowBufferSizeEventHandler!!.andThen(handler)
}
}

}
