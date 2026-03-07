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

import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.*
import com.googlecode.lanterna.screen.VirtualScreen
import com.googlecode.lanterna.gui2.Window.Hint

import java.io.EOFException
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 
 * @author ginkoblongata
 */
 class WindowList {

private val windows = LinkedList()
private val stableOrderingOfWindows = ArrayList()

 var activeWindow:Window? = null
set(activeWindow) {
field = activeWindow
if (activeWindow != null)
{
moveToTop(activeWindow)
}
}
 var isHadWindowAtSomePoint = false
private set

 val windowsInZOrder:List<Window?>?
get() {
return Collections.unmodifiableList(windows)
}

 val windowsInStableOrder:List<Window?>?
get() {
return Collections.unmodifiableList(stableOrderingOfWindows)
}

 fun addWindow(window:Window?) {
if (!stableOrderingOfWindows.contains(window))
{
stableOrderingOfWindows.add(window)
}
if (!windows.contains(window))
{
windows.add(window)
}
if (!window!!.getHints().contains(Window.Hint.NO_FOCUS))
{
activeWindow = window
}
isHadWindowAtSomePoint = true
}

/**
 * Removes the window from this WindowList.
 * @return true if this WindowList contained the specified Window
 */
     fun removeWindow(window:Window?):Boolean {
val contained = windows.remove(window)
stableOrderingOfWindows.remove(window)

if (this.activeWindow === window)
{
 // in case no suitable window is found, so pass control back to the background pane
            activeWindow = null

 //Go backward in reverse and find the first suitable window
            for (index in windows.size() - 1 downTo 0)
{
val candidate = windows.get(index)
if (!candidate!!.getHints().contains(Window.Hint.NO_FOCUS))
{
activeWindow = candidate
break
}
}
}

return contained
}

 fun moveToTop(window:Window?) {
if (!windows.contains(window))
{
throw IllegalArgumentException("Window " + window + " isn't in MultiWindowTextGUI " + this)
}
windows.remove(window)
windows.add(window)
}

 fun moveToBottom(window:Window?) {
if (!windows.contains(window))
{
throw IllegalArgumentException("Window " + window + " isn't in MultiWindowTextGUI " + this)
}
windows.remove(window)
windows.add(0, window)
}
/**
 * Switches the active window by cyclically shuffling the window list. If `reverse` parameter is `false`
 * then the current top window is placed at the bottom of the stack and the window immediately behind it is the new
 * top. If `reverse` is set to `true` then the window at the bottom of the stack is moved up to the
 * front and the previous top window will be immediately below it
 * @param reverse Direction to cycle through the windows
 */
     fun cycleActiveWindow(reverse:Boolean):WindowList {
if (windows.isEmpty() || windows.size() === 1 || (this.activeWindow != null && this.activeWindow!!.getHints().contains(Window.Hint.MODAL)))
{
return this
}
val originalActiveWindow = this.activeWindow
val nextWindow:Window?
if (this.activeWindow == null)
{
 // Cycling out of active background pane
            nextWindow = if (reverse) windows.get(windows.size() - 1) else windows.get(0)
}
else
{
 // Switch to the next window
            nextWindow = getNextWindow(reverse, this.activeWindow)
}

var noFocusWindows = 0
while (nextWindow!!.getHints().contains(Window.Hint.NO_FOCUS))
{
++noFocusWindows
if (noFocusWindows == windows.size())
{
 // All windows are NO_FOCUS, so give up
                return this
}
nextWindow = getNextWindow(reverse, nextWindow)
if (nextWindow === originalActiveWindow)
{
return this
}
}

if (reverse)
{
moveToTop(nextWindow)
}
else if (originalActiveWindow != null)
{
moveToBottom(originalActiveWindow)
}
activeWindow = nextWindow
return this
}

private fun getNextWindow(reverse:Boolean, window:Window?):Window? {
var index = windows.indexOf(window)
if (reverse)
{
if (++index >= windows.size())
{
index = 0
}
}
else
{
if (--index < 0)
{
index = windows.size() - 1
}
}
return windows.get(index)
}
}
