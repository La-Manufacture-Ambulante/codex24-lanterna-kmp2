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
package com.googlecode.lanterna

import com.googlecode.lanterna.graphics.StyleSet
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.ArrayList
import java.util.LinkedList

public object TerminalTextUtils {
    public fun getANSIControlSequenceAt(string: String?, index: Int): String? {
        val len = getANSIControlSequenceLength(string, index)
        return if (len == 0) null else string!!.substring(index, index + len)
    }

    public fun getANSIControlSequenceLength(string: String?, index: Int): Int {
        var len = 0
        val restlen = string!!.length - index
        if (restlen >= 3) {
            val esc = string[index]
            val bracket = string[index + 1]
            if (esc == '\u001B' && bracket == '[') {
                len = 3
                for (i in 2 until restlen) {
                    val ch = string[i + index]
                    if ((ch >= '0' && ch <= '9') || ch == ';') {
                        len++
                    } else {
                        break
                    }
                }
                if (len > restlen) {
                    len = 0
                }
            }
        }
        return len
    }

    public fun isCharCJK(c: Char): Boolean {
        val unicodeBlock = Character.UnicodeBlock.of(c)
        return (unicodeBlock == Character.UnicodeBlock.HIRAGANA) ||
            (unicodeBlock == Character.UnicodeBlock.KATAKANA) ||
            (unicodeBlock == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) ||
            (unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) ||
            (unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO) ||
            (unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT) ||
            (unicodeBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) ||
            (unicodeBlock == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS) ||
            (unicodeBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS && c < '\uFF61')
    }

    public fun isCharThai(c: Char): Boolean {
        val unicodeBlock = Character.UnicodeBlock.of(c)
        return unicodeBlock == Character.UnicodeBlock.THAI
    }

    public fun isCharDoubleWidth(c: Char): Boolean {
        return isCharCJK(c)
    }

    public fun isControlCharacter(c: Char): Boolean {
        return c.code < 32 || c.code == 127
    }

    public fun isPrintableCharacter(c: Char): Boolean {
        return !isControlCharacter(c) || c == '\t' || c == '\n' || c == '\b'
    }

    public fun getColumnWidth(s: String?): Int {
        return getColumnIndex(s, s!!.length)
    }

    public fun getColumnIndex(s: String?, stringCharacterIndex: Int): Int {
        return getColumnIndex(s, stringCharacterIndex, TabBehaviour.CONVERT_TO_FOUR_SPACES, -1)
    }

    public fun getColumnIndex(
        s: String?,
        stringCharacterIndex: Int,
        tabBehaviour: TabBehaviour?,
        firstCharacterColumnPosition: Int
    ): Int {
        var index = 0
        for (i in 0 until stringCharacterIndex) {
            if (s!![i] == '\t') {
                index += tabBehaviour!!.getTabReplacement(firstCharacterColumnPosition).length
            } else {
                if (isCharCJK(s[i])) {
                    index++
                }
                index++
            }
        }
        return index
    }

    public fun getStringCharacterIndex(s: String?, columnIndex: Int): Int {
        var index = 0
        var counter = 0
        while (counter < columnIndex) {
            if (isCharCJK(s!![index++])) {
                counter++
                if (counter == columnIndex) {
                    return index - 1
                }
            }
            counter++
        }
        return index
    }

    public fun fitString(string: String?, availableColumnSpace: Int): String {
        return fitString(string, 0, availableColumnSpace)
    }

    public fun fitString(string: String?, fromColumn: Int, availableColumnSpace: Int): String {
        if (availableColumnSpace <= 0) {
            return ""
        }

        val bob = StringBuilder()
        var column = 0
        var index = 0
        var available = availableColumnSpace

        while (index < string!!.length && column < fromColumn) {
            val c = string[index++]
            column += if (TerminalTextUtils.isCharCJK(c)) 2 else 1
        }
        if (column > fromColumn) {
            bob.append(" ")
            available--
        }

        while (available > 0 && index < string.length) {
            val c = string[index++]
            available -= if (TerminalTextUtils.isCharCJK(c)) 2 else 1
            if (available < 0) {
                bob.append(' ')
            } else {
                bob.append(c)
            }
        }
        return bob.toString()
    }

