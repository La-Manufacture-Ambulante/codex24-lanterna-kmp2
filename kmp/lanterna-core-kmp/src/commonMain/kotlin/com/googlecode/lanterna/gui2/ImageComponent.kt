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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.input.*

/**
 * 
 * @author ginkoblongata
 */
 class ImageComponent:AbstractInteractableComponent() {

private var textImage:TextImage? = null
init{
setTextImage(BasicTextImage(0, 0))
}

 fun setTextImage(textImage:TextImage?) {
this.textImage = textImage
invalidate()
}

@Override
 fun createDefaultRenderer():InteractableRenderer<ImageComponent?>? {
return object:InteractableRenderer<ImageComponent?>() {
@Override
 fun drawComponent(graphics:TextGUIGraphics?, panel:ImageComponent?) {
graphics!!.drawImage(TerminalPosition.TOP_LEFT_CORNER, textImage)
}
@Override
 fun getPreferredSize(panel:ImageComponent?):TerminalSize? {
return textImage!!.getSize()
}
@Override
 fun getCursorLocation(component:ImageComponent?):TerminalPosition? {
 // when null, lanterna hidden cursor for this component
                return null
}
}
}

@Override
 fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
val superResult = super.handleKeyStroke(keyStroke)

 // just arrows and focus move stuff
        if (superResult !== Result.UNHANDLED)
{
return superResult
}

return Result.UNHANDLED
}

}
