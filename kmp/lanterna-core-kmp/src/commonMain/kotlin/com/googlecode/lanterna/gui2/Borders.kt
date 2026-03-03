package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.Component
import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition
import java.util.Arrays

class Borders private constructor() {

    private enum class BorderStyle {
        Solid,
        Bevel,
        ReverseBevel
    }

    private abstract class StandardBorder protected constructor(title: String?, protected val borderStyle: BorderStyle) :
        AbstractBorder() {
        private val title: String

        init {
            if (title == null) {
                throw IllegalArgumentException("Cannot create a border with null title")
            }
            this.title = title
        }

        fun getTitle(): String {
            return title
        }

        override fun toString(): String {
            return javaClass.simpleName + "{" + title + "}"
        }
    }

    private abstract class AbstractBorderRenderer protected constructor(private val borderStyle: BorderStyle) :
        Border.BorderRenderer {
        override fun getPreferredSize(component: Border): TerminalSize {
            val border = component as StandardBorder
            val wrappedComponent: Component? = border.component
            var preferredSize: TerminalSize = if (wrappedComponent == null) {
                TerminalSize.ZERO
            } else {
                wrappedComponent.preferredSize
            }
            preferredSize = preferredSize.withRelativeColumns(2).withRelativeRows(2)
            val borderTitle = border.getTitle()
            return preferredSize.max(
                TerminalSize(
                    if (borderTitle.isEmpty()) 2 else TerminalTextUtils.getColumnWidth(borderTitle) + 4,
                    2
                )
            )
        }

        override fun getWrappedComponentTopLeftOffset(): TerminalPosition {
            return TerminalPosition.OFFSET_1x1
        }

        override fun getWrappedComponentSize(borderSize: TerminalSize): TerminalSize {
            return borderSize
                .withRelativeColumns(-Math.min(2, borderSize.columns))
                .withRelativeRows(-Math.min(2, borderSize.rows))
        }

        override fun drawComponent(graphics: TextGUIGraphics, component: Border) {
            val border = component as StandardBorder
            val wrappedComponent: Component? = border.component
            if (wrappedComponent == null) {
                return
            }
            val drawableArea = graphics.size

            val horizontalLine = getHorizontalLine(component.theme)
            val verticalLine = getVerticalLine(component.theme)
            val bottomLeftCorner = getBottomLeftCorner(component.theme)
            val topLeftCorner = getTopLeftCorner(component.theme)
            val bottomRightCorner = getBottomRightCorner(component.theme)
            val topRightCorner = getTopRightCorner(component.theme)
            val titleLeft = getTitleLeft(component.theme)
            val titleRight = getTitleRight(component.theme)

            val themeDefinition: ThemeDefinition = component.theme.getDefinition(AbstractBorder::class.java)
            if (borderStyle == BorderStyle.Bevel) {
                graphics.applyThemeStyle(themeDefinition.preLight)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            graphics.setCharacter(0, drawableArea.rows - 1, bottomLeftCorner)
            if (drawableArea.rows > 2) {
                graphics.drawLine(TerminalPosition(0, drawableArea.rows - 2), TerminalPosition(0, 1), verticalLine)
            }
            graphics.setCharacter(0, 0, topLeftCorner)
            if (drawableArea.columns > 2) {
                graphics.drawLine(TerminalPosition(1, 0), TerminalPosition(drawableArea.columns - 2, 0), horizontalLine)
            }

            if (borderStyle == BorderStyle.ReverseBevel) {
                graphics.applyThemeStyle(themeDefinition.preLight)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            graphics.setCharacter(drawableArea.columns - 1, 0, topRightCorner)
            if (drawableArea.rows > 2) {
                graphics.drawLine(
                    TerminalPosition(drawableArea.columns - 1, 1),
                    TerminalPosition(drawableArea.columns - 1, drawableArea.rows - 2),
                    verticalLine
                )
            }
            graphics.setCharacter(drawableArea.columns - 1, drawableArea.rows - 1, bottomRightCorner)
            if (drawableArea.columns > 2) {
                graphics.drawLine(
                    TerminalPosition(1, drawableArea.rows - 1),
                    TerminalPosition(drawableArea.columns - 2, drawableArea.rows - 1),
                    horizontalLine
                )
            }

            if (border.getTitle().isNotEmpty() &&
                drawableArea.columns >= TerminalTextUtils.getColumnWidth(border.getTitle()) + 4
            ) {
                graphics.applyThemeStyle(themeDefinition.active)
                graphics.putString(2, 0, border.getTitle())

                if (borderStyle == BorderStyle.Bevel) {
                    graphics.applyThemeStyle(themeDefinition.preLight)
                } else {
                    graphics.applyThemeStyle(themeDefinition.normal)
                }
                graphics.setCharacter(1, 0, titleLeft)
                graphics.setCharacter(2 + TerminalTextUtils.getColumnWidth(border.getTitle()), 0, titleRight)
            }

            wrappedComponent.draw(graphics.newTextGraphics(getWrappedComponentTopLeftOffset(), getWrappedComponentSize(drawableArea)))
            joinLinesWithFrame(graphics)
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

    private class SingleLine private constructor(title: String?, borderStyle: BorderStyle) :
        StandardBorder(title, borderStyle) {
        override fun createDefaultRenderer(): Border.BorderRenderer {
            return SingleLineRenderer(borderStyle)
        }
    }

    private class SingleLineRenderer(borderStyle: BorderStyle) : AbstractBorderRenderer(borderStyle) {
        override fun getTopRightCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("TOP_RIGHT_CORNER", Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
        }

        override fun getBottomRightCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("BOTTOM_RIGHT_CORNER", Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
        }

        override fun getTopLeftCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("TOP_LEFT_CORNER", Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
        }

        override fun getBottomLeftCorner(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("BOTTOM_LEFT_CORNER", Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
        }

        override fun getVerticalLine(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("VERTICAL_LINE", Symbols.SINGLE_LINE_VERTICAL)
        }

        override fun getHorizontalLine(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("HORIZONTAL_LINE", Symbols.SINGLE_LINE_HORIZONTAL)
        }

        override fun getTitleLeft(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("TITLE_LEFT", Symbols.SINGLE_LINE_HORIZONTAL)
        }

        override fun getTitleRight(theme: Theme): Char {
            return theme.getDefinition(SingleLine::class.java)
                .getCharacter("TITLE_RIGHT", Symbols.SINGLE_LINE_HORIZONTAL)
        }
    }

    private class DoubleLine private constructor(title: String?, borderStyle: BorderStyle) :
        StandardBorder(title, borderStyle) {
        override fun createDefaultRenderer(): Border.BorderRenderer {
            return DoubleLineRenderer(borderStyle)
        }
    }

    private class DoubleLineRenderer(borderStyle: BorderStyle) : AbstractBorderRenderer(borderStyle) {
        override fun getTopRightCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("TOP_RIGHT_CORNER", Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER)
        }

        override fun getBottomRightCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("BOTTOM_RIGHT_CORNER", Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER)
        }

        override fun getTopLeftCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("TOP_LEFT_CORNER", Symbols.DOUBLE_LINE_TOP_LEFT_CORNER)
        }

        override fun getBottomLeftCorner(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("BOTTOM_LEFT_CORNER", Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER)
        }

        override fun getVerticalLine(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("VERTICAL_LINE", Symbols.DOUBLE_LINE_VERTICAL)
        }

        override fun getHorizontalLine(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("HORIZONTAL_LINE", Symbols.DOUBLE_LINE_HORIZONTAL)
        }

        override fun getTitleLeft(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("TITLE_LEFT", Symbols.DOUBLE_LINE_HORIZONTAL)
        }

        override fun getTitleRight(theme: Theme): Char {
            return theme.getDefinition(DoubleLine::class.java)
                .getCharacter("TITLE_RIGHT", Symbols.DOUBLE_LINE_HORIZONTAL)
        }
    }

    companion object {
        @JvmStatic
        fun singleLine(): Border {
            return singleLine("")
        }

        @JvmStatic
        fun singleLine(title: String?): Border {
            return SingleLine(title, BorderStyle.Solid)
        }

        @JvmStatic
        fun singleLineBevel(): Border {
            return singleLineBevel("")
        }

        @JvmStatic
        fun singleLineBevel(title: String?): Border {
            return SingleLine(title, BorderStyle.Bevel)
        }

        @JvmStatic
        fun singleLineReverseBevel(): Border {
            return singleLineReverseBevel("")
        }

        @JvmStatic
        fun singleLineReverseBevel(title: String?): Border {
            return SingleLine(title, BorderStyle.ReverseBevel)
        }

        @JvmStatic
        fun doubleLine(): Border {
            return doubleLine("")
        }

        @JvmStatic
        fun doubleLine(title: String?): Border {
            return DoubleLine(title, BorderStyle.Solid)
        }

        @JvmStatic
        fun doubleLineBevel(): Border {
            return doubleLineBevel("")
        }

        @JvmStatic
        fun doubleLineBevel(title: String?): Border {
            return DoubleLine(title, BorderStyle.Bevel)
        }

        @JvmStatic
        fun doubleLineReverseBevel(): Border {
            return doubleLineReverseBevel("")
        }

        @JvmStatic
        fun doubleLineReverseBevel(title: String?): Border {
            return DoubleLine(title, BorderStyle.ReverseBevel)
        }

        @JvmStatic
        fun joinLinesWithFrame(graphics: TextGraphics) {
            val drawableArea = graphics.size
            if (drawableArea.rows <= 2 || drawableArea.columns <= 2) {
                return
            }

            val upperRow = 0
            val lowerRow = drawableArea.rows - 1
            val leftRow = 0
            val rightRow = drawableArea.columns - 1

            val junctionFromBelowSingle: List<Char> = Arrays.asList(
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
                Symbols.DOUBLE_LINE_T_SINGLE_UP
            )
            val junctionFromBelowDouble: List<Char> = Arrays.asList(
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
                Symbols.SINGLE_LINE_T_DOUBLE_UP
            )
            val junctionFromAboveSingle: List<Char> = Arrays.asList(
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
                Symbols.DOUBLE_LINE_T_SINGLE_DOWN
            )
            val junctionFromAboveDouble: List<Char> = Arrays.asList(
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
                Symbols.SINGLE_LINE_T_DOUBLE_DOWN
            )
            val junctionFromLeftSingle: List<Char> = Arrays.asList(
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
                Symbols.DOUBLE_LINE_T_SINGLE_RIGHT
            )
            val junctionFromLeftDouble: List<Char> = Arrays.asList(
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
                Symbols.SINGLE_LINE_T_DOUBLE_RIGHT
            )
            val junctionFromRightSingle: List<Char> = Arrays.asList(
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
                Symbols.DOUBLE_LINE_T_SINGLE_LEFT
            )
            val junctionFromRightDouble: List<Char> = Arrays.asList(
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
                Symbols.SINGLE_LINE_T_DOUBLE_LEFT
            )

            for (column in 1 until (drawableArea.columns - 1)) {
                var borderCharacter: TextCharacter? = graphics.getCharacter(column, upperRow)
                if (borderCharacter == null) {
                    continue
                }
                var neighbourCharacter: TextCharacter? = graphics.getCharacter(column, upperRow + 1)
                if (neighbourCharacter != null) {
                    val neighbour = neighbourCharacter.characterString[0]
                    if (borderCharacter.isCharacter(Symbols.SINGLE_LINE_HORIZONTAL)) {
                        if (junctionFromBelowSingle.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                upperRow,
                                borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOWN)
                            )
                        } else if (junctionFromBelowDouble.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                upperRow,
                                borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_DOWN)
                            )
                        }
                    } else if (borderCharacter.isCharacter(Symbols.DOUBLE_LINE_HORIZONTAL)) {
                        if (junctionFromBelowSingle.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                upperRow,
                                borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_DOWN)
                            )
                        } else if (junctionFromBelowDouble.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                upperRow,
                                borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_DOWN)
                            )
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
                    if (borderCharacter.isCharacter(Symbols.SINGLE_LINE_HORIZONTAL)) {
                        if (junctionFromAboveSingle.contains(neighbour)) {
                            graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_UP))
                        } else if (junctionFromAboveDouble.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                lowerRow,
                                borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_UP)
                            )
                        }
                    } else if (borderCharacter.isCharacter(Symbols.DOUBLE_LINE_HORIZONTAL)) {
                        if (junctionFromAboveSingle.contains(neighbour)) {
                            graphics.setCharacter(
                                column,
                                lowerRow,
                                borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_UP)
                            )
                        } else if (junctionFromAboveDouble.contains(neighbour)) {
                            graphics.setCharacter(column, lowerRow, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_UP))
                        }
                    }
                }
            }

            for (row in 1 until (drawableArea.rows - 1)) {
                var borderCharacter: TextCharacter? = graphics.getCharacter(leftRow, row)
                if (borderCharacter == null) {
                    continue
                }
                var neighbourCharacter: TextCharacter? = graphics.getCharacter(leftRow + 1, row)
                if (neighbourCharacter != null) {
                    val neighbour = neighbourCharacter.characterString[0]
                    if (borderCharacter.isCharacter(Symbols.SINGLE_LINE_VERTICAL)) {
                        if (junctionFromRightSingle.contains(neighbour)) {
                            graphics.setCharacter(leftRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_RIGHT))
                        } else if (junctionFromRightDouble.contains(neighbour)) {
                            graphics.setCharacter(
                                leftRow,
                                row,
                                borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_RIGHT)
                            )
                        }
                    } else if (borderCharacter.isCharacter(Symbols.DOUBLE_LINE_VERTICAL)) {
                        if (junctionFromRightSingle.contains(neighbour)) {
                            graphics.setCharacter(
                                leftRow,
                                row,
                                borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_RIGHT)
                            )
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
                    if (borderCharacter.isCharacter(Symbols.SINGLE_LINE_VERTICAL)) {
                        if (junctionFromLeftSingle.contains(neighbour)) {
                            graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_LEFT))
                        } else if (junctionFromLeftDouble.contains(neighbour)) {
                            graphics.setCharacter(
                                rightRow,
                                row,
                                borderCharacter.withCharacter(Symbols.SINGLE_LINE_T_DOUBLE_LEFT)
                            )
                        }
                    } else if (borderCharacter.isCharacter(Symbols.DOUBLE_LINE_VERTICAL)) {
                        if (junctionFromLeftSingle.contains(neighbour)) {
                            graphics.setCharacter(
                                rightRow,
                                row,
                                borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_SINGLE_LEFT)
                            )
                        } else if (junctionFromLeftDouble.contains(neighbour)) {
                            graphics.setCharacter(rightRow, row, borderCharacter.withCharacter(Symbols.DOUBLE_LINE_T_LEFT))
                        }
                    }
                }
            }
        }
    }
}
