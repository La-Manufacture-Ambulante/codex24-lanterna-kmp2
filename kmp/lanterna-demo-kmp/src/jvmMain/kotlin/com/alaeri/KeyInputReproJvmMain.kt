package com.alaeri

import com.alaeri.nativebrowser.KeyInputReproApp
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

fun main() {
    val terminal = requireNotNull(DefaultTerminalFactory().createTerminal()) {
        "DefaultTerminalFactory returned null terminal"
    }
    val screen = TerminalScreen(terminal)
    try {
        KeyInputReproApp(screen).run()
    } finally {
        terminal.close()
    }
}
