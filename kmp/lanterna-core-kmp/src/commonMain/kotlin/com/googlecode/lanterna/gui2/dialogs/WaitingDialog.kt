package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.AnimatedLabel
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.Panels
import com.googlecode.lanterna.gui2.WindowBasedTextGUI

/**
 * Dialog that displays a text message and optional spinning indicator.
 */
class WaitingDialog private constructor(title: String?, text: String?) : DialogWindow(title) {
    init {
        val mainPanel = Panels.horizontal(Label(text), AnimatedLabel.createClassicSpinningLine())
        component = mainPanel
    }

    override fun showDialog(textGUI: WindowBasedTextGUI): Any? {
        showDialog(textGUI, true)
        return null
    }

    fun showDialog(
        textGUI: WindowBasedTextGUI,
        blockUntilClosed: Boolean,
    ) {
        textGUI.addWindow(this)
        if (blockUntilClosed) {
            waitUntilClosed()
        }
    }

    companion object {
        fun createDialog(
            title: String?,
            text: String?,
        ): WaitingDialog {
            return WaitingDialog(title, text)
        }

        fun showDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            text: String?,
        ): WaitingDialog {
            val waitingDialog = createDialog(title, text)
            waitingDialog.showDialog(textGUI, false)
            return waitingDialog
        }
    }
}
