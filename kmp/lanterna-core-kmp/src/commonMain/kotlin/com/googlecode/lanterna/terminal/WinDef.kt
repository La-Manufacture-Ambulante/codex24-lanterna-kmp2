/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.sun.jna.*
import java.util.Arrays

/**
 * Class containing common Win32 structures involved when operating on the terminal
 */
open class WinDef private constructor() {

    open class HANDLE : PointerType {
        private var immutable: Boolean = false

        constructor()

        constructor(p: Pointer?) {
            this.setPointer(p)
            this.immutable = true
        }

        override fun fromNative(nativeValue: Any?, context: FromNativeContext?): Any? {
            val o = super.fromNative(nativeValue, context)
            return if (WinDef.INVALID_HANDLE_VALUE == o) WinDef.INVALID_HANDLE_VALUE else o
        }

        override fun setPointer(p: Pointer?) {
            if (this.immutable) {
                throw UnsupportedOperationException("immutable reference")
            } else {
                super.setPointer(p)
            }
        }

        override fun toString(): String {
            return java.lang.String.valueOf(this.pointer)
        }
    }

    open class WORD : IntegerType, Comparable<WORD> {
        constructor() : this(0L)

        constructor(value: Long) : super(2, value, true)

        override fun compareTo(other: WORD): Int {
            return compare(this, other)
        }

        companion object {
            const val SIZE: Int = 2
        }
    }

    open class COORD : Structure() {
        @JvmField
        var X: Short = 0

        @JvmField
        var Y: Short = 0

        override fun getFieldOrder(): List<String> {
            return Arrays.asList("X", "Y")
        }

        override fun toString(): String {
            return "COORD{X=$X, Y=$Y}"
        }
    }

    open class SMALL_RECT : Structure() {
        @JvmField
        var Left: Short = 0

        @JvmField
        var Top: Short = 0

        @JvmField
        var Right: Short = 0

        @JvmField
        var Bottom: Short = 0

        override fun getFieldOrder(): List<String> {
            return Arrays.asList("Left", "Top", "Right", "Bottom")
        }

        override fun toString(): String {
            return "SMALL_RECT{Left=$Left, Top=$Top, Right=$Right, Bottom=$Bottom}"
        }
    }

    open class CONSOLE_SCREEN_BUFFER_INFO : Structure() {
        @JvmField
        var dwSize: COORD? = null

        @JvmField
        var dwCursorPosition: COORD? = null

        @JvmField
        var wAttributes: WORD? = null

        @JvmField
        var srWindow: SMALL_RECT? = null

        @JvmField
        var dwMaximumWindowSize: COORD? = null

        override fun getFieldOrder(): List<String> {
            return Arrays.asList("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
        }

        override fun toString(): String {
            return "CONSOLE_SCREEN_BUFFER_INFO{" +
                "dwSize=$dwSize" +
                ", dwCursorPosition=$dwCursorPosition" +
                ", wAttributes=$wAttributes" +
                ", srWindow=$srWindow" +
                ", dwMaximumWindowSize=$dwMaximumWindowSize" +
                '}'
        }
    }

    companion object {
        @JvmField
        val INVALID_HANDLE_VALUE: HANDLE =
            HANDLE(Pointer.createConstant(if (Pointer.SIZE == 8) -1L else 4294967295L))
    }
}
