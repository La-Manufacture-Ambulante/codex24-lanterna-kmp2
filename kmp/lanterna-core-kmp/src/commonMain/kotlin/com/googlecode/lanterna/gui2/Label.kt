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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextColor
import java.util.EnumSet

/**
 * Label is a simple read-only text display component. It supports customized colors and multi-line text.
 * @author Martin
 */
open class Label(text: String?) : AbstractComponent<Label?>() {
    private var lineBuffer: Array<String> = emptyArray()
    private var labelWidth: Int? = 0
    private var labelSize: TerminalSize? = TerminalSize.ZERO
    private var foregroundColor: TextColor? = null
    private var backgroundColor: TextColor? = null
    private val additionalStyles: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)

    init {
        setText(text!!)
    }

    /**
     * Protected access to set the internal representation of the text in this label, to be used by sub-classes of label
     * in certain cases where `setText(..)` doesn't work. In general, you probably want to stick to
     * `setText(..)` instead of this method unless you have a good reason not to.
     * @param lines New lines this label will display
     */
    protected fun setLines(lines: Array<String>) {
        this.lineBuffer = lines
    }

    @Synchronized
    fun setText(text: String) {
        setLines(splitIntoMultipleLines(text))
        this.labelSize = getBounds(lineBuffer, labelSize)
        invalidate()
    }

    @Synchronized
    fun getText(): String {
        if (lineBuffer.isEmpty()) {
            return ""
        }
        val bob = StringBuilder(lineBuffer[0])
        for (i in 1 until lineBuffer.size) {
            bob.append("\n").append(lineBuffer[i])
        }
        return bob.toString()
    }

    protected fun splitIntoMultipleLines(text: String): Array<String> {
        return text.replace("\r", "").split("\n").toTypedArray()
    }

    protected fun getBounds(
        lines: Array<String>,
        currentBounds: TerminalSize?,
    ): TerminalSize? {
        var bounds: TerminalSize? = currentBounds ?: TerminalSize.ZERO
        bounds = bounds?.withRows(lines.size)
        if (labelWidth == null || labelWidth == 0) {
            var preferredWidth = 0
            for (line in lines) {
                val lineWidth = TerminalTextUtils.getColumnWidth(line)
                if (preferredWidth < lineWidth) {
                    preferredWidth = lineWidth
                }
            }
            bounds = bounds!!.withColumns(preferredWidth)
        } else {
            val width = labelWidth!!
            val wordWrapped = TerminalTextUtils.getWordWrappedText(width, *lines)
            bounds = bounds!!.withColumns(width)!!.withRows(wordWrapped.size)
        }
        return bounds
    }

    @Synchronized
    fun setForegroundColor(foregroundColor: TextColor?): Label {
        this.foregroundColor = foregroundColor
        return this
    }

    fun getForegroundColor(): TextColor? {
        return foregroundColor
    }

    @Synchronized
    fun setBackgroundColor(backgroundColor: TextColor?): Label {
        this.backgroundColor = backgroundColor
        return this
    }

    fun getBackgroundColor(): TextColor? {
        return backgroundColor
    }

    @Synchronized
    fun addStyle(sgr: SGR): Label {
        additionalStyles.add(sgr)
        return this
    }

    @Synchronized
    fun removeStyle(sgr: SGR): Label {
        additionalStyles.remove(sgr)
        return this
    }

    @Synchronized
    fun setLabelWidth(labelWidth: Int?): Label {
        this.labelWidth = labelWidth
        return this
    }

    fun getLabelWidth(): Int? {
        return labelWidth
    }

    override fun createDefaultRenderer(): ComponentRenderer<Label?>? {
        return object : ComponentRenderer<Label?> {
            override fun getPreferredSize(component: Label?): TerminalSize? {
                return labelSize
            }

            override fun drawComponent(
                graphics: TextGUIGraphics?,
                component: Label?,
            ) {
                val themeDefinition = component!!.themeDefinition!!
                graphics!!.applyThemeStyle(themeDefinition.normal)
                if (foregroundColor != null) {
                    graphics.setForegroundColor(foregroundColor)
                }
                if (backgroundColor != null) {
                    graphics.setBackgroundColor(backgroundColor)
                }
                for (sgr in additionalStyles) {
                    graphics.enableModifiers(sgr)
                }

                val linesToDraw: Array<String> =
                    if (component.getLabelWidth() == null) {
                        component.lineBuffer
                    } else {
                        TerminalTextUtils.getWordWrappedText(graphics.size!!.columns, *component.lineBuffer)
                            .map { it ?: "" }
                            .toTypedArray()
                    }

                for (row in 0 until kotlin.math.min(graphics.size!!.rows, linesToDraw.size)) {
                    val line = linesToDraw[row]
                    if (graphics.size!!.columns >= labelSize!!.columns) {
                        graphics.putString(0, row, line)
                    } else {
                        val availableColumns = graphics.size!!.columns
                        val fitString = TerminalTextUtils.fitString(line, availableColumns)
                        graphics.putString(0, row, fitString)
                    }
                }
            }
        }
    }
}
