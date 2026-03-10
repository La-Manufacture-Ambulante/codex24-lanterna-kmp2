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
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.KeyStroke

/**
 *
 * @author ginkoblongata
 */
open class ImageComponent : AbstractInteractableComponent<ImageComponent?>() {

    private var textImage: TextImage? = null

    init {
        setTextImage(BasicTextImage(0, 0))
    }

    fun setTextImage(textImage: TextImage?) {
        this.textImage = textImage
        invalidate()
    }

    override fun createDefaultRenderer(): InteractableRenderer<ImageComponent?>? {
        return object : InteractableRenderer<ImageComponent?> {
            override fun drawComponent(graphics: TextGUIGraphics?, panel: ImageComponent?) {
                graphics!!.drawImage(TerminalPosition.TOP_LEFT_CORNER, textImage)
            }

            override fun getPreferredSize(panel: ImageComponent?): TerminalSize? {
                return textImage!!.size
            }

            override fun getCursorLocation(component: ImageComponent?): TerminalPosition? {
                return null
            }
        }
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        val superResult = super.handleKeyStroke(keyStroke)
        if (superResult !== Interactable.Result.UNHANDLED) {
            return superResult
        }
        return Interactable.Result.UNHANDLED
    }
}
