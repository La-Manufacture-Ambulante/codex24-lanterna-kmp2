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
package com.googlecode.lanterna.input

import kotlin.collections.ArrayList

/**
 * Represents one decoded keyboard event.
 */
open class KeyStroke private constructor(
    val keyType: KeyType?,
    character: Char?,
    val isCtrlDown: Boolean,
    val isAltDown: Boolean,
    val isShiftDown: Boolean,
) {
    val character: Char?
    val eventTime: Long

    constructor(keyType: KeyType?) : this(keyType, null, false, false, false)

    constructor(keyType: KeyType?, ctrlDown: Boolean, altDown: Boolean) : this(keyType, null, ctrlDown, altDown, false)

    constructor(keyType: KeyType?, ctrlDown: Boolean, altDown: Boolean, shiftDown: Boolean) :
        this(keyType, null, ctrlDown, altDown, shiftDown)

    constructor(character: Char?, ctrlDown: Boolean, altDown: Boolean) :
        this(KeyType.CHARACTER, character, ctrlDown, altDown, false)

    constructor(character: Char?, ctrlDown: Boolean, altDown: Boolean, shiftDown: Boolean) :
        this(KeyType.CHARACTER, character, ctrlDown, altDown, shiftDown)

    init {
        var actualCharacter = character
        if (keyType == KeyType.CHARACTER && actualCharacter == null) {
            throw IllegalArgumentException(
                "Cannot construct a KeyStroke with type KeyType.Character but no character information",
            )
        }
        when (keyType) {
            KeyType.BACKSPACE -> actualCharacter = '\b'
            KeyType.ENTER -> actualCharacter = '\n'
            KeyType.TAB -> actualCharacter = '\t'
            else -> Unit
        }
        this.character = actualCharacter
        this.eventTime = com.googlecode.lanterna.internal.compat.System.currentTimeMillis()
    }

    /**
     * F3 that is distinguishable from a cursor-location report.
     */
    class RealF3 : KeyStroke(KeyType.F3, false, false, false)

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("KeyStroke{keytype=").append(keyType)
        character?.let { ch ->
            sb.append(", character='")
            when (ch) {
                '\u0000' -> sb.append("^@")
                '\b' -> sb.append("\\b")
                '\t' -> sb.append("\\t")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\u001b' -> sb.append("^[")
                '\u001c' -> sb.append("^\\")
                '\u001d' -> sb.append("^]")
                '\u001e' -> sb.append("^^")
                '\u001f' -> sb.append("^_")
                else -> {
                    if (ch.code <= 26) {
                        sb.append('^').append((ch.code + 64).toChar())
                    } else {
                        sb.append(ch)
                    }
                }
            }
            sb.append('\'')
        }
        if (isCtrlDown || isAltDown || isShiftDown) {
            var sep = ""
            sb.append(", modifiers=[")
            if (isCtrlDown) {
                sb.append(sep).append("ctrl")
                sep = ","
            }
            if (isAltDown) {
                sb.append(sep).append("alt")
                sep = ","
            }
            if (isShiftDown) {
                sb.append(sep).append("shift")
            }
            sb.append("]")
        }
        return sb.append('}').toString()
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 41 * hash + (keyType?.hashCode() ?: 0)
        hash = 41 * hash + (character?.hashCode() ?: 0)
        hash = 41 * hash + if (isCtrlDown) 1 else 0
        hash = 41 * hash + if (isAltDown) 1 else 0
        hash = 41 * hash + if (isShiftDown) 1 else 0
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this::class != other::class) {
            return false
        }
        other as KeyStroke
        return keyType == other.keyType &&
            character == other.character &&
            isCtrlDown == other.isCtrlDown &&
            isAltDown == other.isAltDown &&
            isShiftDown == other.isShiftDown
    }

    companion object {
        fun fromString(keyStr: String): KeyStroke {
            val keyStrLC = keyStr.lowercase()
            if (keyStr.length == 1) {
                return KeyStroke(keyStr[0], false, false)
            }
            if (!keyStr.startsWith("<") || !keyStr.endsWith(">")) {
                throw IllegalArgumentException("Invalid vim notation: $keyStr")
            }
            if (keyStrLC == "<s-tab>") {
                return KeyStroke(KeyType.REVERSE_TAB)
            }
            if (keyStr.contains("-")) {
                val segments = ArrayList(listOf(*keyStr.substring(1, keyStr.length - 1).split("-").toTypedArray()))
                if (segments.size < 2) {
                    throw IllegalArgumentException("Invalid vim notation: $keyStr")
                }
                var characterStr = segments.removeAt(segments.size - 1)
                var altPressed = false
                var ctrlPressed = false
                for (modifier in segments) {
                    when (modifier.lowercase()) {
                        "c" -> ctrlPressed = true
                        "a" -> altPressed = true
                        "s" -> characterStr = characterStr.uppercase()
                    }
                }
                return KeyStroke(characterStr[0], ctrlPressed, altPressed)
            }
            return when (keyStrLC) {
                "<esc>" -> KeyStroke(KeyType.ESCAPE)
                "<cr>", "<enter>", "<return>" -> KeyStroke(KeyType.ENTER)
                "<bs>" -> KeyStroke(KeyType.BACKSPACE)
                "<tab>" -> KeyStroke(KeyType.TAB)
                "<space>" -> KeyStroke(' ', false, false)
                "<up>" -> KeyStroke(KeyType.ARROW_UP)
                "<down>" -> KeyStroke(KeyType.ARROW_DOWN)
                "<left>" -> KeyStroke(KeyType.ARROW_LEFT)
                "<right>" -> KeyStroke(KeyType.ARROW_RIGHT)
                "<insert>" -> KeyStroke(KeyType.INSERT)
                "<del>" -> KeyStroke(KeyType.DELETE)
                "<home>" -> KeyStroke(KeyType.HOME)
                "<end>" -> KeyStroke(KeyType.END)
                "<pageup>" -> KeyStroke(KeyType.PAGE_UP)
                "<pagedown>" -> KeyStroke(KeyType.PAGE_DOWN)
                "<f1>" -> KeyStroke(KeyType.F1)
                "<f2>" -> KeyStroke(KeyType.F2)
                "<f3>" -> KeyStroke(KeyType.F3)
                "<f4>" -> KeyStroke(KeyType.F4)
                "<f5>" -> KeyStroke(KeyType.F5)
                "<f6>" -> KeyStroke(KeyType.F6)
                "<f7>" -> KeyStroke(KeyType.F7)
                "<f8>" -> KeyStroke(KeyType.F8)
                "<f9>" -> KeyStroke(KeyType.F9)
                "<f10>" -> KeyStroke(KeyType.F10)
                "<f11>" -> KeyStroke(KeyType.F11)
                "<f12>" -> KeyStroke(KeyType.F12)
                else -> throw IllegalArgumentException("Invalid vim notation: $keyStr")
            }
        }
    }
}
