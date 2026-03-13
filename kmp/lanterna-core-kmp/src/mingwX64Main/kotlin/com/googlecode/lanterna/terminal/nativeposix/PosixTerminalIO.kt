package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.EOF
import platform.posix.fflush
import platform.posix.fgetc
import platform.posix.fputs
import platform.posix.stdin
import platform.posix.stdout
import platform.windows.GetStdHandle
import platform.windows.STD_INPUT_HANDLE
import platform.windows.WAIT_OBJECT_0
import platform.windows.WAIT_TIMEOUT
import platform.windows.WaitForSingleObject

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    actual fun readByte(): Int? {
        val value = fgetc(stdin)
        return if (value == EOF) null else value
    }

    actual fun hasInput(timeoutMillis: Int): Boolean {
        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt()) ?: return false
        val waitResult = WaitForSingleObject(inputHandle, timeoutMillis.coerceAtLeast(0).toUInt())
        return when (waitResult) {
            WAIT_OBJECT_0.toUInt() -> true
            WAIT_TIMEOUT.toUInt() -> false
            else -> false
        }
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
