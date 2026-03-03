package com.googlecode.lanterna.terminal.win32

import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

open class WindowsConsoleOutputStream : OutputStream {
    private val hConsoleOutput: HANDLE?
    private val decoderCharset: Charset?
    private val buffer = ByteArrayOutputStream()

    constructor(decoder: Charset?) : this(
        Wincon.INSTANCE.GetStdHandle(Wincon.STD_OUTPUT_HANDLE),
        decoder
    )

    constructor(hConsoleOutput: HANDLE?, decoderCharset: Charset?) {
        this.hConsoleOutput = hConsoleOutput
        this.decoderCharset = decoderCharset
    }

    open fun getHandle(): HANDLE? {
        return hConsoleOutput
    }

    open fun getDecoderCharset(): Charset? {
        return decoderCharset
    }

    @Synchronized
    open override fun write(b: Int) {
        buffer.write(b)
    }

    @Synchronized
    open override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.write(b, off, len)
    }

    @Synchronized
    @Throws(IOException::class)
    open override fun flush() {
        val charsetName = decoderCharset?.name() ?: throw NullPointerException()
        var characters = buffer.toString(charsetName)
        buffer.reset()

        val lpNumberOfCharsWritten = IntByReference()
        while (characters.isNotEmpty()) {
            if (!Wincon.INSTANCE.WriteConsole(
                    hConsoleOutput,
                    characters,
                    characters.length,
                    lpNumberOfCharsWritten,
                    null
                )
            ) {
                throw EOFException()
            }
            characters = characters.substring(lpNumberOfCharsWritten.value)
        }
    }
}
