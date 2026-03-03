package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.Window
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet

open class MessageDialogBuilder {
    private var title: String
    private var text: String
    private val buttons: MutableList<MessageDialogButton>
    private val extraWindowHints: MutableSet<Window.Hint>

    constructor() {
        this.title = "MessageDialog"
        this.text = "Text"
        this.buttons = ArrayList()
        this.extraWindowHints = HashSet()
        this.extraWindowHints.add(Window.Hint.CENTERED)
        this.extraWindowHints.add(Window.Hint.MODAL)
    }

    open fun build(): MessageDialog {
        val messageDialog = MessageDialog(
            title,
            text,
            buttons.toTypedArray()
        )
        messageDialog.setHints(extraWindowHints)
        return messageDialog
    }

    open fun setTitle(title: String?): MessageDialogBuilder {
        var titleVar = title
        if (titleVar == null) {
            titleVar = ""
        }
        this.title = titleVar
        return this
    }

    open fun setText(text: String?): MessageDialogBuilder {
        var textVar = text
        if (textVar == null) {
            textVar = ""
        }
        this.text = textVar
        return this
    }

    open fun setExtraWindowHints(extraWindowHints: Collection<Window.Hint>?): MessageDialogBuilder {
        this.extraWindowHints.clear()
        if (extraWindowHints == null) {
            throw NullPointerException()
        }
        this.extraWindowHints.addAll(extraWindowHints)
        return this
    }

    open fun addButton(button: MessageDialogButton?): MessageDialogBuilder {
        if (button != null) {
            buttons.add(button)
        }
        return this
    }
}
