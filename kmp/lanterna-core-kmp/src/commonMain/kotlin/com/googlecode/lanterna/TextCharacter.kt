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
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.EnumSet
import java.util.Objects

/**
 * Represents a single character with additional metadata such as colors and modifiers. This class is immutable and
 * cannot be modified after creation.
 * @author Martin
 */
class TextCharacter private constructor(
    character: String,
    foregroundColor: TextColor?,
    backgroundColor: TextColor?,
    modifiers: EnumSet<SGR>?
) : Serializable {

    /**
     * The "character" might not fit in a Java 16-bit char (emoji and other types) so we store it in a String
     * as of 3.1 instead.
     */
    private val character: String
    private val foregroundColor: TextColor
    private val backgroundColor: TextColor
    private val modifiers: EnumSet<SGR> //This isn't immutable, but we should treat it as such and not expose it!

    init {
        if (character.isEmpty()) {
            throw IllegalArgumentException("Cannot create TextCharacter from an empty string")
        }
        validateSingleCharacter(character)

        // intern the string so we don't waste more memory than necessary
        this.character = character.intern()
        val firstCharacter = character[0]

        // Don't allow creating a TextCharacter containing a control character
        // For backward-compatibility, do allow tab for now
        if (TerminalTextUtils.isControlCharacter(firstCharacter) && firstCharacter != '\t') {
            throw IllegalArgumentException(
                "Cannot create a TextCharacter from a control character (0x" + Integer.toHexString(firstCharacter.code) + ")"
            )
        }

        var fg = foregroundColor
        var bg = backgroundColor
        if (fg == null) {
            fg = TextColor.ANSI.DEFAULT
        }
        if (bg == null) {
            bg = TextColor.ANSI.DEFAULT
        }

        this.foregroundColor = fg
        this.backgroundColor = bg
        this.modifiers = EnumSet.copyOf(modifiers ?: throw NullPointerException())
    }

    /**
     * Creates a {@code ScreenCharacter} based on a supplied character, with default colors and no extra modifiers.
     * @param character Physical character to use
     * @deprecated Use fromCharacter instead
     */
    @Deprecated("")
    constructor(character: Char) : this(character, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

    /**
     * Copies another {@code ScreenCharacter}
     * @param character screenCharacter to copy from
     * @deprecated TextCharacters are immutable so you shouldn't need to call this
     */
    @Deprecated("")
    constructor(character: TextCharacter) : this(
        character.getCharacterString(),
        character.getForegroundColor(),
        character.getBackgroundColor(),
        EnumSet.copyOf(character.getModifiers())
    )

    /**
     * Creates a new {@code ScreenCharacter} based on a physical character, color information and optional modifiers.
     * @param character Physical character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param styles Optional list of modifiers to apply when drawing the character
     * @deprecated Use fromCharacter instead
     */
    @Deprecated("")
    constructor(
        character: Char,
        foregroundColor: TextColor?,
        backgroundColor: TextColor?,
        vararg styles: SGR?
    ) : this(character, foregroundColor, backgroundColor, toEnumSet(*styles))

    /**
     * Creates a new {@code ScreenCharacter} based on a physical character, color information and a set of modifiers.
     * @param character Physical character to refer to
     * @param foregroundColor Foreground color the character has
     * @param backgroundColor Background color the character has
     * @param modifiers Set of modifiers to apply when drawing the character
     * @deprecated Use fromCharacter instead
     */
    @Deprecated("")
    constructor(
        character: Char,
        foregroundColor: TextColor?,
        backgroundColor: TextColor?,
        modifiers: EnumSet<SGR>?
    ) : this(character.toString(), foregroundColor, backgroundColor, modifiers)

    private fun validateSingleCharacter(character: String) {
        val breakIterator = BreakIterator.getCharacterInstance()
        breakIterator.setText(character)
        var firstCharacter: String? = null
        var begin = 0
        var end: Int
        while (breakIterator.next().also { end = it } != BreakIterator.DONE) {
            if (firstCharacter == null) {
                firstCharacter = character.substring(begin, end)
            } else {
                throw IllegalArgumentException("Invalid String for TextCharacter, can only have one logical character")
            }
            begin = breakIterator.current()
        }
    }

    fun `is`(otherCharacter: Char): Boolean {
        return otherCharacter == character[0] && character.length == 1
    }

    /**
     * The actual character this TextCharacter represents
     * @return character of the TextCharacter
     * @deprecated This won't work with advanced characters like emoji
     */
    @Deprecated("")
    fun getCharacter(): Char {
        return character[0]
    }

    /**
     * Returns the character this TextCharacter represents as a String. This is not returning a char
     * @return
     */
    fun getCharacterString(): String {
        return character
    }

    /**
     * Foreground color specified for this TextCharacter
     * @return Foreground color of this TextCharacter
     */
    fun getForegroundColor(): TextColor {
        return foregroundColor
    }

    /**
     * Background color specified for this TextCharacter
     * @return Background color of this TextCharacter
     */
    fun getBackgroundColor(): TextColor {
        return backgroundColor
    }

    /**
     * Returns a set of all active modifiers on this TextCharacter
     * @return Set of active SGR codes
     */
    fun getModifiers(): EnumSet<SGR> {
        return EnumSet.copyOf(modifiers)
    }

    /**
     * Returns true if this TextCharacter has the bold modifier active
     * @return {@code true} if this TextCharacter has the bold modifier active
     */
    fun isBold(): Boolean {
        return modifiers.contains(SGR.BOLD)
    }

    /**
     * Returns true if this TextCharacter has the reverse modifier active
     * @return {@code true} if this TextCharacter has the reverse modifier active
     */
    fun isReversed(): Boolean {
        return modifiers.contains(SGR.REVERSE)
    }

    /**
     * Returns true if this TextCharacter has the underline modifier active
     * @return {@code true} if this TextCharacter has the underline modifier active
     */
    fun isUnderlined(): Boolean {
        return modifiers.contains(SGR.UNDERLINE)
    }

    /**
     * Returns true if this TextCharacter has the blink modifier active
     * @return {@code true} if this TextCharacter has the blink modifier active
     */
    fun isBlinking(): Boolean {
        return modifiers.contains(SGR.BLINK)
    }

    /**
     * Returns true if this TextCharacter has the bordered modifier active
     * @return {@code true} if this TextCharacter has the bordered modifier active
     */
    fun isBordered(): Boolean {
        return modifiers.contains(SGR.BORDERED)
    }

    /**
     * Returns true if this TextCharacter has the crossed-out modifier active
     * @return {@code true} if this TextCharacter has the crossed-out modifier active
     */
    fun isCrossedOut(): Boolean {
        return modifiers.contains(SGR.CROSSED_OUT)
    }

    /**
     * Returns true if this TextCharacter has the italic modifier active
     * @return {@code true} if this TextCharacter has the italic modifier active
     */
    fun isItalic(): Boolean {
        return modifiers.contains(SGR.ITALIC)
    }

    /**
     * Returns a new TextCharacter with the same colors and modifiers but a different underlying character
     * @param character Character the copy should have
     * @return Copy of this TextCharacter with different underlying character
     */
    fun withCharacter(character: Char): TextCharacter {
        if (this.character == character.toString()) {
            return this
        }
        return TextCharacter(character, foregroundColor, backgroundColor, modifiers)
    }

    /**
     * Returns a copy of this TextCharacter with a specified foreground color
     * @param foregroundColor Foreground color the copy should have
     * @return Copy of the TextCharacter with a different foreground color
     */
    fun withForegroundColor(foregroundColor: TextColor?): TextCharacter {
        if (this.foregroundColor == foregroundColor || this.foregroundColor.equals(foregroundColor)) {
            return this
        }
        return TextCharacter(character, foregroundColor, backgroundColor, modifiers)
    }

    /**
     * Returns a copy of this TextCharacter with a specified background color
     * @param backgroundColor Background color the copy should have
     * @return Copy of the TextCharacter with a different background color
     */
    fun withBackgroundColor(backgroundColor: TextColor?): TextCharacter {
        if (this.backgroundColor == backgroundColor || this.backgroundColor.equals(backgroundColor)) {
            return this
        }
        return TextCharacter(character, foregroundColor, backgroundColor, modifiers)
    }

    /**
     * Returns a copy of this TextCharacter with specified list of SGR modifiers. None of the currently active SGR codes
     * will be carried over to the copy, only those in the passed in value.
     * @param modifiers SGR modifiers the copy should have
     * @return Copy of the TextCharacter with a different set of SGR modifiers
     */
    fun withModifiers(modifiers: Collection<SGR>?): TextCharacter {
        val newSet = EnumSet.copyOf(modifiers ?: throw NullPointerException())
        if (modifiers.equals(newSet)) {
            return this
        }
        return TextCharacter(character, foregroundColor, backgroundColor, newSet)
    }

    /**
     * Returns a copy of this TextCharacter with an additional SGR modifier. All of the currently active SGR codes
     * will be carried over to the copy, in addition to the one specified.
     * @param modifier SGR modifiers the copy should have in additional to all currently present
     * @return Copy of the TextCharacter with a new SGR modifier
     */
    fun withModifier(modifier: SGR?): TextCharacter {
        if (modifiers.contains(modifier)) {
            return this
        }
        val newSet = EnumSet.copyOf(this.modifiers)
        newSet.add(modifier)
        return TextCharacter(character, foregroundColor, backgroundColor, newSet)
    }

    /**
     * Returns a copy of this TextCharacter with an SGR modifier removed. All of the currently active SGR codes
     * will be carried over to the copy, except for the one specified. If the current TextCharacter doesn't have the
     * SGR specified, it will return itself.
     * @param modifier SGR modifiers the copy should not have
     * @return Copy of the TextCharacter without the SGR modifier
     */
    fun withoutModifier(modifier: SGR?): TextCharacter {
        if (!modifiers.contains(modifier)) {
            return this
        }
        val newSet = EnumSet.copyOf(this.modifiers)
        newSet.remove(modifier)
        return TextCharacter(character, foregroundColor, backgroundColor, newSet)
    }

    fun isDoubleWidth(): Boolean {
        // TODO: make this better to work properly with emoji and other complicated "characters"
        return TerminalTextUtils.isCharDoubleWidth(character[0]) ||
            isEmoji(character) ||
            // If the character takes up more than one char, assume it's double width (unless thai)
            (character.length > 1 && !TerminalTextUtils.isCharThai(character[0]))
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as TextCharacter
        if (!Objects.equals(this.character, other.character)) {
            return false
        }
        if (!Objects.equals(this.foregroundColor, other.foregroundColor)) {
            return false
        }
        if (!Objects.equals(this.backgroundColor, other.backgroundColor)) {
            return false
        }
        return Objects.equals(this.modifiers, other.modifiers)
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 37 * hash + this.character.hashCode()
        hash = 37 * hash + if (this.foregroundColor != null) this.foregroundColor.hashCode() else 0
        hash = 37 * hash + if (this.backgroundColor != null) this.backgroundColor.hashCode() else 0
        hash = 37 * hash + if (this.modifiers != null) this.modifiers.hashCode() else 0
        return hash
    }

    override fun toString(): String {
        return "TextCharacter{character=$character, foregroundColor=$foregroundColor, backgroundColor=$backgroundColor, modifiers=$modifiers}"
    }

    companion object {
        private fun toEnumSet(vararg modifiers: SGR?): EnumSet<SGR> {
            return if (modifiers.isEmpty()) {
                EnumSet.noneOf(SGR::class.java)
            } else {
                @Suppress("UNCHECKED_CAST")
                EnumSet.copyOf(Arrays.asList(*modifiers) as Collection<SGR>)
            }
        }

        @JvmField
        val DEFAULT_CHARACTER: TextCharacter =
            TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

        @JvmStatic
        fun fromCharacter(c: Char): Array<TextCharacter> {
            return fromString(c.toString())
        }

        @JvmStatic
        fun fromString(string: String?): Array<TextCharacter> {
            return fromString(string, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)
        }

        @JvmStatic
        fun fromCharacter(
            c: Char,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            vararg modifiers: SGR?
        ): TextCharacter {
            return fromString(c.toString(), foregroundColor, backgroundColor, *modifiers)[0]
        }

        @JvmStatic
        fun fromString(
            string: String?,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            vararg modifiers: SGR?
        ): Array<TextCharacter> {
            return fromString(string, foregroundColor, backgroundColor, toEnumSet(*modifiers))
        }

        @JvmStatic
        fun fromString(
            string: String?,
            foregroundColor: TextColor?,
            backgroundColor: TextColor?,
            modifiers: EnumSet<SGR>?
        ): Array<TextCharacter> {
            val breakIterator = BreakIterator.getCharacterInstance()
            breakIterator.setText(string ?: throw NullPointerException())
            val result: MutableList<TextCharacter> = ArrayList()
            var begin = 0
            var end: Int
            while (breakIterator.next().also { end = it } != BreakIterator.DONE) {
                result.add(
                    TextCharacter(
                        (string).substring(begin, end),
                        foregroundColor,
                        backgroundColor,
                        modifiers
                    )
                )
                begin = breakIterator.current()
            }
            return result.toTypedArray()
        }

        private fun isEmoji(s: String): Boolean {
            // This is really hard to do properly and would require an emoji library as a dependency, so here's a hack that
            // basically assumes anything NOT a regular latin1/CJK/thai character is an emoji
            val firstCharacter = s[0]
            return s.length > 1 ||
                !(TerminalTextUtils.isCharCJK(firstCharacter) ||
                    TerminalTextUtils.isPrintableCharacter(firstCharacter) ||
                    TerminalTextUtils.isCharThai(firstCharacter) ||
                    TerminalTextUtils.isCharCJK(firstCharacter) ||
                    TerminalTextUtils.isControlCharacter(firstCharacter))
        }
    }
}
