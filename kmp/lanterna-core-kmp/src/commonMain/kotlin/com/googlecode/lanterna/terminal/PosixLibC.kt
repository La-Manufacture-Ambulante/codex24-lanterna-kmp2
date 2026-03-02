package com.googlecode.lanterna.terminal

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Structure

interface PosixLibC : Library {
    fun tcgetattr(fd: Int, termios_p: termios?): Int
    fun tcsetattr(fd: Int, optional_actions: Int, termios_p: termios?): Int
    fun ioctl(fd: Int, request: Int, winsize: winsize?): Int
    fun signal(sig: Int, fn: sig_t?): sig_t?

    companion object {
        const val STDIN_FILENO: Int = 0
        const val STDOUT_FILENO: Int = 1
        const val TCSANOW: Int = 0
        const val NCCS: Int = 32

        // Constants for c_lflag (converted from Java octal literals: 01, 02, 010)
        const val ISIG: Int = 1
        const val ICANON: Int = 2
        const val ECHO: Int = 8

        // Signals
        const val SIGWINCH: Int = 28

        // Constants for ioctl
        const val TIOCGWINSZ: Int = 0x5413
    }

    interface sig_t : Callback {
        fun invoke(signal: Int)
    }

    class termios : Structure() {
        @JvmField
        var c_iflag: Int = 0 // input mode flags

        @JvmField
        var c_oflag: Int = 0 // output mode flags

        @JvmField
        var c_cflag: Int = 0 // control mode flags

        @JvmField
        var c_lflag: Int = 0 // local mode flags

        @JvmField
        var c_line: Byte = 0 // line discipline

        @JvmField
        var c_cc: ByteArray? = ByteArray(PosixLibC.NCCS) // control characters

        @JvmField
        var c_ispeed: Int = 0 // input speed

        @JvmField
        var c_ospeed: Int = 0 // output speed

        override fun getFieldOrder(): List<String> {
            return listOf(
                "c_iflag",
                "c_oflag",
                "c_cflag",
                "c_lflag",
                "c_line",
                "c_cc",
                "c_ispeed",
                "c_ospeed"
            )
        }

        override fun toString(): String {
            return "termios{" +
                "c_iflag=" + c_iflag +
                ", c_oflag=" + c_oflag +
                ", c_cflag=" + c_cflag +
                ", c_lflag=" + c_lflag +
                ", c_line=" + c_line +
                ", c_cc=" + (c_cc?.contentToString() ?: "null") +
                ", c_ispeed=" + c_ispeed +
                ", c_ospeed=" + c_ospeed +
                '}'
        }
    }

    class winsize : Structure() {
        @JvmField
        var ws_row: Short = 0

        @JvmField
        var ws_col: Short = 0

        @JvmField
        var ws_xpixel: Short = 0

        @JvmField
        var ws_ypixel: Short = 0

        override fun getFieldOrder(): List<String> {
            return listOf("ws_row", "ws_col", "ws_xpixel", "ws_ypixel")
        }

        override fun toString(): String {
            return "winsize{" +
                "ws_row=" + ws_row +
                ", ws_col=" + ws_col +
                ", ws_xpixel=" + ws_xpixel +
                ", ws_ypixel=" + ws_ypixel +
                '}'
        }
    }
}
