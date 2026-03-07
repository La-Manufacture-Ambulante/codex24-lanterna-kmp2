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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.DefaultKeyDecodingProfile
import com.googlecode.lanterna.input.InputDecoder
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal

import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This is the class that does the heavy lifting for both [AWTTerminal] and [SwingTerminal]. It maintains
 * most of the external terminal state and also the main back buffer that is copied to the components area on draw
 * operations.
 * 
 * @author martin
 */
internal abstract class GraphicalTerminalImplementation/**
 * Creates a new GraphicalTerminalImplementation component using custom settings and a custom scroll controller. The
 * scrolling controller will be notified when the terminal's history size grows and will be called when this class
 * needs to figure out the current scrolling position.
 * @param initialTerminalSize Initial size of the terminal, which will be used when calculating the preferred size
 * of the component. If null, it will default to 80x25. If the AWT layout manager forces
 * the component to a different size, the value of this parameter won't have any meaning
 * @param deviceConfiguration Device configuration to use for this SwingTerminal
 * @param colorConfiguration Color configuration to use for this SwingTerminal
 * @param scrollController Controller to use for scrolling, the object passed in will be notified whenever the
 * scrollable area has changed
 */
    (
initialTerminalSize:TerminalSize?, 
 val deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
 val colorConfiguration:TerminalEmulatorColorConfiguration?, 
private val scrollController:TerminalScrollController?):IOSafeTerminal {
private val virtualTerminal:DefaultVirtualTerminal?
private val keyQueue:BlockingQueue<KeyStroke?>?
private val dirtyCellsLookupTable:DirtyCellsLookupTable?

private val enquiryString:String?

private var cursorIsVisible:Boolean = false
private var enableInput:Boolean = false
private var blinkTimer:Timer? = null
private var hasBlinkingText:Boolean = false
private var blinkOn:Boolean = false
private var bellOn:Boolean = false
private var needFullRedraw:Boolean = false

private var lastDrawnCursorPosition:TerminalPosition? = null
private var lastBufferUpdateScrollPosition:Int = 0
private var lastComponentWidth:Int = 0
private var lastComponentHeight:Int = 0

protected var mouseCaptureMode:MouseCaptureMode? = null

 // We use two different data structures to optimize drawing
    //  * A list of modified characters since the last draw (stored in VirtualTerminal)
    //  * A backbuffer with the graphics content
    //
    // The buffer is the most important one as it allows us to re-use what was drawn earlier. It is not reset on every
    // drawing operation but updates just in those places where the map tells us the character has changed.
    private var backbuffer:BufferedImage? = null

 // Used as a middle-ground when copying large segments when scrolling
    private var copybuffer:BufferedImage? = null

/**//////// */
    // First abstract methods that are implemented in AWTTerminalImplementation and SwingTerminalImplementation
    /**//////// */

    /**
 * Used to find out the font height, in pixels
 * @return Terminal font height in pixels
 */
    internal abstract val fontHeight:Int

/**
 * Used to find out the font width, in pixels
 * @return Terminal font width in pixels
 */
    internal abstract val fontWidth:Int

/**
 * Used when requiring the total height of the terminal component, in pixels
 * @return Height of the terminal component, in pixels
 */
    internal abstract val height:Int

/**
 * Used when requiring the total width of the terminal component, in pixels
 * @return Width of the terminal component, in pixels
 */
    internal abstract val width:Int

/**
 * Returns `true` if anti-aliasing is enabled, `false` otherwise
 * @return `true` if anti-aliasing is enabled, `false` otherwise
 */
    internal abstract val isTextAntiAliased:Boolean

/**//////// */
    // Implement all the Swing-related methods
    /**//////// */
    /**
 * Calculates the preferred size of this terminal
 * @return Preferred size of this terminal
 */
     val preferredSize:Dimension?
@Synchronized get() {
return Dimension(fontWidth * virtualTerminal!!.getTerminalSize().getColumns(), 
fontHeight * virtualTerminal!!.getTerminalSize().getRows())
}

 var cursorPosition:TerminalPosition?
@Override
get() {
return virtualTerminal!!.getCursorPosition()
}
@Override
@Synchronized set(position) {
var position = position
if (position.column < 0)
{
position = position.withColumn(0)
}
if (position.row < 0)
{
position = position.withRow(0)
}
virtualTerminal!!.setCursorPosition(position)
}

 val terminalSize:TerminalSize?
@Override
@Synchronized get() {
return virtualTerminal!!.getTerminalSize()
}

init{
var initialTerminalSize = initialTerminalSize

 //This is kind of meaningless since we don't know how large the
        //component is at this point, but we should set it to something
        if (initialTerminalSize == null)
{
initialTerminalSize = TerminalSize(80, 24)
}
this.virtualTerminal = DefaultVirtualTerminal(initialTerminalSize)
this.keyQueue = LinkedBlockingQueue()
this.dirtyCellsLookupTable = DirtyCellsLookupTable()

this.cursorIsVisible = true        //Always start with an activate and visible cursor
this.enableInput = false           //Start with input disabled and activate it once the window is visible
this.enquiryString = "TerminalEmulator"
this.lastDrawnCursorPosition = null
this.lastBufferUpdateScrollPosition = 0
this.lastComponentHeight = 0
this.lastComponentWidth = 0
this.backbuffer = null  // We don't know the dimensions yet
this.copybuffer = null
this.blinkTimer = null
this.hasBlinkingText = false   // Assume initial content doesn't have any blinking text
this.blinkOn = true
this.needFullRedraw = false


virtualTerminal!!.setBacklogSize(deviceConfiguration!!.getLineBufferScrollbackSize())
}

/**
 * Returning the AWT font to use for the specific character. This might not always be the same, in case a we are
 * trying to draw an unusual character (probably CJK) which isn't contained in the standard terminal font.
 * @param character Character to get the font for
 * @return Font to be used for this character
 */
    internal abstract fun getFontForCharacter(character:TextCharacter?):Font? 

/**
 * Called by the `GraphicalTerminalImplementation` when it would like the OS to schedule a repaint of the
 * window
 */
    internal abstract fun repaint() 

@Synchronized  fun onCreated() {
startBlinkTimer()
enableInput = true

 // Reset the queue, just be to sure
        keyQueue!!.clear()
}

@Synchronized  fun onDestroyed() {
stopBlinkTimer()
enableInput = false

 // If a thread is blocked, waiting on something in the keyQueue...
        keyQueue!!.add(KeyStroke(KeyType.EOF))
}

/**
 * Start the timer that triggers blinking
 */
    @Synchronized  fun startBlinkTimer() {
if (blinkTimer != null)
{
 // Already on!
            return 
}
blinkTimer = Timer("LanternaTerminalBlinkTimer", true)
blinkTimer!!.schedule(object:TimerTask() {
@Override
@JvmStatic  fun run() {
blinkOn = !blinkOn
if (hasBlinkingText)
{
repaint()
}
}
}, deviceConfiguration!!.getBlinkLengthInMilliSeconds(), deviceConfiguration!!.getBlinkLengthInMilliSeconds())
}

/**
 * Stops the timer the triggers blinking
 */
    @Synchronized  fun stopBlinkTimer() {
if (blinkTimer == null)
{
 // Already off!
            return 
}
blinkTimer!!.cancel()
blinkTimer = null
}

/**
 * Updates the back buffer (if necessary) and draws it to the component's surface
 * @param componentGraphics Object to use when drawing to the component's surface
 */
    @Synchronized  fun paintComponent(componentGraphics:Graphics?) {
val width = width
val height = height

this.scrollController!!.updateModel(
virtualTerminal!!.getBufferLineCount() * fontHeight, 
height)

var needToUpdateBackBuffer = 
 // User has used the scrollbar, we need to update the back buffer to reflect this
                (lastBufferUpdateScrollPosition != scrollController!!.getScrollingOffset() || 
 // There is blinking text to update
                        hasBlinkingText || 
 // We simply have a hint that we should update everything
                        needFullRedraw)

 // Detect resize
        if (width != lastComponentWidth || height != lastComponentHeight)
{
val columns = width / fontWidth
val rows = height / fontHeight
val terminalSize = virtualTerminal!!.getTerminalSize().withColumns(columns).withRows(rows)
virtualTerminal!!.setTerminalSize(terminalSize)

 // Back buffer needs to be updated since the component size has changed
            needToUpdateBackBuffer = true
}

if (needToUpdateBackBuffer)
{
updateBackBuffer(scrollController!!.getScrollingOffset())
}

ensureGraphicBufferHasRightSize()
var clipBounds = componentGraphics!!.getClipBounds()
if (clipBounds == null)
{
clipBounds = Rectangle(0, 0, width, height)
}
componentGraphics!!.drawImage(
backbuffer, 
 // Destination coordinates
                clipBounds!!.x, 
clipBounds!!.y, 
clipBounds!!.width, 
clipBounds!!.height, 
 // Source coordinates
                clipBounds!!.x, 
clipBounds!!.y, 
clipBounds!!.width, 
clipBounds!!.height, null)

 // Take care of the left-over area at the bottom and right of the component where no character can fit
        //int leftoverHeight = getHeight() % getFontHeight();
        val leftoverWidth = width % fontWidth
componentGraphics!!.setColor(Color.BLACK)
if (leftoverWidth > 0)
{
componentGraphics!!.fillRect(width - leftoverWidth, 0, leftoverWidth, height)
}

 //0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), null);
        this.lastComponentWidth = width
this.lastComponentHeight = height
componentGraphics!!.dispose()
notifyAll()
}

@Synchronized private fun updateBackBuffer(scrollOffsetFromTopInPixels:Int) {
 //long startTime = System.currentTimeMillis();
        val fontWidth = fontWidth
val fontHeight = fontHeight

 //Retrieve the position of the cursor, relative to the scrolling state
        val cursorPosition = virtualTerminal!!.getCursorBufferPosition()
val viewportSize = virtualTerminal!!.getTerminalSize()

val firstVisibleRowIndex = scrollOffsetFromTopInPixels / fontHeight
val lastVisibleRowIndex = (scrollOffsetFromTopInPixels + height) / fontHeight

 //Setup the graphics object
        ensureGraphicBufferHasRightSize()
val backbufferGraphics = backbuffer!!.createGraphics()

if (isTextAntiAliased)
{
backbufferGraphics!!.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
backbufferGraphics!!.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
}

val foundBlinkingCharacters = AtomicBoolean(deviceConfiguration!!.isCursorBlinking())
buildDirtyCellsLookupTable(firstVisibleRowIndex, lastVisibleRowIndex)

 // Detect scrolling
        if (lastBufferUpdateScrollPosition < scrollOffsetFromTopInPixels)
{
val gap = scrollOffsetFromTopInPixels - lastBufferUpdateScrollPosition
if (gap / fontHeight < viewportSize!!.rows)
{
val graphics = copybuffer!!.createGraphics()
graphics!!.setClip(0, 0, width, height - gap)
graphics!!.drawImage(backbuffer, 0, -gap, null)
graphics!!.dispose()
backbufferGraphics!!.drawImage(copybuffer, 0, 0, width, height, 0, 0, width, height, null)
if (!dirtyCellsLookupTable!!.isAllDirty)
{
 //Mark bottom rows as dirty so they are repainted
                    val previousLastVisibleRowIndex = (lastBufferUpdateScrollPosition + height) / fontHeight
for (row in previousLastVisibleRowIndex..lastVisibleRowIndex)
{
dirtyCellsLookupTable!!.setRowDirty(row)
}
}
}
else
{
dirtyCellsLookupTable!!.setAllDirty()
}
}
else if (lastBufferUpdateScrollPosition > scrollOffsetFromTopInPixels)
{
val gap = lastBufferUpdateScrollPosition - scrollOffsetFromTopInPixels
if (gap / fontHeight < viewportSize!!.rows)
{
val graphics = copybuffer!!.createGraphics()
graphics!!.setClip(0, 0, width, height - gap)
graphics!!.drawImage(backbuffer, 0, 0, null)
graphics!!.dispose()
backbufferGraphics!!.drawImage(copybuffer, 0, gap, width, height, 0, 0, width, height - gap, null)
if (!dirtyCellsLookupTable!!.isAllDirty)
{
 //Mark top rows as dirty so they are repainted
                    val previousFirstVisibleRowIndex = lastBufferUpdateScrollPosition / fontHeight
for (row in firstVisibleRowIndex..previousFirstVisibleRowIndex)
{
dirtyCellsLookupTable!!.setRowDirty(row)
}
}
}
else
{
dirtyCellsLookupTable!!.setAllDirty()
}
}

 // Detect component resize
        if (lastComponentWidth < width)
{
if (!dirtyCellsLookupTable!!.isAllDirty)
{
 //Mark right columns as dirty so they are repainted
                val lastVisibleColumnIndex = width / fontWidth
val previousLastVisibleColumnIndex = lastComponentWidth / fontWidth
for (column in previousLastVisibleColumnIndex..lastVisibleColumnIndex)
{
dirtyCellsLookupTable!!.setColumnDirty(column)
}
}
}
if (lastComponentHeight < height)
{
if (!dirtyCellsLookupTable!!.isAllDirty)
{
 //Mark bottom rows as dirty so they are repainted
                val previousLastVisibleRowIndex = (scrollOffsetFromTopInPixels + lastComponentHeight) / fontHeight
for (row in previousLastVisibleRowIndex..lastVisibleRowIndex)
{
dirtyCellsLookupTable!!.setRowDirty(row)
}
}
}

virtualTerminal!!.forEachLine(firstVisibleRowIndex, lastVisibleRowIndex, { rowNumber, bufferLine-> var column = 0
while (column < viewportSize!!.columns)
{
val textCharacter = bufferLine!!.getCharacterAt(column)
var atCursorLocation = cursorPosition!!.equals(column, rowNumber!!.toInt())
 //If next position is the cursor location and this is a double-width character (i.e. cursor is on the padding),
                //consider this location the cursor position since otherwise the cursor will be skipped
                if ((!atCursorLocation && 
cursorPosition!!.column == column + 1 && 
cursorPosition!!.row == rowNumber && 
textCharacter!!.isDoubleWidth()))
{
atCursorLocation = true
}
val isBlinking = textCharacter!!.getModifiers().contains(SGR.BLINK)
if (isBlinking)
{
foundBlinkingCharacters.set(true)
}
if (dirtyCellsLookupTable!!.isAllDirty || dirtyCellsLookupTable!!.isDirty(rowNumber!!.toInt(), column) || isBlinking)
{
val characterWidth = fontWidth * (if (textCharacter!!.isDoubleWidth()) 2 else 1)
var foregroundColor = deriveTrueForegroundColor(textCharacter!!, atCursorLocation)
var backgroundColor = deriveTrueBackgroundColor(textCharacter!!, atCursorLocation)
 //Always draw if the cursor isn't blinking
                    val drawCursor = atCursorLocation && cursorIsVisible && (!deviceConfiguration!!.isCursorBlinking() || blinkOn)    //If the cursor should be displayed and is blinking, only draw when blinkOn is true

 // Visualize bell as all colors inverted
                    if (bellOn)
{
val temp = foregroundColor
foregroundColor = backgroundColor
backgroundColor = temp
}

drawCharacter(backbufferGraphics!!, 
textCharacter, 
column, 
rowNumber!!.toInt(), 
foregroundColor, 
backgroundColor, 
fontWidth, 
fontHeight, 
characterWidth, 
scrollOffsetFromTopInPixels, 
drawCursor)
}
if (textCharacter!!.isDoubleWidth())
{
column++ //Skip the trailing space after a CJK character
}
column++
} })

backbufferGraphics!!.dispose()

 // Update the blink status according to if there were any blinking characters or not
        this.hasBlinkingText = foundBlinkingCharacters.get()
this.lastDrawnCursorPosition = cursorPosition
this.lastBufferUpdateScrollPosition = scrollOffsetFromTopInPixels
this.needFullRedraw = false

 //System.out.println("Updated backbuffer in " + (System.currentTimeMillis() - startTime) + " ms");
    }

private fun buildDirtyCellsLookupTable(firstRowOffset:Int, lastRowOffset:Int) {
if (virtualTerminal!!.isWholeBufferDirtyThenReset() || needFullRedraw)
{
dirtyCellsLookupTable!!.setAllDirty()
return 
}

val viewportSize = virtualTerminal!!.getTerminalSize()
val cursorPosition = virtualTerminal!!.getCursorBufferPosition()

dirtyCellsLookupTable!!.resetAndInitialize(firstRowOffset, lastRowOffset, viewportSize!!.columns)
dirtyCellsLookupTable!!.setDirty(cursorPosition!!)
if (lastDrawnCursorPosition != null && !lastDrawnCursorPosition!!.equals(cursorPosition))
{
if (virtualTerminal!!.getCharacter(lastDrawnCursorPosition).isDoubleWidth())
{
dirtyCellsLookupTable!!.setDirty(lastDrawnCursorPosition!!.withRelativeColumn(1)!!)
}
if (lastDrawnCursorPosition!!.column > 0 && virtualTerminal!!.getCharacter(lastDrawnCursorPosition!!.withRelativeColumn(-1)).isDoubleWidth())
{
dirtyCellsLookupTable!!.setDirty(lastDrawnCursorPosition!!.withRelativeColumn(-1)!!)
}
dirtyCellsLookupTable!!.setDirty(lastDrawnCursorPosition!!)
}

val dirtyCells = virtualTerminal!!.getAndResetDirtyCells()
for (position in dirtyCells!!)
{
dirtyCellsLookupTable!!.setDirty(position!!)
}
}

private fun ensureGraphicBufferHasRightSize() {
if (backbuffer == null)
{
backbuffer = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)
copybuffer = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)

 // We only need to set the content of the backbuffer during initialization time
            val graphics = backbuffer!!.createGraphics()
graphics!!.setColor(colorConfiguration!!.toAWTColor(TextColor.ANSI.DEFAULT, false, false))
graphics!!.fillRect(0, 0, width * 2, height * 2)
graphics!!.dispose()
}
if ((backbuffer!!.getWidth() < width || backbuffer!!.getWidth() > width * 4 || 
backbuffer!!.getHeight() < height || backbuffer!!.getHeight() > height * 4))
{

val newBackbuffer = BufferedImage(Math.max(width, 1) * 2, Math.max(height, 1) * 2, BufferedImage.TYPE_INT_RGB)
val graphics = newBackbuffer.createGraphics()
graphics!!.fillRect(0, 0, newBackbuffer.getWidth(), newBackbuffer.getHeight())
graphics!!.drawImage(backbuffer, 0, 0, null)
graphics!!.dispose()
backbuffer = newBackbuffer

 // Re-initialize the copy buffer, but we don't need to set any content
            copybuffer = BufferedImage(Math.max(width, 1) * 2, Math.max(height, 1) * 2, BufferedImage.TYPE_INT_RGB)
}
}

