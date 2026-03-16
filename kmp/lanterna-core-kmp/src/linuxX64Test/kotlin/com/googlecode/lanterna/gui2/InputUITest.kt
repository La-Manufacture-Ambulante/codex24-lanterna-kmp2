package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal
import kotlin.test.Test
import kotlin.test.assertTrue

class InputUITest {
    @Test
    fun capturesCharacterAndClosesWithEscape() {
        val terminal = DefaultVirtualTerminal(TerminalSize(80, 24))
        val screen = TerminalScreen(terminal)
        screen.startScreen()

        try {
            val windowManager = DefaultWindowManager(EmptyWindowDecorationRenderer(), screen.terminalSize)
            val textGUI = MultiWindowTextGUI(SameTextGUIThread.Factory(), screen, windowManager, null, EmptySpace())
            val window = BasicWindow("Input test")
            window.setHints(
                listOf(
                    Window.Hint.NO_DECORATIONS,
                    Window.Hint.FIT_TERMINAL_WINDOW,
                    Window.Hint.FULL_SCREEN,
                ),
            )
            window.setCloseWindowWithEscape(true)

            val interactable = InputCaptureComponent()
            interactable.withBorder(Borders.doubleLineBevel("Press any key to test capturing the KeyStroke"))
            window.component =
                Panels.vertical(
                    interactable,
                    Label("Use the TAB key to shift focus"),
                    Button("Close", Runnable { window.close() }),
                )
            textGUI.addWindow(window)

            pump(textGUI, 8)

            terminal.addInput(KeyStroke('x', false, false))
            pump(textGUI, 20)
            val renderedAfterX = dumpScreen(screen)
            assertTrue(renderedAfterX.contains("x"), "expected captured character in rendered screen")

            terminal.addInput(KeyStroke(KeyType.ESCAPE))
            pump(textGUI, 20)
            assertTrue(textGUI.windows.isEmpty(), "window should close on escape")
        } finally {
            screen.stopScreen()
        }
    }

    private fun pump(
        textGUI: MultiWindowTextGUI,
        iterations: Int,
    ) {
        val guiThread = requireNotNull(textGUI.guiThread)
        repeat(iterations) {
            guiThread.processEventsAndUpdate()
        }
    }

    private fun dumpScreen(screen: Screen): String {
        val size = screen.terminalSize ?: return ""
        val out = StringBuilder(size.rows * (size.columns + 1))
        for (row in 0 until size.rows) {
            for (col in 0 until size.columns) {
                out.append(screen.getBackCharacter(col, row)?.characterString ?: " ")
            }
            out.append('\n')
        }
        return out.toString()
    }

    private class InputCaptureComponent : AbstractInteractableComponent<InputCaptureComponent>() {
        private var lastKey: String? = null

        override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
            if (keyStroke.keyType == KeyType.TAB || keyStroke.keyType == KeyType.ESCAPE) {
                return super.handleKeyStroke(keyStroke)
            }
            lastKey =
                if (keyStroke.keyType == KeyType.CHARACTER) {
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

                override fun drawComponent(
                    graphics: TextGUIGraphics?,
                    component: InputCaptureComponent?,
                ) {
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
}
