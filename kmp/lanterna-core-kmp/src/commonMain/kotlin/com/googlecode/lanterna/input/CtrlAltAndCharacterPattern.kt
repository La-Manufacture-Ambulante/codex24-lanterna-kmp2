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

/**
 * Character pattern that matches characters pressed while ALT and CTRL are held down.
 */
class CtrlAltAndCharacterPattern : CharacterPattern {
    override fun match(seq: List<Char>?): CharacterPattern.Matching? {
        val sequence = seq ?: return null
        val size = sequence.size
        if (size > 2 || sequence[0] != KeyDecodingProfile.ESC_CODE) {
            return null
        }
        if (size == 1) {
            return CharacterPattern.Matching.NOT_YET
        }

        val ch = sequence[1]
        if (ch.code < 32 && ch != '\b') {
            val ctrlCode = when (ch) {
                KeyDecodingProfile.ESC_CODE -> return null
                '\u0000' -> ' '
                '\u001c' -> '\\'
                '\u001d' -> ']'
                '\u001e' -> '^'
                '\u001f' -> '_'
                else -> ('a'.code - 1 + ch.code).toChar()
            }
            return CharacterPattern.Matching(KeyStroke(ctrlCode, true, true))
        }
        if (ch.code == 0x7f || ch == '\b') {
            return CharacterPattern.Matching(KeyStroke(KeyType.BACKSPACE, false, true))
        }
        return null
    }
}