private fun drawCharacter(
g:Graphics, 
character:TextCharacter?, 
columnIndex:Int, 
rowIndex:Int, 
foregroundColor:Color?, 
backgroundColor:Color?, 
fontWidth:Int, 
fontHeight:Int, 
characterWidth:Int, 
scrollingOffsetInPixels:Int, 
drawCursor:Boolean) {

val x = columnIndex * fontWidth
val y = rowIndex * fontHeight - scrollingOffsetInPixels
g.setColor(backgroundColor)
g.setClip(x, y, characterWidth, fontHeight)
g.fillRect(x, y, characterWidth, fontHeight)

g.setColor(foregroundColor)
val font = getFontForCharacter(character)
g.setFont(font)
val fontMetrics = g.getFontMetrics()
g.drawString(character!!.getCharacterString(), x, y + fontHeight - fontMetrics!!.getDescent() + 1)

if (character!!.isCrossedOut())
{

            val lineStartX = x
val lineStartY = y + (fontHeight / 2)
val lineEndX = lineStartX + characterWidth
g.drawLine(lineStartX, lineStartY, lineEndX, lineStartY)
}
if (character!!.isUnderlined())
{

            val lineStartX = x
val lineStartY = y + fontHeight - fontMetrics!!.getDescent() + 1
val lineEndX = lineStartX + characterWidth
g.drawLine(lineStartX, lineStartY, lineEndX, lineStartY)
}

if (drawCursor)
{
if (deviceConfiguration!!.getCursorColor() == null)
{
g.setColor(foregroundColor)
}
else
{
g.setColor(colorConfiguration!!.toAWTColor(deviceConfiguration!!.getCursorColor(), false, false))
}
if (deviceConfiguration!!.getCursorStyle() === TerminalEmulatorDeviceConfiguration.CursorStyle.UNDER_BAR)
{
g.fillRect(x, y + fontHeight - 3, characterWidth, 2)
}
else if (deviceConfiguration!!.getCursorStyle() === TerminalEmulatorDeviceConfiguration.CursorStyle.VERTICAL_BAR)
{
g.fillRect(x, y + 1, 2, fontHeight - 2)
}
}
}


