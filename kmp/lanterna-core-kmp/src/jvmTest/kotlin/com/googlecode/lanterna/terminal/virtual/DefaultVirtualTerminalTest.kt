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
package com.googlecode.lanterna.terminal.virtual

import com.googlecode.lanterna.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.Terminal
import org.junit.Test

import java.util.Arrays
import java.util.Collections
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert.*

 class DefaultVirtualTerminalTest {
private val virtualTerminal:DefaultVirtualTerminal?
init{
this.virtualTerminal = DefaultVirtualTerminal()
}

@Test
  fun initialTerminalStateIsAsExpected() {
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
val terminalSize = virtualTerminal!!.terminalSize
assertEquals(TerminalSize(80, 24), terminalSize)

for (row in 0 until terminalSize!!.rows)
{
for (column in 0 until terminalSize!!.columns)
{
assertEquals(DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(column, row))
}
}
}

@Test
  fun simpleTestOutputTest() {
val testString = "Hello World!"
for (c in testString.toCharArray())
{
virtualTerminal!!.putCharacter(c)
}
assertLineEquals(testString, 0)
assertLineEquals("", 1)
assertEquals(TerminalPosition(testString.length, 0), virtualTerminal!!.cursorPosition)
}

@Test
  fun multiLineTextTest() {
val toPrint = arrayOf("Hello", "Hallo", "Hallå", "こんにちは")
for (string in toPrint)
{
for (c in string!!.toCharArray())
{
virtualTerminal!!.putCharacter(c)
}
virtualTerminal!!.putCharacter('\n')
}
for (i in toPrint.indices)
{
assertLineEquals(toPrint[i], i)
}
assertEquals(TerminalPosition(0, toPrint.size), virtualTerminal!!.cursorPosition)
}

@Test
  fun singleLineWriteAndReadBackWorks() {
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter(TextCharacter('H'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('O'))
assertEquals(TerminalPosition.TOP_LEFT_CORNER!!.withColumn(5), virtualTerminal!!.cursorPosition)
assertEquals('H', virtualTerminal!!.getCharacter(TerminalPosition(0, 0))!!.characterString[0])
assertEquals('E', virtualTerminal!!.getCharacter(TerminalPosition(1, 0))!!.characterString[0])
assertEquals('L', virtualTerminal!!.getCharacter(TerminalPosition(2, 0))!!.characterString[0])
assertEquals('L', virtualTerminal!!.getCharacter(TerminalPosition(3, 0))!!.characterString[0])
assertEquals('O', virtualTerminal!!.getCharacter(TerminalPosition(4, 0))!!.characterString[0])

assertFalse(virtualTerminal!!.isWholeBufferDirtyThenReset)
assertEquals(TreeSet(Arrays.asList(
TerminalPosition(0, 0), 
TerminalPosition(1, 0), 
TerminalPosition(2, 0), 
TerminalPosition(3, 0), 
TerminalPosition(4, 0))), 
virtualTerminal!!.andResetDirtyCells)

 // Make sure it's reset
        assertEquals(emptySet<TerminalPosition>(), virtualTerminal!!.andResetDirtyCells)
}

@Test
  fun clearAllMarksEverythingAsDirtyAndEverythingInTheTerminalIsReplacedWithDefaultCharacter() {
virtualTerminal!!.setTerminalSize(TerminalSize(10, 5))
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter(TextCharacter('H'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('O'))
virtualTerminal!!.clearScreen()

assertTrue(virtualTerminal!!.isWholeBufferDirtyThenReset)
assertEquals(emptySet<TerminalPosition>(), virtualTerminal!!.andResetDirtyCells)

assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(0, 0)))
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(1, 0)))
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(2, 0)))
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(3, 0)))
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(4, 0)))
}

