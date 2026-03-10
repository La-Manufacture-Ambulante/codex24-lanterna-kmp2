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

import com.googlecode.lanterna.internal.compat.Arrays

/**
 * Simple pattern that matches the input stream against a predefined character sequence.
 */
class BasicCharacterPattern(val result: KeyStroke?, vararg pattern: Char) : CharacterPattern {
    private val pattern: CharArray = pattern

    fun getPattern(): CharArray {
        return Arrays.copyOf(pattern, pattern.size)
    }

    override fun match(seq: List<Char>?): CharacterPattern.Matching? {
        val sequence = seq ?: return null
        val size = sequence.size
        if (size > pattern.size) {
            return null
        }
        for (i in 0 until size) {
            if (pattern[i] != sequence[i]) {
                return null
            }
        }
        return if (size == pattern.size) {
            CharacterPattern.Matching(result)
        } else {
            CharacterPattern.Matching.NOT_YET
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is BasicCharacterPattern && Arrays.equals(pattern, other.pattern)
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 53 * hash + Arrays.hashCode(pattern)
        return hash
    }
}
