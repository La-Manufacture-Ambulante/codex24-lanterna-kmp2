package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.Window
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Dialog builder for the [MessageDialog] class.
 */
class MessageDialogBuilder {
    private var title: String = "MessageDialog"
    private var text: String = "Text"
    private val buttons: MutableList<MessageDialogButton> = ArrayList()
    private val extraWindowHints: MutableSet<Window.Hint?> = HashSet()

    init {
        extraWindowHints.add(Window.Hint.CENTERED)
        extraWindowHints.add(Window.Hint.MODAL)
    }

    fun build(): MessageDialog {
        val messageDialog = MessageDialog(title, text, *buttons.toTypedArray())
        messageDialog.setHints(extraWindowHints)
        return messageDialog
    }

    fun setTitle(title: String?): MessageDialogBuilder {
        this.title = title ?: ""
        return this
    }

    fun setText(text: String?): MessageDialogBuilder {
        this.text = text ?: ""
        return this
    }

    fun setExtraWindowHints(extraWindowHints: Collection<Window.Hint?>?): MessageDialogBuilder {
        this.extraWindowHints.clear()
        this.extraWindowHints.addAll(extraWindowHints.orEmpty())
        return this
    }

    fun addButton(button: MessageDialogButton?): MessageDialogBuilder {
        if (button != null) {
            buttons.add(button)
        }
        return this
    }
}
