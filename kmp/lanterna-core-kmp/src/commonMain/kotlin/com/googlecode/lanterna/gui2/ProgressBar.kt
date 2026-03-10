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
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition

class ProgressBar constructor(min: Int = 0, max: Int = 100, preferredWidth: Int = 0) :
    AbstractComponent<ProgressBar?>() {
    private var min: Int
    private var max: Int
    private var value: Int
    var preferredWidth: Int
    private var labelFormat: String? = "%2.0f%%"

    val progress: Float
        get() = (value - min).toFloat() / max.toFloat()

    val formattedLabel: String
        get() {
            val format = labelFormat ?: return ""
            return formatPercent(format, progress * 100.0f)
        }

    init {
        var adjustedMin = min
        var adjustedPreferredWidth = preferredWidth
        var adjustedMax = max
        if (adjustedMin > adjustedMax) {
            adjustedMin = adjustedMax
        }
        if (adjustedPreferredWidth < 1) {
            adjustedPreferredWidth = 1
        }
        this.min = adjustedMin
        this.max = adjustedMax
        this.value = adjustedMin
        this.preferredWidth = adjustedPreferredWidth
    }

    fun getMin(): Int = min

    fun setMin(min: Int): ProgressBar {
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

    fun getMax(): Int = max

    fun setMax(max: Int): ProgressBar {
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

    fun getValue(): Int = value

    fun setValue(value: Int): ProgressBar {
        var adjustedValue = value
        if (adjustedValue < min) {
            adjustedValue = min
        }
        if (adjustedValue > max) {
            adjustedValue = max
        }
        if (this.value != adjustedValue) {
            this.value = adjustedValue
            invalidate()
        }
        return this
    }

    fun getLabelFormat(): String? = labelFormat

    fun setLabelFormat(labelFormat: String?): ProgressBar {
        this.labelFormat = labelFormat
        invalidate()
        return this
    }

    override fun createDefaultRenderer(): ComponentRenderer<ProgressBar?> {
        return DefaultProgressBarRenderer()
    }

    class DefaultProgressBarRenderer : ComponentRenderer<ProgressBar?> {
        override fun getPreferredSize(component: ProgressBar?): TerminalSize? {
            val progressBar = component ?: return TerminalSize(10, 1)
            val preferredWidth = progressBar.preferredWidth
            if (preferredWidth > 0) {
                return TerminalSize(preferredWidth, 1)
            }

            val labelFormat = progressBar.getLabelFormat()
            return if (labelFormat != null && labelFormat.trim().isNotEmpty()) {
                TerminalSize(TerminalTextUtils.getColumnWidth(formatPercent(labelFormat, 100.0f)) + 2, 1)
            } else {
                TerminalSize(10, 1)
            }
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: ProgressBar?) {
            val g = graphics ?: return
            val progressBar = component ?: return
            val size = g.size ?: return
            if (size.rows == 0 || size.columns == 0) {
                return
            }

            val themeDefinition = progressBar.themeDefinition ?: return
            val columnOfProgress = (progressBar.progress * size.columns).toInt()
            var label = progressBar.formattedLabel
            val labelRow = size.rows / 2
            var labelWidth = TerminalTextUtils.getColumnWidth(label)

            if (labelWidth > size.columns) {
                var trimTail = true
                while (labelWidth > size.columns && label.isNotEmpty()) {
                    label = if (trimTail) {
                        label.substring(0, label.length - 1)
                    } else {
                        label.substring(1)
                    }
                    trimTail = !trimTail
                    labelWidth = TerminalTextUtils.getColumnWidth(label)
                }
            }

            val labelStartPosition = (size.columns - labelWidth) / 2
            for (row in 0 until size.rows) {
                g.applyThemeStyle(themeDefinition.active)
                var column = 0
                while (column < size.columns) {
                    if (column == columnOfProgress) {
                        g.applyThemeStyle(themeDefinition.normal)
                    }
                    if (row == labelRow && column >= labelStartPosition && column < labelStartPosition + labelWidth) {
                        val character = label[TerminalTextUtils.getStringCharacterIndex(label, column - labelStartPosition)]
                        g.setCharacter(column, row, character)
                        if (TerminalTextUtils.isCharDoubleWidth(character)) {
                            column++
                            if (column == columnOfProgress) {
                                g.applyThemeStyle(themeDefinition.normal)
                            }
                        }
                    } else {
                        g.setCharacter(column, row, themeDefinition.getCharacter("FILLER", ' '))
                    }
                    column++
                }
            }
        }
    }

    class LargeProgressBarRenderer : ComponentRenderer<ProgressBar?> {
        override fun getPreferredSize(component: ProgressBar?): TerminalSize? {
            val progressBar = component ?: return TerminalSize(42, 3)
            val preferredWidth = progressBar.preferredWidth
            return if (preferredWidth > 0) {
                TerminalSize(preferredWidth, 3)
            } else {
                TerminalSize(42, 3)
            }
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: ProgressBar?) {
            val g = graphics ?: return
            val progressBar = component ?: return
            val size = g.size ?: return
            if (size.rows == 0 || size.columns == 0) {
                return
            }

            val themeDefinition: ThemeDefinition = progressBar.themeDefinition ?: return
            val columnOfProgress = (progressBar.progress * (size.columns - 4)).toInt()
            var mark25 = -1
            var mark50 = -1
            var mark75 = -1

            if (size.columns > 9) {
                mark50 = (size.columns - 2) / 2
            }
            if (size.columns > 16) {
                mark25 = (size.columns - 2) / 4
                mark75 = mark50 + mark25
            }

            var rowOffset = 0
            if (size.rows >= 3) {
                g.applyThemeStyle(themeDefinition.normal)
                g.drawLine(0, 0, size.columns, 0, ' ')
                if (size.columns > 1) {
                    g.setCharacter(1, 0, '0')
                }
                if (mark25 != -1) {
                    if (progressBar.progress < 0.25f) {
                        g.applyThemeStyle(themeDefinition.insensitive)
                    }
                    g.putString(1 + mark25, 0, "25")
                }
                if (mark50 != -1) {
                    if (progressBar.progress < 0.50f) {
                        g.applyThemeStyle(themeDefinition.insensitive)
                    }
                    g.putString(1 + mark50, 0, "50")
                }
                if (mark75 != -1) {
                    if (progressBar.progress < 0.75f) {
                        g.applyThemeStyle(themeDefinition.insensitive)
                    }
                    g.putString(1 + mark75, 0, "75")
                }
                if (size.columns >= 7) {
                    if (progressBar.progress < 1.0f) {
                        g.applyThemeStyle(themeDefinition.insensitive)
                    }
                    g.putString(size.columns - 3, 0, "100")
                }
                rowOffset++
            }

            for (i in 0 until maxOf(1, size.rows - 2)) {
                g.applyThemeStyle(themeDefinition.normal)
                g.drawLine(0, rowOffset, size.columns, rowOffset, ' ')
                if (size.columns > 2) {
                    g.setCharacter(1, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                }
                if (size.columns > 3) {
                    g.setCharacter(size.columns - 2, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                }
                if (size.columns > 4) {
                    g.applyThemeStyle(themeDefinition.active)
                    for (columnOffset in 2 until size.columns - 2) {
                        if (columnOfProgress + 2 == columnOffset) {
                            g.applyThemeStyle(themeDefinition.normal)
                        }
                        when (columnOffset - 1) {
                            mark25, mark50, mark75 -> g.setCharacter(columnOffset, rowOffset, Symbols.SINGLE_LINE_VERTICAL)
                            else -> g.setCharacter(columnOffset, rowOffset, ' ')
                        }
                    }
                }

                if (((progressBar.progress * ((size.columns - 4) * 2)).toInt()) % 2 == 1) {
                    g.applyThemeStyle(themeDefinition.preLight)
                    g.setCharacter(columnOfProgress + 2, rowOffset, '|')
                }
                rowOffset++
            }

            if (size.rows >= 2) {
                g.applyThemeStyle(themeDefinition.normal)
                g.drawLine(0, rowOffset, size.columns, rowOffset, Symbols.SINGLE_LINE_T_UP)
                g.setCharacter(0, rowOffset, ' ')
                if (size.columns > 1) {
                    g.setCharacter(size.columns - 1, rowOffset, ' ')
                }
                if (size.columns > 2) {
                    g.setCharacter(1, rowOffset, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
                }
                if (size.columns > 3) {
                    g.setCharacter(size.columns - 2, rowOffset, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
                }
            }
        }
    }

    companion object {
        private fun formatPercent(format: String, value: Float): String {
            return if (format.contains("f")) {
                "${value.toInt()}%"
            } else {
                format.replace("%s", value.toString())
            }
        }
    }
}
