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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import java.util.ArrayList

/**
 * Base class for several list box implementations.
 */
abstract class AbstractListBox<V, T : AbstractListBox<V, T>?> @JvmOverloads protected constructor(size: TerminalSize? = null) :
    AbstractInteractableComponent<T>() {

    private val items: MutableList<V> = ArrayList()
    private var selectedIndex: Int = -1
    private var listItemRenderer: ListItemRenderer<V, T>? = null
    protected var scrollOffset: TerminalPosition = TerminalPosition(0, 0)

    init {
        setPreferredSize(size)
        setListItemRenderer(createDefaultListItemRenderer())
    }

    override fun createDefaultRenderer(): InteractableRenderer<T?> {
        return DefaultListBoxRenderer()
    }

    protected open fun createDefaultListItemRenderer(): ListItemRenderer<V, T> {
        return ListItemRenderer()
    }

    internal fun getListItemRenderer(): ListItemRenderer<V, T>? {
        return listItemRenderer
    }

    @Synchronized
    fun setListItemRenderer(listItemRenderer: ListItemRenderer<V, T>?): T? {
        var renderer = listItemRenderer
        if (renderer == null) {
            renderer = createDefaultListItemRenderer()
            if (renderer == null) {
                throw IllegalStateException("createDefaultListItemRenderer returned null")
            }
        }
        this.listItemRenderer = renderer
        return self()
    }

    @Synchronized
    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        try {
            when (keyStroke.keyType) {
                KeyType.TAB -> return Interactable.Result.MOVE_FOCUS_NEXT
                KeyType.REVERSE_TAB -> return Interactable.Result.MOVE_FOCUS_PREVIOUS
                KeyType.ARROW_RIGHT -> return Interactable.Result.MOVE_FOCUS_RIGHT
                KeyType.ARROW_LEFT -> return Interactable.Result.MOVE_FOCUS_LEFT
                KeyType.ARROW_DOWN -> {
                    if (items.isEmpty() || selectedIndex == items.size - 1) {
                        return Interactable.Result.MOVE_FOCUS_DOWN
                    }
                    selectedIndex++
                    return Interactable.Result.HANDLED
                }

                KeyType.ARROW_UP -> {
                    if (items.isEmpty() || selectedIndex == 0) {
                        return Interactable.Result.MOVE_FOCUS_UP
                    }
                    selectedIndex--
                    return Interactable.Result.HANDLED
                }

                KeyType.HOME -> {
                    selectedIndex = 0
                    return Interactable.Result.HANDLED
                }

                KeyType.END -> {
                    selectedIndex = items.size - 1
                    return Interactable.Result.HANDLED
                }

                KeyType.PAGE_UP -> {
                    if (size != null) {
                        setSelectedIndex(getSelectedIndex() - size!!.rows)
                    }
                    return Interactable.Result.HANDLED
                }

                KeyType.PAGE_DOWN -> {
                    if (size != null) {
                        setSelectedIndex(getSelectedIndex() + size!!.rows)
                    }
                    return Interactable.Result.HANDLED
                }

                KeyType.CHARACTER -> {
                    if (!keyStroke.isAltDown && !keyStroke.isCtrlDown && selectByCharacter(keyStroke.character)) {
                        return Interactable.Result.HANDLED
                    }
                    return Interactable.Result.UNHANDLED
                }

                KeyType.MOUSE_EVENT -> {
                    val mouseAction = keyStroke as MouseAction
                    val actionType = mouseAction.actionType
                    if (isMouseMove(keyStroke)) {
                        takeFocus()
                        selectedIndex = getIndexByMouseAction(mouseAction)
                        return Interactable.Result.HANDLED
                    }

                    if (actionType == MouseActionType.CLICK_RELEASE) {
                        return Interactable.Result.HANDLED
                    } else if (actionType == MouseActionType.SCROLL_UP) {
                        setSelectedIndex(getSelectedIndex() - 1)
                        return Interactable.Result.HANDLED
                    } else if (actionType == MouseActionType.SCROLL_DOWN) {
                        setSelectedIndex(getSelectedIndex() + 1)
                        return Interactable.Result.HANDLED
                    }

                    selectedIndex = getIndexByMouseAction(mouseAction)
                    return super.handleKeyStroke(keyStroke)
                }

                else -> return Interactable.Result.UNHANDLED
            }
        } finally {
            invalidate()
        }
    }

    protected fun getIndexByMouseAction(click: MouseAction): Int {
        val global = globalPosition ?: TerminalPosition(0, 0)
        val clickPosition = click.position ?: return 0
        val index = clickPosition.row - global.row - scrollOffset.row
        return kotlin.math.min(index, items.size - 1)
    }

    private fun selectByCharacter(character: Char?): Boolean {
        if (character == null || itemCount == 0) {
            return false
        }
        val lower = character.toString().lowercase()[0]
        val selectedIndex = getSelectedIndex()
        for (i in 0 until itemCount) {
            val index = (selectedIndex + i + 1) % itemCount
            val item = getItemAt(index)
            val label = item?.toString()
            if (!label.isNullOrEmpty()) {
                val firstChar = label[0].lowercaseChar()
                if (firstChar == lower) {
                    setSelectedIndex(index)
                    return true
                }
            }
        }
        return false
    }

    @Synchronized
    override fun afterEnterFocus(direction: Interactable.FocusChangeDirection?, previouslyInFocus: Interactable?) {
        if (items.isEmpty()) {
            return
        }

        if (direction == Interactable.FocusChangeDirection.DOWN) {
            selectedIndex = 0
        } else if (direction == Interactable.FocusChangeDirection.UP) {
            selectedIndex = items.size - 1
        }
    }

    @Synchronized
    open fun addItem(item: V?): T? {
        if (item == null) {
            return self()
        }

        items.add(item)
        if (selectedIndex == -1) {
            selectedIndex = 0
        }
        invalidate()
        return self()
    }

    @Synchronized
    open fun removeItem(index: Int): V {
        val existing = items.removeAt(index)
        if (index < selectedIndex) {
            selectedIndex--
        }
        while (selectedIndex >= items.size) {
            selectedIndex--
        }
        invalidate()
        return existing
    }

    @Synchronized
    open fun clearItems(): T? {
        items.clear()
        selectedIndex = -1
        invalidate()
        return self()
    }

    override val isFocusable: Boolean
        get() {
            if (isEmpty) {
                return false
            }
            return super.isFocusable
        }

    @Synchronized
    fun indexOf(item: V?): Int {
        return items.indexOf(item)
    }

    @Synchronized
    fun getItemAt(index: Int): V {
        return items[index]
    }

    val isEmpty: Boolean
        get() = items.isEmpty()

    val itemCount: Int
        get() = items.size

    @Synchronized
    fun getItems(): List<V> {
        return ArrayList(items)
    }

    @Synchronized
    fun setSelectedIndex(index: Int): T? {
        selectedIndex = kotlin.math.max(0, kotlin.math.min(index, items.size - 1))
        invalidate()
        return self()
    }

    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    val selectedItem: V?
        get() {
            if (selectedIndex == -1) {
                return null
            }
            return items[selectedIndex]
        }

    class DefaultListBoxRenderer<V, T : AbstractListBox<V, T>?> : InteractableRenderer<T?> {
        private val verticalScrollBar: ScrollBar = ScrollBar(Direction.VERTICAL)
        private var scrollTopIndex: Int = 0

        override fun getCursorLocation(listBox: T?): TerminalPosition? {
            if (listBox == null) {
                return null
            }
            if (listBox.themeDefinition?.isCursorVisible != true) {
                return null
            }
            val selectedIndex = listBox.getSelectedIndex()
            val columnAccordingToRenderer = listBox.getListItemRenderer()?.getHotSpotPositionOnLine(selectedIndex) ?: -1
            if (columnAccordingToRenderer == -1) {
                return null
            }
            return TerminalPosition(columnAccordingToRenderer, selectedIndex - scrollTopIndex)
        }

        override fun getPreferredSize(listBox: T?): TerminalSize {
            var maxWidth = 5
            var index = 0
            for (item in listBox?.getItems().orEmpty()) {
                val itemString = listBox?.getListItemRenderer()?.getLabel(listBox, index++, item) ?: ""
                val stringLengthInColumns = TerminalTextUtils.getColumnWidth(itemString)
                if (stringLengthInColumns > maxWidth) {
                    maxWidth = stringLengthInColumns
                }
            }
            return TerminalSize(maxWidth + 1, listBox?.itemCount ?: 0)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, listBox: T?) {
            val activeGraphics = graphics ?: return
            val activeListBox = listBox ?: return

            val themeDefinition: ThemeDefinition =
                activeListBox.theme?.getDefinition(AbstractListBox::class.java) ?: return
            val componentHeight = activeGraphics.size?.rows ?: 0
            val selectedIndex = activeListBox.getSelectedIndex()
            val items = activeListBox.getItems()
            val listItemRenderer = activeListBox.getListItemRenderer() ?: return

            if (selectedIndex != -1) {
                if (selectedIndex < scrollTopIndex) {
                    scrollTopIndex = selectedIndex
                } else if (selectedIndex >= componentHeight + scrollTopIndex) {
                    scrollTopIndex = selectedIndex - componentHeight + 1
                }
            }

            if (items.size > componentHeight && items.size - scrollTopIndex < componentHeight) {
                scrollTopIndex = items.size - componentHeight
            }

            activeListBox.scrollOffset = TerminalPosition(0, -scrollTopIndex)

            activeGraphics.applyThemeStyle(themeDefinition.normal)
            activeGraphics.fill(' ')

            val itemSize = (activeGraphics.size ?: TerminalSize.ZERO).withRows(1)
            for (i in scrollTopIndex until items.size) {
                if (i - scrollTopIndex >= componentHeight) {
                    break
                }
                listItemRenderer.drawItem(
                    activeGraphics.newTextGraphics(TerminalPosition(0, i - scrollTopIndex), itemSize),
                    activeListBox,
                    i,
                    items[i],
                    selectedIndex == i,
                    activeListBox.isFocused,
                )
            }

            activeGraphics.applyThemeStyle(themeDefinition.normal)
            if (items.size > componentHeight) {
                verticalScrollBar.onAdded(activeListBox.parent)
                verticalScrollBar.setViewSize(componentHeight)
                verticalScrollBar.setScrollMaximum(items.size)
                verticalScrollBar.setScrollPosition(scrollTopIndex)
                verticalScrollBar.draw(
                    activeGraphics.newTextGraphics(
                        TerminalPosition((activeGraphics.size ?: TerminalSize.ZERO).columns - 1, 0),
                        TerminalSize(1, (activeGraphics.size ?: TerminalSize.ZERO).rows),
                    ),
                )
            }
        }
    }

    open class ListItemRenderer<V, T : AbstractListBox<V, T>?> {
        open fun getHotSpotPositionOnLine(selectedIndex: Int): Int {
            return 0
        }

        open fun getLabel(listBox: T?, index: Int, item: V?): String {
            return item?.toString() ?: "<null>"
        }

        open fun drawItem(
            graphics: TextGUIGraphics?,
            listBox: T?,
            index: Int,
            item: V?,
            selected: Boolean,
            focused: Boolean,
        ) {
            val activeGraphics = graphics ?: return
            val activeListBox = listBox ?: return
            val themeDefinition = activeListBox.theme?.getDefinition(AbstractListBox::class.java) ?: return
            if (selected && focused) {
                activeGraphics.applyThemeStyle(themeDefinition.selected)
            } else {
                activeGraphics.applyThemeStyle(themeDefinition.normal)
            }
            var label = getLabel(activeListBox, index, item)
            label = TerminalTextUtils.fitString(label, (activeGraphics.size ?: TerminalSize.ZERO).columns) ?: ""
            while (TerminalTextUtils.getColumnWidth(label) < (activeGraphics.size ?: TerminalSize.ZERO).columns) {
                label += " "
            }
            activeGraphics.putString(0, 0, label)
        }
    }
}
