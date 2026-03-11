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
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import com.googlecode.lanterna.gui2.menu.Menu
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.gui2.menu.MenuItem

import java.io.File
import java.io.IOException

class MenuTest:TestBase() {

fun init(textGUI:WindowBasedTextGUI) {
 // Create window to hold the menu
        val window = BasicWindow()
val contentPane = Panel(BorderLayout())
contentPane.addComponent(Panels.vertical(
Separator(Direction.HORIZONTAL).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL)),
MultiColorComponent(),
Button("Close", Runnable { window.close() })))
window.component = contentPane

val menubar = MenuBar()
window.menuBar = menubar

 // "File" menu
        val menuFile = Menu("File")
menubar.add(menuFile)
menuFile.add(MenuItem("Open...", { val file = FileDialogBuilder().build()!!.showDialog(textGUI)
if (file != null)
MessageDialog.showMessageDialog(
textGUI, "Open", "Selected file:\n$file", MessageDialogButton.OK) }))
menuFile.add(MenuItem("Exit", Runnable { window.close() }))

val countryMenu = Menu("Country")
menubar.add(countryMenu)

val germanySubMenu = Menu("Germany")
countryMenu.add(germanySubMenu)
for (state in GERMANY_STATES)
{
germanySubMenu.add(MenuItem(state, DO_NOTHING))
}
val japanSubMenu = Menu("Japan")
countryMenu.add(japanSubMenu)
for (prefecture in JAPAN_PREFECTURES)
{
japanSubMenu.add(MenuItem(prefecture, DO_NOTHING))
}

 // "Help" menu
        val menuHelp = Menu("Help")
menubar.add(menuHelp)
menuHelp.add(MenuItem("Homepage", { MessageDialog.showMessageDialog(
textGUI, "Homepage", "https://github.com/mabe02/lanterna", MessageDialogButton.OK) }))
menuHelp.add(MenuItem("About", { MessageDialog.showMessageDialog(
textGUI, "About", "Lanterna drop-down menu", MessageDialogButton.OK) }))

 // Create textGUI and start textGUI
        textGUI.addWindow(window)
}

private class MultiColorComponent:AbstractComponent<MultiColorComponent?>() {
protected override fun createDefaultRenderer():ComponentRenderer<MultiColorComponent?> {
return object:ComponentRenderer<MultiColorComponent?> {
public override fun getPreferredSize(component:MultiColorComponent?):TerminalSize {
return TerminalSize(40, 15)
}

public override fun drawComponent(graphics:TextGUIGraphics?, component:MultiColorComponent?) {
graphics!!.applyThemeStyle(getTheme()!!.getDefaultDefinition()!!.getNormal())
graphics!!.fill(' ')
var row = 1
for (color in TextColor.ANSI.values())
{
graphics!!.applyThemeStyle(getTheme()!!.getDefaultDefinition()!!.getNormal())
graphics!!.putString(1, row, color.toString() + ": ")
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

private val DO_NOTHING = Runnable { }

private val GERMANY_STATES = arrayOf("Baden-Württemberg", "Bayern", "Berlin", "Brandenburg", "Bremen", "Hamburg", "Hessen", "Mecklenburg-Vorpommern", "Niedersachsen", "Nordrhein-Westfalen", "Rheinland-Pfalz", "Saarland", "Sachsen", "Sachsen-Anhalt", "Schleswig-Holstein", "Thüringen")

private val JAPAN_PREFECTURES = arrayOf("Aichi", "Akita", "Aomori", "Chiba", "Ehime", "Fukui", "Fukuoka", "Fukushima", "Gifu", "Gunma", "Hiroshima", "Hokkaido", "Hyōgo", "Ibaraki", "Ishikawa", "Iwate", "Kagawa", "Kagoshima", "Kanagawa", "Kōchi", "Kumamoto", "Kyoto", "Mie", "Miyagi", "Miyazaki", "Nagano", "Nagasaki", "Nara", "Niigata", "Ōita", "Okayama", "Okinawa", "Osaka", "Saga", "Saitama", "Shiga", "Shimane", "Shizuoka", "Tochigi", "Tokushima", "Tokyo", "Tottori", "Toyama", "Wakayama", "Yamagata", "Yamaguchi", "Yamanashi")
}
}
