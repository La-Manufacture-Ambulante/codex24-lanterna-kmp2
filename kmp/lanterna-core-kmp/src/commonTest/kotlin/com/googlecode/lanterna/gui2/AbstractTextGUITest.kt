package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.NullTextGraphics
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TabBehaviour
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AbstractTextGUITest {
    @Test
    fun assigningThemeMarksGuiPendingUpdate() {
        val gui = TestTextGUI()

        assertFalse(gui.isPendingUpdate)

        gui.theme = SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK)

        assertTrue(gui.isPendingUpdate)
    }
}

private class TestTextGUI : AbstractTextGUI(TestTextGUIThreadFactory(), FakeScreen()) {
    override fun drawGUI(graphics: TextGUIGraphics?) = Unit

    override val cursorPosition: TerminalPosition? = null

    override val focusedInteractable: Interactable? = null

    override fun handleInput(key: KeyStroke?): Boolean = false

    override fun setVirtualScreenEnabled(virtualScreenEnabled: Boolean) = Unit
}

private class TestTextGUIThreadFactory : TextGUIThreadFactory {
    override fun createTextGUIThread(textGUI: TextGUI?): TextGUIThread = TestTextGUIThread()
}

private class TestTextGUIThread : TextGUIThread {
    override val ownerThreadToken = null

    override fun invokeLater(task: GuiTask?) = task?.invoke() ?: Unit

    override fun processEventsAndUpdate(): Boolean = false

    override fun invokeAndWait(task: GuiTask?) = task?.invoke() ?: Unit

    override fun setExceptionHandler(exceptionHandler: TextGUIThread.ExceptionHandler?) = Unit
}

private class FakeScreen : Screen {
    override var cursorPosition: TerminalPosition? = null
    override var tabBehaviour: TabBehaviour? = TabBehaviour.ALIGN_TO_COLUMN_4
    override val terminalSize: TerminalSize = TerminalSize(80, 24)

    override fun startScreen() = Unit

    override fun close() = Unit

    override fun stopScreen() = Unit

    override fun clear() = Unit

    override fun setCharacter(
        column: Int,
        row: Int,
        screenCharacter: TextCharacter?,
    ) = Unit

    override fun setCharacter(
        position: TerminalPosition?,
        screenCharacter: TextCharacter?,
    ) = Unit

    override fun newTextGraphics(): TextGraphics = NullTextGraphics(terminalSize)

    override fun getFrontCharacter(
        column: Int,
        row: Int,
    ): TextCharacter = Screen.DEFAULT_CHARACTER

    override fun getFrontCharacter(position: TerminalPosition?): TextCharacter = Screen.DEFAULT_CHARACTER

    override fun getBackCharacter(
        column: Int,
        row: Int,
    ): TextCharacter = Screen.DEFAULT_CHARACTER

    override fun getBackCharacter(position: TerminalPosition?): TextCharacter = Screen.DEFAULT_CHARACTER

    override fun refresh() = Unit

    override fun refresh(refreshType: Screen.RefreshType?) = Unit

    override fun doResizeIfNecessary(): TerminalSize? = null

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) = Unit

    override fun pollInput(): KeyStroke? = null

    override fun readInput(): KeyStroke? = null
}
