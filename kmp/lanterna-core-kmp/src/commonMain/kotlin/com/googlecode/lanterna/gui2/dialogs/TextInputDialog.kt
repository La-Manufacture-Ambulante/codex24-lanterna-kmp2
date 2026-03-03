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
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.DialogWindow
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import java.math.BigInteger
import java.util.regex.Pattern

open class TextInputDialog internal constructor(
    title: String?,
    description: String?,
    textBoxPreferredSize: TerminalSize?,
    initialContent: String?,
    private val validator: TextInputDialogResultValidator?,
    password: Boolean
) : DialogWindow(title) {

    private val textBox: TextBox = TextBox(textBoxPreferredSize, initialContent)
    private var result: String? = null

    init {
        if (password) {
            textBox.setMask('*')
        }

        val buttonPanel = Panel()
        buttonPanel.setLayoutManager(GridLayout(2).setHorizontalSpacing(1))
        buttonPanel.addComponent(
            Button(LocalizedString.OK.toString(), this::onOK).setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.CENTER,
                    GridLayout.Alignment.CENTER,
                    true,
                    false
                )
            )
        )
        buttonPanel.addComponent(Button(LocalizedString.Cancel.toString(), this::onCancel))

        val mainPanel = Panel()
        mainPanel.setLayoutManager(
            GridLayout(1)
                .setLeftMarginSize(1)
                .setRightMarginSize(1)
        )
        if (description != null) {
            mainPanel.addComponent(Label(description))
        }
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        textBox.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.FILL,
                GridLayout.Alignment.CENTER,
                true,
                false
            )
        ).addTo(mainPanel)
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        buttonPanel.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.END,
                GridLayout.Alignment.CENTER,
                false,
                false
            )
        ).addTo(mainPanel)
        setComponent(mainPanel)
    }

    private fun onOK() {
        val text = textBox.getText()
        if (validator != null) {
            val errorMessage = validator.validate(text)
            if (errorMessage != null) {
                MessageDialog.showMessageDialog(getTextGUI(), getTitle(), errorMessage, MessageDialogButton.OK)
                return
            }
        }
        result = text
        close()
    }

    private fun onCancel() {
        close()
    }

    public override fun showDialog(textGUI: WindowBasedTextGUI?): String? {
        result = null
        super.showDialog(textGUI)
        return result
    }

    companion object {
        @JvmStatic
        fun showDialog(
            textGUI: WindowBasedTextGUI?,
            title: String?,
            description: String?,
            initialContent: String?
        ): String? {
            val textInputDialog = TextInputDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .setInitialContent(initialContent)
                .build()
            return textInputDialog.showDialog(textGUI)
        }

        @JvmStatic
        fun showNumberDialog(
            textGUI: WindowBasedTextGUI?,
            title: String?,
            description: String?,
            initialContent: String?
        ): BigInteger? {
            val textInputDialog = TextInputDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .setInitialContent(initialContent)
                .setValidationPattern(Pattern.compile("[0-9]+"), "Not a number")
                .build()
            val numberString = textInputDialog.showDialog(textGUI)
            return if (numberString != null) BigInteger(numberString) else null
        }

        @JvmStatic
        fun showPasswordDialog(
            textGUI: WindowBasedTextGUI?,
            title: String?,
            description: String?,
            initialContent: String?
        ): String? {
            val textInputDialog = TextInputDialogBuilder()
                .setTitle(title)
                .setDescription(description)
                .setInitialContent(initialContent)
                .setPasswordInput(true)
                .build()
            return textInputDialog.showDialog(textGUI)
        }
    }
}
