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

import java.io.IOException
import java.net.ServerSocket
import java.nio.charset.Charset
import javax.net.ServerSocketFactory

@Suppress("WeakerAccess")
class TelnetTerminalServer
    @Throws(IOException::class)
    constructor(
        serverSocketFactory: ServerSocketFactory,
        port: Int,
        private val charset: Charset,
    ) {
        val serverSocket: ServerSocket = serverSocketFactory.createServerSocket(port)

        @Throws(IOException::class)
        constructor(port: Int) : this(ServerSocketFactory.getDefault(), port)

        @Throws(IOException::class)
        constructor(port: Int, charset: Charset) : this(ServerSocketFactory.getDefault(), port, charset)

        @Throws(IOException::class)
        constructor(serverSocketFactory: ServerSocketFactory, port: Int) : this(
            serverSocketFactory,
            port,
            Charset.defaultCharset(),
        )

        @Throws(IOException::class)
        fun acceptConnection(): TelnetTerminal {
            val clientSocket = serverSocket.accept()
            clientSocket.tcpNoDelay = true
            return TelnetTerminal(clientSocket, charset)
        }

        @Throws(IOException::class)
        fun close() {
            serverSocket.close()
        }
    }
