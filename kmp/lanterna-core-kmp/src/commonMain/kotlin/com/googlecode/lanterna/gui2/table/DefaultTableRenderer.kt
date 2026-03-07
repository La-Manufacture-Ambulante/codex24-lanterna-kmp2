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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.ScrollBar
import com.googlecode.lanterna.gui2.TextGUIGraphics

import java.util.ArrayList
import java.util.TreeSet

/**
 * Default implementation of `TableRenderer`
 * @param <V> Type of data stored in each table cell
 * @author Martin
</V> */
 class DefaultTableRenderer<V>:TableRenderer<V?> {

private val verticalScrollBar:ScrollBar?
private val horizontalScrollBar:ScrollBar?

private var headerVerticalBorderStyle:TableCellBorderStyle? = null
private var headerHorizontalBorderStyle:TableCellBorderStyle? = null
private var cellVerticalBorderStyle:TableCellBorderStyle? = null
private var cellHorizontalBorderStyle:TableCellBorderStyle? = null

 var viewTopRow:Int = 0
 var viewLeftColumn:Int = 0
/**
 * Returns the number of rows that could be drawn on the last draw operation. If the table doesn't have any visible
 * row count set through [Table.setVisibleRows], this is the only way to find out exactly how large the
 * table ended up. But even if you did set the number of visible rows explicitly, due to terminal size constraints
 * the actually drawn size might have been different.
 * @return Number of rows that could be drawn on the last UI update call, will also return 0 if the table has not
 * yet been drawn out
 */
     var visibleRowsOnLastDraw:Int = 0
private set

 //So that we don't have to recalculate the size every time. This still isn't optimal but shouganai.
    private var cachedSize:TerminalSize? = null
private val preferredColumnSizes:List<Integer?>?
private val preferredRowSizes:List<Integer?>?
private val expandableColumns:Set<Integer?>?
private var headerSizeInRows:Int = 0
@get:Override
@set:Override
 var allowPartialColumn:Boolean = false

@get:Override
@set:Override
 var isScrollBarsHidden:Boolean = false

private val isHorizontallySpaced:Boolean
get() {
return (headerHorizontalBorderStyle !== TableCellBorderStyle.NONE || cellHorizontalBorderStyle !== TableCellBorderStyle.NONE)
}
/**
 * Default constructor
 */
    init{
verticalScrollBar = ScrollBar(Direction.VERTICAL)
horizontalScrollBar = ScrollBar(Direction.HORIZONTAL)

headerVerticalBorderStyle = TableCellBorderStyle.NONE
headerHorizontalBorderStyle = TableCellBorderStyle.EMPTY_SPACE
cellVerticalBorderStyle = TableCellBorderStyle.NONE
cellHorizontalBorderStyle = TableCellBorderStyle.EMPTY_SPACE

viewTopRow = 0
viewLeftColumn = 0
visibleRowsOnLastDraw = 0
allowPartialColumn = false
isScrollBarsHidden = false

cachedSize = null

preferredColumnSizes = ArrayList()
preferredRowSizes = ArrayList()
expandableColumns = TreeSet()
headerSizeInRows = 0
}

/**
 * Sets the style to be used when separating the table header row from the actual "data" cells below. This will
 * cause a new line to be added under the header labels, unless set to `TableCellBorderStyle.None`.
 * 
 * @param headerVerticalBorderStyle Style to use to separate Table header from body
 */
    @Synchronized  fun setHeaderVerticalBorderStyle(headerVerticalBorderStyle:TableCellBorderStyle?) {
this.headerVerticalBorderStyle = headerVerticalBorderStyle
}

/**
 * Sets the style to be used when separating the table header labels from each other. This will cause a new
 * column to be added in between each label, unless set to `TableCellBorderStyle.None`.
 * 
 * @param headerHorizontalBorderStyle Style to use when separating header columns horizontally
 */
    @Synchronized  fun setHeaderHorizontalBorderStyle(headerHorizontalBorderStyle:TableCellBorderStyle?) {
this.headerHorizontalBorderStyle = headerHorizontalBorderStyle
}

/**
 * Sets the style to be used when vertically separating table cells from each other. This will cause a new line
 * to be added between every row, unless set to `TableCellBorderStyle.None`.
 * 
 * @param cellVerticalBorderStyle Style to use to separate table cells vertically
 */
    @Synchronized  fun setCellVerticalBorderStyle(cellVerticalBorderStyle:TableCellBorderStyle?) {
this.cellVerticalBorderStyle = cellVerticalBorderStyle
}

/**
 * Sets the style to be used when horizontally separating table cells from each other. This will cause a new
 * column to be added between every row, unless set to `TableCellBorderStyle.None`.
 * 
 * @param cellHorizontalBorderStyle Style to use to separate table cells horizontally
 */
    @Synchronized  fun setCellHorizontalBorderStyle(cellHorizontalBorderStyle:TableCellBorderStyle?) {
this.cellHorizontalBorderStyle = cellHorizontalBorderStyle
}

/**
 * Sets the list of columns (by index, where 0 is the first column) that can be expanded, should the drawable area
 * be larger than the table is requesting.
 * @param expandableColumns Collection of indexes for expandable columns
 */
    @Synchronized  fun setExpandableColumns(expandableColumns:Collection<Integer?>?) {
this.expandableColumns!!.clear()
this.expandableColumns!!.addAll(expandableColumns)
}

@Override
@Synchronized  fun getPreferredSize(table:Table<V?>):TerminalSize {
 //Quick bypass if the table hasn't changed
        if (!table.isInvalid() && cachedSize != null)
{
return cachedSize
}

val tableModel = table.getTableModel()

 // Copy these so we don't modify the renderers state
        var viewLeftColumn = this.viewLeftColumn
var viewTopRow = this.viewTopRow
var visibleColumns = table.getVisibleColumns()
var visibleRows = table.getVisibleRows()
val selectedRow = table.getSelectedRow()
val selectedColumn = table.getSelectedColumn()
val rows = tableModel!!.getRows()
val columnHeaders = tableModel!!.getColumnLabels()
val tableHeaderRenderer = table.getTableHeaderRenderer()
val tableCellRenderer = table.getTableCellRenderer()

if (visibleColumns == 0)
{
visibleColumns = tableModel!!.getColumnCount()
}
if (visibleRows == 0)
{
visibleRows = tableModel!!.getRowCount()
}

preferredColumnSizes!!.clear()
preferredRowSizes!!.clear()

if (tableModel!!.getColumnCount() === 0)
{
return TerminalSize.ZERO
}

 // Adjust view port if necessary (although this is only for the preferred size calculation, we don't actually
        // update the view model)
        if (selectedColumn != -1 && viewLeftColumn > selectedColumn)
{
viewLeftColumn = selectedColumn
}
else if (selectedColumn != 1 && viewLeftColumn <= selectedColumn - visibleColumns)
{
viewLeftColumn = Math.max(0, selectedColumn - visibleColumns + 1)
}
if (viewTopRow > selectedRow)
{
viewTopRow = selectedRow
}
else if (viewTopRow <= selectedRow - visibleRows)
{
viewTopRow = Math.max(0, selectedRow - visibleRows + 1)
}

 // If there are no rows, base the column sizes off of the column labels
        if (rows!!.size() === 0)
{
for (columnIndex in 0 until columnHeaders!!.size())
{
val columnSize = tableHeaderRenderer!!.getPreferredSize(table, columnHeaders!!.get(columnIndex), columnIndex).getColumns()
if (preferredColumnSizes!!.size() === columnIndex)
{
preferredColumnSizes!!.add(columnSize)
}
else
{
if (preferredColumnSizes!!.get(columnIndex) < columnSize)
{
preferredColumnSizes!!.set(columnIndex, columnSize)
}
}
}
}

for (rowIndex in 0 until rows!!.size())
{
val row = rows!!.get(rowIndex)
for (columnIndex in 0 until row!!.size())
{
val cell = row!!.get(columnIndex)
val columnSize = tableCellRenderer!!.getPreferredSize(table, cell, columnIndex, rowIndex).getColumns()
if (preferredColumnSizes!!.size() === columnIndex)
{
preferredColumnSizes!!.add(columnSize)
}
else
{
if (preferredColumnSizes!!.get(columnIndex) < columnSize)
{
preferredColumnSizes!!.set(columnIndex, columnSize)
}
}
}

 //Do the headers too, on the first iteration
            if (rowIndex == 0)
{
for (columnIndex in 0 until row!!.size())
{
val columnSize = tableHeaderRenderer!!.getPreferredSize(table, columnHeaders!!.get(columnIndex), columnIndex).getColumns()
if (preferredColumnSizes!!.size() === columnIndex)
{
preferredColumnSizes!!.add(columnSize)
}
else
{
if (preferredColumnSizes!!.get(columnIndex) < columnSize)
{
preferredColumnSizes!!.set(columnIndex, columnSize)
}
}
}
}
}

for (columnIndex in 0 until columnHeaders!!.size())
{
for (rowIndex in 0 until rows!!.size())
{
val cell = rows!!.get(rowIndex).get(columnIndex)
val rowSize = tableCellRenderer!!.getPreferredSize(table, cell, columnIndex, rowIndex).getRows()
if (preferredRowSizes!!.size() === rowIndex)
{
preferredRowSizes!!.add(rowSize)
}
else
{
if (preferredRowSizes!!.get(rowIndex) < rowSize)
{
preferredRowSizes!!.set(rowIndex, rowSize)
}
}
}
}

var preferredRowSize = 0
var preferredColumnSize = 0
if (table.getVisibleColumns() === 0)
{
for (columnSize in preferredColumnSizes!!)
{
preferredColumnSize += columnSize!!.toInt()
}
}
else
{
for (columnIndex in viewLeftColumn until Math.min(preferredColumnSizes!!.size(), viewLeftColumn + visibleColumns))
{
preferredColumnSize += preferredColumnSizes!!.get(columnIndex)
}
}

if (table.getVisibleRows() === 0)
{
for (rowIndex in 0 until rows!!.size())
{
preferredRowSize += preferredRowSizes!!.get(rowIndex)
}
}
else
{
for (rowIndex in viewTopRow until Math.min(rows!!.size(), viewTopRow + visibleRows))
{
preferredRowSize += preferredRowSizes!!.get(rowIndex)
}
}

headerSizeInRows = 0
for (columnIndex in 0 until columnHeaders!!.size())
{
val headerRows = tableHeaderRenderer!!.getPreferredSize(table, columnHeaders!!.get(columnIndex), columnIndex).getRows()
if (headerSizeInRows < headerRows)
{
headerSizeInRows = headerRows
}
}
preferredRowSize += headerSizeInRows

if (headerVerticalBorderStyle !== TableCellBorderStyle.NONE)
{
preferredRowSize++    //Spacing between header and body
}
if (cellVerticalBorderStyle !== TableCellBorderStyle.NONE)
{
if (!rows!!.isEmpty())
{
preferredRowSize += Math.min(rows!!.size(), visibleRows) - 1 //Vertical space between cells
}
}
if (isHorizontallySpaced)
{
if (!columnHeaders!!.isEmpty())
{
preferredColumnSize += Math.min(tableModel!!.getColumnCount(), visibleColumns) - 1    //Spacing between the columns
}
}

if (!isScrollBarsHidden)
{
 //Add one space taken by scrollbars (we always add one for the vertical scrollbar but for the horizontal only if
            // we think we need one). Unfortunately we don't know the size constraints at this point so we don't know if the
            // table will need to force scrollbars or not. We might think that we don't need a horizontal scrollbar here but
            // it might turn out that we need it.
            preferredColumnSize++
if (visibleColumns < tableModel!!.getColumnCount())
{
preferredRowSize++
}
}

cachedSize = TerminalSize(preferredColumnSize, preferredRowSize)
return cachedSize
}

@Override
 fun getCursorLocation(component:Table<V?>?):TerminalPosition? {
return null
}

@Override
@Synchronized  fun drawComponent(graphics:TextGUIGraphics, table:Table<V?>?) {
 //Get the size
        val area = graphics.getSize()

 //Don't even bother
        if (area!!.rows == 0 || area!!.columns == 0)
{
return 
}

 // Get preferred size if the table model has changed
        if (table!!.isInvalid())
{
getPreferredSize(table!!)
}

val headerSizeIncludingBorder = headerSizeInRows + headerVerticalBorderStyle!!.getSize()
val selectedColumn = table!!.getSelectedColumn()
val selectedRow = table!!.getSelectedRow()

 // Update view port if necessary
        if (selectedColumn != -1 && viewLeftColumn > selectedColumn)
{
viewLeftColumn = selectedColumn
}
if (viewTopRow > selectedRow)
{
viewTopRow = selectedRow
}
if (viewTopRow >= table!!.getTableModel().getRowCount())
{
viewTopRow = Math.max(0, table!!.getTableModel().getRowCount() - 1)
}

var areaWithoutScrollBars = area!!.withRelativeRows(-headerSizeIncludingBorder)
var preferredVisibleRows = table!!.getVisibleRows()
if (preferredVisibleRows == 0)
{
preferredVisibleRows = table!!.getTableModel().getRowCount()
}
var preferredVisibleColumns = table!!.getVisibleColumns()
if (preferredVisibleColumns == 0)
{
preferredVisibleColumns = table!!.getTableModel().getColumnCount()
}

var visibleRows = calculateVisibleRows(areaWithoutScrollBars!!, viewTopRow, preferredVisibleRows)
var needVerticalScrollBar = !isScrollBarsHidden && visibleRows < table!!.getTableModel().getRowCount()
if (needVerticalScrollBar)
{
areaWithoutScrollBars = areaWithoutScrollBars!!.withRelativeColumns(-verticalScrollBar!!.getPreferredSize().getColumns())
}
var visibleColumns = calculateVisibleColumns(areaWithoutScrollBars!!, viewLeftColumn, preferredVisibleColumns)
val needHorizontalScrollBar = !isScrollBarsHidden && visibleColumns < table!!.getTableModel().getColumnCount()
if (needHorizontalScrollBar)
{
areaWithoutScrollBars = areaWithoutScrollBars!!.withRelativeRows(-horizontalScrollBar!!.getPreferredSize().getRows())

 // As we have now a horizontal scrollbar, we need to re-evaluate how many rows are visible
            visibleRows = calculateVisibleRows(areaWithoutScrollBars!!, viewTopRow, preferredVisibleRows)
if (!needVerticalScrollBar && visibleRows < table!!.getTableModel().getRowCount())
{
 // Previously we didn't need a scrollbar but now we do because the horizontal scrollbar took one row
                needVerticalScrollBar = true
areaWithoutScrollBars = areaWithoutScrollBars!!.withRelativeColumns(-verticalScrollBar!!.getPreferredSize().getColumns())

 // Also recalculate visible columns to take into consideration the new vertical scrollbar
                visibleColumns = calculateVisibleColumns(areaWithoutScrollBars!!, viewLeftColumn, preferredVisibleColumns)
}
}

 // Now that we know (roughly) how many rows fit, update view port again if necessary
        while (selectedColumn != 1 && viewLeftColumn <= selectedColumn - visibleColumns)
{
viewLeftColumn = Math.max(0, selectedColumn - visibleColumns + 1)
visibleColumns = calculateVisibleColumns(areaWithoutScrollBars!!, viewLeftColumn, preferredVisibleColumns)
}
while (viewTopRow <= selectedRow - visibleRows)
{
viewTopRow = Math.max(0, selectedRow - visibleRows + 1)
visibleRows = calculateVisibleRows(areaWithoutScrollBars!!, viewTopRow, preferredVisibleRows)
}

val renderColumns:Int
if (allowPartialColumn && visibleColumns < preferredVisibleColumns - viewLeftColumn)
{
renderColumns = visibleColumns + 1
}
else
{
renderColumns = visibleColumns
}

val columnSizes = fitColumnsInAvailableSpace(table!!, areaWithoutScrollBars, visibleColumns)
drawHeader(graphics, table!!, columnSizes)
drawRows(graphics.newTextGraphics(
TerminalPosition(0, headerSizeIncludingBorder), 
 // Can't use areaWithoutScrollBars here because we need to draw the scrollbar too!
                        area!!.withRelativeRows(-headerSizeIncludingBorder)), 
table!!, 
columnSizes, 
visibleRows, 
visibleColumns, 
renderColumns, 
needVerticalScrollBar, 
needHorizontalScrollBar)

visibleRowsOnLastDraw = visibleRows
}

private fun calculateVisibleRows(area:TerminalSize, viewTopRow:Int, preferredVisibleRows:Int):Int {
var remainingVerticalSpace = area.rows
var visibleRows = 0
val borderAdjustment = cellVerticalBorderStyle!!.getSize()
for (row in viewTopRow until preferredRowSizes!!.size())
{
if (preferredVisibleRows == visibleRows)
{
break
}
val rowSize = preferredRowSizes!!.get(row) + borderAdjustment
if (remainingVerticalSpace < rowSize)
{
break
}
remainingVerticalSpace -= rowSize
visibleRows++
}
return visibleRows
}

private fun calculateVisibleColumns(area:TerminalSize, viewLeftColumn:Int, preferredVisibleColumns:Int):Int {
var remainingHorizontalSpace = area.columns
var visibleColumns = 0
val borderAdjustment = cellHorizontalBorderStyle!!.getSize()
for (column in viewLeftColumn until preferredColumnSizes!!.size())
{
if (preferredVisibleColumns == visibleColumns)
{
break
}
val columnSize = preferredColumnSizes!!.get(column) + (if (column > viewLeftColumn) borderAdjustment else 0)
if (remainingHorizontalSpace < columnSize)
{
break
}
remainingHorizontalSpace -= columnSize
visibleColumns++
}
return visibleColumns
}

private fun fitColumnsInAvailableSpace(table:Table<V?>, area:TerminalSize?, visibleColumns:Int):List<Integer?> {
val columnSizes = ArrayList(preferredColumnSizes)
var horizontalSpaceRequirement = 0
val viewLeftColumn = table.getRenderer().getViewLeftColumn()
val headers = table.getTableModel().getColumnLabels()
val endColumnIndex = Math.min(headers!!.size(), viewLeftColumn + visibleColumns)
val visibleExpandableColumns = ArrayList()
for (index in viewLeftColumn until endColumnIndex)
{
horizontalSpaceRequirement += preferredColumnSizes!!.get(index)
if (headerHorizontalBorderStyle !== TableCellBorderStyle.NONE && index < (endColumnIndex - 1))
{
horizontalSpaceRequirement += headerHorizontalBorderStyle!!.getSize()
}
if (expandableColumns!!.contains(index))
{
visibleExpandableColumns.add(index)
}
}
var extraHorizontalSpace = area!!.columns - horizontalSpaceRequirement
while (extraHorizontalSpace > 0 && !visibleExpandableColumns.isEmpty())
{
for (expandableColumnIndex in visibleExpandableColumns)
{
columnSizes.set(expandableColumnIndex, columnSizes.get(expandableColumnIndex) + 1)
extraHorizontalSpace--
if (extraHorizontalSpace == 0)
{
break
}
}
}
return columnSizes
}

private fun drawHeader(graphics:TextGUIGraphics?, table:Table<V?>, columnSizes:List<Integer?>?) {
val theme = table.getTheme()
val tableHeaderRenderer = table.getTableHeaderRenderer()
val headers = table.getTableModel().getColumnLabels()
val viewLeftColumn = table.getRenderer().getViewLeftColumn()
var visibleColumns = table.getVisibleColumns()
if (visibleColumns == 0)
{
visibleColumns = table.getTableModel().getColumnCount()
}
var leftPosition = 0
val endColumnIndex = Math.min(headers!!.size(), viewLeftColumn + visibleColumns)
for (index in viewLeftColumn until endColumnIndex)
{
val label = headers!!.get(index)
val size = TerminalSize(columnSizes!!.get(index), headerSizeInRows)
tableHeaderRenderer!!.drawHeader(table, label, index, graphics!!.newTextGraphics(TerminalPosition(leftPosition, 0), size))
leftPosition += size.columns
if (headerHorizontalBorderStyle !== TableCellBorderStyle.NONE && index < (endColumnIndex - 1))
{
graphics!!.applyThemeStyle(theme!!.getDefinition(Table::class.java).getNormal())
graphics!!.setCharacter(leftPosition, 0, getVerticalCharacter(headerHorizontalBorderStyle!!))
leftPosition++
}
}

if (headerVerticalBorderStyle !== TableCellBorderStyle.NONE)
{
leftPosition = 0
val topPosition = headerSizeInRows
graphics!!.applyThemeStyle(theme!!.getDefinition(Table::class.java).getNormal())
for (i in viewLeftColumn until endColumnIndex)
{
if (i > viewLeftColumn)
{
graphics!!.setCharacter(
leftPosition, 
topPosition, 
getJunctionCharacter(
headerVerticalBorderStyle, 
headerHorizontalBorderStyle, 
cellHorizontalBorderStyle))
leftPosition++
}
val columnWidth = columnSizes!!.get(i)
graphics!!.drawLine(leftPosition, topPosition, leftPosition + columnWidth - 1, topPosition, getHorizontalCharacter(headerVerticalBorderStyle!!))
leftPosition += columnWidth
}
 //Expand out the line in case the area is bigger
            if (leftPosition < graphics!!.getSize().getColumns())
{
graphics!!.drawLine(leftPosition, topPosition, graphics!!.getSize().getColumns() - 1, topPosition, getHorizontalCharacter(headerVerticalBorderStyle!!))
}
}
}

private fun drawRows(
graphics:TextGUIGraphics, 
table:Table<V?>, 
columnSizes:List<Integer?>?, 
visibleRows:Int, 
visibleColumns:Int, 
renderColumns:Int, 
needVerticalScrollBar:Boolean, 
needHorizontalScrollBar:Boolean) {
var graphics = graphics
val theme = table.getTheme()
val themeDefinition = theme!!.getDefinition(Table::class.java)
val area = graphics.getSize()
val tableCellRenderer = table.getTableCellRenderer()
val tableModel = table.getTableModel()
val rows = tableModel!!.getRows()
val viewTopRow = table.getRenderer().getViewTopRow()
val viewLeftColumn = table.getRenderer().getViewLeftColumn()

 //Draw scrollbars (if needed)
        if (needVerticalScrollBar)
{
val verticalScrollBarPreferredSize = verticalScrollBar!!.getPreferredSize()
var scrollBarHeight = graphics.getSize().getRows()
if (needHorizontalScrollBar)
{
scrollBarHeight--
}
verticalScrollBar!!.setPosition(TerminalPosition(graphics.getSize().getColumns() - verticalScrollBarPreferredSize!!.columns, 0))
verticalScrollBar!!.setSize(verticalScrollBarPreferredSize!!.withRows(scrollBarHeight))
verticalScrollBar!!.setScrollMaximum(rows!!.size())
verticalScrollBar!!.setViewSize(visibleRows)
verticalScrollBar!!.setScrollPosition(viewTopRow)

 // Ensure the parent is correct
            if (table.getParent() !== verticalScrollBar!!.getParent())
{
if (verticalScrollBar!!.getParent() != null)
{
verticalScrollBar!!.onRemoved(verticalScrollBar!!.getParent())
}
if (table.getParent() != null)
{
verticalScrollBar!!.onAdded(table.getParent())
}
}

 // Finally draw the thing
            verticalScrollBar!!.draw(graphics.newTextGraphics(verticalScrollBar!!.getPosition(), verticalScrollBar!!.getSize()))

 // Adjust graphics object to the remaining area when the vertical scrollbar is subtracted
            graphics = graphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, graphics.getSize().withRelativeColumns(-verticalScrollBarPreferredSize!!.columns))
}
if (needHorizontalScrollBar)
{
val horizontalScrollBarPreferredSize = horizontalScrollBar!!.getPreferredSize()
val scrollBarWidth = graphics.getSize().getColumns()
horizontalScrollBar!!.setPosition(TerminalPosition(0, graphics.getSize().getRows() - horizontalScrollBarPreferredSize!!.rows))
horizontalScrollBar!!.setSize(horizontalScrollBarPreferredSize!!.withColumns(scrollBarWidth))
horizontalScrollBar!!.setScrollMaximum(tableModel!!.getColumnCount())
horizontalScrollBar!!.setViewSize(visibleColumns)
horizontalScrollBar!!.setScrollPosition(viewLeftColumn)

 // Ensure the parent is correct
            if (table.getParent() !== horizontalScrollBar!!.getParent())
{
if (horizontalScrollBar!!.getParent() != null)
{
horizontalScrollBar!!.onRemoved(horizontalScrollBar!!.getParent())
}
if (table.getParent() != null)
{
horizontalScrollBar!!.onAdded(table.getParent())
}
}

 // Finally draw the thing
            horizontalScrollBar!!.draw(graphics.newTextGraphics(horizontalScrollBar!!.getPosition(), horizontalScrollBar!!.getSize()))

 // Adjust graphics object to the remaining area when the horizontal scrollbar is subtracted
            graphics = graphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, graphics.getSize().withRelativeRows(-horizontalScrollBarPreferredSize!!.rows))
}

