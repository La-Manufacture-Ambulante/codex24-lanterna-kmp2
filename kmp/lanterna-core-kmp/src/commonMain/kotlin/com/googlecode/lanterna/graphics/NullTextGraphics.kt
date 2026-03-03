package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.StyleSet
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextImage
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.Arrays
import java.util.Collection
import java.util.EnumSet

internal class NullTextGraphics(size: TerminalSize?) : TextGraphics {
    private val size: TerminalSize? = size
    private var foregroundColor: TextColor? = TextColor.ANSI.DEFAULT
    private var backgroundColor: TextColor? = TextColor.ANSI.DEFAULT
    private var tabBehaviour: TabBehaviour? = TabBehaviour.ALIGN_TO_COLUMN_4
    private val activeModifiers: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)

    fun toScreenPosition(pos: TerminalPosition?): TerminalPosition? {
        return null
    }

    override fun getSize(): TerminalSize? {
        return size
    }

    @Throws(IllegalArgumentException::class)
    override fun newTextGraphics(topLeftCorner: TerminalPosition?, size: TerminalSize?): TextGraphics {
        return this
    }

    override fun getBackgroundColor(): TextColor? {
        return backgroundColor
    }

    override fun setBackgroundColor(backgroundColor: TextColor?): TextGraphics {
        this.backgroundColor = backgroundColor
        return this
    }

    override fun getForegroundColor(): TextColor? {
        return foregroundColor
    }

    override fun setForegroundColor(foregroundColor: TextColor?): TextGraphics {
        this.foregroundColor = foregroundColor
        return this
    }

    override fun enableModifiers(modifiers: Array<out SGR?>?): TextGraphics {
        val m = modifiers ?: throw NullPointerException()
        @Suppress("UNCHECKED_CAST")
        val list = Arrays.asList(*m) as Collection<SGR>
        activeModifiers.addAll(list)
        return this
    }

    override fun disableModifiers(modifiers: Array<out SGR?>?): TextGraphics {
        val m = modifiers ?: throw NullPointerException()
        @Suppress("UNCHECKED_CAST")
        val list = Arrays.asList(*m) as Collection<SGR>
        activeModifiers.removeAll(list)
        return this
    }

    override fun setModifiers(modifiers: EnumSet<SGR>?): TextGraphics {
        clearModifiers()
        activeModifiers.addAll(modifiers ?: throw NullPointerException())
        return this
    }

    override fun clearModifiers(): TextGraphics {
        activeModifiers.clear()
        return this
    }

    override fun getActiveModifiers(): EnumSet<SGR> {
        return EnumSet.copyOf(activeModifiers)
    }

    override fun getTabBehaviour(): TabBehaviour? {
        return tabBehaviour
    }

    override fun setTabBehaviour(tabBehaviour: TabBehaviour?): TextGraphics {
        this.tabBehaviour = tabBehaviour
        return this
    }

    override fun fill(c: Char): TextGraphics {
        return this
    }

    override fun setCharacter(column: Int, row: Int, character: Char): TextGraphics {
        return this
    }

    override fun setCharacter(column: Int, row: Int, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun setCharacter(position: TerminalPosition?, character: Char): TextGraphics {
        return this
    }

    override fun setCharacter(position: TerminalPosition?, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: Char): TextGraphics {
        return this
    }

    override fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: Char): TextGraphics {
        return this
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun drawTriangle(p1: TerminalPosition?, p2: TerminalPosition?, p3: TerminalPosition?, character: Char): TextGraphics {
        return this
    }

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?
    ): TextGraphics {
        return this
    }

    override fun fillTriangle(p1: TerminalPosition?, p2: TerminalPosition?, p3: TerminalPosition?, character: Char): TextGraphics {
        return this
    }

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?
    ): TextGraphics {
        return this
    }

    override fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics {
        return this
    }

    override fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics {
        return this
    }

    override fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics {
        return this
    }

    override fun drawImage(topLeft: TerminalPosition?, image: TextImage?): TextGraphics {
        return this
    }

    override fun drawImage(
        topLeft: TerminalPosition?,
        image: TextImage?,
        sourceImageTopLeft: TerminalPosition?,
        sourceImageSize: TerminalSize?
    ): TextGraphics {
        return this
    }

    override fun putString(column: Int, row: Int, string: String?): TextGraphics {
        return this
    }

    override fun putString(position: TerminalPosition?, string: String?): TextGraphics {
        return this
    }

    override fun putString(
        column: Int,
        row: Int,
        string: String?,
        extraModifier: SGR?,
        optionalExtraModifiers: Array<out SGR?>?
    ): TextGraphics {
        return this
    }

    override fun putString(
        position: TerminalPosition?,
        string: String?,
        extraModifier: SGR?,
        optionalExtraModifiers: Array<out SGR?>?
    ): TextGraphics {
        return this
    }

    override fun putString(column: Int, row: Int, string: String?, extraModifiers: Collection<SGR>?): TextGraphics {
        return this
    }

    override fun putCSIStyledString(column: Int, row: Int, string: String?): TextGraphics {
        return this
    }

    override fun putCSIStyledString(position: TerminalPosition?, string: String?): TextGraphics {
        return this
    }

    override fun getCharacter(column: Int, row: Int): TextCharacter? {
        return null
    }

    override fun getCharacter(position: TerminalPosition?): TextCharacter? {
        return null
    }

    override fun setStyleFrom(source: StyleSet<*>): TextGraphics {
        setBackgroundColor(source.backgroundColor)
        setForegroundColor(source.foregroundColor)
        setModifiers(source.activeModifiers)
        return this
    }
}
