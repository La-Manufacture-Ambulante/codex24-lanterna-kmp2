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
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import java.util.ArrayList
import java.util.regex.Pattern

/**
 * Editable text component supporting single-line and multi-line modes.
 */
class TextBox constructor(
    preferredSize: TerminalSize?,
    initialContent: String,
    private val style: Style =
        if ((preferredSize != null && preferredSize.rows > 1) || initialContent.contains("\n")) {
            Style.MULTI_LINE
        } else {
            Style.SINGLE_LINE
        },
) : AbstractInteractableComponent<TextBox>() {

    enum class Style {
        SINGLE_LINE,
        MULTI_LINE,
    }

    private val lines: MutableList<String> = ArrayList()
    private var caretPosition: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER
    private var caretWarp: Boolean = false
    private var readOnly: Boolean = false
    private var horizontalFocusSwitching: Boolean = style == Style.SINGLE_LINE
    private var verticalFocusSwitching: Boolean = true
    private val maxLineLength: Int = -1
    internal var longestRow: Int = 1
    private var mask: Char? = null
    private var validationPattern: Pattern? = null
    private var textChangeListener: TextChangeListener? = null

    constructor() : this(TerminalSize(10, 1), "", Style.SINGLE_LINE)

    constructor(initialContent: String) : this(
        null,
        initialContent,
        if (initialContent.contains("\n")) Style.MULTI_LINE else Style.SINGLE_LINE,
    )

    constructor(initialContent: String, style: Style) : this(null, initialContent, style)

    constructor(preferredSize: TerminalSize?) : this(
        preferredSize,
        "",
        if (preferredSize != null && preferredSize.rows > 1) Style.MULTI_LINE else Style.SINGLE_LINE,
    )

    constructor(preferredSize: TerminalSize?, style: Style) : this(preferredSize, "", style)

    constructor(preferredSize: TerminalSize?, initialContent: String) : this(
        preferredSize,
        initialContent,
        if ((preferredSize != null && preferredSize.rows > 1) || initialContent.contains("\n")) {
            Style.MULTI_LINE
        } else {
            Style.SINGLE_LINE
        },
    )

    init {
        setText(initialContent)
        caretPosition = TerminalPosition.TOP_LEFT_CORNER.withColumn(getLine(0).length)!!
        val resolvedPreferredSize = preferredSize ?: TerminalSize(kotlin.math.max(10, longestRow), lines.size)
        setPreferredSize(resolvedPreferredSize)
    }

    var validationRegex: Pattern?
        get() = validationPattern
        @Synchronized set(value) {
            if (value != null) {
                for (line in lines) {
                    if (!validated(line)) {
                        throw IllegalStateException("TextBox validation pattern $value does not match existing content")
                    }
                }
            }
            validationPattern = value
        }

    @Synchronized
    fun setValidationPattern(validationPattern: Pattern?): TextBox {
        validationRegex = validationPattern
        return this
    }

    @Synchronized
    fun setTextChangeListener(textChangeListener: TextChangeListener?): TextBox {
        this.textChangeListener = textChangeListener
        return this
    }

    @Synchronized
    fun setText(text: String): TextBox {
        var split = text.split("\n")
        if (split.isEmpty()) {
            split = listOf("")
        }
        lines.clear()
        longestRow = 1
        for (line in split) {
            addLine(line)
        }
        if (caretPosition.row > lines.size - 1) {
            caretPosition = caretPosition.withRow(lines.size - 1)!!
        }
        if (caretPosition.column > lines[caretPosition.row].length) {
            caretPosition = caretPosition.withColumn(lines[caretPosition.row].length)!!
        }
        invalidate()
        return this
    }

    override val renderer: TextBoxRenderer?
        get() = super.renderer as TextBoxRenderer?

    @Synchronized
    fun addLine(line: String): TextBox {
        val bob = StringBuilder()
        for (i in line.indices) {
            val c = line[i]
            if (c == '\n' && style == Style.MULTI_LINE) {
                val string = bob.toString()
                val lineWidth = TerminalTextUtils.getColumnWidth(string)
                lines.add(string)
                if (longestRow < lineWidth + 1) {
                    longestRow = lineWidth + 1
                }
                addLine(line.substring(i + 1))
                return this
            } else if (Character.isISOControl(c)) {
                continue
            }
            bob.append(c)
        }

        val string = bob.toString()
        if (!validated(string)) {
            throw IllegalStateException("TextBox validation pattern $validationPattern does not match the supplied text")
        }
        val lineWidth = TerminalTextUtils.getColumnWidth(string)
        lines.add(string)
        if (longestRow < lineWidth + 1) {
            longestRow = lineWidth + 1
        }
        fireOnTextChanged(false)
        invalidate()
        return this
    }

    @Synchronized
    fun removeLine(lineIndex: Int): TextBox {
        if (style == Style.SINGLE_LINE) {
            if (lineIndex == 0) {
                setText("")
                return this
            }
            throw ArrayIndexOutOfBoundsException("Cannot remove line $lineIndex from a single-line TextBox")
        }

        if (lineIndex < 0 || lineIndex >= lines.size) {
            throw ArrayIndexOutOfBoundsException("Invalid line index for TextBox with ${lines.size} lines: $lineIndex")
        }
        lines.removeAt(lineIndex)
        when {
            caretPosition.row == lineIndex -> setCaretPosition(caretPosition.row, caretPosition.column)
            caretPosition.row > lineIndex -> setCaretPosition(caretPosition.row - 1, caretPosition.column)
        }
        fireOnTextChanged(false)
        return this
    }

    fun setCaretWarp(caretWarp: Boolean): TextBox {
        this.caretWarp = caretWarp
        return this
    }

    fun isCaretWarp(): Boolean = caretWarp

    fun getCaretPosition(): TerminalPosition = caretPosition

    @Synchronized
    fun setCaretPosition(column: Int): TextBox {
        return setCaretPosition(caretPosition.row, column)
    }

    @Synchronized
    fun setCaretPosition(line: Int, column: Int): TextBox {
        var resolvedLine = line
        var resolvedColumn = column
        if (resolvedLine < 0) {
            resolvedLine = 0
        } else if (resolvedLine >= lines.size) {
            resolvedLine = lines.size - 1
        }
        if (resolvedColumn < 0) {
            resolvedColumn = 0
        } else if (resolvedColumn > lines[resolvedLine].length) {
            resolvedColumn = lines[resolvedLine].length
        }
        caretPosition = caretPosition.withRow(resolvedLine)!!.withColumn(resolvedColumn)!!
        return this
    }

    val text: String
        @Synchronized get() {
            val bob = StringBuilder(lines[0])
            for (i in 1 until lines.size) {
                bob.append("\n").append(lines[i])
            }
            return bob.toString()
        }

    fun getTextOrDefault(defaultValueIfEmpty: String): String {
        val text = text
        return if (text.isEmpty()) defaultValueIfEmpty else text
    }

    fun getMask(): Char? = mask

    fun setMask(mask: Char?): TextBox {
        if (mask != null && TerminalTextUtils.isCharCJK(mask)) {
            throw IllegalArgumentException("Cannot use a CJK character as a mask")
        }
        this.mask = mask
        invalidate()
        return this
    }

    fun isReadOnly(): Boolean = readOnly

    fun setReadOnly(readOnly: Boolean): TextBox {
        this.readOnly = readOnly
        invalidate()
        return this
    }

    fun isVerticalFocusSwitching(): Boolean = verticalFocusSwitching

    fun setVerticalFocusSwitching(verticalFocusSwitching: Boolean): TextBox {
        this.verticalFocusSwitching = verticalFocusSwitching
        return this
    }

    fun isHorizontalFocusSwitching(): Boolean = horizontalFocusSwitching

    fun setHorizontalFocusSwitching(horizontalFocusSwitching: Boolean): TextBox {
        this.horizontalFocusSwitching = horizontalFocusSwitching
        return this
    }

    @Synchronized
    fun getLine(index: Int): String = lines[index]

    @Synchronized
    fun getLineCount(): Int = lines.size

    override fun createDefaultRenderer(): TextBoxRenderer {
        return DefaultTextBoxRenderer()
    }

    @Synchronized
    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (readOnly) {
            return handleKeyStrokeReadOnly(keyStroke)
        }

        var line = lines[caretPosition.row]
        var lineWasModified = false
        var result: Interactable.Result? = null

        when (keyStroke.keyType) {
            KeyType.CHARACTER -> {
                if (maxLineLength == -1 || maxLineLength > line.length + 1) {
                    line = line.substring(0, caretPosition.column) + keyStroke.character + line.substring(caretPosition.column)
                    if (validated(line)) {
                        lines[caretPosition.row] = line
                        lineWasModified = true
                        caretPosition = caretPosition.withRelativeColumn(1)!!
                    }
                }
                result = Interactable.Result.HANDLED
            }

            KeyType.BACKSPACE -> {
                if (caretPosition.column > 0) {
                    line = line.substring(0, caretPosition.column - 1) + line.substring(caretPosition.column)
                    if (validated(line)) {
                        lines[caretPosition.row] = line
                        lineWasModified = true
                        caretPosition = caretPosition.withRelativeColumn(-1)!!
                    }
                } else if (style == Style.MULTI_LINE && caretPosition.row > 0) {
                    val concatenatedLines = lines[caretPosition.row - 1] + line
                    if (validated(concatenatedLines)) {
                        lines.removeAt(caretPosition.row)
                        caretPosition = caretPosition.withRelativeRow(-1)!!
                        caretPosition = caretPosition.withColumn(lines[caretPosition.row].length)!!
                        lines[caretPosition.row] = concatenatedLines
                        lineWasModified = true
                    }
                }
                result = Interactable.Result.HANDLED
            }

            KeyType.DELETE -> {
                if (caretPosition.column < line.length) {
                    line = line.substring(0, caretPosition.column) + line.substring(caretPosition.column + 1)
                    if (validated(line)) {
                        lines[caretPosition.row] = line
                        lineWasModified = true
                    }
                } else if (style == Style.MULTI_LINE && caretPosition.row < lines.size - 1) {
                    val concatenatedLines = line + lines[caretPosition.row + 1]
                    if (validated(concatenatedLines)) {
                        lines[caretPosition.row] = concatenatedLines
                        lines.removeAt(caretPosition.row + 1)
                        lineWasModified = true
                    }
                }
                result = Interactable.Result.HANDLED
            }

            KeyType.ARROW_LEFT -> {
                if (caretPosition.column > 0) {
                    caretPosition = caretPosition.withRelativeColumn(-1)!!
                } else if (style == Style.MULTI_LINE && caretWarp && caretPosition.row > 0) {
                    caretPosition = caretPosition.withRelativeRow(-1)!!
                    caretPosition = caretPosition.withColumn(lines[caretPosition.row].length)!!
                } else if (horizontalFocusSwitching) {
                    result = Interactable.Result.MOVE_FOCUS_LEFT
                }
                result = result ?: Interactable.Result.HANDLED
            }

            KeyType.ARROW_RIGHT -> {
                if (caretPosition.column < lines[caretPosition.row].length) {
                    caretPosition = caretPosition.withRelativeColumn(1)!!
                } else if (style == Style.MULTI_LINE && caretWarp && caretPosition.row < lines.size - 1) {
                    caretPosition = caretPosition.withRelativeRow(1)!!
                    caretPosition = caretPosition.withColumn(0)!!
                } else if (horizontalFocusSwitching) {
                    result = Interactable.Result.MOVE_FOCUS_RIGHT
                }
                result = result ?: Interactable.Result.HANDLED
            }

            KeyType.ARROW_UP -> {
                if (canMoveCaretUp()) {
                    performMoveCaretUp()
                } else if (verticalFocusSwitching) {
                    result = Interactable.Result.MOVE_FOCUS_UP
                }
                result = result ?: Interactable.Result.HANDLED
            }

            KeyType.ARROW_DOWN -> {
                if (canMoveCaretDown()) {
                    performMoveCaretDown()
                } else if (verticalFocusSwitching) {
                    result = Interactable.Result.MOVE_FOCUS_DOWN
                }
                result = result ?: Interactable.Result.HANDLED
            }

            KeyType.END -> {
                caretPosition = caretPosition.withColumn(line.length)!!
                result = Interactable.Result.HANDLED
            }

            KeyType.ENTER -> {
                if (style == Style.SINGLE_LINE) {
                    result = Interactable.Result.MOVE_FOCUS_NEXT
                } else {
                    val newLine = line.substring(caretPosition.column)
                    val oldLine = line.substring(0, caretPosition.column)
                    if (validated(newLine) && validated(oldLine)) {
                        lines[caretPosition.row] = oldLine
                        lines.add(caretPosition.row + 1, newLine)
                        caretPosition = caretPosition.withColumn(0)!!.withRelativeRow(1)!!
                        lineWasModified = true
                    }
                    result = Interactable.Result.HANDLED
                }
            }

            KeyType.HOME -> {
                caretPosition = caretPosition.withColumn(0)!!
                result = Interactable.Result.HANDLED
            }

            KeyType.PAGE_DOWN -> {
                caretPosition = caretPosition.withRelativeRow(size?.rows ?: 0)!!
                if (caretPosition.row > lines.size - 1) {
                    caretPosition = caretPosition.withRow(lines.size - 1)!!
                }
                if (lines[caretPosition.row].length < caretPosition.column) {
                    caretPosition = caretPosition.withColumn(lines[caretPosition.row].length)!!
                }
                result = Interactable.Result.HANDLED
            }

            KeyType.PAGE_UP -> {
                caretPosition = caretPosition.withRelativeRow(-(size?.rows ?: 0))!!
                if (caretPosition.row < 0) {
                    caretPosition = caretPosition.withRow(0)!!
                }
                if (lines[caretPosition.row].length < caretPosition.column) {
                    caretPosition = caretPosition.withColumn(lines[caretPosition.row].length)!!
                }
                result = Interactable.Result.HANDLED
            }

            KeyType.MOUSE_EVENT -> {
                if (!isFocused) {
                    result = null
                } else if (isMouseMove(keyStroke)) {
                    result = Interactable.Result.UNHANDLED
                } else {
                    val mouseAction = keyStroke as MouseAction
                    when (mouseAction.actionType) {
                        MouseActionType.SCROLL_UP -> if (canMoveCaretUp()) performMoveCaretUp()
                        MouseActionType.SCROLL_DOWN -> if (canMoveCaretDown()) performMoveCaretDown()
                        else -> {
                            val offset = renderer?.viewTopLeft ?: TerminalPosition.TOP_LEFT_CORNER
                            val globalPosition = globalPosition ?: TerminalPosition.TOP_LEFT_CORNER
                            var newCaretPositionColumn =
                                (mouseAction.position?.column ?: 0) - globalPosition.column + offset.column
                            val newCaretPositionRow =
                                (mouseAction.position?.row ?: 0) - globalPosition.row + offset.row
                            if (newCaretPositionRow in 0 until lines.size) {
                                val newActiveLine = lines[newCaretPositionRow]
                                val minPositionAttempt = 0
                                val maxPositionAttempt = newActiveLine.length
                                newCaretPositionColumn =
                                    kotlin.math.max(
                                        minPositionAttempt,
                                        kotlin.math.min(newCaretPositionColumn, maxPositionAttempt),
                                    )
                                caretPosition = caretPosition.with(TerminalPosition(newCaretPositionColumn, newCaretPositionRow))!!
                            }
                        }
                    }
                    result = Interactable.Result.HANDLED
                }
            }

            else -> {
                // fall through
            }
        }

        if (result == null) {
            result = super.handleKeyStroke(keyStroke)
        } else if (lineWasModified) {
            fireOnTextChanged(true)
        }
        return result
    }

    private fun canMoveCaretUp(): Boolean = caretPosition.row > 0

    private fun canMoveCaretDown(): Boolean = caretPosition.row < lines.size - 1

    private fun performMoveCaretUp() {
        val trueColumnPosition = TerminalTextUtils.getColumnIndex(lines[caretPosition.row], caretPosition.column)
        caretPosition = caretPosition.withRelativeRow(-1)!!
        val line = lines[caretPosition.row]
        caretPosition =
            if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line)) {
                caretPosition.withColumn(line.length)!!
            } else {
                caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition))!!
            }
    }

    private fun performMoveCaretDown() {
        val trueColumnPosition = TerminalTextUtils.getColumnIndex(lines[caretPosition.row], caretPosition.column)
        caretPosition = caretPosition.withRelativeRow(1)!!
        val line = lines[caretPosition.row]
        caretPosition =
            if (trueColumnPosition > TerminalTextUtils.getColumnWidth(line)) {
                caretPosition.withColumn(line.length)!!
            } else {
                caretPosition.withColumn(TerminalTextUtils.getStringCharacterIndex(line, trueColumnPosition))!!
            }
    }

    private fun validated(line: String): Boolean {
        return validationPattern == null || line.isEmpty() || validationPattern!!.matcher(line).matches()
    }

    private fun handleKeyStrokeReadOnly(keyStroke: KeyStroke): Interactable.Result? {
        val activeRenderer = renderer ?: return super.handleKeyStroke(keyStroke)
        when (keyStroke.keyType) {
            KeyType.ARROW_LEFT -> {
                if (activeRenderer.viewTopLeft.column == 0 && horizontalFocusSwitching) {
                    return Interactable.Result.MOVE_FOCUS_LEFT
                }
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeColumn(-1)!!
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_RIGHT -> {
                if (activeRenderer.viewTopLeft.column + (size?.columns ?: 0) == longestRow && horizontalFocusSwitching) {
                    return Interactable.Result.MOVE_FOCUS_RIGHT
                }
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeColumn(1)!!
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_UP -> {
                if (activeRenderer.viewTopLeft.row == 0 && verticalFocusSwitching) {
                    return Interactable.Result.MOVE_FOCUS_UP
                }
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeRow(-1)!!
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_DOWN -> {
                if (activeRenderer.viewTopLeft.row + (size?.rows ?: 0) == lines.size && verticalFocusSwitching) {
                    return Interactable.Result.MOVE_FOCUS_DOWN
                }
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeRow(1)!!
                return Interactable.Result.HANDLED
            }

            KeyType.HOME -> {
                activeRenderer.viewTopLeft = TerminalPosition.TOP_LEFT_CORNER
                return Interactable.Result.HANDLED
            }

            KeyType.END -> {
                activeRenderer.viewTopLeft =
                    TerminalPosition.TOP_LEFT_CORNER.withRow(getLineCount() - (size?.rows ?: 0))!!
                return Interactable.Result.HANDLED
            }

            KeyType.PAGE_DOWN -> {
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeRow(size?.rows ?: 0)!!
                return Interactable.Result.HANDLED
            }

            KeyType.PAGE_UP -> {
                activeRenderer.viewTopLeft = activeRenderer.viewTopLeft.withRelativeRow(-(size?.rows ?: 0))!!
                return Interactable.Result.HANDLED
            }

            else -> {
                // fall through
            }
        }
        return super.handleKeyStroke(keyStroke)
    }

    private fun fireOnTextChanged(initiatedByUserInteraction: Boolean) {
        val listener = textChangeListener ?: return
        listener.onTextChanged(text, initiatedByUserInteraction)
    }

    interface TextBoxRenderer : InteractableRenderer<TextBox?> {
        var viewTopLeft: TerminalPosition
    }

    class DefaultTextBoxRenderer : TextBoxRenderer {
        override var viewTopLeft: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER
            set(position) {
                var adjusted = position
                if (adjusted.column < 0) {
                    adjusted = adjusted.withColumn(0)!!
                }
                if (adjusted.row < 0) {
                    adjusted = adjusted.withRow(0)!!
                }
                field = adjusted
            }

        private val verticalScrollBar = ScrollBar(Direction.VERTICAL)
        private val horizontalScrollBar = ScrollBar(Direction.HORIZONTAL)
        private var hideScrollBars: Boolean = false
        private var unusedSpaceCharacter: Char? = null

        fun setUnusedSpaceCharacter(unusedSpaceCharacter: Char) {
            if (TerminalTextUtils.isCharDoubleWidth(unusedSpaceCharacter)) {
                throw IllegalArgumentException(
                    "Cannot use a double-width character as the unused space character in a TextBox",
                )
            }
            this.unusedSpaceCharacter = unusedSpaceCharacter
        }

        override fun getCursorLocation(component: TextBox?): TerminalPosition? {
            val activeComponent = component ?: return null
            if (activeComponent.isReadOnly()) {
                return null
            }

            var caretPosition = activeComponent.getCaretPosition()
            val line = activeComponent.getLine(caretPosition.row)
            caretPosition = caretPosition.withColumn(kotlin.math.min(caretPosition.column, line.length))!!

            return caretPosition
                .withColumn(TerminalTextUtils.getColumnIndex(line, caretPosition.column))!!
                .withRelativeColumn(-viewTopLeft.column)!!
                .withRelativeRow(-viewTopLeft.row)
        }

        override fun getPreferredSize(component: TextBox?): TerminalSize {
            val activeComponent = component ?: return TerminalSize.ZERO
            return TerminalSize(activeComponent.longestRow, activeComponent.lines.size)
        }

        fun setHideScrollBars(hideScrollBars: Boolean) {
            this.hideScrollBars = hideScrollBars
        }

        override fun drawComponent(graphics: TextGUIGraphics?, component: TextBox?) {
            val activeGraphics = graphics ?: return
            val activeComponent = component ?: return

            var realTextArea = activeGraphics.size ?: TerminalSize.ZERO
            if (realTextArea.rows == 0 || realTextArea.columns == 0) {
                return
            }

            var drawVerticalScrollBar = false
            var drawHorizontalScrollBar = false
            val textBoxLineCount = activeComponent.getLineCount()
            if (!hideScrollBars && textBoxLineCount > realTextArea.rows && realTextArea.columns > 1) {
                realTextArea = realTextArea.withRelativeColumns(-1)!!
                drawVerticalScrollBar = true
            }
            if (!hideScrollBars && activeComponent.longestRow > realTextArea.columns && realTextArea.rows > 1) {
                realTextArea = realTextArea.withRelativeRows(-1)!!
                drawHorizontalScrollBar = true
                if (textBoxLineCount > realTextArea.rows && !drawVerticalScrollBar) {
                    realTextArea = realTextArea.withRelativeColumns(-1)!!
                    drawVerticalScrollBar = true
                }
            }

            drawTextArea(
                activeGraphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, realTextArea),
                activeComponent,
            )

            if (drawVerticalScrollBar) {
                verticalScrollBar.onAdded(activeComponent.parent)
                verticalScrollBar.setViewSize(realTextArea.rows)
                verticalScrollBar.setScrollMaximum(textBoxLineCount)
                verticalScrollBar.setScrollPosition(viewTopLeft.row)
                verticalScrollBar.draw(
                    activeGraphics.newTextGraphics(
                        TerminalPosition((activeGraphics.size ?: TerminalSize.ZERO).columns - 1, 0),
                        TerminalSize(1, (activeGraphics.size ?: TerminalSize.ZERO).rows - if (drawHorizontalScrollBar) 1 else 0),
                    ),
                )
            }
            if (drawHorizontalScrollBar) {
                horizontalScrollBar.onAdded(activeComponent.parent)
                horizontalScrollBar.setViewSize(realTextArea.columns)
                horizontalScrollBar.setScrollMaximum(activeComponent.longestRow - 1)
                horizontalScrollBar.setScrollPosition(viewTopLeft.column)
                horizontalScrollBar.draw(
                    activeGraphics.newTextGraphics(
                        TerminalPosition(0, (activeGraphics.size ?: TerminalSize.ZERO).rows - 1),
                        TerminalSize((activeGraphics.size ?: TerminalSize.ZERO).columns - if (drawVerticalScrollBar) 1 else 0, 1),
                    ),
                )
            }
        }

        private fun drawTextArea(graphics: TextGUIGraphics?, component: TextBox) {
            val activeGraphics = graphics ?: return
            val textAreaSize = activeGraphics.size ?: TerminalSize.ZERO
            if (viewTopLeft.column + textAreaSize.columns > component.longestRow) {
                viewTopLeft = viewTopLeft.withColumn(component.longestRow - textAreaSize.columns)!!
                if (viewTopLeft.column < 0) {
                    viewTopLeft = viewTopLeft.withColumn(0)!!
                }
            }
            if (viewTopLeft.row + textAreaSize.rows > component.getLineCount()) {
                viewTopLeft = viewTopLeft.withRow(component.getLineCount() - textAreaSize.rows)!!
                if (viewTopLeft.row < 0) {
                    viewTopLeft = viewTopLeft.withRow(0)!!
                }
            }

            val themeDefinition: ThemeDefinition = component.themeDefinition ?: return
            when {
                component.isFocused && component.isReadOnly() -> activeGraphics.applyThemeStyle(themeDefinition.selected)
                component.isFocused -> activeGraphics.applyThemeStyle(themeDefinition.active)
                component.isReadOnly() -> activeGraphics.applyThemeStyle(themeDefinition.insensitive)
                else -> activeGraphics.applyThemeStyle(themeDefinition.normal)
            }

            val fillCharacter = unusedSpaceCharacter ?: themeDefinition.getCharacter("FILL", ' ')
            activeGraphics.fill(fillCharacter)

            if (!component.isReadOnly()) {
                var caretPosition = component.getCaretPosition()
                val caretLine = component.getLine(caretPosition.row)
                caretPosition = caretPosition.withColumn(kotlin.math.min(caretPosition.column, caretLine.length))!!

                val trueColumnPosition = TerminalTextUtils.getColumnIndex(caretLine, caretPosition.column)
                if (trueColumnPosition < viewTopLeft.column) {
                    viewTopLeft = viewTopLeft.withColumn(trueColumnPosition)!!
                } else if (trueColumnPosition >= textAreaSize.columns + viewTopLeft.column) {
                    viewTopLeft = viewTopLeft.withColumn(trueColumnPosition - textAreaSize.columns + 1)!!
                }
                if (caretPosition.row < viewTopLeft.row) {
                    viewTopLeft = viewTopLeft.withRow(caretPosition.row)!!
                } else if (caretPosition.row >= textAreaSize.rows + viewTopLeft.row) {
                    viewTopLeft = viewTopLeft.withRow(caretPosition.row - textAreaSize.rows + 1)!!
                }

                if (trueColumnPosition - viewTopLeft.column == (activeGraphics.size ?: TerminalSize.ZERO).columns - 1) {
                    if (caretLine.length > caretPosition.column &&
                        TerminalTextUtils.isCharCJK(caretLine[caretPosition.column])
                    ) {
                        viewTopLeft = viewTopLeft.withRelativeColumn(1)!!
                    }
                }
            }

            for (row in 0 until textAreaSize.rows) {
                val rowIndex = row + viewTopLeft.row
                if (rowIndex >= component.lines.size) {
                    continue
                }
                var line = component.lines[rowIndex]
                if (component.getMask() != null) {
                    val builder = StringBuilder()
                    repeat(line.length) {
                        builder.append(component.getMask())
                    }
                    line = builder.toString()
                }
                activeGraphics.putString(0, row, TerminalTextUtils.fitString(line, viewTopLeft.column, textAreaSize.columns))
            }
        }
    }

    interface TextChangeListener {
        fun onTextChanged(newText: String, changedByUserInteraction: Boolean)
    }
}