var topPosition = 0
for (rowIndex in viewTopRow until Math.min(viewTopRow + visibleRows, rows!!.size()))
{
var leftPosition = 0
val row = rows!!.get(rowIndex)
for (columnIndex in viewLeftColumn until Math.min(viewLeftColumn + renderColumns, row!!.size()))
{
if (columnIndex > viewLeftColumn)
{
if (table.getSelectedRow() === rowIndex && !table.isCellSelection())
{
if (table.isFocused())
{
graphics.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics.applyThemeStyle(themeDefinition!!.getSelected())
}
}
else
{
graphics.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics.setCharacter(leftPosition, topPosition, getVerticalCharacter(cellHorizontalBorderStyle!!))
leftPosition++
}
val cell = row!!.get(columnIndex)
val cellPosition = TerminalPosition(leftPosition, topPosition)
val cellArea = TerminalSize(columnSizes!!.get(columnIndex), preferredRowSizes!!.get(rowIndex))
tableCellRenderer!!.drawCell(table, cell, columnIndex, rowIndex, graphics.newTextGraphics(cellPosition, cellArea))
leftPosition += cellArea.columns

if (columnIndex < row!!.size() - 1)
{
if (table.getSelectedRow() === rowIndex && !table.isCellSelection())
{
if (table.isFocused())
{
graphics.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics.applyThemeStyle(themeDefinition!!.getSelected())
}
}
else
{
graphics.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics.setCharacter(leftPosition, topPosition, getVerticalCharacter(cellHorizontalBorderStyle!!))
}

if (leftPosition > area!!.columns)
{
break
}
}
topPosition += preferredRowSizes!!.get(rowIndex)
if (cellVerticalBorderStyle !== TableCellBorderStyle.NONE)
{
leftPosition = 0
graphics.applyThemeStyle(themeDefinition!!.getNormal())
for (i in viewLeftColumn until Math.min(viewLeftColumn + renderColumns + 1, row!!.size()))
{
if (i > viewLeftColumn)
{
graphics.setCharacter(
leftPosition, 
topPosition, 
getJunctionCharacter(
cellVerticalBorderStyle, 
cellHorizontalBorderStyle, 
cellHorizontalBorderStyle))
leftPosition++
}
val columnWidth = columnSizes!!.get(i)
graphics.drawLine(leftPosition, topPosition, leftPosition + columnWidth - 1, topPosition, getHorizontalCharacter(cellVerticalBorderStyle!!))
leftPosition += columnWidth
}
topPosition += cellVerticalBorderStyle!!.getSize()
}
if (topPosition > area!!.rows)
{
break
}
}
}

private fun getHorizontalCharacter(style:TableCellBorderStyle):Char {
when (style) {
SINGLE_LINE -> return Symbols.SINGLE_LINE_HORIZONTAL
DOUBLE_LINE -> return Symbols.DOUBLE_LINE_HORIZONTAL
else -> return ' '
}
}

private fun getVerticalCharacter(style:TableCellBorderStyle):Char {
when (style) {
SINGLE_LINE -> return Symbols.SINGLE_LINE_VERTICAL
DOUBLE_LINE -> return Symbols.DOUBLE_LINE_VERTICAL
else -> return ' '
}
}

private fun getJunctionCharacter(mainStyle:TableCellBorderStyle?, styleAbove:TableCellBorderStyle?, styleBelow:TableCellBorderStyle?):Char {
if (mainStyle === TableCellBorderStyle.SINGLE_LINE)
{
if (styleAbove === TableCellBorderStyle.SINGLE_LINE)
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
return Symbols.SINGLE_LINE_CROSS
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
 //There isn't any character for this, give upper side priority
                    return Symbols.SINGLE_LINE_T_UP
}
else
{
return Symbols.SINGLE_LINE_T_UP
}
}
else if (styleAbove === TableCellBorderStyle.DOUBLE_LINE)
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
 //There isn't any character for this, give upper side priority
                    return Symbols.SINGLE_LINE_T_DOUBLE_UP
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
return Symbols.DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS
}
else
{
return Symbols.SINGLE_LINE_T_DOUBLE_UP
}
}
else
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
return Symbols.SINGLE_LINE_T_DOWN
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
return Symbols.SINGLE_LINE_T_DOUBLE_DOWN
}
else
{
return Symbols.SINGLE_LINE_HORIZONTAL
}
}
}
else if (mainStyle === TableCellBorderStyle.DOUBLE_LINE)
{
if (styleAbove === TableCellBorderStyle.SINGLE_LINE)
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
return Symbols.DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
 //There isn't any character for this, give upper side priority
                    return Symbols.DOUBLE_LINE_T_SINGLE_UP
}
else
{
return Symbols.DOUBLE_LINE_T_SINGLE_UP
}
}
else if (styleAbove === TableCellBorderStyle.DOUBLE_LINE)
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
 //There isn't any character for this, give upper side priority
                    return Symbols.DOUBLE_LINE_T_UP
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
return Symbols.DOUBLE_LINE_CROSS
}
else
{
return Symbols.DOUBLE_LINE_T_UP
}
}
else
{
if (styleBelow === TableCellBorderStyle.SINGLE_LINE)
{
return Symbols.DOUBLE_LINE_T_SINGLE_DOWN
}
else if (styleBelow === TableCellBorderStyle.DOUBLE_LINE)
{
return Symbols.DOUBLE_LINE_T_DOWN
}
else
{
return Symbols.DOUBLE_LINE_HORIZONTAL
}
}
}
else
{
return ' '
}
}
}
