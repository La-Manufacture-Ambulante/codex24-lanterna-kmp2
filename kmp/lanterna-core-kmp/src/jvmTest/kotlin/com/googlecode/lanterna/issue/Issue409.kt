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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.graphics.DefaultMutableThemeStyle
import com.googlecode.lanterna.graphics.DelegatingTheme
import com.googlecode.lanterna.graphics.DelegatingThemeDefinition
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException
import java.util.ArrayList
import kotlin.reflect.KClass

object Issue409 {
    fun main(args: Array<String?>?) {
        try {
            val screen = DefaultTerminalFactory().createScreen()
            screen.startScreen()

            val window = BasicWindow()

            val panel = Panel()
            panel.addComponent(CustomBackgroundTextBox(TextColor.ANSI.RED))
            panel.addComponent(EmptySpace())
            panel.addComponent(CustomBackgroundTextBox(TextColor.ANSI.GREEN))
            panel.addComponent(EmptySpace())
            val cyclingThemesTextBox = CyclingThemesTextBox()
            panel.addComponent(cyclingThemesTextBox)
            panel.addComponent(EmptySpace())
            panel.addComponent(Button("Close") { window.close() })

            window.component = panel
            val gui = MultiWindowTextGUI(screen)
            gui.addWindow(window)
            Thread({
                var counter = 0
                while (cyclingThemesTextBox.textGUI != null) {
                    if (++counter % 200 == 0) {
                        gui.guiThread?.invokeLater { cyclingThemesTextBox.nextTheme() }
                    } else {
                        try {
                            Thread.sleep(10)
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                }
            }).start()

            window.waitUntilClosed()
            screen.stopScreen()
        } catch (e: IOException) {
            e!!.printStackTrace()
        }
    }

    private class CustomBackgroundTextBox(color: TextColor.ANSI) : TextBox("Custom " + color.name) {
        init {
            setTheme(
                object : DelegatingTheme(theme ?: LanternaThemes.defaultTheme!!) {
                    public override fun getDefinition(clazz: KClass<*>?): ThemeDefinition {
                        val themeDefinition = super.getDefinition(clazz)
                        return FixedBackgroundTextBoxThemeStyle(themeDefinition!!, color)
                    }
                },
            )
        }
    }

    private class CyclingThemesTextBox : TextBox("Cycling themes: default") {
        private val systemThemes: List<String>
        private var index: Int = 0

        init {
            setPreferredSize(TerminalSize(40, 1))
            @Suppress("UNCHECKED_CAST")
            systemThemes = ArrayList(LanternaThemes.registeredThemes as Collection<String>)
            index = 0
        }

        internal fun nextTheme() {
            if (++index == systemThemes.size) {
                index = 0
            }
            val name = systemThemes[index]
            val theme = LanternaThemes.getRegisteredTheme(name)
            setTheme(theme)
            setText("Cycling themes: " + name)
        }
    }

    private class FixedBackgroundTextBoxThemeStyle(
        definition: ThemeDefinition,
        private val color: TextColor.ANSI?,
    ) : DelegatingThemeDefinition(definition) {
        override val normal: ThemeStyle?
            get() {
                val mutableThemeStyle = DefaultMutableThemeStyle(super.normal!!)
                return mutableThemeStyle.setBackground(color)
            }

        override val active: ThemeStyle?
            get() {
                val mutableThemeStyle = DefaultMutableThemeStyle(super.active!!)
                return mutableThemeStyle.setBackground(color)
            }
    }
}
