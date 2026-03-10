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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import kotlin.collections.ArrayList
import java.util.IdentityHashMap
import java.util.LinkedList
import java.util.Queue
import java.util.TreeSet

/**
 * This emulates the behaviour of the GridLayout in SWT (as opposed to the one in AWT/Swing). I originally ported the
 * SWT class itself but due to licensing concerns (the eclipse license is not compatible with LGPL) I was advised not to
 * do that. This is a partial implementation and some of the semantics have changed, but in general it works the same
 * way so the SWT documentation will generally match.
 */
class GridLayout(private val numberOfColumns: Int) : LayoutManager {
    enum class Alignment {
        BEGINNING,
        CENTER,
        END,
        FILL,
    }

    internal class GridLayoutData(
        val horizontalAlignment: Alignment,
        val verticalAlignment: Alignment,
        val grabExtraHorizontalSpace: Boolean,
        val grabExtraVerticalSpace: Boolean,
        val horizontalSpan: Int,
        val verticalSpan: Int,
    ) : LayoutData {
        init {
            if (horizontalSpan < 1 || verticalSpan < 1) {
                throw IllegalArgumentException("Horizontal/Vertical span must be 1 or greater")
            }
        }
    }

    private var horizontalSpacing: Int = 1
    private var verticalSpacing: Int = 0
    private var topMarginSize: Int = 0
    private var bottomMarginSize: Int = 0
    private var leftMarginSize: Int = 1
    private var rightMarginSize: Int = 1
    private var changed: Boolean = true

    fun getHorizontalSpacing(): Int {
        return horizontalSpacing
    }

    fun setHorizontalSpacing(horizontalSpacing: Int): GridLayout {
        if (horizontalSpacing < 0) {
            throw IllegalArgumentException("Horizontal spacing cannot be less than 0")
        }
        this.horizontalSpacing = horizontalSpacing
        this.changed = true
        return this
    }

    fun getVerticalSpacing(): Int {
        return verticalSpacing
    }

    fun setVerticalSpacing(verticalSpacing: Int): GridLayout {
        if (verticalSpacing < 0) {
            throw IllegalArgumentException("Vertical spacing cannot be less than 0")
        }
        this.verticalSpacing = verticalSpacing
        this.changed = true
        return this
    }

    fun getTopMarginSize(): Int {
        return topMarginSize
    }

    fun setTopMarginSize(topMarginSize: Int): GridLayout {
        if (topMarginSize < 0) {
            throw IllegalArgumentException("Top margin size cannot be less than 0")
        }
        this.topMarginSize = topMarginSize
        this.changed = true
        return this
    }

    fun getBottomMarginSize(): Int {
        return bottomMarginSize
    }

    fun setBottomMarginSize(bottomMarginSize: Int): GridLayout {
        if (bottomMarginSize < 0) {
            throw IllegalArgumentException("Bottom margin size cannot be less than 0")
        }
        this.bottomMarginSize = bottomMarginSize
        this.changed = true
        return this
    }

    fun getLeftMarginSize(): Int {
        return leftMarginSize
    }

    fun setLeftMarginSize(leftMarginSize: Int): GridLayout {
        if (leftMarginSize < 0) {
            throw IllegalArgumentException("Left margin size cannot be less than 0")
        }
        this.leftMarginSize = leftMarginSize
        this.changed = true
        return this
    }

    fun getRightMarginSize(): Int {
        return rightMarginSize
    }

    fun setRightMarginSize(rightMarginSize: Int): GridLayout {
        if (rightMarginSize < 0) {
            throw IllegalArgumentException("Right margin size cannot be less than 0")
        }
        this.rightMarginSize = rightMarginSize
        this.changed = true
        return this
    }

    override fun hasChanged(): Boolean {
        return this.changed
    }

