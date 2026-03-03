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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition

interface Component : TextGUIElement {
    fun getPosition(): TerminalPosition?

    fun getGlobalPosition(): TerminalPosition?

    fun setPosition(position: TerminalPosition?): Component?

    fun getSize(): TerminalSize?

    fun setSize(size: TerminalSize?): Component?

    fun getPreferredSize(): TerminalSize?

    fun setPreferredSize(explicitPreferredSize: TerminalSize?): Component?

    fun setLayoutData(data: LayoutData?): Component?

    fun getLayoutData(): LayoutData?

    fun isVisible(): Boolean

    fun setVisible(visible: Boolean): Component?

    fun getParent(): Container?

    fun hasParent(parent: Container?): Boolean

    fun getTextGUI(): TextGUI?

    fun getTheme(): Theme?

    fun getThemeDefinition(): ThemeDefinition?

    fun setTheme(theme: Theme?): Component?

    fun isInside(container: Container?): Boolean

    fun getRenderer(): ComponentRenderer<out Component?>?

    fun invalidate()

    fun withBorder(border: Border?): Border?

    fun toBasePane(position: TerminalPosition?): TerminalPosition?

    fun toGlobal(position: TerminalPosition?): TerminalPosition?

    fun getBasePane(): BasePane?

    fun addTo(panel: Panel?): Component?

    fun onAdded(container: Container?)

    fun onRemoved(container: Container?)
}
