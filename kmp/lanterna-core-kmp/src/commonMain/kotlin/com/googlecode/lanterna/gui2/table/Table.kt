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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * The table class is an interactable component that displays a grid of cells containing data along with a header of
 * labels. It supports scrolling when the number of rows and/or columns gets too large to fit and also supports
 * user selection which is either row-based or cell-based. User will move the current selection by using the arrow keys
 * on the keyboard.
 * @param <V> Type of data to store in the table cells, presented through `toString()`
 * @author Martin
</V> */
 class Table<V>/**
 * Creates a new `Table` with the specified table model
 * @param tableModel Table model
 */
    (tableModel:TableModel?):AbstractInteractableComponent<Table<V?>?>() {
private var tableModel:TableModel<V?>? = null
private val tableModelListener:TableModel.Listener<V?>?  // Used to invalidate the table whenever the model changes
private var tableHeaderRenderer:TableHeaderRenderer<V?>? = null
private var tableCellRenderer:TableCellRenderer<V?>? = null
private var selectAction:Runnable? = null
private var cellSelection:Boolean = false
private var visibleRows:Int = 0
private var visibleColumns:Int = 0
private var selectedRow:Int = 0
private var selectedColumn:Int = 0
private var escapeByArrowKey:Boolean = false

/**
 * Returns the index of the row that is currently the first row visible. This is always 0 unless scrolling has been
 * enabled and either the user or the software (through `setViewTopRow(..)`) has scrolled down.
 * @return Index of the row that is currently the first row visible
 */
     val viewTopRow:Int
@Deprecated("Use the table renderers method instead")
get() {
return renderer!!.getViewTopRow()
}

/**
 * Returns the index of the first row that is currently visible.
 * @return the index of the first row that is currently visible
 */
     val firstViewedRowIndex:Int
get() {
return renderer!!.getViewTopRow()
}

/**
 * Returns the index of the last row that is currently visible.
 * @return the index of the last row that is currently visible
 */
     val lastViewedRowIndex:Int
get() {
val visibleRows = renderer!!.getVisibleRowsOnLastDraw()
return Math.min(renderer!!.getViewTopRow() + visibleRows - 1, tableModel!!.getRowCount() - 1)
}

/**
 * Returns the index of the column that is currently the first column visible. This is always 0 unless scrolling has
 * been enabled and either the user or the software (through `setViewLeftColumn(..)`) has scrolled to the
 * right.
 * @return Index of the column that is currently the first column visible
 */
     val viewLeftColumn:Int
@Deprecated("Use the table renderers method instead")
get() {
return renderer!!.getViewLeftColumn()
}

 val renderer:TableRenderer<V?>?
@Override
get() {
return super.getRenderer() as TableRenderer<V?>
}

/**
 * Creates a new `Table` with the number of columns as specified by the array of labels
 * @param columnLabels Creates one column per label in the array, must be more than one
 */
     constructor(vararg columnLabels:String?) : this(TableModel(columnLabels)) {}

init{
this.tableHeaderRenderer = DefaultTableHeaderRenderer()
this.tableCellRenderer = DefaultTableCellRenderer()
this.tableModel = tableModel

this.selectAction = null
this.visibleColumns = 0
this.visibleRows = 0
this.cellSelection = false
this.selectedRow = 0
this.selectedColumn = -1
this.escapeByArrowKey = true

this.tableModelListener = object:TableModel.Listener<V?>() {
@Override
 fun onRowAdded(model:TableModel<V?>?, index:Int) {
if (index <= selectedRow)
{
selectedRow = Math.min(model!!.getRowCount() - 1, selectedRow + 1)
}
invalidate()
}

@Override
 fun onRowRemoved(model:TableModel<V?>?, index:Int, oldRow:List<V?>?) {
if (index < selectedRow)
{
selectedRow = Math.max(0, selectedRow - 1)
}
else
{
 // We may have deleted the selected row
                    val rowCount = model!!.getRowCount()
if (selectedRow > rowCount - 1)
{
selectedRow = Math.max(0, rowCount - 1)
}
}
invalidate()
}

@Override
 fun onColumnAdded(model:TableModel<V?>?, index:Int) {
invalidate()
}

@Override
 fun onColumnRemoved(model:TableModel<V?>?, index:Int, oldHeader:String?, oldColumn:List<V?>?) {
invalidate()
}

@Override
 fun onCellChanged(model:TableModel<V?>?, row:Int, column:Int, oldValue:V?, newValue:V?) {
invalidate()
}
}
this.tableModel!!.addListener(tableModelListener)
}

/**
 * Returns the underlying table model
 * @return Underlying table model
 */
     fun getTableModel():TableModel<V?>? {
return tableModel
}

/**
 * Updates the table with a new table model, effectively replacing the content of the table completely
 * @param tableModel New table model
 * @return Itself
 */
    @Synchronized  fun setTableModel(tableModel:TableModel<V?>?):Table<V?> {
if (tableModel == null)
{
throw IllegalArgumentException("Cannot assign a null TableModel")
}
this.tableModel!!.removeListener(tableModelListener)
this.tableModel = tableModel
this.tableModel!!.addListener(tableModelListener)
invalidate()
return this
}

/**
 * Returns the `TableCellRenderer` used by this table when drawing cells
 * @return `TableCellRenderer` used by this table when drawing cells
 */
     fun getTableCellRenderer():TableCellRenderer<V?>? {
return tableCellRenderer
}

/**
 * Replaces the `TableCellRenderer` used by this table when drawing cells
 * @param tableCellRenderer New `TableCellRenderer` to use
 * @return Itself
 */
    @Synchronized  fun setTableCellRenderer(tableCellRenderer:TableCellRenderer<V?>?):Table<V?> {
this.tableCellRenderer = tableCellRenderer
invalidate()
return this
}

/**
 * Returns the `TableHeaderRenderer` used by this table when drawing the table's header
 * @return `TableHeaderRenderer` used by this table when drawing the table's header
 */
     fun getTableHeaderRenderer():TableHeaderRenderer<V?>? {
return tableHeaderRenderer
}

/**
 * Replaces the `TableHeaderRenderer` used by this table when drawing the table's header
 * @param tableHeaderRenderer New `TableHeaderRenderer` to use
 * @return Itself
 */
    @Synchronized  fun setTableHeaderRenderer(tableHeaderRenderer:TableHeaderRenderer<V?>?):Table<V?> {
this.tableHeaderRenderer = tableHeaderRenderer
invalidate()
return this
}

/**
 * Sets the number of columns this table should show. If there are more columns in the table model, a scrollbar will
 * be used to allow the user to scroll left and right and view all columns.
 * @param visibleColumns Number of columns to display at once
 */
    @Synchronized  fun setVisibleColumns(visibleColumns:Int) {
this.visibleColumns = visibleColumns
invalidate()
}

/**
 * Returns the number of columns this table will show. If there are more columns in the table model, a scrollbar
 * will be used to allow the user to scroll left and right and view all columns.
 * @return Number of visible columns for this table
 */
     fun getVisibleColumns():Int {
return visibleColumns
}

/**
 * Sets the number of rows this table will show. If there are more rows in the table model, a scrollbar will be used
 * to allow the user to scroll up and down and view all rows.
 * @param visibleRows Number of rows to display at once
 */
    @Synchronized  fun setVisibleRows(visibleRows:Int) {
this.visibleRows = visibleRows
invalidate()
}

/**
 * Returns the number of rows this table will show. If there are more rows in the table model, a scrollbar will be
 * used to allow the user to scroll up and down and view all rows.
 * @return Number of rows to display at once
 */
     fun getVisibleRows():Int {
return visibleRows
}

/**
 * Sets the view row offset for the first row to display in the table. Calling this with 0 will make the first row
 * in the model be the first visible row in the table.
 * 
 * @param viewTopRow Index of the row that is currently the first row visible
 * @return Itself
 */
    @Deprecated("Use the table renderers method instead")
@Synchronized  fun setViewTopRow(viewTopRow:Int):Table<V?> {
renderer!!.setViewTopRow(viewTopRow)
return this
}

/**
 * Sets the view column offset for the first column to display in the table. Calling this with 0 will make the first
 * column in the model be the first visible column in the table.
 * 
 * @param viewLeftColumn Index of the column that is currently the first column visible
 * @return Itself
 */
    @Deprecated("Use the table renderers method instead")
@Synchronized  fun setViewLeftColumn(viewLeftColumn:Int):Table<V?> {
renderer!!.setViewLeftColumn(viewLeftColumn)
return this
}

/**
 * Returns the currently selection column index, if in cell-selection mode. Otherwise it returns -1.
 * @return In cell-selection mode returns the index of the selected column, otherwise -1
 */
     fun getSelectedColumn():Int {
return selectedColumn
}

/**
 * If in cell selection mode, updates which column is selected and ensures the selected column is visible in the
 * view. If not in cell selection mode, does nothing.
 * @param selectedColumn Index of the column that should be selected
 * @return Itself
 */
    @Synchronized  fun setSelectedColumn(selectedColumn:Int):Table<V?> {
if (cellSelection)
{
this.selectedColumn = selectedColumn
}
return this
}

/**
 * Returns the index of the currently selected row
 * @return Index of the currently selected row
 */
     fun getSelectedRow():Int {
return selectedRow
}

/**
 * Sets the index of the selected row and ensures the selected row is visible in the view
 * @param selectedRow Index of the row to select
 * @return Itself
 */
    @Synchronized  fun setSelectedRow(selectedRow:Int):Table<V?> {
var selectedRow = selectedRow
if (selectedRow < 0)
{
throw IllegalArgumentException("selectedRow must be >= 0 but was " + selectedRow)
}
val rowCount = tableModel!!.getRowCount()
if (rowCount == 0)
{
selectedRow = 0
}
else if (selectedRow > rowCount - 1)
{
selectedRow = rowCount - 1
}
this.selectedRow = selectedRow
return this
}

/**
 * If `true`, the user will be able to select and navigate individual cells, otherwise the user can only
 * select full rows.
 * @param cellSelection `true` if cell selection should be enabled, `false` for row selection
 * @return Itself
 */
    @Synchronized  fun setCellSelection(cellSelection:Boolean):Table<V?> {
this.cellSelection = cellSelection
if (cellSelection && selectedColumn == -1)
{
selectedColumn = 0
}
else if (!cellSelection)
{
selectedColumn = -1
}
return this
}

/**
 * Returns `true` if this table is in cell-selection mode, otherwise `false`
 * @return `true` if this table is in cell-selection mode, otherwise `false`
 */
     fun isCellSelection():Boolean {
return cellSelection
}

/**
 * Assigns an action to run whenever the user presses the enter or space key while focused on the table. If called with
 * `null`, no action will be run.
 * @param selectAction Action to perform when user presses the enter or space key
 * @return Itself
 */
    @Synchronized  fun setSelectAction(selectAction:Runnable?):Table<V?> {
this.selectAction = selectAction
return this
}

/**
 * Returns `true` if this table can be navigated away from when the selected row is at one of the extremes and
 * the user presses the array key to continue in that direction. With `escapeByArrowKey` set to `true`,
 * this will move focus away from the table in the direction the user pressed, if `false` then nothing will
 * happen.
 * @return `true` if user can switch focus away from the table using arrow keys, `false` otherwise
 */
     fun isEscapeByArrowKey():Boolean {
return escapeByArrowKey
}

/**
 * Sets the flag for if this table can be navigated away from when the selected row is at one of the extremes and
 * the user presses the array key to continue in that direction. With `escapeByArrowKey` set to `true`,
 * this will move focus away from the table in the direction the user pressed, if `false` then nothing will
 * happen.
 * @param escapeByArrowKey `true` if user can switch focus away from the table using arrow keys, `false` otherwise
 * @return Itself
 */
    @Synchronized  fun setEscapeByArrowKey(escapeByArrowKey:Boolean):Table<V?> {
this.escapeByArrowKey = escapeByArrowKey
return this
}

@Override
protected fun createDefaultRenderer():TableRenderer<V?>? {
return DefaultTableRenderer()
}

@Override
 fun handleKeyStroke(keyStroke:KeyStroke):Result? {
when (keyStroke.getKeyType()) {
ARROW_UP -> if (selectedRow > 0)
{
selectedRow--
}
else if (escapeByArrowKey)
{
return Result.MOVE_FOCUS_UP
}
ARROW_DOWN -> if (selectedRow < tableModel!!.getRowCount() - 1)
{
selectedRow++
}
else if (escapeByArrowKey)
{
return Result.MOVE_FOCUS_DOWN
}
PAGE_UP -> if (renderer!!.getVisibleRowsOnLastDraw() > 0 && selectedRow > 0)
{
selectedRow -= Math.min(renderer!!.getVisibleRowsOnLastDraw() - 1, selectedRow)
}
PAGE_DOWN -> if (renderer!!.getVisibleRowsOnLastDraw() > 0 && selectedRow < tableModel!!.getRowCount() - 1)
{
val toEndDistance = tableModel!!.getRowCount() - 1 - selectedRow
selectedRow += Math.min(renderer!!.getVisibleRowsOnLastDraw() - 1, toEndDistance)
}
HOME -> selectedRow = 0
END -> selectedRow = tableModel!!.getRowCount() - 1
ARROW_LEFT -> if (cellSelection && selectedColumn > 0)
{
selectedColumn--
}
else if (escapeByArrowKey)
{
return Result.MOVE_FOCUS_LEFT
}
ARROW_RIGHT -> if (cellSelection && selectedColumn < tableModel!!.getColumnCount() - 1)
{
selectedColumn++
}
else if (escapeByArrowKey)
{
return Result.MOVE_FOCUS_RIGHT
}
CHARACTER, ENTER -> if (isKeyboardActivationStroke(keyStroke))
{
val runnable = selectAction   //To avoid synchronizing
if (runnable != null)
{
runnable!!.run()
}
else
{
return Result.HANDLED
}
break
}
else
{
return super.handleKeyStroke(keyStroke)
}
MOUSE_EVENT -> {
val action = keyStroke as MouseAction
val actionType = action!!.getActionType()
if (actionType === MouseActionType.MOVE)
{
 // do nothing
                    return Result.UNHANDLED
}
if (!isFocused())
{
super.handleKeyStroke(keyStroke)
}
val mouseRow = getRowByMouseAction(keyStroke as MouseAction)
val mouseColumn = getColumnByMouseAction(keyStroke as MouseAction)
val isDifferentCell = mouseRow != selectedRow || mouseColumn != selectedColumn
selectedRow = mouseRow
selectedColumn = mouseColumn
if (isDifferentCell)
{
return handleKeyStroke(KeyStroke(KeyType.ENTER))
}
}
else -> return super.handleKeyStroke(keyStroke)
}
invalidate()
return Result.HANDLED
}

/**
 * By converting [TerminalPosition]s to
 * [.toGlobal] gets row clicked on by mouse action.
 * 
 * @return row of a table that was clicked on with [MouseAction]
 */
    protected fun getRowByMouseAction(mouseAction:MouseAction):Int {
val minPossible = firstViewedRowIndex
val maxPossible = lastViewedRowIndex
val mouseSpecified = mouseAction.getPosition().getRow() - getGlobalPosition().getRow() - 1

return Math.max(minPossible, Math.min(mouseSpecified, maxPossible))
}

/**
 * By converting [TerminalPosition]s to
 * [.toGlobal] and by comparing widths of column
 * headers, gets column clicked on by mouse action.
 * 
 * @return row of a table that was clicked on with [MouseAction]
 */
    protected fun getColumnByMouseAction(mouseAction:MouseAction):Int {
val maxColumnIndex = tableModel!!.getColumnCount() - 1
var column = 0
var columnSize = tableHeaderRenderer!!.getPreferredSize(this, tableModel!!.getColumnLabel(column), column).getColumns()
var globalColumnMoused = mouseAction.getPosition().getColumn() - getGlobalPosition().getColumn()
while (globalColumnMoused - columnSize - 1 >= 0 && column < maxColumnIndex)
{
globalColumnMoused -= columnSize
column++
columnSize = tableHeaderRenderer!!.getPreferredSize(this, tableModel!!.getColumnLabel(column), column).getColumns()
}
return column
}

}
