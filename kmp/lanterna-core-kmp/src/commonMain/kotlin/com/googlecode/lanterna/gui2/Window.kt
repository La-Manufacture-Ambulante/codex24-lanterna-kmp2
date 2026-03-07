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
import com.googlecode.lanterna.TerminalRectangle
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke

/**
 * Window is a base unit in the TextGUI system, it represents a collection of components grouped together, usually
 * surrounded by a border and a title. Modern computer system GUIs are normally based around the metaphor of windows,
 * so I don't think you should have any problems understanding what this means.
 * @author Martin
 */
 interface Window:BasePane {

/**
 * DON'T CALL THIS METHOD YOURSELF, it is called automatically by the TextGUI system when you add a window. If you
 * call it with the intention of adding the window to the specified TextGUI, you need to read the documentation
 * on how to use windows.
 * @param textGUI TextGUI this window belongs to from now on
 */
    @get:Override
 var textGUI:WindowBasedTextGUI?

/**
 * This method returns the title of the window, which is normally drawn at the top-left corder of the window
 * decoration, but depending on the `WindowDecorationRenderer` used by the `TextGUI`
 * @return title of the window
 */
     val title:String?

/**
 * This values is optionally used by the window manager to decide if the windows should be drawn or not. In an
 * invisible state, the window is still considered active in the TextGUI but just not drawn and not receiving any
 * input events. Please note that window managers may choose not to implement this.
 * 
 * @return Whether the window wants to be visible or not
 */
    /**
 * This values is optionally used by the window manager to decide if the windows should be drawn or not. In an
 * invisible state, the window is still considered active in the TextGUI but just not drawn and not receiving any
 * input events. Please note that window managers may choose not to implement this.
 * 
 * @param visible whether the window should be visible or not
 */
     var isVisible:Boolean

/**
 * This method is used to determine if the window requires re-drawing. The most common cause for this is the some
 * of its components has changed and we need a re-draw to make these changes visible.
 * @return `true` if the window would like to be re-drawn, `false` if the window doesn't need
 */
    @get:Override
 val isInvalid:Boolean


/**
 * Return the last known size of the window including window decoration and the window position as a TerminalRectangle.
 * @return the decorated size and position of the window
 */
     val bounds:TerminalRectangle
get() {
val position = position
val size = decoratedSize
return TerminalRectangle(position!!.column, position!!.row, size!!.columns, size!!.rows)
}

/**
 * Returns the size this window would like to be
 * @return Desired size of this window
 */
     val preferredSize:TerminalSize?

/**
 * Returns a set of window hints that can be used by the text gui system, the window manager or any other part that
 * is interacting with windows.
 * @return Set of hints defined for this window
 */
     val hints:Set<Hint?>?

/**
 * Returns the position of the window, as last specified by the window manager. This position does not include
 * window decorations but is the top-left position of the first usable space of the window.
 * @return Position, relative to the top-left corner of the terminal, of the top-left corner of the window
 */
    /**
 * This method is called by the GUI system to update the window on where the window manager placed it. Calling this
 * yourself will have no effect other than making the `getPosition()` call incorrect until the next redraw,
 * unless you have specified through window hints that you don't want the window manager to automatically place
 * the window. Notice that the position here is expressed in "global" coordinates, which means measured from the
 * top-left corner of the terminal itself.
 * @param topLeft Global coordinates of the top-left corner of the window
 */
     var position:TerminalPosition?

/**
 * Returns the last known size of the window. This is in general derived from the last drawing operation, how large
 * area the window was allowed to draw on. This size does not include window decorations.
 * @return Size of the window
 */
    /**
 * This method is called by the GUI system to update the window on how large it is, excluding window decorations.
 * Calling this yourself will generally make no difference in the size of the window, since it will be reset on the
 * next redraw based on how large area the TextGraphics given is covering. However, if you add the FIXED_SIZE
 * window hint, the auto-size calculation will be turned off and you can use this method to set how large you want
 * the window to be.
 * 
 * 
 * **Important:** if you are writing your own [WindowManager], you should call `setDecoratedSize`
 * instead of this when decided the size of the window.
 * @param size New size of your fixed-size window
 */
    @set:Deprecated("This method is deprecated now as it probably doesn't do what you think. Please use\n"+
"      {@code setFixedSize} or {@code setDecoratedSize} instead, depending on what you are trying to do.")
 var size:TerminalSize?

/**
 * Returns the last known size of the window including window decorations put on by the window manager. The value
 * returned here is passed in during drawing by the TextGUI through `setDecoratedSize(..)`.
 * @return Size of the window, including window decorations
 */
    /**
 * This method is called by the GUI system to update the window on how large it is, counting window decorations too.
 * Calling this yourself will have no effect other than making the `getDecoratedSize()` call incorrect until
 * the next redraw.
 * 
 * 
 * **Important:** if you are writing your own [WindowManager], you should call this method instead of
 * `setSize` when decided the size of the window.
 * @param decoratedSize Size of the window, including window decorations
 */
     var decoratedSize:TerminalSize?

/**
 * Returns a post-renderer the GUI system should invoke after the window has been drawn. This can be used to
 * creating effects like shadows, overlays, etc. If this returns `null`, the GUI system will fall back to
 * it's own global override and after that to the current theme. If these are all `null`, no post-rendering
 * is done.
 * @return [WindowPostRenderer] to invoke after this window is drawn, or `null` fallback to the GUI
 * system's default.
 */
     val postRenderer:WindowPostRenderer?

/**
 * Returns the component which is the top-level in the component hierarchy inside this window.
 * @return Top-level component in the window
 */
    /**
 * Sets the top-level component in the window, this will be the only component unless it's a container of some kind
 * that you add child-components to.
 * @param component Component to use as the top-level object in the Window
 */
    @get:Override
@set:Override
 var component:Component?

/**
 * Returns the component in the window that currently has input focus. There can only be one component at a time
 * being in focus.
 * @return Interactable component that is currently in receiving input focus
 */
    /**
 * Sets the component currently in focus within this window, or sets no component in focus if `null`
 * is passed in.
 * @param interactable Interactable to focus, or `null` to clear focus
 */
    @get:Override
@set:Override
 var focusedInteractable:Interactable?

/**
 * Returns the position of where to put the terminal cursor according to this window. This is typically
 * derived from which component has focus, or `null` if no component has focus or if the window doesn't
 * want the cursor to be visible. Note that the coordinates are in local coordinate space, relative to the top-left
 * corner of the window. You can use your TextGUI implementation to translate these to global coordinates.
 * @return Local position of where to place the cursor, or `null` if the cursor shouldn't be visible
 */
    @get:Override
 val cursorPosition:TerminalPosition?

/**
 * Returns the [MenuBar] assigned to this window, if any, otherwise returns {code null}.
 * @return The active menu bar or `null`
 */
    /**
 * Sets the active [MenuBar] for this window. The menu will be rendered at the top, inside the window
 * decorations, if set. If called with `null`, any previously set menu bar is removed.
 * @param menubar The [MenuBar] to assign to this window
 */
    @get:Override
@set:Override
 var menuBar:MenuBar?
/**
 * Window hints are meta-data stored along with the window that can be used to give the GUI system some ideas of how
 * this window wants to be treated. There are no guarantees that the hints will be honoured though.
 * 
 * You can declare your own window hints by sub-classing this class.  It is highly recommended to provide
 * your custom hints a good `.toString()`. You'd surely prefer in a debug-session to see the Hints
 * of a Window as `[Expanded, Modal]` than as `[foo.Bar@12345, foo.Bar@fedcba]`
 */
     class Hint {

private val info:String?

protected constructor() {}

protected constructor(info:String?) {
this.info = info
}
 fun toString():String? {
if (info != null)
{
return info
}
else
{
return super.toString()
}
}

companion object {
/**
 * With this hint, the TextGUI system should not draw any decorations around the window. Decorated size will be
 * the same as the window size.
 */
         val NO_DECORATIONS = Hint("NoDeco")

/**
 * With this hint, the TextGUI system should skip running any post renderers for the window. By default this
 * means the window won't have any shadow.
 */
         val NO_POST_RENDERING = Hint("NoPostRend")

/**
 * With this hint, the window should never receive focus by the window manager
 */
         val NO_FOCUS = Hint("NoFocus")

/**
 * With this hint, the window wants to be at the center of the terminal instead of using the cascading layout
 * which is the standard.
 */
         val CENTERED = Hint("Centered")

/**
 * Windows with this hint should not be positioned by the window manager, rather they should use whatever
 * position is pre-set.
 */
         val FIXED_POSITION = Hint("FixedPos")

/**
 * Windows with this hint should (optionally) be rendered differently by the window manager to distiguish them
 * from ordinary windows. This is intended to be used only by menu popups (See [MenuBar],
 * [com.googlecode.lanterna.gui2.menu.Menu] and [com.googlecode.lanterna.gui2.menu.MenuItem]).
 */
         val MENU_POPUP = Hint("MenuPopup")

/**
 * Windows with this hint should not be automatically sized by the window manager (using
 * `getPreferredSize()`), rather should rely on the code manually setting the size of the window using
 * `setFixedSize(..)`.
 */
         val FIXED_SIZE = Hint("FixedSize")

/**
 * With this hint, don't let the window grow larger than the terminal screen, rather set components to a smaller
 * size than they prefer.
 */
         val FIT_TERMINAL_WINDOW = Hint("FitTermWin")

/**
 * This hint tells the window manager that this window should have exclusive access to the keyboard input until
 * it is closed. For window managers that allows the user to switch between open windows, putting a window on
 * the screen with this hint should make the window manager temporarily disable that function until the window
 * is closed.
 */
         val MODAL = Hint("Modal")

/**
 * A window with this hint would like to be placed covering the entire screen. Use this in combination with
 * NO_DECORATIONS if you want the content area to take up the entire terminal.
 */
         val FULL_SCREEN = Hint("FullScreen")

/**
 * This window hint tells the window manager that the window should be taking up almost the entire screen,
 * leaving only a small space around it. This is different from `FULL_SCREEN` which takes all available
 * space and completely hide the background and any other window behind it.
 */
         val EXPANDED = Hint("Expanded")
}
}

/**
 * Invalidates the whole window (including all of its child components) which will cause it to be recalculated
 * and redrawn.
 */
    @Override
@JvmStatic  fun invalidate() 

/**
 * Closes the window, which will remove it from the GUI
 */
    @JvmStatic  fun close() 

/**
 * Updates the set of active hints for this window. Please note that it's up to the window manager if these hints
 * will be honored or not.
 * @param hints Set of hints to be active for this window
 */
     fun setHints(hints:Collection<Hint?>?) 

/**
 * Calling this method will add the FIXED_SIZE window hint (if it wasn't present already) and attempt to force the
 * window to always have the size specified. Notice that it's up to the [WindowManager] if this size and hint
 * are going to be honored.
 * @param size New size of your fixed-size window
 */
     fun setFixedSize(size:TerminalSize?) 

/**
 * This method is called by the GUI system to update the window on, as of the last drawing operation, the distance
 * from the top-left position of the window including decorations to the top-left position of the actual content
 * area. If this window has no decorations, it will be always 0x0. Do not call this method yourself.
 * @param offset Offset from the top-left corner of the window (including decorations) to the top-left corner of
 * the content area.
 */
     fun setContentOffset(offset:TerminalPosition?) 

/**
 * Waits for the window to close. Please note that this can cause deadlocks if care is not taken. Also, this method
 * will swallow any interrupts, if you need a wait method that throws InterruptedException, you'll have to implement
 * this yourself.
 */
    @JvmStatic  fun waitUntilClosed() 

/**
 * Adds a [WindowListener] to this [Window]. If it has already been added, the call will do nothing.
 * @param windowListener Listener to attach to this [Window]
 */
     fun addWindowListener(windowListener:WindowListener?) 

/**
 * Removes a [WindowListener] from this [Window]. If the listener isn't in the list of listeners, this
 * call does nothing.
 * @param windowListener Listener to remove from this [Window]
 */
     fun removeWindowListener(windowListener:WindowListener?) 

/**////////////////////////////////////////////////////////////
 * //// Below here are methods from BasePane                  ////
 * / We duplicate them here to make the JavaDoc more clear ////
 */
    /**
 * Called by the GUI system (or something imitating the GUI system) to draw the window. The TextGUIGraphics object
 * should be used to perform the drawing operations.
 * @param graphics TextGraphics object to draw with
 */
    @Override
 fun draw(graphics:TextGUIGraphics?) 

/**
 * Called by the GUI system's window manager when it has decided that this window should receive the keyboard input.
 * The window will decide what to do with this input, usually sending it to one of its sub-components, but if it
 * isn't able to find any handler for this input it should return `false` so that the window manager can take
 * further decisions on what to do with it.
 * @param key Keyboard input
 * @return `true` If the window could handle the input, false otherwise
 */
    @Override
 fun handleInput(key:KeyStroke?):Boolean 

/**
 * @see Window.toGlobalFromContentRelative
 * @see Window.toGlobalFromDecoratedRelative
 */
    @Override
@Deprecated("This is deprecated in favor of calling either of: {@code toGlobalFromContentRelative()} or {@code toGlobalFromDecoratedRelative()}.\n"+
"      ")
 fun toGlobal(localPosition:TerminalPosition?):TerminalPosition? 

/**
 * Returns a position in the window content's local coordinate space to global coordinates
 * @param localPosition The local position to translate
 * @return The local position translated to global coordinates
 */
     fun toGlobalFromContentRelative(localPosition:TerminalPosition?):TerminalPosition? 
/**
 * Returns a position in the decorated window local coordinate space to global coordinates
 * @param decoratedPosition The position inside the window (taking decorations into account too)
 * @return The local position translated to global coordinates
 */
     fun toGlobalFromDecoratedRelative(decoratedPosition:TerminalPosition?):TerminalPosition? 

/**
 * @see Window.fromGlobalToContentRelative
 * @see Window.fromGlobalToDecoratedRelative
 */
    @Override
@Deprecated("This is deprecated in favor of calling either of: {@code fromGlobalToContentRelative()} or {@code fromGlobalToDecoratedRelative()}\n"+
"      ")
 fun fromGlobal(position:TerminalPosition?):TerminalPosition? 

/**
 * Returns a position expressed in global coordinates, i.e. row and column offset from the top-left corner of the
 * terminal into a position relative to the top-left corner of the window's content. Calling
 * `fromGlobalToContentRelative(toGlobalFromContentRelative(..))` should return the exact same position.
 * @param position Position expressed in global coordinates to translate to local coordinates of this Window's content.
 * @return The global coordinates expressed as local coordinates
 */
     fun fromGlobalToContentRelative(position:TerminalPosition?):TerminalPosition? 
/**
 * Returns a position expressed in global coordinates, i.e. row and column offset from the top-left corner of the
 * terminal into a position relative to the top-left corner of the window including it's decoration. Calling
 * `fromGlobalToDecoratedRelative(toGlobalFromDecoratedRelative(..))` should return the exact same position.
 * @param position Position expressed in global coordinates to translate to local coordinates of this window including it's decoration.
 * @return The global coordinates expressed as local coordinates
 */
     fun fromGlobalToDecoratedRelative(position:TerminalPosition?):TerminalPosition? 
}
