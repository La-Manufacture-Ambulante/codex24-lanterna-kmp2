package com.alaeri.nativebrowser

import com.googlecode.lanterna.screen.Screen

class KeyInputReproApp(private val screen: Screen) {
    private val history = ArrayDeque<String>()

    fun run() {
        try {
            screen.startScreen()
            screen.cursorPosition = null
            var running = true
            while (running) {
                val key = screen.readInput()
                val keyTypeName = key?.keyType?.toString() ?: "null"
                val line =
                    if (key == null) {
                        "null key"
                    } else {
                        "keyType=$keyTypeName char=${key.character ?: '-'} raw=$key"
                    }
                history.addLast(line)
                if (history.size == 1 && keyTypeName.uppercase() == "EOF") {
                    history.addLast("hint: immediate EOF usually means stdin is not tty-backed.")
                }
                while (history.size > 8) history.removeFirst()

                val tg = screen.newTextGraphics()
                tg?.putString(0, 0, "Lanterna key-input repro (Esc/EOF to exit)")
                tg?.putString(0, 1, "Type characters then Enter.")
                history.forEachIndexed { index, value ->
                    tg?.putString(0, 3 + index, value.padEnd(120))
                }
                screen.refresh()

                if (key == null || isExitKeyName(keyTypeName)) {
                    running = false
                }
            }
        } finally {
            screen.stopScreen()
        }

        println("Key-input repro finished.")
        history.forEach { println(it) }
    }

    private fun isExitKeyName(name: String): Boolean {
        val normalized = name.uppercase().replace("_", "").replace(" ", "")
        return normalized == "EOF" || normalized == "ESCAPE"
    }
}
