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
package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.*
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.Arrays
import java.util.EnumSet

/**
 * TextGraphics implementation that does nothing, but has a pre-defined size
 * @author martin
 */
internal class NullTextGraphics/**
 * Creates a new `NullTextGraphics` that will return the specified size value if asked how big it is but other
 * than that ignore all other calls.
 * @param size The size to report
 */
    (@get:Override
 val size:TerminalSize?):TextGraphics {
private var foregroundColor:TextColor? = null
private var backgroundColor:TextColor? = null
private var tabBehaviour:TabBehaviour? = null
private val activeModifiers:EnumSet<SGR?>?

init{
this.foregroundColor = TextColor.ANSI.DEFAULT
this.backgroundColor = TextColor.ANSI.DEFAULT
this.tabBehaviour = TabBehaviour.ALIGN_TO_COLUMN_4
this.activeModifiers = EnumSet.noneOf(SGR::class.java)
}

/**
 * The default implementation just returns null, as this Graphics never writes anywhere.
 * @param pos position to translate
 * @return null
 */
     fun toScreenPosition(pos:TerminalPosition?):TerminalPosition? {
return null
}

@Override
@Throws(IllegalArgumentException::class)
 fun newTextGraphics(topLeftCorner:TerminalPosition?, size:TerminalSize?):TextGraphics? {
return this
}

@Override
 fun getBackgroundColor():TextColor? {
return backgroundColor
}

@Override
 fun setBackgroundColor(backgroundColor:TextColor?):TextGraphics? {
this.backgroundColor = backgroundColor
return this
}

@Override
 fun getForegroundColor():TextColor? {
return foregroundColor
}

@Override
 fun setForegroundColor(foregroundColor:TextColor?):TextGraphics? {
this.foregroundColor = foregroundColor
return this
}

@Override
 fun enableModifiers(vararg modifiers:SGR?):TextGraphics? {
activeModifiers!!.addAll(Arrays.asList(modifiers))
return this
}

@Override
 fun disableModifiers(vararg modifiers:SGR?):TextGraphics? {
activeModifiers!!.removeAll(Arrays.asList(modifiers))
return this
}

@Override
 fun setModifiers(modifiers:EnumSet<SGR?>?):TextGraphics? {
clearModifiers()
activeModifiers!!.addAll(modifiers)
return this
}

@Override
 fun clearModifiers():TextGraphics? {
activeModifiers!!.clear()
return this
}

@Override
 fun getActiveModifiers():EnumSet<SGR?>? {
return EnumSet.copyOf(activeModifiers)
}

@Override
 fun getTabBehaviour():TabBehaviour? {
return tabBehaviour
}

@Override
 fun setTabBehaviour(tabBehaviour:TabBehaviour?):TextGraphics? {
this.tabBehaviour = tabBehaviour
return this
}

@Override
 fun fill(c:Char):TextGraphics? {
return this
}

@Override
 fun setCharacter(column:Int, row:Int, character:Char):TextGraphics? {
return this
}

@Override
 fun setCharacter(column:Int, row:Int, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun setCharacter(position:TerminalPosition?, character:Char):TextGraphics? {
return this
}

@Override
 fun setCharacter(position:TerminalPosition?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:Char):TextGraphics? {
return this
}

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:Char):TextGraphics? {
return this
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGraphics? {
return this
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGraphics? {
return this
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGraphics? {
return this
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGraphics? {
return this
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGraphics? {
return this
}

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?):TextGraphics? {
return this
}

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?, sourceImageTopLeft:TerminalPosition?, sourceImageSize:TerminalSize?):TextGraphics? {
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?):TextGraphics? {
return this
}

@Override
 fun putString(position:TerminalPosition?, string:String?):TextGraphics? {
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGraphics? {
return this
}

@Override
 fun putString(position:TerminalPosition?, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGraphics? {
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifiers:Collection<SGR?>?):TextGraphics? {
return this
}

@Override
 fun putCSIStyledString(column:Int, row:Int, string:String?):TextGraphics? {
return this
}

@Override
 fun putCSIStyledString(position:TerminalPosition?, string:String?):TextGraphics? {
return this
}

@Override
 fun getCharacter(column:Int, row:Int):TextCharacter? {
return null
}

@Override
 fun getCharacter(position:TerminalPosition?):TextCharacter? {
return null
}

@Override
 fun setStyleFrom(source:StyleSet<*>):TextGraphics? {
setBackgroundColor(source.getBackgroundColor())
setForegroundColor(source.getForegroundColor())
setModifiers(source.getActiveModifiers())
return this
}

}
