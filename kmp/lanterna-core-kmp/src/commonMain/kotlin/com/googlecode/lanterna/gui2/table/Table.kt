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

import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * The table class is an interactable component that displays a grid of cells containing data along with a header.
 */
open class Table<V>(tableModel: TableModel<V?>) : AbstractInteractableComponent<Table<V?>>() {
    private var tableModel: TableModel<V?> = tableModel
    private val tableModelListener: TableModel.Listener<V?>
    private var tableHeaderRenderer: TableHeaderRenderer<V?> = DefaultTableHeaderRenderer<V>()
    private var tableCellRenderer: TableCellRenderer<V?> = DefaultTableCellRenderer<V>()
    private var selectAction: Runnable? = null
    private var cellSelection: Boolean = false
    private var visibleRows: Int = 0
    private var visibleColumns: Int = 0
    private var selectedRow: Int = 0
    private var selectedColumn: Int = -1
    private var escapeByArrowKey: Boolean = true

    constructor(vararg columnLabels: String?) : this(TableModel<V?>(*columnLabels))

    init {
        tableModelListener =
            object : TableModel.Listener<V?> {
                override fun onRowAdded(
                    model: TableModel<V?>?,
                    index: Int,
                ) {
                    if (index <= selectedRow) {
                        selectedRow = kotlin.math.min((model?.getRowCount() ?: 1) - 1, selectedRow + 1)
                    }
                    invalidate()
                }

                override fun onRowRemoved(
                    model: TableModel<V?>?,
                    index: Int,
                    oldRow: List<V?>,
                ) {
                    if (index < selectedRow) {
                        selectedRow = kotlin.math.max(0, selectedRow - 1)
                    } else {
                        val rowCount = model?.getRowCount() ?: 0
                        if (selectedRow > rowCount - 1) {
                            selectedRow = kotlin.math.max(0, rowCount - 1)
                        }
                    }
                    invalidate()
                }

                override fun onColumnAdded(
                    model: TableModel<V?>?,
                    index: Int,
                ) {
                    invalidate()
                }

                override fun onColumnRemoved(
                    model: TableModel<V?>?,
                    index: Int,
                    oldHeader: String?,
                    oldColumn: List<V?>,
                ) {
                    invalidate()
                }

                override fun onCellChanged(
                    model: TableModel<V?>?,
                    row: Int,
                    column: Int,
                    oldValue: V?,
                    newValue: V?,
                ) {
                    invalidate()
                }
            }
        this.tableModel.addListener(tableModelListener)
    }

    fun getTableModel(): TableModel<V?> = tableModel

    @Synchronized
    fun setTableModel(tableModel: TableModel<V?>?): Table<V?> {
        requireNotNull(tableModel) { "Cannot assign a null TableModel" }
        this.tableModel.removeListener(tableModelListener)
        this.tableModel = tableModel
        this.tableModel.addListener(tableModelListener)
        invalidate()
        return self()
    }

    fun getTableCellRenderer(): TableCellRenderer<V?> = tableCellRenderer

    @Synchronized
    fun setTableCellRenderer(tableCellRenderer: TableCellRenderer<V?>?): Table<V?> {
        requireNotNull(tableCellRenderer) { "Cannot assign a null TableCellRenderer" }
        this.tableCellRenderer = tableCellRenderer
        invalidate()
        return self()
    }

    fun getTableHeaderRenderer(): TableHeaderRenderer<V?> = tableHeaderRenderer

    @Synchronized
    fun setTableHeaderRenderer(tableHeaderRenderer: TableHeaderRenderer<V?>?): Table<V?> {
        requireNotNull(tableHeaderRenderer) { "Cannot assign a null TableHeaderRenderer" }
        this.tableHeaderRenderer = tableHeaderRenderer
        invalidate()
        return self()
    }

    @Synchronized
    fun setVisibleColumns(visibleColumns: Int) {
        this.visibleColumns = visibleColumns
        invalidate()
    }

    fun getVisibleColumns(): Int = visibleColumns

    @Synchronized
    fun setVisibleRows(visibleRows: Int) {
        this.visibleRows = visibleRows
        invalidate()
    }

    fun getVisibleRows(): Int = visibleRows

    @Deprecated("Use the table renderers method instead")
    fun getViewTopRow(): Int = renderer?.viewTopRow ?: 0

    fun getFirstViewedRowIndex(): Int = renderer?.viewTopRow ?: 0

    fun getLastViewedRowIndex(): Int {
        val currentRenderer = renderer ?: return 0
        val visibleRows = currentRenderer.visibleRowsOnLastDraw
        return kotlin.math.min(currentRenderer.viewTopRow + visibleRows - 1, tableModel.getRowCount() - 1)
    }

    @Deprecated("Use the table renderers method instead")
    @Synchronized
    fun setViewTopRow(viewTopRow: Int): Table<V?> {
        renderer?.viewTopRow = viewTopRow
        return self()
    }

    @Deprecated("Use the table renderers method instead")
    fun getViewLeftColumn(): Int = renderer?.viewLeftColumn ?: 0

    @Deprecated("Use the table renderers method instead")
    @Synchronized
    fun setViewLeftColumn(viewLeftColumn: Int): Table<V?> {
        renderer?.viewLeftColumn = viewLeftColumn
        return self()
    }

    fun getSelectedColumn(): Int = selectedColumn

    @Synchronized
    fun setSelectedColumn(selectedColumn: Int): Table<V?> {
        if (cellSelection) {
            this.selectedColumn = selectedColumn
        }
        return self()
    }

    fun getSelectedRow(): Int = selectedRow

