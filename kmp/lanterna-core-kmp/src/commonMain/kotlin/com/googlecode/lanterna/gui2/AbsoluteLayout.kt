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

/**
 * Layout manager that places components where they are manually specified to be and sizes them to the size they are
 * manually assigned to. When using the AbsoluteLayout, please use setPosition(..) and setSize(..) manually on each
 * component to choose where to place them. Components that have not had their position and size explicitly set will
 * not be visible.
 *
 * @author martin
 */
class AbsoluteLayout : LayoutManager {
    override fun getPreferredSize(components: List<Component?>?): TerminalSize? {
        var size: TerminalSize? = TerminalSize.ZERO
        for (component in components.orEmpty()) {
            if (component == null) {
                continue
            }
            val position = component.position ?: continue
            val componentSize = component.size ?: continue
            size = size?.max(
                TerminalSize(
                    position.column + componentSize.columns,
                    position.row + componentSize.rows,
                ),
            ) ?: TerminalSize(
                position.column + componentSize.columns,
                position.row + componentSize.rows,
            )
        }
        return size ?: TerminalSize.ZERO
    }

    override fun doLayout(area: TerminalSize?, components: List<Component?>?) {
        // Do nothing
    }

    override fun hasChanged(): Boolean {
        return false
    }
}
