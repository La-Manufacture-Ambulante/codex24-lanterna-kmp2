package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.EOF
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.getchar
import platform.posix.stdout

@OptIn(ExperimentalForeignApi::class)
object PosixTerminalIO {
    fun readByte(): Int? {
        val value = getchar()
        return if (value == EOF) null else value
    }

    fun write(value: String) {
        fputs(value, stdout)
    }

    fun flush() {
        fflush(stdout)
    }
}
