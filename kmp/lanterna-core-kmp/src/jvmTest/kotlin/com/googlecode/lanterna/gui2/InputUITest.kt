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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.io.IOException

@SuppressWarnings("rawtypes")
class InputUITest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Input test")
        val interactable = InputCaptureComponent()
        interactable.withBorder(Borders.doubleLineBevel("Press any key to test capturing the KeyStroke"))

        window.component = Panels.vertical(
            interactable,
            Label("Use the TAB key to shift focus"),
            Button("Close", Runnable { window.close() }),
        )
        textGUI.addWindow(window)
    }

    private class InputCaptureComponent : AbstractInteractableComponent<InputCaptureComponent>() {
        private var lastKey: String? = null

        override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
            if (keyStroke.keyType == KeyType.TAB) {
                return super.handleKeyStroke(keyStroke)
            }
            lastKey = if (keyStroke.keyType == KeyType.CHARACTER) {
                keyStroke.character?.toString() ?: ""
            } else {
                keyStroke.keyType.toString()
            }
            if (keyStroke.isCtrlDown) lastKey += " + CTRL"
            if (keyStroke.isAltDown) lastKey += " + ALT"
            if (keyStroke.isShiftDown) lastKey += " + SHIFT"
            return Interactable.Result.HANDLED
        }

        override fun createDefaultRenderer(): InteractableRenderer<InputCaptureComponent?> {
            return object : InteractableRenderer<InputCaptureComponent?> {
                override fun getCursorLocation(component: InputCaptureComponent?): TerminalPosition? {
                    val size = component?.size ?: return TerminalPosition.TOP_LEFT_CORNER
                    val adjustedSize = size.withRelative(-1, -1) ?: size
                    return TerminalPosition(adjustedSize.columns, adjustedSize.rows)
                }

                override fun getPreferredSize(component: InputCaptureComponent?): TerminalSize {
                    return TerminalSize(70, 5)
                }

                override fun drawComponent(graphics: TextGUIGraphics?, component: InputCaptureComponent?) {
                    val g = graphics ?: return
                    g.setBackgroundColor(TextColor.ANSI.BLACK)
                    g.setForegroundColor(TextColor.ANSI.WHITE)
                    g.fill(' ')
                    val text = component?.lastKey
                    if (text != null) {
                        val leftPosition = 35 - (text.length / 2)
                        g.putString(leftPosition, 2, text)
                    }
                }
            }
        }
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        
        fun main(args: Array<String?>?) {
            InputUITest().run(args)
        }
    }
}
