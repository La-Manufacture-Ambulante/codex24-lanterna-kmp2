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
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.WindowBasedTextGUI

/**
 * Dialog containing a multiple item action list box.
 */
class ActionListDialog internal constructor(
    title: String?,
    description: String?,
    actionListPreferredSize: TerminalSize?,
    canCancel: Boolean,
    private val closeAutomatically: Boolean,
    actions: List<Runnable>,
) : DialogWindow(title) {
    init {
        val listBox = ActionListBox(actionListPreferredSize)
        for (action in actions) {
            listBox.addItem(
                action.toString(),
                Runnable {
                    action.run()
                    if (closeAutomatically) {
                        close()
                    }
                },
            )
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

    private fun onCancel() {
        close()
    }

    companion object {
        /**
         * Helper method for immediately displaying an [ActionListDialog]. The method returns when the dialog closes.
         */
        fun showDialog(textGUI: WindowBasedTextGUI, title: String?, description: String?, vararg items: Runnable) {
            val actionListDialog = ActionListDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .addActions(*items)
                .build()
            actionListDialog.showDialog(textGUI)
        }
    }
}
