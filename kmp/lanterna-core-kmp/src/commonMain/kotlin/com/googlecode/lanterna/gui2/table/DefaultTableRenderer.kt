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
package com.googlecode.lanterna.gui2.table

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.ScrollBar
import com.googlecode.lanterna.gui2.TextGUIGraphics
import java.util.ArrayList
import java.util.TreeSet

/**
 * Default implementation of [TableRenderer].
 */
open class DefaultTableRenderer<V> : TableRenderer<V?> {
    private val verticalScrollBar = ScrollBar(Direction.VERTICAL)
    private val horizontalScrollBar = ScrollBar(Direction.HORIZONTAL)

    private var headerVerticalBorderStyle: TableCellBorderStyle = TableCellBorderStyle.NONE
    private var headerHorizontalBorderStyle: TableCellBorderStyle = TableCellBorderStyle.EMPTY_SPACE
    private var cellVerticalBorderStyle: TableCellBorderStyle = TableCellBorderStyle.NONE
    private var cellHorizontalBorderStyle: TableCellBorderStyle = TableCellBorderStyle.EMPTY_SPACE

    override var viewTopRow: Int = 0
    override var viewLeftColumn: Int = 0
    override var visibleRowsOnLastDraw: Int = 0
        protected set

    private var cachedSize: TerminalSize? = null
    private val preferredColumnSizes: MutableList<Int> = ArrayList()
    private val preferredRowSizes: MutableList<Int> = ArrayList()
    private val expandableColumns: MutableSet<Int> = TreeSet()
    private var headerSizeInRows: Int = 0

    override var allowPartialColumn: Boolean = false
    override var isScrollBarsHidden: Boolean = false

    private val isHorizontallySpaced: Boolean
        get() = headerHorizontalBorderStyle != TableCellBorderStyle.NONE ||
            cellHorizontalBorderStyle != TableCellBorderStyle.NONE

    @Synchronized
    fun setHeaderVerticalBorderStyle(headerVerticalBorderStyle: TableCellBorderStyle?) {
        this.headerVerticalBorderStyle = headerVerticalBorderStyle ?: TableCellBorderStyle.NONE
    }

    @Synchronized
    fun setHeaderHorizontalBorderStyle(headerHorizontalBorderStyle: TableCellBorderStyle?) {
        this.headerHorizontalBorderStyle = headerHorizontalBorderStyle ?: TableCellBorderStyle.NONE
    }

    @Synchronized
    fun setCellVerticalBorderStyle(cellVerticalBorderStyle: TableCellBorderStyle?) {
        this.cellVerticalBorderStyle = cellVerticalBorderStyle ?: TableCellBorderStyle.NONE
    }

    @Synchronized
    fun setCellHorizontalBorderStyle(cellHorizontalBorderStyle: TableCellBorderStyle?) {
        this.cellHorizontalBorderStyle = cellHorizontalBorderStyle ?: TableCellBorderStyle.NONE
    }

    @Synchronized
    fun setExpandableColumns(expandableColumns: Collection<Int>?) {
        this.expandableColumns.clear()
        if (expandableColumns != null) {
            this.expandableColumns.addAll(expandableColumns)
        }
    }

