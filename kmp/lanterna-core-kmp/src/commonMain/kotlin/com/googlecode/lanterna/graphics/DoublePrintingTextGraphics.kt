package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter

open class DoublePrintingTextGraphics(underlyingTextGraphics: TextGraphics?) : AbstractTextGraphics() {
    private val underlyingTextGraphics: TextGraphics? = underlyingTextGraphics

    private fun requireUnderlyingTextGraphics(): TextGraphics {
        return underlyingTextGraphics ?: throw NullPointerException()
    }

    override open fun setCharacter(columnIndex: Int, rowIndex: Int, textCharacter: TextCharacter?): TextGraphics? {
        var doubledColumnIndex = columnIndex
        doubledColumnIndex *= 2
        val backend = requireUnderlyingTextGraphics()
        backend.setCharacter(doubledColumnIndex, rowIndex, textCharacter)
        backend.setCharacter(doubledColumnIndex + 1, rowIndex, textCharacter)
        return this
    }

    override open fun getCharacter(columnIndex: Int, rowIndex: Int): TextCharacter? {
        var doubledColumnIndex = columnIndex
        doubledColumnIndex *= 2
        return requireUnderlyingTextGraphics().getCharacter(doubledColumnIndex, rowIndex)
    }

    override open fun getSize(): TerminalSize? {
        val size = requireUnderlyingTextGraphics().getSize()
        return size.withColumns(size.getColumns() / 2)
    }

    override open fun toScreenPosition(pos: TerminalPosition): TerminalPosition? {
        return requireUnderlyingTextGraphics().toScreenPosition(pos.multiply(MULTIPLIER))
    }

    private companion object {
        private val MULTIPLIER = TerminalPosition(2, 1)
    }
}
