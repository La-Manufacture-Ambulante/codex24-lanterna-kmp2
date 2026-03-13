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
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.internal.compat.AtomicBoolean
import java.io.IOException
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class MultiWindowManagerTest : TestBase() {
    private var virtualScreenEnabled = true
    private var buttonToggleVirtualScreen: Button? = null

    fun init(textGUI: WindowBasedTextGUI) {
        textGUI.backgroundPane?.component = BackgroundComponent()

        val mainWindow = BasicWindow("Multi Window Test")
        val contentArea = Panel().setLayoutManager(LinearLayout(Direction.VERTICAL))
        contentArea.addComponent(Button("Add new window", Runnable { onNewWindow(textGUI) }))
        buttonToggleVirtualScreen =
            Button(
                "Virtual Screen: Enabled",
                Runnable {
                    virtualScreenEnabled = !virtualScreenEnabled
                    textGUI.setVirtualScreenEnabled(virtualScreenEnabled)
                    buttonToggleVirtualScreen?.setLabel(
                        "Virtual Screen: " + if (virtualScreenEnabled) "Enabled" else "Disabled",
                    )
                },
            )
        contentArea.addComponent(buttonToggleVirtualScreen)
        contentArea.addComponent(EmptySpace(TerminalSize.ONE))
        contentArea.addComponent(Button("Close", Runnable { mainWindow.close() }))
        mainWindow.component = contentArea

        textGUI.addListener(
            object : TextGUI.Listener {
                override fun onUnhandledKeyStroke(
                    textGUI: TextGUI?,
                    keyStroke: KeyStroke?,
                ): Boolean {
                    val gui = textGUI as? WindowBasedTextGUI ?: return false
                    val key = keyStroke ?: return false
                    if ((key.isCtrlDown && key.keyType == KeyType.TAB) || key.keyType == KeyType.F6) {
                        gui.cycleActiveWindow(false)
                        return true
                    }
                    if ((key.isCtrlDown && key.keyType == KeyType.REVERSE_TAB) || key.keyType == KeyType.F7) {
                        gui.cycleActiveWindow(true)
                        return true
                    }
                    return false
                }
            },
        )

        textGUI.addWindow(mainWindow)
    }

    private fun onNewWindow(textGUI: WindowBasedTextGUI) {
        val window = DynamicWindow()
        val themes = LanternaThemes.registeredThemes.filterNotNull()
        if (themes.isNotEmpty()) {
            nextTheme = (nextTheme + 1) % themes.size
        }
        textGUI.addWindow(window)
    }

    private class DynamicWindow : BasicWindow("Window #${WINDOW_COUNTER.incrementAndGet()}") {
        private val labelWindowSize: Label
        private val labelWindowPosition: Label
        private val labelUnlockWindow: Label

        init {
            val statsTableContainer = Panel().setLayoutManager(GridLayout(2))
            statsTableContainer.addComponent(Label("Position:"))
            labelWindowPosition = Label("")
            statsTableContainer.addComponent(labelWindowPosition)
            statsTableContainer.addComponent(Label("Size:"))
            labelWindowSize = Label("")
            statsTableContainer.addComponent(labelWindowSize)
            statsTableContainer.addComponent(Label("Auto-sized:"))
            labelUnlockWindow = Label("true")
            statsTableContainer.addComponent(labelUnlockWindow)

            addWindowListener(
                object : WindowListener {
                    override fun onResized(
                        window: Window?,
                        oldSize: TerminalSize?,
                        newSize: TerminalSize?,
                    ) {
                        if (newSize != null) {
                            labelWindowSize.setText(newSize.toString())
                        }
                    }

                    override fun onMoved(
                        window: Window?,
                        oldPosition: TerminalPosition?,
                        newPosition: TerminalPosition?,
                    ) {
                        if (newPosition != null) {
                            labelWindowPosition.setText(newPosition.toString())
                        }
                    }

                    override fun onInput(
                        basePane: Window?,
                        keyStroke: KeyStroke?,
                        deliverEvent: AtomicBoolean?,
                    ) = Unit

                    override fun onUnhandledInput(
                        basePane: Window?,
                        keyStroke: KeyStroke?,
                        hasBeenHandled: AtomicBoolean?,
                    ) = Unit
                },
            )

            val contentArea = Panel().setLayoutManager(GridLayout(1))
            contentArea.addComponent(statsTableContainer)
            contentArea.addComponent(EmptySpace(TerminalSize.ONE))
            contentArea.addComponent(Label("Move window with ALT+Arrow\nResize window with CTRL+Arrow"))
            contentArea.addComponent(
                EmptySpace(TerminalSize.ONE)
                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true)),
            )
            contentArea.addComponent(
                Panels.horizontal(
                    Button("Toggle auto-sized", Runnable { toggleManaged() }),
                    Button("Close", Runnable { close() }),
                ),
            )
            component = contentArea
        }

        private fun toggleManaged() {
            val isManaged = hints?.contains(Window.Hint.FIXED_SIZE) != true
            if (isManaged) {
                setHints(Collections.emptyList())
            } else {
                setHints(Collections.singletonList(Window.Hint.FIXED_SIZE))
            }
            labelUnlockWindow.setText(isManaged.toString())
        }

        override fun handleInput(key: KeyStroke?): Boolean {
            var handled = super.handleInput(key)
            if (!handled && key != null) {
                when (key.keyType) {
                    KeyType.ARROW_DOWN -> {
                        if (key.isAltDown) {
                            position = position?.withRelativeRow(1)
                        } else if (key.isCtrlDown) {
                            setFixedSize(size?.withRelativeRows(1))
                            labelUnlockWindow.setText("false")
                        }
                        handled = true
                    }

                    KeyType.ARROW_LEFT -> {
                        if (key.isAltDown) {
                            position = position?.withRelativeColumn(-1)
                        } else if (key.isCtrlDown && (size?.columns ?: 0) > 1) {
                            setFixedSize(size?.withRelativeColumns(-1))
                            labelUnlockWindow.setText("false")
                        }
                        handled = true
                    }

                    KeyType.ARROW_RIGHT -> {
                        if (key.isAltDown) {
                            position = position?.withRelativeColumn(1)
                        } else if (key.isCtrlDown) {
                            setFixedSize(size?.withRelativeColumns(1))
                            labelUnlockWindow.setText("false")
                        }
                        handled = true
                    }

                    KeyType.ARROW_UP -> {
                        if (key.isAltDown) {
                            position = position?.withRelativeRow(-1)
                        } else if (key.isCtrlDown && (size?.rows ?: 0) > 1) {
                            setFixedSize(size?.withRelativeRows(-1))
                            labelUnlockWindow.setText("false")
                        }
                        handled = true
                    }

                    else -> Unit
                }
            }
            return handled
        }
    }

    private class BackgroundComponent : EmptySpace() {
        override fun createDefaultRenderer(): ComponentRenderer<EmptySpace?> {
            return object : ComponentRenderer<EmptySpace?> {
                override fun getPreferredSize(component: EmptySpace?): TerminalSize {
                    return TerminalSize.ONE
                }

                override fun drawComponent(
                    graphics: TextGUIGraphics?,
                    component: EmptySpace?,
                ) {
                    val g = graphics ?: return
                    val c = component ?: return
                    val definition = c.theme?.getDefinition(GUIBackdrop::class) ?: return
                    g.applyThemeStyle(definition.normal)
                    g.fill('・')
                    val text = "Press <CTRL+Tab>/F6 and <CTRL+Shift+Tab>/F7 to cycle active window"
                    val size = g.size ?: TerminalSize.ZERO
                    g.putString(size.columns - text.length - 4, size.rows - 1, text)
                }
            }
        }
    }

    companion object {
        private val WINDOW_COUNTER = AtomicInteger(0)
        private var nextTheme = 0

        @JvmStatic
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            MultiWindowManagerTest().run(args)
        }
    }
}
