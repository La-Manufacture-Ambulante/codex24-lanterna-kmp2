package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen

import java.io.IOException

 class WindowManagerTest:TestBase() {

protected fun createTextGUI(screen:Screen):MultiWindowTextGUI {
return MultiWindowTextGUI(SeparateTextGUIThread.Factory(), screen, CustomWindowManager())
}

fun init(textGUI:WindowBasedTextGUI) {
val mainWindow = BasicWindow("Window Manager Test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(EmptySpace(TerminalSize.ONE))
contentArea.addComponent(Button("Close", Runnable { mainWindow.close() }))
mainWindow.component = contentArea
textGUI.addWindow(mainWindow)
}

private class CustomWindowManager:DefaultWindowManager() {
override fun onAdded(textGUI: WindowBasedTextGUI?, window: Window?, allWindows: List<Window?>?) {
super.onAdded(textGUI, window, allWindows)
val w = window ?: return
val screen = textGUI?.screen?.terminalSize ?: return
w.decoratedSize = (w.preferredSize ?: TerminalSize.ZERO).withRelative(12, 10)
w.position = TerminalPosition(
screen.columns - (w.decoratedSize?.columns ?: 0) - 1,
screen.rows - (w.decoratedSize?.rows ?: 0) - 1
)
}
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
WindowManagerTest().run(args)
}
}
}
