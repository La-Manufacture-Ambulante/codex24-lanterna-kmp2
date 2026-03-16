package com.alaeri

import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import platform.posix.O_RDONLY
import platform.posix.STDIN_FILENO
import platform.posix.close
import platform.posix.dup2
import platform.posix.open
import kotlin.test.Test

fun keyInputReproMain() {
    val terminal = DefaultTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)
    val history = ArrayDeque<String>()

    try {
        screen.startScreen()
        screen.cursorPosition = null
        var running = true
        while (running) {
            val key = screen.readInput()
            val line =
                if (key == null) {
                    "null key"
                } else {
                    "keyType=${key.keyType} char=${key.character ?: '-'} raw=$key"
                }
            history.addLast(line)
            while (history.size > 8) history.removeFirst()

            val tg = screen.newTextGraphics()
            tg.putString(0, 0, "Lanterna KMP key-input repro (Esc to exit)")
            tg.putString(0, 1, "Type characters and check if char changes.")
            history.forEachIndexed { index, value ->
                tg.putString(0, 3 + index, value.padEnd(120))
            }
            screen.refresh()

            if (key == null || key.keyType == KeyType.EOF || key.keyType == KeyType.ESCAPE) {
                running = false
            }
        }
    } finally {
        screen.stopScreen()
        terminal.close()
    }

    println("Key-input repro finished.")
    history.forEach { println(it) }
}

fun keyInputReproTtyBackedStdinMain() {
    val ttyFd = open("/dev/tty", O_RDONLY)
    if (ttyFd < 0) {
        println("Skipping tty-backed repro: /dev/tty is unavailable.")
        return
    }
    try {
        if (dup2(ttyFd, STDIN_FILENO) < 0) {
            println("Skipping tty-backed repro: failed to bind /dev/tty to stdin.")
            return
        }
        keyInputReproMain()
    } finally {
        close(ttyFd)
    }
}

class KeyInputReproNativeLinuxTest {
    @Test
    fun keyInputRepro() {
        keyInputReproMain()
    }

    @Test
    fun keyInputReproTtyBackedStdin() {
        keyInputReproTtyBackedStdinMain()
    }
}
