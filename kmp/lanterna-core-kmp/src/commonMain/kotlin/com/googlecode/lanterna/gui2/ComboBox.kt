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
@file:Suppress("ktlint:standard:max-line-length")

package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This is a simple combo box implementation that allows the user to select one out of multiple items through a
 * drop-down menu. If the combo box is not in read-only mode, the user can also enter free text in the combo box, much
 * like a `TextBox`.
 * @param <V> Type to use for the items in the combo box
 * @author Martin
 */
class ComboBox<V>(items: Collection<V>, selectedIndex: Int) : AbstractInteractableComponent<ComboBox<V>?>() {
    /**
     * Listener interface that can be used to catch user events on the combo box
     */
    interface Listener {
        /**
         * This method is called whenever the user changes selection from one item to another in the combo box
         * @param selectedIndex Index of the item which is now selected
         * @param previousSelection Index of the item which was previously selected
         * @param changedByUserInteraction If `true` then this selection change happened because of user
         * interaction with the combo box. If `false` then the selected item was set programmatically.
         */
        fun onSelectionChanged(
            selectedIndex: Int,
            previousSelection: Int,
            changedByUserInteraction: Boolean,
        )
    }

    private val items: MutableList<V> = ArrayList()
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    private var popupWindow: PopupWindow? = null
    var text: String = ""
        private set
    private var selectedIndex: Int = 0

    private var readOnly: Boolean = true
    private var dropDownFocused: Boolean = true

    /**
     * For writable combo boxes, this method returns the position where the text input cursor is right now. Meaning, if
     * the user types some character, where are those are going to be inserted in the string that is currently
     * displayed. If the text input position equals the size of the currently displayed text, new characters will be
     * appended at the end. The user can usually move the text input position by using left and right arrow keys on the
     * keyboard.
     * @return Current text input position
     */
    var textInputPosition: Int = 0
        private set

    /**
     * Returns the number of items to display in drop down at one time, if there are more items in the model there will
     * be a scrollbar to help the user navigate. If this returns 0, the combo box will always grow to show all items in
     * the list, which might cause undesired effects if you put really a lot of items into the combo box.
     *
     * @return Number of items (rows) that will be displayed in the combo box, or 0 if the combo box will always grow to
     * accommodate
     */
    var dropDownNumberOfRows: Int = 10

    constructor(vararg items: V) : this(items.asList())

    constructor(items: Collection<V>) : this(items, if (items.isEmpty()) -1 else 0)

    constructor(initialText: String, items: Collection<V>) : this(items, -1) {
        this.text = initialText
    }

    init {
        for (item in items) {
            if (item == null) {
                throw IllegalArgumentException("Cannot add null elements to a ComboBox")
            }
            this.items.add(item)
        }
        this.selectedIndex = selectedIndex
        this.readOnly = true
        this.dropDownFocused = true
        this.textInputPosition = 0
        this.dropDownNumberOfRows = 10
        this.text = if (selectedIndex != -1) this.items[selectedIndex].toString() else ""
    }

    @Synchronized
    fun addItem(item: V?): ComboBox<V> {
        if (item == null) {
            throw IllegalArgumentException("Cannot add null elements to a ComboBox")
        }
        items.add(item)
        if (selectedIndex == -1 && items.size == 1) {
            setSelectedIndex(0)
        }
        invalidate()
        return this
    }

    @Synchronized
    fun addItem(
        index: Int,
        item: V?,
    ): ComboBox<V> {
        if (item == null) {
            throw IllegalArgumentException("Cannot add null elements to a ComboBox")
        }
        items.add(index, item)
        if (index <= selectedIndex) {
            setSelectedIndex(selectedIndex + 1)
        }
        invalidate()
        return this
    }

    @Synchronized
    fun clearItems(): ComboBox<V> {
        items.clear()
        setSelectedIndex(-1)
        invalidate()
        return this
    }