    @Synchronized
    override fun getPreferredSize(component: Table<V?>?): TerminalSize {
        val table = component ?: return TerminalSize.ZERO
        if (!table.isInvalid && cachedSize != null) {
            return cachedSize ?: TerminalSize.ZERO
        }

        val tableModel = table.getTableModel() ?: return TerminalSize.ZERO
        var localViewLeftColumn = viewLeftColumn
        var localViewTopRow = viewTopRow
        var visibleColumns = table.getVisibleColumns()
        var visibleRows = table.getVisibleRows()
        val selectedRow = table.getSelectedRow()
        val selectedColumn = table.getSelectedColumn()
        val rows = tableModel.getRows()
        val columnHeaders = tableModel.getColumnLabels()
        val tableHeaderRenderer = table.getTableHeaderRenderer() ?: return TerminalSize.ZERO
        val tableCellRenderer = table.getTableCellRenderer() ?: return TerminalSize.ZERO

        if (visibleColumns == 0) {
            visibleColumns = tableModel.getColumnCount()
        }
        if (visibleRows == 0) {
            visibleRows = tableModel.getRowCount()
        }

        preferredColumnSizes.clear()
        preferredRowSizes.clear()

        if (tableModel.getColumnCount() == 0) {
            return TerminalSize.ZERO
        }

        if (selectedColumn != -1 && localViewLeftColumn > selectedColumn) {
            localViewLeftColumn = selectedColumn
        } else if (selectedColumn != -1 && localViewLeftColumn <= selectedColumn - visibleColumns) {
            localViewLeftColumn = kotlin.math.max(0, selectedColumn - visibleColumns + 1)
        }
        if (localViewTopRow > selectedRow) {
            localViewTopRow = selectedRow
        } else if (localViewTopRow <= selectedRow - visibleRows) {
            localViewTopRow = kotlin.math.max(0, selectedRow - visibleRows + 1)
        }

        if (rows.isEmpty()) {
            for (columnIndex in columnHeaders.indices) {
                val columnSize =
                    tableHeaderRenderer.getPreferredSize(table, columnHeaders[columnIndex], columnIndex)?.columns ?: 0
                if (preferredColumnSizes.size == columnIndex) {
                    preferredColumnSizes.add(columnSize)
                } else if (preferredColumnSizes[columnIndex] < columnSize) {
                    preferredColumnSizes[columnIndex] = columnSize
                }
            }
        }

        for (rowIndex in rows.indices) {
            val row = rows[rowIndex]
            for (columnIndex in row.indices) {
                val cell = row[columnIndex]
                val columnSize = tableCellRenderer.getPreferredSize(table, cell, columnIndex, rowIndex)?.columns ?: 0
                if (preferredColumnSizes.size == columnIndex) {
                    preferredColumnSizes.add(columnSize)
                } else if (preferredColumnSizes[columnIndex] < columnSize) {
                    preferredColumnSizes[columnIndex] = columnSize
                }
            }

            if (rowIndex == 0) {
                for (columnIndex in row.indices) {
                    val columnSize =
                        tableHeaderRenderer.getPreferredSize(table, columnHeaders[columnIndex], columnIndex)?.columns ?: 0
                    if (preferredColumnSizes.size == columnIndex) {
                        preferredColumnSizes.add(columnSize)
                    } else if (preferredColumnSizes[columnIndex] < columnSize) {
                        preferredColumnSizes[columnIndex] = columnSize
                    }
                }
            }
        }

        for (columnIndex in columnHeaders.indices) {
            for (rowIndex in rows.indices) {
                val cell = rows[rowIndex][columnIndex]
                val rowSize = tableCellRenderer.getPreferredSize(table, cell, columnIndex, rowIndex)?.rows ?: 0
                if (preferredRowSizes.size == rowIndex) {
                    preferredRowSizes.add(rowSize)
                } else if (preferredRowSizes[rowIndex] < rowSize) {
                    preferredRowSizes[rowIndex] = rowSize
                }
            }
        }

        var preferredRowSize = 0
        var preferredColumnSize = 0
        if (table.getVisibleColumns() == 0) {
            for (columnSize in preferredColumnSizes) {
                preferredColumnSize += columnSize
            }
        } else {
            for (columnIndex in localViewLeftColumn until kotlin.math.min(preferredColumnSizes.size, localViewLeftColumn + visibleColumns)) {
                preferredColumnSize += preferredColumnSizes[columnIndex]
            }
        }

        if (table.getVisibleRows() == 0) {
            for (rowIndex in rows.indices) {
                preferredRowSize += preferredRowSizes[rowIndex]
            }
        } else {
            for (rowIndex in localViewTopRow until kotlin.math.min(rows.size, localViewTopRow + visibleRows)) {
                preferredRowSize += preferredRowSizes[rowIndex]
            }
        }

        headerSizeInRows = 0
        for (columnIndex in columnHeaders.indices) {
            val headerRows = tableHeaderRenderer.getPreferredSize(table, columnHeaders[columnIndex], columnIndex)?.rows ?: 0
            if (headerSizeInRows < headerRows) {
                headerSizeInRows = headerRows
            }
        }
        preferredRowSize += headerSizeInRows

        if (headerVerticalBorderStyle != TableCellBorderStyle.NONE) {
            preferredRowSize++
        }
        if (cellVerticalBorderStyle != TableCellBorderStyle.NONE && rows.isNotEmpty()) {
            preferredRowSize += kotlin.math.min(rows.size, visibleRows) - 1
        }
        if (isHorizontallySpaced && columnHeaders.isNotEmpty()) {
            preferredColumnSize += kotlin.math.min(tableModel.getColumnCount(), visibleColumns) - 1
        }
        if (!isScrollBarsHidden) {
            preferredColumnSize++
            if (visibleColumns < tableModel.getColumnCount()) {
                preferredRowSize++
            }
        }

        cachedSize = TerminalSize(preferredColumnSize, preferredRowSize)
        return cachedSize ?: TerminalSize.ZERO
    }