@Test
  fun replacingAllContentTriggersWholeTerminalIsDirty() {
virtualTerminal!!.setTerminalSize(TerminalSize(5, 3))
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter(TextCharacter('H'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('O'))
virtualTerminal!!.putCharacter(TextCharacter('H'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('O'))
virtualTerminal!!.putCharacter(TextCharacter('B'))
virtualTerminal!!.putCharacter(TextCharacter('Y'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('!'))

assertTrue(virtualTerminal!!.isWholeBufferDirtyThenReset)
assertEquals(emptySet<TerminalPosition>(), virtualTerminal!!.andResetDirtyCells)
}

@Test
  fun tooLongLinesWrap() {
virtualTerminal!!.setTerminalSize(TerminalSize(5, 5))
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter(TextCharacter('H'))
virtualTerminal!!.putCharacter(TextCharacter('E'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('L'))
virtualTerminal!!.putCharacter(TextCharacter('O'))
virtualTerminal!!.putCharacter(TextCharacter('!'))
assertEquals(TerminalPosition.OFFSET_1x1, virtualTerminal!!.cursorPosition)

 // Expected layout:
        // |HELLO|
        // |!    |
        // where the cursor is one column after the '!'
    }

@Test
  fun makeSureDoubleWidthCharactersWrapProperly() {
virtualTerminal!!.setTerminalSize(TerminalSize(9, 5))
assertEquals(TerminalPosition.TOP_LEFT_CORNER, virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter(TextCharacter('こ'))
virtualTerminal!!.putCharacter(TextCharacter('ん'))
virtualTerminal!!.putCharacter(TextCharacter('に'))
virtualTerminal!!.putCharacter(TextCharacter('ち'))
virtualTerminal!!.putCharacter(TextCharacter('は'))
virtualTerminal!!.putCharacter(TextCharacter('!'))
assertEquals(TerminalPosition(3, 1), virtualTerminal!!.cursorPosition)

 // Expected layout:
        // |こんにち|
        // |は!    |
        // where the cursor is one column after the '!' (2 + 1 = 3rd column)

        // Make sure there's a default padding character at 8x0
        assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(8, 0)))
}

@Test
  fun overwritingDoubleWidthCharactersEraseTheOtherHalf() {
virtualTerminal!!.setTerminalSize(TerminalSize(5, 5))
virtualTerminal!!.putCharacter(TextCharacter('画'))
virtualTerminal!!.putCharacter(TextCharacter('面'))

assertEquals('画', virtualTerminal!!.getCharacter(TerminalPosition(0, 0))!!.characterString[0])
assertEquals('画', virtualTerminal!!.getCharacter(TerminalPosition(1, 0))!!.characterString[0])
assertEquals('面', virtualTerminal!!.getCharacter(TerminalPosition(2, 0))!!.characterString[0])
assertEquals('面', virtualTerminal!!.getCharacter(TerminalPosition(3, 0))!!.characterString[0])

virtualTerminal!!.cursorPosition = TerminalPosition(0, 0)
virtualTerminal!!.putCharacter(TextCharacter('Y'))

assertEquals('Y', virtualTerminal!!.getCharacter(TerminalPosition(0, 0))!!.characterString[0])
assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(1, 0)))

virtualTerminal!!.cursorPosition = TerminalPosition(3, 0)
virtualTerminal!!.putCharacter(TextCharacter('V'))

assertEquals(TextCharacter.DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(TerminalPosition(2, 0)))
assertEquals('V', virtualTerminal!!.getCharacter(TerminalPosition(3, 0))!!.characterString[0])
}

@Test
  fun testCursorPositionUpdatesWhenTerminalSizeChanges() {
virtualTerminal!!.setTerminalSize(TerminalSize(3, 3))
virtualTerminal!!.putCharacter('\n')
virtualTerminal!!.putCharacter('\n')
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter('\n')
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter('\n')
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)

 // Shrink viewport
        virtualTerminal!!.setTerminalSize(TerminalSize(3, 2))
assertEquals(TerminalPosition(0, 1), virtualTerminal!!.cursorPosition)

 // Restore
        virtualTerminal!!.setTerminalSize(TerminalSize(3, 3))
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)

 // Enlarge
        virtualTerminal!!.setTerminalSize(TerminalSize(3, 4))
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorPosition)
virtualTerminal!!.setTerminalSize(TerminalSize(3, 5))
assertEquals(TerminalPosition(0, 4), virtualTerminal!!.cursorPosition)

 // We've reached the total size of the buffer, enlarging it further shouldn't affect the cursor position
        virtualTerminal!!.setTerminalSize(TerminalSize(3, 6))
assertEquals(TerminalPosition(0, 4), virtualTerminal!!.cursorPosition)
virtualTerminal!!.setTerminalSize(TerminalSize(3, 7))
assertEquals(TerminalPosition(0, 4), virtualTerminal!!.cursorPosition)
}

