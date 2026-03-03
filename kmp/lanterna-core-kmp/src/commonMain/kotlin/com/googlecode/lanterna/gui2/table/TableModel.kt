package com.googlecode.lanterna.gui2.table

import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.List

open class TableModel<V> {

    interface Listener<V> {
        fun onRowAdded(model: TableModel<V>?, index: Int)
        fun onRowRemoved(model: TableModel<V>?, index: Int, oldRow: List<V?>?)
        fun onColumnAdded(model: TableModel<V>?, index: Int)
        fun onColumnRemoved(model: TableModel<V>?, index: Int, oldHeader: String?, oldColumn: List<V?>?)
        fun onCellChanged(model: TableModel<V>?, row: Int, column: Int, oldValue: V?, newValue: V?)
    }

    private val columns: MutableList<String?>
    private val rows: MutableList<MutableList<V?>>
    private val listeners: MutableList<Listener<V>?>

    constructor(columnLabels: Array<out String?>?) : this(Arrays.asList(*requireArray(columnLabels)))

    constructor(columnLabels: List<String?>) {
        if (columnLabels.isEmpty()) {
            throw IllegalArgumentException("Table model needs at least one column")
        }
        this.columns = ArrayList(columnLabels)
        this.rows = ArrayList()
        this.listeners = ArrayList()
    }

    @Synchronized
    open fun getColumnCount(): Int {
        return columns.size
    }

    @Synchronized
    open fun getRowCount(): Int {
        return rows.size
    }

    @Synchronized
    open fun getRows(): List<List<V?>> {
        val copy: MutableList<List<V?>> = ArrayList()
        for (row in rows) {
            copy.add(ArrayList(row))
        }
        return copy
    }

    @Synchronized
    open fun getColumnLabels(): List<String?> {
        return ArrayList(columns)
    }

    @Synchronized
    open fun getRow(index: Int): List<V?> {
        return ArrayList(rows[index])
    }

    @Synchronized
    open fun getColumn(index: Int): List<V?> {
        val columnData: MutableList<V?> = ArrayList()
        var row = 0
        while (row < getRowCount()) {
            columnData.add(getCell(index, row))
            row++
        }
        return columnData
    }

    @Synchronized
    fun addRow(values: Array<out V?>?): TableModel<V> {
        addRow(Arrays.asList(*requireArray(values)))
        return this
    }

    @Synchronized
    open fun addRow(values: Collection<V?>): TableModel<V> {
        insertRow(getRowCount(), values)
        return this
    }

    @Synchronized
    open fun insertRow(index: Int, values: Collection<V?>): TableModel<V> {
        val list: ArrayList<V?> = ArrayList(values)
        rows.add(index, list)
        for (listener in listeners) {
            if (listener == null) {
                throw NullPointerException()
            }
            listener.onRowAdded(this, index)
        }
        return this
    }

    @Synchronized
    open fun removeRow(index: Int): TableModel<V> {
        val removedRow: List<V?> = rows.removeAt(index)
        for (listener in listeners) {
            if (listener == null) {
                throw NullPointerException()
            }
            listener.onRowRemoved(this, index, removedRow)
        }
        return this
    }

    @Synchronized
    open fun clear(): TableModel<V> {
        while (rows.size > 0) {
            removeRow(0)
        }
        return this
    }

    @Synchronized
    open fun getColumnLabel(index: Int): String? {
        return columns[index]
    }

    @Synchronized
    open fun setColumnLabel(index: Int, newLabel: String?): TableModel<V> {
        columns[index] = newLabel
        return this
    }

    @Synchronized
    open fun addColumn(label: String?, newColumnValues: Array<out V?>?): TableModel<V> {
        return insertColumn(getColumnCount(), label, newColumnValues)
    }

    @Synchronized
    open fun insertColumn(index: Int, label: String?, newColumnValues: Array<out V?>?): TableModel<V> {
        columns.add(index, label)
        var i = 0
        while (i < rows.size) {
            val row: MutableList<V?> = rows[i]

            var j = row.size
            while (j < index) {
                row.add(null)
                j++
            }

            if (newColumnValues != null && i < newColumnValues.size && newColumnValues[i] != null) {
                row.add(index, newColumnValues[i])
            } else {
                row.add(index, null)
            }
            i++
        }

        for (listener in listeners) {
            if (listener == null) {
                throw NullPointerException()
            }
            listener.onColumnAdded(this, index)
        }
        return this
    }

    @Synchronized
    open fun removeColumn(index: Int): TableModel<V> {
        val removedColumnHeader: String? = columns.removeAt(index)
        val removedColumn: MutableList<V?> = ArrayList()
        for (row in rows) {
            removedColumn.add(row.removeAt(index))
        }
        for (listener in listeners) {
            if (listener == null) {
                throw NullPointerException()
            }
            listener.onColumnRemoved(this, index, removedColumnHeader, removedColumn)
        }
        return this
    }

    @Synchronized
    open fun getCell(columnIndex: Int, rowIndex: Int): V? {
        if (rowIndex < 0 || columnIndex < 0) {
            throw IndexOutOfBoundsException("Invalid row or column index: $rowIndex $columnIndex")
        } else if (rowIndex >= getRowCount()) {
            throw IndexOutOfBoundsException("TableModel has " + getRowCount() + " rows, invalid access at rowIndex " + rowIndex)
        }
        if (columnIndex >= getColumnCount()) {
            throw IndexOutOfBoundsException("TableModel has " + columnIndex + " columns, invalid access at columnIndex " + columnIndex)
        }
        return rows[rowIndex][columnIndex]
    }

    @Synchronized
    open fun setCell(columnIndex: Int, rowIndex: Int, value: V?): TableModel<V> {
        getCell(columnIndex, rowIndex)
        val row: MutableList<V?> = rows[rowIndex]

        var j = row.size
        while (j < columnIndex) {
            row.add(null)
            j++
        }

        val existingValue: V? = row[columnIndex]
        if (existingValue === value) {
            return this
        }
        row[columnIndex] = value
        for (listener in listeners) {
            if (listener == null) {
                throw NullPointerException()
            }
            listener.onCellChanged(this, rowIndex, columnIndex, existingValue, value)
        }
        return this
    }

    open fun addListener(listener: Listener<V>?): TableModel<V> {
        listeners.add(listener)
        return this
    }

    open fun removeListener(listener: Listener<V>?): TableModel<V> {
        listeners.remove(listener)
        return this
    }

    companion object {
        private fun <T> requireArray(values: Array<out T?>?): Array<out T?> {
            if (values == null) {
                throw NullPointerException()
            }
            return values
        }
    }
}
