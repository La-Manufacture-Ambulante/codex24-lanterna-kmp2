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
 * Copyright (C) 2010-2016 Martin
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BorderLayout
import com.googlecode.lanterna.gui2.BorderLayout.Location
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.input.KeyStroke
import java.io.File
import java.util.Arrays
import java.util.Comparator

/**
 * Dialog that allows the user to iterate the file system and pick directory.
 *
 * @author Martin
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
open class DirectoryDialog(
    title: String?,
    description: String?,
    actionLabel: String?,
    dialogSize: TerminalSize?,
    private val showHiddenDirs: Boolean,
    selectedObject: File?
) : DialogWindow(title) {

    private val dirListBox: ActionListBox
    private val dirBox: TextBox
    private var directory: File? = null
    private var selectedDir: File? = null

    init {
        var selectedObjectVar = selectedObject

        if (selectedObjectVar == null || !selectedObjectVar.exists()) {
            selectedObjectVar = File("").absoluteFile
        }
        selectedObjectVar = selectedObjectVar.absoluteFile

        val contentPane = Panel()
        contentPane.setLayoutManager(BorderLayout())

        val dirsPane = Panel()
        dirsPane.setLayoutManager(BorderLayout())
        contentPane.addComponent(dirsPane, Location.CENTER)

        if (description != null) {
            contentPane.addComponent(Label(description), Location.TOP)
        }

        val dialogSizeNonNull = dialogSize ?: throw NullPointerException()
        val unitHeight = dialogSizeNonNull.rows

        dirListBox = ActionListBox(TerminalSize(dialogSizeNonNull.columns, unitHeight))
        dirsPane.addComponent(dirListBox.withBorder(Borders.singleLine()), Location.CENTER)

        dirBox = TextBox(TerminalSize(dialogSizeNonNull.columns, 1))
        dirsPane.addComponent(dirBox.withBorder(Borders.singleLine()), Location.BOTTOM)

        val panelButtons = Panel(GridLayout(2))
        panelButtons.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.END,
                GridLayout.Alignment.CENTER,
                false,
                false,
                2,
                1
            )
        )
        panelButtons.addComponent(
            Button(actionLabel, OkHandler()).setAccelerator(KeyStroke('s', false, true))
        )
        panelButtons.addComponent(
            Button(LocalizedString.Cancel.toString(), CancelHandler()).setAccelerator(KeyStroke('c', false, true))
        )
        contentPane.addComponent(panelButtons, Location.BOTTOM)

        if (selectedObjectVar.isFile) {
            directory = selectedObjectVar.parentFile
        } else if (selectedObjectVar.isDirectory) {
            directory = selectedObjectVar
        }

        reloadViews(directory)
        setComponent(contentPane)
    }

    /**
     * {@inheritDoc}
     *
     * @param textGUI Text GUI to add the dialog to
     * @return The directory which was selected in the dialog or {@code null} if the dialog was cancelled
     */
    public override fun showDialog(textGUI: WindowBasedTextGUI?): File? {
        selectedDir = null
        super.showDialog(textGUI)
        return selectedDir
    }

    private inner class OkHandler : Runnable {
        override fun run() {
            val dir = File(dirBox.text)
            if (dir.exists() && dir.isDirectory) {
                selectedDir = dir
                close()
            } else {
                MessageDialog.showMessageDialog(
                    textGUI,
                    "Error",
                    "Please select a valid directory name",
                    MessageDialogButton.OK
                )
            }
        }
    }

    private inner class CancelHandler : Runnable {
        override fun run() {
            selectedDir = null
            close()
        }
    }

    private class DoNothing : Runnable {
        override fun run() {
        }
    }

    private fun reloadViews(directory: File?) {
        val directoryNonNull = directory ?: throw NullPointerException()
        dirBox.setText(directoryNonNull.absolutePath)
        dirListBox.clearItems()
        val entries = directoryNonNull.listFiles()
        if (entries == null) {
            return
        }
        Arrays.sort(entries, Comparator.comparing<File, String> { o -> o.name.toLowerCase() })
        if (directoryNonNull.absoluteFile.parentFile != null) {
            dirListBox.addItem("..") {
                this@DirectoryDialog.directory = directoryNonNull.absoluteFile.parentFile
                reloadViews(directoryNonNull.absoluteFile.parentFile)
            }
        } else {
            val roots = File.listRoots()
            for (entry in roots) {
                if (entry.canRead()) {
                    dirListBox.addItem("[" + entry.path + "]") {
                        this@DirectoryDialog.directory = entry
                        reloadViews(entry)
                    }
                }
            }
        }
        for (entry in entries) {
            if (entry.isHidden && !showHiddenDirs) {
                continue
            }
            if (entry.isDirectory) {
                dirListBox.addItem(entry.name) {
                    this@DirectoryDialog.directory = entry
                    reloadViews(entry)
                }
            }
        }
        if (dirListBox.isEmpty()) {
            dirListBox.addItem("<empty>", DoNothing())
        }
    }
}
