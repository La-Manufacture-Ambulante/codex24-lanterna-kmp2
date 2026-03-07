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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

import java.io.IOException

/**
 * VirtualScreen wraps a normal screen and presents it as a screen that has a configurable minimum size; if the real
 * screen is smaller than this size, the presented screen will add scrolling to get around it. To anyone using this
 * class, it will appear and behave just as a normal screen. Scrolling is done by using CTRL + arrow keys.
 * 
 * 
 * The use case for this class is to allow you to set a minimum size that you can count on be honored, no matter how
 * small the user makes the terminal. This should make programming GUIs easier.
 * @author Martin
 */
 class VirtualScreen/**
 * Creates a new VirtualScreen that wraps a supplied Screen. The screen passed in here should be the real screen
 * that is created on top of the real `Terminal`, it will have the correct size and content for what's
 * actually displayed to the user, but this class will present everything as one view with a fixed minimum size,
 * no matter what size the real terminal has.
 * 
 * 
 * The initial minimum size will be the current size of the screen.
 * @param screen Real screen that will be used when drawing the whole or partial virtual screen
 */
    (private val realScreen:Screen?):AbstractScreen(realScreen.getTerminalSize()) {
private val frameRenderer:FrameRenderer?
private var minimumSize:TerminalSize? = null
private var viewportTopLeft:TerminalPosition? = null
/**
 * Returns the current size of the viewport. This will generally match the dimensions of the underlying terminal.
 * @return Viewport size for this [VirtualScreen]
 */
     var viewportSize:TerminalSize? = null
private set
private var scrollWithCTRL:Boolean = false

init{
this.frameRenderer = DefaultFrameRenderer()
this.minimumSize = realScreen.getTerminalSize()
this.viewportTopLeft = TerminalPosition.TOP_LEFT_CORNER
this.viewportSize = minimumSize
this.scrollWithCTRL = false
}

/**
 * Sets the minimum size we want the virtual screen to have. If the user resizes the real terminal to something
 * smaller than this, the virtual screen will refuse to make it smaller and add scrollbars to the view.
 * @param minimumSize Minimum size we want the screen to have
 */
     fun setMinimumSize(minimumSize:TerminalSize) {
this.minimumSize = minimumSize
val virtualSize = minimumSize.max(realScreen!!.getTerminalSize())
if (!minimumSize.equals(virtualSize))
{
addResizeRequest(virtualSize)
super.doResizeIfNecessary()
}
calculateViewport(realScreen!!.getTerminalSize())
}

/**
 * Returns the minimum size this virtual screen can have. If the real terminal is made smaller than this, the
 * virtual screen will draw scrollbars and implement scrolling
 * @return Minimum size configured for this virtual screen
 */
     fun getMinimumSize():TerminalSize? {
return minimumSize
}

/**
 * When the viewport is too small, user can scroll using ALT + arrow keys, but ALT can be replaced by CTRL by
 * calling this method.
 * @param scrollOnCTRL Scroll using CTRL instead of ALT if set to `true`, ALT if `false`
 */
     fun setScrollOnCTRL(scrollOnCTRL:Boolean) {
this.scrollWithCTRL = scrollOnCTRL
}

 fun setViewportTopLeft(position:TerminalPosition?) {
viewportTopLeft = position
while (viewportTopLeft!!.column > 0 && viewportTopLeft!!.column + viewportSize!!.columns > minimumSize!!.columns)
{
viewportTopLeft = viewportTopLeft!!.withRelativeColumn(-1)
}
while (viewportTopLeft!!.row > 0 && viewportTopLeft!!.row + viewportSize!!.rows > minimumSize!!.rows)
{
viewportTopLeft = viewportTopLeft!!.withRelativeRow(-1)
}
}

@Override
@Throws(IOException::class)
 fun startScreen() {
realScreen!!.startScreen()
}

@Override
@Throws(IOException::class)
 fun stopScreen() {
realScreen!!.stopScreen()
}

@Override
 fun getFrontCharacter(position:TerminalPosition?):TextCharacter? {
return null
}

@Override
 fun setCursorPosition(position:TerminalPosition?) {
var position = position
super.setCursorPosition(position)
if (position == null)
{
realScreen!!.setCursorPosition(null)
return 
}
position = position!!.withRelativeColumn(-viewportTopLeft!!.column)!!.withRelativeRow(-viewportTopLeft!!.row)
if ((position!!.column >= 0 && position!!.column < viewportSize!!.columns && 
position!!.row >= 0 && position!!.row < viewportSize!!.rows))
{
realScreen!!.setCursorPosition(position)
}
else
{
realScreen!!.setCursorPosition(null)
}
}

@Override
@Synchronized  fun doResizeIfNecessary():TerminalSize? {
val underlyingSize = realScreen!!.doResizeIfNecessary()
if (underlyingSize == null)
{
return null
}

val newVirtualSize = calculateViewport(underlyingSize)
if (!getTerminalSize().equals(newVirtualSize))
{
addResizeRequest(newVirtualSize)
return super.doResizeIfNecessary()
}
return newVirtualSize
}

private fun calculateViewport(realTerminalSize:TerminalSize?):TerminalSize {
val newVirtualSize = minimumSize!!.max(realTerminalSize!!)
if (newVirtualSize!!.equals(realTerminalSize))
{
viewportSize = realTerminalSize
viewportTopLeft = TerminalPosition.TOP_LEFT_CORNER
}
else
{
val newViewportSize = frameRenderer!!.getViewportSize(realTerminalSize, newVirtualSize)
if (newViewportSize!!.rows > viewportSize!!.rows)
{
viewportTopLeft = viewportTopLeft!!.withRow(Math.max(0, viewportTopLeft!!.row - (newViewportSize!!.rows - viewportSize!!.rows)))
}
if (newViewportSize!!.columns > viewportSize!!.columns)
{
viewportTopLeft = viewportTopLeft!!.withColumn(Math.max(0, viewportTopLeft!!.column - (newViewportSize!!.columns - viewportSize!!.columns)))
}
viewportSize = newViewportSize
}
return newVirtualSize
}

@Override
@Throws(IOException::class)
 fun refresh(refreshType:RefreshType?) {
setCursorPosition(getCursorPosition()) //Make sure the cursor is at the correct position
if (!viewportSize!!.equals(realScreen!!.getTerminalSize()))
{
frameRenderer!!.drawFrame(
realScreen!!.newTextGraphics(), 
realScreen!!.getTerminalSize(), 
getTerminalSize(), 
viewportTopLeft)
}

 //Copy the rows
        val viewportOffset = frameRenderer!!.viewportOffset
if (realScreen is AbstractScreen)
{
val asAbstractScreen = realScreen as AbstractScreen?
getBackBuffer().copyTo(
asAbstractScreen!!.getBackBuffer(), 
viewportTopLeft!!.row, 
viewportSize!!.rows, 
viewportTopLeft!!.column, 
viewportSize!!.columns, 
viewportOffset!!.row, 
viewportOffset!!.column)
}
else
{
for (y in 0 until viewportSize!!.rows)
{
for (x in 0 until viewportSize!!.columns)
{
realScreen!!.setCharacter(
x + viewportOffset!!.column, 
y + viewportOffset!!.row, 
getBackBuffer().getCharacterAt(
x + viewportTopLeft!!.column, 
y + viewportTopLeft!!.row))
}
}
}
realScreen!!.refresh(refreshType)
}

@Override
@Throws(IOException::class)
 fun pollInput():KeyStroke? {
return filter(realScreen!!.pollInput())
}

@Override
@Throws(IOException::class)
 fun readInput():KeyStroke? {
return filter(realScreen!!.readInput())
}

@Throws(IOException::class)
private fun filter(keyStroke:KeyStroke?):KeyStroke? {
if (keyStroke == null)
{
return null
}
else if (isScrollTrigger(keyStroke) && keyStroke!!.getKeyType() === KeyType.ARROW_LEFT)
{
if (viewportTopLeft!!.column > 0)
{
viewportTopLeft = viewportTopLeft!!.withRelativeColumn(-1)
refresh()
return null
}
}
else if (isScrollTrigger(keyStroke) && keyStroke!!.getKeyType() === KeyType.ARROW_RIGHT)
{
if (viewportTopLeft!!.column + viewportSize!!.columns < getTerminalSize().getColumns())
{
viewportTopLeft = viewportTopLeft!!.withRelativeColumn(1)
refresh()
return null
}
}
else if (isScrollTrigger(keyStroke) && keyStroke!!.getKeyType() === KeyType.ARROW_UP)
{
if (viewportTopLeft!!.row > 0)
{
viewportTopLeft = viewportTopLeft!!.withRelativeRow(-1)
realScreen!!.scrollLines(0, viewportSize!!.rows - 1, -1)
refresh()
return null
}
}
else if (isScrollTrigger(keyStroke) && keyStroke!!.getKeyType() === KeyType.ARROW_DOWN)
{
if (viewportTopLeft!!.row + viewportSize!!.rows < getTerminalSize().getRows())
{
viewportTopLeft = viewportTopLeft!!.withRelativeRow(1)
realScreen!!.scrollLines(0, viewportSize!!.rows - 1, 1)
refresh()
return null
}
}
else if (isScrollTrigger(keyStroke) && keyStroke!!.getKeyType() === KeyType.PAGE_UP)
{
if (viewportTopLeft!!.row > 0)
{
val scroll = Math.min(viewportSize!!.rows, viewportTopLeft!!.row)
viewportTopLeft = viewportTopLeft!!.withRelativeRow(-scroll)
realScreen!!.scrollLines(0, viewportSize!!.rows - scroll, -scroll)
refresh()
return null
}
}
else if (isScrollTrigger(keyStroke) && (keyStroke!!.getKeyType() === KeyType.PAGE_DOWN || isSpaceBarPress(keyStroke!!)))
{
if (viewportTopLeft!!.row + viewportSize!!.rows < getTerminalSize().getRows())
{
var scroll = viewportSize!!.rows
if (viewportTopLeft!!.row + viewportSize!!.rows + scroll >= getTerminalSize().getRows())
{
scroll = getTerminalSize().getRows() - viewportTopLeft!!.row - viewportSize!!.rows
}
viewportTopLeft = viewportTopLeft!!.withRelativeRow(scroll)
realScreen!!.scrollLines(0, viewportSize!!.rows - scroll, scroll)
refresh()
return null
}
}
return keyStroke
}

private fun isSpaceBarPress(keyStroke:KeyStroke):Boolean {
return keyStroke.getKeyType() === KeyType.CHARACTER && keyStroke.getCharacter() === ' '
}

private fun isScrollTrigger(keyStroke:KeyStroke?):Boolean {
return if (scrollWithCTRL) keyStroke!!.isCtrlDown() else keyStroke!!.isAltDown()
}

@Override
 fun scrollLines(firstLine:Int, lastLine:Int, distance:Int) {
var firstLine = firstLine
var lastLine = lastLine
 // do base class stuff (scroll own back buffer)
        super.scrollLines(firstLine, lastLine, distance)
 // vertical range visible in realScreen:
        val vpFirst = viewportTopLeft!!.row
val vpRows = viewportSize!!.rows
 // adapt to realScreen range:
        firstLine = Math.max(0, firstLine - vpFirst)
lastLine = Math.min(vpRows - 1, lastLine - vpFirst)
 // if resulting range non-empty: scroll that range in realScreen:
        if (firstLine <= lastLine)
{
realScreen!!.scrollLines(firstLine, lastLine, distance)
}
}

/**
 * Interface for rendering the virtual screen's frame when the real terminal is too small for the virtual screen
 */
     interface FrameRenderer {

/**
 * Where in the virtual screen should the top-left position of the viewport be? To draw the viewport from the
 * top-left position of the screen, return 0x0 (or TerminalPosition.TOP_LEFT_CORNER) here.
 * @return Position of the top-left corner of the viewport inside the screen
 */
         val viewportOffset:TerminalPosition?
/**
 * Given the size of the real terminal and the current size of the virtual screen, how large should the viewport
 * where the screen content is drawn be?
 * @param realSize Size of the real terminal
 * @param virtualSize Size of the virtual screen
 * @return Size of the viewport, according to this FrameRenderer
 */
         fun getViewportSize(realSize:TerminalSize?, virtualSize:TerminalSize?):TerminalSize? 

/**
 * Drawn the 'frame', meaning anything that is outside the viewport (title, scrollbar, etc)
 * @param graphics Graphics to use to text drawing operations
 * @param realSize Size of the real terminal
 * @param virtualSize Size of the virtual screen
 * @param virtualScrollPosition If the virtual screen is larger than the real terminal, this is the current
 * scroll offset the VirtualScreen is using
 */
         fun drawFrame(
graphics:TextGraphics?, 
realSize:TerminalSize?, 
virtualSize:TerminalSize?, 
virtualScrollPosition:TerminalPosition?) 
}

private class DefaultFrameRenderer:FrameRenderer {

public override val viewportOffset:TerminalPosition
@Override
get() {
return TerminalPosition.TOP_LEFT_CORNER
}
@Override
public override fun getViewportSize(realSize:TerminalSize, virtualSize:TerminalSize?):TerminalSize? {
if (realSize.columns > 1 && realSize.rows > 2)
{
return realSize.withRelativeColumns(-1)!!.withRelativeRows(-2)
}
else
{
return realSize
}
}

@Override
public override fun drawFrame(
graphics:TextGraphics?, 
realSize:TerminalSize, 
virtualSize:TerminalSize?, 
virtualScrollPosition:TerminalPosition?) {

if (realSize.columns == 1 || realSize.rows <= 2)
{
return 
}
val viewportSize = getViewportSize(realSize, virtualSize)

graphics!!.setForegroundColor(TextColor.ANSI.WHITE)
graphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
graphics!!.fill(' ')
graphics!!.putString(0, graphics!!.getSize().getRows() - 1, "Terminal too small, use ALT+arrows to scroll")

val horizontalSize = (((viewportSize!!.columns).toDouble() / virtualSize!!.columns.toDouble()) * (viewportSize!!.columns)).toInt()
var scrollable = viewportSize!!.columns - horizontalSize - 1
val horizontalPosition = (scrollable.toDouble() * (virtualScrollPosition!!.column.toDouble() / (virtualSize!!.columns - viewportSize!!.columns).toDouble())).toInt()
graphics!!.drawLine(
TerminalPosition(horizontalPosition, graphics!!.getSize().getRows() - 2), 
TerminalPosition(horizontalPosition + horizontalSize, graphics!!.getSize().getRows() - 2), 
Symbols.BLOCK_MIDDLE)

val verticalSize = (((viewportSize!!.rows).toDouble() / virtualSize!!.rows.toDouble()) * (viewportSize!!.rows)).toInt()
scrollable = viewportSize!!.rows - verticalSize - 1
val verticalPosition = (scrollable.toDouble() * (virtualScrollPosition!!.row.toDouble() / (virtualSize!!.rows - viewportSize!!.rows).toDouble())).toInt()
graphics!!.drawLine(
TerminalPosition(graphics!!.getSize().getColumns() - 1, verticalPosition), 
TerminalPosition(graphics!!.getSize().getColumns() - 1, verticalPosition + verticalSize), 
Symbols.BLOCK_MIDDLE)
}
}
}
