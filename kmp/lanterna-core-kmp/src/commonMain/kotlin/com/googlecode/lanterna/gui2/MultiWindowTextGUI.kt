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
 * This is the main Text GUI implementation built into Lanterna, supporting multiple tiled windows and a dynamic
 * background area that can be fully customized. If you want to create a text-based GUI with windows and controls,
 * it's very likely this is what you want to use.
 * 
 * 
 * Note: This class used to always wrap the [Screen] object with a [VirtualScreen] to ensure that the UI
 * always fits. As of 3.1.0, we don't do this anymore so when you create the [MultiWindowTextGUI] you can wrap
 * the screen parameter yourself if you want to keep this behavior.
 * @author Martin
 */
 class MultiWindowTextGUI:AbstractTextGUI, WindowBasedTextGUI {
@get:Override
 val windowManager:WindowManager?
@get:Override
 val backgroundPane:BasePane?
private val windowList:WindowList?
private val windowRenderBufferCache:IdentityHashMap<Window?, TextImage?>?
@get:Override
 val windowPostRenderer:WindowPostRenderer?

/**
 * Returns whether the TextGUI should return EOF when you try to read input while there are no windows in the window
 * manager. When this is true (true by default) will make the GUI automatically exit when the last window has been
 * closed.
 * @return Should the GUI return EOF when there are no windows left
 */
    /**
 * Sets whether the TextGUI should return EOF when you try to read input while there are no windows in the window
 * manager. Setting this to true (off by default) will make the GUI automatically exit when the last window has been
 * closed.
 * @param eofWhenNoWindows Should the GUI return EOF when there are no windows left
 */
     var isEOFWhenNoWindows:Boolean = false

private var titleBarDragWindow:Window? = null
private var originWindowPosition:TerminalPosition? = null
private var dragStart:TerminalPosition? = null

 val isPendingUpdate:Boolean
@Override
@Synchronized get() {
for (window in windows!!)
{
if (window!!.isVisible() && window!!.isInvalid())
{
return true
}
}
return super.isPendingUpdate() || backgroundPane!!.isInvalid() || windowManager!!.isInvalid()
}

 val cursorPosition:TerminalPosition?
@Override
@Synchronized get() {
val activeWindow = activeWindow
if (activeWindow != null)
{
return activeWindow!!.toGlobal(activeWindow!!.getCursorPosition())
}
else
{
return backgroundPane!!.getCursorPosition()
}
}

 val focusedInteractable:Interactable?
@Override
@Synchronized get() {
val activeWindow = activeWindow
if (activeWindow != null)
{
return activeWindow!!.getFocusedInteractable()
}
else
{
return backgroundPane!!.getFocusedInteractable()
}
}

 val windows:Collection<Window?>?
@Override
@Synchronized get() {
return windowList!!.getWindowsInZOrder()
}

 val activeWindow:Window?
@Override
@Synchronized get() {
return windowList!!.getActiveWindow()
}

/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be a solid color, depending on theme (default is blue). The
 * current thread will be used as the GUI thread for all Lanterna library operations.
 * @param screen Screen to use as the backend for drawing operations
 */
     constructor(screen:Screen?) : this(SameTextGUIThread.Factory(), screen) {}

/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI is a solid color as decided by the `backgroundColor` parameter.
 * @param screen Screen to use as the backend for drawing operations
 * @param backgroundColor Color to use for the GUI background
 */
    @Deprecated("It's preferred to use a custom background component if you want to customize the background color,\n"+
"      or you should change the theme. Using this constructor won't work well with theming.")
 constructor(
screen:Screen?, 
backgroundColor:TextColor?) : this(screen, DefaultWindowManager(), EmptySpace(backgroundColor)) {}

/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be the component supplied instead of the usual backdrop. This
 * constructor allows you to set a custom [WindowManager] instead of [DefaultWindowManager].
 * @param screen Screen to use as the backend for drawing operations
 * @param windowManager Window manager implementation to use
 * @param background Component to use as the background of the GUI, behind all the windows
 */
     constructor(
screen:Screen?, 
windowManager:WindowManager?, 
background:Component?) : this(screen, windowManager, null, background) {}

/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be the component supplied instead of the usual backdrop. This
 * constructor allows you to set a custom [WindowManager] instead of [DefaultWindowManager] as well
 * as a custom [WindowPostRenderer] that can be used to tweak the appearance of any window.
 * @param screen Screen to use as the backend for drawing operations
 * @param windowManager Window manager implementation to use
 * @param postRenderer `WindowPostRenderer` object to invoke after each window has been drawn
 * @param background Component to use as the background of the GUI, behind all the windows
 */
     constructor(
screen:Screen?, 
windowManager:WindowManager?, 
postRenderer:WindowPostRenderer?, 
background:Component?) : this(SameTextGUIThread.Factory(), screen, windowManager, postRenderer, background) {}

/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be the component supplied instead of the usual backdrop. This
 * constructor allows you to set a custom [WindowManager] instead of [DefaultWindowManager] as well
 * as a custom [WindowPostRenderer] that can be used to tweak the appearance of any window. This constructor
 * also allows you to control the threading model for the UI.
 * @param guiThreadFactory Factory implementation to use when creating the `TextGUIThread`
 * @param screen Screen to use as the backend for drawing operations
 * @param windowManager Window manager implementation to use
 * @param postRenderer `WindowPostRenderer` object to invoke after each window has been drawn
 * @param background Component to use as the background of the GUI, behind all the windows
 */
    @JvmOverloads  constructor(
guiThreadFactory:TextGUIThreadFactory?, 
screen:Screen?, 
windowManager:WindowManager? = DefaultWindowManager(), 
postRenderer:WindowPostRenderer? = null, 
background:Component? = GUIBackdrop()) : super(guiThreadFactory, screen) {
var background = background
windowList = WindowList()
if (windowManager == null)
{
throw IllegalArgumentException("Creating a window-based TextGUI requires a WindowManager")
}
if (background == null)
{
 //Use a sensible default instead of throwing
            background = GUIBackdrop()
}
this.windowManager = windowManager
this.backgroundPane = object:AbstractBasePane<BasePane?>() {
 val textGUI:TextGUI?
@Override
get() {
return this@MultiWindowTextGUI
}

@Override
 fun toGlobal(localPosition:TerminalPosition?):TerminalPosition? {
return localPosition
}

 fun fromGlobal(globalPosition:TerminalPosition?):TerminalPosition? {
return globalPosition
}

internal fun self():BasePane? {
return this
}
}
this.backgroundPane!!.setComponent(background)
this.windowRenderBufferCache = IdentityHashMap()
this.windowPostRenderer = postRenderer
this.isEOFWhenNoWindows = false
}

@Override
@Synchronized @Throws(IOException::class)
 fun updateScreen() {
if (getScreen() is VirtualScreen)
{
 // If the user has passed in a virtual screen, we should calculate the minimum size required and tell it.
            // Previously the constructor always wrapped the screen in a VirtualScreen, but now we need to check.
            var minimumTerminalSize:TerminalSize? = TerminalSize.ZERO
for (window in windows!!)
{
if (window!!.isVisible())
{
if ((window!!.getHints().contains(Window.Hint.FULL_SCREEN) || 
window!!.getHints().contains(Window.Hint.FIT_TERMINAL_WINDOW) || 
window!!.getHints().contains(Window.Hint.EXPANDED)))
{
 //Don't take full screen windows or auto-sized windows into account
                        continue
}
val lastPosition = window!!.getPosition()
minimumTerminalSize = minimumTerminalSize!!.max(
 //Add position to size to get the bottom-right corner of the window
                            window!!.getDecoratedSize().withRelative(
Math.max(lastPosition!!.column, 0), 
Math.max(lastPosition!!.row, 0)))
}
}
(getScreen() as VirtualScreen).setMinimumSize(minimumTerminalSize)
}
super.updateScreen()
}

@Override
@Synchronized @Throws(IOException::class)
protected fun readKeyStroke():KeyStroke? {
val keyStroke = super.pollInput()
if (windowList!!.isHadWindowAtSomePoint() && isEOFWhenNoWindows && keyStroke == null && windows!!.isEmpty())
{
return KeyStroke(KeyType.EOF)
}
else if (keyStroke != null)
{
return keyStroke
}
else
{
return super.readKeyStroke()
}
}

@Override
@Synchronized protected fun drawGUI(graphics:TextGUIGraphics?) {
drawBackgroundPane(graphics)
windowManager!!.prepareWindows(this, windowList!!.getWindowsInStableOrder(), graphics!!.getSize())
for (window in windows!!)
{
if (window!!.isVisible())
{
 // First draw windows to a buffer, then copy it to the real destination. This is to make physical off-screen
                // drawing work better. Store the buffers in a cache so we don't have to re-create them every time.
                var textImage = windowRenderBufferCache!!.get(window)
if (textImage == null || !textImage!!.getSize().equals(window!!.getDecoratedSize()))
{
textImage = BasicTextImage(window!!.getDecoratedSize())
windowRenderBufferCache!!.put(window, textImage)
}
val windowGraphics = DefaultTextGUIGraphics(this, textImage!!.newTextGraphics())
var insideWindowDecorationsGraphics:TextGUIGraphics? = windowGraphics
var contentOffset:TerminalPosition? = TerminalPosition.TOP_LEFT_CORNER
if (!window!!.getHints().contains(Window.Hint.NO_DECORATIONS))
{
val decorationRenderer = windowManager!!.getWindowDecorationRenderer(window)
insideWindowDecorationsGraphics = decorationRenderer!!.draw(this, windowGraphics, window)
contentOffset = decorationRenderer!!.getOffset(window)
}

window!!.draw(insideWindowDecorationsGraphics)
window!!.setContentOffset(contentOffset)
if (windowGraphics !== insideWindowDecorationsGraphics)
{
Borders.joinLinesWithFrame(windowGraphics)
}

graphics!!.drawImage(window!!.getPosition(), textImage)

if (!window!!.getHints().contains(Window.Hint.NO_POST_RENDERING))
{
if (window!!.getPostRenderer() != null)
{
window!!.getPostRenderer().postRender(graphics, this, window)
}
else if (windowPostRenderer != null)
{
windowPostRenderer!!.postRender(graphics, this, window)
}
else if (getTheme().getWindowPostRenderer() != null)
{
getTheme().getWindowPostRenderer().postRender(graphics, this, window)
}
}
}
}

 // Purge the render buffer cache from windows that have been removed
        windowRenderBufferCache!!.keySet().retainAll(windows)
}

private fun drawBackgroundPane(graphics:TextGUIGraphics?) {
backgroundPane!!.draw(DefaultTextGUIGraphics(this, graphics))
}

/**
 * This method used to exist to control if the virtual screen should by bypassed or not. Since 3.1.0 calling this
 * has no effect since we don't force a VirtualScreen anymore and you control it yourself when you create the GUI.
 * @param virtualScreenEnabled Not used anymore
 */
    @Override
@Deprecated("This method don't do anything anymore (as of 3.1.0)")
 fun setVirtualScreenEnabled(virtualScreenEnabled:Boolean) {}

@Override
@Synchronized  fun handleInput(keyStroke:KeyStroke?):Boolean {
ifMouseDownPossiblyChangeActiveWindow(keyStroke)
ifMouseDownPossiblyStartTitleDrag(keyStroke)
ifMouseDragPossiblyMoveWindow(keyStroke)
val activeWindow = activeWindow
if (activeWindow != null)
{
return activeWindow!!.handleInput(keyStroke)
}
else
{
return backgroundPane!!.handleInput(keyStroke)
}
}

@Synchronized protected fun ifMouseDownPossiblyChangeActiveWindow(keyStroke:KeyStroke?) {
if (!(keyStroke is MouseAction))
{
return 
}
val mouse = keyStroke as MouseAction?
if (mouse!!.isMouseDown())
{
 // for now, active windows do not overlap?
            // by happenstance, the last in the list in case of many overlapping will be active
            val priorActiveWindow = activeWindow
val anyHit = AtomicBoolean(false)
val snapshot = ArrayList(windows)
for (w in snapshot)
{
w!!.getBounds().whenContains(mouse!!.getPosition(), { 
 // Only change active window if the active MODAL dialog or if 
                	// window is not MODAL 
                	if ((!priorActiveWindow!!.getHints().contains(Hint.MODAL) || (priorActiveWindow!!.getHints().contains(Hint.MODAL) && w === priorActiveWindow)))
{
setActiveWindow(w)
anyHit.set(true)
} })
}
 // clear popup menus if they clicked onto another window or missed all windows
            if (priorActiveWindow != null && (priorActiveWindow !== activeWindow || !anyHit.get()))
{
if (priorActiveWindow!!.getHints().contains(Hint.MENU_POPUP))
{
priorActiveWindow!!.close()
}
}
}
}

protected fun ifMouseDownPossiblyStartTitleDrag(keyStroke:KeyStroke?) {
if (!(keyStroke is MouseAction))
{
return 
}
val mouse = keyStroke as MouseAction?
if (mouse!!.isMouseDown())
{
titleBarDragWindow = null
dragStart = null
val window = activeWindow
if (window == null)
{
return 
}

if (window!!.getHints().contains(Hint.MENU_POPUP))
{
 // popup windows are not draggable
                return 
}

val decorator = windowManager!!.getWindowDecorationRenderer(window)
val titleBarRectangle = decorator!!.getTitleBarRectangle(window)
val local = window!!.fromGlobalToDecoratedRelative(mouse!!.getPosition())
titleBarRectangle!!.whenContains(local!!, { titleBarDragWindow = window
originWindowPosition = titleBarDragWindow!!.getPosition()
dragStart = mouse!!.getPosition()
moveToTop(window) })
}

}
protected fun ifMouseDragPossiblyMoveWindow(keyStroke:KeyStroke?) {
if (titleBarDragWindow == null)
{
return 
}
if (!(keyStroke is MouseAction))
{
return 
}
val mouse = keyStroke as MouseAction?
if (mouse!!.isMouseDrag())
{
val mp = mouse!!.getPosition()
val wp = originWindowPosition
val dx = mp!!.column - dragStart!!.column
val dy = mp!!.row - dragStart!!.row
changeWindowHintsForDragged(titleBarDragWindow)
titleBarDragWindow!!.setPosition(TerminalPosition(wp!!.column + dx, wp!!.row + dy))
 // TODO ? any additional children popups (shown menus, etc) should also be moved (or just closed)
        }

}
/**
 * In order for window to be draggable, it would no longer be CENTERED.
 * Removes Hint.CENTERED, adds Hint.FIXED_POSITION to the window hints.
 */
    protected fun changeWindowHintsForDragged(window:Window?) {
val hints = HashSet(titleBarDragWindow!!.getHints())
hints.remove(Hint.CENTERED)
hints.add(Hint.FIXED_POSITION)
titleBarDragWindow!!.setHints(hints)
}

@Override
@Synchronized  fun addWindow(window:Window):WindowBasedTextGUI? {
 //To protect against NPE if the user forgot to set a content component
        if (window.getComponent() == null)
{
window.setComponent(EmptySpace(TerminalSize.ONE))
}

if (window.getTextGUI() != null)
{
window.getTextGUI().removeWindow(window)
}
window.setTextGUI(this)
windowManager!!.onAdded(this, window, windowList!!.getWindowsInStableOrder())

windowList!!.addWindow(window)

invalidate()
return this
}

@Override
 fun addWindowAndWait(window:Window?):WindowBasedTextGUI? {
addWindow(window!!)
window!!.waitUntilClosed()
return this
}

@Override
@Synchronized  fun removeWindow(window:Window?):WindowBasedTextGUI? {
val contained = windowList!!.removeWindow(window)
if (!contained)
{
 //Didn't contain this window
            return this
}
window!!.setTextGUI(null)
windowManager!!.onRemoved(this, window, windowList!!.getWindowsInStableOrder())
invalidate()
return this
}

@Override
 fun waitForWindowToClose(window:Window) {
while (window.getTextGUI() != null)
{
var sleep = true
val guiThread = getGUIThread()
if (Thread.currentThread() === guiThread!!.getThread())
{
try
{
sleep = !guiThread!!.processEventsAndUpdate()
}
catch (ignore:EOFException) {
 //The GUI has closed so allow exit
                    break
}
catch (e:IOException) {
throw RuntimeException("Unexpected IOException while waiting for window to close", e)
}

}
if (sleep)
{
try
{
Thread.sleep(1)
}
catch (ignore:InterruptedException) {}

}
}
}

@Override
@Synchronized  fun setActiveWindow(activeWindow:Window?):MultiWindowTextGUI {
windowList!!.setActiveWindow(activeWindow)
return this
}

@Override
@Synchronized  fun moveToTop(window:Window?):WindowBasedTextGUI? {
windowList!!.moveToTop(window)
invalidate()
return this
}

@Synchronized  fun moveToBottom(window:Window?):WindowBasedTextGUI? {
windowList!!.moveToBottom(window)
invalidate()
return this
}

/**
 * Switches the active window by cyclically shuffling the window list. If `reverse` parameter is `false`
 * then the current top window is placed at the bottom of the stack and the window immediately behind it is the new
 * top. If `reverse` is set to `true` then the window at the bottom of the stack is moved up to the
 * front and the previous top window will be immediately below it
 * @param reverse Direction to cycle through the windows
 * @return Itself
 */
    @Synchronized  fun cycleActiveWindow(reverse:Boolean):WindowBasedTextGUI? {
windowList!!.cycleActiveWindow(reverse)
return this
}
}/**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be a solid color, depending on theme (default is blue). This
 * constructor allows you control the threading model for the UI.
 * @param guiThreadFactory Factory implementation to use when creating the `TextGUIThread`
 * @param screen Screen to use as the backend for drawing operations
 *//**
 * Creates a new `MultiWindowTextGUI` that uses the specified `Screen` as the backend for all drawing
 * operations. The background area of the GUI will be a solid color, depending on theme (default is blue). This
 * constructor allows you control the threading model for the UI and set a custom [WindowManager].
 * @param guiThreadFactory Factory implementation to use when creating the `TextGUIThread`
 * @param screen Screen to use as the backend for drawing operations
 * @param windowManager Custom window manager to use
 */
