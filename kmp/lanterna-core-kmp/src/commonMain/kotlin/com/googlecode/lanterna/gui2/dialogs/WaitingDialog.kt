package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.*

open class WaitingDialog private constructor(title: String, text: String) : DialogWindow(title) {
    init {
        val mainPanel: Panel = Panels.horizontal(
            Label(text),
            AnimatedLabel.createClassicSpinningLine()
        )
        setComponent(mainPanel)
    }

    open override fun showDialog(textGUI: WindowBasedTextGUI): Any? {
        showDialog(textGUI, true)
        return null
    }

    /**
     * Displays the waiting dialog and optionally blocks until another thread closes it
     * @param textGUI GUI to add the dialog to
     * @param blockUntilClosed If `true`, the method call will block until another thread calls `close()` on
     *                         the dialog, otherwise the method call returns immediately
     */
    open fun showDialog(textGUI: WindowBasedTextGUI, blockUntilClosed: Boolean) {
        textGUI.addWindow(this)

        if (blockUntilClosed) {
            //Wait for the window to close, in case the window manager doesn't honor the MODAL hint
            waitUntilClosed()
        }
    }

    companion object {
        /**
         * Creates a new waiting dialog
         * @param title Title of the waiting dialog
         * @param text Text to display on the waiting dialog
         * @return Created waiting dialog
         */
        @JvmStatic
        fun createDialog(title: String, text: String): WaitingDialog {
            return WaitingDialog(title, text)
        }

        /**
         * Creates and displays a waiting dialog without blocking for it to finish
         * @param textGUI GUI to add the dialog to
         * @param title Title of the waiting dialog
         * @param text Text to display on the waiting dialog
         * @return Created waiting dialog
         */
        @JvmStatic
        fun showDialog(textGUI: WindowBasedTextGUI, title: String, text: String): WaitingDialog {
            val waitingDialog = createDialog(title, text)
            waitingDialog.showDialog(textGUI, false)
            return waitingDialog
        }
    }
}
