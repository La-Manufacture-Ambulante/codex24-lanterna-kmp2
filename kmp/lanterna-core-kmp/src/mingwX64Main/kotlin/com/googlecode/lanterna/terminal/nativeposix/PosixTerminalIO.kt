package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.EOF
import platform.posix.fflush
import platform.posix.fgetc
import platform.posix.fputs
import platform.posix.stdin
import platform.posix.stdout
import platform.windows.FlushFileBuffers
import platform.windows.GetStdHandle
import platform.windows.ReadFile
import platform.windows.STD_INPUT_HANDLE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.WAIT_OBJECT_0
import platform.windows.WAIT_TIMEOUT
import platform.windows.WaitForSingleObject
import platform.windows.WriteFile

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    actual fun readByte(): Int? {
        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt())
        if (inputHandle != null) {
            memScoped {
                val bytesRead = alloc<UIntVarOf<UInt>>()
                val readBuffer = ByteArray(1)
                val readOk = readBuffer.usePinned { pinned ->
                    ReadFile(inputHandle, pinned.addressOf(0), 1u, bytesRead.ptr, null) != 0
                }
                if (readOk && bytesRead.value > 0u) {
                    return readBuffer[0].toInt() and 0xFF
                }
            }
        }

        val fallbackValue = fgetc(stdin)
        return if (fallbackValue == EOF) null else fallbackValue
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
        writeBytes(value.encodeToByteArray())
    }

    actual fun writeByte(value: Int) {
        write(value.toChar().toString())
    }

    actual fun writeBytes(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }

        val outputHandle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
        if (outputHandle != null) {
            val fullyWritten =
                bytes.usePinned { pinned ->
                    var offset = 0
                    while (offset < bytes.size) {
                        val chunkWritten =
                            memScoped {
                                val bytesWritten = alloc<UIntVarOf<UInt>>()
                                val remaining = (bytes.size - offset).toUInt()
                                val writeOk =
                                    WriteFile(
                                        outputHandle,
                                        pinned.addressOf(offset),
                                        remaining,
                                        bytesWritten.ptr,
                                        null,
                                    ) != 0
                                if (!writeOk) {
                                    return@memScoped -1
                                }
                                bytesWritten.value.toInt()
                            }
                        if (chunkWritten <= 0) {
                            return@usePinned false
                        }
                        offset += chunkWritten
                    }
                    true
                }
            if (fullyWritten) {
                return
            }
        }

        fputs(bytes.decodeToString(), stdout)
    }

    actual fun flush() {
        val outputHandle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
        if (outputHandle != null && FlushFileBuffers(outputHandle) != 0) {
            return
        }
        fflush(stdout)
    }
}
