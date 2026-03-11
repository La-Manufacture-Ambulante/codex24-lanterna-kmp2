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
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

object FullScreenTextGUITest {
    @JvmStatic
    @Throws(IOException::class, InterruptedException::class)
    fun main(args: Array<String?>?) {
        val screen: Screen = TestTerminalFactory(args).createScreen()
            ?: return
        screen.startScreen()

        val stop = AtomicBoolean(false)
        val textGUI = MultiWindowTextGUI(screen)
        textGUI.addListener(object : TextGUI.Listener {
            override fun onUnhandledKeyStroke(textGUI: TextGUI?, keyStroke: KeyStroke?): Boolean {
                if (keyStroke?.keyType == KeyType.ESCAPE) {
                    stop.set(true)
                    return true
                }
                return false
            }
        })

        try {
            textGUI.backgroundPane.component = BIOS()
            while (!stop.get()) {
                if (textGUI.guiThread?.processEventsAndUpdate() != true) {
                    Thread.sleep(1)
                }
            }
        } catch (_: EOFException) {
            // Terminal closed.
        } finally {
            screen.stopScreen()
        }
    }

    private class BIOS : Panel() {
        private val background: TextImage = createBackground()

        init {
            setLayoutManager(AbsoluteLayout())
            val labels = listOf(
                "Standard Lanterna Features",
                "Advanced Lanterna Features",
                "Advanced Terminal Features",
                "Unintegrated Peripherals",
                "Power Management Setup",
                "Non-PnP/ISA Configurations",
                "Terminal Health Status",
                "Frequency/Current Control",
                "Load Fail-Safe Defaults",
                "Load Optimized Defaults",
                "Set Supervisor Password",
                "Set User Password",
                "Save & Exit Setup",
                "Exit Without Saving",
            )

            labels.forEachIndexed { index, label ->
                val col = if (index < 7) 3 else 43
                val row = 3 + (index % 7) * 2
                val button = Button("  $label")
                button.setPosition(TerminalPosition(col, row))
                button.setPreferredSize(TerminalSize(35, 1))
                addComponent(button)
            }

            val helpLabel = Label("Esc : Quit")
            helpLabel.setForegroundColor(TextColor.ANSI.YELLOW)
            helpLabel.setBackgroundColor(TextColor.ANSI.BLUE)
            helpLabel.addStyle(SGR.BOLD)
            helpLabel.setPosition(TerminalPosition(2, 22))
            helpLabel.setPreferredSize(TerminalSize(76, 1))
            addComponent(helpLabel)
        }

        private fun createBackground(): TextImage {
            val image = BasicTextImage(80, 25)
            val graphics = image.newTextGraphics()
            graphics.setForegroundColor(TextColor.ANSI.WHITE)
            graphics.setBackgroundColor(TextColor.ANSI.BLUE)
            graphics.fill(' ')
            graphics.enableModifiers(SGR.BOLD)

            graphics.putString(7, 0, "Reminds you of some BIOS, doesn't it?")
            graphics.setCharacter(0, 1, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER)
            graphics.drawLine(1, 1, 78, 1, Symbols.DOUBLE_LINE_HORIZONTAL)
            graphics.setCharacter(79, 1, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER)
            graphics.drawLine(79, 2, 79, 23, Symbols.DOUBLE_LINE_VERTICAL)
            graphics.setCharacter(79, 24, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER)
            graphics.drawLine(1, 24, 78, 24, Symbols.DOUBLE_LINE_HORIZONTAL)
            graphics.setCharacter(0, 24, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER)
            graphics.drawLine(0, 2, 0, 23, Symbols.DOUBLE_LINE_VERTICAL)

            graphics.setCharacter(0, 17, Symbols.DOUBLE_LINE_T_SINGLE_RIGHT)
            graphics.drawLine(1, 17, 78, 17, Symbols.SINGLE_LINE_HORIZONTAL)
            graphics.setCharacter(79, 17, Symbols.DOUBLE_LINE_T_SINGLE_LEFT)
            graphics.setCharacter(40, 17, Symbols.SINGLE_LINE_T_UP)
            graphics.drawLine(40, 2, 40, 16, Symbols.SINGLE_LINE_VERTICAL)
            graphics.setCharacter(40, 1, Symbols.DOUBLE_LINE_T_SINGLE_DOWN)

            graphics.setCharacter(0, 20, Symbols.DOUBLE_LINE_T_SINGLE_RIGHT)
            graphics.drawLine(1, 20, 78, 20, Symbols.SINGLE_LINE_HORIZONTAL)
            graphics.setCharacter(79, 20, Symbols.DOUBLE_LINE_T_SINGLE_LEFT)

            graphics.putString(2, 18, "Esc : Quit")
            graphics.putString(
                42,
                18,
                Symbols.ARROW_UP.toString() + " " + Symbols.ARROW_DOWN + " " + Symbols.ARROW_RIGHT + " " +
                    Symbols.ARROW_LEFT + "   : Select Item",
            )
            graphics.putString(2, 19, "F10 : Save & Exit Setup")
            return image
        }

        override fun createDefaultRenderer(): ComponentRenderer<Panel?> {
            val panelRenderer = super.createDefaultRenderer() as Panel.DefaultPanelRenderer
            panelRenderer.setFillAreaBeforeDrawingComponents(false)
            return object : ComponentRenderer<Panel?> {
                override fun getPreferredSize(component: Panel?): TerminalSize {
                    return TerminalSize(80, 24)
                }

                override fun drawComponent(graphics: TextGUIGraphics?, component: Panel?) {
                    val g = graphics ?: return
                    g.setBackgroundColor(TextColor.ANSI.BLACK)
                    g.fill(' ')
                    g.drawImage(TerminalPosition.TOP_LEFT_CORNER, background)
                    panelRenderer.drawComponent(g, this@BIOS)
                }
            }
        }
    }
}
