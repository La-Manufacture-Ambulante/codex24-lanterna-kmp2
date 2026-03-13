package com.googlecode.lanterna.terminal.nativeposix

expect object PosixTerminalIO {
    fun readByte(): Int?

    fun hasInput(timeoutMillis: Int = 0): Boolean

    fun write(value: String)

    fun writeByte(value: Int)

    fun writeBytes(bytes: ByteArray)

    fun flush()
}
