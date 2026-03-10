package com.googlecode.lanterna.terminal.swing

import java.awt.Component
import java.awt.Rectangle
import java.awt.font.TextHitInfo
import java.awt.im.InputMethodRequests
import java.text.AttributedCharacterIterator

internal class TerminalInputMethodRequests(
    private val owner: Component,
    private val terminalImplementation: GraphicalTerminalImplementation,
) : InputMethodRequests {
    override fun getTextLocation(offset: TextHitInfo?): Rectangle {
        val location = owner.locationOnScreen
        val cursorPosition = terminalImplementation.cursorPosition
        val offsetX = requireNotNull(cursorPosition).column * terminalImplementation.fontWidth
        val offsetY = cursorPosition.row * terminalImplementation.fontHeight + terminalImplementation.fontHeight
        return Rectangle(location.x + offsetX, location.y + offsetY, 0, 0)
    }

    override fun getLocationOffset(x: Int, y: Int): TextHitInfo? = null

    override fun getInsertPositionOffset(): Int = 0

    override fun getCommittedText(
        beginIndex: Int,
        endIndex: Int,
        attributes: Array<AttributedCharacterIterator.Attribute>?,
    ): AttributedCharacterIterator? = null

    override fun getCommittedTextLength(): Int = 0

    override fun cancelLatestCommittedText(
        attributes: Array<AttributedCharacterIterator.Attribute>?,
    ): AttributedCharacterIterator? = null

    override fun getSelectedText(
        attributes: Array<AttributedCharacterIterator.Attribute>?,
    ): AttributedCharacterIterator? = null
}
