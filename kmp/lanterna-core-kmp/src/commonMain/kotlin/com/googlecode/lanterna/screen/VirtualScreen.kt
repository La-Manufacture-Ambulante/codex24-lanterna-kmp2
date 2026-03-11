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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.io.IOException

/**
 * VirtualScreen wraps a normal screen and presents it as a screen that has a configurable minimum size; if the real
 * screen is smaller than this size, the presented screen will add scrolling to get around it. To anyone using this
 * class, it will appear and behave just as a normal screen. Scrolling is done by using CTRL + arrow keys.
 *
 * The use case for this class is to allow you to set a minimum size that you can count on be honored, no matter how
 * small the user makes the terminal. This should make programming GUIs easier.
 * @author Martin
 */
class VirtualScreen(private val realScreen: Screen) : AbstractScreen(realScreen.terminalSize) {
    private val frameRenderer: FrameRenderer = DefaultFrameRenderer()
    private var minimumSize: TerminalSize? = realScreen.terminalSize
    private var viewportTopLeft: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER

    /**
     * Returns the current size of the viewport. This will generally match the dimensions of the underlying terminal.
     * @return Viewport size for this [VirtualScreen]
     */
    var viewportSize: TerminalSize? = minimumSize
        private set
    private var scrollWithCTRL = false

    /**
     * Sets the minimum size we want the virtual screen to have. If the user resizes the real terminal to something
     * smaller than this, the virtual screen will refuse to make it smaller and add scrollbars to the view.
     * @param minimumSize Minimum size we want the screen to have
     */
    fun setMinimumSize(minimumSize: TerminalSize) {
        this.minimumSize = minimumSize
        val virtualSize = minimumSize.max(realScreen.terminalSize ?: minimumSize) ?: minimumSize
        if (minimumSize != virtualSize) {
            addResizeRequest(virtualSize)
            super.doResizeIfNecessary()
        }
        calculateViewport(realScreen.terminalSize ?: minimumSize)
    }

    /**
     * Returns the minimum size this virtual screen can have. If the real terminal is made smaller than this, the
     * virtual screen will draw scrollbars and implement scrolling
     * @return Minimum size configured for this virtual screen
     */
    fun getMinimumSize(): TerminalSize? = minimumSize

    /**
     * When the viewport is too small, user can scroll using ALT + arrow keys, but ALT can be replaced by CTRL by
     * calling this method.
     * @param scrollOnCTRL Scroll using CTRL instead of ALT if set to `true`, ALT if `false`
     */
    fun setScrollOnCTRL(scrollOnCTRL: Boolean) {
        scrollWithCTRL = scrollOnCTRL
    }

    fun setViewportTopLeft(position: TerminalPosition?) {
        viewportTopLeft = position ?: TerminalPosition.TOP_LEFT_CORNER
        while (
            viewportTopLeft.column > 0 &&
            viewportTopLeft.column + (viewportSize?.columns ?: 0) > (minimumSize?.columns ?: 0)
        ) {
            viewportTopLeft = viewportTopLeft.withRelativeColumn(-1) ?: viewportTopLeft
        }
        while (
            viewportTopLeft.row > 0 &&
            viewportTopLeft.row + (viewportSize?.rows ?: 0) > (minimumSize?.rows ?: 0)
        ) {
            viewportTopLeft = viewportTopLeft.withRelativeRow(-1) ?: viewportTopLeft
        }
    }

    @Throws(IOException::class)
    override fun startScreen() {
        realScreen.startScreen()
    }

    @Throws(IOException::class)
    override fun stopScreen() {
        realScreen.stopScreen()
    }

    override fun getFrontCharacter(position: TerminalPosition?): TextCharacter? = null

    override var cursorPosition: TerminalPosition?
        get() = super.cursorPosition
        set(value) {
            super.cursorPosition = value
            if (value == null) {
                realScreen.cursorPosition = null
                return
            }
            val translated = value.withRelativeColumn(-viewportTopLeft.column)?.withRelativeRow(-viewportTopLeft.row)
            if (
                translated != null &&
                translated.column >= 0 &&
                translated.column < (viewportSize?.columns ?: 0) &&
                translated.row >= 0 &&
                translated.row < (viewportSize?.rows ?: 0)
            ) {
                realScreen.cursorPosition = translated
            } else {
                realScreen.cursorPosition = null
            }
        }

    @Synchronized
    override fun doResizeIfNecessary(): TerminalSize? {
        val underlyingSize = realScreen.doResizeIfNecessary() ?: return null
        val newVirtualSize = calculateViewport(underlyingSize)
        if (terminalSize != newVirtualSize) {
            addResizeRequest(newVirtualSize)
            return super.doResizeIfNecessary()
        }
        return newVirtualSize
    }

