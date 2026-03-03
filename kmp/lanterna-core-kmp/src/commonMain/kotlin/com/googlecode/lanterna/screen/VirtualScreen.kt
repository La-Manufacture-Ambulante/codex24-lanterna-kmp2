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
import kotlin.math.max
import kotlin.math.min

open class VirtualScreen(screen: Screen) : AbstractScreen(screen.getTerminalSize()) {
    private val realScreen: Screen
    private val frameRenderer: FrameRenderer
    private var minimumSize: TerminalSize
    private var viewportTopLeft: TerminalPosition
    private var viewportSize: TerminalSize
    private var scrollWithCTRL: Boolean

    init {
        this.frameRenderer = DefaultFrameRenderer()
        this.realScreen = screen
        this.minimumSize = screen.getTerminalSize()
        this.viewportTopLeft = TerminalPosition.TOP_LEFT_CORNER
        this.viewportSize = minimumSize
        this.scrollWithCTRL = false
    }

    open fun setMinimumSize(minimumSize: TerminalSize) {
        this.minimumSize = minimumSize
        val virtualSize = minimumSize.max(realScreen.getTerminalSize())
        if (!minimumSize.equals(virtualSize)) {
            addResizeRequest(virtualSize)
            super.doResizeIfNecessary()
        }
        calculateViewport(realScreen.getTerminalSize())
    }

    open fun getMinimumSize(): TerminalSize {
        return minimumSize
    }

    open fun getViewportSize(): TerminalSize {
        return viewportSize
    }

    open fun setScrollOnCTRL(scrollOnCTRL: Boolean) {
        this.scrollWithCTRL = scrollOnCTRL
    }