    public fun getWordWrappedText(maxWidth: Int, vararg lines: String?): List<String?> {
        if (maxWidth <= 0) {
            return java.util.Arrays.asList(*lines)
        }

        val result: MutableList<String?> = ArrayList()
        val linesToBeWrapped: LinkedList<String?> = LinkedList(java.util.Arrays.asList(*lines))
        while (!linesToBeWrapped.isEmpty()) {
            val row = linesToBeWrapped.removeFirst()
            val rowWidth = getColumnWidth(row)
            if (rowWidth <= maxWidth) {
                result.add(row)
            } else {
                val characterIndexMax = getStringCharacterIndex(row, maxWidth)
                var characterIndex = characterIndexMax
                while (characterIndex >= 0 &&
                    !Character.isSpaceChar(row!![characterIndex]) &&
                    !isCharCJK(row[characterIndex])
                ) {
                    characterIndex--
                }

                if (characterIndex >= 0 && characterIndex < characterIndexMax &&
                    isCharCJK(row!![characterIndex])
                ) {
                    characterIndex++
                }

                if (characterIndex < 0) {
                    characterIndex = kotlin.math.max(characterIndexMax, 1)
                    result.add(row!!.substring(0, characterIndex))
                    linesToBeWrapped.addFirst(row.substring(characterIndex))
                } else {
                    characterIndex = kotlin.math.max(characterIndex, 1)
                    result.add(row!!.substring(0, characterIndex))
                    while (characterIndex < row.length &&
                        Character.isSpaceChar(row[characterIndex])
                    ) {
                        characterIndex++
                    }
                    if (characterIndex < row.length) {
                        linesToBeWrapped.addFirst(row.substring(characterIndex))
                    }
                }
            }
        }
        return result
    }

    private fun mapCodesToIntegerArray(codes: Array<String>): Array<Int?> {
        val result = arrayOfNulls<Int>(codes.size)
        for (i in result.indices) {
            if (codes[i].isEmpty()) {
                result[i] = 0
            } else {
                try {
                    result[i] = Integer.parseInt(codes[i])
                } catch (ignored: NumberFormatException) {
                    throw IllegalArgumentException("Unknown CSI code " + codes[i])
                }
            }
        }
        return result
    }

    public fun updateModifiersFromCSICode(
        controlSequence: String?,
        target: StyleSet<*>?,
        original: StyleSet<*>?
    ) {
        var sequence = controlSequence!!
        val controlCodeType = sequence[sequence.length - 1]
        sequence = sequence.substring(2, sequence.length - 1)
        val codes = mapCodesToIntegerArray(sequence.split(";").toTypedArray())

        val palette = TextColor.ANSI.values()

        if (controlCodeType == 'm') {
            var i = 0
            while (i < codes.size) {
                val code = codes[i]!!
                when (code) {
                    0 -> target!!.setStyleFrom(original)
                    1 -> target!!.enableModifiers(SGR.BOLD)
                    3 -> target!!.enableModifiers(SGR.ITALIC)
                    4 -> target!!.enableModifiers(SGR.UNDERLINE)
                    5 -> target!!.enableModifiers(SGR.BLINK)
                    7 -> target!!.enableModifiers(SGR.REVERSE)
                    21, 22 -> target!!.disableModifiers(SGR.BOLD)
                    23 -> target!!.disableModifiers(SGR.ITALIC)
                    24 -> target!!.disableModifiers(SGR.UNDERLINE)
                    25 -> target!!.disableModifiers(SGR.BLINK)
                    27 -> target!!.disableModifiers(SGR.REVERSE)
                    38 -> {
                        if (i + 2 < codes.size && codes[i + 1] == 5) {
                            target!!.setForegroundColor(TextColor.Indexed(codes[i + 2]!!))
                            i += 2
                        } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                            target!!.setForegroundColor(
                                TextColor.RGB(codes[i + 2]!!, codes[i + 3]!!, codes[i + 4]!!)
                            )
                            i += 4
                        }
                    }
                    39 -> target!!.setForegroundColor(original!!.foregroundColor)
                    48 -> {
                        if (i + 2 < codes.size && codes[i + 1] == 5) {
                            target!!.setBackgroundColor(TextColor.Indexed(codes[i + 2]!!))
                            i += 2
                        } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                            target!!.setBackgroundColor(
                                TextColor.RGB(codes[i + 2]!!, codes[i + 3]!!, codes[i + 4]!!)
                            )
                            i += 4
                        }
                    }
                    49 -> target!!.setBackgroundColor(original!!.backgroundColor)
                    else -> {
                        if (code >= 30 && code <= 37) {
                            target!!.setForegroundColor(palette[code - 30])
                        } else if (code >= 40 && code <= 47) {
                            target!!.setBackgroundColor(palette[code - 40])
                        }
                    }
                }
                i++
            }
        }
    }
}