    private fun calculateViewport(realTerminalSize: TerminalSize): TerminalSize {
        val currentMinimumSize = minimumSize ?: realTerminalSize
        val newVirtualSize = currentMinimumSize.max(realTerminalSize) ?: realTerminalSize
        if (newVirtualSize == realTerminalSize) {
            viewportSize = realTerminalSize
            viewportTopLeft = TerminalPosition.TOP_LEFT_CORNER
        } else {
            val currentViewportSize = viewportSize ?: realTerminalSize
            val newViewportSize = frameRenderer.getViewportSize(realTerminalSize, newVirtualSize) ?: realTerminalSize
            if (newViewportSize.rows > currentViewportSize.rows) {
                viewportTopLeft =
                    viewportTopLeft.withRow(maxOf(0, viewportTopLeft.row - (newViewportSize.rows - currentViewportSize.rows)))
                        ?: viewportTopLeft
            }
            if (newViewportSize.columns > currentViewportSize.columns) {
                viewportTopLeft =
                    viewportTopLeft.withColumn(maxOf(0, viewportTopLeft.column - (newViewportSize.columns - currentViewportSize.columns)))
                        ?: viewportTopLeft
            }
            viewportSize = newViewportSize
        }
        return newVirtualSize
    }

    @Throws(IOException::class)
    override fun refresh(refreshType: Screen.RefreshType?) {
        cursorPosition = cursorPosition
        val currentViewportSize = viewportSize ?: return
        val realTerminalSize = realScreen.terminalSize ?: return
        if (currentViewportSize != realTerminalSize) {
            frameRenderer.drawFrame(realScreen.newTextGraphics(), realTerminalSize, terminalSize, viewportTopLeft)
        }

        val viewportOffset = frameRenderer.viewportOffset ?: TerminalPosition.TOP_LEFT_CORNER
        for (y in 0 until currentViewportSize.rows) {
            for (x in 0 until currentViewportSize.columns) {
                realScreen.setCharacter(
                    x + viewportOffset.column,
                    y + viewportOffset.row,
                    backBuffer.getCharacterAt(x + viewportTopLeft.column, y + viewportTopLeft.row),
                )
            }
        }
        realScreen.refresh(refreshType)
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? = filter(realScreen.pollInput())

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? = filter(realScreen.readInput())

    @Throws(IOException::class)
    private fun filter(keyStroke: KeyStroke?): KeyStroke? {
        val stroke = keyStroke ?: return null
        val currentViewportSize = viewportSize ?: return stroke
        val currentTerminalSize = terminalSize ?: return stroke

        when {
            isScrollTrigger(stroke) && stroke.keyType == KeyType.ARROW_LEFT -> {
                if (viewportTopLeft.column > 0) {
                    viewportTopLeft = viewportTopLeft.withRelativeColumn(-1) ?: viewportTopLeft
                    refresh()
                    return null
                }
            }
            isScrollTrigger(stroke) && stroke.keyType == KeyType.ARROW_RIGHT -> {
                if (viewportTopLeft.column + currentViewportSize.columns < currentTerminalSize.columns) {
                    viewportTopLeft = viewportTopLeft.withRelativeColumn(1) ?: viewportTopLeft
                    refresh()
                    return null
                }
            }
            isScrollTrigger(stroke) && stroke.keyType == KeyType.ARROW_UP -> {
                if (viewportTopLeft.row > 0) {
                    viewportTopLeft = viewportTopLeft.withRelativeRow(-1) ?: viewportTopLeft
                    realScreen.scrollLines(0, currentViewportSize.rows - 1, -1)
                    refresh()
                    return null
                }
            }
            isScrollTrigger(stroke) && stroke.keyType == KeyType.ARROW_DOWN -> {
                if (viewportTopLeft.row + currentViewportSize.rows < currentTerminalSize.rows) {
                    viewportTopLeft = viewportTopLeft.withRelativeRow(1) ?: viewportTopLeft
                    realScreen.scrollLines(0, currentViewportSize.rows - 1, 1)
                    refresh()
                    return null
                }
            }
            isScrollTrigger(stroke) && stroke.keyType == KeyType.PAGE_UP -> {
                if (viewportTopLeft.row > 0) {
                    val scroll = minOf(currentViewportSize.rows, viewportTopLeft.row)
                    viewportTopLeft = viewportTopLeft.withRelativeRow(-scroll) ?: viewportTopLeft
                    realScreen.scrollLines(0, currentViewportSize.rows - scroll, -scroll)
                    refresh()
                    return null
                }
            }
            isScrollTrigger(stroke) && (stroke.keyType == KeyType.PAGE_DOWN || isSpaceBarPress(stroke)) -> {
                if (viewportTopLeft.row + currentViewportSize.rows < currentTerminalSize.rows) {
                    var scroll = currentViewportSize.rows
                    if (viewportTopLeft.row + currentViewportSize.rows + scroll >= currentTerminalSize.rows) {
                        scroll = currentTerminalSize.rows - viewportTopLeft.row - currentViewportSize.rows
                    }
                    viewportTopLeft = viewportTopLeft.withRelativeRow(scroll) ?: viewportTopLeft
                    realScreen.scrollLines(0, currentViewportSize.rows - scroll, scroll)
                    refresh()
                    return null
                }
            }
        }
        return stroke
    }

    private fun isSpaceBarPress(keyStroke: KeyStroke): Boolean {
        return keyStroke.keyType == KeyType.CHARACTER && keyStroke.character == ' '
    }

    private fun isScrollTrigger(keyStroke: KeyStroke): Boolean {
        return if (scrollWithCTRL) keyStroke.isCtrlDown else keyStroke.isAltDown
    }

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        super.scrollLines(firstLine, lastLine, distance)
        val vpFirst = viewportTopLeft.row
        val vpRows = viewportSize?.rows ?: 0
        val adjustedFirstLine = maxOf(0, firstLine - vpFirst)
        val adjustedLastLine = minOf(vpRows - 1, lastLine - vpFirst)
        if (adjustedFirstLine <= adjustedLastLine) {
            realScreen.scrollLines(adjustedFirstLine, adjustedLastLine, distance)
        }
    }

