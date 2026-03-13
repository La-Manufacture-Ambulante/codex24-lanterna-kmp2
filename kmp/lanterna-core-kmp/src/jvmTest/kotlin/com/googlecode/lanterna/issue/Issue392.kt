package com.googlecode.lanterna.issue
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.TextGUIThread
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object Issue392 {
    private var textGUI: MultiWindowTextGUI? = null

    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminalFactory = DefaultTerminalFactory()
        val terminal = terminalFactory.createTerminal()!!
        val screen = TerminalScreen(terminal)
        screen.startScreen()
        textGUI = MultiWindowTextGUI(screen)
        setExceptionHandler()
        val window = BasicWindow()

        val button = Button("test")
        button.addListener(
            object : Button.Listener {
                override fun onTriggered(button: Button?) {
                    setExceptionHandler()
                    throw RuntimeException("This should be caught in the uncaght exception handler!")
                }
            },
        )
        window.component = button

        textGUI!!.addWindowAndWait(window)
        screen.stopScreen()
    }

    private fun setExceptionHandler() {
        textGUI!!.guiThread!!.setExceptionHandler(
            object : TextGUIThread.ExceptionHandler {
                private fun handleException(e: Throwable): Boolean {
                    System.err.println("### Caught!")
                    e.printStackTrace()
                    return false
                }

                override fun onException(error: Throwable): Boolean {
                    return handleException(error)
                }
            },
        )
    }
}
