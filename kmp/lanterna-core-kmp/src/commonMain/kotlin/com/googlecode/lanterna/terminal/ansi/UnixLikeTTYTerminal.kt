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
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Arrays

abstract class UnixLikeTTYTerminal
@Throws(IOException::class)
protected constructor(
    private val ttyDev: File?,
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
    terminalCtrlCBehaviour: CtrlCBehaviour?
) : UnixLikeTerminal(
    terminalInput,
    terminalOutput,
    terminalCharset,
    terminalCtrlCBehaviour
) {

    private var sttyStatusToRestore: String? = null

    init {
        // Take ownership of the terminal
        realAcquire()
    }

    @Throws(IOException::class)
    protected open override fun acquire() {
        // Hack!
    }

    @Throws(IOException::class)
    private fun realAcquire() {
        super.acquire()
    }

    @Throws(IOException::class)
    protected open override fun registerTerminalResizeListener(onResize: Runnable?) {
        try {
            val signalClass = Class.forName("sun.misc.Signal")
            for (m: Method in signalClass.declaredMethods) {
                if ("handle" == m.name) {
                    val windowResizeHandler = Proxy.newProxyInstance(
                        javaClass.classLoader,
                        arrayOf(Class.forName("sun.misc.SignalHandler"))
                    ) { _, method, _ ->
                        if ("handle" == method.name) {
                            java.util.Objects.requireNonNull(onResize).run()
                        }
                        null
                    }
                    m.invoke(
                        null,
                        signalClass.getConstructor(String::class.java).newInstance("WINCH"),
                        windowResizeHandler
                    )
                }
            }
        } catch (_: Throwable) {
            // We're probably running on a non-Sun JVM and there's no way to catch signals without resorting to native
            // code integration
        }
    }

    @Throws(IOException::class)
    protected open override fun saveTerminalSettings() {
        sttyStatusToRestore = runSTTYCommand(arrayOf("-g")).trim()
    }

    @Throws(IOException::class)
    protected open override fun restoreTerminalSettings() {
        if (sttyStatusToRestore != null) {
            runSTTYCommand(arrayOf(sttyStatusToRestore))
        }
    }

    @Throws(IOException::class)
    protected open override fun keyEchoEnabled(enabled: Boolean) {
        runSTTYCommand(arrayOf(if (enabled) "echo" else "-echo"))
    }

    @Throws(IOException::class)
    protected open override fun canonicalMode(enabled: Boolean) {
        runSTTYCommand(arrayOf(if (enabled) "icanon" else "-icanon"))
        if (!enabled) {
            runSTTYCommand(arrayOf("min", "1"))
        }
    }

    @Throws(IOException::class)
    protected open override fun keyStrokeSignalsEnabled(enabled: Boolean) {
        if (enabled) {
            runSTTYCommand(arrayOf("intr", "^C"))
        } else {
            runSTTYCommand(arrayOf("intr", "undef"))
        }
    }

    @Throws(IOException::class)
    protected open fun runSTTYCommand(parameters: Array<out String?>?): String {
        val commandLine = ArrayList<String?>(Arrays.asList(*getSTTYCommand()))
        commandLine.addAll(Arrays.asList(*java.util.Objects.requireNonNull(parameters)))
        return exec(commandLine.toTypedArray())
    }

    @Throws(IOException::class)
    protected open fun exec(cmd: Array<out String?>?): String {
        @Suppress("UNCHECKED_CAST")
        val nonNullCmd = java.util.Objects.requireNonNull(cmd) as Array<String>
        val pb = ProcessBuilder(*nonNullCmd)
        if (ttyDev != null) {
            pb.redirectInput(ProcessBuilder.Redirect.from(ttyDev))
        }
        val process = pb.start()
        val stdoutBuffer = ByteArrayOutputStream()
        val stdout = process.inputStream
        var readByte = stdout.read()
        while (readByte >= 0) {
            stdoutBuffer.write(readByte)
            readByte = stdout.read()
        }
        val stdoutBufferInputStream = ByteArrayInputStream(stdoutBuffer.toByteArray())
        val reader = BufferedReader(InputStreamReader(stdoutBufferInputStream))
        val builder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
        }
        reader.close()
        return builder.toString()
    }

    protected open fun getSTTYCommand(): Array<String> {
        val sttyOverride =
            System.getProperty("com.googlecode.lanterna.terminal.UnixTerminal.sttyCommand")
        return if (sttyOverride != null) {
            arrayOf(sttyOverride)
        } else {
            // Issue #519: this will hopefully be more portable across linux distributions
            // Previously we hard-coded "/bin/stty" here
            arrayOf(
                "/usr/bin/env",
                "stty"
            )
        }
    }
}
