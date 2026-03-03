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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.input.*
import java.util.*

/**
 * @author ginkoblongata
 */
open class SplitPanel protected constructor(a: Component, b: Component, isHorizontal: Boolean) : Panel() {

    private val compA: Component
    private val thumb: ImageComponent
    private val compB: Component

    private var isHorizontal: Boolean
    private var ratio: Double = 0.5

    init {
        this.compA = a
        this.compB = b
        this.isHorizontal = isHorizontal
        thumb = makeThumb()
        layoutManager = ScrollPanelLayoutManager()
        setRatio(10, 10)

        addComponent(a)
        addComponent(thumb)
        addComponent(b)
    }

    internal fun makeThumb(): ImageComponent {
        val imageComponent: ImageComponent = object : ImageComponent() {
            var aSize: TerminalSize? = null
            var bSize: TerminalSize? = null
            var tSize: TerminalSize? = null
            var down: TerminalPosition? = null
            var drag: TerminalPosition? = null

            override fun handleKeyStroke(keyStroke: KeyStroke?): Interactable.Result {
                val result: Interactable.Result = if (keyStroke is MouseAction) {
                    handleMouseAction(keyStroke)
                } else {
                    // TODO: Implement keyboard based resizing
                    super.handleKeyStroke(keyStroke)
                }
                return result
            }

            private fun handleMouseAction(mouseAction: MouseAction): Interactable.Result {
                if (mouseAction.isMouseDown) {
                    aSize = compA.size
                    bSize = compB.size
                    tSize = thumb.size
                    down = mouseAction.position
                }
                if (mouseAction.isMouseDrag) {
                    drag = mouseAction.position

                    // xxxxxxxxxxxxxxxxxxxxx
                    // this is a hack, should not be needed if the pane drag
                    // only on mouse down'd comp stuff was completely working
                    if (down == null) {
                        down = drag
                    }
                    // xxxxxxxxxxxxxxxxxxxxx

                    val dragPos = drag ?: throw NullPointerException()
                    val downPos = down ?: throw NullPointerException()
                    val delta = if (isHorizontal) dragPos.minus(downPos).column else dragPos.minus(downPos).row
                    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                    if (isHorizontal) {
                        val localTSize = tSize ?: throw NullPointerException()
                        val localASize = aSize ?: throw NullPointerException()
                        val localBSize = bSize ?: throw NullPointerException()
                        val a = Math.max(1, localTSize.columns + localASize.columns + delta)
                        val b = Math.max(1, localBSize.columns - delta)
                        setRatio(a, b)
                    } else {
                        val localTSize = tSize ?: throw NullPointerException()
                        val localASize = aSize ?: throw NullPointerException()
                        val localBSize = bSize ?: throw NullPointerException()
                        val a = Math.max(1, localTSize.rows + localASize.rows + delta)
                        val b = Math.max(1, localBSize.rows - delta)
                        setRatio(a, b)
                    }
                    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                }
                if (mouseAction.isMouseUp) {
                    down = null
                    drag = null
                }
                return Interactable.Result.HANDLED
            }
        }
        return imageComponent
    }