    @Synchronized
    fun setSelectedRow(selectedRow: Int): Table<V?> {
        require(selectedRow >= 0) { "selectedRow must be >= 0 but was $selectedRow" }
        var nextSelectedRow = selectedRow
        val rowCount = tableModel.getRowCount()
        nextSelectedRow =
            if (rowCount == 0) {
                0
            } else if (nextSelectedRow > rowCount - 1) {
                rowCount - 1
            } else {
                nextSelectedRow
            }
        this.selectedRow = nextSelectedRow
        return self()
    }

    @Synchronized
    fun setCellSelection(cellSelection: Boolean): Table<V?> {
        this.cellSelection = cellSelection
        selectedColumn =
            if (cellSelection && selectedColumn == -1) {
                0
            } else if (!cellSelection) {
                -1
            } else {
                selectedColumn
            }
        return self()
    }

    fun isCellSelection(): Boolean = cellSelection

    @Synchronized
    fun setSelectAction(selectAction: Runnable?): Table<V?> {
        this.selectAction = selectAction
        return self()
    }

    fun isEscapeByArrowKey(): Boolean = escapeByArrowKey

    @Synchronized
    fun setEscapeByArrowKey(escapeByArrowKey: Boolean): Table<V?> {
        this.escapeByArrowKey = escapeByArrowKey
        return self()
    }

    override fun createDefaultRenderer(): TableRenderer<V?>? {
        return DefaultTableRenderer<V>()
    }

    override val renderer: TableRenderer<V?>?
        get() = super.renderer as TableRenderer<V?>?

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        when (keyStroke.keyType) {
            KeyType.ARROW_UP -> {
                if (selectedRow > 0) {
                    selectedRow--
                } else if (escapeByArrowKey) {
                    return Interactable.Result.MOVE_FOCUS_UP
                }
            }

            KeyType.ARROW_DOWN -> {
                if (selectedRow < tableModel.getRowCount() - 1) {
                    selectedRow++
                } else if (escapeByArrowKey) {
                    return Interactable.Result.MOVE_FOCUS_DOWN
                }
            }

            KeyType.PAGE_UP -> {
                val visibleRows = renderer?.visibleRowsOnLastDraw ?: 0
                if (visibleRows > 0 && selectedRow > 0) {
                    selectedRow -= kotlin.math.min(visibleRows - 1, selectedRow)
                }
            }

            KeyType.PAGE_DOWN -> {
                val visibleRows = renderer?.visibleRowsOnLastDraw ?: 0
                if (visibleRows > 0 && selectedRow < tableModel.getRowCount() - 1) {
                    val toEndDistance = tableModel.getRowCount() - 1 - selectedRow
                    selectedRow += kotlin.math.min(visibleRows - 1, toEndDistance)
                }
            }

            KeyType.HOME -> selectedRow = 0
            KeyType.END -> selectedRow = tableModel.getRowCount() - 1

            KeyType.ARROW_LEFT -> {
                if (cellSelection && selectedColumn > 0) {
                    selectedColumn--
                } else if (escapeByArrowKey) {
                    return Interactable.Result.MOVE_FOCUS_LEFT
                }
            }

            KeyType.ARROW_RIGHT -> {
                if (cellSelection && selectedColumn < tableModel.getColumnCount() - 1) {
                    selectedColumn++
                } else if (escapeByArrowKey) {
                    return Interactable.Result.MOVE_FOCUS_RIGHT
                }
            }

            KeyType.CHARACTER, KeyType.ENTER -> {
                if (isKeyboardActivationStroke(keyStroke)) {
                    val runnable = selectAction
                    if (runnable != null) {
                        runnable.run()
                    } else {
                        return Interactable.Result.HANDLED
                    }
                } else {
                    return super.handleKeyStroke(keyStroke)
                }
            }

            KeyType.MOUSE_EVENT -> {
                val action = keyStroke as? MouseAction ?: return Interactable.Result.UNHANDLED
                if (action.actionType == MouseActionType.MOVE) {
                    return Interactable.Result.UNHANDLED
                }
                if (!isFocused) {
                    super.handleKeyStroke(keyStroke)
                }
                val mouseRow = getRowByMouseAction(action)
                val mouseColumn = getColumnByMouseAction(action)
                val isDifferentCell = mouseRow != selectedRow || mouseColumn != selectedColumn
                selectedRow = mouseRow
                selectedColumn = mouseColumn
                if (isDifferentCell) {
                    return handleKeyStroke(KeyStroke(KeyType.ENTER))
                }
            }

            else -> return super.handleKeyStroke(keyStroke)
        }
        invalidate()
        return Interactable.Result.HANDLED
    }

    protected fun getRowByMouseAction(mouseAction: MouseAction): Int {
        val minPossible = getFirstViewedRowIndex()
        val maxPossible = getLastViewedRowIndex()
        val mouseSpecified = (mouseAction.position?.row ?: 0) - (globalPosition?.row ?: 0) - 1
        return kotlin.math.max(minPossible, kotlin.math.min(mouseSpecified, maxPossible))
    }

    protected fun getColumnByMouseAction(mouseAction: MouseAction): Int {
        val maxColumnIndex = tableModel.getColumnCount() - 1
        var column = 0
        var columnSize = tableHeaderRenderer.getPreferredSize(self(), tableModel.getColumnLabel(column), column)?.columns ?: 0
        var globalColumnMoused = (mouseAction.position?.column ?: 0) - (globalPosition?.column ?: 0)
        while (globalColumnMoused - columnSize - 1 >= 0 && column < maxColumnIndex) {
            globalColumnMoused -= columnSize
            column++
            columnSize = tableHeaderRenderer.getPreferredSize(self(), tableModel.getColumnLabel(column), column)?.columns ?: 0
        }
        return column
    }
}
