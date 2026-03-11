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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.input

import com.googlecode.lanterna.internal.compat.Character

/**
 * Character pattern that matches characters pressed while ALT is held down.
 */
class AltAndCharacterPattern : CharacterPattern {
    override fun match(seq: List<Char>?): CharacterPattern.Matching? {
        val sequence = seq ?: return null
        val size = sequence.size
        if (size > 2 || sequence[0] != KeyDecodingProfile.ESC_CODE) {
            return null
        }
        if (size == 1) {
            return CharacterPattern.Matching.NOT_YET
        }
        val character = sequence[1]
        if (Character.isISOControl(character)) {
            return null
        }
        return CharacterPattern.Matching(KeyStroke(character, false, true))
    }
}
