package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.EOF
import platform.posix.POLLIN
import platform.posix.STDIN_FILENO
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.getchar
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.stdout

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    actual fun readByte(): Int? {
        val value = getchar()
        return if (value == EOF) null else value
    }

    actual fun hasInput(timeoutMillis: Int): Boolean =
        memScoped {
            val descriptor = alloc<pollfd>()
            descriptor.fd = STDIN_FILENO
            descriptor.events = POLLIN.convert()
            descriptor.revents = 0

            val pollResult = poll(descriptor.ptr, 1.convert(), timeoutMillis)
            pollResult > 0 && (descriptor.revents.toInt() and POLLIN) != 0
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