@Test
  fun textScrollingOutOfTheBacklogDisappears() {
virtualTerminal!!.setTerminalSize(TerminalSize(10, 3))
 // Backlog of 1, meaning viewport size + 1 row
        virtualTerminal!!.setBacklogSize(1)
putString("Line 1\n")
assertEquals(TerminalPosition(0, 1), virtualTerminal!!.cursorPosition)
assertEquals(virtualTerminal!!.cursorPosition, virtualTerminal!!.cursorBufferPosition)
putString("Line 2\n")
putString("Line 3\n")
putString("Line 4\n") // This should knock out "Line 1"

 // Expected content:
        //(|Line 1    | <- discarded)
        // ------------
        // |Line 2    | <- backlog
        // ------------
        // |Line 3    | <- viewport
        // |Line 4    | <- viewport
        // |          | <- viewport

        assertBufferLineEquals("Line 2", 0)
assertBufferLineEquals("Line 3", 1)
assertLineEquals("Line 3", 0)
assertLineEquals("Line 4", 1)
assertLineEquals("", 2)
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorBufferPosition)

 // Make terminal bigger
        virtualTerminal!!.setTerminalSize(TerminalSize(10, 4))

 // Now "Line 2" should be the top row
        assertLineEquals("Line 2", 0)
assertLineEquals("Line 3", 1)
assertLineEquals("Line 4", 2)
assertLineEquals("", 3)
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorPosition)
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorBufferPosition)

 // Make it even bigger
        virtualTerminal!!.setTerminalSize(TerminalSize(10, 5))

 // Should make no difference, the viewport will add an empty row at the end, because there is nothing in the
        // backlog to insert at the top
        assertLineEquals("Line 2", 0)
assertLineEquals("Line 3", 1)
assertLineEquals("Line 4", 2)
assertLineEquals("", 3)
assertLineEquals("", 4)
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorPosition)
assertEquals(TerminalPosition(0, 3), virtualTerminal!!.cursorBufferPosition)
}

@Test
  fun backlogTrimmingAdjustsCursorPositionAndDirtyCells() {
virtualTerminal!!.setTerminalSize(TerminalSize(80, 3))
virtualTerminal!!.setBacklogSize(0)
virtualTerminal!!.putCharacter(fromChar('A'))
virtualTerminal!!.cursorPosition = TerminalPosition(1, 1)
virtualTerminal!!.putCharacter(fromChar('B'))
virtualTerminal!!.cursorPosition = TerminalPosition(2, 2)
virtualTerminal!!.putCharacter(fromChar('C'))

assertLineEquals("A", 0)
assertLineEquals(" B", 1)
assertLineEquals("  C", 2)

 // Dirty positions should now be these
        assertEquals(TreeSet(Arrays.asList(
TerminalPosition(0, 0), 
TerminalPosition(1, 1), 
TerminalPosition(2, 2))), virtualTerminal!!.dirtyCells)
assertEquals(TerminalPosition(3, 2), virtualTerminal!!.cursorPosition)

 // Add one more row to shift out the first line
        virtualTerminal!!.putCharacter('\n')

 // Dirty positions should now be adjusted
        assertEquals(TreeSet(Arrays.asList(
TerminalPosition(1, 0), 
TerminalPosition(2, 1))), virtualTerminal!!.dirtyCells)
assertEquals(TerminalPosition(0, 2), virtualTerminal!!.cursorPosition)
}

@Test
  fun testPrivateMode() {
val ROWS = 5
virtualTerminal!!.setTerminalSize(TerminalSize(20, ROWS))
for (i in 1..ROWS + 2)
{
putString("Line " + i + "\n")
}
assertEquals(TerminalPosition(0, ROWS - 1), virtualTerminal!!.cursorPosition)
assertEquals(TerminalPosition(0, ROWS + 2), virtualTerminal!!.cursorBufferPosition)

virtualTerminal!!.enterPrivateMode()
assertEquals(TerminalPosition(0, 0), virtualTerminal!!.cursorPosition)
assertEquals(TerminalPosition(0, 0), virtualTerminal!!.cursorBufferPosition)
for (i in 0 until ROWS)
{
assertLineEquals("", i)
}

 // There should be no backlog in private mode
        for (i in 1..ROWS + 4)
{
putString("Line " + i + "\n")
}
for (i in 0 until ROWS - 1)
{
assertLineEquals("Line " + (i + 6), i)
}
assertLineEquals("", ROWS - 1)
assertEquals(5, virtualTerminal!!.bufferLineCount.toLong())

virtualTerminal!!.exitPrivateMode()
for (i in 0 until ROWS - 1)
{
assertLineEquals("Line " + (i + 4), i)
}
assertLineEquals("", ROWS - 1)
}

