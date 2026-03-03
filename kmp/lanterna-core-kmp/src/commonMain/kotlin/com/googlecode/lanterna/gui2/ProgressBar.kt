package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition

open class ProgressBar : AbstractComponent<ProgressBar>() {
    private var min: Int
    private var max: Int
    private var value: Int
    private var preferredWidth: Int
    private var labelFormat: String?

    constructor() : this(0, 100)

    constructor(min: Int, max: Int) : this(min, max, 0)

    constructor(min: Int, max: Int, preferredWidth: Int) {
        var minVar = min
        var preferredWidthVar = preferredWidth
        if (minVar > max) {
            minVar = max
        }
        this.min = minVar
        this.max = max
        this.value = minVar
        this.labelFormat = "%2.0f%%"

        if (preferredWidthVar < 1) {
            preferredWidthVar = 1
        }
        this.preferredWidth = preferredWidthVar
    }

    open fun getMin(): Int {
        return min
    }

    @Synchronized
    open fun setMin(min: Int): ProgressBar {
        if (min > max) {
            setMax(min)
        }
        if (min > value) {
            setValue(min)
        }
        if (this.min != min) {
            this.min = min
            invalidate()
        }
        return this
    }

    open fun getMax(): Int {
        return max
    }

    @Synchronized
    open fun setMax(max: Int): ProgressBar {
        if (max < min) {
            setMin(max)
        }
        if (max < value) {
            setValue(max)
        }
        if (this.max != max) {
            this.max = max
            invalidate()
        }
        return this
    }

    open fun getValue(): Int {
        return value
    }

    @Synchronized
    open fun setValue(value: Int): ProgressBar {
        var valueVar = value
        if (valueVar < min) {
            valueVar = min
        }
        if (valueVar > max) {
            valueVar = max
        }
        if (this.value != valueVar) {
            this.value = valueVar
            invalidate()
        }
        return this
    }

    open fun getPreferredWidth(): Int {
        return preferredWidth
    }

    open fun setPreferredWidth(preferredWidth: Int) {
        this.preferredWidth = preferredWidth
    }

    open fun getLabelFormat(): String? {
        return labelFormat
    }

    @Synchronized
    open fun setLabelFormat(labelFormat: String?): ProgressBar {
        this.labelFormat = labelFormat
        invalidate()
        return this
    }

    @Synchronized
    open fun getProgress(): Float {
        return (value - min).toFloat() / max.toFloat()
    }

    @Synchronized
    open fun getFormattedLabel(): String {
        val format = labelFormat ?: return ""
        return String.format(format, getProgress() * 100.0f)
    }

    protected override fun createDefaultRenderer(): ComponentRenderer<ProgressBar> {
        return DefaultProgressBarRenderer()
    }

    open class DefaultProgressBarRenderer : ComponentRenderer<ProgressBar> {
        override fun getPreferredSize(component: ProgressBar): TerminalSize {
            val preferredWidth = component.getPreferredWidth()
            if (preferredWidth > 0) {
                return TerminalSize(preferredWidth, 1)
            }

            val labelFormat = component.getLabelFormat()
            return if (labelFormat != null && !labelFormat.trim().isEmpty()) {
                TerminalSize(
                    TerminalTextUtils.getColumnWidth(String.format(labelFormat, 100.0f)) + 2,
                    1
                )
            } else {
                TerminalSize(10, 1)
            }
        }

        override fun drawComponent(graphics: TextGUIGraphics, component: ProgressBar) {
            val size = graphics.getSize()
            if (size.getRows() == 0 || size.getColumns() == 0) {
                return
            }
            val themeDefinition: ThemeDefinition = component.getThemeDefinition()
            val columnOfProgress = (component.getProgress() * size.getColumns()).toInt()
            var label = component.getFormattedLabel()
            val labelRow = size.getRows() / 2

            var labelWidth = TerminalTextUtils.getColumnWidth(label)
            if (labelWidth > size.getColumns()) {
                var tail = true
                while (labelWidth > size.getColumns()) {
                    label = if (tail) {
                        label.substring(0, label.length - 1)
                    } else {
                        label.substring(1)
                    }
                    tail = !tail
                    labelWidth = TerminalTextUtils.getColumnWidth(label)
                }
            }
            val labelStartPosition = (size.getColumns() - labelWidth) / 2

            for (row in 0 until size.getRows()) {
                graphics.applyThemeStyle(themeDefinition.getActive())
                var column = 0
                while (column < size.getColumns()) {
                    if (column == columnOfProgress) {
                        graphics.applyThemeStyle(themeDefinition.getNormal())
                    }
                    if (row == labelRow && column >= labelStartPosition && column < labelStartPosition + labelWidth) {
                        val character = label[TerminalTextUtils.getStringCharacterIndex(label, column - labelStartPosition)]
                        graphics.setCharacter(column, row, character)
                        if (TerminalTextUtils.isCharDoubleWidth(character)) {
                            column++
                            if (column == columnOfProgress) {
                                graphics.applyThemeStyle(themeDefinition.getNormal())
                            }
                        }
                    } else {
                        graphics.setCharacter(column, row, themeDefinition.getCharacter("FILLER", ' '))
                    }
                    column++
                }
            }
        }
    }

