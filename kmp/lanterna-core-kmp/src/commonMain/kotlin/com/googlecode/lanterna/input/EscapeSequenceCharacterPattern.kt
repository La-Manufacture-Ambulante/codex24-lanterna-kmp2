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

open class EscapeSequenceCharacterPattern : CharacterPattern {
    private enum class State {
        START, INTRO, NUM1, NUM2, DONE
    }

    companion object {
        const val SHIFT: Int = 1
        const val ALT: Int = 2
        const val CTRL: Int = 4
    }

    protected val stdMap: MutableMap<Int, KeyType?> = HashMap()
    protected val finMap: MutableMap<Char, KeyType?> = HashMap()
    protected var useEscEsc: Boolean = true

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
        var bShift = false
        var bCtrl = false
        var bAlt = false
        if (key == null) {
            return null
        }
        if (mods >= 0) {
            bShift = (mods and SHIFT) != 0
            bAlt = (mods and ALT) != 0
            bCtrl = (mods and CTRL) != 0
        } else if (mods == -1 && key == KeyType.F3) {
            return KeyStroke.RealF3()
        }
        return KeyStroke(key, bCtrl, bAlt, bShift)
    }

    protected open fun getKeyStrokeRaw(first: Char, num1: Int, num2: Int, last: Char, bEsc: Boolean): KeyStroke? {
        val kt: KeyType?
        var bPuttyCtrl = false
        var bRealF3 = false
        if (last == '~' && stdMap.containsKey(num1)) {
            kt = stdMap[num1]
        } else if (finMap.containsKey(last)) {
            kt = finMap[last]
            if (first == 'O') {
                if (last >= 'A' && last <= 'D') {
                    bPuttyCtrl = true
                }
                if (last == 'R') {
                    bRealF3 = true
                }
            }
        } else {
            kt = null
        }
        var mods = num2 - 1
        if (bEsc) {
            if (mods >= 0) {
                mods = mods or ALT
            } else {
                mods = ALT
            }
        }
        if (bPuttyCtrl) {
            if (mods >= 0) {
                mods = mods or CTRL
            } else {
                mods = CTRL
            }
        }
        if (bRealF3) {
            mods = -1
        }
        return getKeyStroke(kt, mods)
    }

    override open fun match(cur: List<Char?>): CharacterPattern.Matching? {
        var state = State.START
        var num1 = 0
        var num2 = 0
        var first = '\u0000'
        var last = '\u0000'
        var bEsc = false

        for (chNullable in cur) {
            val ch = chNullable ?: throw NullPointerException()
            when (state) {
                State.START -> {
                    if (ch != KeyDecodingProfile.ESC_CODE) {
                        return null
                    }
                    state = State.INTRO
                    continue
                }

                State.INTRO -> {
                    if (useEscEsc && ch == KeyDecodingProfile.ESC_CODE && !bEsc) {
                        bEsc = true
                        continue
                    }
                    if (ch != '[' && ch != 'O') {
                        return null
                    }
                    first = ch
                    state = State.NUM1
                    continue
                }

                State.NUM1 -> {
                    if (ch == ';') {
                        state = State.NUM2
                    } else if (Character.isDigit(ch)) {
                        num1 = num1 * 10 + Character.digit(ch, 10)
                    } else {
                        last = ch
                        state = State.DONE
                    }
                    continue
                }

                State.NUM2 -> {
                    if (Character.isDigit(ch)) {
                        num2 = num2 * 10 + Character.digit(ch, 10)
                    } else {
                        last = ch
                        state = State.DONE
                    }
                    continue
                }

                State.DONE -> {
                    return null
                }
            }
        }
        return if (state == State.DONE) {
            val ks = getKeyStrokeRaw(first, num1, num2, last, bEsc)
            if (ks != null) CharacterPattern.Matching(ks) else null
        } else {
            CharacterPattern.Matching.NOT_YET
        }
    }
}
