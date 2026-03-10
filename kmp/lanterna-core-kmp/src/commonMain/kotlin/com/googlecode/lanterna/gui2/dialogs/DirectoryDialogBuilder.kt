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
import com.googlecode.lanterna.gui2.LocalizedString
import java.io.File

/**
 * Dialog builder for the [DirectoryDialog] class.
 */
class DirectoryDialogBuilder : AbstractDialogBuilder<DirectoryDialogBuilder, DirectoryDialog>("DirectoryDialog") {
    private var actionLabel: String? = LocalizedString.OK.toString()
    private var suggestedSize: TerminalSize? = TerminalSize(45, 10)
    private var selectedDir: File? = null
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

    fun setSelectedDirectory(selectedDir: File?): DirectoryDialogBuilder {
        this.selectedDir = selectedDir
        return this
    }

    fun getSelectedDirectory(): File? = selectedDir

    fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    fun isShowHiddenDirectories(): Boolean = showHiddenDirectories

    override fun self(): DirectoryDialogBuilder = this
}
