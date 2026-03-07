package com.googlecode.lanterna.terminal.win32

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference

 class WindowsConsoleOutputStream( val handle:HANDLE?,  val decoderCharset:Charset?):OutputStream() {
private val buffer = ByteArrayOutputStream()

 constructor(decoder:Charset?) : this(Wincon.INSTANCE.GetStdHandle(Wincon.STD_OUTPUT_HANDLE), decoder) {}


@Override
@Synchronized  fun write(b:Int) {
buffer.write(b)
}

@Override
@Synchronized  fun write(b:ByteArray?, off:Int, len:Int) {
buffer.write(b, off, len)
}

@Override
@Synchronized @Throws(IOException::class)
 fun flush() {
var characters = buffer.toString(decoderCharset!!.name())
buffer.reset()

val lpNumberOfCharsWritten = IntByReference()
while (!characters!!.isEmpty())
{
if (!Wincon.INSTANCE.WriteConsole(handle, characters, characters!!.length(), lpNumberOfCharsWritten, null))
{
throw EOFException()
}
characters = characters!!.substring(lpNumberOfCharsWritten.getValue())
}
}

}
