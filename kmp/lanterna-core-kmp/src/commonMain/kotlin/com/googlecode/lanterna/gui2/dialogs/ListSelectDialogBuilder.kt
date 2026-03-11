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
import kotlin.collections.ArrayList

/**
 * Dialog builder for the [ListSelectDialog] class, use this to create instances of that class and customize them.
 * @author Martin
 */
class ListSelectDialogBuilder<T> : AbstractDialogBuilder<ListSelectDialogBuilder<T>, ListSelectDialog<T>>("ListSelectDialog") {
    private val content: MutableList<T> = ArrayList()
    private var listBoxSize: TerminalSize? = null
    private var canCancel: Boolean = true

    override fun self(): ListSelectDialogBuilder<T> = this

    override fun buildDialog(): ListSelectDialog<T> {
        return ListSelectDialog(getTitle(), getDescription(), listBoxSize, canCancel, content)
    }

    /**
     * Sets preferred list-box size.
     */
    fun setListBoxSize(listBoxSize: TerminalSize?): ListSelectDialogBuilder<T> {
        this.listBoxSize = listBoxSize
        return this
    }

    /**
     * Returns preferred list-box size.
     */
    fun getListBoxSize(): TerminalSize? = listBoxSize

    /**
     * Controls whether cancel controls are enabled.
     */
    fun setCanCancel(canCancel: Boolean): ListSelectDialogBuilder<T> {
        this.canCancel = canCancel
        return this
    }

    /**
     * Returns whether cancel controls are enabled.
     */
    fun isCanCancel(): Boolean = canCancel

    /**
     * Adds a selectable list item.
     */
    fun addListItem(item: T): ListSelectDialogBuilder<T> {
        content.add(item)
        return this
    }

    /**
     * Adds multiple selectable list items.
     */
    fun addListItems(vararg items: T): ListSelectDialogBuilder<T> {
        content.addAll(listOf(*items))
        return this
    }

    /**
     * Returns a copy of configured list items.
     */
    fun getListItems(): List<T> = ArrayList(content)
}
