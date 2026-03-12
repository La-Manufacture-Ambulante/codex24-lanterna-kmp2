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
package com.googlecode.lanterna.terminal.ansi

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.reflect.Proxy
import java.nio.charset.Charset

/**
 * UnixLikeTerminal extends from ANSITerminal and defines functionality that is common to `UnixTerminal` and
 * `CygwinTerminal`, like setting tty modes; echo, cbreak and minimum characters for reading as well as a shutdown
 * hook to set the tty back to original state at the end.
 *
 * If requested, it handles Control-C input to terminate the program, and hooks into Unix WINCH signal to detect
 * when the user has resized the terminal, if supported by the JVM.
 *
 * @author Andreas
 * @author Martin
 */
abstract class UnixLikeTTYTerminal
    @Throws(IOException::class)
    protected constructor(
        private val ttyDev: File?,
        terminalInput: InputStream?,
        terminalOutput: OutputStream?,
        terminalCharset: Charset?,
        terminalCtrlCBehaviour: CtrlCBehaviour,
    ) : UnixLikeTerminal(terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {
        private var sttyStatusToRestore: String? = null

        init {
            realAcquire()
        }

        @Throws(IOException::class)
        override fun acquire() {
        }

        @Throws(IOException::class)
        private fun realAcquire() {
            super.acquire()
        }

        @Throws(IOException::class)
        override fun registerTerminalResizeListener(onResize: Runnable) {
            try {
                val signalClass = Class.forName("sun.misc.Signal")
                for (method in signalClass.declaredMethods) {
                    if (method.name == "handle") {
                        val windowResizeHandler =
                            Proxy.newProxyInstance(
                                javaClass.classLoader,
                                arrayOf(Class.forName("sun.misc.SignalHandler")),
                            ) { _, invokedMethod, _ ->
                                if (invokedMethod.name == "handle") {
                                    onResize.run()
                                }
                                null
                            }
                        method.invoke(null, signalClass.getConstructor(String::class.java).newInstance("WINCH"), windowResizeHandler)
                    }
                }
            } catch (_: Throwable) {
            }
        }

        @Throws(IOException::class)
        override fun saveTerminalSettings() {
            sttyStatusToRestore = runSTTYCommand("-g").trim()
        }

        @Throws(IOException::class)
        override fun restoreTerminalSettings() {
            if (sttyStatusToRestore != null) {
                runSTTYCommand(sttyStatusToRestore!!)
            }
        }

        @Throws(IOException::class)
        override fun keyEchoEnabled(enabled: Boolean) {
            runSTTYCommand(if (enabled) "echo" else "-echo")
        }

        @Throws(IOException::class)
        override fun canonicalMode(enabled: Boolean) {
            runSTTYCommand(if (enabled) "icanon" else "-icanon")
            if (!enabled) {
                runSTTYCommand("min", "1")
            }
        }

        @Throws(IOException::class)
        override fun keyStrokeSignalsEnabled(enabled: Boolean) {
            if (enabled) {
                runSTTYCommand("intr", "^C")
            } else {
                runSTTYCommand("intr", "undef")
            }
        }

        @Throws(IOException::class)
        protected open fun runSTTYCommand(vararg parameters: String): String {
            val commandLine = getSTTYCommand().toMutableList()
            commandLine.addAll(parameters)
            return exec(*commandLine.toTypedArray())
        }

        @Throws(IOException::class)
        protected fun exec(vararg cmd: String): String {
            val processBuilder = ProcessBuilder(*cmd)
            if (ttyDev != null) {
                processBuilder.redirectInput(ProcessBuilder.Redirect.from(ttyDev))
            }
            val process = processBuilder.start()
            val stdoutBuffer = ByteArrayOutputStream()
            val stdout = process.inputStream
            var readByte = stdout.read()
            while (readByte >= 0) {
                stdoutBuffer.write(readByte)
                readByte = stdout.read()
            }
            val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(stdoutBuffer.toByteArray())))
            val builder = StringBuilder()
            var line = reader.readLine()
            while (line != null) {
                builder.append(line)
                line = reader.readLine()
            }
            reader.close()
            return builder.toString()
        }

        protected open fun getSTTYCommand(): Array<String> {
            val sttyOverride = System.getProperty("com.googlecode.lanterna.terminal.UnixTerminal.sttyCommand")
            return if (sttyOverride != null) {
                arrayOf(sttyOverride)
            } else {
                arrayOf("/usr/bin/env", "stty")
            }
        }
    }
