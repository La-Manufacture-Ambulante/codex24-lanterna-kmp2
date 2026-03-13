package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.POLLIN
import platform.posix.STDIN_FILENO
import platform.posix.fflush
import platform.posix.fputs
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.read
import platform.posix.stdout

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    actual fun readByte(): Int? = readByteFromFd(STDIN_FILENO)

    actual fun hasInput(timeoutMillis: Int): Boolean = pollFdReady(STDIN_FILENO, timeoutMillis)

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
    return STDIN_FILENO
}

internal fun resolveInputCandidatesForTest(
    stdinIsTty: Boolean,
    ttyFd: Int,
): IntArray {
    return intArrayOf(STDIN_FILENO)
}

internal fun resolveBlockingInputFdForTest(
    stdinIsTty: Boolean,
    ttyFd: Int,
): Int {
    return STDIN_FILENO
}

@OptIn(ExperimentalForeignApi::class)
private fun pollFdReady(
    fd: Int,
    timeoutMillis: Int,
): Boolean =
    memScoped {
        val descriptor = alloc<pollfd>()
        descriptor.fd = fd
        descriptor.events = POLLIN.convert()
        descriptor.revents = 0
        val pollResult = poll(descriptor.ptr, 1.convert(), timeoutMillis)
        pollResult > 0 && (descriptor.revents.toInt() and POLLIN) != 0
    }

@OptIn(ExperimentalForeignApi::class)
private fun readByteFromFd(fd: Int): Int? {
    val buffer = ByteArray(1)
    val bytesRead =
        buffer.usePinned { pinned ->
            read(fd, pinned.addressOf(0), 1.convert())
        }
    if (bytesRead <= 0) {
        return null
    }
    return buffer[0].toInt() and 0xFF
}
