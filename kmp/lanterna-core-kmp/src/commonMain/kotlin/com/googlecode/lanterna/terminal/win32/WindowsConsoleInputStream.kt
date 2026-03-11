package com.googlecode.lanterna.terminal.win32

import com.googlecode.lanterna.terminal.win32.WinDef.INPUT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.KEY_EVENT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.MOUSE_EVENT_RECORD
import com.googlecode.lanterna.terminal.win32.WinDef.WINDOW_BUFFER_SIZE_RECORD
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.platform.win32.Wincon as JnaWincon
import com.sun.jna.ptr.IntByReference
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.Arrays
import java.util.function.Consumer

class WindowsConsoleInputStream(
    private val hConsoleInput: HANDLE?,
    val encoderCharset: Charset,
) : InputStream() {
    private var buffer: ByteBuffer = ByteBuffer.allocate(0)
    private var keyEventHandler: Consumer<KEY_EVENT_RECORD>? = null
    private var mouseEventHandler: Consumer<MOUSE_EVENT_RECORD>? = null
    private var windowBufferSizeEventHandler: Consumer<WINDOW_BUFFER_SIZE_RECORD>? = null

    constructor(encoderCharset: Charset) : this(
        Wincon.INSTANCE.GetStdHandle(JnaWincon.STD_INPUT_HANDLE),
        encoderCharset,
    )

    fun getHandle(): HANDLE? = hConsoleInput

    @Throws(IOException::class)
    private fun readConsoleInput(): Array<INPUT_RECORD?> {
        val lpBuffer = arrayOfNulls<INPUT_RECORD>(64)
        val lpNumberOfEventsRead = IntByReference()
        if (Wincon.INSTANCE.ReadConsoleInput(hConsoleInput, lpBuffer, lpBuffer.size, lpNumberOfEventsRead)) {
            val n = lpNumberOfEventsRead.value
            return Arrays.copyOfRange(lpBuffer, 0, n)
        }
        throw EOFException()
    }

    private fun availableConsoleInput(): Int {
        val numberOfEvents = IntByReference()
        return if (Wincon.INSTANCE.GetNumberOfConsoleInputEvents(hConsoleInput, numberOfEvents)) {
            numberOfEvents.value
        } else {
            0
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(): Int {
        while (!buffer.hasRemaining()) {
            buffer = readKeyEvents(true)
        }
        return buffer.get().toInt() and 0xff
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(b: ByteArray, offset: Int, length: Int): Int {
        while (length > 0 && !buffer.hasRemaining()) {
            buffer = readKeyEvents(true)
        }
        val n = minOf(buffer.remaining(), length)
        buffer.get(b, offset, n)
        return n
    }

    @Synchronized
    @Throws(IOException::class)
    override fun available(): Int {
        if (buffer.hasRemaining()) {
            return buffer.remaining()
        }
        buffer = readKeyEvents(false)
        return buffer.remaining()
    }

    @Throws(IOException::class)
    private fun readKeyEvents(blocking: Boolean): ByteBuffer {
        val keyEvents = StringBuilder()
        if (blocking || availableConsoleInput() > 0) {
            for (inputRecord in readConsoleInput()) {
                if (inputRecord != null) {
                    filter(inputRecord, keyEvents)
                }
            }
        }
        return encoderCharset.encode(CharBuffer.wrap(keyEvents))
    }

    @Throws(IOException::class)
    private fun filter(input: INPUT_RECORD, keyEvents: Appendable) {
        when (input.EventType) {
            INPUT_RECORD.KEY_EVENT -> {
                if (input.Event.KeyEvent.uChar.code != 0 && input.Event.KeyEvent.bKeyDown) {
                    keyEvents.append(input.Event.KeyEvent.uChar)
                }
                keyEventHandler?.accept(input.Event.KeyEvent)
            }
            INPUT_RECORD.MOUSE_EVENT -> mouseEventHandler?.accept(input.Event.MouseEvent)
            INPUT_RECORD.WINDOW_BUFFER_SIZE_EVENT -> {
                windowBufferSizeEventHandler?.accept(input.Event.WindowBufferSizeEvent)
            }
        }
    }

    fun onKeyEvent(handler: Consumer<KEY_EVENT_RECORD>) {
        keyEventHandler = if (keyEventHandler == null) handler else keyEventHandler!!.andThen(handler)
    }

    fun onMouseEvent(handler: Consumer<MOUSE_EVENT_RECORD>) {
        mouseEventHandler = if (mouseEventHandler == null) handler else mouseEventHandler!!.andThen(handler)
    }

    fun onWindowBufferSizeEvent(handler: Consumer<WINDOW_BUFFER_SIZE_RECORD>) {
        windowBufferSizeEventHandler =
            if (windowBufferSizeEventHandler == null) handler else windowBufferSizeEventHandler!!.andThen(handler)
    }
}
