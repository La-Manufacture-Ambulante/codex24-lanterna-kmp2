package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal
import org.junit.Before
import org.junit.Test

import java.io.IOException
import java.util.Arrays

import org.junit.Assert.assertEquals

 class TableUnitTests {

private lateinit var terminal: DefaultVirtualTerminal
private lateinit var gui: MultiWindowTextGUI
private lateinit var window: BasicWindow
private lateinit var table: Table<String?>
private lateinit var model: TableModel<String?>

@Before
@Throws(IOException::class)
  fun setUp() {
val size = TerminalSize(30, 24)
terminal = DefaultVirtualTerminal(size)
val screen = TerminalScreen(terminal)
screen.startScreen()
val windowManager = DefaultWindowManager(EmptyWindowDecorationRenderer(), size)
gui = MultiWindowTextGUI(SeparateTextGUIThread.Factory(), screen, windowManager, null, EmptySpace())
window = BasicWindow()
window.setHints(Arrays.asList(Hint.NO_DECORATIONS, Hint.FIT_TERMINAL_WINDOW, Hint.FULL_SCREEN))
table = Table<String?>("a", "b")
window.setComponent(Panel(LinearLayout().setSpacing(0)).addComponent(table, LinearLayout.createLayoutData(LinearLayout.Alignment.FILL)))
gui.addWindow(window)
model = table.getTableModel()
}

@Test
@Throws(Exception::class)
  fun testSimpleTable() {
model!!.addRow("A1", "B1")
assertScreenEquals(("" + 
"a  b\n" + 
"A1 B1"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAndColumns() {
addRowsWithLongSecondColumn(4)
assertScreenEquals(("" + 
"a  b\n" + 
"A1                           ▲\n" + 
"A2                           █\n" + 
"A3                           ▼\n" + 
"◄█████████████▒▒▒▒▒▒▒▒▒▒▒▒▒▒►"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAndColumnsPartially() {
table!!.getRenderer().setAllowPartialColumn(true)
addRowsWithLongSecondColumn(4)
assertScreenEquals(("" + 
"a  b\n" + 
"A1 BBBBBBBBBBBBBBBBBBBBBBBBBB▲\n" + 
"A2 BBBBBBBBBBBBBBBBBBBBBBBBBB█\n" + 
"A3 BBBBBBBBBBBBBBBBBBBBBBBBBB▼\n" + 
"◄█████████████▒▒▒▒▒▒▒▒▒▒▒▒▒▒►"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAndColumnsPartiallyWhenHorizontallyScrolled() {
model = TableModel<String?>("x", "a", "b")
table.setTableModel(this.model)
table.getRenderer().setAllowPartialColumn(true)
table.getRenderer().setViewLeftColumn(1)
addRowsWithLongThirdColumn(4)
assertScreenEquals(("" + 
"a  b\n" + 
"A1 BBBBBBBBBBBBBBBBBBBBBBBBBB▲\n" + 
"A2 BBBBBBBBBBBBBBBBBBBBBBBBBB█\n" + 
"A3 BBBBBBBBBBBBBBBBBBBBBBBBBB▼\n" + 
"◄▒▒▒▒▒▒▒▒▒█████████▒▒▒▒▒▒▒▒▒►"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRows() {
table!!.setVisibleRows(2)
addFourRows()
assertScreenEquals(("" + 
"a  b\n" + 
"A1 B1                        ▲\n" + 
"A2 B2                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAndColumnsWithRestrictedVerticalSpace() {
table!!.setVisibleRows(3)
addRowsWithLongSecondColumn(4)
assertScreenEquals(("" + 
"a  b\n" + 
"A1                           ▲\n" + 
"A2                           ▼\n" + 
"◄█████████████▒▒▒▒▒▒▒▒▒▒▒▒▒▒►"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsWithoutVerticalScrollBar() {
table!!.setVisibleRows(2)
table!!.getRenderer().setScrollBarsHidden(true)
addFourRows()
assertScreenEquals(("" + 
"a  b\n" + 
"A1 B1\n" + 
"A2 B2"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleColumnsWithoutHorizontalScrollBar() {
table!!.setVisibleRows(2)
table!!.getRenderer().setScrollBarsHidden(true)
addRowsWithLongSecondColumn(2)
assertScreenEquals(("" + 
"a  b\n" + 
"A1\n" + 
"A2"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAndColumnsWithoutHorizontalScrollBar() {
table!!.setVisibleRows(2)
table!!.getRenderer().setScrollBarsHidden(true)
addRowsWithLongSecondColumn(4)
assertScreenEquals(("" + 
"a  b\n" + 
"A1\n" + 
"A2"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsWithSelection() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(1)
assertScreenEquals(("" + 
"a  b\n" + 
"A1 B1                        ▲\n" + 
"A2 B2                        ▼"))
table!!.setSelectedRow(2)
assertScreenEquals(("" + 
"a  b\n" + 
"A2 B2                        ▲\n" + 
"A3 B3                        ▼"))
table!!.setSelectedRow(3)
assertScreenEquals(("" + 
"a  b\n" + 
"A3 B3                        ▲\n" + 
"A4 B4                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsWithSelectionOffScreen() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(3)
assertScreenEquals(("" + 
"a  b\n" + 
"A3 B3                        ▲\n" + 
"A4 B4                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsWithSelectionBeyondRowCount() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(300)
assertScreenEquals(("" + 
"a  b\n" + 
"A3 B3                        ▲\n" + 
"A4 B4                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAfterRemovingSelectedRow() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(3)
model!!.removeRow(3)
assertScreenEquals(("" + 
"a  b\n" + 
"A2 B2                        ▲\n" + 
"A3 B3                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAfterInsertingBeforeSelectedRow() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(2)
assertScreenEquals(("" + 
"a  b\n" + 
"A2 B2                        ▲\n" + 
"A3 B3                        ▼"))
model!!.insertRow(0, Arrays.asList("AX", "AX"))
assertScreenEquals(("" + 
"a  b\n" + 
"A2 B2                        ▲\n" + 
"A3 B3                        ▼"))
}

@Test
@Throws(Exception::class)
  fun testRendersVisibleRowsAfterRemovingRowBeforeSelectedRow() {
table!!.setVisibleRows(2)
addFourRows()
table!!.setSelectedRow(3)
model!!.removeRow(0)
assertScreenEquals(("" + 
"a  b\n" + 
"A3 B3                        ▲\n" + 
"A4 B4                        ▼"))
}

 // ---------------- END OF TESTS ----------------

    private fun addFourRows() {
model!!.addRow("A1", "B1")
model!!.addRow("A2", "B2")
model!!.addRow("A3", "B3")
model!!.addRow("A4", "B4")
}

private fun addRowsWithLongSecondColumn(rows:Int) {
for (i in 1..rows)
{
model!!.addRow("A" + i, "BBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + i)
}
}

private fun addRowsWithLongThirdColumn(rows:Int) {
for (i in 1..rows)
{
model!!.addRow("X", "A" + i, "BBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + i)
}
}

@Throws(IOException::class)
private fun assertScreenEquals(expected:String?) {
gui.updateScreen()
assertEquals(expected, stripTrailingNewlines(terminal.toString()))
}

private fun stripTrailingNewlines(s:String):String? {
return s.replaceAll("(?s)[\\s\n]+$", "")
}
}
