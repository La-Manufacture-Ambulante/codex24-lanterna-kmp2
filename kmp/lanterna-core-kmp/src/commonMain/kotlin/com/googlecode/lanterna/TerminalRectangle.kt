package com.googlecode.lanterna

/**
 * This class is immutable and cannot change its internal state after creation.
 *
 * @author ginkoblongata
 */
class TerminalRectangle(x: Int, y: Int, width: Int, height: Int) {

    // one of the benefits of immutable: ease of usage
    val position: TerminalPosition = TerminalPosition(x, y)
    val size: TerminalSize = TerminalSize(width, height)
    val x: Int = x
    val y: Int = y
    val width: Int = width
    val height: Int = height

    val xAndWidth: Int = x + width
    val yAndHeight: Int = y + height

    /**
     * @return Returns the width of this rect, in number of columns
     */
    fun getColumns(): Int {
        return width
    }

    /**
     * @return Returns the height of this rect representation, in number of rows
     */
    fun getRows(): Int {
        return height
    }

    /**
     * Creates a new rect based on this rect, but with a different width
     * @param columns Width of the new rect, in columns
     * @return New rect based on this one, but with a new width
     */
    fun withColumns(columns: Int): TerminalRectangle {
        return TerminalRectangle(x, y, columns, height)
    }

    /**
     * Creates a new rect based on this rect, but with a different height
     * @param rows Height of the new rect, in rows
     * @return New rect based on this one, but with a new height
     */
    fun withRows(rows: Int): TerminalRectangle {
        return TerminalRectangle(x, y, width, rows)
    }

    fun whenContains(p: TerminalPosition?, op: Runnable?): Boolean {
        return whenContains(p!!.getColumn(), p.getRow(), op)
    }

    fun whenContains(x: Int, y: Int, op: Runnable?): Boolean {
        if (this.x <= x && x < this.xAndWidth && this.y <= y && y < this.yAndHeight) {
            op!!.run()
            return true
        }
        return false
    }

    override fun toString(): String {
        return "{x: $x, y: $y, width: $width, height: $height}"
    }

    override fun equals(obj: Any?): Boolean {
        return obj != null &&
            obj.javaClass == javaClass &&
            position == (obj as TerminalRectangle).position &&
            size == obj.size
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + position.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}
