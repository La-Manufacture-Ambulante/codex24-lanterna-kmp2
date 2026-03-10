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
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * Border helpers and implementations.
 */
object Borders {
    private enum class BorderStyle {
        Solid,
        Bevel,
        ReverseBevel,
    }

    @JvmOverloads
    fun singleLine(title: String = ""): Border {
        return SingleLine(title, BorderStyle.Solid)
    }

    @JvmOverloads
    fun singleLineBevel(title: String = ""): Border {
        return SingleLine(title, BorderStyle.Bevel)
    }

    @JvmOverloads
    fun singleLineReverseBevel(title: String = ""): Border {
        return SingleLine(title, BorderStyle.ReverseBevel)
    }

    @JvmOverloads
    fun doubleLine(title: String = ""): Border {
        return DoubleLine(title, BorderStyle.Solid)
    }

    @JvmOverloads
    fun doubleLineBevel(title: String = ""): Border {
        return DoubleLine(title, BorderStyle.Bevel)
    }

    @JvmOverloads
    fun doubleLineReverseBevel(title: String = ""): Border {
        return DoubleLine(title, BorderStyle.ReverseBevel)
    }

    private abstract class StandardBorder(
        private val title: String,
        protected val borderStyle: BorderStyle,
    ) : AbstractBorder() {
        fun getTitle(): String {
            return title
        }

        override fun toString(): String {
            return javaClass.simpleName + "{" + title + "}"
        }
    }

    private abstract class AbstractBorderRenderer(
        private val borderStyle: BorderStyle,
    ) : Border.BorderRenderer {
        override val wrappedComponentTopLeftOffset: TerminalPosition
            get() = TerminalPosition.OFFSET_1x1

        override fun getPreferredSize(component: Border?): TerminalSize {
            val border = component as StandardBorder
            val wrappedComponent = border.component
            var preferredSize = if (wrappedComponent == null) {
                TerminalSize.ZERO
            } else {
                wrappedComponent.preferredSize ?: TerminalSize.ZERO
            }
            preferredSize = preferredSize.withRelativeColumns(2)?.withRelativeRows(2) ?: TerminalSize.ZERO
            val borderTitle = border.getTitle()
            val titleWidth = if (borderTitle.isEmpty()) 2 else TerminalTextUtils.getColumnWidth(borderTitle) + 4
            return preferredSize.max(TerminalSize(titleWidth, 2)) ?: preferredSize
        }

        override fun getWrappedComponentSize(borderSize: TerminalSize?): TerminalSize {
            val size = borderSize ?: TerminalSize.ZERO
            return size
                .withRelativeColumns(-kotlin.math.min(2, size.columns))
                ?.withRelativeRows(-kotlin.math.min(2, size.rows))
                ?: TerminalSize.ZERO
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: Border?) {
            val border = component as? StandardBorder ?: return
            val wrappedComponent = border.component ?: return
            val g = graphics ?: return
            val drawableArea = g.size ?: return
            val theme = component.theme ?: return

            val horizontalLine = getHorizontalLine(theme)
            val verticalLine = getVerticalLine(theme)
            val bottomLeftCorner = getBottomLeftCorner(theme)
            val topLeftCorner = getTopLeftCorner(theme)
            val bottomRightCorner = getBottomRightCorner(theme)
            val topRightCorner = getTopRightCorner(theme)
            val titleLeft = getTitleLeft(theme)
            val titleRight = getTitleRight(theme)

            val themeDefinition: ThemeDefinition = theme.getDefinition(AbstractBorder::class.java) ?: return
            if (borderStyle == BorderStyle.Bevel) {
                g.applyThemeStyle(themeDefinition.preLight)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }
            g.setCharacter(0, drawableArea.rows - 1, bottomLeftCorner)
            if (drawableArea.rows > 2) {
                g.drawLine(TerminalPosition(0, drawableArea.rows - 2), TerminalPosition(0, 1), verticalLine)
            }
            g.setCharacter(0, 0, topLeftCorner)
            if (drawableArea.columns > 2) {
                g.drawLine(TerminalPosition(1, 0), TerminalPosition(drawableArea.columns - 2, 0), horizontalLine)
            }

            if (borderStyle == BorderStyle.ReverseBevel) {
                g.applyThemeStyle(themeDefinition.preLight)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }
            g.setCharacter(drawableArea.columns - 1, 0, topRightCorner)
            if (drawableArea.rows > 2) {
                g.drawLine(
                    TerminalPosition(drawableArea.columns - 1, 1),
                    TerminalPosition(drawableArea.columns - 1, drawableArea.rows - 2),
                    verticalLine,
                )
            }
            g.setCharacter(drawableArea.columns - 1, drawableArea.rows - 1, bottomRightCorner)
            if (drawableArea.columns > 2) {
                g.drawLine(
                    TerminalPosition(1, drawableArea.rows - 1),
                    TerminalPosition(drawableArea.columns - 2, drawableArea.rows - 1),
                    horizontalLine,
                )
            }

            if (border.getTitle().isNotEmpty() && drawableArea.columns >= TerminalTextUtils.getColumnWidth(border.getTitle()) + 4) {
                g.applyThemeStyle(themeDefinition.active)
                g.putString(2, 0, border.getTitle())

                if (borderStyle == BorderStyle.Bevel) {
                    g.applyThemeStyle(themeDefinition.preLight)
                } else {
                    g.applyThemeStyle(themeDefinition.normal)
                }
                g.setCharacter(1, 0, titleLeft)
                g.setCharacter(2 + TerminalTextUtils.getColumnWidth(border.getTitle()), 0, titleRight)
            }

            wrappedComponent.draw(g.newTextGraphics(wrappedComponentTopLeftOffset, getWrappedComponentSize(drawableArea)))
            joinLinesWithFrame(g)
        }

        protected abstract fun getHorizontalLine(theme: Theme): Char
        protected abstract fun getVerticalLine(theme: Theme): Char
        protected abstract fun getBottomLeftCorner(theme: Theme): Char
        protected abstract fun getTopLeftCorner(theme: Theme): Char
        protected abstract fun getBottomRightCorner(theme: Theme): Char
        protected abstract fun getTopRightCorner(theme: Theme): Char
        protected abstract fun getTitleLeft(theme: Theme): Char
        protected abstract fun getTitleRight(theme: Theme): Char
    }