@Test
  fun testForEachLine() {
val ROWS = 40
virtualTerminal!!.setTerminalSize(TerminalSize(10, 5))
for (i in 1..ROWS)
{
putString("Line " + i + "\n")
}
virtualTerminal!!.forEachLine(0, ROWS, object : VirtualTerminal.BufferWalker {
override fun onLine(rowNumber: Int, bufferLine: VirtualTerminal.BufferLine?) {
if (rowNumber == ROWS) {
assertLineEquals("", bufferLine)
} else {
assertLineEquals("Line " + (rowNumber + 1), bufferLine)
}
}
})
}

@Test
  fun testColorAndSGR() {
virtualTerminal!!.putCharacter('A')
virtualTerminal!!.setBackgroundColor(TextColor.ANSI.BLUE)
virtualTerminal!!.setForegroundColor(TextColor.ANSI.WHITE)
virtualTerminal!!.putCharacter('B')
virtualTerminal!!.enableSGR(SGR.BOLD)
virtualTerminal!!.enableSGR(SGR.UNDERLINE)
virtualTerminal!!.putCharacter('C')
virtualTerminal!!.disableSGR(SGR.BOLD)
virtualTerminal!!.putCharacter('D')
virtualTerminal!!.resetColorAndSGR()
virtualTerminal!!.putCharacter('E')

assertEquals(TextCharacter.DEFAULT_CHARACTER!!.withCharacter('A'), virtualTerminal!!.getCharacter(0, 0))
assertEquals(TextCharacter('B', TextColor.ANSI.WHITE, TextColor.ANSI.BLUE), virtualTerminal!!.getCharacter(1, 0))
assertEquals(TextCharacter('C', TextColor.ANSI.WHITE, TextColor.ANSI.BLUE, SGR.BOLD, SGR.UNDERLINE), virtualTerminal!!.getCharacter(2, 0))
assertEquals(TextCharacter('D', TextColor.ANSI.WHITE, TextColor.ANSI.BLUE, SGR.UNDERLINE), virtualTerminal!!.getCharacter(3, 0))
assertEquals(TextCharacter.DEFAULT_CHARACTER!!.withCharacter('E'), virtualTerminal!!.getCharacter(4, 0))
}