    open fun setViewportTopLeft(position: TerminalPosition) {
        viewportTopLeft = position
        while (viewportTopLeft.getColumn() > 0 && viewportTopLeft.getColumn() + viewportSize.getColumns() > minimumSize.getColumns()) {
            viewportTopLeft = viewportTopLeft.withRelativeColumn(-1)
        }
        while (viewportTopLeft.getRow() > 0 && viewportTopLeft.getRow() + viewportSize.getRows() > minimumSize.getRows()) {
            viewportTopLeft = viewportTopLeft.withRelativeRow(-1)
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

    override fun getFrontCharacter(position: TerminalPosition?): TextCharacter? {
        return null
    }

    override fun setCursorPosition(position: TerminalPosition?) {
        super.setCursorPosition(position)
        if (position == null) {
            realScreen.setCursorPosition(null)
            return
        }
        var adjustedPosition = position
        adjustedPosition = adjustedPosition
            .withRelativeColumn(-viewportTopLeft.getColumn())
            .withRelativeRow(-viewportTopLeft.getRow())
        if (adjustedPosition.getColumn() >= 0 && adjustedPosition.getColumn() < viewportSize.getColumns() &&
            adjustedPosition.getRow() >= 0 && adjustedPosition.getRow() < viewportSize.getRows()
        ) {
            realScreen.setCursorPosition(adjustedPosition)
        } else {
            realScreen.setCursorPosition(null)
        }
    }

    @Synchronized
    override fun doResizeIfNecessary(): TerminalSize? {
        val underlyingSize = realScreen.doResizeIfNecessary() ?: return null

        val newVirtualSize = calculateViewport(underlyingSize)
        if (!getTerminalSize().equals(newVirtualSize)) {
            addResizeRequest(newVirtualSize)
            return super.doResizeIfNecessary()
        }
        return newVirtualSize
    }

    private fun calculateViewport(realTerminalSize: TerminalSize): TerminalSize {
        val newVirtualSize = minimumSize.max(realTerminalSize)
        if (newVirtualSize.equals(realTerminalSize)) {
            viewportSize = realTerminalSize
            viewportTopLeft = TerminalPosition.TOP_LEFT_CORNER
        } else {
            val newViewportSize = frameRenderer.getViewportSize(realTerminalSize, newVirtualSize)
            if (newViewportSize.getRows() > viewportSize.getRows()) {
                viewportTopLeft = viewportTopLeft.withRow(
                    max(0, viewportTopLeft.getRow() - (newViewportSize.getRows() - viewportSize.getRows()))
                )
            }
            if (newViewportSize.getColumns() > viewportSize.getColumns()) {
                viewportTopLeft = viewportTopLeft.withColumn(
                    max(0, viewportTopLeft.getColumn() - (newViewportSize.getColumns() - viewportSize.getColumns()))
                )
            }
            viewportSize = newViewportSize
        }
        return newVirtualSize
    }

    @Throws(IOException::class)
    override fun refresh(refreshType: RefreshType) {
        setCursorPosition(getCursorPosition())
        if (!viewportSize.equals(realScreen.getTerminalSize())) {
            frameRenderer.drawFrame(
                realScreen.newTextGraphics(),
                realScreen.getTerminalSize(),
                getTerminalSize(),
                viewportTopLeft
            )
        }

        val viewportOffset = frameRenderer.getViewportOffset()
        if (realScreen is AbstractScreen) {
            val asAbstractScreen = realScreen as AbstractScreen
            getBackBuffer().copyTo(
                asAbstractScreen.getBackBuffer(),
                viewportTopLeft.getRow(),
                viewportSize.getRows(),
                viewportTopLeft.getColumn(),
                viewportSize.getColumns(),
                viewportOffset.getRow(),
                viewportOffset.getColumn()
            )
        } else {
            for (y in 0 until viewportSize.getRows()) {
                for (x in 0 until viewportSize.getColumns()) {
                    realScreen.setCharacter(
                        x + viewportOffset.getColumn(),
                        y + viewportOffset.getRow(),
                        getBackBuffer().getCharacterAt(
                            x + viewportTopLeft.getColumn(),
                            y + viewportTopLeft.getRow()
                        )
                    )
                }
            }
        }
        realScreen.refresh(refreshType)
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        return filter(realScreen.pollInput())
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? {
        return filter(realScreen.readInput())
    }

    @Throws(IOException::class)
    private fun filter(keyStroke: KeyStroke?): KeyStroke? {
        if (keyStroke == null) {
            return null
        } else if (isScrollTrigger(keyStroke) && keyStroke.getKeyType() == KeyType.ARROW_LEFT) {
            if (viewportTopLeft.getColumn() > 0) {
                viewportTopLeft = viewportTopLeft.withRelativeColumn(-1)
                refresh()
                return null
            }
        } else if (isScrollTrigger(keyStroke) && keyStroke.getKeyType() == KeyType.ARROW_RIGHT) {
            if (viewportTopLeft.getColumn() + viewportSize.getColumns() < getTerminalSize().getColumns()) {
                viewportTopLeft = viewportTopLeft.withRelativeColumn(1)
                refresh()
                return null
            }
        } else if (isScrollTrigger(keyStroke) && keyStroke.getKeyType() == KeyType.ARROW_UP) {
            if (viewportTopLeft.getRow() > 0) {
                viewportTopLeft = viewportTopLeft.withRelativeRow(-1)
                realScreen.scrollLines(0, viewportSize.getRows() - 1, -1)
                refresh()
                return null
            }
        } else if (isScrollTrigger(keyStroke) && keyStroke.getKeyType() == KeyType.ARROW_DOWN) {
            if (viewportTopLeft.getRow() + viewportSize.getRows() < getTerminalSize().getRows()) {
                viewportTopLeft = viewportTopLeft.withRelativeRow(1)
                realScreen.scrollLines(0, viewportSize.getRows() - 1, 1)
                refresh()
                return null
            }
        } else if (isScrollTrigger(keyStroke) && keyStroke.getKeyType() == KeyType.PAGE_UP) {
            if (viewportTopLeft.getRow() > 0) {
                val scroll = min(viewportSize.getRows(), viewportTopLeft.getRow())
                viewportTopLeft = viewportTopLeft.withRelativeRow(-scroll)
                realScreen.scrollLines(0, viewportSize.getRows() - scroll, -scroll)
                refresh()
                return null
            }
        } else if (isScrollTrigger(keyStroke) && (keyStroke.getKeyType() == KeyType.PAGE_DOWN || isSpaceBarPress(keyStroke))) {
            if (viewportTopLeft.getRow() + viewportSize.getRows() < getTerminalSize().getRows()) {
                var scroll = viewportSize.getRows()
                if (viewportTopLeft.getRow() + viewportSize.getRows() + scroll >= getTerminalSize().getRows()) {
                    scroll = getTerminalSize().getRows() - viewportTopLeft.getRow() - viewportSize.getRows()
                }
                viewportTopLeft = viewportTopLeft.withRelativeRow(scroll)
                realScreen.scrollLines(0, viewportSize.getRows() - scroll, scroll)
                refresh()
                return null
            }
        }
        return keyStroke
    }

    private fun isScrollTrigger(keyStroke: KeyStroke): Boolean {
        return if (scrollWithCTRL) keyStroke.isCtrlDown() else keyStroke.isAltDown()
    }

    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        super.scrollLines(firstLine, lastLine, distance)

        val vpFirst = viewportTopLeft.getRow()
        val vpRows = viewportSize.getRows()

        val adaptedFirstLine = max(0, firstLine - vpFirst)
        val adaptedLastLine = min(vpRows - 1, lastLine - vpFirst)

        if (adaptedFirstLine <= adaptedLastLine) {
            realScreen.scrollLines(adaptedFirstLine, adaptedLastLine, distance)
        }
    }

    interface FrameRenderer {
        fun getViewportSize(realSize: TerminalSize, virtualSize: TerminalSize): TerminalSize

        fun getViewportOffset(): TerminalPosition

        fun drawFrame(
            graphics: TextGraphics,
            realSize: TerminalSize,
            virtualSize: TerminalSize,
            virtualScrollPosition: TerminalPosition
        )
    }

    private class DefaultFrameRenderer : FrameRenderer {
        override fun getViewportSize(realSize: TerminalSize, virtualSize: TerminalSize): TerminalSize {
            return if (realSize.getColumns() > 1 && realSize.getRows() > 2) {
                realSize.withRelativeColumns(-1).withRelativeRows(-2)
            } else {
                realSize
            }
        }

        override fun getViewportOffset(): TerminalPosition {
            return TerminalPosition.TOP_LEFT_CORNER
        }

        override fun drawFrame(
            graphics: TextGraphics,
            realSize: TerminalSize,
            virtualSize: TerminalSize,
            virtualScrollPosition: TerminalPosition
        ) {
            if (realSize.getColumns() == 1 || realSize.getRows() <= 2) {
                return
            }
            val viewportSize = getViewportSize(realSize, virtualSize)

            graphics.setForegroundColor(TextColor.ANSI.WHITE)
            graphics.setBackgroundColor(TextColor.ANSI.BLACK)
            graphics.fill(' ')
            graphics.putString(0, graphics.getSize().getRows() - 1, "Terminal too small, use ALT+arrows to scroll")

            val horizontalSize =
                ((viewportSize.getColumns().toDouble() / virtualSize.getColumns().toDouble()) * viewportSize.getColumns().toDouble()).toInt()
            var scrollable = viewportSize.getColumns() - horizontalSize - 1
            val horizontalPosition =
                (scrollable.toDouble() * (virtualScrollPosition.getColumn().toDouble() / (virtualSize.getColumns() - viewportSize.getColumns()).toDouble())).toInt()
            graphics.drawLine(
                TerminalPosition(horizontalPosition, graphics.getSize().getRows() - 2),
                TerminalPosition(horizontalPosition + horizontalSize, graphics.getSize().getRows() - 2),
                Symbols.BLOCK_MIDDLE
            )

            val verticalSize =
                ((viewportSize.getRows().toDouble() / virtualSize.getRows().toDouble()) * viewportSize.getRows().toDouble()).toInt()
            scrollable = viewportSize.getRows() - verticalSize - 1
            val verticalPosition =
                (scrollable.toDouble() * (virtualScrollPosition.getRow().toDouble() / (virtualSize.getRows() - viewportSize.getRows()).toDouble())).toInt()
            graphics.drawLine(
                TerminalPosition(graphics.getSize().getColumns() - 1, verticalPosition),
                TerminalPosition(graphics.getSize().getColumns() - 1, verticalPosition + verticalSize),
                Symbols.BLOCK_MIDDLE
            )
        }
    }

    companion object {
        private fun isSpaceBarPress(keyStroke: KeyStroke): Boolean {
            return keyStroke.getKeyType() == KeyType.CHARACTER && keyStroke.getCharacter() == ' '
        }
    }
}
