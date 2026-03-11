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
 * Dialog builder for the [FileDialog] class, use this to create instances of that class and customize them.
 * @author Martin
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

    /**
     * Returns the action button label.
     */
    fun getActionLabel(): String? = actionLabel

    /**
     * Sets suggested dialog size.
     */
    fun setSuggestedSize(suggestedSize: TerminalSize?): FileDialogBuilder {
        this.suggestedSize = suggestedSize
        return this
    }

    /**
     * Returns suggested dialog size.
     */
    fun getSuggestedSize(): TerminalSize? = suggestedSize

    /**
     * Sets initially selected file.
     */
    fun setSelectedFile(selectedFile: File?): FileDialogBuilder {
        this.selectedFile = selectedFile
        return this
    }

    /**
     * Returns initially selected file.
     */
    fun getSelectedFile(): File? = selectedFile

    /**
     * Controls visibility of hidden files/directories.
     */
    fun setShowHiddenDirectories(showHiddenDirectories: Boolean) {
        this.showHiddenDirectories = showHiddenDirectories
    }

    /**
     * Returns whether hidden files/directories are shown.
     */
    fun isShowHiddenDirectories(): Boolean = showHiddenDirectories

    override fun self(): FileDialogBuilder = this
}
