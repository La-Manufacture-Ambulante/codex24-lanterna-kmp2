package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.TextGUIThread
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import java.io.IOException

 object Issue392 {
private var textGUI:MultiWindowTextGUI? = null

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminalFactory = DefaultTerminalFactory()
val terminal = terminalFactory.createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()
textGUI = MultiWindowTextGUI(screen)
setExceptionHandler()
val window = BasicWindow()

val button = Button("test")
button.addListener({
setExceptionHandler()
throw RuntimeException("This should be caught in the uncaght exception handler!") })
window.component = button

textGUI!!.addWindowAndWait(window)
screen.stopScreen()
}

private fun setExceptionHandler() {
textGUI!!.getGUIThread()!!.setExceptionHandler(object:TextGUIThread.ExceptionHandler {

private fun handleException(e:Exception):Boolean {
System.err.println("### Caught!")
e.printStackTrace()
return false
}

public override fun onIOException(e:IOException?):Boolean {
return handleException(e!!)
}

public override fun onRuntimeException(e:RuntimeException?):Boolean {
return handleException(e!!)
}
})
}
}
