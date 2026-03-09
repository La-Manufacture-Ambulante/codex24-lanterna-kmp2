package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Panels
import com.googlecode.lanterna.gui2.Separator
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import java.io.File
import java.util.Arrays
import java.util.Comparator

/**
 * Dialog that allows the user to iterate the file system and pick file to open/save.
 */
class FileDialog(
    title: String?,
    description: String?,
    actionLabel: String?,
    dialogSize: TerminalSize,
    private val showHiddenFilesAndDirs: Boolean,
    selectedObject: File?,
) : DialogWindow(title) {
    private val fileListBox: ActionListBox
    private val directoryListBox: ActionListBox
    private val fileBox: TextBox
    private val okButton: Button

    private var directory: File? = null
    private var selectedFile: File? = null

    init {
        var resolvedSelectedObject = selectedObject
        if (resolvedSelectedObject == null || !resolvedSelectedObject.exists()) {
            resolvedSelectedObject = File("").absoluteFile
        }
        resolvedSelectedObject = resolvedSelectedObject.absoluteFile

        val contentPane = Panel()
        contentPane.setLayoutManager(GridLayout(2))

        if (description != null) {
            Label(description)
                .setLayoutData(
                    GridLayout.createLayoutData(
                        GridLayout.Alignment.BEGINNING,
                        GridLayout.Alignment.CENTER,
                        false,
                        false,
                        2,
                        1,
                    ),
                )
                .addTo(contentPane)
        }

        val unitWidth = dialogSize.columns / 3
        val unitHeight = dialogSize.rows

        FileSystemLocationLabel()
            .setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.FILL,
                    GridLayout.Alignment.CENTER,
                    true,
                    false,
                    2,
                    1,
                ),
            )
            .addTo(contentPane)

        fileListBox = ActionListBox(TerminalSize(unitWidth * 2, unitHeight))
        fileListBox.withBorder(Borders.singleLine())
            .setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.BEGINNING,
                    GridLayout.Alignment.CENTER,
                    false,
                    false,
                ),
            )
            .addTo(contentPane)

        directoryListBox = ActionListBox(TerminalSize(unitWidth, unitHeight))
        directoryListBox.withBorder(Borders.singleLine()).addTo(contentPane)

        fileBox = TextBox()
            .setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.FILL,
                    GridLayout.Alignment.CENTER,
                    true,
                    false,
                    2,
                    1,
                ),
            )
            .addTo(contentPane) ?: TextBox()

        Separator(Direction.HORIZONTAL)
            .setLayoutData(
                GridLayout.createLayoutData(
                    GridLayout.Alignment.FILL,
                    GridLayout.Alignment.CENTER,
                    true,
                    false,
                    2,
                    1,
                ),
            )
            .addTo(contentPane)

        okButton = Button(actionLabel, OkHandler())
        Panels.grid(
            2,
            okButton,
            Button(LocalizedString.Cancel.toString(), CancelHandler()),
        ).setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.END,
                GridLayout.Alignment.CENTER,
                false,
                false,
                2,
                1,
            ),
        ).addTo(contentPane)

        when {
            resolvedSelectedObject.isFile -> {
                directory = resolvedSelectedObject.parentFile
                fileBox.setText(resolvedSelectedObject.name)
            }
            resolvedSelectedObject.isDirectory -> directory = resolvedSelectedObject
        }

        reloadViews(directory ?: File("").absoluteFile)
        component = contentPane
    }

    override fun showDialog(textGUI: WindowBasedTextGUI): File? {
        selectedFile = null
        super.showDialog(textGUI)
        return selectedFile
    }

    private inner class OkHandler : Runnable {
        override fun run() {
            if (fileBox.text.isNotEmpty()) {
                val file = File(fileBox.text)
                selectedFile = if (file.isAbsolute) file else File(directory, fileBox.text)
                close()
            } else {
                MessageDialog.showMessageDialog(
                    textGUI,
                    "Error",
                    "Please select a valid file name",
                    MessageDialogButton.OK,
                )
            }
        }
    }

    private inner class CancelHandler : Runnable {
        override fun run() {
            selectedFile = null
            close()
        }
    }

    private class DoNothing : Runnable {
        override fun run() {}
    }

    private fun reloadViews(directory: File) {
        directoryListBox.clearItems()
        fileListBox.clearItems()
        val entries = directory.listFiles() ?: return
        Arrays.sort(entries, Comparator.comparing { file -> file.name.lowercase() })
        val parent = directory.absoluteFile.parentFile
        if (parent != null) {
            directoryListBox.addItem(
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
                    directoryListBox.addItem(
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
            if (entry.isHidden && !showHiddenFilesAndDirs) {
                continue
            }
            if (entry.isDirectory) {
                directoryListBox.addItem(
                    entry.name,
                    Runnable {
                        this.directory = entry
                        reloadViews(entry)
                    },
                )
            } else {
                fileListBox.addItem(
                    entry.name,
                    Runnable {
                        fileBox.setText(entry.name)
                        focusedInteractable = okButton
                    },
                )
            }
        }

        if (fileListBox.isEmpty) {
            fileListBox.addItem("<empty>", DoNothing())
        }
    }

    private inner class FileSystemLocationLabel : Label("") {
        init {
            setPreferredSize(TerminalSize.ONE)
        }

        override fun onBeforeDrawing() {
            val area = size ?: return
            var absolutePath = directory?.absolutePath ?: ""
            val absolutePathLengthInColumns = TerminalTextUtils.getColumnWidth(absolutePath)
            if (area.columns < absolutePathLengthInColumns) {
                absolutePath = absolutePath.substring(absolutePathLengthInColumns - area.columns)
                absolutePath = "..." + absolutePath.substring(kotlin.math.min(absolutePathLengthInColumns, 3))
            }
            setText(absolutePath)
        }
    }
}
