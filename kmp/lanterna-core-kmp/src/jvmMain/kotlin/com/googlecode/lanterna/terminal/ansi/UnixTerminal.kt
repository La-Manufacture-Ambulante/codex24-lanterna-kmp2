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

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

class UnixTerminal
    @Throws(IOException::class)
    private constructor(
        terminalDevice: File,
        terminalInput: InputStream,
        terminalOutput: OutputStream,
        terminalCharset: Charset,
        terminalCtrlCBehaviour: CtrlCBehaviour,
    ) : UnixLikeTTYTerminal(terminalDevice, terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {
        @Throws(IOException::class)
        constructor() : this(System.`in`, System.out, Charset.defaultCharset())

        @Throws(IOException::class)
        constructor(
            terminalInput: InputStream,
            terminalOutput: OutputStream,
            terminalCharset: Charset,
        ) : this(terminalInput, terminalOutput, terminalCharset, CtrlCBehaviour.CTRL_C_KILLS_APPLICATION)

        @Throws(IOException::class)
        constructor(
            terminalInput: InputStream,
            terminalOutput: OutputStream,
            terminalCharset: Charset,
            terminalCtrlCBehaviour: CtrlCBehaviour,
        ) : this(File("/dev/tty"), terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour)
    }
