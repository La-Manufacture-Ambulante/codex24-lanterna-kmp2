package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.internal.io.IOException
import kotlin.system.exitProcess

open class UnixLikeTerminal(
    private val terminalCtrlCBehaviour: CtrlCBehaviour = CtrlCBehaviour.CTRL_C_KILLS_APPLICATION,
) : ANSITerminal() {
    enum class CtrlCBehaviour {
        TRAP,
        CTRL_C_KILLS_APPLICATION,
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        val keyStroke = super.pollInput()
        handleCtrlC(keyStroke)
        return keyStroke
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke? {
        val keyStroke = super.readInput()
        handleCtrlC(keyStroke)
        return keyStroke
    }

    @Throws(IOException::class)
    private fun handleCtrlC(keyStroke: KeyStroke?) {
        if (
            keyStroke != null &&
            terminalCtrlCBehaviour == CtrlCBehaviour.CTRL_C_KILLS_APPLICATION &&
            keyStroke.character == 'c' &&
            keyStroke.isCtrlDown &&
            !keyStroke.isAltDown
        ) {
            if (isInPrivateMode()) {
                exitPrivateMode()
            }
            exitProcess(1)
        }
    }
}