private fun deriveTrueForegroundColor(character:TextCharacter, atCursorLocation:Boolean):Color? {
val foregroundColor = character.getForegroundColor()
val backgroundColor = character.getBackgroundColor()
var reverse = character.isReversed()
val blink = character.isBlinking()

if (cursorIsVisible && atCursorLocation)
{
if ((deviceConfiguration!!.getCursorStyle() === TerminalEmulatorDeviceConfiguration.CursorStyle.REVERSED && (!deviceConfiguration!!.isCursorBlinking() || !blinkOn)))
{
reverse = true
}
}

if (reverse && (!blink || !blinkOn))
{
return colorConfiguration!!.toAWTColor(backgroundColor, backgroundColor !== TextColor.ANSI.DEFAULT, character.isBold())
}
else if (!reverse && blink && blinkOn)
{
return colorConfiguration!!.toAWTColor(backgroundColor, false, character.isBold())
}
else
{
return colorConfiguration!!.toAWTColor(foregroundColor, true, character.isBold())
}
}

private fun deriveTrueBackgroundColor(character:TextCharacter, atCursorLocation:Boolean):Color? {
val foregroundColor = character.getForegroundColor()
var backgroundColor = character.getBackgroundColor()
var reverse = character.isReversed()

if (cursorIsVisible && atCursorLocation)
{
if ((deviceConfiguration!!.getCursorStyle() === TerminalEmulatorDeviceConfiguration.CursorStyle.REVERSED && (!deviceConfiguration!!.isCursorBlinking() || !blinkOn)))
{
reverse = true
}
else if (deviceConfiguration!!.getCursorStyle() === TerminalEmulatorDeviceConfiguration.CursorStyle.FIXED_BACKGROUND)
{
backgroundColor = deviceConfiguration!!.getCursorColor()
}
}

if (reverse)
{
return colorConfiguration!!.toAWTColor(foregroundColor, backgroundColor === TextColor.ANSI.DEFAULT, character.isBold())
}
else
{
return colorConfiguration!!.toAWTColor(backgroundColor, false, false)
}
}

 fun addInput(keyStroke:KeyStroke?) {
keyQueue!!.add(keyStroke)
}

