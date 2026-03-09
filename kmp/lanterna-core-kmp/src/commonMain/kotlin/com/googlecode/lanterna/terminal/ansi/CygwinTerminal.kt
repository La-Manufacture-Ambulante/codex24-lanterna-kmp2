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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.TerminalSize
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.regex.Pattern

class CygwinTerminal @Throws(IOException::class) constructor(
    terminalInput: InputStream,
    terminalOutput: OutputStream,
    terminalCharset: Charset,
) : UnixLikeTTYTerminal(null, terminalInput, terminalOutput, terminalCharset, CtrlCBehaviour.TRAP) {
    override fun findTerminalSize(): TerminalSize {
        return try {
            val stty = runSTTYCommand("-a")
            val matcher = STTY_SIZE_PATTERN.matcher(stty)
            if (matcher.matches()) {
                TerminalSize(matcher.group(2).toInt(), matcher.group(1).toInt())
            } else {
                TerminalSize(80, 24)
            }
        } catch (_: Throwable) {
            TerminalSize(80, 24)
        }
    }

    @Throws(IOException::class)
    override fun runSTTYCommand(vararg parameters: String): String {
        val commandLine = mutableListOf(findSTTY(), "-F", pseudoTerminalDevice)
        commandLine.addAll(parameters)
        return exec(*commandLine.toTypedArray())
    }

    @Throws(IOException::class)
    override fun acquire() {
        super.acquire()
    }

    private fun findSTTY(): String {
        return STTY_LOCATION
    }

    private val pseudoTerminalDevice: String
        get() = "/dev/pty0"

    companion object {
        private const val JAVA_LIBRARY_PATH_PROPERTY = "java.library.path"
        private const val CYGWIN_HOME_ENV = "CYGWIN_HOME"
        private val STTY_LOCATION = findProgram("stty.exe")
        private val STTY_SIZE_PATTERN = Pattern.compile(".*rows ([0-9]+);.*columns ([0-9]+);.*")

        private fun findProgram(programName: String): String {
            val cygwinHome = System.getenv(CYGWIN_HOME_ENV)
            if (cygwinHome != null) {
                val cygwinHomeBinFile = File("$cygwinHome/bin", programName)
                if (cygwinHomeBinFile.exists()) {
                    return cygwinHomeBinFile.absolutePath
                }
            }
            val paths = System.getProperty(JAVA_LIBRARY_PATH_PROPERTY).split(";")
            for (path in paths) {
                val shBin = File(path, programName)
                if (shBin.exists()) {
                    return shBin.absolutePath
                }
            }
            return programName
        }
    }
}
