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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The list box will display a number of items, of which one and only one can be marked as selected.
 * The user can select an item in the list box by pressing the return key or space bar key. If you
 * select one item when another item is already selected, the previously selected item will be
 * deselected and the highlighted item will be the selected one instead.
 * @author Martin
 */
class RadioBoxList<V> @JvmOverloads constructor(preferredSize: TerminalSize? = null) :
    AbstractListBox<V, RadioBoxList<V>>(preferredSize) {

    /**
     * Listener interface that can be attached to the `RadioBoxList` in order to be notified on user actions.
     */
    interface Listener {
        /**
         * Called by the `RadioBoxList` when the user changes which item is selected.
         * @param selectedIndex Index of the newly selected item, or -1 if the selection has been cleared (can only be
         * done programmatically)
         * @param previousSelection The index of the previously selected item which is now no longer selected, or -1 if
         * nothing was previously selected
         */
        fun onSelectionChanged(selectedIndex: Int, previousSelection: Int)
    }

    private val listeners = CopyOnWriteArrayList<Listener>()
    private var checkedIndex: Int = -1

    var checkedItemIndex: Int
        get() = checkedIndex
        @Synchronized set(index) {
            if (index < -1 || index >= itemCount) {
                return
            }
            setCheckedIndex(index)
        }

    var checkedItem: V?
        @Synchronized get() {
            if (checkedIndex == -1 || checkedIndex >= itemCount) {
                return null
            }
            return getItemAt(checkedIndex)
        }
        @Synchronized set(item) {
            if (item == null) {
                setCheckedIndex(-1)
            } else {
                checkedItemIndex = indexOf(item)
            }
        }

    override fun createDefaultListItemRenderer(): ListItemRenderer<V, RadioBoxList<V>> {
        return RadioBoxListItemRenderer()
    }

    @Synchronized
    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (isKeyboardActivationStroke(keyStroke)) {
            setCheckedIndex(getSelectedIndex())
            return super.handleKeyStroke(keyStroke)
        }

        if (keyStroke.keyType == KeyType.MOUSE_EVENT) {
            val mouseAction = keyStroke as MouseAction
            val actionType = mouseAction.actionType
            if (isMouseMove(keyStroke) ||
                actionType == MouseActionType.CLICK_RELEASE ||
                actionType == MouseActionType.SCROLL_UP ||
                actionType == MouseActionType.SCROLL_DOWN
            ) {
                return super.handleKeyStroke(keyStroke)
            }

            val existingIndex = getSelectedIndex()
            val newIndex = getIndexByMouseAction(mouseAction)
            if (existingIndex != newIndex || !isFocused) {
                val result = super.handleKeyStroke(keyStroke)
                setCheckedIndex(getSelectedIndex())
                return result
            }
            setCheckedIndex(getSelectedIndex())
            return Interactable.Result.HANDLED
        }

        return super.handleKeyStroke(keyStroke)
    }

    @Synchronized
    override fun removeItem(index: Int): V {
        val item = super.removeItem(index)
        if (index < checkedIndex) {
            checkedIndex--
        }
        while (checkedIndex >= itemCount) {
            checkedIndex--
        }
        return item
    }

    @Synchronized
    override fun clearItems(): RadioBoxList<V>? {
        setCheckedIndex(-1)
        return super.clearItems()
    }

    @Synchronized
    fun isChecked(item: V?): Boolean? {
        if (item == null) {
            return null
        }
        if (indexOf(item) == -1) {
            return null
        }
        return checkedIndex == indexOf(item)
    }

    @Synchronized
    fun isChecked(index: Int): Boolean {
        if (index < 0 || index >= itemCount) {
            return false
        }
        return checkedIndex == index
    }

    @Synchronized
    fun clearSelection() {
        setCheckedIndex(-1)
    }

    /**
     * Adds a new listener to the `RadioBoxList` that will be called on certain user actions.
     * @param listener Listener to attach to this `RadioBoxList`
     * @return Itself
     */
    fun addListener(listener: Listener?): RadioBoxList<V> {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener)
        }
        return this
    }

    /**
     * Removes a listener from this `RadioBoxList` so that if it had been added earlier, it will no longer be called
     * on user actions.
     * @param listener Listener to remove from this `RadioBoxList`
     * @return Itself
     */
    fun removeListener(listener: Listener?): RadioBoxList<V> {
        if (listener != null) {
            listeners.remove(listener)
        }
        return this
    }

    private fun setCheckedIndex(index: Int) {
        val previouslyChecked = checkedIndex
        checkedIndex = index
        invalidate()
        runOnGUIThreadIfExistsOtherwiseRunDirect(
            Runnable {
                for (listener in listeners) {
                    listener.onSelectionChanged(checkedIndex, previouslyChecked)
                }
            },
        )
    }

    /**
     * Default renderer for this component which is used unless overridden. The selected state is drawn on the left side
     * of the item label using a "< >" block filled with an "o" if the item is the selected one.
     * @param <V> Type of items in the [RadioBoxList]
     */
    class RadioBoxListItemRenderer<V> : ListItemRenderer<V, RadioBoxList<V>>() {
        override fun getHotSpotPositionOnLine(selectedIndex: Int): Int {
            return 1
        }

        protected fun getItemText(listBox: RadioBoxList<V>, index: Int, item: V?): String {
            return (item ?: "<null>").toString()
        }

        override fun getLabel(listBox: RadioBoxList<V>?, index: Int, item: V?): String {
            val activeListBox = listBox ?: return "< > ${getItemTextFallback(item)}"
            val check = if (activeListBox.checkedItemIndex == index) "o" else " "
            return "<$check> ${getItemText(activeListBox, index, item)}"
        }

        override fun drawItem(
            graphics: TextGUIGraphics?,
            listBox: RadioBoxList<V>?,
            index: Int,
            item: V?,
            selected: Boolean,
            focused: Boolean,
        ) {
            val activeGraphics = graphics ?: return
            val activeListBox = listBox ?: return
            val themeDefinition: ThemeDefinition =
                activeListBox.theme?.getDefinition(RadioBoxList::class.java) ?: return
            val itemStyle: ThemeStyle =
                when {
                    selected && !focused -> themeDefinition.selected
                    selected -> themeDefinition.active
                    focused -> themeDefinition.insensitive
                    else -> themeDefinition.normal
                } ?: return

            if (themeDefinition.getBooleanProperty("CLEAR_WITH_NORMAL", false)) {
                activeGraphics.applyThemeStyle(themeDefinition.normal)
                activeGraphics.fill(' ')
                activeGraphics.applyThemeStyle(itemStyle)
            } else {
                activeGraphics.applyThemeStyle(itemStyle)
                activeGraphics.fill(' ')
            }

            val brackets =
                "${themeDefinition.getCharacter("LEFT_BRACKET", '<')} ${themeDefinition.getCharacter("RIGHT_BRACKET", '>')}"
            if (themeDefinition.getBooleanProperty("FIXED_BRACKET_COLOR", false)) {
                activeGraphics.applyThemeStyle(themeDefinition.preLight)
                activeGraphics.putString(0, 0, brackets)
                activeGraphics.applyThemeStyle(itemStyle)
            } else {
                activeGraphics.putString(0, 0, brackets)
            }

            val text = getItemText(activeListBox, index, item)
            activeGraphics.putString(4, 0, text)

            val itemChecked = activeListBox.checkedItemIndex == index
            val marker = themeDefinition.getCharacter("MARKER", 'o')
            if (themeDefinition.getBooleanProperty("MARKER_WITH_NORMAL", false)) {
                activeGraphics.applyThemeStyle(themeDefinition.normal)
            }
            if (selected && focused && themeDefinition.getBooleanProperty("HOTSPOT_PRELIGHT", false)) {
                activeGraphics.applyThemeStyle(themeDefinition.preLight)
            }
            activeGraphics.setCharacter(1, 0, if (itemChecked) marker else ' ')
        }

        private fun getItemTextFallback(item: V?): String {
            return (item ?: "<null>").toString()
        }
    }
}
