package com.googlecode.lanterna.terminal.win32

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.Union

interface WinDef : com.sun.jna.platform.win32.WinDef {
    /**
     * COORD structure
     */
    @FieldOrder("X", "Y")
    class COORD : Structure() {
        @JvmField
        var X: Short = 0

        @JvmField
        var Y: Short = 0

        override fun toString(): String = String.format("COORD(%s,%s)", X, Y)
    }

    /**
     * SMALL_RECT structure
     */
    @FieldOrder("Left", "Top", "Right", "Bottom")
    class SMALL_RECT : Structure() {
        @JvmField
        var Left: Short = 0

        @JvmField
        var Top: Short = 0

        @JvmField
        var Right: Short = 0

        @JvmField
        var Bottom: Short = 0

        override fun toString(): String = String.format("SMALL_RECT(%s,%s)(%s,%s)", Left, Top, Right, Bottom)
    }

    /**
     * CONSOLE_SCREEN_BUFFER_INFO structure
     */
    @FieldOrder("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
    class CONSOLE_SCREEN_BUFFER_INFO : Structure() {
        @JvmField
        var dwSize: COORD = COORD()

        @JvmField
        var dwCursorPosition: COORD = COORD()

        @JvmField
        var wAttributes: Short = 0

        @JvmField
        var srWindow: SMALL_RECT = SMALL_RECT()

        @JvmField
        var dwMaximumWindowSize: COORD = COORD()

        override fun toString(): String {
            return String.format(
                "CONSOLE_SCREEN_BUFFER_INFO(%s,%s,%s,%s,%s)",
                dwSize,
                dwCursorPosition,
                wAttributes,
                srWindow,
                dwMaximumWindowSize,
            )
        }
    }

    @FieldOrder("EventType", "Event")
    class INPUT_RECORD : Structure() {
        @JvmField
        var EventType: Short = 0

        @JvmField
        var Event: EventUnion = EventUnion()

        class EventUnion : Union() {
            @JvmField
            var KeyEvent: KEY_EVENT_RECORD = KEY_EVENT_RECORD()

            @JvmField
            var MouseEvent: MOUSE_EVENT_RECORD = MOUSE_EVENT_RECORD()

            @JvmField
            var WindowBufferSizeEvent: WINDOW_BUFFER_SIZE_RECORD = WINDOW_BUFFER_SIZE_RECORD()
        }

        override fun read() {
            super.read()
            when (EventType) {
                KEY_EVENT -> Event.setType("KeyEvent")
                MOUSE_EVENT -> Event.setType("MouseEvent")
                WINDOW_BUFFER_SIZE_EVENT -> Event.setType("WindowBufferSizeEvent")
            }
            Event.read()
        }

        override fun toString(): String = String.format("INPUT_RECORD(%s)", EventType)

        companion object {
            const val KEY_EVENT: Short = 0x01
            const val MOUSE_EVENT: Short = 0x02
            const val WINDOW_BUFFER_SIZE_EVENT: Short = 0x04
        }
    }

    @FieldOrder("bKeyDown", "wRepeatCount", "wVirtualKeyCode", "wVirtualScanCode", "uChar", "dwControlKeyState")
    class KEY_EVENT_RECORD : Structure() {
        @JvmField
        var bKeyDown: Boolean = false

        @JvmField
        var wRepeatCount: Short = 0

        @JvmField
        var wVirtualKeyCode: Short = 0

        @JvmField
        var wVirtualScanCode: Short = 0

        @JvmField
        var uChar: Char = 0.toChar()

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
                dwControlKeyState,
            )
        }
    }

    @FieldOrder("dwMousePosition", "dwButtonState", "dwControlKeyState", "dwEventFlags")
    class MOUSE_EVENT_RECORD : Structure() {
        @JvmField
        var dwMousePosition: COORD = COORD()

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
                dwEventFlags,
            )
        }
    }

    @FieldOrder("dwSize")
    class WINDOW_BUFFER_SIZE_RECORD : Structure() {
        @JvmField
        var dwSize: COORD = COORD()

        override fun toString(): String = String.format("WINDOW_BUFFER_SIZE_RECORD(%s)", dwSize)
    }
}
