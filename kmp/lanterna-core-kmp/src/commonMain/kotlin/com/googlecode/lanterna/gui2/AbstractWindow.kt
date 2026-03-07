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
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyType

import java.util.*

/**
 * Abstract Window has most of the code requiring for a window to function, all concrete window implementations extends
 * from this in one way or another. You can define your own window by extending from this, as an alternative to building
 * up the GUI externally by constructing a `BasicWindow` and adding components to it.
 * @author Martin
 */
abstract class AbstractWindow/**
 * Creates a window with a specific title that will (probably) be drawn in the window decorations
 * @param title Title of this window
 */
     @JvmOverloads  constructor(private var title:String? = ""):AbstractBasePane<Window?>(), Window {
private var textGUI:WindowBasedTextGUI? = null
@get:Override
@set:Override
 var isVisible:Boolean = false
private var lastKnownSize:TerminalSize? = null
@get:Override
@set:Override
 var decoratedSize:TerminalSize? = null
private var lastKnownPosition:TerminalPosition? = null
private var contentOffset:TerminalPosition? = null
private var hints:Set<Hint?>? = null
@get:Override
 var postRenderer:WindowPostRenderer? = null
private set
private var closeWindowWithEscape:Boolean = false

 val preferredSize:TerminalSize?
@Override
get() {
var preferredSize = contentHolder.getPreferredSize()
val menuBar = getMenuBar()
if (menuBar!!.getMenuCount() > 0)
{
val menuPreferredSize = menuBar!!.getPreferredSize()
preferredSize = preferredSize!!.withRelativeRows(menuPreferredSize!!.rows)!!
.withColumns(Math.max(menuPreferredSize!!.columns, preferredSize!!.columns))
}
return preferredSize
}

 // Fire listeners
 var position:TerminalPosition?
@Override
get() {
return lastKnownPosition
}
@Override
set(topLeft) {
val oldPosition = this.lastKnownPosition
this.lastKnownPosition = topLeft
for (listener in getBasePaneListeners())
{
if (listener is WindowListener)
{
(listener as WindowListener).onMoved(this, oldPosition, topLeft)
}
}
}

 var size:TerminalSize?
@Override
get() {
return lastKnownSize
}
@Override
@Deprecated
set(size) {
setSize(size, true)
}

init{
this.textGUI = null
this.isVisible = true
this.contentOffset = TerminalPosition.TOP_LEFT_CORNER
this.lastKnownPosition = null
this.lastKnownSize = null
this.decoratedSize = null
this.closeWindowWithEscape = false

this.hints = HashSet()
}

/**
 * Setting this property to `true` will cause pressing the ESC key to close the window. This used to be the
 * default behaviour of lanterna 3 during the development cycle but is not longer the case. You are encouraged to
 * put proper buttons or other kind of components to clearly mark to the user how to close the window instead of
 * magically taking ESC, but sometimes it can be useful (when doing testing, for example) to enable this mode.
 * @param closeWindowWithEscape If `true`, this window will self-close if you press ESC key
 */
     fun setCloseWindowWithEscape(closeWindowWithEscape:Boolean) {
this.closeWindowWithEscape = closeWindowWithEscape
}

@Override
 fun setTextGUI(textGUI:WindowBasedTextGUI?) {
 //This is kind of stupid check, but might cause it to blow up on people using the library incorrectly instead of
        //just causing weird behaviour
        if (this.textGUI != null && textGUI != null)
{
throw UnsupportedOperationException(("Are you calling setTextGUI yourself? Please read the documentation" 
+ " in that case (this could also be a bug in Lanterna, please report it if you are sure you are " 
+ "not calling Window.setTextGUI(..) from your code)"))
}
this.textGUI = textGUI
}

@Override
 fun getTextGUI():WindowBasedTextGUI? {
return textGUI
}

/**
 * Alters the title of the window to the supplied string
 * @param title New title of the window
 */
     fun setTitle(title:String?) {
this.title = title
invalidate()
}

@Override
 fun getTitle():String? {
return title
}

@Override
 fun draw(graphics:TextGUIGraphics) {
if (!graphics.getSize().equals(lastKnownSize))
{
getComponent().invalidate()
}
setSize(graphics.getSize(), false)
super.draw(graphics)
}

@Override
 fun handleInput(key:KeyStroke?):Boolean {
val handled = super.handleInput(key)
if (!handled && closeWindowWithEscape && key!!.getKeyType() === KeyType.ESCAPE)
{
close()
return true
}
return handled
}

/**
 * @see Window.toGlobalFromContentRelative
 */
    @Override
@Deprecated
 fun toGlobal(localPosition:TerminalPosition?):TerminalPosition? {
return toGlobalFromContentRelative(localPosition)
}

@Override
 fun toGlobalFromContentRelative(contentLocalPosition:TerminalPosition?):TerminalPosition? {
if (contentLocalPosition == null)
{
return null
}
return lastKnownPosition!!.withRelative(contentOffset!!.withRelative(contentLocalPosition!!)!!)
}

@Override
@Deprecated
 fun toGlobalFromDecoratedRelative(localPosition:TerminalPosition?):TerminalPosition? {
if (localPosition == null)
{
return null
}
return lastKnownPosition!!.withRelative(localPosition!!)
}

/**
 * @see Window.fromGlobalToContentRelative
 */
    @Override
@Deprecated
 fun fromGlobal(globalPosition:TerminalPosition?):TerminalPosition? {
return fromGlobalToContentRelative(globalPosition)
}

@Override
 fun fromGlobalToContentRelative(globalPosition:TerminalPosition?):TerminalPosition? {
if (globalPosition == null || lastKnownPosition == null)
{
return null
}
return globalPosition!!.withRelative(
-lastKnownPosition!!.column - contentOffset!!.column, 
-lastKnownPosition!!.row - contentOffset!!.row)
}

@Override
 fun fromGlobalToDecoratedRelative(globalPosition:TerminalPosition?):TerminalPosition? {
if (globalPosition == null || lastKnownPosition == null)
{
return null
}
return globalPosition!!.withRelative(
-lastKnownPosition!!.column, 
-lastKnownPosition!!.row)
}

@Override
 fun setHints(hints:Collection<Hint?>?) {
this.hints = HashSet(hints)
invalidate()
}

@Override
 fun getHints():Set<Hint?>? {
return Collections.unmodifiableSet(hints)
}

@Override
 fun addWindowListener(windowListener:WindowListener?) {
addBasePaneListener(windowListener)
}

@Override
 fun removeWindowListener(windowListener:WindowListener?) {
removeBasePaneListener(windowListener)
}

/**
 * Sets the post-renderer to use for this window. This will override the default from the GUI system (if there is
 * one set, otherwise from the theme).
 * @param windowPostRenderer Window post-renderer to assign to this window
 */
     fun setWindowPostRenderer(windowPostRenderer:WindowPostRenderer?) {
this.postRenderer = windowPostRenderer
}

@Override
 fun setFixedSize(size:TerminalSize?) {
hints!!.add(Hint.FIXED_SIZE)
size = size
}

private fun setSize(size:TerminalSize?, invalidate:Boolean) {
val oldSize = this.lastKnownSize
this.lastKnownSize = size
if (invalidate)
{
invalidate()
}

 // Fire listeners
        for (listener in getBasePaneListeners())
{
if (listener is WindowListener)
{
(listener as WindowListener).onResized(this, oldSize, size)
}
}
}

@Override
 fun setContentOffset(offset:TerminalPosition?) {
this.contentOffset = offset
}

@Override
@JvmStatic  fun close() {
if (textGUI != null)
{
textGUI!!.removeWindow(this)
}
}

@Override
@JvmStatic  fun waitUntilClosed() {
val textGUI = getTextGUI()
if (textGUI != null)
{
textGUI!!.waitForWindowToClose(this)
}
}

internal fun self():Window? {
return this
}
}/**
 * Default constructor, this creates a window with no title
 */
