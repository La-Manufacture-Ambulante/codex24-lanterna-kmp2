package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.LocalizedString
import java.io.File

open class DirectoryDialogBuilder : AbstractDialogBuilder<DirectoryDialogBuilder, DirectoryDialog>("DirectoryDialog") {

    private var actionLabel: String?
    private var suggestedSize: TerminalSize?
    private var selectedDir: File?
    private var showHiddenDirectories: Boolean

    init {
        actionLabel = LocalizedString.OK.toString()
        suggestedSize = TerminalSize(45, 10)
        showHiddenDirectories = false
        selectedDir = null
    }

    protected override fun buildDialog(): DirectoryDialog {
        return DirectoryDialog(title, description, actionLabel, suggestedSize, showHiddenDirectories, selectedDir)
    }

    open fun setActionLabel(actionLabel: String?): DirectoryDialogBuilder {
        this.actionLabel = actionLabel
        return this
    }

    open fun getActionLabel(): String? {
        return actionLabel
    }

    open fun setSuggestedSize(suggestedSize: TerminalSize?): DirectoryDialogBuilder {
        this.suggestedSize = suggestedSize
        return this
    }

    open fun getSuggestedSize(): TerminalSize? {
        return suggestedSize
    }

    open fun setSelectedDirectory(selectedDir: File?): DirectoryDialogBuilder {
        this.selectedDir = selectedDir
        return this
    }

    open fun getSelectedDirectory(): File? {
        return selectedDir
    }

    open fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    open fun isShowHiddenDirectories(): Boolean {
        return showHiddenDirectories
    }

    protected override fun self(): DirectoryDialogBuilder {
        return this
    }
}
