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
package com.googlecode.lanterna.gui2.table

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.gui2.TextGUIGraphics

/**
 * Default implementation of [TableCellRenderer].
 */
open class DefaultTableCellRenderer<V> : TableCellRenderer<V?> {
    override fun getPreferredSize(
        table: Table<V?>?,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
    ): TerminalSize {
        val lines = getContent(cell)
        var maxWidth = 0
        for (line in lines) {
            val length = TerminalTextUtils.getColumnWidth(line)
            if (maxWidth < length) {
                maxWidth = length
            }
        }
        return TerminalSize(maxWidth, lines.size)
    }

    override fun drawCell(
        table: Table<V?>?,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
        textGUIGraphics: TextGUIGraphics?,
    ) {
        val activeTable = table ?: return
        val activeGraphics = textGUIGraphics ?: return
        val isSelected =
            (activeTable.getSelectedColumn() == columnIndex && activeTable.getSelectedRow() == rowIndex) ||
                (activeTable.getSelectedRow() == rowIndex && !activeTable.isCellSelection())
        applyStyle(activeTable, cell, columnIndex, rowIndex, isSelected, activeGraphics)
        beforeRender(activeTable, cell, columnIndex, rowIndex, isSelected, activeGraphics)
        render(activeTable, cell, columnIndex, rowIndex, isSelected, activeGraphics)
        afterRender(activeTable, cell, columnIndex, rowIndex, isSelected, activeGraphics)
    }

    protected open fun applyStyle(
        table: Table<V?>,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
        isSelected: Boolean,
        textGUIGraphics: TextGUIGraphics,
    ) {
        val themeDefinition = table.themeDefinition ?: return
        if (isSelected) {
            if (table.isFocused) {
                textGUIGraphics.applyThemeStyle(themeDefinition.active)
            } else {
                textGUIGraphics.applyThemeStyle(themeDefinition.selected)
            }
        } else {
            textGUIGraphics.applyThemeStyle(themeDefinition.normal)
        }
    }

    protected open fun beforeRender(
        table: Table<V?>?,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
        isSelected: Boolean,
        textGUIGraphics: TextGUIGraphics,
    ) {
        textGUIGraphics.fill(' ')
    }

    protected open fun render(
        table: Table<V?>?,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
        isSelected: Boolean,
        textGUIGraphics: TextGUIGraphics,
    ) {
        val lines = getContent(cell)
        var rowCount = 0
        for (line in lines) {
            textGUIGraphics.putString(0, rowCount++, line)
        }
    }

    protected open fun afterRender(
        table: Table<V?>?,
        cell: V?,
        columnIndex: Int,
        rowIndex: Int,
        isSelected: Boolean,
        textGUIGraphics: TextGUIGraphics,
    ) {
    }

    protected open fun getContent(cell: V?): Array<String> {
        return if (cell == null) {
            arrayOf("")
        } else {
            cell.toString().split(Regex("\r?\n")).toTypedArray()
        }
    }
}