    internal inner class ScrollPanelLayoutManager : LayoutManager {

        internal var hasChanged: Boolean

        init {
            hasChanged = true
        }

        override fun getPreferredSize(components: MutableList<Component>?): TerminalSize {
            val sizeA = compA.preferredSize
            val aWidth = sizeA.columns
            val aHeight = sizeA.rows
            val sizeB = compB.preferredSize
            val bWidth = sizeB.columns
            val bHeight = sizeB.rows

            val tWidth = thumb.preferredSize.columns
            val tHeight = thumb.preferredSize.rows

            return if (isHorizontal) {
                TerminalSize(aWidth + tWidth + bWidth, Math.max(aHeight, Math.max(tHeight, bHeight)))
            } else {
                TerminalSize(Math.max(aWidth, Math.max(tWidth, bWidth)), aHeight + tHeight + bHeight)
            }
        }

        override fun doLayout(area: TerminalSize?, components: MutableList<Component>?) {
            val size = size

            // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            // TODO: themed
            val length = if (isHorizontal) size.rows else size.columns
            val tsize = TerminalSize(if (isHorizontal) 1 else length, if (!isHorizontal) 1 else length)
            val textImage: TextImage = BasicTextImage(tsize)
            val theme = theme
            val themeDefinition = theme.defaultDefinition
            val themeStyle = themeDefinition.normal

            var thumbRenderer = TextCharacter.fromCharacter(
                if (isHorizontal) Symbols.SINGLE_LINE_VERTICAL else Symbols.SINGLE_LINE_HORIZONTAL,
                themeStyle.foreground,
                themeStyle.background
            )
            if (thumb.isFocused) {
                thumbRenderer = thumbRenderer.withModifier(SGR.BOLD)
            }

            textImage.setAll(thumbRenderer)
            thumb.textImage = textImage
            // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

            val tWidth = thumb.preferredSize.columns
            val tHeight = thumb.preferredSize.rows

            var w = size.columns
            var h = size.rows

            if (isHorizontal) {
                w -= tWidth
            } else {
                h -= tHeight
            }

            val compAPrevSize = compA.size
            val compBPrevSize = compB.size
            val thumbPrevSize = thumb.size
            val compAPrevPos = compA.position
            val compBPrevPos = compB.position
            val thumbPrevPos = thumb.position

            if (isHorizontal) {
                val leftWidth = Math.max(0, (w * ratio).toInt())
                val leftHeight = Math.max(0, Math.min(compA.preferredSize.rows, h))

                val rightWidth = Math.max(0, w - leftWidth)
                val rightHeight = Math.max(0, Math.min(compB.preferredSize.rows, h))

                compA.size = TerminalSize(leftWidth, leftHeight)
                thumb.size = thumb.preferredSize
                compB.size = TerminalSize(rightWidth, rightHeight)

                compA.position = TerminalPosition(0, 0)
                thumb.position = TerminalPosition(leftWidth, h / 2 - tHeight / 2)
                compB.position = TerminalPosition(leftWidth + tWidth, 0)
            } else {
                val leftWidth = Math.max(0, Math.min(compA.preferredSize.columns, w))
                val leftHeight = Math.max(0, (h * ratio).toInt())

                val rightWidth = Math.max(0, Math.min(compB.preferredSize.columns, w))
                val rightHeight = Math.max(0, h - leftHeight)

                compA.size = TerminalSize(leftWidth, leftHeight)
                thumb.size = thumb.preferredSize
                compB.size = TerminalSize(rightWidth, rightHeight)

                compA.position = TerminalPosition(0, 0)
                thumb.position = TerminalPosition(w / 2 - tWidth / 2, leftHeight)
                compB.position = TerminalPosition(0, leftHeight + tHeight)
            }

            hasChanged = compAPrevPos != compA.position ||
                compAPrevSize != compA.size ||
                compBPrevPos != compB.position ||
                compBPrevSize != compB.size ||
                thumbPrevPos != thumb.position ||
                thumbPrevSize != thumb.size
        }

        override fun hasChanged(): Boolean {
            return hasChanged
        }
    }

    /*
     * Use whatever sizing.
     *
     *
     */
    open fun setRatio(left: Int, right: Int) {
        if (left == 0 || right == 0) {
            ratio = 0.5
        } else {
            val total = Math.abs(left) + Math.abs(right)
            ratio = left.toDouble() / total.toDouble()
        }
    }

    open fun setThumbVisible(visible: Boolean) {
        thumb.isVisible = visible

        if (visible) {
            this.preferredSize = null
        } else {
            thumb.preferredSize = TerminalSize(1, 1)
        }
    }

    public override open fun isInvalid(): Boolean {
        return super.isInvalid()
    }

    companion object {
        @JvmStatic
        fun ofHorizontal(left: Component, right: Component): SplitPanel {
            val split = SplitPanel(left, right, true)
            return split
        }

        @JvmStatic
        fun ofVertical(top: Component, bottom: Component): SplitPanel {
            val split = SplitPanel(top, bottom, false)
            return split
        }
    }
}
