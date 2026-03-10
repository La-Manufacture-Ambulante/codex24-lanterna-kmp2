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

import java.util.HashMap

/**
 * Matches terminal escape sequences representing special keys and key modifiers.
 */
open class EscapeSequenceCharacterPattern : CharacterPattern {
    protected val stdMap: MutableMap<Int, KeyType?> = HashMap()
    protected val finMap: MutableMap<Char, KeyType?> = HashMap()
    protected var useEscEsc = true

    private enum class State {
        START,
        INTRO,
        NUM1,
        NUM2,
        DONE,
    }

    init {
        finMap['A'] = KeyType.ARROW_UP
        finMap['B'] = KeyType.ARROW_DOWN
        finMap['C'] = KeyType.ARROW_RIGHT
        finMap['D'] = KeyType.ARROW_LEFT
        finMap['E'] = KeyType.KEY_TYPE
        finMap['G'] = KeyType.KEY_TYPE
        finMap['H'] = KeyType.HOME
        finMap['F'] = KeyType.END
        finMap['P'] = KeyType.F1
        finMap['Q'] = KeyType.F2
        finMap['R'] = KeyType.F3
        finMap['S'] = KeyType.F4
        finMap['Z'] = KeyType.REVERSE_TAB

        stdMap[1] = KeyType.HOME
        stdMap[2] = KeyType.INSERT
        stdMap[3] = KeyType.DELETE
        stdMap[4] = KeyType.END
        stdMap[5] = KeyType.PAGE_UP
        stdMap[6] = KeyType.PAGE_DOWN
        stdMap[11] = KeyType.F1
        stdMap[12] = KeyType.F2
        stdMap[13] = KeyType.F3
        stdMap[14] = KeyType.F4
        stdMap[15] = KeyType.F5
        stdMap[16] = KeyType.F5
        stdMap[17] = KeyType.F6
        stdMap[18] = KeyType.F7
        stdMap[19] = KeyType.F8
        stdMap[20] = KeyType.F9
        stdMap[21] = KeyType.F10
        stdMap[23] = KeyType.F11
        stdMap[24] = KeyType.F12
        stdMap[25] = KeyType.F13
        stdMap[26] = KeyType.F14
        stdMap[28] = KeyType.F15
        stdMap[29] = KeyType.F16
        stdMap[31] = KeyType.F17
        stdMap[32] = KeyType.F18
        stdMap[33] = KeyType.F19
    }

    protected open fun getKeyStroke(key: KeyType?, mods: Int): KeyStroke? {
        if (key == null) {
            return null
        }
        if (mods == -1 && key == KeyType.F3) {
            return KeyStroke.RealF3()
        }

        val shift = mods >= 0 && (mods and SHIFT) != 0
        val alt = mods >= 0 && (mods and ALT) != 0
        val ctrl = mods >= 0 && (mods and CTRL) != 0
        return KeyStroke(key, ctrl, alt, shift)
    }

    protected open fun getKeyStrokeRaw(first: Char, num1: Int, num2: Int, last: Char, escaped: Boolean): KeyStroke? {
        var puttyCtrl = false
        var realF3 = false

        val keyType = when {
            last == '~' && stdMap.containsKey(num1) -> stdMap[num1]
            finMap.containsKey(last) -> {
                if (first == 'O') {
                    if (last in 'A'..'D') {
                        puttyCtrl = true
                    }
                    if (last == 'R') {
                        realF3 = true
                    }
                }
                finMap[last]
            }
            else -> null
        }

        var mods = num2 - 1
        if (escaped) {
            mods = if (mods >= 0) mods or ALT else ALT
        }
        if (puttyCtrl) {
            mods = if (mods >= 0) mods or CTRL else CTRL
        }
        if (realF3) {
            mods = -1
        }
        return getKeyStroke(keyType, mods)
    }

    override fun match(cur: List<Char>?): CharacterPattern.Matching? {
        val sequence = cur ?: return null
        var state = State.START
        var num1 = 0
        var num2 = 0
        var first = '\u0000'
        var last = '\u0000'
        var escaped = false

        for (ch in sequence) {
            when (state) {
                State.START -> {
                    if (ch != KeyDecodingProfile.ESC_CODE) {
                        return null
                    }
                    state = State.INTRO
                }
                State.INTRO -> {
                    if (useEscEsc && ch == KeyDecodingProfile.ESC_CODE && !escaped) {
                        escaped = true
                        continue
                    }
                    if (ch != '[' && ch != 'O') {
                        return null
                    }
                    first = ch
                    state = State.NUM1
                }
                State.NUM1 -> {
                    when {
                        ch == ';' -> state = State.NUM2
                        Character.isDigit(ch) -> num1 = num1 * 10 + Character.digit(ch, 10)
                        else -> {
                            last = ch
                            state = State.DONE
                        }
                    }
                }
                State.NUM2 -> {
                    if (Character.isDigit(ch)) {
                        num2 = num2 * 10 + Character.digit(ch, 10)
                    } else {
                        last = ch
                        state = State.DONE
                    }
                }
                State.DONE -> return null
            }
        }

        return if (state == State.DONE) {
            getKeyStrokeRaw(first, num1, num2, last, escaped)?.let { CharacterPattern.Matching(it) }
        } else {
            CharacterPattern.Matching.NOT_YET
        }
    }

    companion object {
        const val SHIFT = 1
        const val ALT = 2
        const val CTRL = 4
    }
}
