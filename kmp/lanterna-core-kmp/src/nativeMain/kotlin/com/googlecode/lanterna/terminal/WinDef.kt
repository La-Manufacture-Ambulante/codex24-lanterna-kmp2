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
package com.googlecode.lanterna.terminal

import com.sun.jna.*

import java.util.Arrays

/**
 * Class containing common Win32 structures involved when operating on the terminal
 */
 object WinDef {

 val INVALID_HANDLE_VALUE = HANDLE(Pointer.createConstant(if (Pointer.SIZE === 8) -1L else 4294967295L))

 class HANDLE:PointerType {
private val immutable:Boolean

 constructor() {}

 constructor(p:Pointer?) {
this.setPointer(p)
this.immutable = true
}

public override fun fromNative(nativeValue:Object?, context:FromNativeContext?):Object? {
val o = super.fromNative(nativeValue, context)
return if (INVALID_HANDLE_VALUE == o) INVALID_HANDLE_VALUE else o
}

public override fun setPointer(p:Pointer?) {
if (this.immutable)
{
throw UnsupportedOperationException("immutable reference")
}
else
{
super.setPointer(p)
}
}

public override fun toString():String? {
return String.valueOf(this.getPointer())
}
}

 class WORD @JvmOverloads  constructor(value:Long = 0L):IntegerType(2, value, true), Comparable<WORD?> {

 fun compareTo(other:WORD?):Int {
return IntegerType.compare<WORD?>(this, other)
}

companion object {
 val SIZE = 2
}
}

 class COORD:Structure() {
 var X:Short = 0
 var Y:Short = 0

@Override
protected override fun getFieldOrder():List? {
return Arrays.asList("X", "Y")
}

@Override
public override fun toString():String? {
return ("COORD{" + 
"X=" + X + 
", Y=" + Y + 
'}'.toString())
}
}

 class SMALL_RECT:Structure() {
 var Left:Short = 0
 var Top:Short = 0
 var Right:Short = 0
 var Bottom:Short = 0

@Override
protected override fun getFieldOrder():List? {
return Arrays.asList("Left", "Top", "Right", "Bottom")
}

@Override
public override fun toString():String? {
return ("SMALL_RECT{" + 
"Left=" + Left + 
", Top=" + Top + 
", Right=" + Right + 
", Bottom=" + Bottom + 
'}'.toString())
}
}

 class CONSOLE_SCREEN_BUFFER_INFO:Structure() {
 var dwSize:COORD? = null
 var dwCursorPosition:COORD? = null
 var wAttributes:WORD? = null
 var srWindow:SMALL_RECT? = null
 var dwMaximumWindowSize:COORD? = null

protected override fun getFieldOrder():List? {
return Arrays.asList("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
}

@Override
public override fun toString():String? {
return ("CONSOLE_SCREEN_BUFFER_INFO{" + 
"dwSize=" + dwSize + 
", dwCursorPosition=" + dwCursorPosition + 
", wAttributes=" + wAttributes + 
", srWindow=" + srWindow + 
", dwMaximumWindowSize=" + dwMaximumWindowSize + 
'}'.toString())
}
}
}
