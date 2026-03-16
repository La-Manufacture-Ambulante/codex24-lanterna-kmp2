package com.alaeri

import com.alaeri.nativebrowser.KeyInputReproApp
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import platform.posix.O_RDONLY
import platform.posix.STDIN_FILENO
import platform.posix.close
import platform.posix.dup2
import platform.posix.open

fun keyInputReproMain() {
    bindStdinToTtyIfAvailable()
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

private fun bindStdinToTtyIfAvailable() {
    val ttyFd = open("/dev/tty", O_RDONLY)
    if (ttyFd < 0) {
        return
    }
    try {
        dup2(ttyFd, STDIN_FILENO)
    } finally {
        close(ttyFd)
    }
}