/**//////// */
    // Then delegate all Terminal interface methods to the virtual terminal implementation
    //
    // Some of these methods we need to pass to the AWT-thread, which makes the call asynchronous. Hopefully this isn't
    // causing too much problem...
    /**//////// */
    @Override
 fun pollInput():KeyStroke? {
if (!enableInput)
{
return KeyStroke(KeyType.EOF)
}
return keyQueue!!.poll()
}

@Override
 fun readInput():KeyStroke? {
 // Synchronize on keyQueue here so only one thread is inside keyQueue.take()
        synchronized (keyQueue) {
if (!enableInput)
{
return KeyStroke(KeyType.EOF)
}
try
{
return keyQueue!!.take()
}
catch (ignore:InterruptedException) {
throw RuntimeException("Blocking input was interrupted")
}

}
}

@Override
@Synchronized  fun enterPrivateMode() {
virtualTerminal!!.enterPrivateMode()
clearBackBuffer()
flush()
}

@Override
@Synchronized  fun exitPrivateMode() {
virtualTerminal!!.exitPrivateMode()
clearBackBuffer()
flush()
}

@Override
@Synchronized  fun clearScreen() {
virtualTerminal!!.clearScreen()
clearBackBuffer()
}

/**
 * Clears out the back buffer and the resets the visual state so next paint operation will do a full repaint of
 * everything
 */
    private fun clearBackBuffer() {
 // Manually clear the backbuffer
        if (backbuffer != null)
{
val graphics = backbuffer!!.createGraphics()
val backgroundColor = colorConfiguration!!.toAWTColor(TextColor.ANSI.DEFAULT, false, false)
graphics!!.setColor(backgroundColor)
graphics!!.fillRect(0, 0, width, height)
graphics!!.dispose()
}
}

