package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.LocalizedString
import java.io.File

/**
 * Dialog builder for the [DirectoryDialog] class.
 */
class DirectoryDialogBuilder : AbstractDialogBuilder<DirectoryDialogBuilder, DirectoryDialog>("DirectoryDialog") {
    private var actionLabel: String? = LocalizedString.OK.toString()
    private var suggestedSize: TerminalSize? = TerminalSize(45, 10)
    private var selectedDir: File? = null
    var isShowHiddenDirectories: Boolean = false

    override fun buildDialog(): DirectoryDialog {
        return DirectoryDialog(
            title,
            description,
            actionLabel,
            suggestedSize ?: TerminalSize(45, 10),
            isShowHiddenDirectories,
            selectedDir,
        )
    }

    fun setActionLabel(actionLabel: String?): DirectoryDialogBuilder {
        this.actionLabel = actionLabel
        return this
    }

    fun getActionLabel(): String? = actionLabel

    fun setSuggestedSize(suggestedSize: TerminalSize?): DirectoryDialogBuilder {
        this.suggestedSize = suggestedSize
        return this
    }

    fun getSuggestedSize(): TerminalSize? = suggestedSize

    fun setSelectedDirectory(selectedDir: File?): DirectoryDialogBuilder {
        this.selectedDir = selectedDir
        return this
    }

    fun getSelectedDirectory(): File? = selectedDir

    fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.isShowHiddenDirectories = showHiddenDirectories
    }

    override fun self(): DirectoryDialogBuilder = this
}
