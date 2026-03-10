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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction

class SplitPanel protected constructor(
    private val compA: Component,
    private val compB: Component,
    private val isHorizontal: Boolean,
) : Panel() {

    private val thumb: ImageComponent = makeThumb()
    private var ratio: Double = 0.5

    init {
        setLayoutManager(ScrollPanelLayoutManager())
        setRatio(10, 10)

        addComponent(compA)
        addComponent(thumb)
        addComponent(compB)
    }

    override val isInvalid: Boolean
        get() = super.isInvalid

    internal fun makeThumb(): ImageComponent {
        return object : ImageComponent() {
            private var aSize: TerminalSize? = null
            private var bSize: TerminalSize? = null
            private var tSize: TerminalSize? = null
            private var down: TerminalPosition? = null
            private var drag: TerminalPosition? = null

            override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
                return if (keyStroke is MouseAction) {
                    handleMouseAction(keyStroke)
                } else {
                    super.handleKeyStroke(keyStroke)
                }
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
                    if (down == null) {
                        down = drag
                    }

                    val deltaPosition = drag?.minus(down ?: TerminalPosition.TOP_LEFT_CORNER) ?: TerminalPosition.TOP_LEFT_CORNER
                    val delta = if (isHorizontal) deltaPosition.column else deltaPosition.row
                    if (isHorizontal) {
                        val a = maxOf(1, (tSize?.columns ?: 0) + (aSize?.columns ?: 0) + delta)
                        val b = maxOf(1, (bSize?.columns ?: 0) - delta)
                        setRatio(a, b)
                    } else {
                        val a = maxOf(1, (tSize?.rows ?: 0) + (aSize?.rows ?: 0) + delta)
                        val b = maxOf(1, (bSize?.rows ?: 0) - delta)
                        setRatio(a, b)
                    }
                }

                if (mouseAction.isMouseUp) {
                    down = null
                    drag = null
                }
                return Interactable.Result.HANDLED
            }
        }
    }

    internal inner class ScrollPanelLayoutManager : LayoutManager {
        private var changed: Boolean = true

        override fun getPreferredSize(components: List<Component?>?): TerminalSize {
            val sizeA = compA.preferredSize ?: TerminalSize.ZERO
            val sizeB = compB.preferredSize ?: TerminalSize.ZERO
            val thumbSize = thumb.preferredSize ?: TerminalSize.ONE

            return if (isHorizontal) {
                TerminalSize(
                    sizeA.columns + thumbSize.columns + sizeB.columns,
                    maxOf(sizeA.rows, maxOf(thumbSize.rows, sizeB.rows)),
                )
            } else {
                TerminalSize(
                    maxOf(sizeA.columns, maxOf(thumbSize.columns, sizeB.columns)),
                    sizeA.rows + thumbSize.rows + sizeB.rows,
                )
            }
        }

        override fun doLayout(area: TerminalSize?, components: List<Component?>?) {
            val panelSize = size ?: TerminalSize.ZERO

            val length = if (isHorizontal) panelSize.rows else panelSize.columns
            val thumbImageSize = TerminalSize(if (isHorizontal) 1 else length, if (!isHorizontal) 1 else length)
            val textImage: TextImage = BasicTextImage(thumbImageSize)
            val themeDefinition = theme?.defaultDefinition
            val themeStyle = themeDefinition?.normal
            var thumbRenderer = TextCharacter.fromCharacter(
                if (isHorizontal) Symbols.SINGLE_LINE_VERTICAL else Symbols.SINGLE_LINE_HORIZONTAL,
                themeStyle?.foreground,
                themeStyle?.background,
            )
            if (thumb.isFocused && thumbRenderer != null) {
                thumbRenderer = thumbRenderer.withModifier(SGR.BOLD)
            }
            textImage.setAll(thumbRenderer)
            thumb.setTextImage(textImage)

            val thumbSize = thumb.preferredSize ?: TerminalSize.ONE
            var width = panelSize.columns
            var height = panelSize.rows
            if (isHorizontal) {
                width -= thumbSize.columns
            } else {
                height -= thumbSize.rows
            }

            val compAPrevSize = compA.size
            val compBPrevSize = compB.size
            val thumbPrevSize = thumb.size
            val compAPrevPos = compA.position
            val compBPrevPos = compB.position
            val thumbPrevPos = thumb.position

            if (isHorizontal) {
                val leftWidth = maxOf(0, (width * ratio).toInt())
                val leftHeight = maxOf(0, minOf(compA.preferredSize?.rows ?: 0, height))
                val rightWidth = maxOf(0, width - leftWidth)
                val rightHeight = maxOf(0, minOf(compB.preferredSize?.rows ?: 0, height))

                compA.setSize(TerminalSize(leftWidth, leftHeight))
                thumb.setSize(thumbSize)
                compB.setSize(TerminalSize(rightWidth, rightHeight))

                compA.setPosition(TerminalPosition(0, 0))
                thumb.setPosition(TerminalPosition(leftWidth, height / 2 - thumbSize.rows / 2))
                compB.setPosition(TerminalPosition(leftWidth + thumbSize.columns, 0))
            } else {
                val topWidth = maxOf(0, minOf(compA.preferredSize?.columns ?: 0, width))
                val topHeight = maxOf(0, (height * ratio).toInt())
                val bottomWidth = maxOf(0, minOf(compB.preferredSize?.columns ?: 0, width))
                val bottomHeight = maxOf(0, height - topHeight)

                compA.setSize(TerminalSize(topWidth, topHeight))
                thumb.setSize(thumbSize)
                compB.setSize(TerminalSize(bottomWidth, bottomHeight))

                compA.setPosition(TerminalPosition(0, 0))
                thumb.setPosition(TerminalPosition(width / 2 - thumbSize.columns / 2, topHeight))
                compB.setPosition(TerminalPosition(0, topHeight + thumbSize.rows))
            }

            changed =
                compAPrevPos != compA.position ||
                compAPrevSize != compA.size ||
                compBPrevPos != compB.position ||
                compBPrevSize != compB.size ||
                thumbPrevPos != thumb.position ||
                thumbPrevSize != thumb.size
        }

        override fun hasChanged(): Boolean = changed
    }

    fun setRatio(left: Int, right: Int) {
        ratio =
            if (left == 0 || right == 0) {
                0.5
            } else {
                left.toDouble() / (kotlin.math.abs(left) + kotlin.math.abs(right)).toDouble()
            }
    }

    fun setThumbVisible(visible: Boolean) {
        thumb.setVisible(visible)
        if (visible) {
            setPreferredSize(null)
        } else {
            thumb.setPreferredSize(TerminalSize(1, 1))
        }
    }

    companion object {
        fun ofHorizontal(left: Component?, right: Component?): SplitPanel {
            requireNotNull(left) { "Left component cannot be null" }
            requireNotNull(right) { "Right component cannot be null" }
            return SplitPanel(left, right, true)
        }

        fun ofVertical(top: Component?, bottom: Component?): SplitPanel {
            requireNotNull(top) { "Top component cannot be null" }
            requireNotNull(bottom) { "Bottom component cannot be null" }
            return SplitPanel(top, bottom, false)
        }
    }
}
