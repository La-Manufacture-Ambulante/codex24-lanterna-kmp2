package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object Issue334 {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal = DefaultTerminalFactory().createTerminal()!!
        val screen = TerminalScreen(terminal)
        screen.startScreen()

        // Create panel to hold components
        val panel = Panel()
        panel.setLayoutManager(GridLayout(1))
        panel.addComponent(Label(""))

        // Create gui and start gui
        val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))

        // Create window to hold the panel
        val window = BasicWindow()
        window.component = Panels.vertical(panel)
        window.setCloseWindowWithEscape(true)

        gui.addWindowAndWait(window)
        screen.stopScreen()
        terminal!!.close()
    }
}
