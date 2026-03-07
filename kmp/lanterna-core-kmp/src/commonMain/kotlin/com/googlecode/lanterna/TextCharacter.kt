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
import java.util.EnumSet
import java.util.Objects

/**
 * Represents a single character with additional metadata such as colors and modifiers. This class is immutable and
 * cannot be modified after creation.
 * @author Martin
 */
 class TextCharacter:Serializable {

/**
 * The "character" might not fit in a Java 16-bit char (emoji and other types) so we store it in a String
 * as of 3.1 instead.
 */
    /**
 * Returns the character this TextCharacter represents as a String. This is not returning a char
 * @return
 */
     val characterString:String?
/**
 * Foreground color specified for this TextCharacter
 * @return Foreground color of this TextCharacter
 */
     val foregroundColor:TextColor?
/**
 * Background color specified for this TextCharacter
 * @return Background color of this TextCharacter
 */
     val backgroundColor:TextColor?
private val modifiers:EnumSet<SGR?>?  //This isn't immutable, but we should treat it as such and not expose it!

/**
 * The actual character this TextCharacter represents
 * @return character of the TextCharacter
 */
     val character:Char
@Deprecated("This won't work with advanced characters like emoji")
get() {
return characterString!!.charAt(0)
}

/**
 * Returns true if this TextCharacter has the bold modifier active
 * @return `true` if this TextCharacter has the bold modifier active
 */
     val isBold:Boolean
get() {
return modifiers!!.contains(SGR.BOLD)
}

/**
 * Returns true if this TextCharacter has the reverse modifier active
 * @return `true` if this TextCharacter has the reverse modifier active
 */
     val isReversed:Boolean
get() {
return modifiers!!.contains(SGR.REVERSE)
}

/**
 * Returns true if this TextCharacter has the underline modifier active
 * @return `true` if this TextCharacter has the underline modifier active
 */
     val isUnderlined:Boolean
get() {
return modifiers!!.contains(SGR.UNDERLINE)
}

/**
 * Returns true if this TextCharacter has the blink modifier active
 * @return `true` if this TextCharacter has the blink modifier active
 */
     val isBlinking:Boolean
get() {
return modifiers!!.contains(SGR.BLINK)
}

/**
 * Returns true if this TextCharacter has the bordered modifier active
 * @return `true` if this TextCharacter has the bordered modifier active
 */
     val isBordered:Boolean
get() {
return modifiers!!.contains(SGR.BORDERED)
}

/**
 * Returns true if this TextCharacter has the crossed-out modifier active
 * @return `true` if this TextCharacter has the crossed-out modifier active
 */
     val isCrossedOut:Boolean
get() {
return modifiers!!.contains(SGR.CROSSED_OUT)
}

/**
 * Returns true if this TextCharacter has the italic modifier active
 * @return `true` if this TextCharacter has the italic modifier active
 */
     val isItalic:Boolean
get() {
return modifiers!!.contains(SGR.ITALIC)
}

 // TODO: make this better to work properly with emoji and other complicated "characters"
 // If the character takes up more than one char, assume it's double width (unless thai)
 val isDoubleWidth:Boolean
get() {
return (TerminalTextUtils.isCharDoubleWidth(characterString!!.charAt(0)) || 
isEmoji(characterString!!) || 
(characterString!!.length() > 1 && !TerminalTextUtils.isCharThai(characterString!!.charAt(0))))
}

/**
 * Creates a `ScreenCharacter` based on a supplied character, with default colors and no extra modifiers.
 * @param character Physical character to use
 */
    @Deprecated("Use fromCharacter instead")
 constructor(character:Char) : this(character, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT) {}

/**
 * Copies another `ScreenCharacter`
 * @param character screenCharacter to copy from
 */
    @Deprecated("TextCharacters are immutable so you shouldn't need to call this")
 constructor(character:TextCharacter) : this(character.characterString, 
character.foregroundColor, 
character.backgroundColor, 
EnumSet.copyOf(character.getModifiers())) {}

/**
 * Creates a new `ScreenCharacter` based on a physical character, color information and optional modifiers.
 * @param character Physical character to refer to
 * @param foregroundColor Foreground color the character has
 * @param backgroundColor Background color the character has
 * @param styles Optional list of modifiers to apply when drawing the character
 */
    @SuppressWarnings("WeakerAccess")
@Deprecated("Use fromCharacter instead")
 constructor(
character:Char, 
foregroundColor:TextColor?, 
backgroundColor:TextColor?, 
vararg styles:SGR?) : this(character, 
foregroundColor, 
backgroundColor, 
toEnumSet(*styles!!)) {}

/**
 * Creates a new `ScreenCharacter` based on a physical character, color information and a set of modifiers.
 * @param character Physical character to refer to
 * @param foregroundColor Foreground color the character has
 * @param backgroundColor Background color the character has
 * @param modifiers Set of modifiers to apply when drawing the character
 */
    @Deprecated("Use fromCharacter instead")
 constructor(
character:Char, 
foregroundColor:TextColor?, 
backgroundColor:TextColor?, 
modifiers:EnumSet<SGR?>?) : this(Character.toString(character), foregroundColor, backgroundColor, modifiers) {}

/**
 * Creates a new `ScreenCharacter` based on a physical character, color information and a set of modifiers.
 * @param character Physical character to refer to
 * @param foregroundColor Foreground color the character has
 * @param backgroundColor Background color the character has
 * @param modifiers Set of modifiers to apply when drawing the character
 */
    private constructor(
character:String, 
foregroundColor:TextColor?, 
backgroundColor:TextColor?, 
modifiers:EnumSet<SGR?>?) {
var foregroundColor = foregroundColor
var backgroundColor = backgroundColor

if (character.isEmpty())
{
throw IllegalArgumentException("Cannot create TextCharacter from an empty string")
}
validateSingleCharacter(character)

 // intern the string so we don't waste more memory than necessary
        this.characterString = character.intern()
val firstCharacter = character.charAt(0)

 // Don't allow creating a TextCharacter containing a control character
        // For backward-compatibility, do allow tab for now
        if (TerminalTextUtils.isControlCharacter(firstCharacter) && firstCharacter != '\t')
{
throw IllegalArgumentException("Cannot create a TextCharacter from a control character (0x" + Integer.toHexString(firstCharacter) + ")")
}

if (foregroundColor == null)
{
foregroundColor = TextColor.ANSI.DEFAULT
}
if (backgroundColor == null)
{
backgroundColor = TextColor.ANSI.DEFAULT
}

this.foregroundColor = foregroundColor
this.backgroundColor = backgroundColor
this.modifiers = EnumSet.copyOf(modifiers)
}

private fun validateSingleCharacter(character:String?) {
val breakIterator = BreakIterator.getCharacterInstance()
breakIterator!!.setText(character)
var firstCharacter:String? = null
var begin = 0
var end = 0
while ((end = breakIterator!!.next()) != BreakIterator.DONE)
{
if (firstCharacter == null)
{
firstCharacter = character!!.substring(begin, end)
}
else
{
throw IllegalArgumentException("Invalid String for TextCharacter, can only have one logical character")
}
begin = breakIterator!!.current()
}
}

 fun `is`(otherCharacter:Char):Boolean {
return otherCharacter == characterString!!.charAt(0) && characterString!!.length() === 1
}

/**
 * Returns a set of all active modifiers on this TextCharacter
 * @return Set of active SGR codes
 */
     fun getModifiers():EnumSet<SGR?>? {
return EnumSet.copyOf(modifiers)
}

/**
 * Returns a new TextCharacter with the same colors and modifiers but a different underlying character
 * @param character Character the copy should have
 * @return Copy of this TextCharacter with different underlying character
 */
    @SuppressWarnings("SameParameterValue")
 fun withCharacter(character:Char):TextCharacter {
if (this.characterString!!.equals(Character.toString(character)))
{
return this
}
return TextCharacter(character, foregroundColor, backgroundColor, modifiers)
}

/**
 * Returns a copy of this TextCharacter with a specified foreground color
 * @param foregroundColor Foreground color the copy should have
 * @return Copy of the TextCharacter with a different foreground color
 */
     fun withForegroundColor(foregroundColor:TextColor?):TextCharacter {
if (this.foregroundColor === foregroundColor || this.foregroundColor!!.equals(foregroundColor))
{
return this
}
return TextCharacter(characterString!!, foregroundColor, backgroundColor, modifiers)
}

/**
 * Returns a copy of this TextCharacter with a specified background color
 * @param backgroundColor Background color the copy should have
 * @return Copy of the TextCharacter with a different background color
 */
     fun withBackgroundColor(backgroundColor:TextColor?):TextCharacter {
if (this.backgroundColor === backgroundColor || this.backgroundColor!!.equals(backgroundColor))
{
return this
}
return TextCharacter(characterString!!, foregroundColor, backgroundColor, modifiers)
}

/**
 * Returns a copy of this TextCharacter with specified list of SGR modifiers. None of the currently active SGR codes
 * will be carried over to the copy, only those in the passed in value.
 * @param modifiers SGR modifiers the copy should have
 * @return Copy of the TextCharacter with a different set of SGR modifiers
 */
     fun withModifiers(modifiers:Collection<SGR?>?):TextCharacter {
val newSet = EnumSet.copyOf(modifiers)
if (modifiers!!.equals(newSet))
{
return this
}
return TextCharacter(characterString!!, foregroundColor, backgroundColor, newSet)
}

/**
 * Returns a copy of this TextCharacter with an additional SGR modifier. All of the currently active SGR codes
 * will be carried over to the copy, in addition to the one specified.
 * @param modifier SGR modifiers the copy should have in additional to all currently present
 * @return Copy of the TextCharacter with a new SGR modifier
 */
     fun withModifier(modifier:SGR?):TextCharacter {
if (modifiers!!.contains(modifier))
{
return this
}
val newSet = EnumSet.copyOf(this.modifiers)
newSet!!.add(modifier)
return TextCharacter(characterString!!, foregroundColor, backgroundColor, newSet)
}

/**
 * Returns a copy of this TextCharacter with an SGR modifier removed. All of the currently active SGR codes
 * will be carried over to the copy, except for the one specified. If the current TextCharacter doesn't have the
 * SGR specified, it will return itself.
 * @param modifier SGR modifiers the copy should not have
 * @return Copy of the TextCharacter without the SGR modifier
 */
     fun withoutModifier(modifier:SGR?):TextCharacter {
if (!modifiers!!.contains(modifier))
{
return this
}
val newSet = EnumSet.copyOf(this.modifiers)
newSet!!.remove(modifier)
return TextCharacter(characterString!!, foregroundColor, backgroundColor, newSet)
}

@SuppressWarnings("SimplifiableIfStatement")
@Override
 fun equals(obj:Object?):Boolean {
if (obj == null)
{
return false
}
if (getClass() !== obj!!.getClass())
{
return false
}
val other = obj as TextCharacter?
if (!Objects.equals(this.characterString, other!!.characterString))
{
return false
}
if (!Objects.equals(this.foregroundColor, other!!.foregroundColor))
{
return false
}
if (!Objects.equals(this.backgroundColor, other!!.backgroundColor))
{
return false
}
return Objects.equals(this.modifiers, other!!.modifiers)
}

@Override
 fun hashCode():Int {
var hash = 7
hash = 37 * hash + this.characterString!!.hashCode()
hash = 37 * hash + (if (this.foregroundColor != null) this.foregroundColor!!.hashCode() else 0)
hash = 37 * hash + (if (this.backgroundColor != null) this.backgroundColor!!.hashCode() else 0)
hash = 37 * hash + (if (this.modifiers != null) this.modifiers!!.hashCode() else 0)
return hash
}

@Override
 fun toString():String? {
return "TextCharacter{" + "character=" + characterString + ", foregroundColor=" + foregroundColor + ", backgroundColor=" + backgroundColor + ", modifiers=" + modifiers + '}'.toString()
}

companion object {
private fun toEnumSet(vararg modifiers:SGR?):EnumSet<SGR?>? {
if (modifiers.size == 0)
{
return EnumSet.noneOf(SGR::class.java)
}
else
{
return EnumSet.copyOf(Arrays.asList(modifiers))
}
}

 val DEFAULT_CHARACTER = TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)

 fun fromCharacter(c:Char):Array<TextCharacter?>? {
return fromString(Character.toString(c))
}

 fun fromString(string:String?):Array<TextCharacter?>? {
return fromString(string, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)
}

 fun fromCharacter(c:Char, foregroundColor:TextColor?, backgroundColor:TextColor?, vararg modifiers:SGR?):TextCharacter? {
return fromString(Character.toString(c), foregroundColor, backgroundColor, modifiers)[0]
}

 fun fromString(
string:String?, 
foregroundColor:TextColor?, 
backgroundColor:TextColor?, 
vararg modifiers:SGR?):Array<TextCharacter?>? {
return fromString(string, foregroundColor, backgroundColor, toEnumSet(*modifiers!!))
}

 fun fromString(
string:String?, 
foregroundColor:TextColor?, 
backgroundColor:TextColor?, 
modifiers:EnumSet<SGR?>?):Array<TextCharacter?>? {

val breakIterator = BreakIterator.getCharacterInstance()
breakIterator!!.setText(string)
val result = ArrayList()
var begin = 0
var end = 0
while ((end = breakIterator!!.next()) != BreakIterator.DONE)
{
result.add(TextCharacter(string!!.substring(begin, end), foregroundColor, backgroundColor, modifiers))
begin = breakIterator!!.current()
}
return result.toArray(arrayOfNulls<TextCharacter?>(0))
}

private fun isEmoji(s:String):Boolean {
 // This is really hard to do properly and would require an emoji library as a dependency, so here's a hack that
        // basically assumes anything NOT a regular latin1/CJK/thai character is an emoji
        val firstCharacter = s.charAt(0)
return (s.length() > 1 || !((TerminalTextUtils.isCharCJK(firstCharacter) || 
TerminalTextUtils.isPrintableCharacter(firstCharacter) || 
TerminalTextUtils.isCharThai(firstCharacter) || 
TerminalTextUtils.isCharCJK(firstCharacter) || 
TerminalTextUtils.isControlCharacter(firstCharacter))))
}
}
}
