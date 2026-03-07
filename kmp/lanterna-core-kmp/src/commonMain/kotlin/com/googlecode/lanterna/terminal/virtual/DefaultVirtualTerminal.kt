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
package com.googlecode.lanterna.terminal.virtual

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.terminal.AbstractTerminal

import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

 class DefaultVirtualTerminal/**
 * Creates a new virtual terminal with an initial size set
 * @param initialTerminalSize Starting size of the virtual terminal
 */
     @JvmOverloads  constructor(private var terminalSize:TerminalSize? = TerminalSize(80, 24)):AbstractTerminal(), VirtualTerminal {
private val regularTextBuffer:TextBuffer?
private val privateModeTextBuffer:TextBuffer?
private val dirtyTerminalCells:TreeSet<TerminalPosition?>?
private val listeners:List<VirtualTerminalListener?>?

private var currentTextBuffer:TextBuffer? = null
private var wholeBufferDirty:Boolean = false
@get:Override
@get:Synchronized @set:Override
@set:Synchronized  var isCursorVisible:Boolean = false
private var backlogSize:Int = 0

private val inputQueue:BlockingQueue<KeyStroke?>?
private val activeModifiers:EnumSet<SGR?>?
private var activeForegroundColor:TextColor? = null
private var activeBackgroundColor:TextColor? = null

 // Global coordinates, i.e. relative to the top-left corner of the full buffer
    @get:Override
@get:Synchronized  var cursorBufferPosition:TerminalPosition? = null
private set

 // Used when switching back from private mode, to restore the earlier cursor position
    private var savedCursorPosition:TerminalPosition? = null

 var cursorPosition:TerminalPosition?
@Override
@Synchronized get() {
if (bufferLineCount <= terminalSize!!.rows)
{
return cursorBufferPosition
}
else
{
return cursorBufferPosition!!.withRelativeRow(-(bufferLineCount - terminalSize!!.rows))
}
}
@Override
@Synchronized set(cursorPosition) {
var cursorPosition = cursorPosition
if (terminalSize!!.rows < bufferLineCount)
{
cursorPosition = cursorPosition!!.withRelativeRow(bufferLineCount - terminalSize!!.rows)
}
this.cursorBufferPosition = cursorPosition
correctCursor()
}

 val dirtyCells:TreeSet<TerminalPosition?>?
@Synchronized get() {
return TreeSet(dirtyTerminalCells)
}

 val andResetDirtyCells:TreeSet<TerminalPosition?>?
@Synchronized get() {
val copy = TreeSet(dirtyTerminalCells)
dirtyTerminalCells!!.clear()
return copy
}

 val isWholeBufferDirtyThenReset:Boolean
@Synchronized get() {
val copy = wholeBufferDirty
wholeBufferDirty = false
return copy
}

 val bufferLineCount:Int
@Override
@Synchronized get() {
return currentTextBuffer!!.getLineCount()
}

init{
this.regularTextBuffer = TextBuffer()
this.privateModeTextBuffer = TextBuffer()
this.dirtyTerminalCells = TreeSet()
this.listeners = ArrayList()

 // Terminal state
        this.inputQueue = LinkedBlockingQueue()
this.activeModifiers = EnumSet.noneOf(SGR::class.java)
this.activeForegroundColor = TextColor.ANSI.DEFAULT
this.activeBackgroundColor = TextColor.ANSI.DEFAULT

 // Start with regular mode
        this.currentTextBuffer = regularTextBuffer
this.wholeBufferDirty = false
this.isCursorVisible = true
this.cursorBufferPosition = TerminalPosition.TOP_LEFT_CORNER
this.savedCursorPosition = TerminalPosition.TOP_LEFT_CORNER
this.backlogSize = 1000
}

/**///////////////////////////////////////////////////////////////////////////////////////////////////////////////// */
    // Terminal interface methods (and related)
    /**///////////////////////////////////////////////////////////////////////////////////////////////////////////////// */
    @Override
@Synchronized  fun getTerminalSize():TerminalSize? {
return terminalSize
}

@Override
@Synchronized  fun setTerminalSize(newSize:TerminalSize?) {
this.terminalSize = newSize
trimBufferBacklog()
correctCursor()
for (listener in listeners!!)
{
listener!!.onResized(this, terminalSize)
}
super.onResized(newSize!!.columns, newSize!!.rows)
}

@Override
@Synchronized @JvmStatic  fun enterPrivateMode() {
currentTextBuffer = privateModeTextBuffer
savedCursorPosition = cursorBufferPosition
cursorPosition = TerminalPosition.TOP_LEFT_CORNER
setWholeBufferDirty()
}

@Override
@Synchronized @JvmStatic  fun exitPrivateMode() {
currentTextBuffer = regularTextBuffer
cursorBufferPosition = savedCursorPosition
setWholeBufferDirty()
}

@Override
@Synchronized @JvmStatic  fun clearScreen() {
currentTextBuffer!!.clear()
setWholeBufferDirty()
cursorPosition = TerminalPosition.TOP_LEFT_CORNER
}

@Override
@Synchronized  fun setCursorPosition(x:Int, y:Int) {
cursorPosition = cursorBufferPosition!!.withColumn(x)!!.withRow(y)
}

@Override
@Synchronized  fun putCharacter(c:Char) {
if (c == '\n')
{
moveCursorToNextLine()
}
else if (TerminalTextUtils.isPrintableCharacter(c))
{
putCharacter(TextCharacter(c, activeForegroundColor, activeBackgroundColor, activeModifiers))
}
}

@Override
@Synchronized  fun putString(string:String?) {
for (textCharacter in TextCharacter.fromString(string, activeForegroundColor, activeBackgroundColor, activeModifiers))
{
putCharacter(textCharacter!!)
}
}

@Override
@Synchronized  fun enableSGR(sgr:SGR?) {
activeModifiers!!.add(sgr)
}

@Override
@Synchronized  fun disableSGR(sgr:SGR?) {
activeModifiers!!.remove(sgr)
}

@Override
@Synchronized @JvmStatic  fun resetColorAndSGR() {
this.activeModifiers!!.clear()
this.activeForegroundColor = TextColor.ANSI.DEFAULT
this.activeBackgroundColor = TextColor.ANSI.DEFAULT
}

@Override
@Synchronized  fun setForegroundColor(color:TextColor?) {
this.activeForegroundColor = color
}

@Override
@Synchronized  fun setBackgroundColor(color:TextColor?) {
this.activeBackgroundColor = color
}

@Override
@Synchronized  fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
return getClass().getName().getBytes()
}