    override fun getCursorLocation(component: Table<V?>?): TerminalPosition? {
        return null
    }

    @Synchronized
    override fun drawComponent(graphics: TextGUIGraphics?, component: Table<V?>?) {
        var activeGraphics = graphics ?: return
        val table = component ?: return
        val area = activeGraphics.size ?: return
        if (area.rows == 0 || area.columns == 0) {
            return
        }

        if (table.isInvalid) {
            getPreferredSize(table)
        }

        val headerSizeIncludingBorder = headerSizeInRows + headerVerticalBorderStyle.size
        val selectedColumn = table.getSelectedColumn()
        val selectedRow = table.getSelectedRow()

        if (selectedColumn != -1 && viewLeftColumn > selectedColumn) {
            viewLeftColumn = selectedColumn
        }
        if (viewTopRow > selectedRow) {
            viewTopRow = selectedRow
        }
        val tableModel = table.getTableModel() ?: return
        if (viewTopRow >= tableModel.getRowCount()) {
            viewTopRow = kotlin.math.max(0, tableModel.getRowCount() - 1)
        }

        var areaWithoutScrollBars = area.withRelativeRows(-headerSizeIncludingBorder) ?: TerminalSize.ZERO
        var preferredVisibleRows = table.getVisibleRows()
        if (preferredVisibleRows == 0) {
            preferredVisibleRows = tableModel.getRowCount()
        }
        var preferredVisibleColumns = table.getVisibleColumns()
        if (preferredVisibleColumns == 0) {
            preferredVisibleColumns = tableModel.getColumnCount()
        }

        var visibleRows = calculateVisibleRows(areaWithoutScrollBars, viewTopRow, preferredVisibleRows)
        var needVerticalScrollBar = !isScrollBarsHidden && visibleRows < tableModel.getRowCount()
        if (needVerticalScrollBar) {
            areaWithoutScrollBars =
                areaWithoutScrollBars.withRelativeColumns(-(verticalScrollBar.preferredSize?.columns ?: 0)) ?: areaWithoutScrollBars
        }
        var visibleColumns = calculateVisibleColumns(areaWithoutScrollBars, viewLeftColumn, preferredVisibleColumns)
        var needHorizontalScrollBar = !isScrollBarsHidden && visibleColumns < tableModel.getColumnCount()
        if (needHorizontalScrollBar) {
            areaWithoutScrollBars =
                areaWithoutScrollBars.withRelativeRows(-(horizontalScrollBar.preferredSize?.rows ?: 0)) ?: areaWithoutScrollBars
            visibleRows = calculateVisibleRows(areaWithoutScrollBars, viewTopRow, preferredVisibleRows)
            if (!needVerticalScrollBar && visibleRows < tableModel.getRowCount()) {
                needVerticalScrollBar = true
                areaWithoutScrollBars =
                    areaWithoutScrollBars.withRelativeColumns(-(verticalScrollBar.preferredSize?.columns ?: 0)) ?: areaWithoutScrollBars
                visibleColumns = calculateVisibleColumns(areaWithoutScrollBars, viewLeftColumn, preferredVisibleColumns)
            }
        }

        while (selectedColumn != -1 && viewLeftColumn <= selectedColumn - visibleColumns) {
            viewLeftColumn = kotlin.math.max(0, selectedColumn - visibleColumns + 1)
            visibleColumns = calculateVisibleColumns(areaWithoutScrollBars, viewLeftColumn, preferredVisibleColumns)
        }
        while (viewTopRow <= selectedRow - visibleRows) {
            viewTopRow = kotlin.math.max(0, selectedRow - visibleRows + 1)
            visibleRows = calculateVisibleRows(areaWithoutScrollBars, viewTopRow, preferredVisibleRows)
        }

        val renderColumns = if (allowPartialColumn && visibleColumns < preferredVisibleColumns - viewLeftColumn) {
            visibleColumns + 1
        } else {
            visibleColumns
        }

        val columnSizes = fitColumnsInAvailableSpace(table, areaWithoutScrollBars, visibleColumns)
        drawHeader(activeGraphics, table, columnSizes)

        val rowGraphics = activeGraphics.newTextGraphics(
            TerminalPosition(0, headerSizeIncludingBorder),
            area.withRelativeRows(-headerSizeIncludingBorder),
        ) ?: return
        drawRows(
            rowGraphics,
            table,
            columnSizes,
            visibleRows,
            visibleColumns,
            renderColumns,
            needVerticalScrollBar,
            needHorizontalScrollBar,
        )

        visibleRowsOnLastDraw = visibleRows
    }

