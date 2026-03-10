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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.WindowBasedTextGUI

/**
 * Dialog that allows the user to select an item from a list.
 */
class ListSelectDialog<T> internal constructor(
    title: String?,
    description: String?,
    listBoxPreferredSize: TerminalSize?,
    canCancel: Boolean,
    content: List<T>,
) : DialogWindow(title) {
    private var result: T? = null

    init {
        require(content.isNotEmpty()) { "ListSelectDialog needs at least one item" }

        val listBox = ActionListBox(listBoxPreferredSize)
        for (item in content) {
            listBox.addItem(item.toString(), Runnable { onSelect(item) })
        }

        val mainPanel = Panel()
        mainPanel.setLayoutManager(
            GridLayout(1)
                .setLeftMarginSize(1)
                .setRightMarginSize(1),
        )
        if (description != null) {
            mainPanel.addComponent(Label(description))
            mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        }
        listBox.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.CENTER,
                true,
                false,
            ),
        )
        listBox.addTo(mainPanel)
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))

        if (canCancel) {
            val buttonPanel = Panel()
            buttonPanel.setLayoutManager(GridLayout(2).setHorizontalSpacing(1))
            buttonPanel.addComponent(
                Button(LocalizedString.Cancel.toString(), Runnable { onCancel() }).setLayoutData(
                    GridLayout.createLayoutData(
                        GridLayout.Alignment.CENTER,
                        GridLayout.Alignment.CENTER,
                        true,
                        false,
                    ),
                ),
            )
            buttonPanel.setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.END,
                    GridLayout.Alignment.CENTER,
                    false,
                    false,
                ),
            )
            buttonPanel.addTo(mainPanel)
        }
        component = mainPanel
    }

    private fun onSelect(item: T) {
        result = item
        close()
    }

    private fun onCancel() {
        close()
    }

    /**
     * Opens the dialog and returns the selected item, or `null` if cancelled.
     */
    override fun showDialog(textGUI: WindowBasedTextGUI): T? {
        result = null
        super.showDialog(textGUI)
        return result
    }

    companion object {
        /**
         * Convenience helper creating and showing a [ListSelectDialog] with automatic list-box sizing.
         */
        fun <T> showDialog(textGUI: WindowBasedTextGUI, title: String?, description: String?, vararg items: T): T? {
            return showDialog(textGUI, title, description, null, *items)
        }

        /**
         * Convenience helper creating and showing a [ListSelectDialog] with explicit list-box height.
         */
        fun <T> showDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            description: String?,
            listBoxHeight: Int,
            vararg items: T,
        ): T? {
            var width = 0
            for (item in items) {
                width = maxOf(width, TerminalTextUtils.getColumnWidth(item.toString()))
            }
            width += 2
            return showDialog(textGUI, title, description, TerminalSize(width, listBoxHeight), *items)
        }

        /**
         * Convenience helper creating and showing a [ListSelectDialog] with explicit list-box size.
         */
        fun <T> showDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            description: String?,
            listBoxSize: TerminalSize?,
            vararg items: T,
        ): T? {
            val listSelectDialog = ListSelectDialogBuilder<T>()
                .setTitle(title)
                .setDescription(description)
                .setListBoxSize(listBoxSize)
                .addListItems(*items)
                .build()
            return listSelectDialog.showDialog(textGUI)
        }
    }
}
