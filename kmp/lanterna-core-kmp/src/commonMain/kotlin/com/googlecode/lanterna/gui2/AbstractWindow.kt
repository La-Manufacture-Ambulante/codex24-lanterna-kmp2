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
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlin.collections.HashSet

/**
 * Abstract [Window] implementation with shared behavior.
 */
abstract class AbstractWindow protected constructor(initialTitle: String? = "") : AbstractBasePane<Window?>(), Window {
    override var textGUI: WindowBasedTextGUI? = null
    override var isVisible: Boolean = true
    override var title: String? = initialTitle
        set(value) {
            field = value
            invalidate()
        }

    private var lastKnownSize: TerminalSize? = null
    private var lastKnownDecoratedSize: TerminalSize? = null
    private var lastKnownPosition: TerminalPosition? = null
    private var contentOffset: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER ?: TerminalPosition(0, 0)

    private var hintsBacking: MutableSet<Hint?> = HashSet()
    private var windowPostRenderer: WindowPostRenderer? = null
    private var closeWindowWithEscape: Boolean = false

    fun setCloseWindowWithEscape(closeWindowWithEscape: Boolean) {
        this.closeWindowWithEscape = closeWindowWithEscape
    }

    override fun draw(graphics: TextGUIGraphics?) {
        val activeGraphics = graphics ?: return
        if (activeGraphics.size != lastKnownSize) {
            component?.invalidate()
        }
        setSizeInternal(activeGraphics.size, false)
        super.draw(activeGraphics)
    }

    override fun handleInput(key: KeyStroke?): Boolean {
        val handled = super.handleInput(key)
        if (!handled && closeWindowWithEscape && key?.keyType == KeyType.ESCAPE) {
            close()
            return true
        }
        return handled
    }

    @Deprecated("Use toGlobalFromContentRelative")
    override fun toGlobal(localPosition: TerminalPosition?): TerminalPosition? {
        return toGlobalFromContentRelative(localPosition)
    }

    override fun toGlobalFromContentRelative(contentLocalPosition: TerminalPosition?): TerminalPosition? {
        if (contentLocalPosition == null) {
            return null
        }
        val position = lastKnownPosition ?: return null
        val withOffset = contentOffset.withRelative(contentLocalPosition) ?: contentLocalPosition
        return position.withRelative(withOffset)
    }

    @Deprecated("Use toGlobalFromDecoratedRelative")
    override fun toGlobalFromDecoratedRelative(localPosition: TerminalPosition?): TerminalPosition? {
        if (localPosition == null) {
            return null
        }
        val position = lastKnownPosition ?: return null
        return position.withRelative(localPosition)
    }

    @Deprecated("Use fromGlobalToContentRelative")
    override fun fromGlobal(globalPosition: TerminalPosition?): TerminalPosition? {
        return fromGlobalToContentRelative(globalPosition)
    }

    override fun fromGlobalToContentRelative(globalPosition: TerminalPosition?): TerminalPosition? {
        val position = lastKnownPosition
        if (globalPosition == null || position == null) {
            return null
        }
        return globalPosition.withRelative(-position.column - contentOffset.column, -position.row - contentOffset.row)
    }

    override fun fromGlobalToDecoratedRelative(globalPosition: TerminalPosition?): TerminalPosition? {
        val position = lastKnownPosition
        if (globalPosition == null || position == null) {
            return null
        }
        return globalPosition.withRelative(-position.column, -position.row)
    }

    override val preferredSize: TerminalSize?
        get() {
            var preferredSize: TerminalSize? = contentHolder.preferredSize ?: TerminalSize.ZERO
            val menuBar: MenuBar? = menuBar
            if (menuBar != null && menuBar.menuCount > 0) {
                val menuPreferredSize = menuBar.preferredSize ?: TerminalSize.ZERO
                preferredSize =
                    preferredSize?.withRelativeRows(menuPreferredSize.rows)
                        ?.withColumns(kotlin.math.max(menuPreferredSize.columns, preferredSize?.columns ?: 0))
            }
            return preferredSize ?: TerminalSize.ZERO
        }

    override fun setHints(hints: Collection<Hint?>?) {
        hintsBacking = HashSet(hints.orEmpty())
        invalidate()
    }

    override val hints: Set<Hint?>
        get() = hintsBacking.toSet()

    override val postRenderer: WindowPostRenderer?
        get() = windowPostRenderer

    override fun addWindowListener(windowListener: WindowListener?) {
        if (windowListener != null) {
            addBasePaneListener(windowListener)
        }
    }

    override fun removeWindowListener(windowListener: WindowListener?) {
        if (windowListener != null) {
            removeBasePaneListener(windowListener)
        }
    }

    fun setWindowPostRenderer(windowPostRenderer: WindowPostRenderer?) {
        this.windowPostRenderer = windowPostRenderer
    }

    override var position: TerminalPosition?
        get() = lastKnownPosition
        set(value) {
            val oldPosition = lastKnownPosition
            lastKnownPosition = value

            for (listener in basePaneListeners) {
                if (listener is WindowListener) {
                    listener.onMoved(this, oldPosition, value)
                }
            }
        }

    @Deprecated("Use setFixedSize or setDecoratedSize")
    override var size: TerminalSize?
        get() = lastKnownSize
        set(value) {
            setSizeInternal(value, true)
        }

    override fun setFixedSize(size: TerminalSize?) {
        hintsBacking.add(Hint.FIXED_SIZE)
        this.size = size
    }

    private fun setSizeInternal(
        size: TerminalSize?,
        invalidate: Boolean,
    ) {
        val oldSize = lastKnownSize
        lastKnownSize = size
        if (invalidate) {
            invalidate()
        }

        for (listener in basePaneListeners) {
            if (listener is WindowListener) {
                listener.onResized(this, oldSize, size)
            }
        }
    }

    override var decoratedSize: TerminalSize?
        get() = lastKnownDecoratedSize
        set(value) {
            lastKnownDecoratedSize = value
        }

    override fun setContentOffset(offset: TerminalPosition?) {
        if (offset != null) {
            contentOffset = offset
        }
    }

    override fun close() {
        textGUI?.removeWindow(this)
    }

    override fun waitUntilClosed() {
        textGUI?.waitForWindowToClose(this)
    }

    override fun self(): Window {
        return this
    }
}