@Override
@Synchronized @JvmStatic  fun bell() {
for (listener in listeners!!)
{
listener!!.onBell()
}
}

@Override
@Synchronized @JvmStatic  fun flush() {
for (listener in listeners!!)
{
listener!!.onFlush()
}
}

@Override
@JvmStatic  fun close() {
for (listener in listeners!!)
{
listener!!.onClose()
}
}

@Override
@Synchronized  fun pollInput():KeyStroke? {
return inputQueue!!.poll()
}

@Override
@Synchronized  fun readInput():KeyStroke? {
try
{
return inputQueue!!.take()
}
catch (e:InterruptedException) {
throw RuntimeException("Unexpected interrupt", e)
}

}

@Override
 fun newTextGraphics():TextGraphics? {
return VirtualTerminalTextGraphics(this)
}

/**///////////////////////////////////////////////////////////////////////////////////////////////////////////////// */
    // VirtualTerminal specific methods
    /**///////////////////////////////////////////////////////////////////////////////////////////////////////////////// */

    @Override
@Synchronized  fun addVirtualTerminalListener(listener:VirtualTerminalListener?) {
if (listener != null)
{
listeners!!.add(listener)
}
}

@Override
@Synchronized  fun removeVirtualTerminalListener(listener:VirtualTerminalListener?) {
listeners!!.remove(listener)
}

@Override
@Synchronized  fun setBacklogSize(backlogSize:Int) {
this.backlogSize = backlogSize
}

@Override
 fun addInput(keyStroke:KeyStroke?) {
inputQueue!!.add(keyStroke)
}

@Override
@Synchronized  fun getCharacter(position:TerminalPosition):TextCharacter? {
return getCharacter(position.column, position.row)
}

@Override
@Synchronized  fun getCharacter(column:Int, row:Int):TextCharacter? {
var row = row
if (terminalSize!!.rows < currentTextBuffer!!.getLineCount())
{
row += currentTextBuffer!!.getLineCount() - terminalSize!!.rows
}
return getBufferCharacter(column, row)
}

@Override
 fun getBufferCharacter(column:Int, row:Int):TextCharacter? {
return currentTextBuffer!!.getCharacter(row, column)
}

@Override
 fun getBufferCharacter(position:TerminalPosition):TextCharacter? {
return getBufferCharacter(position.column, position.row)
}

@Override
@Synchronized  fun forEachLine(startRow:Int, endRow:Int, bufferWalker:BufferWalker?) {
val emptyLine = { column-> TextCharacter.DEFAULT_CHARACTER }
val iterator = currentTextBuffer!!.getLinesFrom(startRow)
for (row in startRow..endRow)
{
var bufferLine = emptyLine
if (iterator!!.hasNext())
{
val list = iterator!!.next()
bufferLine = { column->
if (column >= list!!.size())
{
return TextCharacter.DEFAULT_CHARACTER
}
list!!.get(column) }
}
bufferWalker!!.onLine(row, bufferLine)
}
}

