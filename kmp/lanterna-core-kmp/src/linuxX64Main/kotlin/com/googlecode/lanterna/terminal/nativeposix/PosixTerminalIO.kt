package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.EOF
import platform.posix.O_RDONLY
import platform.posix.POLLIN
import platform.posix.STDIN_FILENO
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.getchar
import platform.posix.isatty
import platform.posix.open
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.read
import platform.posix.stdout

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    private val ttyInputFd: Int by lazy {
        open("/dev/tty", O_RDONLY)
    }

    private fun activeInputFd(): Int {
        return resolveInputFdForTest(
            stdinIsTty = isatty(STDIN_FILENO) == 1,
            ttyFd = ttyInputFd,
        )
    }

    actual fun readByte(): Int? {
        val inputFd = activeInputFd()
        if (inputFd < 0) {
            val value = getchar()
            return if (value == EOF) null else value
        }

        val buffer = ByteArray(1)
        val bytesRead =
            buffer.usePinned { pinned ->
                read(inputFd, pinned.addressOf(0), 1.convert())
            }
        if (bytesRead <= 0) {
            return null
        }
        return buffer[0].toInt() and 0xFF
    }

    actual fun hasInput(timeoutMillis: Int): Boolean =
        memScoped {
            val descriptor = alloc<pollfd>()
            descriptor.fd = activeInputFd()
            descriptor.events = POLLIN.convert()
            descriptor.revents = 0

            if (descriptor.fd < 0) {
                return@memScoped false
            }

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

internal fun resolveInputFdForTest(
    stdinIsTty: Boolean,
    ttyFd: Int,
): Int {
    if (stdinIsTty) {
        return STDIN_FILENO
    }
    return ttyFd
}
