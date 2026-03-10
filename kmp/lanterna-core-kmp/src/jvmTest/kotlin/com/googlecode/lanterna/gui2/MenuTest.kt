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

import java.io.File
import java.io.IOException

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import com.googlecode.lanterna.gui2.menu.Menu
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.gui2.menu.MenuItem
import com.googlecode.lanterna.input.KeyStroke

 class MenuTest:TestBase() {

@Override
 fun init(textGUI:WindowBasedTextGUI?) {
 // Create window to hold the menu
        val window = BasicWindow()
val contentPane = Panel(BorderLayout())
contentPane.addComponent(Panels.vertical(
Separator(Direction.HORIZONTAL).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL)), 
MultiColorComponent(), 
Button("Close", ???({ window.close() }))))
window.setComponent(contentPane)

val menubar = MenuBar()
window.setMenuBar(menubar)

 // "File" menu w/Accelerator Set
        val menuFile = Menu("File").setAccelerator(KeyStroke('f', false, true))
menubar.add(menuFile)
menuFile!!.add(MenuItem("Open...", { val file = FileDialogBuilder().build().showDialog(textGUI)
if (file != null)
MessageDialog.showMessageDialog(
textGUI, "Open", "Selected file:\n" + file!!, MessageDialogButton.OK) }).setAccelerator(KeyStroke('o', false, false)))
menuFile!!.add(MenuItem("Exit", ???({ window.close() })).setAccelerator(KeyStroke('x', false, false)))

 // Menu w/accelerator set
        val countryMenu = Menu("Country").setAccelerator(KeyStroke('c', false, true))
menubar.add(countryMenu)

 // Menu w/accelerator not set
        val germanySubMenu = Menu("Germany").setAccelerator(KeyStroke('g', false, false))
countryMenu!!.add(germanySubMenu)
for (state in GERMANY_STATES)
{
germanySubMenu!!.add(MenuItem(state, DO_NOTHING))
}

 // Menu w/accelerator set
        val japanSubMenu = Menu("Japan").setAccelerator(KeyStroke('j', false, false))
countryMenu!!.add(japanSubMenu)
for (prefecture in JAPAN_PREFECTURES)
{
japanSubMenu!!.add(MenuItem(prefecture, DO_NOTHING))
}

 // "Help" menu w/accelerator set
        val menuHelp = Menu("Help").setAccelerator(KeyStroke('h', false, true))
menubar.add(menuHelp)
menuHelp!!.add(MenuItem("Homepage", { MessageDialog.showMessageDialog(
textGUI, "Homepage", "https://github.com/mabe02/lanterna", MessageDialogButton.OK) }).setAccelerator(KeyStroke('h', false, false)))
menuHelp!!.add(MenuItem("About", { MessageDialog.showMessageDialog(
textGUI, "About", "Lanterna drop-down menu", MessageDialogButton.OK) }).setAccelerator(KeyStroke('a', false, false)))

 // Create textGUI and start textGUI
        textGUI!!.addWindow(window)
}

private class MultiColorComponent:AbstractComponent<MultiColorComponent?>() {
@Override
protected fun createDefaultRenderer():ComponentRenderer<MultiColorComponent?> {
return object:ComponentRenderer<MultiColorComponent?>() {
@Override
 fun getPreferredSize(component:MultiColorComponent?):TerminalSize {
return TerminalSize(40, 15)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:MultiColorComponent?) {
graphics!!.applyThemeStyle(getTheme().getDefaultDefinition().getNormal())
graphics!!.fill(' ')
var row = 1
for (color in TextColor.ANSI.values())
{
graphics!!.applyThemeStyle(getTheme().getDefaultDefinition().getNormal())
graphics!!.putString(1, row, color!!.toString() + ": ")
graphics!!.setForegroundColor(TextColor.ANSI.BLACK)
graphics!!.setBackgroundColor(color)
graphics!!.putString(20, row++, "     TEXT     ")
}
}
}
}
}

companion object {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
MenuTest().run(args)
}

private val DO_NOTHING = {  }

private val GERMANY_STATES = arrayOf<String?>("Baden-Württemberg", "Bayern", "Berlin", "Brandenburg", "Bremen", "Hamburg", "Hessen", "Mecklenburg-Vorpommern", "Niedersachsen", "Nordrhein-Westfalen", "Rheinland-Pfalz", "Saarland", "Sachsen", "Sachsen-Anhalt", "Schleswig-Holstein", "Thüringen")

private val JAPAN_PREFECTURES = arrayOf<String?>("Aichi", "Akita", "Aomori", "Chiba", "Ehime", "Fukui", "Fukuoka", "Fukushima", "Gifu", "Gunma", "Hiroshima", "Hokkaido", "Hyōgo", "Ibaraki", "Ishikawa", "Iwate", "Kagawa", "Kagoshima", "Kanagawa", "Kōchi", "Kumamoto", "Kyoto", "Mie", "Miyagi", "Miyazaki", "Nagano", "Nagasaki", "Nara", "Niigata", "Ōita", "Okayama", "Okinawa", "Osaka", "Saga", "Saitama", "Shiga", "Shimane", "Shizuoka", "Tochigi", "Tokushima", "Tokyo", "Tottori", "Toyama", "Wakayama", "Yamagata", "Yamaguchi", "Yamanashi")
}
}
