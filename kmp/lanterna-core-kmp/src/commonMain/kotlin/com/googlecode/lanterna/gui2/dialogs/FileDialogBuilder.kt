package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.LocalizedString
import java.io.File

open class FileDialogBuilder : AbstractDialogBuilder<FileDialogBuilder, FileDialog>("FileDialog") {
    private var actionLabel: String?
    private var suggestedSize: TerminalSize?
    private var selectedFile: File?
    private var showHiddenDirectories: Boolean

    init {
        actionLabel = LocalizedString.OK.toString()
        suggestedSize = TerminalSize(45, 10)
        showHiddenDirectories = false
        selectedFile = null
    }

    protected open override fun buildDialog(): FileDialog {
        return FileDialog(title, description, actionLabel, suggestedSize, showHiddenDirectories, selectedFile)
    }

    open fun setActionLabel(actionLabel: String?): FileDialogBuilder {
        this.actionLabel = actionLabel
        return this
    }

    open fun getActionLabel(): String? {
        return actionLabel
    }

    open fun setSuggestedSize(suggestedSize: TerminalSize?): FileDialogBuilder {
        this.suggestedSize = suggestedSize
        return this
    }

    open fun getSuggestedSize(): TerminalSize? {
        return suggestedSize
    }

    open fun setSelectedFile(selectedFile: File?): FileDialogBuilder {
        this.selectedFile = selectedFile
        return this
    }

    open fun getSelectedFile(): File? {
        return selectedFile
    }

    open fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    open fun isShowHiddenDirectories(): Boolean {
        return showHiddenDirectories
    }

    protected open override fun self(): FileDialogBuilder {
        return this
    }
}
