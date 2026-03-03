package com.googlecode.lanterna.terminal.win32

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.Union

interface WinDef : com.sun.jna.platform.win32.WinDef {

    @FieldOrder(value = ["X", "Y"])
    open class COORD : Structure() {
        @JvmField
        var X: Short = 0

        @JvmField
        var Y: Short = 0

        override fun toString(): String {
            return String.format("COORD(%s,%s)", X, Y)
        }
    }

    @FieldOrder(value = ["Left", "Top", "Right", "Bottom"])
    open class SMALL_RECT : Structure() {
        @JvmField
        var Left: Short = 0

        @JvmField
        var Top: Short = 0

        @JvmField
        var Right: Short = 0

        @JvmField
        var Bottom: Short = 0

        override fun toString(): String {
            return String.format("SMALL_RECT(%s,%s)(%s,%s)", Left, Top, Right, Bottom)
        }
    }

    @FieldOrder(value = ["dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize"])
    open class CONSOLE_SCREEN_BUFFER_INFO : Structure() {
        @JvmField
        var dwSize: COORD? = null

        @JvmField
        var dwCursorPosition: COORD? = null

        @JvmField
        var wAttributes: Short = 0

        @JvmField
        var srWindow: SMALL_RECT? = null

        @JvmField
        var dwMaximumWindowSize: COORD? = null

        override fun toString(): String {
            return String.format(
                "CONSOLE_SCREEN_BUFFER_INFO(%s,%s,%s,%s,%s)",
                dwSize,
                dwCursorPosition,
                wAttributes,
                srWindow,
                dwMaximumWindowSize
            )
        }
    }

    @FieldOrder(value = ["EventType", "Event"])
    open class INPUT_RECORD : Structure() {
        companion object {
            @JvmField
            val KEY_EVENT: Short = 0x01

            @JvmField
            val MOUSE_EVENT: Short = 0x02

            @JvmField
            val WINDOW_BUFFER_SIZE_EVENT: Short = 0x04
        }

        @JvmField
        var EventType: Short = 0

        @JvmField
        var Event: Event? = null

        open class Event : Union() {
            @JvmField
            var KeyEvent: KEY_EVENT_RECORD? = null

            @JvmField
            var MouseEvent: MOUSE_EVENT_RECORD? = null

            @JvmField
            var WindowBufferSizeEvent: WINDOW_BUFFER_SIZE_RECORD? = null
        }

        override fun read() {
            super.read()
            when (EventType) {
                KEY_EVENT -> {
                    val event = Event ?: throw NullPointerException()
                    event.setType("KeyEvent")
                }

                MOUSE_EVENT -> {
                    val event = Event ?: throw NullPointerException()
                    event.setType("MouseEvent")
                }

                WINDOW_BUFFER_SIZE_EVENT -> {
                    val event = Event ?: throw NullPointerException()
                    event.setType("WindowBufferSizeEvent")
                }
            }
            val event = Event ?: throw NullPointerException()
            event.read()
        }

        override fun toString(): String {
            return String.format("INPUT_RECORD(%s)", EventType)
        }
    }

    @FieldOrder(value = ["bKeyDown", "wRepeatCount", "wVirtualKeyCode", "wVirtualScanCode", "uChar", "dwControlKeyState"])
    open class KEY_EVENT_RECORD : Structure() {
        @JvmField
        var bKeyDown: Boolean = false

        @JvmField
        var wRepeatCount: Short = 0

        @JvmField
        var wVirtualKeyCode: Short = 0

        @JvmField
        var wVirtualScanCode: Short = 0

        @JvmField
        var uChar: Char = '\u0000'

        @JvmField
        var dwControlKeyState: Int = 0

        override fun toString(): String {
            return String.format(
                "KEY_EVENT_RECORD(%s,%s,%s,%s,%s,%s)",
                bKeyDown,
                wRepeatCount,
                wVirtualKeyCode,
                wVirtualScanCode,
                uChar,
                dwControlKeyState
            )
        }
    }

    @FieldOrder(value = ["dwMousePosition", "dwButtonState", "dwControlKeyState", "dwEventFlags"])
    open class MOUSE_EVENT_RECORD : Structure() {
        @JvmField
        var dwMousePosition: COORD? = null

        @JvmField
        var dwButtonState: Int = 0

        @JvmField
        var dwControlKeyState: Int = 0

        @JvmField
        var dwEventFlags: Int = 0

        override fun toString(): String {
            return String.format(
                "MOUSE_EVENT_RECORD(%s,%s,%s,%s)",
                dwMousePosition,
                dwButtonState,
                dwControlKeyState,
                dwEventFlags
            )
        }
    }

    @FieldOrder(value = ["dwSize"])
    open class WINDOW_BUFFER_SIZE_RECORD : Structure() {
        @JvmField
        var dwSize: COORD? = null

        override fun toString(): String {
            return String.format("WINDOW_BUFFER_SIZE_RECORD(%s)", dwSize)
        }
    }
}
