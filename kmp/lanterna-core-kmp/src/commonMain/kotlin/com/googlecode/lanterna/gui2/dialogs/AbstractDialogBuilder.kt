package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.Window
import java.util.Collections
import java.util.HashSet

/**
 * Abstract class for dialog building, containing much shared code between different kinds of dialogs.
 */
abstract class AbstractDialogBuilder<B, T : DialogWindow>(protected var title: String?) {
    protected var description: String? = null
    protected var extraWindowHints: Set<Window.Hint?> = Collections.singleton(Window.Hint.CENTERED)

    fun setTitle(title: String?): B {
        this.title = title ?: ""
        return self()
    }

    fun getTitle(): String? = title

    fun setDescription(description: String?): B {
        this.description = description
        return self()
    }

    fun getDescription(): String? = description

    fun setExtraWindowHints(extraWindowHints: Set<Window.Hint?>?): B {
        this.extraWindowHints = extraWindowHints ?: emptySet()
        return self()
    }

    fun getExtraWindowHints(): Set<Window.Hint?> = extraWindowHints

    protected abstract fun self(): B

    protected abstract fun buildDialog(): T

    fun build(): T {
        val dialog = buildDialog()
        if (extraWindowHints.isNotEmpty()) {
            val combinedHints = HashSet(dialog.hints.orEmpty())
            combinedHints.addAll(extraWindowHints)
            dialog.setHints(combinedHints)
        }
        return dialog
    }
}
