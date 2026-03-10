package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.Window
import java.util.Collections
import java.util.HashSet

/**
 * Abstract class for dialog building, containing much shared code between different kinds of dialogs.
 */
abstract class AbstractDialogBuilder<B, T : DialogWindow>(initialTitle: String?) {
    private var dialogTitle: String? = initialTitle
    private var dialogDescription: String? = null
    private var dialogExtraWindowHints: Set<Window.Hint?> = Collections.singleton(Window.Hint.CENTERED)

    fun setTitle(title: String?): B {
        this.dialogTitle = title ?: ""
        return self()
    }

    fun getTitle(): String? = dialogTitle

    fun setDescription(description: String?): B {
        this.dialogDescription = description
        return self()
    }

    fun getDescription(): String? = dialogDescription

    fun setExtraWindowHints(extraWindowHints: Set<Window.Hint?>?): B {
        this.dialogExtraWindowHints = extraWindowHints ?: emptySet()
        return self()
    }

    fun getExtraWindowHints(): Set<Window.Hint?> = dialogExtraWindowHints

    protected abstract fun self(): B

    protected abstract fun buildDialog(): T

    fun build(): T {
        val dialog = buildDialog()
        if (dialogExtraWindowHints.isNotEmpty()) {
            val combinedHints = HashSet(dialog.hints.orEmpty())
            combinedHints.addAll(dialogExtraWindowHints)
            dialog.setHints(combinedHints)
        }
        return dialog
    }
}
