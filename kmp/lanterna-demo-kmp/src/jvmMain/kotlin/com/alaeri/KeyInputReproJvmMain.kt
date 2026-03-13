package com.alaeri

import com.alaeri.nativebrowser.KeyInputReproApp
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

fun main() {
    val terminal =
        runCatching { DefaultTerminalFactory().createTerminal() }
            .getOrNull()
            ?: run {
                System.err.println("KeyInputRepro requires a TTY-backed terminal (/dev/tty).")
                return
            }
    val screen = TerminalScreen(terminal)
    try {
        KeyInputReproApp(screen).run()
    } finally {
        terminal.close()
    }
}
