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
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import java.io.IOException

class LineWrappingLabelTest : TestBase() {
    private var windowSize: TerminalSize = TerminalSize(70, 15)

    protected fun createTextGUI(screen: Screen): MultiWindowTextGUI {
        return MultiWindowTextGUI(
            SeparateTextGUIThread.Factory(),
            screen,
            DefaultWindowManager(),
            WindowShadowRenderer(),
            EmptySpace(TextColor.ANSI.BLUE),
        )
    }

    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Wrapping label test")
        val contentPane = Panel()
        contentPane.setLayoutManager(BorderLayout())
        contentPane.addComponent(Label("Resize window by holding ctrl and pressing arrow keys").setLayoutData(BorderLayout.Location.TOP))
        val bigTextLabel = Label(BIG_TEXT)
        bigTextLabel.withBorder(Borders.doubleLine())
        contentPane.addComponent(bigTextLabel.setLayoutData(BorderLayout.Location.CENTER))
        contentPane.addComponent(Button("Close", Runnable { window.close() }).setLayoutData(BorderLayout.Location.BOTTOM))

        window.component = contentPane

        textGUI.addListener(
            object : TextGUI.Listener {
                override fun onUnhandledKeyStroke(
                    textGUI1: TextGUI?,
                    keyStroke: com.googlecode.lanterna.input.KeyStroke?,
                ): Boolean {
                    if (keyStroke?.isCtrlDown != true) {
                        return false
                    }
                    when (keyStroke.keyType) {
                        KeyType.ARROW_UP -> {
                            windowSize =
                                (
                                    if (windowSize.rows > 1) {
                                        windowSize.withRelativeRows(-1)
                                    } else {
                                        windowSize.withRelativeRows(1)
                                    }
                                ) ?: windowSize
                            return true
                        }
                        KeyType.ARROW_DOWN -> {
                            windowSize = windowSize.withRelativeRows(1) ?: windowSize
                            return true
                        }
                        KeyType.ARROW_LEFT -> {
                            windowSize =
                                (
                                    if (windowSize.columns > 1) {
                                        windowSize.withRelativeColumns(-1)
                                    } else {
                                        windowSize.withRelativeColumns(1)
                                    }
                                ) ?: windowSize
                            return true
                        }
                        KeyType.ARROW_RIGHT -> {
                            windowSize = windowSize.withRelativeColumns(1) ?: windowSize
                            return true
                        }
                        else -> return false
                    }
                }
            },
        )

        textGUI.addWindow(window)
    }

    companion object {
        val BIG_TEXT: String = (
            "                   GNU LESSER GENERAL PUBLIC LICENSE\n" +
                "                       Version 3, 29 June 2007\n" +
                "\n" +
                " Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>\n" +
                " Everyone is permitted to copy and distribute verbatim copies of this license document, " +
                "but changing it is not allowed.\n" +
                "\n" +
                "\n" +
                "  This version of the GNU Lesser General Public License incorporates the terms and conditions " +
                "of version 3 of the GNU General Public License, supplemented by the additional permissions " +
                "listed below.\n" +
                "\n" +
                "  0. Additional Definitions.\n" +
                "\n" +
                "  As used herein, \"this License\" refers to version 3 of the GNU Lesser General Public " +
                "License, and the \"GNU GPL\" refers to version 3 of the GNU General Public License.\n" +
                "\n" +
                "  \"The Library\" refers to a covered work governed by this License, other than an Application " +
                "or a Combined Work as defined below."
        )

        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            LineWrappingLabelTest().run(args)
        }
    }
}
