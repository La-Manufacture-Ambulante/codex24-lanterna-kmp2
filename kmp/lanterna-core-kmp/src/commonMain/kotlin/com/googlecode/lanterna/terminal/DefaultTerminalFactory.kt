/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
@file:Suppress("ktlint:standard:max-line-length")

package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.CygwinTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.googlecode.lanterna.terminal.ansi.UnixTerminal
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorColorConfiguration
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorDeviceConfiguration
import java.io.Console
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.nio.charset.Charset
import java.util.EnumSet

class DefaultTerminalFactory
    @Suppress("SameParameterValue", "WeakerAccess")
    constructor(
        private val outputStream: OutputStream,
        private val inputStream: InputStream,
        private val charset: Charset,
    ) : TerminalFactory {
        private var initialTerminalSize: TerminalSize? = null
        private var forceTextTerminal = false
        private var preferTerminalEmulator = false
        private var forceAWTOverSwing = false
        private var telnetPort = -1
        private var inputTimeout = -1
        private var title: String? = null
        private var autoOpenTerminalFrame = true
        private val autoCloseTriggers = EnumSet.of(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE)
        private var colorConfiguration: TerminalEmulatorColorConfiguration? = null
        private var deviceConfiguration: TerminalEmulatorDeviceConfiguration? = null
        private var fontConfiguration: AWTTerminalFontConfiguration? = null
        private var mouseCaptureMode: MouseCaptureMode? = null
        private var unixTerminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour =
            UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION

        constructor() : this(DEFAULT_OUTPUT_STREAM, DEFAULT_INPUT_STREAM, DEFAULT_CHARSET)

        @Throws(IOException::class)
        override fun createTerminal(): Terminal? {
            return if (forceTextTerminal || isAwtHeadless() || (hasTerminal() && !preferTerminalEmulator)) {
                createHeadlessTerminal()
            } else if (!preferTerminalEmulator && mouseCaptureMode != null && telnetPort > 0) {
                createTelnetTerminal()
            } else {
                createTerminalEmulator()
            }
        }

        @Throws(IOException::class)
        fun createHeadlessTerminal(): Terminal? {
            if (telnetPort > 0 && System.console() == null) {
                return createTelnetTerminal()
            }
            if (isOperatingSystemWindows()) {
                return createWindowsTerminal()
            }
            return createUnixTerminal(outputStream, inputStream, charset)
        }

        fun createTerminalEmulator(): Terminal {
            val terminal: Terminal =
                if (!forceAWTOverSwing && hasSwing()) {
                    val swingTerminalFrame = createSwingTerminal()
                    if (mouseCaptureMode != null) {
                        swingTerminalFrame.swingTerminal?.setMouseCaptureMode(mouseCaptureMode)
                    }
                    swingTerminalFrame
                } else {
                    val awtTerminalFrame = createAWTTerminal()
                    if (mouseCaptureMode != null) {
                        awtTerminalFrame.awtTerminal?.setMouseCaptureMode(mouseCaptureMode)
                    }
                    awtTerminalFrame
                }
            if (autoOpenTerminalFrame) {
                makeWindowVisible(terminal)
            }
            return terminal
        }

        fun createAWTTerminal(): AWTTerminalFrame {
            return AWTTerminalFrame(
                title,
                initialTerminalSize,
                deviceConfiguration,
                fontConfiguration,
                colorConfiguration,
                *autoCloseTriggers.toTypedArray(),
            )
        }

        fun createSwingTerminal(): SwingTerminalFrame {
            return SwingTerminalFrame(
                title,
                initialTerminalSize,
                deviceConfiguration,
                fontConfiguration as? SwingTerminalFontConfiguration,
                colorConfiguration,
                *autoCloseTriggers.toTypedArray(),
            )
        }

        fun createTelnetTerminal(): TelnetTerminal {
            try {
                System.err.print("Waiting for incoming telnet connection on port $telnetPort ... ")
                System.err.flush()

                val telnetTerminalServer = TelnetTerminalServer(telnetPort)
                val rawTerminal = requireNotNull(telnetTerminalServer.acceptConnection())
                telnetTerminalServer.close()

                System.err.println("Ok, got it!")

                if (mouseCaptureMode != null) {
                    rawTerminal.setMouseCaptureMode(mouseCaptureMode)
                }
                if (inputTimeout >= 0) {
                    rawTerminal.inputDecoder.setTimeoutUnits(inputTimeout)
                }
                return rawTerminal
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
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

        fun setAutoOpenTerminalEmulatorWindow(autoOpenTerminalFrame: Boolean): DefaultTerminalFactory {
            this.autoOpenTerminalFrame = autoOpenTerminalFrame
            return this
        }

        fun setTerminalEmulatorTitle(title: String?): DefaultTerminalFactory {
            this.title = title
            return this
        }

        fun setTerminalEmulatorFrameAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): DefaultTerminalFactory {
            autoCloseTriggers.clear()
            if (autoCloseTrigger != null) {
                autoCloseTriggers.add(autoCloseTrigger)
            }
            return this
        }

        fun addTerminalEmulatorFrameAutoCloseTrigger(autoCloseTrigger: TerminalEmulatorAutoCloseTrigger?): DefaultTerminalFactory {
            if (autoCloseTrigger != null) {
                autoCloseTriggers.add(autoCloseTrigger)
            }
            return this
        }

        fun setTerminalEmulatorColorConfiguration(colorConfiguration: TerminalEmulatorColorConfiguration?): DefaultTerminalFactory {
            this.colorConfiguration = colorConfiguration
            return this
        }

        fun setTerminalEmulatorDeviceConfiguration(deviceConfiguration: TerminalEmulatorDeviceConfiguration?): DefaultTerminalFactory {
            this.deviceConfiguration = deviceConfiguration
            return this
        }

        fun setTerminalEmulatorFontConfiguration(fontConfiguration: AWTTerminalFontConfiguration?): DefaultTerminalFactory {
            this.fontConfiguration = fontConfiguration
            return this
        }

        fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?): DefaultTerminalFactory {
            this.mouseCaptureMode = mouseCaptureMode
            return this
        }

        @Throws(IOException::class)
        fun createScreen(): TerminalScreen {
            return TerminalScreen(requireNotNull(createTerminal()))
        }

        private fun isAwtHeadless(): Boolean {
            return try {
                val cls = Class.forName("java.awt.GraphicsEnvironment")
                val method = cls.getDeclaredMethod("isHeadless")
                method.invoke(null) as Boolean
            } catch (_: Exception) {
                true
            }
        }

        private fun hasSwing(): Boolean {
            return try {
                Class.forName("javax.swing.JComponent")
                true
            } catch (_: Exception) {
                false
            }
        }

        private fun makeWindowVisible(terminal: Terminal) {
            try {
                val cls = Class.forName("java.awt.Window")
                val method = cls.getDeclaredMethod("setVisible", Boolean::class.javaPrimitiveType)
                method.invoke(terminal, true)
            } catch (e: Exception) {
                throw RuntimeException("Failed to make terminal emulator window visible.", e)
            }
        }

        @Throws(IOException::class)
        private fun createWindowsTerminal(): Terminal {
            try {
                val nativeImplementation = Class.forName("com.googlecode.lanterna.terminal.win32.WindowsTerminal")
                val constructor: Constructor<*> =
                    nativeImplementation.getConstructor(
                        InputStream::class.java,
                        OutputStream::class.java,
                        Charset::class.java,
                        UnixLikeTerminal.CtrlCBehaviour::class.java,
                    )
                return constructor.newInstance(
                    inputStream,
                    outputStream,
                    charset,
                    UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION,
                ) as Terminal
            } catch (_: Exception) {
                try {
                    return createCygwinTerminal(outputStream, inputStream, charset)
                } catch (e: IOException) {
                    throw IOException(
                        "To use Lanterna on Windows, either add JNA (and jna-platform) to the classpath or use javaw! (see https://github.com/mabe02/lanterna/issues/335)",
                        e,
                    )
                }
            } catch (_: NoClassDefFoundError) {
                try {
                    return createCygwinTerminal(outputStream, inputStream, charset)
                } catch (e: IOException) {
                    throw IOException(
                        "To use Lanterna on Windows, either add JNA (and jna-platform) to the classpath or use javaw! (see https://github.com/mabe02/lanterna/issues/335)",
                        e,
                    )
                }
            }
        }

        @Throws(IOException::class)
        private fun createCygwinTerminal(
            outputStream: OutputStream,
            inputStream: InputStream,
            charset: Charset,
        ): Terminal {
            val cygwinTerminal = CygwinTerminal(inputStream, outputStream, charset)
            if (inputTimeout >= 0) {
                cygwinTerminal.inputDecoder.setTimeoutUnits(inputTimeout)
            }
            return cygwinTerminal
        }

        @Throws(IOException::class)
        private fun createUnixTerminal(
            outputStream: OutputStream,
            inputStream: InputStream,
            charset: Charset,
        ): Terminal {
            val unixTerminal: UnixTerminal =
                try {
                    val nativeImplementation = Class.forName("com.googlecode.lanterna.terminal.NativeGNULinuxTerminal")
                    val constructor: Constructor<*> =
                        nativeImplementation.getConstructor(
                            InputStream::class.java,
                            OutputStream::class.java,
                            Charset::class.java,
                            UnixLikeTerminal.CtrlCBehaviour::class.java,
                        )
                    constructor.newInstance(inputStream, outputStream, charset, unixTerminalCtrlCBehaviour) as UnixTerminal
                } catch (_: Exception) {
                    UnixTerminal(inputStream, outputStream, charset, unixTerminalCtrlCBehaviour)
                }
            if (mouseCaptureMode != null) {
                unixTerminal.setMouseCaptureMode(mouseCaptureMode)
            }
            if (inputTimeout >= 0) {
                unixTerminal.inputDecoder.setTimeoutUnits(inputTimeout)
            }
            return unixTerminal
        }

        companion object {
            private val DEFAULT_OUTPUT_STREAM: OutputStream = System.out
            private val DEFAULT_INPUT_STREAM: InputStream = System.`in`
            private val DEFAULT_CHARSET: Charset = Charset.defaultCharset()

            private fun isOperatingSystemWindows(): Boolean {
                return System.getProperty("os.name", "").lowercase().startsWith("windows")
            }

            private fun hasTerminal(): Boolean {
                val console = System.console()
                return console != null && isTerminalCheckJDK22(console)
            }

            private fun isTerminalCheckJDK22(console: Console): Boolean {
                return try {
                    val isTerminal = Console::class.java.getMethod("isTerminal")
                    isTerminal.invoke(console) as Boolean
                } catch (_: NoSuchMethodException) {
                    true
                } catch (_: InvocationTargetException) {
                    true
                } catch (_: IllegalAccessException) {
                    true
                }
            }
        }
    }
