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
import com.googlecode.lanterna.internal.compat.Character
import com.googlecode.lanterna.internal.compat.LinkedList
import com.googlecode.lanterna.screen.TabBehaviour
import kotlin.collections.ArrayList

/**
 * This class contains a number of utility methods for analyzing characters and strings in a terminal context.
 */
object TerminalTextUtils {
    fun getANSIControlSequenceAt(
        string: String?,
        index: Int,
    ): String? {
        val s = string ?: return null
        val len = getANSIControlSequenceLength(s, index)
        return if (len == 0) null else s.substring(index, index + len)
    }

    fun getANSIControlSequenceLength(
        string: String,
        index: Int,
    ): Int {
        var len = 0
        val restLen = string.length - index
        if (restLen >= 3) {
            val esc = string[index]
            val bracket = string[index + 1]
            if (esc.code == 0x1B && bracket == '[') {
                len = 3
                for (i in 2 until restLen) {
                    val ch = string[i + index]
                    if ((ch in '0'..'9') || ch == ';') {
                        len++
                    } else {
                        break
                    }
                }
                if (len > restLen) {
                    len = 0
                }
            }
        }
        return len
    }

    fun isCharCJK(c: Char): Boolean {
        val unicodeBlock = com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.of(c)
        return (
            (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.HIRAGANA) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.KATAKANA) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.HANGUL_JAMO) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.HANGUL_SYLLABLES) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) ||
                (unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS) ||
                (
                    unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS &&
                        c.code < 0xFF61
                )
        )
    }

    fun isCharThai(c: Char): Boolean {
        val unicodeBlock = com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.of(c)
        return unicodeBlock === com.googlecode.lanterna.internal.compat.Character.UnicodeBlock.THAI
    }

    fun isCharDoubleWidth(c: Char): Boolean = isCharCJK(c)

    fun isControlCharacter(c: Char): Boolean = c.code < 32 || c.code == 127

    fun isPrintableCharacter(c: Char): Boolean = !isControlCharacter(c) || c == '\t' || c == '\n' || c == '\b'

    fun getColumnWidth(s: String?): Int = getColumnIndex(s, s!!.length)

    fun getColumnIndex(
        s: String?,
        stringCharacterIndex: Int,
        tabBehaviour: TabBehaviour? = TabBehaviour.CONVERT_TO_FOUR_SPACES,
        firstCharacterColumnPosition: Int = -1,
    ): Int {
        val text = s ?: return 0
        var index = 0
        for (i in 0 until stringCharacterIndex) {
            if (text[i] == '\t') {
                index += tabBehaviour!!.getTabReplacement(firstCharacterColumnPosition)!!.length
            } else {
                if (isCharCJK(text[i])) {
                    index++
                }
                index++
            }
        }
        return index
    }

    fun getStringCharacterIndex(
        s: String?,
        columnIndex: Int,
    ): Int {
        val text = s ?: return 0
        var index = 0
        var counter = 0
        while (counter < columnIndex) {
            if (isCharCJK(text[index++])) {
                counter++
                if (counter == columnIndex) {
                    return index - 1
                }
            }
            counter++
        }
        return index
    }

    fun fitString(
        string: String?,
        availableColumnSpace: Int,
    ): String? = fitString(string, 0, availableColumnSpace)

    fun fitString(
        string: String?,
        fromColumn: Int,
        availableColumnSpace: Int,
    ): String? {
        var available = availableColumnSpace
        if (available <= 0) {
            return ""
        }

        val text = string ?: return ""
        val out = StringBuilder()
        var column = 0
        var index = 0
        while (index < text.length && column < fromColumn) {
            val c = text[index++]
            column += if (isCharCJK(c)) 2 else 1
        }
        if (column > fromColumn) {
            out.append(" ")
            available--
        }

        while (available > 0 && index < text.length) {
            val c = text[index++]
            available -= if (isCharCJK(c)) 2 else 1
            if (available < 0) {
                out.append(' ')
            } else {
                out.append(c)
            }
        }
        return out.toString()
    }

    fun getWordWrappedText(
        maxWidth: Int,
        vararg lines: String?,
    ): List<String?> {
        if (maxWidth <= 0) {
            return lines.asList()
        }

        val result = ArrayList<String?>()
        val linesToBeWrapped = LinkedList(lines.asList())
        while (!linesToBeWrapped.isEmpty()) {
            val row = linesToBeWrapped.removeFirst()
            val rowWidth = getColumnWidth(row)
            if (rowWidth <= maxWidth) {
                result.add(row)
            } else {
                val text = row ?: ""
                val characterIndexMax = getStringCharacterIndex(text, maxWidth)
                var characterIndex = characterIndexMax
                while (characterIndex >= 0 &&
                    !com.googlecode.lanterna.internal.compat.Character.isSpaceChar(text[characterIndex]) &&
                    !isCharCJK(text[characterIndex])
                ) {
                    characterIndex--
                }
                if (characterIndex >= 0 &&
                    characterIndex < characterIndexMax &&
                    isCharCJK(text[characterIndex])
                ) {
                    characterIndex++
                }

                if (characterIndex < 0) {
                    characterIndex = maxOf(characterIndexMax, 1)
                    result.add(text.substring(0, characterIndex))
                    linesToBeWrapped.addFirst(text.substring(characterIndex))
                } else {
                    characterIndex = maxOf(characterIndex, 1)
                    result.add(text.substring(0, characterIndex))
                    while (
                        characterIndex < text.length &&
                        com.googlecode.lanterna.internal.compat.Character.isSpaceChar(text[characterIndex])
                    ) {
                        characterIndex++
                    }
                    if (characterIndex < text.length) {
                        linesToBeWrapped.addFirst(text.substring(characterIndex))
                    }
                }
            }
        }
        return result
    }

    private fun mapCodesToIntArray(codes: Array<String>): IntArray {
        val result = IntArray(codes.size)
        for (i in codes.indices) {
            val code = codes[i]
            if (code.isEmpty()) {
                result[i] = 0
            } else {
                try {
                    result[i] = code.toInt()
                } catch (_: NumberFormatException) {
                    throw IllegalArgumentException("Unknown CSI code $code")
                }
            }
        }
        return result
    }

    fun updateModifiersFromCSICode(
        controlSequence: String,
        target: StyleSet<*>?,
        original: StyleSet<*>?,
    ) {
        if (target == null || original == null || controlSequence.length < 3) {
            return
        }
        val controlCodeType = controlSequence[controlSequence.length - 1]
        val payload = controlSequence.substring(2, controlSequence.length - 1)
        val codes = mapCodesToIntArray(payload.split(";").toTypedArray())

        val palette = TextColor.ANSI.values()
        if (controlCodeType == 'm') {
            var i = 0
            while (i < codes.size) {
                val code = codes[i]
                when (code) {
                    0 -> target.setStyleFrom(original)
                    1 -> target.enableModifiers(SGR.BOLD)
                    3 -> target.enableModifiers(SGR.ITALIC)
                    4 -> target.enableModifiers(SGR.UNDERLINE)
                    5 -> target.enableModifiers(SGR.BLINK)
                    7 -> target.enableModifiers(SGR.REVERSE)
                    21, 22 -> target.disableModifiers(SGR.BOLD)
                    23 -> target.disableModifiers(SGR.ITALIC)
                    24 -> target.disableModifiers(SGR.UNDERLINE)
                    25 -> target.disableModifiers(SGR.BLINK)
                    27 -> target.disableModifiers(SGR.REVERSE)
                    38 -> {
                        if (i + 2 < codes.size && codes[i + 1] == 5) {
                            target.setForegroundColor(TextColor.Indexed(codes[i + 2]))
                            i += 2
                        } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                            target.setForegroundColor(TextColor.RGB(codes[i + 2], codes[i + 3], codes[i + 4]))
                            i += 4
                        }
                    }
                    39 -> target.setForegroundColor(original.foregroundColor)
                    48 -> {
                        if (i + 2 < codes.size && codes[i + 1] == 5) {
                            target.setBackgroundColor(TextColor.Indexed(codes[i + 2]))
                            i += 2
                        } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                            target.setBackgroundColor(TextColor.RGB(codes[i + 2], codes[i + 3], codes[i + 4]))
                            i += 4
                        }
                    }
                    49 -> target.setBackgroundColor(original.backgroundColor)
                    else -> {
                        if (code in 30..37) {
                            target.setForegroundColor(palette[code - 30])
                        } else if (code in 40..47) {
                            target.setBackgroundColor(palette[code - 40])
                        }
                    }
                }
                i++
            }
        }
    }
}