    @Synchronized
    fun removeItem(item: V?): ComboBox<V> {
        val index = items.indexOf(item)
        if (index == -1) {
            return this
        }
        return removeItem(index)
    }

    @Synchronized
    fun removeItem(index: Int): ComboBox<V> {
        items.removeAt(index)
        if (index < selectedIndex) {
            setSelectedIndex(selectedIndex - 1)
        } else if (index == selectedIndex) {
            setSelectedIndex(-1)
        }
        invalidate()
        return this
    }

    @Synchronized
    fun setItem(
        index: Int,
        item: V?,
    ): ComboBox<V> {
        if (item == null) {
            throw IllegalArgumentException("Cannot add null elements to a ComboBox")
        }
        items[index] = item
        invalidate()
        return this
    }

    @Synchronized
    fun getItemCount(): Int {
        return items.size
    }

    @Synchronized
    fun getItem(index: Int): V {
        return items[index]
    }

    @Synchronized
    fun setReadOnly(readOnly: Boolean): ComboBox<V> {
        this.readOnly = readOnly
        if (readOnly) {
            dropDownFocused = true
        }
        return this
    }

    fun isReadOnly(): Boolean {
        return readOnly
    }

    fun isDropDownFocused(): Boolean {
        return dropDownFocused || isReadOnly()
    }

    fun setSelectedIndex(selectedIndex: Int) {
        setSelectedIndex(selectedIndex, false)
    }

    @Synchronized
    private fun setSelectedIndex(
        selectedIndex: Int,
        changedByUserInteraction: Boolean,
    ) {
        if (items.size <= selectedIndex || selectedIndex < -1) {
            throw IndexOutOfBoundsException("Illegal argument to ComboBox.setSelectedIndex: $selectedIndex")
        }
        val oldSelection = this.selectedIndex
        this.selectedIndex = selectedIndex
        if (selectedIndex == -1) {
            updateText("")
        } else {
            updateText(items[selectedIndex].toString())
        }
        runOnGUIThreadIfExistsOtherwiseRunDirect(
            Runnable {
                for (listener in listeners) {
                    listener.onSelectionChanged(selectedIndex, oldSelection, changedByUserInteraction)
                }
            },
        )
        invalidate()
    }

    @Synchronized
    fun setSelectedItem(item: V?) {
        if (item == null) {
            setSelectedIndex(-1)
        } else {
            val indexOf = items.indexOf(item)
            if (indexOf != -1) {
                setSelectedIndex(indexOf)
            } else if (!readOnly) {
                updateText(item.toString())
            }
        }
    }

    private fun updateText(newText: String) {
        text = newText
        if (textInputPosition > text.length) {
            textInputPosition = text.length
        }
    }

    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    @Synchronized
    fun getSelectedItem(): V? {
        return if (getSelectedIndex() > -1) getItem(getSelectedIndex()) else null
    }

