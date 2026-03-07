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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.dialogs.*
import com.googlecode.lanterna.gui2.table.DefaultTableCellRenderer
import com.googlecode.lanterna.gui2.table.DefaultTableRenderer
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableCellBorderStyle
import com.googlecode.lanterna.gui2.table.TableModel

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.regex.Pattern

/**
 * Test for the Table component
 */
 class TableTest:TestBase() {

private var columnCounter = 4

@Override
 fun init(textGUI:WindowBasedTextGUI?) {
val window = BasicWindow("Table container test")
window.setHints(Collections.singletonList(Window.Hint.FIT_TERMINAL_WINDOW))

val table = Table("Column 1", "Column 2", "Column 3")
table.setTableCellRenderer(object:DefaultTableCellRenderer<String?>() {
@Override
protected fun applyStyle(table:Table<String?>?, cell:String?, columnIndex:Int, rowIndex:Int, isSelected:Boolean, textGUIGraphics:TextGUIGraphics?) {
super.applyStyle(table, cell, columnIndex, rowIndex, isSelected, textGUIGraphics)
if (columnIndex == 1)
{
textGUIGraphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
textGUIGraphics!!.setForegroundColor(TextColor.ANSI.GREEN)
if (isSelected)
{
textGUIGraphics!!.enableModifiers(SGR.REVERSE)
}
}
}
})
val model = table.getTableModel()
for (i in 1..29)
{
model!!.addRow("Row" + i, "Row" + i, "Row" + i)
}

val buttonPanel = Panel()
buttonPanel.setLayoutManager(LinearLayout(Direction.HORIZONTAL))

buttonPanel.addComponent(Button("Add...", { ActionListDialogBuilder()
.setTitle("Add to table")
.addAction("Row", { val labels = ArrayList()
for (i in 0 until model!!.getColumnCount())
{
labels.add("Row" + (model!!.getRowCount() + 1))
}
model!!.addRow(labels.toArray(arrayOfNulls<String?>(0)))
table.invalidate() })
.addAction("5 Rows", { for (row in 0..4)
{
val labels = ArrayList()
for (i in 0 until model!!.getColumnCount())
{
labels.add("Row" + (model!!.getRowCount() + 1))
}
model!!.addRow(labels.toArray(arrayOfNulls<String?>(0)))
}
table.invalidate() })
.addAction("Column", { val labels = ArrayList()
for (i in 0 until model!!.getRowCount())
{
labels.add("Row" + (i + 1))
}
model!!.addColumn("Column " + (columnCounter++), labels.toArray(arrayOfNulls<String?>(0)))
table.invalidate() })
.build()
.showDialog(textGUI) }))
buttonPanel.addComponent(Button("Modify...", { onModify(textGUI, table) }))
buttonPanel.addComponent(Button("Remove...", { ActionListDialogBuilder()
.setTitle("Remove from table")
.addAction("Row", { val numberAsText = askForANumber(textGUI, "Enter row # to remove (0-" + (model!!.getRowCount() - 1) + ")")
if (numberAsText != null)
{
model!!.removeRow(Integer.parseInt(numberAsText))
} })
.addAction("Column", { val numberAsText = askForANumber(textGUI, "Enter column # to remove (0-" + (model!!.getColumnCount() - 1) + ")")
if (numberAsText != null)
{
model!!.removeColumn(Integer.parseInt(numberAsText))
} })
.build()
.showDialog(textGUI) }))
buttonPanel.addComponent(Button("Close", ???({ window.close() })))

window.setComponent(Panels.vertical(
table.withBorder(Borders.singleLineBevel("Table")), 
buttonPanel))
textGUI!!.addWindow(window)
}

private fun onModify(textGUI:WindowBasedTextGUI?, table:Table<String?>?) {
val dialogChoices = arrayOf<String?>("Change table content", "Change table style", "Change view size", "Force re-calculate/re-draw")
val choice = chooseAString(textGUI, "Modify what?", *dialogChoices)

        if (choice == null)
{}
else if (choice!!.equals(dialogChoices[0]))
{
onModifyContent(textGUI, table!!)
}
else if (choice!!.equals(dialogChoices[1]))
{
onModifyStyle(textGUI, table!!)
}
else if (choice!!.equals(dialogChoices[2]))
{
onModifyViewSize(textGUI, table)
}
else if (choice!!.equals(dialogChoices[3]))
{
table!!.invalidate()
}
}

private fun onModifyContent(textGUI:WindowBasedTextGUI?, table:Table<String?>) {
val model = table.getTableModel()
val columnIndexAsText = askForANumber(textGUI, "Enter column # to modify (0-" + (model!!.getColumnCount() - 1) + ")")
if (columnIndexAsText == null)
{
return 
}
val rowIndexAsText = askForANumber(textGUI, "Enter row # to modify (0-" + (model!!.getRowCount() - 1) + ")")
if (rowIndexAsText == null)
{
return 
}
val newLabel = askForAString(textGUI, "Enter new label for the table cell at row " + rowIndexAsText + " column " + columnIndexAsText)
if (newLabel != null)
{
model!!.setCell(Integer.parseInt(columnIndexAsText), Integer.parseInt(rowIndexAsText), newLabel)
}
}

private fun onModifyStyle(textGUI:WindowBasedTextGUI?, table:Table<String?>) {
val dialogChoices = arrayOf<String?>("Header border style (vertical)", "Header border style (horizontal)", "Cell border style (vertical)", "Cell border style (horizontal)", "Toggle cell selection")
val choice = chooseAString(textGUI, "Which style do you want to change?", *dialogChoices)
val renderer = table.getRenderer() as DefaultTableRenderer<String?>
if (choice == null)
{
return 
}
else if (choice!!.equals(dialogChoices[4]))
{
table.setCellSelection(!table.isCellSelection())
}
else
{
val newStyle = ListSelectDialogBuilder<TableCellBorderStyle?>()
.setTitle("Choose a new style")
.addListItems(TableCellBorderStyle.values())
.build()
.showDialog(textGUI)
if (newStyle != null)
{
if (choice!!.equals(dialogChoices[0]))
{
renderer!!.setHeaderVerticalBorderStyle(newStyle)
}
else if (choice!!.equals(dialogChoices[1]))
{
renderer!!.setHeaderHorizontalBorderStyle(newStyle)
}
else if (choice!!.equals(dialogChoices[2]))
{
renderer!!.setCellVerticalBorderStyle(newStyle)
}
else if (choice!!.equals(dialogChoices[3]))
{
renderer!!.setCellHorizontalBorderStyle(newStyle)
}
}
}
table.invalidate()
}

private fun onModifyViewSize(textGUI:WindowBasedTextGUI?, table:Table<String?>?) {
val verticalViewSize = askForANumber(textGUI, "Enter number of rows to display at once (0 = all)")
if (verticalViewSize == null)
{
return 
}
table!!.setVisibleRows(Integer.parseInt(verticalViewSize))
val horizontalViewSize = askForANumber(textGUI, "Enter number of columns to display at once (0 = all)")
if (horizontalViewSize == null)
{
return 
}
table!!.setVisibleColumns(Integer.parseInt(horizontalViewSize))
}

private fun chooseAString(textGUI:WindowBasedTextGUI?, title:String?, vararg items:String?):String? {
return ListSelectDialogBuilder<String?>()
.setTitle(title)
.addListItems(items)
.build()
.showDialog(textGUI)
}

private fun askForAString(textGUI:WindowBasedTextGUI?, title:String?):String? {
return TextInputDialogBuilder()
.setTitle(title)
.build()
.showDialog(textGUI)
}

private fun askForANumber(textGUI:WindowBasedTextGUI?, title:String?, initialNumber:String? = ""):String? {
return TextInputDialogBuilder()
.setTitle(title)
.setInitialContent(initialNumber)
.setValidationPattern(Pattern.compile("[0-9]+"), "Not a number")
.build()
.showDialog(textGUI)
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
TableTest().run(args)
}
}
}
