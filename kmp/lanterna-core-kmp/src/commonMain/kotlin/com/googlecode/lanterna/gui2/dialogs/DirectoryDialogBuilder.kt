package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.filesystem.LanternaFile
import com.googlecode.lanterna.gui2.LocalizedString

/**
 * Dialog builder for the [DirectoryDialog] class.
 */
class DirectoryDialogBuilder : AbstractDialogBuilder<DirectoryDialogBuilder, DirectoryDialog>("DirectoryDialog") {
    private var actionLabel: String? = LocalizedString.OK.toString()
    private var suggestedSize: TerminalSize? = TerminalSize(45, 10)
    private var selectedDir: LanternaFile? = null
    private var showHiddenDirectories: Boolean = false

    override fun buildDialog(): DirectoryDialog {
        return DirectoryDialog(
            getTitle(),
            getDescription(),
            actionLabel,
            suggestedSize ?: TerminalSize(45, 10),
            showHiddenDirectories,
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

    fun setSelectedDirectory(selectedDir: LanternaFile?): DirectoryDialogBuilder {
        this.selectedDir = selectedDir
        return this
    }

    fun getSelectedDirectory(): LanternaFile? = selectedDir

    fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    fun isShowHiddenDirectories(): Boolean = showHiddenDirectories

    override fun self(): DirectoryDialogBuilder = this
}
