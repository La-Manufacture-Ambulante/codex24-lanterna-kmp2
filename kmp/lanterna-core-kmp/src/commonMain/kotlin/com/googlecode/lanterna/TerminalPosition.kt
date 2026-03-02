package com.googlecode.lanterna

class TerminalPosition(column: Int, row: Int) : Comparable<TerminalPosition> {
    companion object {
        @JvmField
        val TOP_LEFT_CORNER = TerminalPosition(0, 0)

        @JvmField
        val OFFSET_1x1 = TerminalPosition(1, 1)
    }

    private val row: Int = row
    private val column: Int = column

    fun getColumn(): Int {
        return column
    }

    fun getRow(): Int {
        return row
    }

    fun withRow(row: Int): TerminalPosition {
        if (row == 0 && this.column == 0) {
            return TOP_LEFT_CORNER
        }
        return TerminalPosition(this.column, row)
    }

    fun withColumn(column: Int): TerminalPosition {
        if (column == 0 && this.row == 0) {
            return TOP_LEFT_CORNER
        }
        return TerminalPosition(column, this.row)
    }

    fun withRelativeColumn(delta: Int): TerminalPosition {
        if (delta == 0) {
            return this
        }
        return withColumn(column + delta)
    }

    fun withRelativeRow(delta: Int): TerminalPosition {
        if (delta == 0) {
            return this
        }
        return withRow(row + delta)
    }

    fun withRelative(translate: TerminalPosition?): TerminalPosition {
        return withRelative(translate!!.getColumn(), translate.getRow())
    }

    fun withRelative(deltaColumn: Int, deltaRow: Int): TerminalPosition {
        return withRelativeRow(deltaRow).withRelativeColumn(deltaColumn)
    }

    fun with(position: TerminalPosition?): TerminalPosition? {
        if (equals(position)) {
            return this
        }
        return position
    }

    fun plus(position: TerminalPosition?): TerminalPosition {
        return withRelative(position)
    }

    fun minus(position: TerminalPosition?): TerminalPosition {
        return withRelative(-position!!.getColumn(), -position.getRow())
    }

    fun multiply(position: TerminalPosition?): TerminalPosition {
        return TerminalPosition(column * position!!.column, row * position.row)
    }

    fun divide(denominator: TerminalPosition?): TerminalPosition {
        return TerminalPosition(column / denominator!!.column, row / denominator.row)
    }

    fun abs(): TerminalPosition {
        val x = kotlin.math.abs(column)
        val y = kotlin.math.abs(row)
        return TerminalPosition(x, y)
    }

    fun min(position: TerminalPosition?): TerminalPosition {
        val x = kotlin.math.min(column, position!!.column)
        val y = kotlin.math.min(row, position.row)
        return TerminalPosition(x, y)
    }

    fun max(position: TerminalPosition?): TerminalPosition {
        val x = kotlin.math.max(column, position!!.column)
        val y = kotlin.math.max(row, position.row)
        return TerminalPosition(x, y)
    }

    override fun compareTo(o: TerminalPosition): Int {
        if (row < o.row) {
            return -1
        } else if (row == o.row) {
            if (column < o.column) {
                return -1
            } else if (column == o.column) {
                return 0
            }
        }
        return 1
    }

    override fun toString(): String {
        return "[$column:$row]"
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 23 * hash + this.row
        hash = 23 * hash + this.column
        return hash
    }

    fun equals(columnIndex: Int, rowIndex: Int): Boolean {
        return this.column == columnIndex && this.row == rowIndex
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as TerminalPosition
        return this.row == other.row && this.column == other.column
    }
}
