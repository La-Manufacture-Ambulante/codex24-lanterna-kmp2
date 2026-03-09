/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.*
import com.googlecode.lanterna.terminal.*

import java.io.IOException
import java.util.Arrays

 object Issue190 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val factory = DefaultTerminalFactory()
factory.setInitialTerminalSize(TerminalSize(150, 50))
factory.setTerminalEmulatorTitle("name")
val terminal = factory.createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()

val panel = Panel()
panel.setLayoutManager(BorderLayout())

val channels = ActionListBox()
channels.setLayoutData(BorderLayout.Location.LEFT)
panel.addComponent(channels.withBorder(Borders.singleLine("Channels")))

val log = TextBox("", TextBox.Style.MULTI_LINE)
log.setReadOnly(true)
log.setLayoutData(BorderLayout.Location.CENTER)
panel.addComponent(log.withBorder(Borders.singleLine("Log")))

val options = Panel()
options.setLayoutData(BorderLayout.Location.BOTTOM)

options.withBorder(Borders.singleLine("Send Message"))

options.setLayoutManager(BorderLayout())

val input = TextBox("Message", TextBox.Style.SINGLE_LINE)
input.setLayoutData(BorderLayout.Location.CENTER)
options.addComponent(input)

val send = Button("Send", { input.setText("") })
send.setLayoutData(BorderLayout.Location.RIGHT)
options.addComponent(send)

panel.addComponent(options.withBorder(Borders.singleLine("Send Message")))

val window = BasicWindow()
window.setComponent(panel.withBorder(Borders.doubleLine("DarkOwlBot")))

window.setHints(Arrays.asList(Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW))

val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
gui.addWindowAndWait(window)
}
}
