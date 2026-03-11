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
 * Default window manager implementation.
 */
open class DefaultWindowManager(
    private val windowDecorationRendererOverride: WindowDecorationRenderer?,
    initialScreenSize: TerminalSize?,
) : WindowManager {

    private var lastKnownScreenSize: TerminalSize = initialScreenSize ?: TerminalSize(80, 24)

    constructor() : this(null, null)

    constructor(initialScreenSize: TerminalSize?) : this(null, initialScreenSize)

    override val isInvalid: Boolean
        get() = false

    override fun getWindowDecorationRenderer(window: Window?): WindowDecorationRenderer? {
        val w = window ?: return DefaultWindowDecorationRenderer()
        return when {
            w.hints?.contains(Window.Hint.NO_DECORATIONS) == true -> EmptyWindowDecorationRenderer()
            windowDecorationRendererOverride != null -> windowDecorationRendererOverride
            w.theme?.windowDecorationRenderer != null -> w.theme?.windowDecorationRenderer
            else -> DefaultWindowDecorationRenderer()
        }
    }

    override fun onAdded(textGUI: WindowBasedTextGUI?, window: Window?, allWindows: List<Window?>?) {
        val w = window ?: return
        val windows = allWindows ?: emptyList()
        val decorationRenderer = getWindowDecorationRenderer(w) ?: DefaultWindowDecorationRenderer()
        val expectedDecoratedSize = decorationRenderer.getDecoratedSize(w, w.preferredSize) ?: TerminalSize.ZERO
        w.decoratedSize = expectedDecoratedSize

        if (w.hints?.contains(Window.Hint.FIXED_POSITION) == true) {
            // Assume already placed.
        } else if (windows.isEmpty()) {
            w.position = TerminalPosition.OFFSET_1x1
        } else if (w.hints?.contains(Window.Hint.CENTERED) == true) {
            val left = (lastKnownScreenSize.columns - expectedDecoratedSize.columns) / 2
            val top = (lastKnownScreenSize.rows - expectedDecoratedSize.rows) / 2
            w.position = TerminalPosition(left, top)
        } else {
            val prev = windows[windows.size - 1]
            var nextPosition = (prev?.position ?: TerminalPosition.OFFSET_1x1).withRelative(2, 1)
                ?: TerminalPosition.OFFSET_1x1
            if (nextPosition.column + expectedDecoratedSize.columns > lastKnownScreenSize.columns ||
                nextPosition.row + expectedDecoratedSize.rows > lastKnownScreenSize.rows
            ) {
                nextPosition = TerminalPosition.OFFSET_1x1
            }
            w.position = nextPosition
        }

        prepareWindow(lastKnownScreenSize, w)
    }

    override fun onRemoved(textGUI: WindowBasedTextGUI?, window: Window?, allWindows: List<Window?>?) {
        // NOP
    }

    override fun prepareWindows(textGUI: WindowBasedTextGUI?, allWindows: List<Window?>?, screenSize: TerminalSize?) {
        lastKnownScreenSize = screenSize ?: lastKnownScreenSize
        for (window in allWindows.orEmpty()) {
            if (window != null) {
                prepareWindow(lastKnownScreenSize, window)
            }
        }
    }

    protected fun prepareWindow(screenSize: TerminalSize, window: Window) {
        val contentAreaSize = if (window.hints?.contains(Window.Hint.FIXED_SIZE) == true) {
            window.size
        } else {
            window.preferredSize
        }

        var size = getWindowDecorationRenderer(window)?.getDecoratedSize(window, contentAreaSize) ?: TerminalSize.ZERO
        var position = window.position ?: TerminalPosition.TOP_LEFT_CORNER

        if (window.hints?.contains(Window.Hint.FULL_SCREEN) == true) {
            position = TerminalPosition.TOP_LEFT_CORNER
            size = screenSize
        } else if (window.hints?.contains(Window.Hint.EXPANDED) == true) {
            position = TerminalPosition.OFFSET_1x1
            size = screenSize.withRelative(
                -kotlin.math.min(4, screenSize.columns),
                -kotlin.math.min(3, screenSize.rows),
            ) ?: screenSize
            if (size != window.decoratedSize) {
                window.invalidate()
            }
        } else if (window.hints?.contains(Window.Hint.FIT_TERMINAL_WINDOW) == true ||
            window.hints?.contains(Window.Hint.CENTERED) == true
        ) {
            while (position.row > 0 && position.row + size.rows > screenSize.rows) {
                position = position.withRelativeRow(-1) ?: position
            }
            while (position.column > 0 && position.column + size.columns > screenSize.columns) {
                position = position.withRelativeColumn(-1) ?: position
            }
            if (position.row + size.rows > screenSize.rows) {
                size = size.withRows(screenSize.rows - position.row) ?: size
            }
            if (position.column + size.columns > screenSize.columns) {
                size = size.withColumns(screenSize.columns - position.column) ?: size
            }
            if (window.hints?.contains(Window.Hint.CENTERED) == true) {
                val left = (lastKnownScreenSize.columns - size.columns) / 2
                val top = (lastKnownScreenSize.rows - size.rows) / 2
                position = TerminalPosition(left, top)
            }
        }

        window.position = position
        window.decoratedSize = size
    }
}
