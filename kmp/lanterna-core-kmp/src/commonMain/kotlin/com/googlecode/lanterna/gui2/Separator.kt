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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * Static non-interactive component that is typically rendered as a single line.
 */
class Separator(val direction: Direction?) : AbstractComponent<Separator>() {
    init {
        require(direction != null) { "Cannot create a separator with a null direction" }
    }

    override fun createDefaultRenderer(): DefaultSeparatorRenderer {
        return DefaultSeparatorRenderer()
    }

    abstract class SeparatorRenderer : ComponentRenderer<Separator?>

    class DefaultSeparatorRenderer : SeparatorRenderer() {
        override fun getPreferredSize(component: Separator?): TerminalSize {
            return TerminalSize.ONE
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: Separator?) {
            val activeGraphics = graphics ?: return
            val activeComponent = component ?: return
            val themeDefinition: ThemeDefinition = activeComponent.themeDefinition ?: return
            activeGraphics.applyThemeStyle(themeDefinition.normal)
            val character =
                themeDefinition.getCharacter(
                    activeComponent.direction!!.name.uppercase(),
                    if (activeComponent.direction == Direction.HORIZONTAL) {
                        Symbols.SINGLE_LINE_HORIZONTAL
                    } else {
                        Symbols.SINGLE_LINE_VERTICAL
                    },
                )
            activeGraphics.fill(character)
        }
    }
}
