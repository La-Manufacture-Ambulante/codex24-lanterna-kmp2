/*
 * Author Valentin(linouxis9), modified by Andreas(avl42)
 */
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object Issue359 {
    fun main(args: Array<String?>?) {
        try {
            val screen = DefaultTerminalFactory().createScreen()
            screen!!.startScreen()

            val window = BasicWindow()
            val button = Button("Hello")

            // Replacing a Component by itself just Border-wrapped
            // caused a NullPointerException lateron from within
            //   the call to gui.addWindowAndWait(window);
            window.component = button
            window.component = button.withBorder(Borders.singleLine("Border"))

            val gui = MultiWindowTextGUI(screen)
            gui.addWindowAndWait(window)
        } catch (e: IOException) {
            e!!.printStackTrace()
        }
    }
}
