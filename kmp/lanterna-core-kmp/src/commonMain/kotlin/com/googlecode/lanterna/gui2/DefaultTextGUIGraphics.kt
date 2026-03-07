package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.EnumSet

/**
 * Created by Martin on 2017-08-11.
 */
 class DefaultTextGUIGraphics internal constructor(@get:Override
 val textGUI:TextGUI?, private val backend:TextGraphics?):TextGUIGraphics {

 val size:TerminalSize?
@Override
get() {
return backend!!.getSize()
}

 val backgroundColor:TextColor?
@Override
get() {
return backend!!.getBackgroundColor()
}

 val foregroundColor:TextColor?
@Override
get() {
return backend!!.getForegroundColor()
}

 val activeModifiers:EnumSet<SGR?>?
@Override
get() {
return backend!!.getActiveModifiers()
}

 val tabBehaviour:TabBehaviour?
@Override
get() {
return backend!!.getTabBehaviour()
}

@Override
@Throws(IllegalArgumentException::class)
 fun newTextGraphics(topLeftCorner:TerminalPosition?, size:TerminalSize?):DefaultTextGUIGraphics {
return DefaultTextGUIGraphics(textGUI, backend!!.newTextGraphics(topLeftCorner, size))
}

@Override
 fun applyThemeStyle(themeStyle:ThemeStyle):DefaultTextGUIGraphics {
setForegroundColor(themeStyle.getForeground())
setBackgroundColor(themeStyle.getBackground())
setModifiers(themeStyle.getSGRs())
return this
}

@Override
 fun setBackgroundColor(backgroundColor:TextColor?):DefaultTextGUIGraphics {
backend!!.setBackgroundColor(backgroundColor)
return this
}

@Override
 fun setForegroundColor(foregroundColor:TextColor?):DefaultTextGUIGraphics {
backend!!.setForegroundColor(foregroundColor)
return this
}

@Override
 fun enableModifiers(vararg modifiers:SGR?):DefaultTextGUIGraphics {
backend!!.enableModifiers(modifiers)
return this
}

@Override
 fun disableModifiers(vararg modifiers:SGR?):DefaultTextGUIGraphics {
backend!!.disableModifiers(modifiers)
return this
}

@Override
 fun setModifiers(modifiers:EnumSet<SGR?>?):DefaultTextGUIGraphics {
backend!!.setModifiers(modifiers)
return this
}

@Override
 fun clearModifiers():DefaultTextGUIGraphics {
backend!!.clearModifiers()
return this
}

@Override
 fun setTabBehaviour(tabBehaviour:TabBehaviour?):DefaultTextGUIGraphics {
backend!!.setTabBehaviour(tabBehaviour)
return this
}

@Override
 fun fill(c:Char):DefaultTextGUIGraphics {
backend!!.fill(c)
return this
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):DefaultTextGUIGraphics {
backend!!.fillRectangle(topLeft, size, character)
return this
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.fillRectangle(topLeft, size, character)
return this
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):DefaultTextGUIGraphics {
backend!!.drawRectangle(topLeft, size, character)
return this
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.drawRectangle(topLeft, size, character)
return this
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):DefaultTextGUIGraphics {
backend!!.fillTriangle(p1, p2, p3, character)
return this
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.fillTriangle(p1, p2, p3, character)
return this
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):DefaultTextGUIGraphics {
backend!!.drawTriangle(p1, p2, p3, character)
return this
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.drawTriangle(p1, p2, p3, character)
return this
}

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:Char):DefaultTextGUIGraphics {
backend!!.drawLine(fromPoint, toPoint, character)
return this
}

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.drawLine(fromPoint, toPoint, character)
return this
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:Char):DefaultTextGUIGraphics {
backend!!.drawLine(fromX, fromY, toX, toY, character)
return this
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.drawLine(fromX, fromY, toX, toY, character)
return this
}

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?):DefaultTextGUIGraphics {
backend!!.drawImage(topLeft, image)
return this
}

@Override
 fun drawImage(topLeft:TerminalPosition?, image:TextImage?, sourceImageTopLeft:TerminalPosition?, sourceImageSize:TerminalSize?):DefaultTextGUIGraphics {
backend!!.drawImage(topLeft, image, sourceImageTopLeft, sourceImageSize)
return this
}

@Override
 fun setCharacter(position:TerminalPosition?, character:Char):DefaultTextGUIGraphics {
backend!!.setCharacter(position, character)
return this
}

@Override
 fun setCharacter(position:TerminalPosition?, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.setCharacter(position, character)
return this
}

@Override
 fun setCharacter(column:Int, row:Int, character:Char):DefaultTextGUIGraphics {
backend!!.setCharacter(column, row, character)
return this
}

@Override
 fun setCharacter(column:Int, row:Int, character:TextCharacter?):DefaultTextGUIGraphics {
backend!!.setCharacter(column, row, character)
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?):DefaultTextGUIGraphics {
backend!!.putString(column, row, string)
return this
}

@Override
 fun putString(position:TerminalPosition?, string:String?):DefaultTextGUIGraphics {
backend!!.putString(position, string)
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):DefaultTextGUIGraphics {
backend!!.putString(column, row, string, extraModifier, optionalExtraModifiers)
return this
}

@Override
 fun putString(position:TerminalPosition?, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):DefaultTextGUIGraphics {
backend!!.putString(position, string, extraModifier, optionalExtraModifiers)
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifiers:Collection<SGR?>?):DefaultTextGUIGraphics {
backend!!.putString(column, row, string, extraModifiers)
return this
}

@Override
 fun putCSIStyledString(column:Int, row:Int, string:String?):DefaultTextGUIGraphics {
backend!!.putCSIStyledString(column, row, string)
return this
}

@Override
 fun putCSIStyledString(position:TerminalPosition?, string:String?):DefaultTextGUIGraphics {
backend!!.putCSIStyledString(position, string)
return this
}

@Override
 fun getCharacter(column:Int, row:Int):TextCharacter? {
return backend!!.getCharacter(column, row)
}

@Override
 fun getCharacter(position:TerminalPosition?):TextCharacter? {
return backend!!.getCharacter(position)
}

@Override
 fun setStyleFrom(source:StyleSet<*>):DefaultTextGUIGraphics {
setBackgroundColor(source.getBackgroundColor())
setForegroundColor(source.getForegroundColor())
setModifiers(source.getActiveModifiers())
return this
}

@Override
 fun toScreenPosition(pos:TerminalPosition?):TerminalPosition? {
return backend!!.toScreenPosition(pos)
}
}
