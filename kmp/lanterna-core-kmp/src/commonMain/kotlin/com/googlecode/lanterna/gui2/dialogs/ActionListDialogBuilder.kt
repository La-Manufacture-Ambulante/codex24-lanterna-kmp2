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
import java.util.ArrayList
import java.util.Arrays

/**
 * Dialog builder for the [ActionListDialog] class, use this to create instances of that class and customize them.
 * @author Martin
 */
class ActionListDialogBuilder : AbstractDialogBuilder<ActionListDialogBuilder, ActionListDialog>("ActionListDialogBuilder") {
    private val actions: MutableList<Runnable> = ArrayList()
    private var listBoxSize: TerminalSize? = null
    private var canCancel: Boolean = true
    private var closeAutomatically: Boolean = true

    override fun self(): ActionListDialogBuilder = this

    override fun buildDialog(): ActionListDialog {
        return ActionListDialog(
            getTitle(),
            getDescription(),
            listBoxSize,
            canCancel,
            closeAutomatically,
            actions,
        )
    }

    fun setListBoxSize(listBoxSize: TerminalSize?): ActionListDialogBuilder {
        this.listBoxSize = listBoxSize
        return this
    }

    /**
     * Returns the preferred list-box size for the dialog.
     */
    fun getListBoxSize(): TerminalSize? = listBoxSize

    /**
     * Controls whether the dialog can be cancelled by the user.
     */
    fun setCanCancel(canCancel: Boolean): ActionListDialogBuilder {
        this.canCancel = canCancel
        return this
    }

    /**
     * Returns `true` if cancel controls are enabled.
     */
    fun isCanCancel(): Boolean = canCancel

    /**
     * Adds an action item with an explicit display label.
     */
    fun addAction(
        label: String?,
        action: Runnable,
    ): ActionListDialogBuilder {
        return addAction(
            object : Runnable {
                override fun toString(): String {
                    return label ?: ""
                }

                override fun run() {
                    action.run()
                }
            },
        )
    }

    /**
     * Adds an action item, using [Runnable.toString] as display text.
     */
    fun addAction(action: Runnable): ActionListDialogBuilder {
        actions.add(action)
        return this
    }

    /**
     * Adds multiple action items.
     */
    fun addActions(vararg actions: Runnable): ActionListDialogBuilder {
        this.actions.addAll(Arrays.asList(*actions))
        return this
    }

    /**
     * Returns a copy of configured actions.
     */
    fun getActions(): List<Runnable> = ArrayList(actions)

    /**
     * Controls whether selecting an action should automatically close the dialog.
     */
    fun setCloseAutomaticallyOnAction(closeAutomatically: Boolean): ActionListDialogBuilder {
        this.closeAutomatically = closeAutomatically
        return this
    }
}