    override fun getPreferredSize(components: List<Component?>?): TerminalSize? {
        var preferredSize: TerminalSize? = TerminalSize.ZERO
        if (components == null || components.isEmpty()) {
            return preferredSize!!.withRelative(leftMarginSize + rightMarginSize, topMarginSize + bottomMarginSize)
        }

        var table = buildTable(components)
        table = eliminateUnusedRowsAndColumns(table)

        var preferredWidth = 0
        var preferredHeight = 0
        for (width in getPreferredColumnWidths(table)) {
            preferredWidth += width
        }
        for (height in getPreferredRowHeights(table)) {
            preferredHeight += height
        }
        preferredSize = preferredSize!!.withRelative(preferredWidth, preferredHeight)
        preferredSize = preferredSize!!.withRelativeColumns(leftMarginSize + rightMarginSize + (table[0].size - 1) * horizontalSpacing)
        preferredSize = preferredSize!!.withRelativeRows(topMarginSize + bottomMarginSize + (table.size - 1) * verticalSpacing)
        return preferredSize
    }

    override fun doLayout(area: TerminalSize?, components: List<Component?>?) {
        if (area == null || components == null) {
            changed = false
            return
        }

        var localArea: TerminalSize? = area
        var table = buildTable(components)
        table = eliminateUnusedRowsAndColumns(table)

        if (
            localArea == TerminalSize.ZERO ||
            table.isEmpty() ||
            localArea!!.columns <= leftMarginSize + rightMarginSize + ((table[0].size - 1) * horizontalSpacing) ||
            localArea.rows <= bottomMarginSize + topMarginSize + ((table.size - 1) * verticalSpacing)
        ) {
            changed = false
            return
        }

        localArea = localArea!!.withRelative(-leftMarginSize - rightMarginSize, -topMarginSize - bottomMarginSize)

        val sizeMap = IdentityHashMap<Component, TerminalSize?>()
        val positionMap = IdentityHashMap<Component, TerminalPosition?>()

        val columnWidths = getPreferredColumnWidths(table)
        val expandableColumns = getExpandableColumns(table)

        val areaWithoutHorizontalSpacing = localArea!!.withRelativeColumns(-horizontalSpacing * (table[0].size - 1))
        var totalWidth = shrinkWidthToFitArea(areaWithoutHorizontalSpacing, columnWidths)

        while (areaWithoutHorizontalSpacing!!.columns > totalWidth && expandableColumns.isNotEmpty()) {
            totalWidth = grabExtraHorizontalSpace(areaWithoutHorizontalSpacing, columnWidths, expandableColumns, totalWidth)
        }

        val rowHeights = getPreferredRowHeights(table)
        val expandableRows = getExpandableRows(table)
        val areaWithoutVerticalSpacing = localArea.withRelativeRows(-verticalSpacing * (table.size - 1))
        var totalHeight = shrinkHeightToFitArea(areaWithoutVerticalSpacing, rowHeights)
        while (areaWithoutVerticalSpacing!!.rows > totalHeight && expandableRows.isNotEmpty()) {
            totalHeight = grabExtraVerticalSpace(areaWithoutVerticalSpacing, rowHeights, expandableRows, totalHeight)
        }

        var tableCellTopLeft: TerminalPosition? = TerminalPosition.TOP_LEFT_CORNER
        for (y in table.indices) {
            tableCellTopLeft = tableCellTopLeft!!.withColumn(0)
            for (x in table[y].indices) {
                val component = table[y][x]
                if (component != null && !positionMap.containsKey(component)) {
                    val layoutData = getLayoutData(component)
                    var size = component.preferredSize
                    var position = tableCellTopLeft

                    var availableHorizontalSpace = 0
                    var availableVerticalSpace = 0
                    var i = 0
                    while (i < layoutData.horizontalSpan && x + i < columnWidths.size) {
                        availableHorizontalSpace += columnWidths[x + i] + if (i > 0) horizontalSpacing else 0
                        i++
                    }
                    i = 0
                    while (i < layoutData.verticalSpan) {
                        availableVerticalSpace += rowHeights[y + i] + if (i > 0) verticalSpacing else 0
                        i++
                    }

                    size = size!!.withColumns(kotlin.math.min(size.columns, availableHorizontalSpace))
                    size = size!!.withRows(kotlin.math.min(size.rows, availableVerticalSpace))

                    when (layoutData.horizontalAlignment) {
                        Alignment.CENTER -> position = position!!.withRelativeColumn((availableHorizontalSpace - size!!.columns) / 2)
                        Alignment.END -> position = position!!.withRelativeColumn(availableHorizontalSpace - size!!.columns)
                        Alignment.FILL -> size = size!!.withColumns(availableHorizontalSpace)
                        else -> {}
                    }
                    when (layoutData.verticalAlignment) {
                        Alignment.CENTER -> position = position!!.withRelativeRow((availableVerticalSpace - size!!.rows) / 2)
                        Alignment.END -> position = position!!.withRelativeRow(availableVerticalSpace - size!!.rows)
                        Alignment.FILL -> size = size!!.withRows(availableVerticalSpace)
                        else -> {}
                    }

                    sizeMap[component] = size
                    positionMap[component] = position
                }
                tableCellTopLeft = tableCellTopLeft!!.withRelativeColumn(columnWidths[x] + horizontalSpacing)
            }
            tableCellTopLeft = tableCellTopLeft!!.withRelativeRow(rowHeights[y] + verticalSpacing)
        }

        for (component in components) {
            if (component != null) {
                component.setPosition(positionMap[component]!!.withRelative(leftMarginSize, topMarginSize))
                component.setSize(sizeMap[component])
            }
        }
        this.changed = false
    }

