package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.LocalizedString

/**
 * Available selection of buttons that can be added to a [MessageDialog].
 */
enum class MessageDialogButton(private val label: LocalizedString) {
    OK(LocalizedString.OK),
    CANCEL(LocalizedString.Cancel),
    YES(LocalizedString.Yes),
    NO(LocalizedString.No),
    CLOSE(LocalizedString.Close),
    ABORT(LocalizedString.Abort),
    IGNORE(LocalizedString.Ignore),
    RETRY(LocalizedString.Retry),
    CONTINUE(LocalizedString.Continue),
    ;

    override fun toString(): String {
        return label.toString()
    }
}
