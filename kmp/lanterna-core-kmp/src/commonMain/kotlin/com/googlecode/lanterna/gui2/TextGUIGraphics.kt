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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.EnumSet

/**
 * TextGraphics implementation used by TextGUI when doing any drawing operation.
 * @author Martin
 */
 interface TextGUIGraphics:ThemedTextGraphics, TextGraphics {
/**
 * Returns the `TextGUI` this `TextGUIGraphics` belongs to
 * @return `TextGUI` this `TextGUIGraphics` belongs to
 */
     val textGUI:TextGUI?

@Override
@Throws(IllegalArgumentException::class)
 fun newTextGraphics(topLeftCorner:TerminalPosition?, size:TerminalSize?):TextGUIGraphics? 

@Override
 fun applyThemeStyle(themeStyle:ThemeStyle?):TextGUIGraphics? 

@Override
 fun setBackgroundColor(backgroundColor:TextColor?):TextGUIGraphics? 

@Override
 fun setForegroundColor(foregroundColor:TextColor?):TextGUIGraphics? 

@Override
 fun enableModifiers(vararg modifiers:SGR?):TextGUIGraphics? 

@Override
 fun disableModifiers(vararg modifiers:SGR?):TextGUIGraphics? 

@Override
 fun setModifiers(modifiers:EnumSet<SGR?>?):TextGUIGraphics? 

@Override
 fun clearModifiers():TextGUIGraphics? 

@Override
 fun setTabBehaviour(tabBehaviour:TabBehaviour?):TextGUIGraphics? 

@Override
 fun fill(c:Char):TextGUIGraphics? 

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGUIGraphics? 

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGUIGraphics? 

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGUIGraphics? 

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGUIGraphics? 

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:Char):TextGUIGraphics? 

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:Char):TextGUIGraphics? 

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?):TextGUIGraphics? 

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?, sourceImageTopLeft:TerminalPosition?, sourceImageSize:TerminalSize?):TextGUIGraphics? 

@Override
 fun setCharacter(position:TerminalPosition?, character:Char):TextGUIGraphics? 

@Override
 fun setCharacter(position:TerminalPosition?, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun setCharacter(column:Int, row:Int, character:Char):TextGUIGraphics? 

@Override
 fun setCharacter(column:Int, row:Int, character:TextCharacter?):TextGUIGraphics? 

@Override
 fun putString(column:Int, row:Int, string:String?):TextGUIGraphics? 

@Override
 fun putString(position:TerminalPosition?, string:String?):TextGUIGraphics? 

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGUIGraphics? 

@Override
 fun putString(position:TerminalPosition?, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGUIGraphics? 

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifiers:Collection<SGR?>?):TextGUIGraphics? 

@Override
 fun putCSIStyledString(column:Int, row:Int, string:String?):TextGUIGraphics? 

@Override
 fun putCSIStyledString(position:TerminalPosition?, string:String?):TextGUIGraphics? 

@Override
 fun setStyleFrom(source:StyleSet<*>?):TextGUIGraphics? 

}
