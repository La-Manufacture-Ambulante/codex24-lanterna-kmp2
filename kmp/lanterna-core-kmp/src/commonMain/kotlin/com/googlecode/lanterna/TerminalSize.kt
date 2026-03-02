package com.googlecode.lanterna

class TerminalSize(columns: Int, rows: Int) {
    companion object {
        @JvmField
        val ZERO: TerminalSize = TerminalSize(0, 0)

        @JvmField
        val ONE: TerminalSize = TerminalSize(1, 1)
    }

    private val columns: Int
    private val rows: Int

    init {
        if (columns < 0 || rows < 0) {
            throw IllegalArgumentException(
                "TerminalSize dimensions cannot be less than 0: [columns: $columns, rows: $rows]"
            )
        }

        this.columns = columns
        this.rows = rows
    }

    fun getColumns(): Int {
        return columns
    }

    fun withColumns(columns: Int): TerminalSize {
        if (this.columns == columns) {
            return this
        }
        if (columns == 0 && this.rows == 0) {
            return ZERO
        }
        return TerminalSize(columns, this.rows)
    }

    fun getRows(): Int {
        return rows
    }

    fun withRows(rows: Int): TerminalSize {
        if (this.rows == rows) {
            return this
        }
        if (rows == 0 && this.columns == 0) {
            return ZERO
        }
        return TerminalSize(this.columns, rows)
    }

    fun withRelativeColumns(delta: Int): TerminalSize {
        if (delta == 0) {
            return this
        }
        return withColumns(kotlin.math.max(0, columns + delta))
    }

    fun withRelativeRows(delta: Int): TerminalSize {
        if (delta == 0) {
            return this
        }
        return withRows(kotlin.math.max(0, rows + delta))
    }

    fun withRelative(delta: TerminalSize?): TerminalSize {
        return withRelative(delta!!.getColumns(), delta.getRows())
    }

    fun withRelative(deltaColumns: Int, deltaRows: Int): TerminalSize {
        return withRelativeRows(deltaRows).withRelativeColumns(deltaColumns)
    }

    fun max(other: TerminalSize?): TerminalSize {
        return withColumns(kotlin.math.max(columns, other!!.columns))
            .withRows(kotlin.math.max(rows, other.rows))
    }

    fun min(other: TerminalSize?): TerminalSize {
        return withColumns(kotlin.math.min(columns, other!!.columns))
            .withRows(kotlin.math.min(rows, other.rows))
    }

    fun with(size: TerminalSize?): TerminalSize? {
        if (equals(size)) {
            return this
        }
        return size
    }

    override fun toString(): String {
        return "{$columns" + "x" + "$rows}"
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj !is TerminalSize) {
            return false
        }

        val other = obj
        return columns == other.columns && rows == other.rows
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 53 * hash + this.columns
        hash = 53 * hash + this.rows
        return hash
    }
}
