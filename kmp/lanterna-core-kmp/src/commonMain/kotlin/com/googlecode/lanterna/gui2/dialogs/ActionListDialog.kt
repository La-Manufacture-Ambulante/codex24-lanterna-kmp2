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
        fun showDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            description: String?,
            vararg items: Runnable,
        ) {
            val actionListDialog =
                ActionListDialogBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .addActions(*items)
                    .build()
            actionListDialog.showDialog(textGUI)
        }
    }
}
