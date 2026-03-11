package com.googlecode.lanterna.terminal.win32

import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.platform.win32.Wincon as JnaWincon
import com.sun.jna.ptr.IntByReference
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

class WindowsConsoleOutputStream(
    private val hConsoleOutput: HANDLE?,
    val decoderCharset: Charset,
) : OutputStream() {
    private val buffer = ByteArrayOutputStream()

    constructor(decoderCharset: Charset) : this(
        Wincon.INSTANCE.GetStdHandle(JnaWincon.STD_OUTPUT_HANDLE),
        decoderCharset,
    )

    fun getHandle(): HANDLE? = hConsoleOutput

    @Synchronized
    override fun write(b: Int) {
        buffer.write(b)
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.write(b, off, len)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun flush() {
        var characters = buffer.toString(decoderCharset.name())
        buffer.reset()

        val numberOfCharsWritten = IntByReference()
        while (characters.isNotEmpty()) {
            if (!Wincon.INSTANCE.WriteConsole(hConsoleOutput, characters, characters.length, numberOfCharsWritten, null)) {
                throw EOFException()
            }
            characters = characters.substring(numberOfCharsWritten.value)
        }
    }
}
