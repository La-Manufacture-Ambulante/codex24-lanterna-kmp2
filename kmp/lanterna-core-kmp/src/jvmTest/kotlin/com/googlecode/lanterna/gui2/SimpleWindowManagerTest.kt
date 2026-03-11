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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TestUtils

import java.io.*
import java.util.Arrays
import java.util.Collections

/**
 * Test/example class for various kinds of window manager behaviours
 * @author Martin
 */
 class SimpleWindowManagerTest:TestBase() {

fun init(textGUI:WindowBasedTextGUI) {
val mainWindow = BasicWindow("Choose test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(Button("Centered window", { textGUI.addWindow(CenteredWindow()) }))
contentArea.addComponent(Button("Undecorated window", { textGUI.addWindow(UndecoratedWindow()) }))
contentArea.addComponent(Button("Undecorated + Centered window", { textGUI.addWindow(UndecoratedCenteredWindow()) }))
contentArea.addComponent(Button("Full-screen window", { textGUI.addWindow(FullScreenWindow(true)) }))
contentArea.addComponent(Button("Undecorated + Full-screen window", { textGUI.addWindow(FullScreenWindow(false)) }))
contentArea.addComponent(Button("Expanded window", { textGUI.addWindow(ExpandedWindow(true)) }))
contentArea.addComponent(Button("Undecorated + Expanded window", { textGUI.addWindow(ExpandedWindow(false)) }))
contentArea.addComponent(Button("Close", Runnable { mainWindow.close() }))
mainWindow.component = contentArea
textGUI.addWindow(mainWindow)
}

private class CenteredWindow internal constructor():TestWindow("Centered window") {
init{
setHints(Collections.singletonList(Window.Hint.CENTERED))
}
}

private class UndecoratedWindow internal constructor():TestWindow("Undecorated") {
init{
setHints(Collections.singletonList(Window.Hint.NO_DECORATIONS))
}
}

private class UndecoratedCenteredWindow internal constructor():TestWindow("UndecoratedCentered") {

init{
setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.CENTERED))
}
}

private class FullScreenWindow(decorations:Boolean):TestWindow("FullScreenWindow") {

init{

val content = Panel()
content.setLayoutManager(BorderLayout())
val textBox = TextBox(TestUtils.downloadGPL() ?: "", TextBox.Style.MULTI_LINE)
textBox.setLayoutData(BorderLayout.Location.CENTER)
textBox.setReadOnly(true)
content.addComponent(textBox)

component = content

setHints(
    if (decorations) {
        Collections.singletonList(Window.Hint.FULL_SCREEN)
    } else {
        Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS)
    }
)
}
}

private class ExpandedWindow(decorations:Boolean):TestWindow("ExpandedWindow") {

init{

val content = Panel()
content.setLayoutManager(BorderLayout())
val textBox = TextBox(TestUtils.downloadGPL() ?: "", TextBox.Style.MULTI_LINE)
textBox.setLayoutData(BorderLayout.Location.CENTER)
textBox.setReadOnly(true)
content.addComponent(textBox)

component = content

setHints(
    if (decorations) {
        Collections.singletonList(Window.Hint.EXPANDED)
    } else {
        Arrays.asList(Window.Hint.EXPANDED, Window.Hint.NO_DECORATIONS)
    }
)
}
}

private open class TestWindow internal constructor(title:String):BasicWindow(title) {
init{
component = Button("Close", Runnable { this.close() })
setCloseWindowWithEscape(true)
}
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
SimpleWindowManagerTest().run(args)
}
}
}
