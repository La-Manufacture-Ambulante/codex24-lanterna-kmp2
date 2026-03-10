package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import kotlin.collections.ArrayList
import com.googlecode.lanterna.internal.compat.Arrays

/**
 * Dialog builder for the [ListSelectDialog] class.
 */
class ListSelectDialogBuilder<T> : AbstractDialogBuilder<ListSelectDialogBuilder<T>, ListSelectDialog<T>>("ListSelectDialog") {
    private val content: MutableList<T> = ArrayList()
    private var listBoxSize: TerminalSize? = null
    private var canCancel: Boolean = true

    override fun self(): ListSelectDialogBuilder<T> = this

    override fun buildDialog(): ListSelectDialog<T> {
        return ListSelectDialog(getTitle(), getDescription(), listBoxSize, canCancel, content)
    }

    fun setListBoxSize(listBoxSize: TerminalSize?): ListSelectDialogBuilder<T> {
        this.listBoxSize = listBoxSize
        return this
    }

    fun getListBoxSize(): TerminalSize? = listBoxSize

    fun setCanCancel(canCancel: Boolean): ListSelectDialogBuilder<T> {
        this.canCancel = canCancel
        return this
    }

    fun isCanCancel(): Boolean = canCancel

    fun addListItem(item: T): ListSelectDialogBuilder<T> {
        content.add(item)
        return this
    }

    fun addListItems(vararg items: T): ListSelectDialogBuilder<T> {
        content.addAll(Arrays.asList(*items))
        return this
    }

    fun getListItems(): List<T> = ArrayList(content)
}