@Override
@Synchronized  fun setCursorPosition(x:Int, y:Int) {
cursorPosition = TerminalPosition(x, y)
}

@Override
 fun setCursorVisible(visible:Boolean) {
cursorIsVisible = visible
}

@Override
@Synchronized  fun putCharacter(c:Char) {
virtualTerminal!!.putCharacter(c)
}

@Override
 fun putString(string:String?) {
virtualTerminal!!.putString(string)
}

@Override
 fun newTextGraphics():TextGraphics? {
return virtualTerminal!!.newTextGraphics()
}

@Override
 fun enableSGR(sgr:SGR?) {
virtualTerminal!!.enableSGR(sgr)
}

@Override
 fun disableSGR(sgr:SGR?) {
virtualTerminal!!.disableSGR(sgr)
}

@Override
 fun resetColorAndSGR() {
virtualTerminal!!.resetColorAndSGR()
}

@Override
 fun setForegroundColor(color:TextColor?) {
virtualTerminal!!.setForegroundColor(color)
}

@Override
 fun setBackgroundColor(color:TextColor?) {
virtualTerminal!!.setBackgroundColor(color)
}

@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
return enquiryString!!.getBytes()
}

@Override
 fun bell() {
if (bellOn)
{
return 
}

 // Flash the screen...
        bellOn = true
needFullRedraw = true
updateBackBuffer(scrollController!!.getScrollingOffset())
repaint()
 // Unify this with the blink timer and just do the whole timer logic ourselves?
        object:Thread("BellSilencer") {
@Override
@JvmStatic  fun run() {
try
{
Thread.sleep(100)
}
catch (ignore:InterruptedException) {}

bellOn = false
needFullRedraw = true
updateBackBuffer(scrollController!!.getScrollingOffset())
repaint()
}
}.start()

 // ...and make a sound
        Toolkit.getDefaultToolkit().beep()
}

