package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException
import java.util.Collections

object Issue380 {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val screen = DefaultTerminalFactory().createScreen()
        screen!!.startScreen()
        val gui = MultiWindowTextGUI(screen)
        val window = GridWindowWithTwoLargeComponents()
        window.setHints(Collections.singletonList(Window.Hint.EXPANDED))
        gui.addWindow(window)
        gui.waitForWindowToClose(window)
        screen!!.stopScreen()
    }

    private class GridWindowWithTwoLargeComponents internal constructor() : AbstractWindow() {
        init {
            // two column grid
            val p = Panel(GridLayout(2))

            // spanning component in the first row
            p.addComponent(
                Label("My dummy label"),
                GridLayout.createLayoutData(
                    GridLayout.Alignment.FILL,
                    GridLayout.Alignment.BEGINNING,
                    true,
                    false,
                    2,
                    1,
                ),
            )

            // col 1, row 2
            p.addComponent(
                TextBox(),
                GridLayout.createLayoutData(
                    GridLayout.Alignment.FILL,
                    GridLayout.Alignment.FILL,
                    true,
                    true,
                ),
            )
            // col 2, row 2
            p.addComponent(
                this.buildButtonPanel(),
                GridLayout.createLayoutData(
                    GridLayout.Alignment.BEGINNING,
                    GridLayout.Alignment.BEGINNING,
                    false,
                    false,
                ),
            )

            // spanning component in row 3
            p.addComponent(
                this.buildButtonBar(),
                GridLayout.createLayoutData(
                    GridLayout.Alignment.CENTER,
                    GridLayout.Alignment.BEGINNING,
                    false,
                    false,
                    2,
                    1,
                ),
            )
            component = p
        }

        private fun buildButtonPanel(): Component {
            val panel = Panel(LinearLayout(Direction.VERTICAL))
            panel.addComponent(Button("One"))
            panel.addComponent(Button("Two"))
            panel.addComponent(Button("Three"))
            return panel
        }

        private fun buildButtonBar(): Component {
            return Button("Close", Runnable { this.close() })
        }
    }
}
