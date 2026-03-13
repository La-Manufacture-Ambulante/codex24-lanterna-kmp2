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
import platform.windows.GetNumberOfConsoleInputEvents
import platform.windows.GetStdHandle
import platform.windows.INPUT_RECORD
import platform.windows.KEY_EVENT
import platform.windows.LEFT_ALT_PRESSED
import platform.windows.LEFT_CTRL_PRESSED
import platform.windows.ReadFile
import platform.windows.ReadConsoleInputW
import platform.windows.RIGHT_ALT_PRESSED
import platform.windows.RIGHT_CTRL_PRESSED
import platform.windows.STD_INPUT_HANDLE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.SHIFT_PRESSED
import platform.windows.WAIT_OBJECT_0
import platform.windows.WAIT_TIMEOUT
import platform.windows.WaitForSingleObject
import platform.windows.WriteFile

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalIO {
    private val inputByteQueue = ArrayDeque<Byte>()

    actual fun readByte(): Int? {
        val buffered = inputByteQueue.removeFirstOrNull()
        if (buffered != null) {
            return buffered.toInt() and 0xFF
        }

        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt())
        if (inputHandle != null) {
            val eventBytes = readConsoleInputEventBytes(inputHandle)
            if (eventBytes != null) {
                eventBytes.forEach(inputByteQueue::addLast)
                val fromEvent = inputByteQueue.removeFirstOrNull()
                if (fromEvent != null) {
                    return fromEvent.toInt() and 0xFF
                }
            }

            memScoped {
                val bytesRead = alloc<UIntVarOf<UInt>>()
                val readBuffer = ByteArray(1)
                val readOk =
                    readBuffer.usePinned { pinned ->
                        ReadFile(inputHandle, pinned.addressOf(0), 1u, bytesRead.ptr, null) != 0
                    }
                if (readOk && bytesRead.value > 0u) {
                    return readBuffer[0].toInt() and 0xFF
                }
                return null
            }
        }

        val fallbackValue = fgetc(stdin)
        return if (fallbackValue == EOF) null else fallbackValue
    }

    actual fun hasInput(timeoutMillis: Int): Boolean {
        if (inputByteQueue.isNotEmpty()) {
            return true
        }
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

    private fun readConsoleInputEventBytes(inputHandle: platform.windows.HANDLE?): ByteArray? =
        memScoped {
            if (inputHandle == null) {
                return null
            }
            val pendingEvents = alloc<UIntVarOf<UInt>>()
            if (GetNumberOfConsoleInputEvents(inputHandle, pendingEvents.ptr) == 0 || pendingEvents.value == 0u) {
                return null
            }

            repeat(pendingEvents.value.toInt()) {
                val inputRecord = alloc<INPUT_RECORD>()
                val eventsRead = alloc<UIntVarOf<UInt>>()
                if (ReadConsoleInputW(inputHandle, inputRecord.ptr, 1u, eventsRead.ptr) == 0 || eventsRead.value == 0u) {
                    return null
                }
                if (inputRecord.EventType != KEY_EVENT.toUShort()) {
                    return@repeat
                }
                val keyEvent = inputRecord.Event.KeyEvent
                if (keyEvent.bKeyDown.toInt() == 0) {
                    return@repeat
                }
                val controlKeyState = keyEvent.dwControlKeyState.toInt()
                val codePoint = keyEvent.uChar.UnicodeChar.toInt()
                if (codePoint != 0) {
                    val text = codePoint.toChar().toString()
                    if ((controlKeyState and (LEFT_ALT_PRESSED or RIGHT_ALT_PRESSED)) != 0) {
                        return "\u001B$text".encodeToByteArray()
                    }
                    return text.encodeToByteArray()
                }
                val specialBytes = mapVirtualKeyToAnsi(keyEvent.wVirtualKeyCode.toInt(), controlKeyState)
                if (specialBytes != null) {
                    return specialBytes
                }
            }
            return null
        }

    private fun mapVirtualKeyToAnsi(
        virtualKeyCode: Int,
        controlKeyState: Int,
    ): ByteArray? =
        when (virtualKeyCode) {
            0x08 -> byteArrayOf(0x7F) // Backspace
            0x09 -> byteArrayOf('\t'.code.toByte()) // Tab
            0x0D -> byteArrayOf('\r'.code.toByte()) // Enter
            0x1B -> byteArrayOf(0x1B) // Escape
            0x21 -> modifiedTilde(5, controlKeyState) // PageUp
            0x22 -> modifiedTilde(6, controlKeyState) // PageDown
            0x23 -> modifiedCursor("F", controlKeyState) // End
            0x24 -> modifiedCursor("H", controlKeyState) // Home
            0x25 -> modifiedCursor("D", controlKeyState) // Left
            0x26 -> modifiedCursor("A", controlKeyState) // Up
            0x27 -> modifiedCursor("C", controlKeyState) // Right
            0x28 -> modifiedCursor("B", controlKeyState) // Down
            0x2D -> modifiedTilde(2, controlKeyState) // Insert
            0x2E -> modifiedTilde(3, controlKeyState) // Delete
            else -> null
        }

    private fun modifiedCursor(
        suffix: String,
        controlKeyState: Int,
    ): ByteArray {
        val modifier = csiModifier(controlKeyState)
        return if (modifier == 1) {
            "\u001B[$suffix".encodeToByteArray()
        } else {
            "\u001B[1;${modifier}$suffix".encodeToByteArray()
        }
    }

    private fun modifiedTilde(
        code: Int,
        controlKeyState: Int,
    ): ByteArray {
        val modifier = csiModifier(controlKeyState)
        return if (modifier == 1) {
            "\u001B[${code}~".encodeToByteArray()
        } else {
            "\u001B[${code};${modifier}~".encodeToByteArray()
        }
    }

    private fun csiModifier(controlKeyState: Int): Int {
        var value = 1
        if ((controlKeyState and SHIFT_PRESSED) != 0) {
            value += 1
        }
        if ((controlKeyState and (LEFT_ALT_PRESSED or RIGHT_ALT_PRESSED)) != 0) {
            value += 2
        }
        if ((controlKeyState and (LEFT_CTRL_PRESSED or RIGHT_CTRL_PRESSED)) != 0) {
            value += 4
        }
        return value
    }
}
