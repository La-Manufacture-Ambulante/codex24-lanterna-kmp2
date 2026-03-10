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

import java.io.Serializable
import java.text.BreakIterator
import kotlin.collections.ArrayList
import java.util.Arrays
import java.util.EnumSet

/**
 * Represents a single character with additional metadata such as colors and modifiers.
 */
class TextCharacter private constructor(
    val characterString: String,
    val foregroundColor: TextColor,
    val backgroundColor: TextColor,
    private val modifiers: EnumSet<SGR>,
) : Serializable {

    @Deprecated("This won't work with advanced characters like emoji")
    val character: Char
        get() = characterString[0]

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
            TerminalTextUtils.isCharDoubleWidth(characterString[0]) ||
                isEmoji(characterString) ||
                (characterString.length > 1 && !TerminalTextUtils.isCharThai(characterString[0]))
            )

    @Deprecated("Use fromCharacter instead")
    constructor(character: Char) : this(
        Character.toString(character),
        TextColor.ANSI.DEFAULT,
        TextColor.ANSI.DEFAULT,
        EnumSet.noneOf(SGR::class.java),
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
        Character.toString(character),
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
        Character.toString(character),
        foregroundColor ?: TextColor.ANSI.DEFAULT,
        backgroundColor ?: TextColor.ANSI.DEFAULT,
        modifiers?.let { EnumSet.copyOf(it) } ?: EnumSet.noneOf(SGR::class.java),
    )

    init {
        if (characterString.isEmpty()) {
            throw IllegalArgumentException("Cannot create TextCharacter from an empty string")
        }
        validateSingleCharacter(characterString)

        val firstCharacter = characterString[0]
        if (TerminalTextUtils.isControlCharacter(firstCharacter) && firstCharacter != '\t') {
            throw IllegalArgumentException(
                "Cannot create a TextCharacter from a control character (0x${firstCharacter.code.toString(16)})",
            )
        }
    }

    private fun validateSingleCharacter(character: String) {
        val breakIterator = BreakIterator.getCharacterInstance()
        breakIterator.setText(character)
        var seen = false
        var begin = 0
        var end = breakIterator.next()
        while (end != BreakIterator.DONE) {
            if (seen) {
                throw IllegalArgumentException("Invalid String for TextCharacter, can only have one logical character")
            }
            seen = true
            begin = end
            end = breakIterator.next()
        }
        if (!seen) {
            throw IllegalArgumentException("Invalid String for TextCharacter, can only have one logical character")
        }
    }

    fun `is`(otherCharacter: Char): Boolean {
        return otherCharacter == characterString[0] && characterString.length == 1
    }

    fun getModifiers(): EnumSet<SGR> = EnumSet.copyOf(modifiers)

    fun withCharacter(character: Char): TextCharacter {
        if (characterString == Character.toString(character)) {
            return this
        }
        return TextCharacter(Character.toString(character), foregroundColor, backgroundColor, EnumSet.copyOf(modifiers))
    }

    fun withForegroundColor(foregroundColor: TextColor?): TextCharacter {
        val fg = foregroundColor ?: TextColor.ANSI.DEFAULT
        if (this.foregroundColor == fg) {
            return this
        }
        return TextCharacter(characterString, fg, backgroundColor, EnumSet.copyOf(modifiers))
    }

    fun withBackgroundColor(backgroundColor: TextColor?): TextCharacter {
        val bg = backgroundColor ?: TextColor.ANSI.DEFAULT
        if (this.backgroundColor == bg) {
            return this
        }
        return TextCharacter(characterString, foregroundColor, bg, EnumSet.copyOf(modifiers))
    }

    fun withModifiers(modifiers: Collection<SGR?>?): TextCharacter {
        val newSet = toEnumSetFromCollection(modifiers)
        if (this.modifiers == newSet) {
            return this
        }
        return TextCharacter(characterString, foregroundColor, backgroundColor, newSet)
    }

    fun withModifier(modifier: SGR?): TextCharacter {
        if (modifier == null || modifiers.contains(modifier)) {
            return this
        }
        val newSet = EnumSet.copyOf(modifiers)
        newSet.add(modifier)
        return TextCharacter(characterString, foregroundColor, backgroundColor, newSet)
    }

    fun withoutModifier(modifier: SGR?): TextCharacter {
        if (modifier == null || !modifiers.contains(modifier)) {
            return this
        }
        val newSet = EnumSet.copyOf(modifiers)
        newSet.remove(modifier)
        return TextCharacter(characterString, foregroundColor, backgroundColor, newSet)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TextCharacter) {
            return false
        }
        return characterString == other.characterString &&
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
        return "TextCharacter{character=$characterString, foregroundColor=$foregroundColor, backgroundColor=$backgroundColor, modifiers=$modifiers}"
    }

    companion object {
        private fun toEnumSet(vararg modifiers: SGR?): EnumSet<SGR> {
            val result = EnumSet.noneOf(SGR::class.java)
            for (modifier in modifiers) {
                if (modifier != null) {
                    result.add(modifier)
                }
            }
            return result
        }

        private fun toEnumSetFromCollection(modifiers: Collection<SGR?>?): EnumSet<SGR> {
            val result = EnumSet.noneOf(SGR::class.java)
            if (modifiers != null) {
                for (modifier in modifiers) {
                    if (modifier != null) {
                        result.add(modifier)
                    }
                }
            }
            return result
        }

        val DEFAULT_CHARACTER = TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

        fun fromCharacter(c: Char): Array<TextCharacter?>? = fromString(Character.toString(c))

        fun fromString(string: String?): Array<TextCharacter?>? = fromString(string, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

        fun fromCharacter(
            c: Char,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            vararg modifiers: SGR?,
        ): TextCharacter? {
            return fromString(Character.toString(c), foregroundColor, backgroundColor, *modifiers)?.get(0)
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
            val mods = modifiers?.let { EnumSet.copyOf(it) } ?: EnumSet.noneOf(SGR::class.java)

            val breakIterator = BreakIterator.getCharacterInstance()
            breakIterator.setText(text)
            val result = ArrayList<TextCharacter?>()
            var begin = 0
            var end = breakIterator.next()
            while (end != BreakIterator.DONE) {
                result.add(TextCharacter(text.substring(begin, end), fg, bg, EnumSet.copyOf(mods)))
                begin = end
                end = breakIterator.next()
            }
            return result.toTypedArray()
        }

        private fun isEmoji(s: String): Boolean {
            val firstCharacter = s[0]
            return (
                s.length > 1 ||
                    !(
                        TerminalTextUtils.isCharCJK(firstCharacter) ||
                            TerminalTextUtils.isPrintableCharacter(firstCharacter) ||
                            TerminalTextUtils.isCharThai(firstCharacter) ||
                            TerminalTextUtils.isCharCJK(firstCharacter) ||
                            TerminalTextUtils.isControlCharacter(firstCharacter)
                        )
                )
        }
    }
}
