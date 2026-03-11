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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * Classic scrollbar that can be used to display where inside a larger component a view is showing. This implementation
 * is not interactable and needs to be driven externally, meaning you can't focus on the scrollbar itself, you have to
 * update its state as part of another component being modified. `ScrollBar`s are either horizontal or vertical,
 * which affects the way they appear and how they are drawn.
 *
 * This class works on two concepts, the min-position-max values and the view size. The minimum value is always 0 and
 * cannot be changed. The maximum value is 100 and can be adjusted programmatically. Position value is wherever along the
 * axis of 0 to max the scrollbar's tracker currently is placed. The view size is an important concept, it determines
 * how big the tracker should be and limits the position so that it can only reach `maximum value - view size`.
 *
 * @author Martin
 */
class ScrollBar(val direction: Direction) : AbstractComponent<ScrollBar>() {
    private var maximum: Int = 100
    private var scrollPosition: Int = 0
    private var viewSize: Int = 0

    fun setScrollMaximum(maximum: Int): ScrollBar {
        require(maximum >= 0) { "Cannot set ScrollBar maximum to $maximum" }
        if (this.maximum != maximum) {
            this.maximum = maximum
            invalidate()
        }
        return this
    }

    fun getScrollMaximum(): Int = maximum

    fun setScrollPosition(position: Int): ScrollBar {
        val newPosition = minOf(position, maximum)
        if (scrollPosition != newPosition) {
            scrollPosition = newPosition
            invalidate()
        }
        return this
    }

    fun getScrollPosition(): Int = scrollPosition

    fun setViewSize(viewSize: Int): ScrollBar {
        this.viewSize = viewSize
        return this
    }

    fun getViewSize(): Int {
        if (viewSize > 0) {
            return viewSize
        }
        val currentSize = size ?: TerminalSize.ZERO
        return if (direction == Direction.HORIZONTAL) currentSize.columns else currentSize.rows
    }

    override fun createDefaultRenderer(): ComponentRenderer<ScrollBar?> {
        return DefaultScrollBarRenderer()
    }

    /**
     * Helper class for making new `ScrollBar` renderers a little bit cleaner.
     */
    abstract class ScrollBarRenderer : ComponentRenderer<ScrollBar?> {
        override fun getPreferredSize(component: ScrollBar?): TerminalSize {
            return TerminalSize.ONE
        }
    }

    /**
     * Default renderer for `ScrollBar` which will be used unless overridden.
     */
    class DefaultScrollBarRenderer : ScrollBarRenderer() {
        private var growScrollTracker: Boolean = true

        fun setGrowScrollTracker(growScrollTracker: Boolean) {
            this.growScrollTracker = growScrollTracker
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: ScrollBar?) {
            val activeGraphics = graphics ?: return
            val activeComponent = component ?: return
            val size = activeGraphics.size ?: TerminalSize.ZERO
            val direction = activeComponent.direction
            var position = activeComponent.getScrollPosition()
            val maximum = activeComponent.getScrollMaximum()
            val viewSize = activeComponent.getViewSize()

            if (size.rows == 0 || size.columns == 0) {
                return
            }

            if (position + viewSize >= maximum) {
                position = maxOf(0, maximum - viewSize)
                activeComponent.setScrollPosition(position)
            }

            val themeDefinition: ThemeDefinition = activeComponent.themeDefinition ?: return
            activeGraphics.applyThemeStyle(themeDefinition.normal)

            if (direction == Direction.VERTICAL) {
                drawVertical(activeGraphics, themeDefinition, size, position, maximum, viewSize)
            } else {
                drawHorizontal(activeGraphics, themeDefinition, size, position, maximum, viewSize)
            }
        }