    private fun getPreferredColumnWidths(table: Array<Array<Component?>>): IntArray {
        val actualNumberOfColumns = table[0].size
        val columnWidths = IntArray(actualNumberOfColumns)

        for (row in table) {
            for (i in 0 until actualNumberOfColumns) {
                val component = row[i] ?: continue
                val layoutData = getLayoutData(component)
                if (layoutData.horizontalSpan == 1) {
                    columnWidths[i] = kotlin.math.max(columnWidths[i], component.preferredSize!!.columns)
                }
            }
        }

        for (row in table) {
            var i = 0
            while (i < actualNumberOfColumns) {
                val component = row[i]
                if (component == null) {
                    i++
                    continue
                }
                val layoutData = getLayoutData(component)
                val horizontalSpan = kotlin.math.min(layoutData.horizontalSpan, actualNumberOfColumns - i)
                if (horizontalSpan > 1) {
                    var accumWidth = 0
                    for (j in i until i + horizontalSpan) {
                        accumWidth += columnWidths[j]
                    }

                    val preferredWidth = component.preferredSize!!.columns
                    if (preferredWidth > accumWidth) {
                        var columnOffset = 0
                        do {
                            columnWidths[i + columnOffset++]++
                            accumWidth++
                            if (columnOffset == horizontalSpan) {
                                columnOffset = 0
                            }
                        } while (preferredWidth > accumWidth)
                    }
                }
                i += horizontalSpan
            }
        }
        return columnWidths
    }

    private fun getPreferredRowHeights(table: Array<Array<Component?>>): IntArray {
        val numberOfRows = table.size
        val rowHeights = IntArray(numberOfRows)

        var rowIndex = 0
        for (row in table) {
            for (component in row) {
                if (component == null) {
                    continue
                }
                val layoutData = getLayoutData(component)
                if (layoutData.verticalSpan == 1) {
                    rowHeights[rowIndex] = kotlin.math.max(rowHeights[rowIndex], component.preferredSize!!.rows)
                }
            }
            rowIndex++
        }

        for (x in 0 until numberOfColumns) {
            var y = 0
            while (y < numberOfRows) {
                if (x >= table[y].size) {
                    y++
                    continue
                }
                val component = table[y][x]
                if (component == null) {
                    y++
                    continue
                }
                val layoutData = getLayoutData(component)
                if (layoutData.verticalSpan > 1) {
                    var accumulatedHeight = 0
                    for (i in y until y + layoutData.verticalSpan) {
                        accumulatedHeight += rowHeights[i]
                    }

                    val preferredHeight = component.preferredSize!!.rows
                    if (preferredHeight > accumulatedHeight) {
                        var rowOffset = 0
                        do {
                            rowHeights[y + rowOffset++]++
                            accumulatedHeight++
                            if (rowOffset == layoutData.verticalSpan) {
                                rowOffset = 0
                            }
                        } while (preferredHeight > accumulatedHeight)
                    }
                }
                y += layoutData.verticalSpan
            }
        }
        return rowHeights
    }

