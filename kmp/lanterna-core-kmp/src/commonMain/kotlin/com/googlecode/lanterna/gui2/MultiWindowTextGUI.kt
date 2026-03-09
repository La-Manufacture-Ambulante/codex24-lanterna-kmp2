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
import com.googlecode.lanterna.TerminalRectangle
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.VirtualScreen
import java.io.EOFException
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Default window-based text GUI implementation.
 */
class MultiWindowTextGUI : AbstractTextGUI, WindowBasedTextGUI {
    override val windowManager: WindowManager
    override val backgroundPane: BasePane
    private val windowList: WindowList = WindowList()
    private val windowRenderBufferCache: IdentityHashMap<Window, TextImage> = IdentityHashMap()
    override val windowPostRenderer: WindowPostRenderer?

    var isEOFWhenNoWindows: Boolean = false

    private var titleBarDragWindow: Window? = null
    private var originWindowPosition: TerminalPosition? = null
    private var dragStart: TerminalPosition? = null

    override val isPendingUpdate: Boolean
        @Synchronized get() {
            for (window in windows.orEmpty()) {
                if (window != null && window.isVisible && window.isInvalid) {
                    return true
                }
            }
            return super.isPendingUpdate || backgroundPane.isInvalid || windowManager.isInvalid
        }

    override val cursorPosition: TerminalPosition?
        @Synchronized get() {
            val activeWindow = activeWindow
            return if (activeWindow != null) {
                activeWindow.toGlobal(activeWindow.cursorPosition)
            } else {
                backgroundPane.cursorPosition
            }
        }

    override val focusedInteractable: Interactable?
        @Synchronized get() {
            val activeWindow = activeWindow
            return if (activeWindow != null) {
                activeWindow.focusedInteractable
            } else {
                backgroundPane.focusedInteractable
            }
        }

    override val windows: Collection<Window?>
        @Synchronized get() = windowList.windowsInZOrder ?: emptyList()

    override val activeWindow: Window?
        @Synchronized get() = windowList.activeWindow

    constructor(screen: Screen?) : this(SameTextGUIThread.Factory(), screen)

    constructor(guiThreadFactory: TextGUIThreadFactory?, screen: Screen?) : this(
        guiThreadFactory,
        screen,
        DefaultWindowManager(),
        null,
        GUIBackdrop(),
    )

    constructor(guiThreadFactory: TextGUIThreadFactory?, screen: Screen?, windowManager: WindowManager?) : this(
        guiThreadFactory,
        screen,
        windowManager,
        null,
        GUIBackdrop(),
    )

    @Deprecated(
        "It's preferred to use a custom background component if you want to customize the background color, " +
            "or you should change the theme. Using this constructor won't work well with theming.",
    )
    constructor(screen: Screen?, backgroundColor: TextColor?) : this(
        screen,
        DefaultWindowManager(),
        EmptySpace(backgroundColor),
    )

    constructor(screen: Screen?, windowManager: WindowManager?, background: Component?) : this(
        screen,
        windowManager,
        null,
        background,
    )

    constructor(
        screen: Screen?,
        windowManager: WindowManager?,
        postRenderer: WindowPostRenderer?,
        background: Component?,
    ) : this(SameTextGUIThread.Factory(), screen, windowManager, postRenderer, background)

