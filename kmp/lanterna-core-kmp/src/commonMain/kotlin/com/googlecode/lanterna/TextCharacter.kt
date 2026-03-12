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
package com.googlecode.lanterna

import com.googlecode.lanterna.internal.compat.Character
import com.googlecode.lanterna.internal.compat.EnumSet
import kotlin.collections.ArrayList

/**
 * Represents a single character with additional metadata such as colors and modifiers.
 * This class is immutable and cannot be modified after creation.
 *
 * @author Martin
 */
class TextCharacter private constructor(
    val characterString: String,
    val foregroundColor: TextColor,
    val backgroundColor: TextColor,
    private val modifiers: EnumSet<SGR>,
) {
    @Deprecated("This won't work with advanced characters like emoji")
    val character: Char
        get() = characterString.first()

    val isBold: Boolean
        get() = modifiers.contains(SGR.BOLD)

    val isReversed: Boolean
        get() = modifiers.contains(SGR.REVERSE)

    val isUnderlined: Boolean
        get() = modifiers.contains(SGR.UNDERLINE)

    val isBlinking: Boolean
        get() = modifiers.contains(SGR.BLINK)

    val isBordered: Boolean
        get() = modifiers.contains(SGR.BORDERED)

    val isCrossedOut: Boolean
        get() = modifiers.contains(SGR.CROSSED_OUT)

    val isItalic: Boolean
        get() = modifiers.contains(SGR.ITALIC)

    val isDoubleWidth: Boolean
        get() = (
            TerminalTextUtils.isCharDoubleWidth(characterString.first()) ||
                isEmoji(characterString) ||
                (characterString.length > 1 && !TerminalTextUtils.isCharThai(characterString.first()))
        )

    @Deprecated("Use fromCharacter instead")
    constructor(character: Char) : this(
        com.googlecode.lanterna.internal.compat.Character.toString(character),
        TextColor.ANSI.DEFAULT,
        TextColor.ANSI.DEFAULT,
        EnumSet.noneOf(SGR::class),
    )

    @Deprecated("TextCharacters are immutable so you shouldn't need to call this")
    constructor(character: TextCharacter) : this(
        character.characterString,
        character.foregroundColor,
        character.backgroundColor,
        EnumSet.copyOf(character.modifiers),
    )

    @Deprecated("Use fromCharacter instead")
    constructor(
        character: Char,
        foregroundColor: TextColor?,
        backgroundColor: TextColor?,
        vararg styles: SGR?,
    ) : this(
        com.googlecode.lanterna.internal.compat.Character.toString(character),
        foregroundColor ?: TextColor.ANSI.DEFAULT,
        backgroundColor ?: TextColor.ANSI.DEFAULT,
        toEnumSet(*styles),
    )

    @Deprecated("Use fromCharacter instead")
    constructor(
        character: Char,
        foregroundColor: TextColor?,
        backgroundColor: TextColor?,
        modifiers: EnumSet<SGR>?,
    ) : this(
        com.googlecode.lanterna.internal.compat.Character.toString(character),
        foregroundColor ?: TextColor.ANSI.DEFAULT,
        backgroundColor ?: TextColor.ANSI.DEFAULT,
        modifiers?.let { EnumSet.copyOf(it) } ?: EnumSet.noneOf(SGR::class),
    )

    init {
        require(characterString.isNotEmpty()) { "Cannot create TextCharacter from an empty string" }
        validateSingleCharacter(characterString)
        val first = characterString.first()
        require(!TerminalTextUtils.isControlCharacter(first) || first == '\t') {
            "Cannot create TextCharacter from control character 0x${first.code.toString(16)}"
        }
    }

    @Suppress("ktlint:standard:function-naming")
    fun `is`(otherCharacter: Char): Boolean {
        return characterString.length == 1 && characterString[0] == otherCharacter
    }

    fun getModifiers(): EnumSet<SGR> = EnumSet.copyOf(modifiers)

    fun withCharacter(character: Char): TextCharacter {
        val resolved = com.googlecode.lanterna.internal.compat.Character.toString(character)
        if (characterString == resolved) {
            return this
        }
        return TextCharacter(resolved, foregroundColor, backgroundColor, EnumSet.copyOf(modifiers))
    }

    fun withForegroundColor(foregroundColor: TextColor?): TextCharacter {
        val resolved = foregroundColor ?: TextColor.ANSI.DEFAULT
        if (this.foregroundColor == resolved) {
            return this
        }
        return TextCharacter(characterString, resolved, backgroundColor, EnumSet.copyOf(modifiers))
    }

    fun withBackgroundColor(backgroundColor: TextColor?): TextCharacter {
        val resolved = backgroundColor ?: TextColor.ANSI.DEFAULT
        if (this.backgroundColor == resolved) {
            return this
        }
        return TextCharacter(characterString, foregroundColor, resolved, EnumSet.copyOf(modifiers))
    }

    fun withModifiers(modifiers: Collection<SGR?>?): TextCharacter {
        val resolved = toEnumSetFromCollection(modifiers)
        if (this.modifiers == resolved) {
            return this
        }
        return TextCharacter(characterString, foregroundColor, backgroundColor, resolved)
    }

    fun withModifier(modifier: SGR?): TextCharacter {
        if (modifier == null || modifiers.contains(modifier)) {
            return this
        }
        val resolved = EnumSet.copyOf(modifiers)
        resolved.add(modifier)
        return TextCharacter(characterString, foregroundColor, backgroundColor, resolved)
    }

    fun withoutModifier(modifier: SGR?): TextCharacter {
        if (modifier == null || !modifiers.contains(modifier)) {
            return this
        }
        val resolved = EnumSet.copyOf(modifiers)
        resolved.remove(modifier)
        return TextCharacter(characterString, foregroundColor, backgroundColor, resolved)
    }

    override fun equals(other: Any?): Boolean {
        return other is TextCharacter &&
            characterString == other.characterString &&
            foregroundColor == other.foregroundColor &&
            backgroundColor == other.backgroundColor &&
            modifiers == other.modifiers
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 37 * hash + characterString.hashCode()
        hash = 37 * hash + foregroundColor.hashCode()
        hash = 37 * hash + backgroundColor.hashCode()
        hash = 37 * hash + modifiers.hashCode()
        return hash
    }

    override fun toString(): String {
        return "TextCharacter{character=$characterString, foregroundColor=$foregroundColor, " +
            "backgroundColor=$backgroundColor, modifiers=$modifiers}"
    }

    companion object {
        val DEFAULT_CHARACTER = TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

        fun fromCharacter(c: Char): Array<TextCharacter?>? {
            return fromString(com.googlecode.lanterna.internal.compat.Character.toString(c))
        }

        fun fromString(string: String?): Array<TextCharacter?>? {
            return fromString(string, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)
        }

        fun fromCharacter(
            c: Char,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            vararg modifiers: SGR?,
        ): TextCharacter? {
            return fromString(
                com.googlecode.lanterna.internal.compat.Character.toString(c),
                foregroundColor,
                backgroundColor,
                *modifiers,
            )?.firstOrNull()
        }

        fun fromString(
            string: String?,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            vararg modifiers: SGR?,
        ): Array<TextCharacter?>? {
            return fromString(string, foregroundColor, backgroundColor, toEnumSet(*modifiers))
        }

        fun fromString(
            string: String?,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            modifiers: EnumSet<SGR>?,
        ): Array<TextCharacter?>? {
            val text = string ?: return emptyArray()
            val fg = foregroundColor ?: TextColor.ANSI.DEFAULT
            val bg = backgroundColor ?: TextColor.ANSI.DEFAULT
            val sgr = modifiers?.let { EnumSet.copyOf(it) } ?: EnumSet.noneOf(SGR::class)

            val result = ArrayList<TextCharacter?>()
            for (cluster in splitDisplayClusters(text)) {
                result.add(TextCharacter(cluster, fg, bg, EnumSet.copyOf(sgr)))
            }
            return result.toTypedArray()
        }

        private fun validateSingleCharacter(character: String) {
            if (splitDisplayClusters(character).size != 1) {
                throw IllegalArgumentException("Invalid String for TextCharacter, can only have one logical character")
            }
        }

        private fun isEmoji(value: String): Boolean {
            // Mirrors the Java heuristic while keeping common-safe behavior.
            val firstCharacter = value[0]
            return value.length > 1 ||
                !(
                    TerminalTextUtils.isCharCJK(firstCharacter) ||
                        TerminalTextUtils.isPrintableCharacter(firstCharacter) ||
                        TerminalTextUtils.isCharThai(firstCharacter) ||
                        TerminalTextUtils.isCharCJK(firstCharacter) ||
                        TerminalTextUtils.isControlCharacter(firstCharacter)
                )
        }

        // Kotlin/Native common code has no BreakIterator, so this mirrors Java's logical-character split semantics.
        private fun splitDisplayClusters(text: String): List<String> {
            if (text.isEmpty()) {
                return emptyList()
            }
            val result = ArrayList<String>()
            var index = 0
            while (index < text.length) {
                val cluster = StringBuilder()
                var slice = readCodePointSlice(text, index)
                cluster.append(slice.value)
                index = slice.nextIndex
                while (index < text.length) {
                    val nextCodePoint = peekCodePoint(text, index)
                    if (nextCodePoint == null || !shouldJoinCluster(nextCodePoint)) {
                        break
                    }
                    if (nextCodePoint == 0x200D) {
                        slice = readCodePointSlice(text, index)
                        cluster.append(slice.value)
                        index = slice.nextIndex
                        if (index < text.length) {
                            slice = readCodePointSlice(text, index)
                            cluster.append(slice.value)
                            index = slice.nextIndex
                        }
                        continue
                    }
                    slice = readCodePointSlice(text, index)
                    cluster.append(slice.value)
                    index = slice.nextIndex
                }
                result.add(cluster.toString())
            }
            return result
        }

        private fun shouldJoinCluster(codePoint: Int): Boolean {
            if (codePoint == 0x200D) {
                return true
            }
            if (codePoint in 0x1F3FB..0x1F3FF) {
                return true // emoji skin tone modifiers
            }
            if (codePoint in 0xFE00..0xFE0F) {
                return true // variation selectors
            }
            if (codePoint in 0x0300..0x036F ||
                codePoint in 0x1AB0..0x1AFF ||
                codePoint in 0x1DC0..0x1DFF ||
                codePoint in 0x20D0..0x20FF ||
                codePoint in 0xFE20..0xFE2F ||
                codePoint in 0x0E31..0x0E4E
            ) {
                return true // combining marks (including Thai mark range used by existing tests)
            }
            return false
        }

        private fun peekCodePoint(
            text: String,
            index: Int,
        ): Int? {
            if (index >= text.length) {
                return null
            }
            val first = text[index]
            if (first.isHighSurrogate() && index + 1 < text.length) {
                val second = text[index + 1]
                if (second.isLowSurrogate()) {
                    val high = first.code - 0xD800
                    val low = second.code - 0xDC00
                    return 0x10000 + (high shl 10) + low
                }
            }
            return first.code
        }

        private data class CodePointSlice(
            val value: String,
            val nextIndex: Int,
        )

        private fun readCodePointSlice(
            text: String,
            index: Int,
        ): CodePointSlice {
            if (index >= text.length) {
                return CodePointSlice("", index)
            }
            val first = text[index]
            if (first.isHighSurrogate() && index + 1 < text.length) {
                val second = text[index + 1]
                if (second.isLowSurrogate()) {
                    return CodePointSlice(
                        buildString(2) {
                            append(first)
                            append(second)
                        },
                        index + 2,
                    )
                }
            }
            return CodePointSlice(first.toString(), index + 1)
        }

        private fun toEnumSet(vararg modifiers: SGR?): EnumSet<SGR> {
            val resolved = EnumSet.noneOf(SGR::class)
            modifiers.filterNotNull().forEach { resolved.add(it) }
            return resolved
        }

        private fun toEnumSetFromCollection(modifiers: Collection<SGR?>?): EnumSet<SGR> {
            val resolved = EnumSet.noneOf(SGR::class)
            modifiers.orEmpty().filterNotNull().forEach { resolved.add(it) }
            return resolved
        }
    }
}