    private fun calculateVisibleRows(area: TerminalSize, localViewTopRow: Int, preferredVisibleRows: Int): Int {
        var remainingVerticalSpace = area.rows
        var visibleRows = 0
        val borderAdjustment = cellVerticalBorderStyle.size
        for (row in localViewTopRow until preferredRowSizes.size) {
            if (preferredVisibleRows == visibleRows) {
                break
            }
            val rowSize = preferredRowSizes[row] + borderAdjustment
            if (remainingVerticalSpace < rowSize) {
                break
            }
            remainingVerticalSpace -= rowSize
            visibleRows++
        }
        return visibleRows
    }

    private fun calculateVisibleColumns(area: TerminalSize, localViewLeftColumn: Int, preferredVisibleColumns: Int): Int {
        var remainingHorizontalSpace = area.columns
        var visibleColumns = 0
        val borderAdjustment = cellHorizontalBorderStyle.size
        for (column in localViewLeftColumn until preferredColumnSizes.size) {
            if (preferredVisibleColumns == visibleColumns) {
                break
            }
            val columnSize = preferredColumnSizes[column] + if (column > localViewLeftColumn) borderAdjustment else 0
            if (remainingHorizontalSpace < columnSize) {
                break
            }
            remainingHorizontalSpace -= columnSize
            visibleColumns++
        }
        return visibleColumns
    }

    private fun fitColumnsInAvailableSpace(table: Table<V?>, area: TerminalSize, visibleColumns: Int): MutableList<Int> {
        val columnSizes = ArrayList(preferredColumnSizes)
        var horizontalSpaceRequirement = 0
        val localViewLeftColumn = table.renderer?.viewLeftColumn ?: 0
        val headers = table.getTableModel()?.getColumnLabels().orEmpty()
        val endColumnIndex = kotlin.math.min(headers.size, localViewLeftColumn + visibleColumns)
        val visibleExpandableColumns = ArrayList<Int>()
        for (index in localViewLeftColumn until endColumnIndex) {
            horizontalSpaceRequirement += preferredColumnSizes[index]
            if (headerHorizontalBorderStyle != TableCellBorderStyle.NONE && index < endColumnIndex - 1) {
                horizontalSpaceRequirement += headerHorizontalBorderStyle.size
            }
            if (expandableColumns.contains(index)) {
                visibleExpandableColumns.add(index)
            }
        }
        var extraHorizontalSpace = area.columns - horizontalSpaceRequirement
        while (extraHorizontalSpace > 0 && visibleExpandableColumns.isNotEmpty()) {
            for (expandableColumnIndex in visibleExpandableColumns) {
                columnSizes[expandableColumnIndex] = columnSizes[expandableColumnIndex] + 1
                extraHorizontalSpace--
                if (extraHorizontalSpace == 0) {
                    break
                }
            }
        }
        return columnSizes
    }