@Override
@Synchronized  fun flush() {
updateBackBuffer(scrollController!!.getScrollingOffset())
repaint()
}

@Override
 fun close() {
 // No action
    }

@Override
 fun addResizeListener(listener:TerminalResizeListener?) {
virtualTerminal!!.addResizeListener(listener)
}

@Override
 fun removeResizeListener(listener:TerminalResizeListener?) {
virtualTerminal!!.removeResizeListener(listener)
}

 fun setMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?) {
this.mouseCaptureMode = mouseCaptureMode
updateMouseCaptureMode(mouseCaptureMode)
}

protected fun updateMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?) {
 // Let the AWT and Swing implementations do the job here
    }

/**
 * Class that translates AWT key events into Lanterna [KeyStroke]
 */
    protected inner class TerminalInputListener:KeyAdapter() {
@Override
 fun keyTyped(e:KeyEvent) {
var character = e.getKeyChar()
val altDown = (e.getModifiersEx() and InputEvent.ALT_DOWN_MASK) !== 0
val ctrlDown = (e.getModifiersEx() and InputEvent.CTRL_DOWN_MASK) !== 0
val shiftDown = (e.getModifiersEx() and InputEvent.SHIFT_DOWN_MASK) !== 0

if (!TYPED_KEYS_TO_IGNORE.contains(character))
{
 //We need to re-adjust alphabet characters if ctrl was pressed, just like for the AnsiTerminal
                if (ctrlDown && character.toInt() > 0 && character.toInt() < 0x1a)
{
character = ('a'.toInt() - 1 + character.toInt()).toChar()
if (shiftDown)
{
character = Character.toUpperCase(character)
}
}

 // Check if clipboard is avavilable and this was a paste (ctrl + shift + v) before
                // adding the key to the input queue
                if (!altDown && ctrlDown && shiftDown && character == 'V' && deviceConfiguration!!.isClipboardAvailable())
{
pasteClipboardContent()
}
else
{
keyQueue!!.add(KeyStroke(character, ctrlDown, altDown, shiftDown))
}
}
}

@Override
 fun keyPressed(e:KeyEvent) {
val altDown = (e.getModifiersEx() and InputEvent.ALT_DOWN_MASK) !== 0
val ctrlDown = (e.getModifiersEx() and InputEvent.CTRL_DOWN_MASK) !== 0
val shiftDown = (e.getModifiersEx() and InputEvent.SHIFT_DOWN_MASK) !== 0
if (e.getKeyCode() === KeyEvent.VK_ENTER)
{
keyQueue!!.add(KeyStroke(KeyType.ENTER, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_ESCAPE)
{
keyQueue!!.add(KeyStroke(KeyType.ESCAPE, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_BACK_SPACE)
{
keyQueue!!.add(KeyStroke(KeyType.BACKSPACE, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_LEFT)
{
keyQueue!!.add(KeyStroke(KeyType.ARROW_LEFT, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_RIGHT)
{
keyQueue!!.add(KeyStroke(KeyType.ARROW_RIGHT, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_UP)
{
keyQueue!!.add(KeyStroke(KeyType.ARROW_UP, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_DOWN)
{
keyQueue!!.add(KeyStroke(KeyType.ARROW_DOWN, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_INSERT)
{
 // This could be a paste (shift+insert) if the clipboard is available
                if (!altDown && !ctrlDown && shiftDown && deviceConfiguration!!.isClipboardAvailable())
{
pasteClipboardContent()
}
else
{
keyQueue!!.add(KeyStroke(KeyType.INSERT, ctrlDown, altDown, shiftDown))
}
}
else if (e.getKeyCode() === KeyEvent.VK_DELETE)
{
keyQueue!!.add(KeyStroke(KeyType.DELETE, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_HOME)
{
keyQueue!!.add(KeyStroke(KeyType.HOME, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_END)
{
keyQueue!!.add(KeyStroke(KeyType.END, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_PAGE_UP)
{
keyQueue!!.add(KeyStroke(KeyType.PAGE_UP, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_PAGE_DOWN)
{
keyQueue!!.add(KeyStroke(KeyType.PAGE_DOWN, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F1)
{
keyQueue!!.add(KeyStroke(KeyType.F1, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F2)
{
keyQueue!!.add(KeyStroke(KeyType.F2, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F3)
{
keyQueue!!.add(KeyStroke(KeyType.F3, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F4)
{
keyQueue!!.add(KeyStroke(KeyType.F4, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F5)
{
keyQueue!!.add(KeyStroke(KeyType.F5, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F6)
{
keyQueue!!.add(KeyStroke(KeyType.F6, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F7)
{
keyQueue!!.add(KeyStroke(KeyType.F7, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F8)
{
keyQueue!!.add(KeyStroke(KeyType.F8, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F9)
{
keyQueue!!.add(KeyStroke(KeyType.F9, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F10)
{
keyQueue!!.add(KeyStroke(KeyType.F10, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F11)
{
keyQueue!!.add(KeyStroke(KeyType.F11, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_F12)
{
keyQueue!!.add(KeyStroke(KeyType.F12, ctrlDown, altDown, shiftDown))
}
else if (e.getKeyCode() === KeyEvent.VK_TAB)
{
if (e.isShiftDown())
{
keyQueue!!.add(KeyStroke(KeyType.REVERSE_TAB, ctrlDown, altDown, false))
}
else
{
keyQueue!!.add(KeyStroke(KeyType.TAB, ctrlDown, altDown, shiftDown))
}
}
else
{
 //keyTyped doesn't catch this scenario (for whatever reason...) so we have to do it here
                if (altDown && ctrlDown && e.getKeyCode() >= 'A' && e.getKeyCode() <= 'Z')
{
var character = e.getKeyCode() as Char
if (!shiftDown)
{
character = Character.toLowerCase(character)
}
keyQueue!!.add(KeyStroke(character, true, true, shiftDown))
}
}
}
}

 // This is mostly unimplemented, we could hook more of this into ExtendedTerminal's mouse functions
    protected inner class TerminalMouseListener:MouseAdapter {
private val mouseCaptureMode:MouseCaptureMode? = null

 constructor() : super() {}

 constructor(mouseCaptureMode:MouseCaptureMode?) : super() {
this.mouseCaptureMode = mouseCaptureMode
}

private fun convertButton(awtButton:Int):Int {
var button = 0
when (awtButton) {
MouseEvent.BUTTON1 -> button = 1
MouseEvent.BUTTON2 -> button = 3
MouseEvent.BUTTON3 -> button = 2
else -> {}
}
return button
}

@Override
 fun mouseClicked(e:MouseEvent?) {
if ((MouseInfo.getNumberOfButtons() > 2 && 
e!!.getButton() === MouseEvent.BUTTON2 && 
deviceConfiguration!!.isClipboardAvailable()))
{
pasteSelectionContent()
}
}

@Override
 fun mousePressed(e:MouseEvent?) {
if (mouseCaptureMode != null)
{
keyQueue!!.add(MouseAction(MouseActionType.CLICK_DOWN, convertButton(e!!.getButton()), TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
}

@Override
 fun mouseReleased(e:MouseEvent?) {
if (mouseCaptureMode != null)
{
keyQueue!!.add(MouseAction(MouseActionType.CLICK_RELEASE, convertButton(e!!.getButton()), TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
}

@Override
 fun mouseWheelMoved(e:MouseWheelEvent?) {
if (mouseCaptureMode != null)
{
val rotation = e!!.getWheelRotation()
if (rotation > 0)
{
keyQueue!!.add(MouseAction(MouseActionType.SCROLL_DOWN, 5, TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
else
{
keyQueue!!.add(MouseAction(MouseActionType.SCROLL_UP, 4, TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
}
}

@Override
 fun mouseMoved(e:MouseEvent?) {
if (mouseCaptureMode === MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE || mouseCaptureMode === MouseCaptureMode.CLICK_AUTODETECT)
{
keyQueue!!.add(MouseAction(MouseActionType.MOVE, 0, TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
}

@Override
 fun mouseDragged(e:MouseEvent?) {
if (mouseCaptureMode === MouseCaptureMode.CLICK_RELEASE_DRAG || mouseCaptureMode === MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE || mouseCaptureMode === MouseCaptureMode.CLICK_AUTODETECT)
{
keyQueue!!.add(MouseAction(MouseActionType.DRAG, convertButton(e!!.getButton()), TerminalPosition(e!!.getX() / fontWidth, e!!.getY() / fontHeight), e!!.isControlDown(), e!!.isAltDown(), e!!.isShiftDown()))
}
}
}

private fun pasteClipboardContent() {
try
{
val systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
if (systemClipboard != null)
{
injectStringAsKeyStrokes(systemClipboard!!.getData(DataFlavor.stringFlavor) as String)
}
}
catch (ignore:Exception) {}

}

private fun pasteSelectionContent() {
try
{
val systemSelection = Toolkit.getDefaultToolkit().getSystemSelection()
if (systemSelection != null)
{
injectStringAsKeyStrokes(systemSelection!!.getData(DataFlavor.stringFlavor) as String)
}
}
catch (ignore:Exception) {}

}

private fun injectStringAsKeyStrokes(string:String?) {
val stringReader = StringReader(string)
val inputDecoder = InputDecoder(stringReader)
inputDecoder.addProfile(DefaultKeyDecodingProfile())
try
{
var keyStroke = inputDecoder.getNextCharacter(false)
while (keyStroke != null && keyStroke!!.getKeyType() !== KeyType.EOF)
{
keyQueue!!.add(keyStroke)
keyStroke = inputDecoder.getNextCharacter(false)
}
}
catch (ignore:IOException) {}

}

private class DirtyCellsLookupTable internal constructor() {
private val table:List<BitSet?>?
private var firstRowIndex:Int = 0
internal var isAllDirty:Boolean = false
private set

init{
table = ArrayList()
firstRowIndex = -1
isAllDirty = false
}

internal fun resetAndInitialize(firstRowIndex:Int, lastRowIndex:Int, columns:Int) {
this.firstRowIndex = firstRowIndex
this.isAllDirty = false
val rows = lastRowIndex - firstRowIndex + 1
while (table!!.size() < rows)
{
table!!.add(BitSet(columns))
}
while (table!!.size() > rows)
{
table!!.remove(table!!.size() - 1)
}
for (index in 0 until table!!.size())
{
if (table!!.get(index).size() !== columns)
{
table!!.set(index, BitSet(columns))
}
else
{
table!!.get(index).clear()
}
}
}

@JvmStatic internal fun setAllDirty() {
isAllDirty = true
}

internal fun setDirty(position:TerminalPosition) {
if ((position.row < firstRowIndex || position.row >= firstRowIndex + table!!.size()))
{
return 
}
val tableRow = table!!.get(position.row - firstRowIndex)
if (position.column < tableRow!!.size())
{
tableRow!!.set(position.column)
}
}

internal fun setRowDirty(rowNumber:Int) {
val row = table!!.get(rowNumber - firstRowIndex)
row!!.set(0, row!!.size())
}

internal fun setColumnDirty(column:Int) {
for (row in table!!)
{
if (column < row!!.size())
{
row!!.set(column)
}
}
}

internal fun isDirty(row:Int, column:Int):Boolean {
if (row < firstRowIndex || row >= firstRowIndex + table!!.size())
{
return false
}
val tableRow = table!!.get(row - firstRowIndex)
if (column < tableRow!!.size())
{
return tableRow!!.get(column)
}
else
{
return false
}
}
}

companion object {

/**//////// */
    // Remaining are private internal classes used by SwingTerminal
    /**//////// */
    private val TYPED_KEYS_TO_IGNORE = HashSet(Arrays.asList('\n', '\t', '\r', '\b', '\u001b', 127.toChar()))
}
}