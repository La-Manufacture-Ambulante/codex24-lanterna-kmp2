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

import java.util.ArrayList
import java.util.Arrays
import java.util.Objects

/**
 * Represents the user pressing a key on the keyboard. If the user held down ctrl and/or alt before pressing the key,
 * this may be recorded in this class, depending on the terminal implementation and if such information in available.
 * KeyStroke objects are normally constructed by a KeyDecodingProfile, which works off a character stream that likely
 * coming from the system's standard input. Because of this, the class can only represent what can be read and
 * interpreted from the input stream; for example, certain key-combinations like ctrl+i is indistinguishable from a tab
 * key press.
 * 
 * 
 * Use the <tt>keyType</tt> field to determine what kind of key was pressed. For ordinary letters, numbers and symbols, the
 * <tt>keyType</tt> will be <tt>KeyType.Character</tt> and the actual character value of the key is in the
 * <tt>character</tt> field. Please note that return (\n) and tab (\t) are not sorted under type <tt>KeyType.Character</tt>
 * but <tt>KeyType.Enter</tt> and <tt>KeyType.Tab</tt> instead.
 * @author martin
 */
 class KeyStroke private constructor(/**
 * Type of key that was pressed on the keyboard, as represented by the KeyType enum. If the value if
 * KeyType.Character, you need to call getCharacter() to find out which letter, number or symbol that was actually
 * pressed.
 * @return Type of key on the keyboard that was pressed
 */
     val keyType:KeyType?, character:Character?, /**
 * @return Returns true if ctrl was help down while the key was typed (depending on terminal implementation)
 */
     val isCtrlDown:Boolean, /**
 * @return Returns true if alt was help down while the key was typed (depending on terminal implementation)
 */
     val isAltDown:Boolean, /**
 * @return Returns true if shift was help down while the key was typed (depending on terminal implementation)
 */
     val isShiftDown:Boolean) {
/**
 * For keystrokes of ordinary keys (letters, digits, symbols), this method returns the actual character value of the
 * key. For all other key types, it returns null.
 * @return Character value of the key pressed, or null if it was a special key
 */
     val character:Character?
/**
 * Gets the time when the keystroke was recorded. This isn't necessarily the time the keystroke happened, but when
 * Lanterna received the event, so it may not be accurate down to the millisecond.
 * @return The unix time of when the keystroke happened, in milliseconds
 */
     val eventTime:Long

/**
 * Constructs a KeyStroke based on a supplied keyType; character will be null and both ctrl and alt will be
 * considered not pressed. If you try to construct a KeyStroke with type KeyType.Character with this constructor, it
 * will always throw an exception; use another overload that allows you to specify the character value instead.
 * @param keyType Type of the key pressed by this keystroke
 */
     constructor(keyType:KeyType?) : this(keyType, null, false, false, false) {}

/**
 * Constructs a KeyStroke based on a supplied keyType; character will be null.
 * If you try to construct a KeyStroke with type KeyType.Character with this constructor, it
 * will always throw an exception; use another overload that allows you to specify the character value instead.
 * @param keyType Type of the key pressed by this keystroke
 * @param ctrlDown Was ctrl held down when the main key was pressed?
 * @param altDown Was alt held down when the main key was pressed?
 */
     constructor(keyType:KeyType?, ctrlDown:Boolean, altDown:Boolean) : this(keyType, null, ctrlDown, altDown, false) {}

/**
 * Constructs a KeyStroke based on a supplied keyType; character will be null.
 * If you try to construct a KeyStroke with type KeyType.Character with this constructor, it
 * will always throw an exception; use another overload that allows you to specify the character value instead.
 * @param keyType Type of the key pressed by this keystroke
 * @param ctrlDown Was ctrl held down when the main key was pressed?
 * @param altDown Was alt held down when the main key was pressed?
 * @param shiftDown Was shift held down when the main key was pressed?
 */
     constructor(keyType:KeyType?, ctrlDown:Boolean, altDown:Boolean, shiftDown:Boolean) : this(keyType, null, ctrlDown, altDown, shiftDown) {}

/**
 * Constructs a KeyStroke based on a supplied character, keyType is implicitly KeyType.Character.
 * 
 * 
 * A character-based KeyStroke does not support the shiftDown flag, as the shift state has
 * already been accounted for in the character itself, depending on user's keyboard layout.
 * @param character Character that was typed on the keyboard
 * @param ctrlDown Was ctrl held down when the main key was pressed?
 * @param altDown Was alt held down when the main key was pressed?
 */
     constructor(character:Character?, ctrlDown:Boolean, altDown:Boolean) : this(KeyType.CHARACTER, character, ctrlDown, altDown, false) {}

/**
 * Constructs a KeyStroke based on a supplied character, keyType is implicitly KeyType.Character.
 * 
 * 
 * A character-based KeyStroke does not support the shiftDown flag, as the shift state has
 * already been accounted for in the character itself, depending on user's keyboard layout.
 * @param character Character that was typed on the keyboard
 * @param ctrlDown Was ctrl held down when the main key was pressed?
 * @param altDown Was alt held down when the main key was pressed?
 * @param shiftDown Was shift held down when the main key was pressed?
 */
     constructor(character:Character?, ctrlDown:Boolean, altDown:Boolean, shiftDown:Boolean) : this(KeyType.CHARACTER, character, ctrlDown, altDown, shiftDown) {}

init{
var character = character
if (keyType === KeyType.CHARACTER && character == null)
{
throw IllegalArgumentException("Cannot construct a KeyStroke with type KeyType.Character but no character information")
}
 //Enforce character for some key types
        when (keyType) {
BACKSPACE -> character = '\b'
ENTER -> character = '\n'
TAB -> character = '\t'
}
this.character = character
this.eventTime = System.currentTimeMillis()
}

/**
 * an F3-KeyStroke that is distinguishable from a CursorLocation report.
 */
     class RealF3:KeyStroke(KeyType.F3, false, false, false)

@Override
 fun toString():String? {
val sb = StringBuilder()
sb.append("KeyStroke{keytype=").append(keyType)
if (character != null)
{
val ch = character!!.toChar()
sb.append(", character='")
when (ch) {
 // many of these cases can only happen through user code:
            0x00 -> sb.append("^@")
0x08 -> sb.append("\\b")
0x09 -> sb.append("\\t")
0x0a -> sb.append("\\n")
0x0d -> sb.append("\\r")
0x1b -> sb.append("^[")
0x1c -> sb.append("^\\")
0x1d -> sb.append("^]")
0x1e -> sb.append("^^")
0x1f -> sb.append("^_")
else -> if (ch!!.toInt() <= 26)
{
sb.append('^').append((ch!!.toInt() + 64).toChar())
}
else
{
sb.append(ch)
}
}
sb.append('\'')
}
if (isCtrlDown || isAltDown || isShiftDown)
{
var sep:String? = ""
sb.append(", modifiers=[")
if (isCtrlDown) {
sb.append(sep).append("ctrl")
sep = ","
}
if (isAltDown) {
sb.append(sep).append("alt")
sep = ","
}
if (isShiftDown) {
sb.append(sep).append("shift")
}
sb.append("]")
}
return sb.append('}').toString()
}

@Override
 fun hashCode():Int {
var hash = 3
hash = 41 * hash + (if (this.keyType != null) this.keyType!!.hashCode() else 0)
hash = 41 * hash + (if (this.character != null) this.character!!.hashCode() else 0)
hash = 41 * hash + (if (this.isCtrlDown) 1 else 0)
hash = 41 * hash + (if (this.isAltDown) 1 else 0)
hash = 41 * hash + (if (this.isShiftDown) 1 else 0)
return hash
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
val other = obj as KeyStroke?
if (this.keyType !== other!!.keyType)
{
return false
}
if (!Objects.equals(this.character, other!!.character))
{
return false
}
return (this.isCtrlDown == other!!.isCtrlDown && 
this.isAltDown == other!!.isAltDown && 
this.isShiftDown == other!!.isShiftDown)
}

companion object {

/**
 * Creates a Key from a string representation in Vim's key notation.
 * 
 * @param keyStr the string representation of this key
 * @return the created [KeyType]
 */
     fun fromString(keyStr:String):KeyStroke {
val keyStrLC = keyStr.toLowerCase()
val k:KeyStroke?
if (keyStr.length() === 1)
{
k = KeyStroke(KeyType.CHARACTER, keyStr.charAt(0), false, false, false)
}
else if (keyStr.startsWith("<") && keyStr.endsWith(">"))
{
if (keyStrLC!!.equals("<s-tab>"))
{
k = KeyStroke(KeyType.REVERSE_TAB)
}
else if (keyStr.contains("-"))
{
val segments = ArrayList(Arrays.asList(keyStr.substring(1, keyStr.length() - 1).split("-")))
if (segments.size() < 2)
{
throw IllegalArgumentException("Invalid vim notation: " + keyStr)
}
var characterStr = segments.remove(segments.size() - 1)
var altPressed = false
var ctrlPressed = false
for (modifier in segments)
{
when (modifier!!.toLowerCase()) {
"c" -> ctrlPressed = true
"a" -> altPressed = true
"s" -> characterStr = characterStr!!.toUpperCase()
}
}
k = KeyStroke(characterStr!!.charAt(0), ctrlPressed, altPressed)
}
else
{
if (keyStrLC!!.startsWith("<esc"))
{
k = KeyStroke(KeyType.ESCAPE)
}
else if (keyStrLC!!.equals("<cr>") || keyStrLC!!.equals("<enter>") || keyStrLC!!.equals("<return>"))
{
k = KeyStroke(KeyType.ENTER)
}
else if (keyStrLC!!.equals("<bs>"))
{
k = KeyStroke(KeyType.BACKSPACE)
}
else if (keyStrLC!!.equals("<tab>"))
{
k = KeyStroke(KeyType.TAB)
}
else if (keyStrLC!!.equals("<space>"))
{
k = KeyStroke(' ', false, false)
}
else if (keyStrLC!!.equals("<up>"))
{
k = KeyStroke(KeyType.ARROW_UP)
}
else if (keyStrLC!!.equals("<down>"))
{
k = KeyStroke(KeyType.ARROW_DOWN)
}
else if (keyStrLC!!.equals("<left>"))
{
k = KeyStroke(KeyType.ARROW_LEFT)
}
else if (keyStrLC!!.equals("<right>"))
{
k = KeyStroke(KeyType.ARROW_RIGHT)
}
else if (keyStrLC!!.equals("<insert>"))
{
k = KeyStroke(KeyType.INSERT)
}
else if (keyStrLC!!.equals("<del>"))
{
k = KeyStroke(KeyType.DELETE)
}
else if (keyStrLC!!.equals("<home>"))
{
k = KeyStroke(KeyType.HOME)
}
else if (keyStrLC!!.equals("<end>"))
{
k = KeyStroke(KeyType.END)
}
else if (keyStrLC!!.equals("<pageup>"))
{
k = KeyStroke(KeyType.PAGE_UP)
}
else if (keyStrLC!!.equals("<pagedown>"))
{
k = KeyStroke(KeyType.PAGE_DOWN)
}
else if (keyStrLC!!.equals("<f1>"))
{
k = KeyStroke(KeyType.F1)
}
else if (keyStrLC!!.equals("<f2>"))
{
k = KeyStroke(KeyType.F2)
}
else if (keyStrLC!!.equals("<f3>"))
{
k = KeyStroke(KeyType.F3)
}
else if (keyStrLC!!.equals("<f4>"))
{
k = KeyStroke(KeyType.F4)
}
else if (keyStrLC!!.equals("<f5>"))
{
k = KeyStroke(KeyType.F5)
}
else if (keyStrLC!!.equals("<f6>"))
{
k = KeyStroke(KeyType.F6)
}
else if (keyStrLC!!.equals("<f7>"))
{
k = KeyStroke(KeyType.F7)
}
else if (keyStrLC!!.equals("<f8>"))
{
k = KeyStroke(KeyType.F8)
}
else if (keyStrLC!!.equals("<f9>"))
{
k = KeyStroke(KeyType.F9)
}
else if (keyStrLC!!.equals("<f10>"))
{
k = KeyStroke(KeyType.F10)
}
else if (keyStrLC!!.equals("<f11>"))
{
k = KeyStroke(KeyType.F11)
}
else if (keyStrLC!!.equals("<f12>"))
{
k = KeyStroke(KeyType.F12)
}
else
{
throw IllegalArgumentException("Invalid vim notation: " + keyStr)
}
}
}
else
{
throw IllegalArgumentException("Invalid vim notation: " + keyStr)
}
return k
}
}
}
