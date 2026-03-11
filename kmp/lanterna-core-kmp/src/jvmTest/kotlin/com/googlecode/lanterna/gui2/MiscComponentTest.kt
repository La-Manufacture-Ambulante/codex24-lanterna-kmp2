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
import com.googlecode.lanterna.TestUtils
import java.io.*
import java.util.Timer
import java.util.TimerTask

class MiscComponentTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Grid layout test")
        val leftPanel = Panel()
        val checkBoxPanel = Panel()
        for (i in 0..3) {
            val checkBox = CheckBox("Checkbox #" + (i + 1))
            checkBoxPanel.addComponent(checkBox)
        }

        var textBoxPanel: Panel? = Panel()
        textBoxPanel!!.addComponent(Panels.horizontal(Label("Normal:   "), TextBox(TerminalSize(12, 1), "Text")))
        textBoxPanel!!.addComponent(Panels.horizontal(Label("Password: "), TextBox(TerminalSize(12, 1), "Text").setMask('*')))

        val buttonPanel = Panel()
        buttonPanel.addComponent(
            Button("Enable spacing", {
                val layoutManager = leftPanel.getLayoutManager() as LinearLayout?
                layoutManager!!.setSpacing(if (layoutManager!!.getSpacing() == 0) 1 else 0)
            }),
        )

        leftPanel.addComponent(checkBoxPanel.withBorder(Borders.singleLine("CheckBoxes")))
        leftPanel.addComponent(textBoxPanel!!.withBorder(Borders.singleLine("TextBoxes")))
        leftPanel.addComponent(buttonPanel.withBorder(Borders.singleLine("Buttons")))

        val rightPanel = Panel()
        textBoxPanel = Panel()
        val readOnlyTextArea = TextBox(TerminalSize(16, 8))
        readOnlyTextArea.setReadOnly(true)
        readOnlyTextArea.setText(TestUtils.downloadGPL()!!)
        textBoxPanel!!.addComponent(readOnlyTextArea)
        rightPanel.addComponent(textBoxPanel!!.withBorder(Borders.singleLine("Read-only")))
        val progressBar = ProgressBar(0, 100, 16)
        progressBar.setRenderer(ProgressBar.LargeProgressBarRenderer())
        progressBar.setLabelFormat("%2.0f%%")
        rightPanel.addComponent(progressBar.withBorder(Borders.singleLine("ProgressBar")))
        rightPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL))

        val timer = Timer("ProgressBar-timer", true)
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    if (progressBar.getValue() == progressBar.getMax()) {
                        progressBar.setValue(0)
                    } else {
                        progressBar.setValue(progressBar.getValue() + 1)
                    }
                }
            },
            250,
            250,
        )

        val contentArea = Panel()
        contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
        contentArea.addComponent(Panels.horizontal(leftPanel, rightPanel))
        contentArea.addComponent(
            Separator(Direction.HORIZONTAL).setLayoutData(
                LinearLayout.createLayoutData(LinearLayout.Alignment.FILL),
            ),
        )
        val okButton =
            Button("OK", {
                window.close()
                timer.cancel()
            }).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.CENTER))
        contentArea.addComponent(okButton)
        window.component = contentArea
        textGUI.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            MiscComponentTest().run(args)
        }
    }
}