    private fun drawHeader(graphics: TextGUIGraphics, table: Table<V?>, columnSizes: List<Int>) {
        val theme = table.theme ?: return
        val tableHeaderRenderer = table.getTableHeaderRenderer() ?: return
        val headers = table.getTableModel()?.getColumnLabels().orEmpty()
        val localViewLeftColumn = table.renderer?.viewLeftColumn ?: 0
        var visibleColumns = table.getVisibleColumns()
        if (visibleColumns == 0) {
            visibleColumns = table.getTableModel()?.getColumnCount() ?: 0
        }
        var leftPosition = 0
        val endColumnIndex = kotlin.math.min(headers.size, localViewLeftColumn + visibleColumns)
        for (index in localViewLeftColumn until endColumnIndex) {
            val label = headers[index]
            val size = TerminalSize(columnSizes[index], headerSizeInRows)
            val headerGraphics = graphics.newTextGraphics(TerminalPosition(leftPosition, 0), size)
            tableHeaderRenderer.drawHeader(table, label, index, headerGraphics)
            leftPosition += size.columns
            if (headerHorizontalBorderStyle != TableCellBorderStyle.NONE && index < endColumnIndex - 1) {
                graphics.applyThemeStyle(theme.getDefinition(Table::class.java)?.normal)
                graphics.setCharacter(leftPosition, 0, getVerticalCharacter(headerHorizontalBorderStyle))
                leftPosition++
            }
        }

        if (headerVerticalBorderStyle != TableCellBorderStyle.NONE) {
            leftPosition = 0
            val topPosition = headerSizeInRows
            graphics.applyThemeStyle(theme.getDefinition(Table::class.java)?.normal)
            for (i in localViewLeftColumn until endColumnIndex) {
                if (i > localViewLeftColumn) {
                    graphics.setCharacter(
                        leftPosition,
                        topPosition,
                        getJunctionCharacter(headerVerticalBorderStyle, headerHorizontalBorderStyle, cellHorizontalBorderStyle),
                    )
                    leftPosition++
                }
                val columnWidth = columnSizes[i]
                graphics.drawLine(
                    leftPosition,
                    topPosition,
                    leftPosition + columnWidth - 1,
                    topPosition,
                    getHorizontalCharacter(headerVerticalBorderStyle),
                )
                leftPosition += columnWidth
            }
            val graphicsSize = graphics.size ?: TerminalSize.ZERO
            if (leftPosition < graphicsSize.columns) {
                graphics.drawLine(
                    leftPosition,
                    topPosition,
                    graphicsSize.columns - 1,
                    topPosition,
                    getHorizontalCharacter(headerVerticalBorderStyle),
                )
            }
        }
    }

