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

import java.util.ArrayList
import java.util.Arrays

/**
 * A [TableModel] contains the data model behind a table.
 */
open class TableModel<V>(columnLabels: List<String?>) {
    interface Listener<V> {
        fun onRowAdded(model: TableModel<V>?, index: Int)
        fun onRowRemoved(model: TableModel<V>?, index: Int, oldRow: List<V>)
        fun onColumnAdded(model: TableModel<V>?, index: Int)
        fun onColumnRemoved(model: TableModel<V>?, index: Int, oldHeader: String?, oldColumn: List<V>)
        fun onCellChanged(model: TableModel<V>?, row: Int, column: Int, oldValue: V, newValue: V)
    }

    private val columns: MutableList<String?> = ArrayList()
    private val rows: MutableList<MutableList<V>> = ArrayList()
    private val listeners: MutableList<Listener<V>> = ArrayList()

    constructor(vararg columnLabels: String?) : this(Arrays.asList(*columnLabels))

    init {
        require(columnLabels.isNotEmpty()) { "Table model needs at least one column" }
        columns.addAll(columnLabels)
    }

    @Synchronized
    fun getColumnCount(): Int = columns.size

    @Synchronized
    fun getRowCount(): Int = rows.size

    @Synchronized
    fun getRows(): List<List<V>> {
        val copy = ArrayList<List<V>>()
        for (row in rows) {
            copy.add(ArrayList(row))
        }
        return copy
    }

    @Synchronized
    fun getColumnLabels(): List<String?> {
        return ArrayList(columns)
    }

    @Synchronized
    fun getRow(index: Int): List<V> {
        return ArrayList(rows[index])
    }

    @Synchronized
    fun getColumn(index: Int): List<V> {
        val columnData = ArrayList<V>()
        for (row in 0 until getRowCount()) {
            columnData.add(getCell(index, row))
        }
        return columnData
    }

    @SafeVarargs
    @Synchronized
    final fun addRow(vararg values: V): TableModel<V> {
        addRow(Arrays.asList(*values))
        return this
    }

    @Synchronized
    fun addRow(values: Collection<V>): TableModel<V> {
        insertRow(getRowCount(), values)
        return this
    }

    @Synchronized
    fun insertRow(index: Int, values: Collection<V>): TableModel<V> {
        val list = ArrayList(values)
        rows.add(index, list)
        for (listener in listeners) {
            listener.onRowAdded(this, index)
        }
        return this
    }

    @Synchronized
    fun removeRow(index: Int): TableModel<V> {
        val removedRow = rows.removeAt(index)
        for (listener in listeners) {
            listener.onRowRemoved(this, index, removedRow)
        }
        return this
    }

    @Synchronized
    fun clear(): TableModel<V> {
        while (rows.isNotEmpty()) {
            removeRow(0)
        }
        return this
    }

    @Synchronized
    fun getColumnLabel(index: Int): String? = columns[index]

    @Synchronized
    fun setColumnLabel(index: Int, newLabel: String?): TableModel<V> {
        columns[index] = newLabel
        return this
    }

    @Synchronized
    fun addColumn(label: String?, newColumnValues: Array<V>?): TableModel<V> {
        return insertColumn(getColumnCount(), label, newColumnValues)
    }

    @Synchronized
    fun insertColumn(index: Int, label: String?, newColumnValues: Array<V>?): TableModel<V> {
        columns.add(index, label)
        for (i in rows.indices) {
            val row = rows[i]
            for (j in row.size until index) {
                row.add(null as V)
            }
            if (newColumnValues != null && i < newColumnValues.size && newColumnValues[i] != null) {
                row.add(index, newColumnValues[i])
            } else {
                row.add(index, null as V)
            }
        }
        for (listener in listeners) {
            listener.onColumnAdded(this, index)
        }
        return this
    }

    @Synchronized
    fun removeColumn(index: Int): TableModel<V> {
        val removedColumnHeader = columns.removeAt(index)
        val removedColumn = ArrayList<V>()
        for (row in rows) {
            removedColumn.add(row.removeAt(index))
        }
        for (listener in listeners) {
            listener.onColumnRemoved(this, index, removedColumnHeader, removedColumn)
        }
        return this
    }

    @Synchronized
    fun getCell(columnIndex: Int, rowIndex: Int): V {
        if (rowIndex < 0 || columnIndex < 0) {
            throw IndexOutOfBoundsException("Invalid row or column index: $rowIndex $columnIndex")
        } else if (rowIndex >= getRowCount()) {
            throw IndexOutOfBoundsException("TableModel has ${getRowCount()} rows, invalid access at rowIndex $rowIndex")
        }
        if (columnIndex >= getColumnCount()) {
            throw IndexOutOfBoundsException("TableModel has $columnIndex columns, invalid access at columnIndex $columnIndex")
        }
        return rows[rowIndex][columnIndex]
    }

    @Synchronized
    fun setCell(columnIndex: Int, rowIndex: Int, value: V): TableModel<V> {
        getCell(columnIndex, rowIndex)
        val row = rows[rowIndex]
        for (j in row.size until columnIndex) {
            row.add(null as V)
        }
        val existingValue = row[columnIndex]
        if (existingValue === value) {
            return this
        }
        row[columnIndex] = value
        for (listener in listeners) {
            listener.onCellChanged(this, rowIndex, columnIndex, existingValue, value)
        }
        return this
    }

    fun addListener(listener: Listener<V>?): TableModel<V> {
        if (listener != null) {
            listeners.add(listener)
        }
        return this
    }

    fun removeListener(listener: Listener<V>?): TableModel<V> {
        listeners.remove(listener)
        return this
    }
}
