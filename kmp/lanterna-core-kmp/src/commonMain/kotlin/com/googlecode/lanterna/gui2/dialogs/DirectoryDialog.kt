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
import com.googlecode.lanterna.gui2.BorderLayout
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import java.io.File
import java.util.Arrays
import java.util.Comparator

/**
 * Dialog that allows the user to iterate the file system and pick directory.
 *
 * @param title Title of the dialog
 * @param description Description of the dialog, is displayed at the top of the content area
 * @param actionLabel Label to use on the "confirm" button, for example "open" or "save"
 * @param dialogSize Rough estimation of how big you want the dialog to be
 * @param showHiddenDirs If `true`, hidden directories will be visible
 * @param selectedObject Initially selected directory node
 */
class DirectoryDialog(
    title: String?,
    description: String?,
    actionLabel: String?,
    dialogSize: TerminalSize,
    private val showHiddenDirs: Boolean,
    selectedObject: File?,
) : DialogWindow(title) {
    private val dirListBox: ActionListBox
    private val dirBox: TextBox
    private var directory: File? = null
    private var selectedDir: File? = null

    init {
        var resolvedSelectedObject = selectedObject
        if (resolvedSelectedObject == null || !resolvedSelectedObject.exists()) {
            resolvedSelectedObject = File("").absoluteFile
        }
        resolvedSelectedObject = resolvedSelectedObject.absoluteFile

        val contentPane = Panel()
        contentPane.setLayoutManager(BorderLayout())

        val dirsPane = Panel()
        dirsPane.setLayoutManager(BorderLayout())
        contentPane.addComponent(dirsPane, BorderLayout.Location.CENTER)

        if (description != null) {
            contentPane.addComponent(Label(description), BorderLayout.Location.TOP)
        }

        val unitHeight = dialogSize.rows
        dirListBox = ActionListBox(TerminalSize(dialogSize.columns, unitHeight))
        dirsPane.addComponent(dirListBox.withBorder(Borders.singleLine()), BorderLayout.Location.CENTER)

        dirBox = TextBox(TerminalSize(dialogSize.columns, 1))
        dirsPane.addComponent(dirBox.withBorder(Borders.singleLine()), BorderLayout.Location.BOTTOM)

        val panelButtons = Panel(GridLayout(2))
        panelButtons.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.END,
                GridLayout.Alignment.CENTER,
                false,
                false,
                2,
                1,
            ),
        )
        panelButtons.addComponent(
            Button(requireNotNull(actionLabel), OkHandler()),
        )
        panelButtons.addComponent(
            Button(LocalizedString.Cancel.toString(), CancelHandler()),
        )
        contentPane.addComponent(panelButtons, BorderLayout.Location.BOTTOM)

        when {
            resolvedSelectedObject.isFile -> directory = resolvedSelectedObject.parentFile
            resolvedSelectedObject.isDirectory -> directory = resolvedSelectedObject
        }

        reloadViews(directory ?: File("").absoluteFile)
        component = contentPane
    }

    /**
     * The directory which was selected in the dialog or `null` if the dialog was cancelled.
     */
    override fun showDialog(textGUI: WindowBasedTextGUI): File? {
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
                val activeTextGUI = textGUI ?: return
                MessageDialog.showMessageDialog(
                    activeTextGUI,
                    "Error",
                    "Please select a valid directory name",
                    MessageDialogButton.OK,
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
        override fun run() {}
    }

    private fun reloadViews(directory: File) {
        dirBox.setText(directory.absolutePath)
        dirListBox.clearItems()
        val entries = directory.listFiles() ?: return
        Arrays.sort(entries, Comparator.comparing { file -> file.name.lowercase() })
        val parent = directory.absoluteFile.parentFile
        if (parent != null) {
            dirListBox.addItem(
                "..",
                Runnable {
                    this.directory = parent
                    reloadViews(parent)
                },
            )
        } else {
            val roots = File.listRoots()
            for (entry in roots) {
                if (entry.canRead()) {
                    dirListBox.addItem(
                        "[${entry.path}]",
                        Runnable {
                            this.directory = entry
                            reloadViews(entry)
                        },
                    )
                }
            }
        }
        for (entry in entries) {
            if (entry.isHidden && !showHiddenDirs) {
                continue
            }
            if (entry.isDirectory) {
                dirListBox.addItem(
                    entry.name,
                    Runnable {
                        this.directory = entry
                        reloadViews(entry)
                    },
                )
            }
        }
        if (dirListBox.isEmpty) {
            dirListBox.addItem("<empty>", DoNothing())
        }
    }
}
