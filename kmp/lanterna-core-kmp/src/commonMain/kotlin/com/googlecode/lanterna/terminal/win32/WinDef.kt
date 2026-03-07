package com.googlecode.lanterna.terminal.win32

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.Union

 interface WinDef:com.sun.jna.platform.win32.WinDef {

/**
 * COORD structure
 */
	@FieldOrder("X", "Y")
 class COORD:Structure() {

 var X:Short = 0
 var Y:Short = 0

@Override
public override fun toString():String? {
return String.format("COORD(%s,%s)", X, Y)
}
}

/**
 * SMALL_RECT structure
 */
	@FieldOrder("Left", "Top", "Right", "Bottom")
 class SMALL_RECT:Structure() {

 var Left:Short = 0
 var Top:Short = 0
 var Right:Short = 0
 var Bottom:Short = 0

@Override
public override fun toString():String? {
return String.format("SMALL_RECT(%s,%s)(%s,%s)", Left, Top, Right, Bottom)
}
}

/**
 * CONSOLE_SCREEN_BUFFER_INFO structure
 */
	@FieldOrder("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
 class CONSOLE_SCREEN_BUFFER_INFO:Structure() {

 var dwSize:COORD? = null
 var dwCursorPosition:COORD? = null
 var wAttributes:Short = 0
 var srWindow:SMALL_RECT? = null
 var dwMaximumWindowSize:COORD? = null

@Override
public override fun toString():String? {
return String.format("CONSOLE_SCREEN_BUFFER_INFO(%s,%s,%s,%s,%s)", dwSize, dwCursorPosition, wAttributes, srWindow, dwMaximumWindowSize)
}
}

@FieldOrder("EventType", "Event")
 class INPUT_RECORD:Structure() {

 var EventType:Short = 0
 var Event:Event? = null

 class Event:Union() {
 var KeyEvent:KEY_EVENT_RECORD? = null
 var MouseEvent:MOUSE_EVENT_RECORD? = null
 var WindowBufferSizeEvent:WINDOW_BUFFER_SIZE_RECORD? = null
}

@Override
@JvmStatic public override fun read() {
super.read()
when (EventType) {
KEY_EVENT -> Event!!.setType("KeyEvent")
MOUSE_EVENT -> Event!!.setType("MouseEvent")
WINDOW_BUFFER_SIZE_EVENT -> Event!!.setType("WindowBufferSizeEvent")
}
Event!!.read()
}

@Override
public override fun toString():String? {
return String.format("INPUT_RECORD(%s)", EventType)
}

companion object {

 val KEY_EVENT:Short = 0x01
 val MOUSE_EVENT:Short = 0x02
 val WINDOW_BUFFER_SIZE_EVENT:Short = 0x04
}
}

@FieldOrder("bKeyDown", "wRepeatCount", "wVirtualKeyCode", "wVirtualScanCode", "uChar", "dwControlKeyState")
 class KEY_EVENT_RECORD:Structure() {

 var bKeyDown:Boolean = false
 var wRepeatCount:Short = 0
 var wVirtualKeyCode:Short = 0
 var wVirtualScanCode:Short = 0
 var uChar:Char = ' '
 var dwControlKeyState:Int = 0

@Override
public override fun toString():String? {
return String.format("KEY_EVENT_RECORD(%s,%s,%s,%s,%s,%s)", bKeyDown, wRepeatCount, wVirtualKeyCode, wVirtualScanCode, uChar, dwControlKeyState)
}
}

@FieldOrder("dwMousePosition", "dwButtonState", "dwControlKeyState", "dwEventFlags")
 class MOUSE_EVENT_RECORD:Structure() {

 var dwMousePosition:COORD? = null
 var dwButtonState:Int = 0
 var dwControlKeyState:Int = 0
 var dwEventFlags:Int = 0

@Override
public override fun toString():String? {
return String.format("MOUSE_EVENT_RECORD(%s,%s,%s,%s)", dwMousePosition, dwButtonState, dwControlKeyState, dwEventFlags)
}
}

@FieldOrder("dwSize")
 class WINDOW_BUFFER_SIZE_RECORD:Structure() {

 var dwSize:COORD? = null

@Override
public override fun toString():String? {
return String.format("WINDOW_BUFFER_SIZE_RECORD(%s)", dwSize)
}
}

}
