package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen

import java.io.IOException

 class WindowManagerTest:TestBase() {

@Override
protected fun createTextGUI(screen:Screen?):MultiWindowTextGUI? {
return MultiWindowTextGUI(SeparateTextGUIThread.Factory(), screen, CustomWindowManager())
}

@Override
 fun init(textGUI:WindowBasedTextGUI) {
val mainWindow = BasicWindow("Window Manager Test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(EmptySpace(TerminalSize.ONE))
contentArea.addComponent(Button("Close", object:Runnable() {
@Override
@JvmStatic  fun run() {
mainWindow.close()
}
}))
mainWindow.setComponent(contentArea)
textGUI.addWindow(mainWindow)
}

private class CustomWindowManager:DefaultWindowManager() {
@Override
protected fun prepareWindow(screenSize:TerminalSize?, window:Window?) {
super.prepareWindow(screenSize, window)

window!!.setDecoratedSize(window!!.getPreferredSize().withRelative(12, 10))
window!!.setPosition(TerminalPosition(
screenSize!!.columns - window!!.getDecoratedSize().getColumns() - 1, 
screenSize!!.rows - window!!.getDecoratedSize().getRows() - 1
))
}
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
WindowManagerTest().run(args)
}
}
}