    fun joinLinesWithFrame(graphics: TextGraphics) {
        val drawableArea = graphics.size ?: return
        if (drawableArea.rows <= 2 || drawableArea.columns <= 2) {
            return
        }

        val upperRow = 0
        val lowerRow = drawableArea.rows - 1
        val leftRow = 0
        val rightRow = drawableArea.columns - 1

        val junctionFromBelowSingle = listOf(
            Symbols.SINGLE_LINE_VERTICAL,
            Symbols.BOLD_FROM_NORMAL_SINGLE_LINE_VERTICAL,
            Symbols.BOLD_SINGLE_LINE_VERTICAL,
            Symbols.SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS,
            Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER,
            Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER,
            Symbols.SINGLE_LINE_T_LEFT,
            Symbols.SINGLE_LINE_T_RIGHT,
            Symbols.SINGLE_LINE_T_UP,
            Symbols.SINGLE_LINE_T_DOUBLE_LEFT,
            Symbols.SINGLE_LINE_T_DOUBLE_RIGHT,
            Symbols.DOUBLE_LINE_T_SINGLE_UP,
        )
        val junctionFromBelowDouble = listOf(
            Symbols.DOUBLE_LINE_VERTICAL,
            Symbols.DOUBLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER,
            Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER,
            Symbols.DOUBLE_LINE_T_LEFT,
            Symbols.DOUBLE_LINE_T_RIGHT,
            Symbols.DOUBLE_LINE_T_UP,
            Symbols.DOUBLE_LINE_T_SINGLE_LEFT,
            Symbols.DOUBLE_LINE_T_SINGLE_RIGHT,
            Symbols.SINGLE_LINE_T_DOUBLE_UP,
        )
        val junctionFromAboveSingle = listOf(
            Symbols.SINGLE_LINE_VERTICAL,
            Symbols.BOLD_TO_NORMAL_SINGLE_LINE_VERTICAL,
            Symbols.BOLD_SINGLE_LINE_VERTICAL,
            Symbols.SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS,
            Symbols.SINGLE_LINE_TOP_LEFT_CORNER,
            Symbols.SINGLE_LINE_TOP_RIGHT_CORNER,
            Symbols.SINGLE_LINE_T_LEFT,
            Symbols.SINGLE_LINE_T_RIGHT,
            Symbols.SINGLE_LINE_T_DOWN,
            Symbols.SINGLE_LINE_T_DOUBLE_LEFT,
            Symbols.SINGLE_LINE_T_DOUBLE_RIGHT,
            Symbols.DOUBLE_LINE_T_SINGLE_DOWN,
        )
        val junctionFromAboveDouble = listOf(
            Symbols.DOUBLE_LINE_VERTICAL,
            Symbols.DOUBLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_TOP_LEFT_CORNER,
            Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER,
            Symbols.DOUBLE_LINE_T_LEFT,
            Symbols.DOUBLE_LINE_T_RIGHT,
            Symbols.DOUBLE_LINE_T_DOWN,
            Symbols.DOUBLE_LINE_T_SINGLE_LEFT,
            Symbols.DOUBLE_LINE_T_SINGLE_RIGHT,
            Symbols.SINGLE_LINE_T_DOUBLE_DOWN,
        )
        val junctionFromLeftSingle = listOf(
            Symbols.SINGLE_LINE_HORIZONTAL,
            Symbols.BOLD_TO_NORMAL_SINGLE_LINE_HORIZONTAL,
            Symbols.BOLD_SINGLE_LINE_HORIZONTAL,
            Symbols.SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS,
            Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER,
            Symbols.SINGLE_LINE_TOP_LEFT_CORNER,
            Symbols.SINGLE_LINE_T_UP,
            Symbols.SINGLE_LINE_T_DOWN,
            Symbols.SINGLE_LINE_T_RIGHT,
            Symbols.SINGLE_LINE_T_DOUBLE_UP,
            Symbols.SINGLE_LINE_T_DOUBLE_DOWN,
            Symbols.DOUBLE_LINE_T_SINGLE_RIGHT,
        )
        val junctionFromLeftDouble = listOf(
            Symbols.DOUBLE_LINE_HORIZONTAL,
            Symbols.DOUBLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER,
            Symbols.DOUBLE_LINE_TOP_LEFT_CORNER,
            Symbols.DOUBLE_LINE_T_UP,
            Symbols.DOUBLE_LINE_T_DOWN,
            Symbols.DOUBLE_LINE_T_RIGHT,
            Symbols.DOUBLE_LINE_T_SINGLE_UP,
            Symbols.DOUBLE_LINE_T_SINGLE_DOWN,
            Symbols.SINGLE_LINE_T_DOUBLE_RIGHT,
        )
        val junctionFromRightSingle = listOf(
            Symbols.SINGLE_LINE_HORIZONTAL,
            Symbols.BOLD_FROM_NORMAL_SINGLE_LINE_HORIZONTAL,
            Symbols.BOLD_SINGLE_LINE_HORIZONTAL,
            Symbols.SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS,
            Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER,
            Symbols.SINGLE_LINE_TOP_RIGHT_CORNER,
            Symbols.SINGLE_LINE_T_UP,
            Symbols.SINGLE_LINE_T_DOWN,
            Symbols.SINGLE_LINE_T_LEFT,
            Symbols.SINGLE_LINE_T_DOUBLE_UP,
            Symbols.SINGLE_LINE_T_DOUBLE_DOWN,
            Symbols.DOUBLE_LINE_T_SINGLE_LEFT,
        )
        val junctionFromRightDouble = listOf(
            Symbols.DOUBLE_LINE_HORIZONTAL,
            Symbols.DOUBLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS,
            Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER,
            Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER,
            Symbols.DOUBLE_LINE_T_UP,
            Symbols.DOUBLE_LINE_T_DOWN,
            Symbols.DOUBLE_LINE_T_LEFT,
            Symbols.DOUBLE_LINE_T_SINGLE_UP,
            Symbols.DOUBLE_LINE_T_SINGLE_DOWN,
            Symbols.SINGLE_LINE_T_DOUBLE_LEFT,
        )

        for (column in 1 until drawableArea.columns - 1) {
            var borderCharacter = graphics.getCharacter(column, upperRow)
            if (borderCharacter == null) {
                continue
            }
            var neighbourCharacter = graphics.getCharacter(column, upperRow + 1)
            if (neighbourCharacter != null) {
                val neighbour = neighbourCharacter.characterString[0]
                if (borderCharacter.`is`(Symbols.SINGLE_LINE_HORIZONTAL)) {
                    if (junctionFromBelowSingle.contains(neighbour)) {
                        graphics.setCharacter(column, upperRow, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOWN))
                    } else if (junctionFromBelowDouble.contains(neighbour)) {
                        graphics.setCharacter(column, upperRow, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_DOWN))
                    }
                } else if (borderCharacter.`is`(Symbols.DOUBLE_LINE_HORIZONTAL)) {
                    if (junctionFromBelowSingle.contains(neighbour)) {
                        graphics.setCharacter(column, upperRow, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_DOWN))
                    } else if (junctionFromBelowDouble.contains(neighbour)) {
                        graphics.setCharacter(column, upperRow, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_DOWN))
                    }
                }
            }

            borderCharacter = graphics.getCharacter(column, lowerRow)
            if (borderCharacter == null) {
                continue
            }
            neighbourCharacter = graphics.getCharacter(column, lowerRow - 1)
            if (neighbourCharacter != null) {
                val neighbour = neighbourCharacter.characterString[0]
                if (borderCharacter.`is`(Symbols.SINGLE_LINE_HORIZONTAL)) {
                    if (junctionFromAboveSingle.contains(neighbour)) {
                        graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_UP))
                    } else if (junctionFromAboveDouble.contains(neighbour)) {
                        graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_UP))
                    }
                } else if (borderCharacter.`is`(Symbols.DOUBLE_LINE_HORIZONTAL)) {
                    if (junctionFromAboveSingle.contains(neighbour)) {
                        graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_UP))
                    } else if (junctionFromAboveDouble.contains(neighbour)) {
                        graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_UP))
                    }
                }
            }
        }

        for (row in 1 until drawableArea.rows - 1) {
            var borderCharacter: TextCharacter? = graphics.getCharacter(leftRow, row)
            if (borderCharacter == null) {
                continue
            }
            var neighbourCharacter = graphics.getCharacter(leftRow + 1, row)
            if (neighbourCharacter != null) {
                val neighbour = neighbourCharacter.characterString[0]
                if (borderCharacter.`is`(Symbols.SINGLE_LINE_VERTICAL)) {
                    if (junctionFromRightSingle.contains(neighbour)) {
                        graphics.setCharacter(leftRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_RIGHT))
                    } else if (junctionFromRightDouble.contains(neighbour)) {
                        graphics.setCharacter(leftRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_RIGHT))
                    }
                } else if (borderCharacter.`is`(Symbols.DOUBLE_LINE_VERTICAL)) {
                    if (junctionFromRightSingle.contains(neighbour)) {
                        graphics.setCharacter(leftRow, row, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_RIGHT))
                    } else if (junctionFromRightDouble.contains(neighbour)) {
                        graphics.setCharacter(leftRow, row, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_RIGHT))
                    }
                }
            }

            borderCharacter = graphics.getCharacter(rightRow, row)
            if (borderCharacter == null) {
                continue
            }
            neighbourCharacter = graphics.getCharacter(rightRow - 1, row)
            if (neighbourCharacter != null) {
                val neighbour = neighbourCharacter.characterString[0]
                if (borderCharacter.`is`(Symbols.SINGLE_LINE_VERTICAL)) {
                    if (junctionFromLeftSingle.contains(neighbour)) {
                        graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_LEFT))
                    } else if (junctionFromLeftDouble.contains(neighbour)) {
                        graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_LEFT))
                    }
                } else if (borderCharacter.`is`(Symbols.DOUBLE_LINE_VERTICAL)) {
                    if (junctionFromLeftSingle.contains(neighbour)) {
                        graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_LEFT))
                    } else if (junctionFromLeftDouble.contains(neighbour)) {
                        graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_LEFT))
                    }
                }
            }
        }
    }

    private class SingleLine(title: String, borderStyle: BorderStyle) : StandardBorder(title, borderStyle) {
        override fun createDefaultRenderer(): Border.BorderRenderer {
            return SingleLineRenderer(borderStyle)
        }
    }

    private class SingleLineRenderer(borderStyle: BorderStyle) : AbstractBorderRenderer(borderStyle) {
        override fun getTopRightCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("TOP_RIGHT_CORNER", Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
                ?: Symbols.SINGLE_LINE_TOP_RIGHT_CORNER
        }

        override fun getBottomRightCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("BOTTOM_RIGHT_CORNER", Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
                ?: Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER
        }

        override fun getTopLeftCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("TOP_LEFT_CORNER", Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
                ?: Symbols.SINGLE_LINE_TOP_LEFT_CORNER
        }

        override fun getBottomLeftCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("BOTTOM_LEFT_CORNER", Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
                ?: Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER
        }

        override fun getVerticalLine(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("VERTICAL_LINE", Symbols.SINGLE_LINE_VERTICAL)
                ?: Symbols.SINGLE_LINE_VERTICAL
        }

        override fun getHorizontalLine(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("HORIZONTAL_LINE", Symbols.SINGLE_LINE_HORIZONTAL)
                ?: Symbols.SINGLE_LINE_HORIZONTAL
        }

        override fun getTitleLeft(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("TITLE_LEFT", Symbols.SINGLE_LINE_HORIZONTAL)
                ?: Symbols.SINGLE_LINE_HORIZONTAL
        }

        override fun getTitleRight(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)?.getCharacter("TITLE_RIGHT", Symbols.SINGLE_LINE_HORIZONTAL)
                ?: Symbols.SINGLE_LINE_HORIZONTAL
        }
    }

    private class DoubleLine(title: String, borderStyle: BorderStyle) : StandardBorder(title, borderStyle) {
        override fun createDefaultRenderer(): Border.BorderRenderer {
            return DoubleLineRenderer(borderStyle)
        }
    }

    private class DoubleLineRenderer(borderStyle: BorderStyle) : AbstractBorderRenderer(borderStyle) {
        override fun getTopRightCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("TOP_RIGHT_CORNER", Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER)
                ?: Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER
        }

        override fun getBottomRightCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("BOTTOM_RIGHT_CORNER", Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER)
                ?: Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER
        }

        override fun getTopLeftCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("TOP_LEFT_CORNER", Symbols.DOUBLE_LINE_TOP_LEFT_CORNER)
                ?: Symbols.DOUBLE_LINE_TOP_LEFT_CORNER
        }

        override fun getBottomLeftCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("BOTTOM_LEFT_CORNER", Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER)
                ?: Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER
        }

        override fun getVerticalLine(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("VERTICAL_LINE", Symbols.DOUBLE_LINE_VERTICAL)
                ?: Symbols.DOUBLE_LINE_VERTICAL
        }

        override fun getHorizontalLine(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("HORIZONTAL_LINE", Symbols.DOUBLE_LINE_HORIZONTAL)
                ?: Symbols.DOUBLE_LINE_HORIZONTAL
        }

        override fun getTitleLeft(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("TITLE_LEFT", Symbols.DOUBLE_LINE_HORIZONTAL)
                ?: Symbols.DOUBLE_LINE_HORIZONTAL
        }

        override fun getTitleRight(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)?.getCharacter("TITLE_RIGHT", Symbols.DOUBLE_LINE_HORIZONTAL)
                ?: Symbols.DOUBLE_LINE_HORIZONTAL
        }
    }
}
