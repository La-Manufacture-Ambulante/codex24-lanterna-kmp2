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

/**
 * The default window manager implementation used by Lanterna. New windows will be generally added in a tiled manner,
 * starting in the top-left corner and moving down-right as new windows are added. By using the various window hints
 * that are available you have some control over how the window manager will place and size the windows.
 * 
 * @author Martin
 */
 class DefaultWindowManager/**
 * Creates a new `DefaultWindowManager` using a specified `windowDecorationRendererOverride` for drawing window
 * decorations. Any size calculations done before the text GUI has actually been started and displayed on the
 * terminal will use the size passed in with the `initialScreenSize` parameter
 * 
 * @param windowDecorationRenderer Window decoration renderer to use when drawing windows
 * @param initialScreenSize Size to assume the terminal has until the text GUI is started and can be notified of the
 * correct size
 */
    (private val windowDecorationRendererOverride:WindowDecorationRenderer?, initialScreenSize:TerminalSize?):WindowManager {
private var lastKnownScreenSize:TerminalSize? = null

 val isInvalid:Boolean
@Override
get() {
return false
}

/**
 * Creates a new `DefaultWindowManager` using a `DefaultWindowDecorationRenderer` for drawing window
 * decorations, unless the current theme has an override. Any size calculations done before the text GUI has
 * actually been started and displayed on the terminal will use the size passed in with the
 * `initialScreenSize` parameter (if `null` then size will be assumed to be 80x24)
 * 
 * @param initialScreenSize Size to assume the terminal has until the text GUI is started and can be notified of the
 * correct size
 */
    @JvmOverloads  constructor(initialScreenSize:TerminalSize? = null) : this(null, initialScreenSize) {}

init{
if (initialScreenSize != null)
{
this.lastKnownScreenSize = initialScreenSize
}
else
{
this.lastKnownScreenSize = TerminalSize(80, 24)
}
}

@Override
 fun getWindowDecorationRenderer(window:Window):WindowDecorationRenderer? {
if (window.getHints().contains(Window.Hint.NO_DECORATIONS))
{
return EmptyWindowDecorationRenderer()
}
else if (windowDecorationRendererOverride != null)
{
return windowDecorationRendererOverride
}
else if (window.getTheme() != null && window.getTheme().getWindowDecorationRenderer() != null)
{
return window.getTheme().getWindowDecorationRenderer()
}
else
{
return DefaultWindowDecorationRenderer()
}
}

@Override
 fun onAdded(textGUI:WindowBasedTextGUI?, window:Window?, allWindows:List<Window?>?) {
val decorationRenderer = getWindowDecorationRenderer(window!!)
val expectedDecoratedSize = decorationRenderer!!.getDecoratedSize(window, window!!.getPreferredSize())
window!!.setDecoratedSize(expectedDecoratedSize)


        if (window!!.getHints().contains(Window.Hint.FIXED_POSITION))
{
 //Don't place the window, assume the position is already set
        }
else if (allWindows!!.isEmpty())
{
window!!.setPosition(TerminalPosition.OFFSET_1x1)
}
else if (window!!.getHints().contains(Window.Hint.CENTERED))
{
val left = (lastKnownScreenSize!!.columns - expectedDecoratedSize!!.columns) / 2
val top = (lastKnownScreenSize!!.rows - expectedDecoratedSize!!.rows) / 2
window!!.setPosition(TerminalPosition(left, top))
}
else
{
var nextPosition = allWindows!!.get(allWindows!!.size() - 1).getPosition().withRelative(2, 1)
if ((nextPosition!!.column + expectedDecoratedSize!!.columns > lastKnownScreenSize!!.columns || nextPosition!!.row + expectedDecoratedSize!!.rows > lastKnownScreenSize!!.rows))
{
nextPosition = TerminalPosition.OFFSET_1x1
}
window!!.setPosition(nextPosition)
}

 // Finally, run through the usual calculations so the window manager's usual prepare method can have it's say
        prepareWindow(lastKnownScreenSize, window!!)
}

@Override
 fun onRemoved(textGUI:WindowBasedTextGUI?, window:Window?, allWindows:List<Window?>?) {
 //NOP
    }

@Override
 fun prepareWindows(textGUI:WindowBasedTextGUI?, allWindows:List<Window?>, screenSize:TerminalSize?) {
this.lastKnownScreenSize = screenSize
for (window in allWindows)
{
prepareWindow(screenSize, window!!)
}
}

/**
 * Called by [DefaultWindowManager] when iterating through all windows to decide their size and position. If
 * you override [DefaultWindowManager] to add your own logic to how windows are placed on the screen, you can
 * override this method and selectively choose which window to interfere with. Note that the two key properties that
 * are read by the GUI system after preparing all windows are the position and decorated size. Your custom
 * implementation should set these two fields directly on the window. You can infer the decorated size from the
 * content size by using the window decoration renderer that is attached to the window manager.
 * 
 * @param screenSize Size of the terminal that is available to draw on
 * @param window Window to prepare decorated size and position for
 */
    protected fun prepareWindow(screenSize:TerminalSize?, window:Window) {
val contentAreaSize:TerminalSize?
if (window.getHints().contains(Window.Hint.FIXED_SIZE))
{
contentAreaSize = window.getSize()
}
else
{
contentAreaSize = window.getPreferredSize()
}
var size = getWindowDecorationRenderer(window)!!.getDecoratedSize(window, contentAreaSize)
var position = window.getPosition()

if (window.getHints().contains(Window.Hint.FULL_SCREEN))
{
position = TerminalPosition.TOP_LEFT_CORNER
size = screenSize
}
else if (window.getHints().contains(Window.Hint.EXPANDED))
{
position = TerminalPosition.OFFSET_1x1
size = screenSize!!.withRelative(
-Math.min(4, screenSize!!.columns), 
-Math.min(3, screenSize!!.rows))
if (!size!!.equals(window.getDecoratedSize()))
{
window.invalidate()
}
}
else if ((window.getHints().contains(Window.Hint.FIT_TERMINAL_WINDOW) || window.getHints().contains(Window.Hint.CENTERED)))
{
 //If the window is too big for the terminal, move it up towards 0x0 and if that's not enough then shrink
            //it instead
            while (position!!.row > 0 && position!!.row + size!!.rows > screenSize!!.rows)
{
position = position!!.withRelativeRow(-1)
}
while (position!!.column > 0 && position!!.column + size!!.columns > screenSize!!.columns)
{
position = position!!.withRelativeColumn(-1)
}
if (position!!.row + size!!.rows > screenSize!!.rows)
{
size = size!!.withRows(screenSize!!.rows - position!!.row)
}
if (position!!.column + size!!.columns > screenSize!!.columns)
{
size = size!!.withColumns(screenSize!!.columns - position!!.column)
}
if (window.getHints().contains(Window.Hint.CENTERED))
{
val left = (lastKnownScreenSize!!.columns - size!!.columns) / 2
val top = (lastKnownScreenSize!!.rows - size!!.rows) / 2
position = TerminalPosition(left, top)
}
}

window.setPosition(position)
window.setDecoratedSize(size)
}
}/**
 * Default constructor, will create a window manager that uses `DefaultWindowDecorationRenderer` for drawing
 * window decorations, unless the current theme has an override. Any size calculations done before the text GUI has
 * actually been started and displayed on the terminal will assume the terminal size is 80x24.
 */
