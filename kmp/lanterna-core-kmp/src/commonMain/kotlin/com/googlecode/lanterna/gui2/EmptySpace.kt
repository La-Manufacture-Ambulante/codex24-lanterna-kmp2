package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor

open class EmptySpace : AbstractComponent<EmptySpace> {
    private val size: TerminalSize?
    private var color: TextColor?

    constructor() : this(null, TerminalSize.ONE)

    constructor(color: TextColor?) : this(color, TerminalSize.ONE)

    constructor(size: TerminalSize?) : this(null, size)

    constructor(color: TextColor?, size: TerminalSize?) {
        this.color = color
        this.size = size
    }

    open fun setColor(color: TextColor?) {
        this.color = color
    }

    open fun getColor(): TextColor? {
        return color
    }

    protected open override fun createDefaultRenderer(): ComponentRenderer<EmptySpace> {
        return object : ComponentRenderer<EmptySpace> {
            override fun getPreferredSize(component: EmptySpace): TerminalSize? {
                return this@EmptySpace.size
            }

            override fun drawComponent(graphics: TextGUIGraphics, component: EmptySpace) {
                graphics.applyThemeStyle(component.themeDefinition.normal)
                val color = this@EmptySpace.color
                if (color != null) {
                    graphics.setBackgroundColor(color)
                }
                graphics.fill(' ')
            }
        }
    }
}
