package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize

interface ComponentRenderer<T : Component> {
    fun getPreferredSize(component: T?): TerminalSize?
    fun drawComponent(graphics: TextGUIGraphics?, component: T?)
}