    private fun getExpandableColumns(table: Array<Array<Component?>>): Set<Int> {
        val expandableColumns = TreeSet<Int>()
        var previousComponent: Component? = null
        for (row in table) {
            for (i in row.indices) {
                if (row[i] == null || row[i] === previousComponent) {
                    continue
                }
                val layoutData = getLayoutData(row[i])
                if (layoutData.grabExtraHorizontalSpace) {
                    expandableColumns.add(i)
                }
                previousComponent = row[i]
            }
        }
        return expandableColumns
    }

    private fun getExpandableRows(table: Array<Array<Component?>>): Set<Int> {
        val expandableRows = TreeSet<Int>()
        var previousComponent: Component? = null
        if (table.isNotEmpty()) {
            for (columnIndex in table[0].indices) {
                for (rowIndex in table.indices) {
                    val cell = table[rowIndex][columnIndex]
                    if (cell == null || cell === previousComponent) {
                        continue
                    }
                    val layoutData = getLayoutData(cell)
                    if (layoutData.grabExtraVerticalSpace) {
                        expandableRows.add(rowIndex)
                    }
                    previousComponent = cell
                }
            }
        }
        return expandableRows
    }

    private fun shrinkWidthToFitArea(area: TerminalSize?, columnWidths: IntArray): Int {
        var totalWidth = 0
        for (width in columnWidths) {
            totalWidth += width
        }
        if (totalWidth > area!!.columns) {
            var columnOffset = 0
            do {
                if (columnWidths[columnOffset] > 0) {
                    columnWidths[columnOffset]--
                    totalWidth--
                }
                if (++columnOffset == columnWidths.size) {
                    columnOffset = 0
                }
            } while (totalWidth > area.columns)
        }
        return totalWidth
    }

    private fun shrinkHeightToFitArea(area: TerminalSize?, rowHeights: IntArray): Int {
        var totalHeight = 0
        for (height in rowHeights) {
            totalHeight += height
        }
        if (totalHeight > area!!.rows) {
            var rowOffset = 0
            do {
                if (rowHeights[rowOffset] > 0) {
                    rowHeights[rowOffset]--
                    totalHeight--
                }
                if (++rowOffset == rowHeights.size) {
                    rowOffset = 0
                }
            } while (totalHeight > area.rows)
        }
        return totalHeight
    }

    private fun grabExtraHorizontalSpace(
        area: TerminalSize?,
        columnWidths: IntArray,
        expandableColumns: Set<Int>,
        totalWidth: Int,
    ): Int {
        var width = totalWidth
        for (columnIndex in expandableColumns) {
            columnWidths[columnIndex]++
            width++
            if (area!!.columns == width) {
                break
            }
        }
        return width
    }

    private fun grabExtraVerticalSpace(
        area: TerminalSize?,
        rowHeights: IntArray,
        expandableRows: Set<Int>,
        totalHeight: Int,
    ): Int {
        var height = totalHeight
        for (rowIndex in expandableRows) {
            rowHeights[rowIndex]++
            height++
            if (area!!.columns == height) {
                break
            }
        }
        return height
    }

