package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.googlecode.lanterna.terminal.ansi.UnixTerminal

class DefaultTerminalFactory : TerminalFactory {
    private var initialTerminalSize: TerminalSize? = null
    private var forceTextTerminal: Boolean = false
    private var preferTerminalEmulator: Boolean = false
    private var telnetPort: Int = -1
    private var inputTimeout: Int = -1
    private var forceAWTOverSwing: Boolean = false
    private var autoOpenTerminalEmulatorWindow: Boolean = true
    private var terminalEmulatorTitle: String? = null
    private var mouseCaptureMode: MouseCaptureMode? = null
    private var unixTerminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour =
        UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION

    @Throws(IOException::class)
    override fun createTerminal(): Terminal {
        if (telnetPort > 0) {
            throw IOException("Telnet terminal is not supported on native targets")
        }
        if (preferTerminalEmulator && !forceTextTerminal) {
            throw IOException("Terminal emulator windows are not supported on native targets")
        }
        val unixTerminal = UnixTerminal(unixTerminalCtrlCBehaviour)
        if (terminalEmulatorTitle != null) {
            unixTerminal.setTitle(terminalEmulatorTitle)
        }
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
        this.forceTextTerminal = forceTextTerminal
        return this
    }

    fun setPreferTerminalEmulator(preferTerminalEmulator: Boolean): DefaultTerminalFactory {
        this.preferTerminalEmulator = preferTerminalEmulator
        return this
    }

    fun setUnixTerminalCtrlCBehaviour(unixTerminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour): DefaultTerminalFactory {
        this.unixTerminalCtrlCBehaviour = unixTerminalCtrlCBehaviour
        return this
    }

    fun setTelnetPort(telnetPort: Int): DefaultTerminalFactory {
        this.telnetPort = telnetPort
        return this
    }

    fun setInputTimeout(inputTimeout: Int): DefaultTerminalFactory {
        this.inputTimeout = inputTimeout
        return this
    }

    fun setForceAWTOverSwing(forceAWTOverSwing: Boolean): DefaultTerminalFactory {
        this.forceAWTOverSwing = forceAWTOverSwing
        return this
    }

    fun setAutoOpenTerminalEmulatorWindow(autoOpenTerminalEmulatorWindow: Boolean): DefaultTerminalFactory {
        this.autoOpenTerminalEmulatorWindow = autoOpenTerminalEmulatorWindow
        return this
    }

    fun setTerminalEmulatorTitle(terminalEmulatorTitle: String?): DefaultTerminalFactory {
        this.terminalEmulatorTitle = terminalEmulatorTitle
        return this
    }

    fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?): DefaultTerminalFactory {
        this.mouseCaptureMode = mouseCaptureMode
        return this
    }
}
