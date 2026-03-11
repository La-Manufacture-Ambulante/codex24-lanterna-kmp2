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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.terminal.swing.ScrollingSwingTerminal
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorColorConfiguration
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorDeviceConfiguration
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Random
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

/**
 * Interactive manual test for [ScrollingSwingTerminal].
 */
@Suppress("FieldCanBeLocal")
class ScrollingSwingTerminalTest : JFrame() {
    private val scrollingSwingTerminal: ScrollingSwingTerminal

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Scrolling Swing Terminal Test"

        val deviceConfiguration =
            TerminalEmulatorDeviceConfiguration.default.withLineBufferScrollbackSize(150)
        scrollingSwingTerminal =
            ScrollingSwingTerminal(
                deviceConfiguration,
                SwingTerminalFontConfiguration.default,
                TerminalEmulatorColorConfiguration.default,
            )

        val terminalContainer = JPanel(BorderLayout())
        terminalContainer.border = BorderFactory.createTitledBorder("Terminal")
        terminalContainer.add(scrollingSwingTerminal, BorderLayout.CENTER)

        val controls = JPanel(FlowLayout(FlowLayout.RIGHT))
        controls.add(createButton("Move cursor") { buttonMoveCursorActionPerformed(it) })
        controls.add(createButton("Clear") { clearActionPerformed(it) })
        controls.add(createButton("Print 1 line") { buttonPrint1LineActionPerformed(it) })
        controls.add(createButton("Print 10 lines") { buttonPrint10LinesActionPerformed(it) })
        controls.add(createButton("Print 100 lines") { buttonPrint100LinesActionPerformed(it) })

        contentPane.layout = BorderLayout()
        contentPane.add(terminalContainer, BorderLayout.CENTER)
        contentPane.add(controls, BorderLayout.SOUTH)
        pack()
    }

    private fun createButton(
        text: String,
        action: (ActionEvent) -> Unit,
    ): JButton {
        val button = JButton(text)
        button.addActionListener(ActionListener { event -> action(event) })
        return button
    }

    private fun buttonPrint100LinesActionPerformed(evt: ActionEvent) {
        printLines(100)
    }

    private fun buttonPrint10LinesActionPerformed(evt: ActionEvent) {
        printLines(10)
    }

    private fun buttonPrint1LineActionPerformed(evt: ActionEvent) {
        printLines(1)
    }

    private fun buttonMoveCursorActionPerformed(evt: ActionEvent) {
        val terminalSize = scrollingSwingTerminal.terminalSize ?: return
        val random = Random()
        scrollingSwingTerminal.setCursorPosition(
            random.nextInt(terminalSize.columns),
            random.nextInt(terminalSize.rows),
        )
        scrollingSwingTerminal.flush()
    }

    private fun clearActionPerformed(evt: ActionEvent) {
        scrollingSwingTerminal.clearScreen()
    }

    private fun printLines(howMany: Int) {
        val random = Random()
        val selection = "abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ"
        repeat(howMany) {
            val words = random.nextInt(10) + 1
            repeat(words) {
                val length = random.nextInt(10) + 2
                repeat(length) {
                    scrollingSwingTerminal.putCharacter(selection[random.nextInt(selection.length)])
                }
                scrollingSwingTerminal.putCharacter(' ')
            }
            scrollingSwingTerminal.putCharacter('\n')
        }
        scrollingSwingTerminal.flush()
    }

    companion object {
        private const val serialVersionUID = 1L

        fun main(args: Array<String?>?) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (ex: ClassNotFoundException) {
                java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest::class.java.name)
                    .log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest::class.java.name)
                    .log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest::class.java.name)
                    .log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest::class.java.name)
                    .log(java.util.logging.Level.SEVERE, null, ex)
            }

            java.awt.EventQueue.invokeLater { ScrollingSwingTerminalTest().isVisible = true }
        }
    }
}
