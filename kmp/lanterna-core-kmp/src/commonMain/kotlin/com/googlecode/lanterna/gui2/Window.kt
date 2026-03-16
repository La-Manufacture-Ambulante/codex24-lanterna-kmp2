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

interface Window : BasePane {
    override var textGUI: WindowBasedTextGUI?

    val title: String?

    var isVisible: Boolean

    override val isInvalid: Boolean

    val bounds: TerminalRectangle
        get() {
            val windowPosition = position ?: TerminalPosition.TOP_LEFT_CORNER
            val windowSize = decoratedSize ?: TerminalSize.ZERO
            return TerminalRectangle(
                windowPosition.column,
                windowPosition.row,
                windowSize.columns,
                windowSize.rows,
            )
        }

    val preferredSize: TerminalSize?

    val hints: Set<Hint?>?

    var position: TerminalPosition?

    @set:Deprecated(
        "This method is deprecated now as it probably doesn't do what you think. " +
            "Please use setFixedSize or setDecoratedSize instead.",
    )
    var size: TerminalSize?

    var decoratedSize: TerminalSize?

    val postRenderer: WindowPostRenderer?

    override var component: Component?

    override var focusedInteractable: Interactable?

    override val cursorPosition: TerminalPosition?

    override var menuBar: MenuBar?

    override fun invalidate()

    fun close()

    fun setHints(hints: Collection<Hint?>?)

    fun setFixedSize(size: TerminalSize?)

    fun setContentOffset(offset: TerminalPosition?)

    fun waitUntilClosed()

    fun addWindowListener(windowListener: WindowListener?)

    fun removeWindowListener(windowListener: WindowListener?)

    override fun draw(graphics: TextGUIGraphics?)

    override fun handleInput(key: KeyStroke?): Boolean

    @Deprecated(
        "This is deprecated in favor of calling either toGlobalFromContentRelative " +
            "or toGlobalFromDecoratedRelative.",
    )
    override fun toGlobal(localPosition: TerminalPosition?): TerminalPosition?

    fun toGlobalFromContentRelative(localPosition: TerminalPosition?): TerminalPosition?

    fun toGlobalFromDecoratedRelative(decoratedPosition: TerminalPosition?): TerminalPosition?

    @Deprecated(
        "This is deprecated in favor of calling either fromGlobalToContentRelative " +
            "or fromGlobalToDecoratedRelative.",
    )
    override fun fromGlobal(position: TerminalPosition?): TerminalPosition?

    fun fromGlobalToContentRelative(position: TerminalPosition?): TerminalPosition?

    fun fromGlobalToDecoratedRelative(position: TerminalPosition?): TerminalPosition?

    open class Hint protected constructor(private val info: String? = null) {
        override fun toString(): String {
            return info ?: super.toString()
        }

        companion object {
            val NO_DECORATIONS = Hint("NoDeco")

            val NO_POST_RENDERING = Hint("NoPostRend")

            val NO_FOCUS = Hint("NoFocus")

            val CENTERED = Hint("Centered")

            val FIXED_POSITION = Hint("FixedPos")

            val MENU_POPUP = Hint("MenuPopup")

            val FIXED_SIZE = Hint("FixedSize")

            val FIT_TERMINAL_WINDOW = Hint("FitTermWin")

            val MODAL = Hint("Modal")

            val FULL_SCREEN = Hint("FullScreen")

            val EXPANDED = Hint("Expanded")
        }
    }
}
