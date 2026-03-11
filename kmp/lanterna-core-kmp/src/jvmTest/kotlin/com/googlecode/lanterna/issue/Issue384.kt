package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.DialogWindow
import com.googlecode.lanterna.gui2.table.DefaultTableRenderer
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

import java.io.IOException
import java.util.Collections
import java.util.TreeSet

 object Issue384 {
private val EXPANDABLE_COLUMNS = TreeSet(Collections.singletonList(1))

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val screen = DefaultTerminalFactory().createScreen()
screen.startScreen()
val textGUI = MultiWindowTextGUI(screen)
val window = BasicWindow("Table container test")
window.setHints(Collections.singletonList(Window.Hint.FIXED_SIZE))
window.setFixedSize(TerminalSize(60, 14))

val table = Table<String?>("Column", "Expanded Column", "Column")
table.setCellSelection(true)
table.setVisibleRows(10)
val tableRenderer = DefaultTableRenderer<String?>()
tableRenderer.setExpandableColumns(Collections.singletonList(1))
table.setRenderer(tableRenderer)

val model = table.getTableModel()
for (i in 1..20)
{
val cellLabel = "Row" + i
model!!.addRow(cellLabel, cellLabel, cellLabel)
}

val buttonPanel = Panel()
buttonPanel.setLayoutManager(LinearLayout(Direction.HORIZONTAL))
buttonPanel.addComponent(Button("Change Expandable Columns", { showExpandableColumnsEditor(textGUI, tableRenderer) }))
buttonPanel.addComponent(Button("Close", Runnable { window.close() }))

window.component = Panels.vertical(
table.withBorder(Borders.singleLineBevel("Table")), 
buttonPanel)
table.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL))
textGUI.addWindow(window)
textGUI.waitForWindowToClose(window)
screen.stopScreen()
}

private fun showExpandableColumnsEditor(textGUI:MultiWindowTextGUI?, tableRenderer:DefaultTableRenderer<String?>?) {
val dialogWindow = object:DialogWindow("Select expandable columns") {

}
val contentPanel = Panel(LinearLayout(Direction.VERTICAL))
val checkBoxList = CheckBoxList<String?>()
checkBoxList.addItem("Column1", EXPANDABLE_COLUMNS.contains(0))
checkBoxList.addItem("Column2", EXPANDABLE_COLUMNS.contains(1))
checkBoxList.addItem("Column3", EXPANDABLE_COLUMNS.contains(2))
contentPanel.addComponent(checkBoxList)
contentPanel.addComponent(Button("OK", { EXPANDABLE_COLUMNS.clear()
for (i in 0..2)
{
if (checkBoxList.isChecked(i) == true)
{
EXPANDABLE_COLUMNS.add(i)
}
}
tableRenderer!!.setExpandableColumns(EXPANDABLE_COLUMNS)
dialogWindow.close() }), LinearLayout.createLayoutData(LinearLayout.Alignment.END))
dialogWindow.component = contentPanel
dialogWindow.showDialog(textGUI!!)
}
}