    private fun drawRows(
        graphics: TextGUIGraphics,
        table: Table<V?>,
        columnSizes: List<Int>,
        visibleRows: Int,
        visibleColumns: Int,
        renderColumns: Int,
        needVerticalScrollBar: Boolean,
        needHorizontalScrollBar: Boolean,
    ) {
        val theme = table.theme ?: return
        val themeDefinition = theme.getDefinition(Table::class.java) ?: return
        val area = graphics.size ?: TerminalSize.ZERO
        val tableCellRenderer = table.getTableCellRenderer() ?: return
        val tableModel = table.getTableModel() ?: return
        val rows = tableModel.getRows()
        val localViewTopRow = table.renderer?.viewTopRow ?: 0
        val localViewLeftColumn = table.renderer?.viewLeftColumn ?: 0

        var activeGraphics = graphics
        if (needVerticalScrollBar) {
            val preferredSize = verticalScrollBar.preferredSize ?: TerminalSize.ONE
            var scrollBarHeight = (activeGraphics.size ?: TerminalSize.ZERO).rows
            if (needHorizontalScrollBar) {
                scrollBarHeight--
            }
            verticalScrollBar.setPosition(
                TerminalPosition((activeGraphics.size ?: TerminalSize.ZERO).columns - preferredSize.columns, 0),
            )
            verticalScrollBar.setSize(preferredSize.withRows(scrollBarHeight))
            verticalScrollBar.setScrollMaximum(rows.size)
            verticalScrollBar.setViewSize(visibleRows)
            verticalScrollBar.setScrollPosition(localViewTopRow)
            if (table.parent !== verticalScrollBar.parent) {
                verticalScrollBar.parent?.let { verticalScrollBar.onRemoved(it) }
                table.parent?.let { verticalScrollBar.onAdded(it) }
            }
            verticalScrollBar.draw(activeGraphics.newTextGraphics(verticalScrollBar.position, verticalScrollBar.size))
            activeGraphics =
                activeGraphics.newTextGraphics(
                    TerminalPosition.TOP_LEFT_CORNER,
                    (activeGraphics.size ?: TerminalSize.ZERO).withRelativeColumns(-preferredSize.columns),
                ) ?: activeGraphics
        }
        if (needHorizontalScrollBar) {
            val preferredSize = horizontalScrollBar.preferredSize ?: TerminalSize.ONE
            val scrollBarWidth = (activeGraphics.size ?: TerminalSize.ZERO).columns
            horizontalScrollBar.setPosition(
                TerminalPosition(0, (activeGraphics.size ?: TerminalSize.ZERO).rows - preferredSize.rows),
            )
            horizontalScrollBar.setSize(preferredSize.withColumns(scrollBarWidth))
            horizontalScrollBar.setScrollMaximum(tableModel.getColumnCount())
            horizontalScrollBar.setViewSize(visibleColumns)
            horizontalScrollBar.setScrollPosition(localViewLeftColumn)
            if (table.parent !== horizontalScrollBar.parent) {
                horizontalScrollBar.parent?.let { horizontalScrollBar.onRemoved(it) }
                table.parent?.let { horizontalScrollBar.onAdded(it) }
            }
            horizontalScrollBar.draw(activeGraphics.newTextGraphics(horizontalScrollBar.position, horizontalScrollBar.size))
            activeGraphics =
                activeGraphics.newTextGraphics(
                    TerminalPosition.TOP_LEFT_CORNER,
                    (activeGraphics.size ?: TerminalSize.ZERO).withRelativeRows(-preferredSize.rows),
                ) ?: activeGraphics
        }

        var topPosition = 0
        for (rowIndex in localViewTopRow until kotlin.math.min(localViewTopRow + visibleRows, rows.size)) {
            var leftPosition = 0
            val row = rows[rowIndex]
            for (columnIndex in localViewLeftColumn until kotlin.math.min(localViewLeftColumn + renderColumns, row.size)) {
                if (columnIndex > localViewLeftColumn) {
                    applyRowDividerStyle(activeGraphics, table, themeDefinition, rowIndex)
                    activeGraphics.setCharacter(leftPosition, topPosition, getVerticalCharacter(cellHorizontalBorderStyle))
                    leftPosition++
                }
                val cell = row[columnIndex]
                val cellPosition = TerminalPosition(leftPosition, topPosition)
                val cellArea = TerminalSize(columnSizes[columnIndex], preferredRowSizes[rowIndex])
                tableCellRenderer.drawCell(table, cell, columnIndex, rowIndex, activeGraphics.newTextGraphics(cellPosition, cellArea))
                leftPosition += cellArea.columns

                if (columnIndex < row.size - 1) {
                    applyRowDividerStyle(activeGraphics, table, themeDefinition, rowIndex)
                    activeGraphics.setCharacter(leftPosition, topPosition, getVerticalCharacter(cellHorizontalBorderStyle))
                }

                if (leftPosition > area.columns) {
                    break
                }
            }
            topPosition += preferredRowSizes[rowIndex]
            if (cellVerticalBorderStyle != TableCellBorderStyle.NONE) {
                leftPosition = 0
                activeGraphics.applyThemeStyle(themeDefinition.normal)
                for (i in localViewLeftColumn until kotlin.math.min(localViewLeftColumn + renderColumns + 1, row.size)) {
                    if (i > localViewLeftColumn) {
                        activeGraphics.setCharacter(
                            leftPosition,
                            topPosition,
                            getJunctionCharacter(cellVerticalBorderStyle, cellHorizontalBorderStyle, cellHorizontalBorderStyle),
                        )
                        leftPosition++
                    }
                    val columnWidth = columnSizes[i]
                    activeGraphics.drawLine(
                        leftPosition,
                        topPosition,
                        leftPosition + columnWidth - 1,
                        topPosition,
                        getHorizontalCharacter(cellVerticalBorderStyle),
                    )
                    leftPosition += columnWidth
                }
                topPosition += cellVerticalBorderStyle.size
            }
            if (topPosition > area.rows) {
                break
            }
        }
    }