@Test
  fun testTabExpansion() {
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(0, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("    XXXXXXXXXXXXXX", 0)

virtualTerminal!!.clearScreen()
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(1, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("X   XXXXXXXXXXXXXX", 0)

virtualTerminal!!.clearScreen()
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(2, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("XX  XXXXXXXXXXXXXX", 0)

virtualTerminal!!.clearScreen()
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(3, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("XXX XXXXXXXXXXXXXX", 0)

virtualTerminal!!.clearScreen()
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(4, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("XXXX    XXXXXXXXXX", 0)

virtualTerminal!!.clearScreen()
putString("XXXXXXXXXXXXXXXXXX")
virtualTerminal!!.setCursorPosition(5, 0)
virtualTerminal!!.putCharacter('\t')
assertLineEquals("XXXXX   XXXXXXXXXX", 0)
}

@Test
  fun testInput() {
val keyStroke1 = KeyStroke('A', false, false)
val keyStroke2 = KeyStroke('B', false, false)
virtualTerminal!!.addInput(keyStroke1)
virtualTerminal!!.addInput(keyStroke2)
assertEquals(keyStroke1, virtualTerminal!!.pollInput())
assertEquals(keyStroke2, virtualTerminal!!.readInput())
}

@Test
  fun testVirtualTerminalListener() {
val flushCounter = AtomicInteger(0)
val bellCounter = AtomicInteger(0)
val resizeCounter = AtomicInteger(0)
val closeCounter = AtomicInteger(0)

val listener = object:VirtualTerminalListener {
 public override fun onFlush() {
flushCounter.incrementAndGet()
}

 public override fun onBell() {
bellCounter.incrementAndGet()
}

public override fun onResized(terminal:Terminal?, newSize:TerminalSize?) {
resizeCounter.incrementAndGet()
}

 public override fun onClose() {
closeCounter.incrementAndGet()
}
}

virtualTerminal!!.flush()
virtualTerminal!!.bell()
virtualTerminal!!.setTerminalSize(TerminalSize(40, 10))
assertEquals(0, flushCounter.get())
assertEquals(0, bellCounter.get())
assertEquals(0, resizeCounter.get())
assertEquals(0, closeCounter.get())

virtualTerminal!!.addVirtualTerminalListener(listener)
virtualTerminal!!.flush()
virtualTerminal!!.bell()
virtualTerminal!!.setTerminalSize(TerminalSize(80, 20))
assertEquals(1, flushCounter.get())
assertEquals(1, bellCounter.get())
assertEquals(1, resizeCounter.get())
assertEquals(0, closeCounter.get())

virtualTerminal!!.close()
assertEquals(1, closeCounter.get())

virtualTerminal!!.removeVirtualTerminalListener(listener)
virtualTerminal!!.flush()
virtualTerminal!!.bell()
virtualTerminal!!.setTerminalSize(TerminalSize(40, 10))
virtualTerminal!!.close()
assertEquals(1, flushCounter.get())
assertEquals(1, bellCounter.get())
assertEquals(1, resizeCounter.get())
assertEquals(1, closeCounter.get())
}

@Test
  fun settingCursorOutsideOfTerminalWindowWillBeAdjusted() {
virtualTerminal!!.setTerminalSize(TerminalSize(10, 5))
virtualTerminal!!.setCursorPosition(20, 10)
assertEquals(TerminalPosition(9, 4), virtualTerminal!!.cursorPosition)

virtualTerminal!!.setCursorPosition(0, 10)
assertEquals(TerminalPosition(0, 4), virtualTerminal!!.cursorPosition)

virtualTerminal!!.setCursorPosition(20, 0)
assertEquals(TerminalPosition(9, 0), virtualTerminal!!.cursorPosition)
}

@Test
  fun puttingCharacterInLastColumnDoesntMoveCursorToNextLine() {
virtualTerminal!!.setTerminalSize(TerminalSize(10, 5))
virtualTerminal!!.setCursorPosition(8, 2)
assertEquals(TerminalPosition(8, 2), virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter('A')
assertEquals(TerminalPosition(9, 2), virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter('B')
assertEquals(TerminalPosition(10, 2), virtualTerminal!!.cursorPosition)
virtualTerminal!!.putCharacter('C')
assertEquals(TerminalPosition(1, 3), virtualTerminal!!.cursorPosition)
assertEquals(DEFAULT_CHARACTER!!.withCharacter('C'), virtualTerminal!!.getCharacter(0, 3))
}

private fun putString(string:String) {
for (c in string.toCharArray())
{
virtualTerminal!!.putCharacter(c)
}
}

private fun fromChar(c:Char):TextCharacter {
return TextCharacter(c)
}

private fun assertLineEquals(expectedLineContent:String, rowNumber:Int) {
var column = 0
for (c in expectedLineContent.toCharArray())
{
assertEquals(DEFAULT_CHARACTER!!.withCharacter(c), virtualTerminal!!.getCharacter(column++, rowNumber))
if (TerminalTextUtils.isCharDoubleWidth(c))
{
column++
}
}
while (column < virtualTerminal!!.terminalSize!!.columns)
{
assertEquals(DEFAULT_CHARACTER, virtualTerminal!!.getCharacter(column++, rowNumber))
}
}

private fun assertBufferLineEquals(expectedBufferLineContent:String, rowNumber:Int) {
var column = 0
for (c in expectedBufferLineContent.toCharArray())
{
assertEquals(DEFAULT_CHARACTER!!.withCharacter(c), virtualTerminal!!.getBufferCharacter(TerminalPosition(column++, rowNumber)))
if (TerminalTextUtils.isCharDoubleWidth(c))
{
column++
}
}
while (column < virtualTerminal!!.terminalSize!!.columns)
{
assertEquals(DEFAULT_CHARACTER, virtualTerminal!!.getBufferCharacter(column++, rowNumber))
}
}

private fun assertLineEquals(expectedLineContent:String, line:VirtualTerminal.BufferLine?) {
var column = 0
for (c in expectedLineContent.toCharArray())
{
assertEquals(DEFAULT_CHARACTER!!.withCharacter(c), line!!.getCharacterAt(column++))
if (TerminalTextUtils.isCharDoubleWidth(c))
{
column++
}
}
while (column < virtualTerminal!!.terminalSize!!.columns)
{
assertEquals(DEFAULT_CHARACTER, line!!.getCharacterAt(column++))
}
}

companion object {
private val DEFAULT_CHARACTER = TextCharacter.DEFAULT_CHARACTER
}
}