        private fun drawVertical(
            graphics: TextGUIGraphics,
            themeDefinition: ThemeDefinition,
            size: TerminalSize,
            position: Int,
            maximum: Int,
            viewSize: Int,
        ) {
            when (size.rows) {
                1 -> graphics.setCharacter(0, 0, themeDefinition.getCharacter("VERTICAL_BACKGROUND", Symbols.BLOCK_MIDDLE))
                2 -> {
                    graphics.setCharacter(0, 0, themeDefinition.getCharacter("UP_ARROW", Symbols.TRIANGLE_UP_POINTING_BLACK))
                    graphics.setCharacter(
                        0,
                        1,
                        themeDefinition.getCharacter("DOWN_ARROW", Symbols.TRIANGLE_DOWN_POINTING_BLACK),
                    )
                }
                else -> {
                    val scrollableArea = size.rows - 2
                    var scrollTrackerSize = 1
                    if (growScrollTracker) {
                        val ratio = clampRatio(viewSize.toFloat() / maximum.toFloat())
                        scrollTrackerSize = maxOf(1, (ratio * scrollableArea.toFloat()).toInt())
                    }

                    val denominator = (maximum - viewSize).toFloat()
                    val ratio = clampRatio(if (denominator == 0f) 0f else position.toFloat() / denominator)
                    val scrollTrackerPosition = (ratio * (scrollableArea - scrollTrackerSize).toFloat()).toInt() + 1

                    graphics.setCharacter(0, 0, themeDefinition.getCharacter("UP_ARROW", Symbols.TRIANGLE_UP_POINTING_BLACK))
                    graphics.drawLine(
                        0,
                        1,
                        0,
                        size.rows - 2,
                        themeDefinition.getCharacter("VERTICAL_BACKGROUND", Symbols.BLOCK_MIDDLE),
                    )
                    graphics.setCharacter(
                        0,
                        size.rows - 1,
                        themeDefinition.getCharacter("DOWN_ARROW", Symbols.TRIANGLE_DOWN_POINTING_BLACK),
                    )
                    when (scrollTrackerSize) {
                        1 -> graphics.setCharacter(
                            0,
                            scrollTrackerPosition,
                            themeDefinition.getCharacter("VERTICAL_SMALL_TRACKER", Symbols.BLOCK_SOLID),
                        )
                        2 -> {
                            graphics.setCharacter(
                                0,
                                scrollTrackerPosition,
                                themeDefinition.getCharacter("VERTICAL_TRACKER_TOP", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                0,
                                scrollTrackerPosition + 1,
                                themeDefinition.getCharacter("VERTICAL_TRACKER_BOTTOM", Symbols.BLOCK_SOLID),
                            )
                        }
                        else -> {
                            graphics.setCharacter(
                                0,
                                scrollTrackerPosition,
                                themeDefinition.getCharacter("VERTICAL_TRACKER_TOP", Symbols.BLOCK_SOLID),
                            )
                            graphics.drawLine(
                                0,
                                scrollTrackerPosition + 1,
                                0,
                                scrollTrackerPosition + scrollTrackerSize - 2,
                                themeDefinition.getCharacter("VERTICAL_TRACKER_BACKGROUND", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                0,
                                scrollTrackerPosition + (scrollTrackerSize / 2),
                                themeDefinition.getCharacter("VERTICAL_SMALL_TRACKER", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                0,
                                scrollTrackerPosition + scrollTrackerSize - 1,
                                themeDefinition.getCharacter("VERTICAL_TRACKER_BOTTOM", Symbols.BLOCK_SOLID),
                            )
                        }
                    }
                }
            }
        }

        private fun drawHorizontal(
            graphics: TextGUIGraphics,
            themeDefinition: ThemeDefinition,
            size: TerminalSize,
            position: Int,
            maximum: Int,
            viewSize: Int,
        ) {
            when (size.columns) {
                1 -> graphics.setCharacter(
                    0,
                    0,
                    themeDefinition.getCharacter("HORIZONTAL_BACKGROUND", Symbols.BLOCK_MIDDLE),
                )
                2 -> {
                    graphics.setCharacter(0, 0, Symbols.TRIANGLE_LEFT_POINTING_BLACK)
                    graphics.setCharacter(1, 0, Symbols.TRIANGLE_RIGHT_POINTING_BLACK)
                }
                else -> {
                    val scrollableArea = size.columns - 2
                    var scrollTrackerSize = 1
                    if (growScrollTracker) {
                        val ratio = clampRatio(viewSize.toFloat() / maximum.toFloat())
                        scrollTrackerSize = maxOf(1, (ratio * scrollableArea.toFloat()).toInt())
                    }

                    val denominator = (maximum - viewSize).toFloat()
                    val ratio = clampRatio(if (denominator == 0f) 0f else position.toFloat() / denominator)
                    val scrollTrackerPosition = (ratio * (scrollableArea - scrollTrackerSize).toFloat()).toInt() + 1

                    graphics.setCharacter(
                        0,
                        0,
                        themeDefinition.getCharacter("LEFT_ARROW", Symbols.TRIANGLE_LEFT_POINTING_BLACK),
                    )
                    graphics.drawLine(
                        1,
                        0,
                        size.columns - 2,
                        0,
                        themeDefinition.getCharacter("HORIZONTAL_BACKGROUND", Symbols.BLOCK_MIDDLE),
                    )
                    graphics.setCharacter(
                        size.columns - 1,
                        0,
                        themeDefinition.getCharacter("RIGHT_ARROW", Symbols.TRIANGLE_RIGHT_POINTING_BLACK),
                    )
                    when (scrollTrackerSize) {
                        1 -> graphics.setCharacter(
                            scrollTrackerPosition,
                            0,
                            themeDefinition.getCharacter("HORIZONTAL_SMALL_TRACKER", Symbols.BLOCK_SOLID),
                        )
                        2 -> {
                            graphics.setCharacter(
                                scrollTrackerPosition,
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_TRACKER_LEFT", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                scrollTrackerPosition + 1,
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_TRACKER_RIGHT", Symbols.BLOCK_SOLID),
                            )
                        }
                        else -> {
                            graphics.setCharacter(
                                scrollTrackerPosition,
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_TRACKER_LEFT", Symbols.BLOCK_SOLID),
                            )
                            graphics.drawLine(
                                scrollTrackerPosition + 1,
                                0,
                                scrollTrackerPosition + scrollTrackerSize - 2,
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_TRACKER_BACKGROUND", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                scrollTrackerPosition + (scrollTrackerSize / 2),
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_SMALL_TRACKER", Symbols.BLOCK_SOLID),
                            )
                            graphics.setCharacter(
                                scrollTrackerPosition + scrollTrackerSize - 1,
                                0,
                                themeDefinition.getCharacter("HORIZONTAL_TRACKER_RIGHT", Symbols.BLOCK_SOLID),
                            )
                        }
                    }
                }
            }
        }

        private fun clampRatio(value: Float): Float {
            return when {
                value < 0.0f -> 0.0f
                value > 1.0f -> 1.0f
                else -> value
            }
        }
    }
}
