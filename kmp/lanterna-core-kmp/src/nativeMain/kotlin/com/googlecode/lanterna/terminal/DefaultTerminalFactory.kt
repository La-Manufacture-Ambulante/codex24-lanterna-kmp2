package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.googlecode.lanterna.terminal.ansi.UnixTerminal

class DefaultTerminalFactory : TerminalFactory {
    private var initialTerminalSize: TerminalSize? = null
    private var inputTimeout: Int = -1
    private var mouseCaptureMode: MouseCaptureMode? = null
    private var unixTerminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour =
        UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION

    @Throws(IOException::class)
    override fun createTerminal(): Terminal {
        val unixTerminal = UnixTerminal(unixTerminalCtrlCBehaviour)
        if (initialTerminalSize != null) {
            unixTerminal.setTerminalSize(initialTerminalSize!!.columns, initialTerminalSize!!.rows)
        }
        if (mouseCaptureMode != null) {
            unixTerminal.setMouseCaptureMode(mouseCaptureMode)
        }
        if (inputTimeout >= 0) {
            unixTerminal.setInputTimeoutUnits(inputTimeout)
        }
        return unixTerminal
    }

    @Throws(IOException::class)
    fun createHeadlessTerminal(): Terminal {
        return createTerminal()
    }

    @Throws(IOException::class)
    fun createScreen(): TerminalScreen {
        return TerminalScreen(createTerminal())
    }

    fun setInitialTerminalSize(initialTerminalSize: TerminalSize?): DefaultTerminalFactory {
        this.initialTerminalSize = initialTerminalSize
        return this
    }

    fun setForceTextTerminal(forceTextTerminal: Boolean): DefaultTerminalFactory {
        return this
    }

    fun setPreferTerminalEmulator(preferTerminalEmulator: Boolean): DefaultTerminalFactory {
        return this
    }

    fun setUnixTerminalCtrlCBehaviour(unixTerminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour): DefaultTerminalFactory {
        this.unixTerminalCtrlCBehaviour = unixTerminalCtrlCBehaviour
        return this
    }

    fun setTelnetPort(telnetPort: Int): DefaultTerminalFactory {
        return this
    }

    fun setInputTimeout(inputTimeout: Int): DefaultTerminalFactory {
        this.inputTimeout = inputTimeout
        return this
    }

    fun setForceAWTOverSwing(forceAWTOverSwing: Boolean): DefaultTerminalFactory {
        return this
    }

    fun setAutoOpenTerminalEmulatorWindow(autoOpenTerminalEmulatorWindow: Boolean): DefaultTerminalFactory {
        return this
    }

    fun setTerminalEmulatorTitle(terminalEmulatorTitle: String?): DefaultTerminalFactory {
        return this
    }

    fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?): DefaultTerminalFactory {
        this.mouseCaptureMode = mouseCaptureMode
        return this
    }
}
