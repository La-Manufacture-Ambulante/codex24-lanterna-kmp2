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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor

/**
 * Simple component which draws a solid color over its area. The size this component will request is specified through
 * it's constructor.
 *
 * @author Martin
 */
open class EmptySpace constructor(
    private var color: TextColor? = null,
    private val requestedSize: TerminalSize? = TerminalSize.ONE,
) : AbstractComponent<EmptySpace?>() {
    constructor(size: TerminalSize?) : this(null, size)

    fun setColor(color: TextColor?) {
        this.color = color
    }

    fun getColor(): TextColor? {
        return color
    }

    override fun createDefaultRenderer(): ComponentRenderer<EmptySpace?>? {
        return object : ComponentRenderer<EmptySpace?> {
            override fun getPreferredSize(component: EmptySpace?): TerminalSize? {
                return requestedSize
            }

            override fun drawComponent(
                graphics: TextGUIGraphics?,
                component: EmptySpace?,
            ) {
                graphics!!.applyThemeStyle(component!!.themeDefinition!!.normal)
                if (color != null) {
                    graphics.setBackgroundColor(color)
                }
                graphics.fill(' ')
            }
        }
    }
}
