package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.LocalizedString
import com.googlecode.lanterna.internal.compat.File

/**
 * Dialog builder for the [FileDialog] class.
 */
class FileDialogBuilder : AbstractDialogBuilder<FileDialogBuilder, FileDialog>("FileDialog") {
    private var actionLabel: String? = LocalizedString.OK.toString()
    private var suggestedSize: TerminalSize? = TerminalSize(45, 10)
    private var selectedFile: File? = null
    private var showHiddenDirectories: Boolean = false

    override fun buildDialog(): FileDialog {
        return FileDialog(
            getTitle(),
            getDescription(),
            actionLabel,
            suggestedSize ?: TerminalSize(45, 10),
            showHiddenDirectories,
            selectedFile,
        )
    }

    fun setActionLabel(actionLabel: String?): FileDialogBuilder {
        this.actionLabel = actionLabel
        return this
    }

    fun getActionLabel(): String? = actionLabel

    fun setSuggestedSize(suggestedSize: TerminalSize?): FileDialogBuilder {
        this.suggestedSize = suggestedSize
        return this
    }

    fun getSuggestedSize(): TerminalSize? = suggestedSize

    fun setSelectedFile(selectedFile: File?): FileDialogBuilder {
        this.selectedFile = selectedFile
        return this
    }

    fun getSelectedFile(): File? = selectedFile

    fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    fun isShowHiddenDirectories(): Boolean = showHiddenDirectories

    override fun self(): FileDialogBuilder = this
}