@Synchronized internal fun putCharacter(terminalCharacter:TextCharacter) {
if (terminalCharacter.`is`('\t'))
{
val nrOfSpaces = TabBehaviour.ALIGN_TO_COLUMN_4.getTabReplacement(cursorBufferPosition!!.column).length()
var i = 0
while (i < nrOfSpaces && cursorBufferPosition!!.column < terminalSize!!.columns - 1)
{
putCharacter(terminalCharacter.withCharacter(' '))
i++
}
}
else
{
val doubleWidth = terminalCharacter.isDoubleWidth()
 // If we're at the last column and the user tries to print a double-width character, reset the cell and move
            // to the next line
            if (cursorBufferPosition!!.column == terminalSize!!.columns - 1 && doubleWidth)
{
currentTextBuffer!!.setCharacter(cursorBufferPosition!!.row, cursorBufferPosition!!.column, TextCharacter.DEFAULT_CHARACTER)
moveCursorToNextLine()
}
if (cursorBufferPosition!!.column == terminalSize!!.columns)
{
moveCursorToNextLine()
}

 // Update the buffer
            val i = currentTextBuffer!!.setCharacter(cursorBufferPosition!!.row, cursorBufferPosition!!.column, terminalCharacter)
if (!wholeBufferDirty)
{
dirtyTerminalCells!!.add(TerminalPosition(cursorBufferPosition!!.column, cursorBufferPosition!!.row))
if (i == 1)
{
dirtyTerminalCells!!.add(TerminalPosition(cursorBufferPosition!!.column + 1, cursorBufferPosition!!.row))
}
else if (i == 2)
{
dirtyTerminalCells!!.add(TerminalPosition(cursorBufferPosition!!.column - 1, cursorBufferPosition!!.row))
}
if (dirtyTerminalCells!!.size() > (terminalSize!!.columns.toDouble() * terminalSize!!.rows.toDouble() * 0.9))
{
setWholeBufferDirty()
}
}

 //Advance cursor
            cursorBufferPosition = cursorBufferPosition!!.withRelativeColumn(if (doubleWidth) 2 else 1)
if (cursorBufferPosition!!.column > terminalSize!!.columns)
{
moveCursorToNextLine()
}
}
}

/**
 * Moves the text cursor to the first column of the next line and trims the backlog of necessary
 */
    private fun moveCursorToNextLine() {
cursorBufferPosition = cursorBufferPosition!!.withColumn(0)!!.withRelativeRow(1)
if (cursorBufferPosition!!.row >= currentTextBuffer!!.getLineCount())
{
currentTextBuffer!!.newLine()
}
trimBufferBacklog()
correctCursor()
}

/**
 * Marks the whole buffer as dirty so every cell is considered in need to repainting. This is used by methods such
 * as clear and bell that will affect all content at once.
 */
    private fun setWholeBufferDirty() {
wholeBufferDirty = true
dirtyTerminalCells!!.clear()
}

private fun trimBufferBacklog() {
 // Now see if we need to discard lines from the backlog
        var bufferBacklogSize = backlogSize
if (currentTextBuffer === privateModeTextBuffer)
{
bufferBacklogSize = 0
}
val trimBacklogRows = currentTextBuffer!!.getLineCount() - (bufferBacklogSize + terminalSize!!.rows)
if (trimBacklogRows > 0)
{
currentTextBuffer!!.removeTopLines(trimBacklogRows)
 // Adjust cursor position
            cursorBufferPosition = cursorBufferPosition!!.withRelativeRow(-trimBacklogRows)
correctCursor()
if (!wholeBufferDirty)
{
 // Adjust all "dirty" positions
                val newDirtySet = TreeSet()
for (dirtyPosition in dirtyTerminalCells!!)
{
val adjustedPosition = dirtyPosition!!.withRelativeRow(-trimBacklogRows)
if (adjustedPosition!!.row >= 0)
{
newDirtySet.add(adjustedPosition)
}
}
dirtyTerminalCells!!.clear()
dirtyTerminalCells!!.addAll(newDirtySet)
}
}
}

private fun correctCursor() {
this.cursorBufferPosition = cursorBufferPosition!!.withColumn(Math.min(cursorBufferPosition!!.column, terminalSize!!.columns - 1))
this.cursorBufferPosition = cursorBufferPosition!!.withRow(Math.min(cursorBufferPosition!!.row, Math.max(terminalSize!!.rows, bufferLineCount) - 1))
this.cursorBufferPosition = TerminalPosition(
Math.max(cursorBufferPosition!!.column, 0), 
Math.max(cursorBufferPosition!!.row, 0))
}

@Override
 fun toString():String? {
return currentTextBuffer!!.toString()
}
}/**
 * Creates a new virtual terminal with an initial size set
 */