    /**
     * Interface for rendering the virtual screen's frame when the real terminal is too small for the virtual screen
     */
    interface FrameRenderer {
        /**
         * Given the size of the real terminal and the current size of the virtual screen, how large should the viewport
         * where the screen content is drawn be?
         * @param realSize Size of the real terminal
         * @param virtualSize Size of the virtual screen
         * @return Size of the viewport where screen content should be drawn
         */
        fun getViewportSize(
            realSize: TerminalSize?,
            virtualSize: TerminalSize?,
        ): TerminalSize?

        /**
         * Returns the top-left coordinate where the viewport starts in the real terminal.
         * @return Top-left position of the viewport
         */
        val viewportOffset: TerminalPosition?

        /**
         * Draws the frame around the viewport.
         * @param graphics Graphics object for drawing
         * @param realSize Size of the real terminal
         * @param virtualSize Size of the virtual screen
         * @param virtualScrollPosition Current scroll position in the virtual screen
         */
        fun drawFrame(
            graphics: TextGraphics?,
            realSize: TerminalSize?,
            virtualSize: TerminalSize?,
            virtualScrollPosition: TerminalPosition?,
        )
    }

    private class DefaultFrameRenderer : FrameRenderer {
        override fun getViewportSize(
            realSize: TerminalSize?,
            virtualSize: TerminalSize?,
        ): TerminalSize? {
            val size = realSize ?: return null
            return if (size.columns > 1 && size.rows > 2) {
                size.withRelativeColumns(-1)?.withRelativeRows(-2)
            } else {
                size
            }
        }

        override val viewportOffset: TerminalPosition
            get() = TerminalPosition.TOP_LEFT_CORNER

        override fun drawFrame(
            graphics: TextGraphics?,
            realSize: TerminalSize?,
            virtualSize: TerminalSize?,
            virtualScrollPosition: TerminalPosition?,
        ) {
            val activeGraphics = graphics ?: return
            val actualRealSize = realSize ?: return
            val actualVirtualSize = virtualSize ?: return
            val actualScrollPosition = virtualScrollPosition ?: return
            if (actualRealSize.columns == 1 || actualRealSize.rows <= 2) {
                return
            }
            val activeViewportSize = getViewportSize(actualRealSize, actualVirtualSize) ?: return
            val graphicsSize = activeGraphics.size ?: return

            activeGraphics.setForegroundColor(TextColor.ANSI.WHITE)
            activeGraphics.setBackgroundColor(TextColor.ANSI.BLACK)
            activeGraphics.fill(' ')
            activeGraphics.putString(0, graphicsSize.rows - 1, "Terminal too small, use ALT+arrows to scroll")

            val horizontalSize =
                ((activeViewportSize.columns.toDouble() / actualVirtualSize.columns.toDouble()) * activeViewportSize.columns).toInt()
            val horizontalScrollable = activeViewportSize.columns - horizontalSize - 1
            val horizontalPosition =
                (
                    horizontalScrollable.toDouble() * (
                        actualScrollPosition.column.toDouble() /
                            (actualVirtualSize.columns - activeViewportSize.columns).toDouble()
                    )
                ).toInt()
            activeGraphics.drawLine(
                TerminalPosition(horizontalPosition, graphicsSize.rows - 2),
                TerminalPosition(horizontalPosition + horizontalSize, graphicsSize.rows - 2),
                Symbols.BLOCK_MIDDLE,
            )

            val verticalSize =
                ((activeViewportSize.rows.toDouble() / actualVirtualSize.rows.toDouble()) * activeViewportSize.rows).toInt()
            val verticalScrollable = activeViewportSize.rows - verticalSize - 1
            val verticalPosition =
                (
                    verticalScrollable.toDouble() * (
                        actualScrollPosition.row.toDouble() /
                            (actualVirtualSize.rows - activeViewportSize.rows).toDouble()
                    )
                ).toInt()
            activeGraphics.drawLine(
                TerminalPosition(graphicsSize.columns - 1, verticalPosition),
                TerminalPosition(graphicsSize.columns - 1, verticalPosition + verticalSize),
                Symbols.BLOCK_MIDDLE,
            )
        }
    }
}