    constructor(
        guiThreadFactory: TextGUIThreadFactory?,
        screen: Screen?,
        windowManager: WindowManager? = DefaultWindowManager(),
        postRenderer: WindowPostRenderer? = null,
        background: Component? = GUIBackdrop(),
    ) : super(guiThreadFactory ?: SameTextGUIThread.Factory(), screen) {
        requireNotNull(windowManager) { "Creating a window-based TextGUI requires a WindowManager" }
        this.windowManager = windowManager
        this.windowPostRenderer = postRenderer
        backgroundPane = object : AbstractBasePane<BasePane?>() {
            override val textGUI: TextGUI
                get() = this@MultiWindowTextGUI

            override fun toGlobal(localPosition: TerminalPosition?): TerminalPosition? = localPosition

            override fun fromGlobal(position: TerminalPosition?): TerminalPosition? = position

            override fun self(): BasePane = this
        }
        backgroundPane.component = background ?: GUIBackdrop()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun updateScreen() {
        val screen = screen
        if (screen is VirtualScreen) {
            var minimumTerminalSize = TerminalSize.ZERO
            for (window in windows.orEmpty()) {
                if (window == null || !window.isVisible) {
                    continue
                }
                val hints = window.hints.orEmpty()
                if (hints.contains(Window.Hint.FULL_SCREEN) ||
                    hints.contains(Window.Hint.FIT_TERMINAL_WINDOW) ||
                    hints.contains(Window.Hint.EXPANDED)
                ) {
                    continue
                }
                val lastPosition = window.position ?: continue
                val decoratedSize = window.decoratedSize ?: continue
                minimumTerminalSize = minimumTerminalSize.max(
                    decoratedSize.withRelative(
                        maxOf(lastPosition.column, 0),
                        maxOf(lastPosition.row, 0),
                    )!!,
                )!!
            }
            screen.setMinimumSize(minimumTerminalSize)
        }
        super.updateScreen()
    }

    @Throws(IOException::class)
    @Synchronized
    override fun readKeyStroke(): KeyStroke? {
        val keyStroke = super.pollInput()
        return when {
            windowList.isHadWindowAtSomePoint && isEOFWhenNoWindows && keyStroke == null && windows.isEmpty() ->
                KeyStroke(KeyType.EOF)
            keyStroke != null -> keyStroke
            else -> super.readKeyStroke()
        }
    }

    @Synchronized
    override fun drawGUI(graphics: TextGUIGraphics?) {
        val targetGraphics = graphics ?: return
        drawBackgroundPane(targetGraphics)
        windowManager.prepareWindows(this, windowList.windowsInStableOrder, targetGraphics.size)
        for (window in windows.orEmpty()) {
            if (window == null || !window.isVisible) {
                continue
            }

            val decoratedSize = window.decoratedSize ?: continue
            var textImage = windowRenderBufferCache[window]
            if (textImage == null || textImage.size != decoratedSize) {
                textImage = BasicTextImage(decoratedSize)
                windowRenderBufferCache[window] = textImage
            }

            val windowGraphics = DefaultTextGUIGraphics(this, textImage.newTextGraphics())
            var insideWindowDecorationsGraphics: TextGUIGraphics = windowGraphics
            var contentOffset = TerminalPosition.TOP_LEFT_CORNER

            if (!window.hints.orEmpty().contains(Window.Hint.NO_DECORATIONS)) {
                val decorationRenderer = windowManager.getWindowDecorationRenderer(window)
                if (decorationRenderer != null) {
                    insideWindowDecorationsGraphics = decorationRenderer.draw(this, windowGraphics, window)
                        ?: windowGraphics
                    contentOffset = decorationRenderer.getOffset(window) ?: TerminalPosition.TOP_LEFT_CORNER
                }
            }

            window.draw(insideWindowDecorationsGraphics)
            window.setContentOffset(contentOffset)
            if (windowGraphics !== insideWindowDecorationsGraphics) {
                Borders.joinLinesWithFrame(windowGraphics)
            }

            targetGraphics.drawImage(window.position, textImage)

            if (!window.hints.orEmpty().contains(Window.Hint.NO_POST_RENDERING)) {
                when {
                    window.postRenderer != null -> window.postRenderer?.postRender(targetGraphics, this, window)
                    windowPostRenderer != null -> windowPostRenderer?.postRender(targetGraphics, this, window)
                    theme?.windowPostRenderer != null -> theme?.windowPostRenderer?.postRender(targetGraphics, this, window)
                }
            }
        }

        windowRenderBufferCache.keys.retainAll(windows.filterNotNull().toSet())
    }

    @Deprecated("This method doesn't do anything anymore (as of 3.1.0)")
    override fun setVirtualScreenEnabled(virtualScreenEnabled: Boolean) {
        // no-op
    }

    @Synchronized
    override fun handleInput(keyStroke: KeyStroke?): Boolean {
        ifMouseDownPossiblyChangeActiveWindow(keyStroke)
        ifMouseDownPossiblyStartTitleDrag(keyStroke)
        ifMouseDragPossiblyMoveWindow(keyStroke)

        val activeWindow = activeWindow
        return if (activeWindow != null) {
            activeWindow.handleInput(keyStroke)
        } else {
            backgroundPane.handleInput(keyStroke)
        }
    }

    @Synchronized
    protected fun ifMouseDownPossiblyChangeActiveWindow(keyStroke: KeyStroke?) {
        val mouse = keyStroke as? MouseAction ?: return
        if (!mouse.isMouseDown) {
            return
        }

        val priorActiveWindow = activeWindow
        val anyHit = AtomicBoolean(false)
        val snapshot = ArrayList(windows.filterNotNull())
        for (window in snapshot) {
            val mousePosition = mouse.position ?: continue
            window.bounds.whenContains(mousePosition, Runnable {
                val modalActiveWindow = priorActiveWindow?.hints.orEmpty().contains(Window.Hint.MODAL)
                if (!modalActiveWindow || window === priorActiveWindow) {
                    setActiveWindow(window)
                    anyHit.set(true)
                }
            })
        }

        if (priorActiveWindow != null &&
            (priorActiveWindow !== activeWindow || !anyHit.get()) &&
            priorActiveWindow.hints.orEmpty().contains(Window.Hint.MENU_POPUP)
        ) {
            priorActiveWindow.close()
        }
    }

    protected fun ifMouseDownPossiblyStartTitleDrag(keyStroke: KeyStroke?) {
        val mouse = keyStroke as? MouseAction ?: return
        if (!mouse.isMouseDown) {
            return
        }

        titleBarDragWindow = null
        dragStart = null

        val window = activeWindow ?: return
        if (window.hints.orEmpty().contains(Window.Hint.MENU_POPUP)) {
            return
        }

        val decorator = windowManager.getWindowDecorationRenderer(window) ?: return
        val titleBarRectangle: TerminalRectangle = decorator.getTitleBarRectangle(window) ?: return
        val local = window.fromGlobalToDecoratedRelative(mouse.position)
        titleBarRectangle.whenContains(local ?: return, Runnable {
            titleBarDragWindow = window
            originWindowPosition = window.position
            dragStart = mouse.position
            moveToTop(window)
        })
    }

    protected fun ifMouseDragPossiblyMoveWindow(keyStroke: KeyStroke?) {
        val draggedWindow = titleBarDragWindow ?: return
        val mouse = keyStroke as? MouseAction ?: return
        if (!mouse.isMouseDrag) {
            return
        }

        val mousePosition = mouse.position ?: return
        val originWindowPosition = originWindowPosition ?: return
        val dragStart = dragStart ?: return
        val dx = mousePosition.column - dragStart.column
        val dy = mousePosition.row - dragStart.row
        changeWindowHintsForDragged(draggedWindow)
        draggedWindow.position = TerminalPosition(originWindowPosition.column + dx, originWindowPosition.row + dy)
    }

    protected fun changeWindowHintsForDragged(window: Window?) {
        val draggedWindow = window ?: return
        val hints = HashSet(draggedWindow.hints.orEmpty())
        hints.remove(Window.Hint.CENTERED)
        hints.add(Window.Hint.FIXED_POSITION)
        draggedWindow.setHints(hints)
    }

    @Synchronized
    override fun addWindow(window: Window?): WindowBasedTextGUI {
        requireNotNull(window) { "Cannot add null window" }
        if (window.component == null) {
            window.component = EmptySpace(TerminalSize.ONE)
        }
        window.textGUI?.removeWindow(window)
        window.textGUI = this
        windowManager.onAdded(this, window, windowList.windowsInStableOrder)
        windowList.addWindow(window)
        invalidate()
        return this
    }

    override fun addWindowAndWait(window: Window?): WindowBasedTextGUI {
        requireNotNull(window) { "Cannot add null window" }
        addWindow(window)
        window.waitUntilClosed()
        return this
    }

    @Synchronized
    override fun removeWindow(window: Window?): WindowBasedTextGUI {
        if (!windowList.removeWindow(window)) {
            return this
        }
        window?.textGUI = null
        windowManager.onRemoved(this, window, windowList.windowsInStableOrder)
        invalidate()
        return this
    }

    override fun waitForWindowToClose(abstractWindow: Window?) {
        val window = abstractWindow ?: return
        while (window.textGUI != null) {
            var sleep = true
            val guiThread = guiThread
            if (Thread.currentThread() === guiThread?.thread) {
                try {
                    sleep = !(guiThread.processEventsAndUpdate())
                } catch (_: EOFException) {
                    break
                } catch (e: IOException) {
                    throw RuntimeException("Unexpected IOException while waiting for window to close", e)
                }
            }
            if (sleep) {
                try {
                    Thread.sleep(1)
                } catch (_: InterruptedException) {
                    // ignored
                }
            }
        }
    }

    @Synchronized
    override fun setActiveWindow(activeWindow: Window?): MultiWindowTextGUI {
        windowList.activeWindow = activeWindow
        return this
    }

    @Synchronized
    override fun moveToTop(window: Window?): WindowBasedTextGUI {
        windowList.moveToTop(window)
        invalidate()
        return this
    }

    @Synchronized
    fun moveToBottom(window: Window?): WindowBasedTextGUI {
        windowList.moveToBottom(window)
        invalidate()
        return this
    }

    @Synchronized
    override fun cycleActiveWindow(reverse: Boolean): WindowBasedTextGUI {
        windowList.cycleActiveWindow(reverse)
        return this
    }

    private fun drawBackgroundPane(graphics: TextGUIGraphics) {
        backgroundPane.draw(DefaultTextGUIGraphics(this, graphics))
    }
}