    private fun applyRowDividerStyle(
        graphics: TextGUIGraphics,
        table: Table<V?>,
        themeDefinition: ThemeDefinition,
        rowIndex: Int,
    ) {
        if (table.getSelectedRow() == rowIndex && !table.isCellSelection()) {
            if (table.isFocused) {
                graphics.applyThemeStyle(themeDefinition.active)
            } else {
                graphics.applyThemeStyle(themeDefinition.selected)
            }
        } else {
            graphics.applyThemeStyle(themeDefinition.normal)
        }
    }

    private fun getHorizontalCharacter(style: TableCellBorderStyle): Char {
        return when (style) {
            TableCellBorderStyle.SINGLE_LINE -> Symbols.SINGLE_LINE_HORIZONTAL
            TableCellBorderStyle.DOUBLE_LINE -> Symbols.DOUBLE_LINE_HORIZONTAL
            else -> ' '
        }
    }

    private fun getVerticalCharacter(style: TableCellBorderStyle): Char {
        return when (style) {
            TableCellBorderStyle.SINGLE_LINE -> Symbols.SINGLE_LINE_VERTICAL
            TableCellBorderStyle.DOUBLE_LINE -> Symbols.DOUBLE_LINE_VERTICAL
            else -> ' '
        }
    }

    private fun getJunctionCharacter(
        mainStyle: TableCellBorderStyle,
        styleAbove: TableCellBorderStyle,
        styleBelow: TableCellBorderStyle,
    ): Char {
        return if (mainStyle == TableCellBorderStyle.SINGLE_LINE) {
            if (styleAbove == TableCellBorderStyle.SINGLE_LINE) {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.SINGLE_LINE_CROSS
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.SINGLE_LINE_T_UP
                } else {
                    Symbols.SINGLE_LINE_T_UP
                }
            } else if (styleAbove == TableCellBorderStyle.DOUBLE_LINE) {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.SINGLE_LINE_T_DOUBLE_UP
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS
                } else {
                    Symbols.SINGLE_LINE_T_DOUBLE_UP
                }
            } else {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.SINGLE_LINE_T_DOWN
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.SINGLE_LINE_T_DOUBLE_DOWN
                } else {
                    Symbols.SINGLE_LINE_HORIZONTAL
                }
            }
        } else if (mainStyle == TableCellBorderStyle.DOUBLE_LINE) {
            if (styleAbove == TableCellBorderStyle.SINGLE_LINE) {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.DOUBLE_LINE_T_SINGLE_UP
                } else {
                    Symbols.DOUBLE_LINE_T_SINGLE_UP
                }
            } else if (styleAbove == TableCellBorderStyle.DOUBLE_LINE) {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.DOUBLE_LINE_T_UP
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.DOUBLE_LINE_CROSS
                } else {
                    Symbols.DOUBLE_LINE_T_UP
                }
            } else {
                if (styleBelow == TableCellBorderStyle.SINGLE_LINE) {
                    Symbols.DOUBLE_LINE_T_SINGLE_DOWN
                } else if (styleBelow == TableCellBorderStyle.DOUBLE_LINE) {
                    Symbols.DOUBLE_LINE_T_DOWN
                } else {
                    Symbols.DOUBLE_LINE_HORIZONTAL
                }
            }
        } else {
            ' '
        }
    }
}
