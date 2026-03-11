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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton

import java.io.IOException
import java.util.TimeZone
import java.util.regex.Pattern

class ComboBoxTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI?) {
        val window = BasicWindow("ComboBoxTest")
        val mainPanel = Panel()

        val comboBoxReadOnly = ComboBox<String>()
        val comboBoxEditable = ComboBox<String>().setReadOnly(false)
        val comboBoxCJK = ComboBox<String>().setReadOnly(false)
        val comboBoxTimeZones = ComboBox<String>().setReadOnly(true)

        for (item in listOf("Berlin", "London", "Paris", "Stockholm", "Tokyo")) {
            comboBoxEditable.addItem(item)
            comboBoxReadOnly.addItem(item)
        }
        for (id in TimeZone.getAvailableIDs()) {
            comboBoxTimeZones.addItem(id)
        }
        comboBoxCJK.addItem("维基百科人人可編輯的自由百科全書")
        comboBoxCJK.addItem("ウィキペディアは誰でも編集できるフリー百科事典です")
        comboBoxCJK.addItem("위키백과는 전 세계 여러 언어로 만들어 나가는 자유 백과사전으로, 누구나 참여하실 수 있습니다.")
        comboBoxCJK.addItem("This is a string without double-width characters")
        comboBoxCJK.setPreferredSize(TerminalSize(13, 1))

        mainPanel.addComponent(
            Panels.horizontal(
                comboBoxReadOnly.withBorder(Borders.singleLine("Read-only")),
                comboBoxEditable.withBorder(Borders.singleLine("Editable")),
                comboBoxCJK.withBorder(Borders.singleLine("CJK")),
            ),
        )
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))

        val textBoxNewItem = TextBox(TerminalSize(20, 1))
        val buttonAddItem = Button("Add") {
            comboBoxEditable.addItem(textBoxNewItem.text)
            comboBoxReadOnly.addItem(textBoxNewItem.text)
            textBoxNewItem.setText("")
            window.focusedInteractable = textBoxNewItem
        }
        val textBoxSetSelectedIndex = TextBox(TerminalSize(20, 1), "0")
        textBoxSetSelectedIndex.setValidationPattern(Pattern.compile("-?[0-9]+"))
        val buttonSetSelectedIndex = Button("Set Selected Index") {
            try {
                val selectedIndex = textBoxSetSelectedIndex.text.toInt()
                comboBoxEditable.setSelectedIndex(selectedIndex)
                comboBoxReadOnly.setSelectedIndex(selectedIndex)
            } catch (e: Exception) {
                MessageDialog.showMessageDialog(requireNotNull(textGUI), e::class.java.name, e.message, MessageDialogButton.OK)
            }
        }
        val textBoxSetSelectedItem = TextBox(TerminalSize(20, 1))
        val buttonSetSelectedItem = Button("Set Selected Item") {
            try {
                comboBoxEditable.setSelectedItem(textBoxSetSelectedItem.text)
                comboBoxReadOnly.setSelectedItem(textBoxSetSelectedItem.text)
            } catch (e: Exception) {
                MessageDialog.showMessageDialog(requireNotNull(textGUI), e::class.java.name, e.message, MessageDialogButton.OK)
            }
        }
        mainPanel.addComponent(
            Panels.vertical(
                Panels.horizontal(textBoxNewItem, buttonAddItem),
                Panels.horizontal(textBoxSetSelectedIndex, buttonSetSelectedIndex),
                Panels.horizontal(textBoxSetSelectedItem, buttonSetSelectedItem),
            ).withBorder(Borders.singleLineBevel("Modify Content")),
        )

        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        mainPanel.addComponent(comboBoxTimeZones.withBorder(Borders.singleLine("Large ComboBox")))
        mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
        mainPanel.addComponent(
            Separator(Direction.HORIZONTAL).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL)),
        )
        mainPanel.addComponent(Button("OK", Runnable { window.close() }))
        window.component = mainPanel
        requireNotNull(textGUI).addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            ComboBoxTest().run(args)
        }
    }
}
