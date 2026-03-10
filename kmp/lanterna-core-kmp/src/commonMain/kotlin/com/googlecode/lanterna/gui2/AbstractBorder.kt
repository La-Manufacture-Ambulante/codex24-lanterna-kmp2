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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Border.BorderRenderer

/**
 * Abstract implementation of [Border] interface with common behavior.
 */
abstract class AbstractBorder : AbstractComposite<Border?>(), Border {
    override val renderer: BorderRenderer?
        get() = super.renderer as BorderRenderer?

    override val layoutData: LayoutData?
        get() = component?.layoutData ?: super.layoutData

    private val wrappedComponentTopLeftOffset: TerminalPosition?
        get() = renderer?.wrappedComponentTopLeftOffset

    override var component: Component?
        get() = super.component
        set(value) {
            super.component = value
            value?.setPosition(TerminalPosition.TOP_LEFT_CORNER)
        }

    override fun setSize(size: TerminalSize?): Border? {
        super.setSize(size)
        component?.setSize(getWrappedComponentSize(size))
        return self()
    }

    override fun setLayoutData(ld: LayoutData?): Border? {
        if (component == null) {
            super.setLayoutData(ld)
        } else {
            component?.setLayoutData(ld)
        }
        return this
    }

    override fun toBasePane(position: TerminalPosition?): TerminalPosition? {
        val terminalPosition = super.toBasePane(position) ?: return null
        return terminalPosition.withRelative(wrappedComponentTopLeftOffset ?: TerminalPosition.TOP_LEFT_CORNER)
    }

    override fun toGlobal(position: TerminalPosition?): TerminalPosition? {
        val terminalPosition = super.toGlobal(position) ?: return null
        return terminalPosition.withRelative(wrappedComponentTopLeftOffset ?: TerminalPosition.TOP_LEFT_CORNER)
    }

    private fun getWrappedComponentSize(borderSize: TerminalSize?): TerminalSize? {
        return renderer?.getWrappedComponentSize(borderSize)
    }
}
