package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.WindowBasedTextGUI

/**
 * Simple message dialog that displays a message and has optional selection/confirmation buttons.
 */
class MessageDialog internal constructor(
    title: String?,
    text: String?,
    vararg buttons: MessageDialogButton,
) : DialogWindow(title) {
    private var result: MessageDialogButton? = null

    init {
        val resolvedButtons = if (buttons.isEmpty()) {
            arrayOf(MessageDialogButton.OK)
        } else {
            buttons
        }

        val buttonPanel = Panel()
        buttonPanel.setLayoutManager(GridLayout(resolvedButtons.size).setHorizontalSpacing(1))
        for (button in resolvedButtons) {
            buttonPanel.addComponent(
                Button(
                    button.toString(),
                    Runnable {
                        result = button
                        close()
                    },
                ),
            )
        }

        val mainPanel = Panel()
        mainPanel.setLayoutManager(
            GridLayout(1)
                .setLeftMarginSize(1)
                .setRightMarginSize(1),
        )
        mainPanel.addComponent(Label(text))
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        buttonPanel.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.END,
                GridLayout.Alignment.CENTER,
                false,
                false,
            ),
        )
        buttonPanel.addTo(mainPanel)
        component = mainPanel
    }

    override fun showDialog(textGUI: WindowBasedTextGUI): MessageDialogButton? {
        result = null
        super.showDialog(textGUI)
        return result
    }

    companion object {
        fun showMessageDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            text: String?,
            vararg buttons: MessageDialogButton,
        ): MessageDialogButton? {
            val builder = MessageDialogBuilder()
                .setTitle(title)
                .setText(text)
            if (buttons.isEmpty()) {
                builder.addButton(MessageDialogButton.OK)
            }
            for (button in buttons) {
                builder.addButton(button)
            }
            return builder.build().showDialog(textGUI)
        }
    }
}
