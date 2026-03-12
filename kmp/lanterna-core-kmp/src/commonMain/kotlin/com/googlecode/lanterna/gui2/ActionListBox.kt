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
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * This class is a list box implementation that displays a number of items that has actions associated with them. You
 * can activate this action by pressing the Enter or Space keys on the keyboard and the action associated with the
 * currently selected item will fire.
 * @author Martin
 */
class ActionListBox
    @JvmOverloads
    constructor(preferredSize: TerminalSize? = null) :
    AbstractListBox<Runnable, ActionListBox>(preferredSize) {
        /**
         * {@inheritDoc}
         *
         * The label of the item in the list box will be the result of calling `.toString()` on the runnable, which
         * might not be what you want to have unless you explicitly declare it. Consider using
         * `addItem(String label, Runnable action` instead, if you want to just set the label easily without having
         * to override `.toString()`.
         *
         * @param item Runnable to execute when the action was selected and fired in the list
         * @return Itself
         */
        override fun addItem(item: Runnable?): ActionListBox? {
            return super.addItem(item)
        }

        /**
         * Adds a new item to the list, which is displayed in the list using a supplied label.
         * @param label Label to use in the list for the new item
         * @param action Runnable to invoke when this action is selected and then triggered
         * @return Itself
         */
        fun addItem(
            label: String?,
            action: Runnable?,
        ): ActionListBox {
            return addItem(
                object : Runnable {
                    override fun run() {
                        action!!.run()
                    }

                    override fun toString(): String {
                        return label!!
                    }
                },
            ) ?: this
        }

        override val cursorLocation: TerminalPosition?
            get() = null

        override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
            if (isKeyboardActivationStroke(keyStroke)) {
                runSelectedItem()
                return Interactable.Result.HANDLED
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

                // includes mouse drag
                val existingIndex = getSelectedIndex()
                val newIndex = getIndexByMouseAction(mouseAction)
                if (existingIndex != newIndex || !isFocused || actionType == MouseActionType.CLICK_DOWN) {
                    // the index has changed, or the focus needs to be obtained, or the user is clicking on the current selection to perform the action again
                    val result = super.handleKeyStroke(keyStroke)
                    runSelectedItem()
                    return result
                }
                return Interactable.Result.HANDLED
            }

            return super.handleKeyStroke(keyStroke)
        }

        fun runSelectedItem() {
            val selectedItem = selectedItem
            if (selectedItem != null) {
                selectedItem.run()
            }
        }
    }