    private fun buildTable(components: List<Component?>): Array<Array<Component?>> {
        val rows = ArrayList<Array<Component?>>()
        val hspans = ArrayList<IntArray>()
        val vspans = ArrayList<IntArray>()

        var rowCount = 0
        var rowsExtent = 1
        val toBePlaced: Queue<Component?> = LinkedList(components)
        while (!toBePlaced.isEmpty() || rowCount < rowsExtent) {
            val row = arrayOfNulls<Component>(numberOfColumns)
            val hspan = IntArray(numberOfColumns)
            val vspan = IntArray(numberOfColumns)

            for (i in 0 until numberOfColumns) {
                if (i > 0 && hspan[i - 1] > 1) {
                    row[i] = row[i - 1]
                    hspan[i] = hspan[i - 1] - 1
                    vspan[i] = vspan[i - 1]
                } else if (rowCount > 0 && vspans[rowCount - 1][i] > 1) {
                    row[i] = rows[rowCount - 1][i]
                    hspan[i] = hspans[rowCount - 1][i]
                    vspan[i] = vspans[rowCount - 1][i] - 1
                } else if (!toBePlaced.isEmpty()) {
                    val component = toBePlaced.poll()
                    val gridLayoutData = getLayoutData(component)

                    row[i] = component
                    hspan[i] = gridLayoutData.horizontalSpan
                    vspan[i] = gridLayoutData.verticalSpan
                    rowsExtent = kotlin.math.max(rowsExtent, rowCount + gridLayoutData.verticalSpan)
                } else {
                    row[i] = null
                    hspan[i] = 1
                    vspan[i] = 1
                }
            }

            rows.add(row)
            hspans.add(hspan)
            vspans.add(vspan)
            rowCount++
        }
        return rows.toTypedArray()
    }

    private fun eliminateUnusedRowsAndColumns(table: Array<Array<Component?>>): Array<Array<Component?>> {
        if (table.isEmpty()) {
            return table
        }

        val rowsToRemove = ArrayList<Int>()
        val columnsToRemove = ArrayList<Int>()

        val tableRows = table.size
        val tableColumns = table[0].size

        columnLoop@ for (column in tableColumns - 1 downTo 1) {
            for (row in table) {
                if (row[column] != null) {
                    continue@columnLoop
                }
            }
            columnsToRemove.add(column)
        }

        rowLoop@ for (row in tableRows - 1 downTo 1) {
            for (column in 0 until tableColumns) {
                if (table[row][column] != null) {
                    continue@rowLoop
                }
            }
            rowsToRemove.add(row)
        }

        if (rowsToRemove.isEmpty() && columnsToRemove.isEmpty()) {
            return table
        }

        val newTable = arrayOfNulls<Array<Component?>>(tableRows - rowsToRemove.size)
        var insertedRowCounter = 0
        for (row in table) {
            val newColumn = arrayOfNulls<Component>(tableColumns - columnsToRemove.size)
            var insertedColumnCounter = 0
            for (column in 0 until tableColumns) {
                if (columnsToRemove.contains(column)) {
                    continue
                }
                newColumn[insertedColumnCounter++] = row[column]
            }
            newTable[insertedRowCounter++] = newColumn
        }
        @Suppress("UNCHECKED_CAST")
        return newTable as Array<Array<Component?>>
    }

    private fun getLayoutData(component: Component?): GridLayoutData {
        val layoutData = component?.layoutData
        return if (layoutData is GridLayoutData) {
            layoutData
        } else {
            DEFAULT
        }
    }

    companion object {
        private val DEFAULT = GridLayoutData(
            Alignment.BEGINNING,
            Alignment.BEGINNING,
            false,
            false,
            1,
            1,
        )

                fun createLayoutData(
            horizontalAlignment: Alignment,
            verticalAlignment: Alignment,
            grabExtraHorizontalSpace: Boolean = false,
            grabExtraVerticalSpace: Boolean = false,
            horizontalSpan: Int = 1,
            verticalSpan: Int = 1,
        ): LayoutData {
            return GridLayoutData(
                horizontalAlignment,
                verticalAlignment,
                grabExtraHorizontalSpace,
                grabExtraVerticalSpace,
                horizontalSpan,
                verticalSpan,
            )
        }

        fun createHorizontallyFilledLayoutData(): LayoutData {
            return createLayoutData(
                Alignment.FILL,
                Alignment.CENTER,
                true,
                false,
                1,
                1,
            )
        }

        fun createHorizontallyFilledLayoutData(horizontalSpan: Int): LayoutData {
            return createLayoutData(
                Alignment.FILL,
                Alignment.CENTER,
                true,
                false,
                horizontalSpan,
                1,
            )
        }

        fun createHorizontallyEndAlignedLayoutData(horizontalSpan: Int): LayoutData {
            return createLayoutData(
                Alignment.END,
                Alignment.CENTER,
                true,
                false,
                horizontalSpan,
                1,
            )
        }
    }
}
