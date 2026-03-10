/*
 * Author Valentin(linouxis9), modified by Andreas(avl42)
 */
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.*
import com.googlecode.lanterna.terminal.*

import java.io.IOException

 object Issue359 {
 fun main(args:Array<String?>?) {
try
{
val screen = DefaultTerminalFactory().createScreen()
screen!!.startScreen()

val window = BasicWindow()
val button = Button("Hello")

 // Replacing a Component by itself just Border-wrapped
            // caused a NullPointerException lateron from within
            //   the call to gui.addWindowAndWait(window);
            window.setComponent(button)
window.setComponent(button.withBorder(Borders.singleLine("Border")))

val gui = MultiWindowTextGUI(screen)
gui.addWindowAndWait(window)
}
catch (e:IOException) {
e!!.printStackTrace()
}

}
}
