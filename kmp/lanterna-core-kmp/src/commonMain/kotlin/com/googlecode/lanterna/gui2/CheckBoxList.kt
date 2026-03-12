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
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import com.googlecode.lanterna.internal.compat.CopyOnWriteArrayList
import kotlin.collections.ArrayList

/**
 * List box where each item has its own checked state.
 */
class CheckBoxList<V> constructor(preferredSize: TerminalSize? = null) :
    AbstractListBox<V, CheckBoxList<V>>(preferredSize) {
        interface Listener {
            fun onStatusChanged(
                itemIndex: Int,
                checked: Boolean,
            )
        }

        private val listeners: MutableList<Listener> = CopyOnWriteArrayList()
        private val itemStatus: MutableList<Boolean> = ArrayList()

        private var stateForMouseDragged: Boolean = false
        private var minIndexForMouseDragged: Int = 0
        private var maxIndexForMouseDragged: Int = 0

        override fun createDefaultListItemRenderer(): ListItemRenderer<V, CheckBoxList<V>> {
            return CheckBoxListItemRenderer()
        }

        override fun clearItems(): CheckBoxList<V>? {
            itemStatus.clear()
            return super.clearItems()
        }

        override fun addItem(item: V?): CheckBoxList<V>? {
            return addItem(item, false)
        }

        override fun removeItem(index: Int): V {
            val item = super.removeItem(index)
            itemStatus.removeAt(index)
            return item
        }

        fun addItem(
            item: V?,
            checkedState: Boolean,
        ): CheckBoxList<V>? {
            itemStatus.add(checkedState)
            return super.addItem(item)
        }

        fun isChecked(item: V?): Boolean? {
            val index = indexOf(item)
            if (index == -1) {
                return null
            }
            return itemStatus[index]
        }

        fun isChecked(index: Int): Boolean? {
            if (index < 0 || index >= itemStatus.size) {
                return null
            }
            return itemStatus[index]
        }

        fun toggleChecked(index: Int): CheckBoxList<V>? {
            setChecked(index, !(isChecked(index) ?: false))
            return self()
        }

        fun setChecked(
            item: V?,
            checked: Boolean,
        ): CheckBoxList<V>? {
            val index = indexOf(item)
            if (index != -1) {
                setChecked(index, checked)
            }
            return self()
        }

        private fun setChecked(
            index: Int,
            checked: Boolean,
        ) {
            if (index !in 0 until itemStatus.size) {
                return
            }
            itemStatus[index] = checked
            runOnGUIThreadIfExistsOtherwiseRunDirect(
                Runnable {
                    for (listener in listeners) {
                        listener.onStatusChanged(index, checked)
                    }
                },
            )
        }

        fun getCheckedItems(): List<V> {
            val result: MutableList<V> = ArrayList()
            for (i in 0 until itemStatus.size) {
                if (itemStatus[i]) {
                    result.add(getItemAt(i))
                }
            }
            return result
        }

        fun addListener(listener: Listener?): CheckBoxList<V> {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener)
            }
            return this
        }

        fun removeListener(listener: Listener?): CheckBoxList<V> {
            listeners.remove(listener)
            return this
        }

        override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
            if (isKeyboardActivationStroke(keyStroke)) {
                toggleChecked(getSelectedIndex())
                return Interactable.Result.HANDLED
            } else if (keyStroke.keyType == KeyType.MOUSE_EVENT) {
                val mouseAction = keyStroke as MouseAction
                val actionType = mouseAction.actionType

                if (isMouseMove(keyStroke) ||
                    actionType == MouseActionType.CLICK_RELEASE ||
                    actionType == MouseActionType.SCROLL_UP ||
                    actionType == MouseActionType.SCROLL_DOWN
                ) {
                    return super.handleKeyStroke(keyStroke)
                }

                val result = super.handleKeyStroke(keyStroke)
                val newIndex = getIndexByMouseAction(mouseAction)
                if (actionType == MouseActionType.CLICK_DOWN) {
                    stateForMouseDragged = !(isChecked(newIndex) ?: false)
                    setChecked(newIndex, stateForMouseDragged)
                    minIndexForMouseDragged = newIndex
                    maxIndexForMouseDragged = newIndex
                }

                minIndexForMouseDragged = kotlin.math.min(minIndexForMouseDragged, newIndex)
                maxIndexForMouseDragged = kotlin.math.max(maxIndexForMouseDragged, newIndex)

                if (actionType == MouseActionType.DRAG) {
                    for (i in minIndexForMouseDragged..maxIndexForMouseDragged) {
                        setChecked(i, stateForMouseDragged)
                    }
                }
                return result
            }

            return super.handleKeyStroke(keyStroke)
        }

        class CheckBoxListItemRenderer<V> : ListItemRenderer<V, CheckBoxList<V>>() {
            override fun getHotSpotPositionOnLine(selectedIndex: Int): Int {
                return 1
            }

            override fun getLabel(
                listBox: CheckBoxList<V>?,
                index: Int,
                item: V?,
            ): String {
                val lb = listBox ?: return "[ ] <null>"
                val check = if (lb.itemStatus[index]) "x" else " "
                val text = (item ?: "<null>").toString()
                return "[$check] $text"
            }

            override fun drawItem(
                graphics: TextGUIGraphics?,
                listBox: CheckBoxList<V>?,
                index: Int,
                item: V?,
                selected: Boolean,
                focused: Boolean,
            ) {
                val g = graphics ?: return
                val lb = listBox ?: return
                val themeDefinition = lb.theme?.getDefinition(CheckBoxList::class) ?: return
                val itemStyle: ThemeStyle =
                    if (selected && !focused) {
                        themeDefinition.selected ?: themeDefinition.normal ?: return
                    } else if (selected) {
                        themeDefinition.active ?: themeDefinition.normal ?: return
                    } else if (focused) {
                        themeDefinition.insensitive ?: themeDefinition.normal ?: return
                    } else {
                        themeDefinition.normal ?: return
                    }

                if (themeDefinition.getBooleanProperty("CLEAR_WITH_NORMAL", false)) {
                    g.applyThemeStyle(themeDefinition.normal)
                    g.fill(' ')
                    g.applyThemeStyle(itemStyle)
                } else {
                    g.applyThemeStyle(itemStyle)
                    g.fill(' ')
                }

                val brackets = "${themeDefinition.getCharacter("LEFT_BRACKET", '[')} ${themeDefinition.getCharacter("RIGHT_BRACKET", ']')}"
                if (themeDefinition.getBooleanProperty("FIXED_BRACKET_COLOR", false)) {
                    g.applyThemeStyle(themeDefinition.preLight)
                    g.putString(0, 0, brackets)
                    g.applyThemeStyle(itemStyle)
                } else {
                    g.putString(0, 0, brackets)
                }

                val text = (item ?: "<null>").toString()
                g.putString(4, 0, text)

                val itemChecked = lb.isChecked(index) ?: false
                val marker = themeDefinition.getCharacter("MARKER", 'x')
                if (themeDefinition.getBooleanProperty("MARKER_WITH_NORMAL", false)) {
                    g.applyThemeStyle(themeDefinition.normal)
                }
                if (selected && focused && themeDefinition.getBooleanProperty("HOTSPOT_PRELIGHT", false)) {
                    g.applyThemeStyle(themeDefinition.preLight)
                }
                g.setCharacter(1, 0, if (itemChecked) marker else ' ')
            }
        }
    }
