package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.AbstractWindow
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.internal.compat.Collections
import kotlin.collections.HashSet

/**
 * Thin layer on top of the [AbstractWindow] class that makes it act more like a modal dialog.
 */
abstract class DialogWindow protected constructor(title: String?) : AbstractWindow(title) {
    init {
        setHints(GLOBAL_DIALOG_HINTS)
    }

    open fun showDialog(textGUI: WindowBasedTextGUI): Any? {
        textGUI.addWindow(this)
        waitUntilClosed()
        return null
    }

    companion object {
        private val GLOBAL_DIALOG_HINTS: Set<Window.Hint?> =
            Collections.unmodifiableSet(HashSet(Collections.singletonList(Window.Hint.MODAL)))
    }
}
