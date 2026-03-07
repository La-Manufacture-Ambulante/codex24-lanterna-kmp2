package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.TerminalPosition

import java.awt.*
import java.awt.font.TextHitInfo
import java.awt.im.InputMethodRequests
import java.text.AttributedCharacterIterator

internal class TerminalInputMethodRequests(private val owner:Component?, private val terminalImplementation:GraphicalTerminalImplementation?):InputMethodRequests {

 val insertPositionOffset:Int
@Override
get() {
return 0
}

 val committedTextLength:Int
@Override
get() {
return 0
}

@Override
 fun getTextLocation(offset:TextHitInfo?):Rectangle? {
val location = owner!!.getLocationOnScreen()
val cursorPosition = terminalImplementation!!.getCursorPosition()

val offsetX = cursorPosition!!.column * terminalImplementation!!.getFontWidth()
val offsetY = cursorPosition!!.row * terminalImplementation!!.getFontHeight() + terminalImplementation!!.getFontHeight()

return Rectangle(location!!.x + offsetX, location!!.y + offsetY, 0, 0)
}

@Override
 fun getLocationOffset(x:Int, y:Int):TextHitInfo? {
return null
}

@Override
 fun getCommittedText(beginIndex:Int, endIndex:Int, attributes:Array<AttributedCharacterIterator.Attribute?>?):AttributedCharacterIterator? {
return null
}

@Override
 fun cancelLatestCommittedText(attributes:Array<AttributedCharacterIterator.Attribute?>?):AttributedCharacterIterator? {
return null
}

@Override
 fun getSelectedText(attributes:Array<AttributedCharacterIterator.Attribute?>?):AttributedCharacterIterator? {
return null
}
}
