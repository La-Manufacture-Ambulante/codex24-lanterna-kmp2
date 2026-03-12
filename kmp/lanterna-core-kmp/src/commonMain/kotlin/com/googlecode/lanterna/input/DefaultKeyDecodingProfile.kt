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
 * Default collection of character patterns used to decode keystrokes.
 */
class DefaultKeyDecodingProfile : KeyDecodingProfile {
    override val patterns: Collection<CharacterPattern>
        get() = ArrayList(COMMON_PATTERNS)

    companion object {
        private val COMMON_PATTERNS =
            listOf<CharacterPattern>(
                BasicCharacterPattern(KeyStroke(KeyType.ESCAPE), KeyDecodingProfile.ESC_CODE),
                BasicCharacterPattern(KeyStroke(KeyType.TAB), '\t'),
                BasicCharacterPattern(KeyStroke(KeyType.ENTER), '\n'),
                BasicCharacterPattern(KeyStroke(KeyType.ENTER), '\r', '\u0000'),
                BasicCharacterPattern(KeyStroke(KeyType.BACKSPACE), 0x7f.toChar()),
                BasicCharacterPattern(KeyStroke(KeyType.BACKSPACE), 0x08.toChar()),
                BasicCharacterPattern(KeyStroke(KeyType.F1), KeyDecodingProfile.ESC_CODE, '[', '[', 'A'),
                BasicCharacterPattern(KeyStroke(KeyType.F2), KeyDecodingProfile.ESC_CODE, '[', '[', 'B'),
                BasicCharacterPattern(KeyStroke(KeyType.F3), KeyDecodingProfile.ESC_CODE, '[', '[', 'C'),
                BasicCharacterPattern(KeyStroke(KeyType.F4), KeyDecodingProfile.ESC_CODE, '[', '[', 'D'),
                BasicCharacterPattern(KeyStroke(KeyType.F5), KeyDecodingProfile.ESC_CODE, '[', '[', 'E'),
                EscapeSequenceCharacterPattern(),
                NormalCharacterPattern(),
                AltAndCharacterPattern(),
                CtrlAndCharacterPattern(),
                CtrlAltAndCharacterPattern(),
                ScreenInfoCharacterPattern(),
                MouseCharacterPattern(),
            )
    }
}