    open class LargeProgressBarRenderer : ComponentRenderer<ProgressBar> {
        override fun getPreferredSize(component: ProgressBar): TerminalSize {
            val preferredWidth = component.getPreferredWidth()
            return if (preferredWidth > 0) {
                TerminalSize(preferredWidth, 3)
            } else {
                TerminalSize(42, 3)
            }
        }

        override fun drawComponent(graphics: TextGUIGraphics, component: ProgressBar) {
            val size = graphics.getSize()
            if (size.getRows() == 0 || size.getColumns() == 0) {
                return
            }
            val themeDefinition: ThemeDefinition = component.getThemeDefinition()
            val columnOfProgress = (component.getProgress() * (size.getColumns() - 4)).toInt()
            var mark25 = -1
            var mark50 = -1
            var mark75 = -1

            if (size.getColumns() > 9) {
                mark50 = (size.getColumns() - 2) / 2
            }
            if (size.getColumns() > 16) {
                mark25 = (size.getColumns() - 2) / 4
                mark75 = mark50 + mark25
            }

            var rowOffset = 0
            if (size.getRows() >= 3) {
                graphics.applyThemeStyle(themeDefinition.getNormal())
                graphics.drawLine(0, 0, size.getColumns(), 0, ' ')
                if (size.getColumns() > 1) {
                    graphics.setCharacter(1, 0, '0')
                }
                if (mark25 != -1) {
                    if (component.getProgress() < 0.25f) {
                        graphics.applyThemeStyle(themeDefinition.getInsensitive())
                    }
                    graphics.putString(1 + mark25, 0, "25")
                }
                if (mark50 != -1) {
                    if (component.getProgress() < 0.50f) {
                        graphics.applyThemeStyle(themeDefinition.getInsensitive())
                    }
                    graphics.putString(1 + mark50, 0, "50")
                }
                if (mark75 != -1) {
                    if (component.getProgress() < 0.75f) {
                        graphics.applyThemeStyle(themeDefinition.getInsensitive())
                    }
                    graphics.putString(1 + mark75, 0, "75")
                }
                if (size.getColumns() >= 7) {
                    if (component.getProgress() < 1.0f) {
                        graphics.applyThemeStyle(themeDefinition.getInsensitive())
                    }
                    graphics.putString(size.getColumns() - 3, 0, "100")
                }
                rowOffset++
            }

            for (i in 0 until Math.max(1, size.getRows() - 2)) {
                graphics.applyThemeStyle(themeDefinition.getNormal())
                graphics.drawLine(0, rowOffset, size.getColumns(), rowOffset, ' ')
                if (size.getColumns() > 2) {
                    graphics.setCharacter(1, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                }
                if (size.getColumns() > 3) {
                    graphics.setCharacter(size.getColumns() - 2, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                }
                if (size.getColumns() > 4) {
                    graphics.applyThemeStyle(themeDefinition.getActive())
                    for (columnOffset in 2 until size.getColumns() - 2) {
                        if (columnOfProgress + 2 == columnOffset) {
                            graphics.applyThemeStyle(themeDefinition.getNormal())
                        }
                        if (mark25 == columnOffset - 1) {
                            graphics.setCharacter(columnOffset, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                        } else if (mark50 == columnOffset - 1) {
                            graphics.setCharacter(columnOffset, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                        } else if (mark75 == columnOffset - 1) {
                            graphics.setCharacter(columnOffset, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                        } else {
                            graphics.setCharacter(columnOffset, rowOffset, ' ')
                        }
                    }
                }

                if (((component.getProgress() * ((size.getColumns() - 4) * 2)).toInt()) % 2 == 1) {
                    graphics.applyThemeStyle(themeDefinition.getPreLight())
                    graphics.setCharacter(columnOfProgress + 2, rowOffset, '|')
                }

                rowOffset++
            }

            if (size.getRows() >= 2) {
                graphics.applyThemeStyle(themeDefinition.getNormal())
                graphics.drawLine(0, rowOffset, size.getColumns(), rowOffset, Symbols.SINGLE_LINE_T_UP)
                graphics.setCharacter(0, rowOffset, ' ')
                if (size.getColumns() > 1) {
                    graphics.setCharacter(size.getColumns() - 1, rowOffset, ' ')
                }
                if (size.getColumns() > 2) {
                    graphics.setCharacter(1, rowOffset, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
                }
                if (size.getColumns() > 3) {
                    graphics.setCharacter(size.getColumns() - 2, rowOffset, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
                }
            }
        }
    }
}