    fun addListener(listener: Listener?): ComboBox<V> {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener)
        }
        return this
    }

    fun removeListener(listener: Listener?): ComboBox<V> {
        listeners.remove(listener)
        return this
    }

    override fun afterEnterFocus(
        direction: Interactable.FocusChangeDirection?,
        previouslyInFocus: Interactable?,
    ) {
        if (direction == Interactable.FocusChangeDirection.RIGHT && !isReadOnly()) {
            dropDownFocused = false
            selectedIndex = 0
        }
    }

    @Synchronized
    override fun afterLeaveFocus(
        direction: Interactable.FocusChangeDirection?,
        nextInFocus: Interactable?,
    ) {
        popupWindow?.close()
    }

    override fun createDefaultRenderer(): InteractableRenderer<ComboBox<V>?> {
        return DefaultComboBoxRenderer()
    }

    @Synchronized
    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        return if (isReadOnly()) {
            handleReadOnlyCBKeyStroke(keyStroke)
        } else {
            handleEditableCBKeyStroke(keyStroke)
        }
    }

    private fun handleReadOnlyCBKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        when (keyStroke.keyType) {
            KeyType.CHARACTER, KeyType.ENTER -> {
                if (isKeyboardActivationStroke(keyStroke)) {
                    showPopup(keyStroke)
                }
                return super.handleKeyStroke(keyStroke)
            }

            KeyType.MOUSE_EVENT -> {
                if (isMouseActivationStroke(keyStroke)) {
                    showPopup(keyStroke)
                }
            }

            else -> {}
        }
        return super.handleKeyStroke(keyStroke)
    }

    protected fun showPopup(keyStroke: KeyStroke?) {
        val popup = PopupWindow()
        popupWindow = popup
        popup.position = toGlobal(TerminalPosition(0, 1))
        val gui = textGUI as? WindowBasedTextGUI ?: return
        gui.addWindow(popup)
        gui.setActiveWindow(popup)
    }

    private fun handleEditableCBKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (isDropDownFocused()) {
            when (keyStroke.keyType) {
                KeyType.REVERSE_TAB, KeyType.ARROW_LEFT -> {
                    dropDownFocused = false
                    textInputPosition = text.length
                    return Interactable.Result.HANDLED
                }

                else -> return handleReadOnlyCBKeyStroke(keyStroke)
            }
        }

        when (keyStroke.keyType) {
            KeyType.CHARACTER -> {
                text = text.substring(0, textInputPosition) + keyStroke.character + text.substring(textInputPosition)
                textInputPosition++
                return Interactable.Result.HANDLED
            }

            KeyType.TAB -> {
                dropDownFocused = true
                return Interactable.Result.HANDLED
            }

            KeyType.BACKSPACE -> {
                if (textInputPosition > 0) {
                    text = text.substring(0, textInputPosition - 1) + text.substring(textInputPosition)
                    textInputPosition--
                }
                return Interactable.Result.HANDLED
            }

            KeyType.DELETE -> {
                if (textInputPosition < text.length) {
                    text = text.substring(0, textInputPosition) + text.substring(textInputPosition + 1)
                }
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_LEFT -> {
                if (textInputPosition > 0) {
                    textInputPosition--
                } else {
                    return Interactable.Result.MOVE_FOCUS_LEFT
                }
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_RIGHT -> {
                if (textInputPosition < text.length) {
                    textInputPosition++
                } else {
                    dropDownFocused = true
                    return Interactable.Result.HANDLED
                }
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_DOWN -> {
                if (selectedIndex < items.size - 1) {
                    setSelectedIndex(selectedIndex + 1, true)
                }
                return Interactable.Result.HANDLED
            }

            KeyType.ARROW_UP -> {
                if (selectedIndex > 0) {
                    setSelectedIndex(selectedIndex - 1, true)
                }
                return Interactable.Result.HANDLED
            }

            else -> {}
        }
        return super.handleKeyStroke(keyStroke)
    }

    private inner class PopupWindow : BasicWindow() {
        private val listBox: ActionListBox

        init {
            setHints(
                listOf(
                    Window.Hint.NO_FOCUS,
                    Window.Hint.FIXED_POSITION,
                    Window.Hint.MENU_POPUP,
                ),
            )
            listBox = ActionListBox((this@ComboBox.size ?: TerminalSize.ZERO).withRows(getItemCount()))
            for (i in 0 until getItemCount()) {
                val item = items[i]
                val index = i
                listBox.addItem(
                    item.toString(),
                    Runnable {
                        setSelectedIndex(index, true)
                        close()
                    },
                )
            }
            listBox.setSelectedIndex(getSelectedIndex())
            val dropDownListPreferredSize = listBox.preferredSize ?: TerminalSize.ZERO
            if (dropDownNumberOfRows > 0) {
                listBox.setPreferredSize(dropDownListPreferredSize.withRows(kotlin.math.min(dropDownNumberOfRows, dropDownListPreferredSize.rows)))
            }
            component = listBox
        }

        override fun close() {
            super.close()
            popupWindow = null
        }

        @Synchronized
        override fun handleInput(keyStroke: KeyStroke?): Boolean {
            if (keyStroke?.keyType == KeyType.ESCAPE) {
                close()
                return true
            }
            return super.handleInput(keyStroke)
        }
    }

    abstract class ComboBoxRenderer<V> : InteractableRenderer<ComboBox<V>?>

    class DefaultComboBoxRenderer<V> : ComboBoxRenderer<V>() {
        private var textVisibleLeftPosition: Int = 0

        override fun getCursorLocation(comboBox: ComboBox<V>?): TerminalPosition? {
            val cb = comboBox ?: return null
            if (cb.isDropDownFocused()) {
                val themeDefinition = cb.themeDefinition ?: return null
                return if (themeDefinition.isCursorVisible) {
                    TerminalPosition((cb.size ?: TerminalSize.ZERO).columns - 1, 0)
                } else {
                    null
                }
            }

            val textInputPosition = cb.textInputPosition
            val textInputColumn = TerminalTextUtils.getColumnWidth(cb.text.substring(0, textInputPosition))
            return TerminalPosition(textInputColumn - textVisibleLeftPosition, 0)
        }

        override fun getPreferredSize(comboBox: ComboBox<V>?): TerminalSize {
            val cb = comboBox ?: return TerminalSize.ONE
            var size =
                TerminalSize.ONE.withColumns((if (cb.getItemCount() == 0) TerminalTextUtils.getColumnWidth(cb.text) else 0) + 2)
                    ?: TerminalSize.ONE
            synchronized(cb) {
                for (i in 0 until cb.getItemCount()) {
                    val item = cb.getItem(i)
                    size = size.max(TerminalSize(TerminalTextUtils.getColumnWidth(item.toString()) + 3, 1)) ?: size
                }
            }
            return size
        }

        override fun drawComponent(
            graphics: TextGUIGraphics?,
            comboBox: ComboBox<V>?,
        ) {
            val g = graphics ?: return
            val cb = comboBox ?: return
            val themeDefinition: ThemeDefinition = cb.themeDefinition ?: return
            if (cb.isReadOnly()) {
                g.applyThemeStyle(themeDefinition.normal)
            } else {
                if (cb.isFocused) {
                    g.applyThemeStyle(themeDefinition.active)
                } else {
                    g.applyThemeStyle(themeDefinition.preLight)
                }
            }
            g.fill(' ')
            val editableArea = (g.size ?: TerminalSize.ZERO).columns - 2
            val textInputPosition = cb.textInputPosition
            val columnsToInputPosition = TerminalTextUtils.getColumnWidth(cb.text.substring(0, textInputPosition))
            if (columnsToInputPosition < textVisibleLeftPosition) {
                textVisibleLeftPosition = columnsToInputPosition
            }
            if (columnsToInputPosition - textVisibleLeftPosition >= editableArea) {
                textVisibleLeftPosition = columnsToInputPosition - editableArea + 1
            }
            if (columnsToInputPosition - textVisibleLeftPosition + 1 == editableArea &&
                cb.text.length > textInputPosition &&
                TerminalTextUtils.isCharCJK(cb.text[textInputPosition])
            ) {
                textVisibleLeftPosition++
            }

            val textToDraw = TerminalTextUtils.fitString(cb.text, textVisibleLeftPosition, editableArea)
            g.putString(0, 0, textToDraw)
            g.applyThemeStyle(themeDefinition.insensitive)
            g.setCharacter(editableArea, 0, themeDefinition.getCharacter("POPUP_SEPARATOR", Symbols.SINGLE_LINE_VERTICAL))
            if (cb.isFocused && cb.isDropDownFocused()) {
                g.applyThemeStyle(themeDefinition.selected)
            }
            g.setCharacter(editableArea + 1, 0, themeDefinition.getCharacter("POPUP", Symbols.TRIANGLE_DOWN_POINTING_BLACK))
        }
    }
}
