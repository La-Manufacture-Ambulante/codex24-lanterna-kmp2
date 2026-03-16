package com.alaeri

import com.alaeri.nativebrowser.KeyInputReproApp
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

fun keyInputReproMain() {
    val terminal =
        runCatching { DefaultTerminalFactory().createTerminal() }
            .getOrNull()
            ?: run {
                println("KeyInputRepro requires a TTY-backed terminal (/dev/tty).")
                return
            }
    val screen = TerminalScreen(terminal)
    try {
        KeyInputReproApp(screen).run()
    } finally {
        terminal.close()
    }
}
