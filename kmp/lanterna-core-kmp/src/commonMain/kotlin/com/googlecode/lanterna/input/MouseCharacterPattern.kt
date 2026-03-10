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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.internal.compat.Matcher
import com.googlecode.lanterna.internal.compat.Pattern

/**
 * Pattern used to detect Xterm-protocol mouse events coming in on the standard input channel
 * Created by martin on 19/07/15.
 * 
 * @author Martin, Andreas
 */
 class MouseCharacterPattern:CharacterPattern {


 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    // some terminals, for example XTerm, issue mouse down when it
    // should be mouse move, after first click then they correctly issues
    // mouse move, do some coercion here to force the correct action
    private var isMouseDown = false
 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    override fun match(seq:List<Char>?):CharacterPattern.Matching? {
val sequence = seq ?: return null
val size = sequence.size
if (size > 15)
{
return null // nope
}

 // check first 3 chars:
        for (i in 0..2)
{
if (i >= (size - 1))
{
return CharacterPattern.Matching.NOT_YET // maybe later
}
if (sequence[i] != HEADER[i])
{
return null // nope
}
}

 // Check if we have a number on the next position
        if (sequence[3].code < 48 || sequence[3].code > 57)
{
return null // nope
}

 // If the size is lower than 7 then we don't have the pattern yet for sure
        if (size < 7)
{
return CharacterPattern.Matching.NOT_YET // maybe later
}

 // converts the list of characters to a string
        val seqAString = sequence.joinToString(separator = "")

 // Check if we match the regex
        val matcher = pattern!!.matcher(seqAString)
if (matcher!!.matches())
{
var shiftDown = false
var altDown = false
var ctrlDown = false

 // Get the button
            val item = com.googlecode.lanterna.internal.compat.Integer.valueOf(matcher!!.group(1))
var button = 0

 // if the 6th bit is set, then it's a wheel event then we check the 1st bit to know if it's up or down
            if ((item and 0x40) != 0)
{
if ((item and 0x1) == 0)
{
button = 4
}
else
{
button = 5
}
}
else if ((item and 0x2) != 0)
{
button = 3
}
else if ((item and 0x1) != 0)
{
button = 1
}
else if ((item and 0x1) == 0)
{
button = 2
}

 // Get the modifier keys (it seems that they do not are always reported correctly depending on the terminal)
            if ((item and 0x4) != 0)
{
shiftDown = true
}
if ((item and 0x8) != 0)
{
altDown = true
}
if ((item and 0x10) != 0)
{
ctrlDown = true
}

 // Get the action
            var actionType:MouseActionType? = null
if (matcher!!.group(4).equals("M"))
{
actionType = MouseActionType.CLICK_DOWN
}
else
{
actionType = MouseActionType.CLICK_RELEASE
}

 // Get the move and drag actions
            if ((item and 0x20) != 0)
{
if ((item and 0x3) != 0)
{
 // In move mode, the bits 0, 1 are set in addition to the 6th bit
                    actionType = MouseActionType.MOVE
button = 0
}
else
{
actionType = MouseActionType.DRAG
}
}
else
{
isMouseDown = (actionType === MouseActionType.CLICK_DOWN)
}

 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            // coerce action types:
            // when in between CLICK_DOWN and CLICK_RELEASE coerce MOVE to DRAG
            // when not between CLICK_DOWN and CLICK_RELEASE coerce DRAG to MOVE
            if (isMouseDown)
{
if (actionType === MouseActionType.MOVE)
{
actionType = MouseActionType.DRAG
}
}
else if (actionType === MouseActionType.DRAG)
{
actionType = MouseActionType.MOVE
}
 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

            // Get the position
            val pos = TerminalPosition(com.googlecode.lanterna.internal.compat.Integer.valueOf(matcher!!.group(2)) - 1, com.googlecode.lanterna.internal.compat.Integer.valueOf(matcher!!.group(3)) - 1)

val ma = MouseAction(actionType, button, pos, ctrlDown, altDown, shiftDown)
return CharacterPattern.Matching(ma) // yep
}
else
{
return CharacterPattern.Matching.NOT_YET // maybe later
}
}

companion object {
private val HEADER = charArrayOf(KeyDecodingProfile.ESC_CODE, '[', '<')
private val pattern = Pattern.compile(".*\\<([0-9]+);([0-9]+);([0-9]+)([mM])")
}
}
