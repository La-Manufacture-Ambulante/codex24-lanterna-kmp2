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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.DefaultKeyDecodingProfile
import com.googlecode.lanterna.input.InputDecoder
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import com.googlecode.lanterna.internal.compat.StringReader
import com.googlecode.lanterna.internal.compat.TimeUnit
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.BitSet
import java.util.HashSet
import java.util.LinkedList
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class GraphicalTerminalImplementation(
    initialTerminalSize: TerminalSize?,
    val deviceConfiguration: TerminalEmulatorDeviceConfiguration?,
    val colorConfiguration: TerminalEmulatorColorConfiguration?,
    private val scrollController: TerminalScrollController?,
) : IOSafeTerminal {
    private val virtualTerminal: DefaultVirtualTerminal
    private val keyQueue: BlockingQueue<KeyStroke>
    private val dirtyCellsLookupTable: DirtyCellsLookupTable
    private val enquiryString: String

    private var cursorIsVisible: Boolean
    private var enableInput: Boolean
    private var blinkTimer: Timer?
    private var hasBlinkingText: Boolean
    private var blinkOn: Boolean
    private var bellOn: Boolean
    private var needFullRedraw: Boolean

    private var lastDrawnCursorPosition: TerminalPosition?
    private var lastBufferUpdateScrollPosition: Int
    private var lastComponentWidth: Int
    private var lastComponentHeight: Int

    protected var activeMouseCaptureMode: MouseCaptureMode? = null

    private var backbuffer: BufferedImage? = null
    private var copybuffer: BufferedImage? = null

    init {
        val resolvedInitialTerminalSize = initialTerminalSize ?: TerminalSize(80, 24)
        virtualTerminal = DefaultVirtualTerminal(resolvedInitialTerminalSize)
        keyQueue = LinkedBlockingQueue()
        dirtyCellsLookupTable = DirtyCellsLookupTable()

        cursorIsVisible = true
        enableInput = false
        enquiryString = "TerminalEmulator"
        lastDrawnCursorPosition = null
        lastBufferUpdateScrollPosition = 0
        lastComponentWidth = 0
        lastComponentHeight = 0
        blinkTimer = null
        hasBlinkingText = false
        blinkOn = true
        bellOn = false
        needFullRedraw = false

        virtualTerminal.setBacklogSize(requireNotNull(deviceConfiguration).lineBufferScrollbackSize)
    }

    internal abstract val fontHeight: Int
    internal abstract val fontWidth: Int
    internal abstract val height: Int
    internal abstract val width: Int
    internal abstract val isTextAntiAliased: Boolean
    internal abstract fun getFontForCharacter(character: TextCharacter): Font
    internal abstract fun repaint()

    internal val preferredSize: java.awt.Dimension
        @Synchronized get() = java.awt.Dimension(
            fontWidth * requireNotNull(virtualTerminal.terminalSize).columns,
            fontHeight * requireNotNull(virtualTerminal.terminalSize).rows,
        )

    @Synchronized
    fun onCreated() {
        startBlinkTimer()
        enableInput = true
        keyQueue.clear()
    }

    @Synchronized
    fun onDestroyed() {
        stopBlinkTimer()
        enableInput = false
        keyQueue.add(KeyStroke(KeyType.EOF))
    }

    @Synchronized
    fun startBlinkTimer() {
        if (blinkTimer != null) {
            return
        }
        blinkTimer = Timer("LanternaTerminalBlinkTimer", true)
        val blinkLength = requireNotNull(deviceConfiguration).blinkLengthInMilliSeconds.toLong()
        blinkTimer!!.schedule(object : TimerTask() {
            override fun run() {
                blinkOn = !blinkOn
                if (hasBlinkingText) {
                    repaint()
                }
            }
        }, blinkLength, blinkLength)
    }

    @Synchronized
    fun stopBlinkTimer() {
        blinkTimer?.cancel()
        blinkTimer = null
    }

    @Synchronized
    fun paintComponent(componentGraphics: Graphics) {
        val currentWidth = width
        val currentHeight = height

        requireNotNull(scrollController).updateModel(virtualTerminal.bufferLineCount * fontHeight, currentHeight)

        var needToUpdateBackBuffer =
            lastBufferUpdateScrollPosition != scrollController.scrollingOffset ||
                hasBlinkingText ||
                needFullRedraw

        if (currentWidth != lastComponentWidth || currentHeight != lastComponentHeight) {
            val columns = currentWidth / fontWidth
            val rows = currentHeight / fontHeight
            val terminalSize = requireNotNull(
                requireNotNull(virtualTerminal.terminalSize).withColumns(columns),
            ).withRows(rows)
            virtualTerminal.setTerminalSize(terminalSize)
            needToUpdateBackBuffer = true
        }

        if (needToUpdateBackBuffer) {
            updateBackBuffer(scrollController.scrollingOffset)
        }

        ensureGraphicBufferHasRightSize()
        val clipBounds = componentGraphics.clipBounds ?: Rectangle(0, 0, width, height)
        componentGraphics.drawImage(
            backbuffer,
            clipBounds.x,
            clipBounds.y,
            clipBounds.width,
            clipBounds.height,
            clipBounds.x,
            clipBounds.y,
            clipBounds.width,
            clipBounds.height,
            null,
        )

        val leftoverWidth = width % fontWidth
        componentGraphics.color = Color.BLACK
        if (leftoverWidth > 0) {
            componentGraphics.fillRect(width - leftoverWidth, 0, leftoverWidth, height)
        }

        lastComponentWidth = currentWidth
        lastComponentHeight = currentHeight
        componentGraphics.dispose()
        (this as java.lang.Object).notifyAll()
    }

    @Synchronized
    private fun updateBackBuffer(scrollOffsetFromTopInPixels: Int) {
        val currentFontWidth = fontWidth
        val currentFontHeight = fontHeight
        val cursorPosition = requireNotNull(virtualTerminal.cursorBufferPosition)
        val viewportSize = requireNotNull(virtualTerminal.terminalSize)
        val firstVisibleRowIndex = scrollOffsetFromTopInPixels / currentFontHeight
        val lastVisibleRowIndex = (scrollOffsetFromTopInPixels + height) / currentFontHeight

        ensureGraphicBufferHasRightSize()
        val backbufferGraphics = requireNotNull(backbuffer).createGraphics()
        if (isTextAntiAliased) {
            backbufferGraphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            )
            backbufferGraphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY,
            )
        }

        val foundBlinkingCharacters = AtomicBoolean(requireNotNull(deviceConfiguration).isCursorBlinking)
        buildDirtyCellsLookupTable(firstVisibleRowIndex, lastVisibleRowIndex)

        if (lastBufferUpdateScrollPosition < scrollOffsetFromTopInPixels) {
            val gap = scrollOffsetFromTopInPixels - lastBufferUpdateScrollPosition
            if (gap / currentFontHeight < viewportSize.rows) {
                val graphics = requireNotNull(copybuffer).createGraphics()
                graphics.setClip(0, 0, width, height - gap)
                graphics.drawImage(backbuffer, 0, -gap, null)
                graphics.dispose()
                backbufferGraphics.drawImage(copybuffer, 0, 0, width, height, 0, 0, width, height, null)
                if (!dirtyCellsLookupTable.isAllDirty()) {
                    val previousLastVisibleRowIndex = (lastBufferUpdateScrollPosition + height) / currentFontHeight
                    for (row in previousLastVisibleRowIndex..lastVisibleRowIndex) {
                        dirtyCellsLookupTable.setRowDirty(row)
                    }
                }
            } else {
                dirtyCellsLookupTable.setAllDirty()
            }
        } else if (lastBufferUpdateScrollPosition > scrollOffsetFromTopInPixels) {
            val gap = lastBufferUpdateScrollPosition - scrollOffsetFromTopInPixels
            if (gap / currentFontHeight < viewportSize.rows) {
                val graphics = requireNotNull(copybuffer).createGraphics()
                graphics.setClip(0, 0, width, height - gap)
                graphics.drawImage(backbuffer, 0, 0, null)
                graphics.dispose()
                backbufferGraphics.drawImage(copybuffer, 0, gap, width, height, 0, 0, width, height - gap, null)
                if (!dirtyCellsLookupTable.isAllDirty()) {
                    val previousFirstVisibleRowIndex = lastBufferUpdateScrollPosition / currentFontHeight
                    for (row in firstVisibleRowIndex..previousFirstVisibleRowIndex) {
                        dirtyCellsLookupTable.setRowDirty(row)
                    }
                }
            } else {
                dirtyCellsLookupTable.setAllDirty()
            }
        }

        if (lastComponentWidth < width && !dirtyCellsLookupTable.isAllDirty()) {
            val lastVisibleColumnIndex = width / currentFontWidth
            val previousLastVisibleColumnIndex = lastComponentWidth / currentFontWidth
            for (column in previousLastVisibleColumnIndex..lastVisibleColumnIndex) {
                dirtyCellsLookupTable.setColumnDirty(column)
            }
        }
        if (lastComponentHeight < height && !dirtyCellsLookupTable.isAllDirty()) {
            val previousLastVisibleRowIndex = (scrollOffsetFromTopInPixels + lastComponentHeight) / currentFontHeight
            for (row in previousLastVisibleRowIndex..lastVisibleRowIndex) {
                dirtyCellsLookupTable.setRowDirty(row)
            }
        }

        (virtualTerminal as com.googlecode.lanterna.terminal.virtual.VirtualTerminal).forEachLine(
            firstVisibleRowIndex,
            lastVisibleRowIndex,
            object : com.googlecode.lanterna.terminal.virtual.VirtualTerminal.BufferWalker {
                override fun onLine(
                    rowNumber: Int,
                    bufferLine: com.googlecode.lanterna.terminal.virtual.VirtualTerminal.BufferLine?,
                ) {
            var column = 0
            while (column < viewportSize.columns) {
                val textCharacter = requireNotNull(requireNotNull(bufferLine).getCharacterAt(column))
                var atCursorLocation = cursorPosition.equals(column, rowNumber)
                if (
                    !atCursorLocation &&
                    cursorPosition.column == column + 1 &&
                    cursorPosition.row == rowNumber &&
                    textCharacter.isDoubleWidth
                ) {
                    atCursorLocation = true
                }
                val isBlinking = textCharacter.getModifiers().contains(SGR.BLINK)
                if (isBlinking) {
                    foundBlinkingCharacters.set(true)
                }
                if (dirtyCellsLookupTable.isAllDirty() || dirtyCellsLookupTable.isDirty(rowNumber, column) || isBlinking) {
                    val characterWidth = currentFontWidth * if (textCharacter.isDoubleWidth) 2 else 1
                    var foregroundColor = deriveTrueForegroundColor(textCharacter, atCursorLocation)
                    var backgroundColor = deriveTrueBackgroundColor(textCharacter, atCursorLocation)
                    val drawCursor =
                        atCursorLocation &&
                            cursorIsVisible &&
                            (!requireNotNull(deviceConfiguration).isCursorBlinking || blinkOn)
                    if (bellOn) {
                        val temp = foregroundColor
                        foregroundColor = backgroundColor
                        backgroundColor = temp
                    }
                    drawCharacter(
                        backbufferGraphics,
                        textCharacter,
                        column,
                        rowNumber,
                        foregroundColor,
                        backgroundColor,
                        currentFontWidth,
                        currentFontHeight,
                        characterWidth,
                        scrollOffsetFromTopInPixels,
                        drawCursor,
                    )
                }
                if (textCharacter.isDoubleWidth) {
                    column++
                }
                column++
            }
                }
            },
        )

        backbufferGraphics.dispose()
        hasBlinkingText = foundBlinkingCharacters.get()
        lastDrawnCursorPosition = cursorPosition
        lastBufferUpdateScrollPosition = scrollOffsetFromTopInPixels
        needFullRedraw = false
    }

    private fun buildDirtyCellsLookupTable(firstRowOffset: Int, lastRowOffset: Int) {
        if (virtualTerminal.isWholeBufferDirtyThenReset || needFullRedraw) {
            dirtyCellsLookupTable.setAllDirty()
            return
        }

        val viewportSize = requireNotNull(virtualTerminal.terminalSize)
        val cursorPosition = requireNotNull(virtualTerminal.cursorBufferPosition)
        dirtyCellsLookupTable.resetAndInitialize(firstRowOffset, lastRowOffset, viewportSize.columns)
        dirtyCellsLookupTable.setDirty(cursorPosition)

        val previousCursorPosition = lastDrawnCursorPosition
        if (previousCursorPosition != null && previousCursorPosition != cursorPosition) {
            if (requireNotNull(virtualTerminal.getCharacter(previousCursorPosition)).isDoubleWidth) {
                dirtyCellsLookupTable.setDirty(requireNotNull(previousCursorPosition.withRelativeColumn(1)))
            }
            if (
                previousCursorPosition.column > 0 &&
                requireNotNull(virtualTerminal.getCharacter(requireNotNull(previousCursorPosition.withRelativeColumn(-1)))).isDoubleWidth
            ) {
                dirtyCellsLookupTable.setDirty(requireNotNull(previousCursorPosition.withRelativeColumn(-1)))
            }
            dirtyCellsLookupTable.setDirty(previousCursorPosition)
        }

        val dirtyCells = virtualTerminal.andResetDirtyCells
        for (position in dirtyCells) {
            dirtyCellsLookupTable.setDirty(position)
        }
    }

    private fun ensureGraphicBufferHasRightSize() {
        if (backbuffer == null) {
            backbuffer = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)
            copybuffer = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)
            val graphics = requireNotNull(backbuffer).createGraphics()
            graphics.color = requireNotNull(colorConfiguration).toAWTColor(TextColor.ANSI.DEFAULT, false, false)
            graphics.fillRect(0, 0, width * 2, height * 2)
            graphics.dispose()
        }

        if (
            requireNotNull(backbuffer).width < width ||
            requireNotNull(backbuffer).width > width * 4 ||
            requireNotNull(backbuffer).height < height ||
            requireNotNull(backbuffer).height > height * 4
        ) {
            val newBackbuffer = BufferedImage(
                maxOf(width, 1) * 2,
                maxOf(height, 1) * 2,
                BufferedImage.TYPE_INT_RGB,
            )
            val graphics = newBackbuffer.createGraphics()
            graphics.fillRect(0, 0, newBackbuffer.width, newBackbuffer.height)
            graphics.drawImage(backbuffer, 0, 0, null)
            graphics.dispose()
            backbuffer = newBackbuffer
            copybuffer = BufferedImage(maxOf(width, 1) * 2, maxOf(height, 1) * 2, BufferedImage.TYPE_INT_RGB)
        }
    }

    private fun drawCharacter(
        graphics: Graphics,
        character: TextCharacter,
        columnIndex: Int,
        rowIndex: Int,
        foregroundColor: Color,
        backgroundColor: Color,
        currentFontWidth: Int,
        currentFontHeight: Int,
        characterWidth: Int,
        scrollingOffsetInPixels: Int,
        drawCursor: Boolean,
    ) {
        val x = columnIndex * currentFontWidth
        val y = rowIndex * currentFontHeight - scrollingOffsetInPixels
        graphics.color = backgroundColor
        graphics.clipRect(x, y, characterWidth, currentFontHeight)
        graphics.fillRect(x, y, characterWidth, currentFontHeight)

        graphics.color = foregroundColor
        val font = getFontForCharacter(character)
        graphics.font = font
        val fontMetrics: FontMetrics = graphics.getFontMetrics()
        graphics.drawString(character.characterString, x, y + currentFontHeight - fontMetrics.descent + 1)

        if (character.isCrossedOut) {
            val lineStartY = y + (currentFontHeight / 2)
            graphics.drawLine(x, lineStartY, x + characterWidth, lineStartY)
        }
        if (character.isUnderlined) {
            val lineStartY = y + currentFontHeight - fontMetrics.descent + 1
            graphics.drawLine(x, lineStartY, x + characterWidth, lineStartY)
        }

        if (drawCursor) {
            graphics.color =
                if (requireNotNull(deviceConfiguration).cursorColor == null) {
                    foregroundColor
                } else {
                    requireNotNull(requireNotNull(colorConfiguration).toAWTColor(deviceConfiguration.cursorColor, false, false))
                }
            when (deviceConfiguration.cursorStyle) {
                TerminalEmulatorDeviceConfiguration.CursorStyle.UNDER_BAR ->
                    graphics.fillRect(x, y + currentFontHeight - 3, characterWidth, 2)
                TerminalEmulatorDeviceConfiguration.CursorStyle.VERTICAL_BAR ->
                    graphics.fillRect(x, y + 1, 2, currentFontHeight - 2)
                else -> Unit
            }
        }
    }

    private fun deriveTrueForegroundColor(character: TextCharacter, atCursorLocation: Boolean): Color {
        val foregroundColor = character.foregroundColor
        val backgroundColor = character.backgroundColor
        var reverse = character.isReversed
        val blink = character.isBlinking

        if (
            cursorIsVisible &&
            atCursorLocation &&
            deviceConfiguration?.cursorStyle == TerminalEmulatorDeviceConfiguration.CursorStyle.REVERSED &&
            (!requireNotNull(deviceConfiguration).isCursorBlinking || !blinkOn)
        ) {
            reverse = true
        }

        return when {
            reverse && (!blink || !blinkOn) ->
                requireNotNull(requireNotNull(colorConfiguration).toAWTColor(
                    backgroundColor,
                    backgroundColor != TextColor.ANSI.DEFAULT,
                    character.isBold,
                ))
            !reverse && blink && blinkOn ->
                requireNotNull(requireNotNull(colorConfiguration).toAWTColor(backgroundColor, false, character.isBold))
            else ->
                requireNotNull(requireNotNull(colorConfiguration).toAWTColor(foregroundColor, true, character.isBold))
        }
    }

    private fun deriveTrueBackgroundColor(character: TextCharacter, atCursorLocation: Boolean): Color {
        val foregroundColor = character.foregroundColor
        var backgroundColor: TextColor? = character.backgroundColor
        var reverse = character.isReversed

        if (cursorIsVisible && atCursorLocation) {
            when (deviceConfiguration?.cursorStyle) {
                TerminalEmulatorDeviceConfiguration.CursorStyle.REVERSED ->
                    if (!requireNotNull(deviceConfiguration).isCursorBlinking || !blinkOn) {
                        reverse = true
                    }
                TerminalEmulatorDeviceConfiguration.CursorStyle.FIXED_BACKGROUND ->
                    backgroundColor = deviceConfiguration.cursorColor
                else -> Unit
            }
        }

        return if (reverse) {
            requireNotNull(requireNotNull(colorConfiguration).toAWTColor(
                foregroundColor,
                backgroundColor == TextColor.ANSI.DEFAULT,
                character.isBold,
            ))
        } else {
            requireNotNull(requireNotNull(colorConfiguration).toAWTColor(backgroundColor, false, false))
        }
    }

    fun addInput(keyStroke: KeyStroke?) {
        if (keyStroke != null) {
            keyQueue.add(keyStroke)
        }
    }

    override fun pollInput(): KeyStroke {
        return if (!enableInput) KeyStroke(KeyType.EOF) else keyQueue.poll() ?: KeyStroke(KeyType.EOF)
    }

    override fun readInput(): KeyStroke {
        synchronized(keyQueue) {
            if (!enableInput) {
                return KeyStroke(KeyType.EOF)
            }
            try {
                return keyQueue.take()
            } catch (_: InterruptedException) {
                throw RuntimeException("Blocking input was interrupted")
            }
        }
    }

    override fun enterPrivateMode() {
        virtualTerminal.enterPrivateMode()
        clearBackBuffer()
        flush()
    }

    override fun exitPrivateMode() {
        virtualTerminal.exitPrivateMode()
        clearBackBuffer()
        flush()
    }

    override fun clearScreen() {
        virtualTerminal.clearScreen()
        clearBackBuffer()
    }

    private fun clearBackBuffer() {
        backbuffer?.let {
            val graphics = it.createGraphics()
            graphics.color = requireNotNull(colorConfiguration).toAWTColor(TextColor.ANSI.DEFAULT, false, false)
            graphics.fillRect(0, 0, width, height)
            graphics.dispose()
        }
    }

    override fun setCursorPosition(x: Int, y: Int) {
        cursorPosition = TerminalPosition(x, y)
    }

    override var cursorPosition: TerminalPosition?
        get() = virtualTerminal.cursorPosition
        set(position) {
            var adjustedPosition = requireNotNull(position)
            if (adjustedPosition.column < 0) {
                adjustedPosition = requireNotNull(adjustedPosition.withColumn(0))
            }
            if (adjustedPosition.row < 0) {
                adjustedPosition = requireNotNull(adjustedPosition.withRow(0))
            }
            virtualTerminal.cursorPosition = adjustedPosition
        }

    override fun setCursorVisible(visible: Boolean) {
        cursorIsVisible = visible
    }

    override fun putCharacter(c: Char) {
        virtualTerminal.putCharacter(c)
    }

    override fun putString(string: String?) {
        virtualTerminal.putString(string)
    }

    override fun newTextGraphics(): TextGraphics? = virtualTerminal.newTextGraphics()

    override fun enableSGR(sgr: SGR?) {
        virtualTerminal.enableSGR(sgr)
    }

    override fun disableSGR(sgr: SGR?) {
        virtualTerminal.disableSGR(sgr)
    }

    override fun resetColorAndSGR() {
        virtualTerminal.resetColorAndSGR()
    }

    override fun setForegroundColor(color: TextColor?) {
        virtualTerminal.setForegroundColor(color)
    }

    override fun setBackgroundColor(color: TextColor?) {
        virtualTerminal.setBackgroundColor(color)
    }

    override val terminalSize: TerminalSize?
        get() = virtualTerminal.terminalSize

    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray = enquiryString.toByteArray()

    override fun bell() {
        if (bellOn) {
            return
        }
        bellOn = true
        needFullRedraw = true
        updateBackBuffer(requireNotNull(scrollController).scrollingOffset)
        repaint()
        object : Thread("BellSilencer") {
            override fun run() {
                try {
                    sleep(100)
                } catch (_: InterruptedException) {
                }
                bellOn = false
                needFullRedraw = true
                updateBackBuffer(requireNotNull(scrollController).scrollingOffset)
                repaint()
            }
        }.start()
        Toolkit.getDefaultToolkit().beep()
    }

    override fun flush() {
        updateBackBuffer(requireNotNull(scrollController).scrollingOffset)
        repaint()
    }

    override fun close() = Unit

    override fun addResizeListener(listener: TerminalResizeListener?) {
        virtualTerminal.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        virtualTerminal.removeResizeListener(listener)
    }

    fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
        this.activeMouseCaptureMode = mouseCaptureMode
        updateMouseCaptureMode(mouseCaptureMode)
    }

    protected open fun updateMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) = Unit

    protected inner class TerminalInputListener : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            var character = e.keyChar
            val altDown = (e.modifiersEx and InputEvent.ALT_DOWN_MASK) != 0
            val ctrlDown = (e.modifiersEx and InputEvent.CTRL_DOWN_MASK) != 0
            val shiftDown = (e.modifiersEx and InputEvent.SHIFT_DOWN_MASK) != 0

            if (!TYPED_KEYS_TO_IGNORE.contains(character)) {
                if (ctrlDown && character.code > 0 && character.code < 0x1a) {
                    character = ('a'.code - 1 + character.code).toChar()
                    if (shiftDown) {
                        character = character.uppercaseChar()
                    }
                }
                if (!altDown && ctrlDown && shiftDown && character == 'V' && requireNotNull(deviceConfiguration).isClipboardAvailable) {
                    pasteClipboardContent()
                } else {
                    keyQueue.add(KeyStroke(character, ctrlDown, altDown, shiftDown))
                }
            }
        }

        override fun keyPressed(e: KeyEvent) {
            val altDown = (e.modifiersEx and InputEvent.ALT_DOWN_MASK) != 0
            val ctrlDown = (e.modifiersEx and InputEvent.CTRL_DOWN_MASK) != 0
            val shiftDown = (e.modifiersEx and InputEvent.SHIFT_DOWN_MASK) != 0
            when (e.keyCode) {
                KeyEvent.VK_ENTER -> keyQueue.add(KeyStroke(KeyType.ENTER, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_ESCAPE -> keyQueue.add(KeyStroke(KeyType.ESCAPE, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_BACK_SPACE -> keyQueue.add(KeyStroke(KeyType.BACKSPACE, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_LEFT -> keyQueue.add(KeyStroke(KeyType.ARROW_LEFT, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_RIGHT -> keyQueue.add(KeyStroke(KeyType.ARROW_RIGHT, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_UP -> keyQueue.add(KeyStroke(KeyType.ARROW_UP, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_DOWN -> keyQueue.add(KeyStroke(KeyType.ARROW_DOWN, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_INSERT ->
                    if (!altDown && !ctrlDown && shiftDown && requireNotNull(deviceConfiguration).isClipboardAvailable) {
                        pasteClipboardContent()
                    } else {
                        keyQueue.add(KeyStroke(KeyType.INSERT, ctrlDown, altDown, shiftDown))
                    }
                KeyEvent.VK_DELETE -> keyQueue.add(KeyStroke(KeyType.DELETE, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_HOME -> keyQueue.add(KeyStroke(KeyType.HOME, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_END -> keyQueue.add(KeyStroke(KeyType.END, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_PAGE_UP -> keyQueue.add(KeyStroke(KeyType.PAGE_UP, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_PAGE_DOWN -> keyQueue.add(KeyStroke(KeyType.PAGE_DOWN, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F1 -> keyQueue.add(KeyStroke(KeyType.F1, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F2 -> keyQueue.add(KeyStroke(KeyType.F2, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F3 -> keyQueue.add(KeyStroke(KeyType.F3, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F4 -> keyQueue.add(KeyStroke(KeyType.F4, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F5 -> keyQueue.add(KeyStroke(KeyType.F5, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F6 -> keyQueue.add(KeyStroke(KeyType.F6, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F7 -> keyQueue.add(KeyStroke(KeyType.F7, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F8 -> keyQueue.add(KeyStroke(KeyType.F8, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F9 -> keyQueue.add(KeyStroke(KeyType.F9, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F10 -> keyQueue.add(KeyStroke(KeyType.F10, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F11 -> keyQueue.add(KeyStroke(KeyType.F11, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_F12 -> keyQueue.add(KeyStroke(KeyType.F12, ctrlDown, altDown, shiftDown))
                KeyEvent.VK_TAB ->
                    if (e.isShiftDown) {
                        keyQueue.add(KeyStroke(KeyType.REVERSE_TAB, ctrlDown, altDown, false))
                    } else {
                        keyQueue.add(KeyStroke(KeyType.TAB, ctrlDown, altDown, shiftDown))
                    }
                else ->
                    if (altDown && ctrlDown && e.keyCode >= 'A'.code && e.keyCode <= 'Z'.code) {
                        var character = e.keyCode.toChar()
                        if (!shiftDown) {
                            character = character.lowercaseChar()
                        }
                        keyQueue.add(KeyStroke(character, true, true, shiftDown))
                    }
            }
        }
    }

    protected open inner class TerminalMouseListener(
        private val activeMouseCaptureMode: MouseCaptureMode? = null,
    ) : MouseAdapter() {
        private fun convertButton(awtButton: Int): Int =
            when (awtButton) {
                MouseEvent.BUTTON1 -> 1
                MouseEvent.BUTTON2 -> 3
                MouseEvent.BUTTON3 -> 2
                else -> 0
            }

        override fun mouseClicked(e: MouseEvent) {
            if (
                MouseInfo.getNumberOfButtons() > 2 &&
                e.button == MouseEvent.BUTTON2 &&
                requireNotNull(deviceConfiguration).isClipboardAvailable
            ) {
                pasteSelectionContent()
            }
        }

        override fun mousePressed(e: MouseEvent) {
            if (activeMouseCaptureMode != null) {
                keyQueue.add(
                    MouseAction(
                        MouseActionType.CLICK_DOWN,
                        convertButton(e.button),
                        TerminalPosition(e.x / fontWidth, e.y / fontHeight),
                        e.isControlDown,
                        e.isAltDown,
                        e.isShiftDown,
                    ),
                )
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            if (activeMouseCaptureMode != null) {
                keyQueue.add(
                    MouseAction(
                        MouseActionType.CLICK_RELEASE,
                        convertButton(e.button),
                        TerminalPosition(e.x / fontWidth, e.y / fontHeight),
                        e.isControlDown,
                        e.isAltDown,
                        e.isShiftDown,
                    ),
                )
            }
        }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
            if (activeMouseCaptureMode != null) {
                keyQueue.add(
                    MouseAction(
                        if (e.wheelRotation > 0) MouseActionType.SCROLL_DOWN else MouseActionType.SCROLL_UP,
                        if (e.wheelRotation > 0) 5 else 4,
                        TerminalPosition(e.x / fontWidth, e.y / fontHeight),
                        e.isControlDown,
                        e.isAltDown,
                        e.isShiftDown,
                    ),
                )
            }
        }

        override fun mouseMoved(e: MouseEvent) {
            if (
                activeMouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE ||
                activeMouseCaptureMode == MouseCaptureMode.CLICK_AUTODETECT
            ) {
                keyQueue.add(
                    MouseAction(
                        MouseActionType.MOVE,
                        0,
                        TerminalPosition(e.x / fontWidth, e.y / fontHeight),
                        e.isControlDown,
                        e.isAltDown,
                        e.isShiftDown,
                    ),
                )
            }
        }

        override fun mouseDragged(e: MouseEvent) {
            if (
                activeMouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG ||
                activeMouseCaptureMode == MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE ||
                activeMouseCaptureMode == MouseCaptureMode.CLICK_AUTODETECT
            ) {
                keyQueue.add(
                    MouseAction(
                        MouseActionType.DRAG,
                        convertButton(e.button),
                        TerminalPosition(e.x / fontWidth, e.y / fontHeight),
                        e.isControlDown,
                        e.isAltDown,
                        e.isShiftDown,
                    ),
                )
            }
        }
    }

    private fun pasteClipboardContent() {
        try {
            val systemClipboard: Clipboard? = Toolkit.getDefaultToolkit().systemClipboard
            if (systemClipboard != null) {
                injectStringAsKeyStrokes(systemClipboard.getData(DataFlavor.stringFlavor) as String)
            }
        } catch (_: Exception) {
        }
    }

    private fun pasteSelectionContent() {
        try {
            val systemSelection: Clipboard? = Toolkit.getDefaultToolkit().systemSelection
            if (systemSelection != null) {
                injectStringAsKeyStrokes(systemSelection.getData(DataFlavor.stringFlavor) as String)
            }
        } catch (_: Exception) {
        }
    }

    private fun injectStringAsKeyStrokes(string: String) {
        val inputDecoder = InputDecoder(StringReader(string))
        inputDecoder.addProfile(DefaultKeyDecodingProfile())
        try {
            var keyStroke = inputDecoder.getNextCharacter(false)
            while (keyStroke != null && keyStroke.keyType != KeyType.EOF) {
                keyQueue.add(keyStroke)
                keyStroke = inputDecoder.getNextCharacter(false)
            }
        } catch (_: IOException) {
        }
    }

    private class DirtyCellsLookupTable {
        private val table: MutableList<BitSet> = ArrayList()
        private var firstRowIndex: Int = -1
        private var allDirty: Boolean = false

        fun resetAndInitialize(firstRowIndex: Int, lastRowIndex: Int, columns: Int) {
            this.firstRowIndex = firstRowIndex
            allDirty = false
            val rows = lastRowIndex - firstRowIndex + 1
            while (table.size < rows) {
                table.add(BitSet(columns))
            }
            while (table.size > rows) {
                table.removeAt(table.size - 1)
            }
            for (index in table.indices) {
                if (table[index].size() != columns) {
                    table[index] = BitSet(columns)
                } else {
                    table[index].clear()
                }
            }
        }

        fun setAllDirty() {
            allDirty = true
        }

        fun isAllDirty(): Boolean = allDirty

        fun setDirty(position: TerminalPosition) {
            if (position.row < firstRowIndex || position.row >= firstRowIndex + table.size) {
                return
            }
            val tableRow = table[position.row - firstRowIndex]
            if (position.column < tableRow.size()) {
                tableRow.set(position.column)
            }
        }

        fun setRowDirty(rowNumber: Int) {
            val row = table[rowNumber - firstRowIndex]
            row.set(0, row.size())
        }

        fun setColumnDirty(column: Int) {
            for (row in table) {
                if (column < row.size()) {
                    row.set(column)
                }
            }
        }

        fun isDirty(row: Int, column: Int): Boolean {
            if (row < firstRowIndex || row >= firstRowIndex + table.size) {
                return false
            }
            val tableRow = table[row - firstRowIndex]
            return column < tableRow.size() && tableRow[column]
        }
    }

    companion object {
        private val TYPED_KEYS_TO_IGNORE: Set<Char> = HashSet(
            Arrays.asList('\n', '\t', '\r', '\b', '\u001b', 127.toChar()),
        )
    }
}
