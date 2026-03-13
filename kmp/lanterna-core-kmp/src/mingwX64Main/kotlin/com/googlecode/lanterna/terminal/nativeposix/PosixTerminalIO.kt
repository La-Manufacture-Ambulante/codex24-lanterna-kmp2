package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.stdout

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    actual fun readByte(): Int? {
        return null
    }

    actual fun hasInput(timeoutMillis: Int): Boolean {
        return false
    }

    actual fun write(value: String) {
        fputs(value, stdout)
    }

    actual fun writeByte(value: Int) {
        write(value.toChar().toString())
    }

    actual fun writeBytes(bytes: ByteArray) {
        write(bytes.decodeToString())
    }

    actual fun flush() {
        fflush(stdout)
    }
}
