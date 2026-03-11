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
@file:Suppress("ktlint:standard:max-line-length")

package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_DO
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_DONT
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_IAC
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_SUBNEGOTIATION
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_SUBNEGOTIATION_END
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_WILL
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.COMMAND_WONT
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.OPTION_ECHO
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.OPTION_EXTEND_ASCII
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.OPTION_LINEMODE
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.OPTION_NAWS
import com.googlecode.lanterna.terminal.ansi.TelnetProtocol.OPTION_SUPPRESS_GO_AHEAD
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.nio.charset.Charset

/**
 * This class is used by the `TelnetTerminalServer` class when a client has connected in; this class will be the
 * interaction point for that client. All operations are sent to the client over the network socket and some of the
 * meta-operations (like echo mode) are communicated using Telnet negotiation language. You can't create objects of this
 * class directly; they are created for you when you are listening for incoming connections using a
 * `TelnetTerminalServer` and a client connects.
 *
 * A good resource on telnet communication is http://www.tcpipguide.com/free/t_TelnetProtocol.htm
 * Also here: http://support.microsoft.com/kb/231866
 * @see TelnetTerminalServer
 * @author martin
 */
class TelnetTerminal
    @Throws(IOException::class)
    private constructor(
        private val socket: Socket,
        inputStream: TelnetClientIACFilterer,
        outputStream: OutputStream,
        terminalCharset: Charset,
    ) : ANSITerminal(inputStream, outputStream, terminalCharset) {
        /**
         * Retrieves the current negotiation state with the client, containing details on what options have been enabled
         * and what the client has said it supports.
         * @return The current negotiation state for this client
         */
        val negotiationState: NegotiationState

        /**
         * Returns the socket address for the remote endpoint of the telnet connection
         * @return SocketAddress representing the remote client
         */
        val remoteSocketAddress: SocketAddress
            get() = socket.remoteSocketAddress

        @Throws(IOException::class)
        internal constructor(
            socket: Socket,
            terminalCharset: Charset,
        ) : this(socket, TelnetClientIACFilterer(socket), socket.getOutputStream(), terminalCharset)

        init {
            negotiationState = inputStream.negotiationState
            inputStream.setEventListener(
                object : TelnetClientEventListener {
                    override fun onResize(
                        columns: Int,
                        rows: Int,
                    ) {
                        this@TelnetTerminal.onResized(columns, rows)
                    }

                    @Throws(IOException::class)
                    override fun requestReply(
                        will: Boolean,
                        option: Byte,
                    ) {
                        writeToTerminal(COMMAND_IAC, if (will) COMMAND_WILL else COMMAND_WONT, option)
                    }
                },
            )
            setLineMode0()
            setEchoOff()
            setResizeNotificationOn()
        }

        @Throws(IOException::class)
        private fun setEchoOff() {
            writeToTerminal(COMMAND_IAC, COMMAND_WILL, OPTION_ECHO)
            flush()
        }

        @Throws(IOException::class)
        private fun setLineMode0() {
            writeToTerminal(
                COMMAND_IAC,
                COMMAND_DO,
                OPTION_LINEMODE,
                COMMAND_IAC,
                COMMAND_SUBNEGOTIATION,
                OPTION_LINEMODE,
                1,
                0,
                COMMAND_IAC,
                COMMAND_SUBNEGOTIATION_END,
            )
            flush()
        }

        @Throws(IOException::class)
        private fun setResizeNotificationOn() {
            writeToTerminal(COMMAND_IAC, COMMAND_DO, OPTION_NAWS)
            flush()
        }

        @Throws(IOException::class)
        override fun close() {
            super.close()
            socket.close()
        }

        /**
         * This class contains some of the various states that the Telnet negotiation protocol defines. Lanterna doesn't
         * support all of them but the more common ones are represented.
         */
        class NegotiationState internal constructor() {
            /**
             * Is the telnet client echo mode turned on (client is echoing characters locally)
             * @return `true` if client echo is enabled
             */
            var isClientEcho: Boolean = true
                internal set

            /**
             * Is the telnet client line mode 0 turned on (client sends character by character instead of line by line)
             * @return `true` if client line mode 0 is enabled
             */
            var isClientLineMode0: Boolean = false
                internal set

            /**
             * Is the telnet client resize notification turned on (client notifies server when the terminal window has
             * changed size)
             * @return `true` if client resize notification is enabled
             */
            var isClientResizeNotification: Boolean = false
                internal set

            /**
             * Is the telnet client suppress go-ahead turned on
             * @return `true` if client suppress go-ahead is enabled
             */
            var isSuppressGoAhead: Boolean = true
                internal set

            /**
             * Is the telnet client extended ascii turned on
             * @return `true` if client extended ascii is enabled
             */
            var isExtendedAscii: Boolean = true
                internal set

            internal fun onUnsupportedStateCommand(
                enabling: Boolean,
                value: Byte,
            ) {
                System.err.println(
                    "Unsupported operation: Client says it " +
                        (if (enabling) "will" else "won't") +
                        " do " +
                        TelnetProtocol.CODE_TO_NAME[value],
                )
            }

            internal fun onUnsupportedRequestCommand(
                askedToDo: Boolean,
                value: Byte,
            ) {
                System.err.println(
                    "Unsupported request: Client asks us, " +
                        (if (askedToDo) "do" else "don't") +
                        " " +
                        TelnetProtocol.CODE_TO_NAME[value],
                )
            }

            internal fun onUnsupportedSubnegotiation(
                option: Byte,
                additionalData: ByteArray,
            ) {
                System.err.println(
                    "Unsupported subnegotiation: Client send " +
                        TelnetProtocol.CODE_TO_NAME[option] +
                        " with extra data " +
                        toList(additionalData),
                )
            }

            private fun toList(array: ByteArray): List<String> {
                val list = ArrayList<String>(array.size)
                for (byteValue in array) {
                    list.add(String.format("%02X ", byteValue))
                }
                return list
            }
        }

        private interface TelnetClientEventListener {
            fun onResize(
                columns: Int,
                rows: Int,
            )

            @Throws(IOException::class)
            fun requestReply(
                will: Boolean,
                option: Byte,
            )
        }

        private class TelnetClientIACFilterer
            @Throws(IOException::class)
            constructor(
                socket: Socket,
            ) : InputStream() {
                val negotiationState = NegotiationState()
                private val inputStream: InputStream = socket.getInputStream()
                private val buffer = ByteArray(64 * 1024)
                private val workingBuffer = ByteArray(1024)
                private var bytesInBuffer = 0
                private var eventListener: TelnetClientEventListener? = null
                private val socket: Socket = socket

                fun setEventListener(eventListener: TelnetClientEventListener?) {
                    this.eventListener = eventListener
                }

                override fun read(): Int {
                    throw UnsupportedOperationException("TelnetClientIACFilterer doesn't support .read()")
                }

                @Throws(IOException::class)
                override fun close() {
                    inputStream.close()
                }

                @Throws(IOException::class)
                override fun available(): Int {
                    if (bytesInBuffer > 0) {
                        return bytesInBuffer
                    }
                    fillBuffer(false)
                    return kotlin.math.abs(bytesInBuffer)
                }

                @Throws(IOException::class)
                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int {
                    if (bytesInBuffer == -1) {
                        return -1
                    }
                    if (available() == 0) {
                        fillBuffer(true)
                    }
                    if (bytesInBuffer <= 0) {
                        return -1
                    }
                    val bytesToCopy = minOf(len, bytesInBuffer)
                    System.arraycopy(buffer, 0, b, off, bytesToCopy)
                    System.arraycopy(buffer, bytesToCopy, buffer, 0, buffer.size - bytesToCopy)
                    bytesInBuffer -= bytesToCopy
                    return bytesToCopy
                }

                @Throws(IOException::class)
                private fun fillBuffer(block: Boolean) {
                    val maxFill = minOf(workingBuffer.size, buffer.size - bytesInBuffer)
                    val oldTimeout = socket.soTimeout
                    if (!block) {
                        socket.soTimeout = 1
                    }
                    val readBytes =
                        try {
                            inputStream.read(workingBuffer, 0, maxFill)
                        } catch (_: SocketTimeoutException) {
                            0
                        }
                    if (!block) {
                        socket.soTimeout = oldTimeout
                    }
                    if (readBytes == -1) {
                        bytesInBuffer = -1
                        return
                    }
                    var i = 0
                    while (i < readBytes) {
                        if (workingBuffer[i] == COMMAND_IAC) {
                            i++
                            if (i >= readBytes) {
                                break
                            }
                            if (workingBuffer[i] in byteArrayOf(COMMAND_DO, COMMAND_DONT, COMMAND_WILL, COMMAND_WONT)) {
                                parseCommand(workingBuffer, i, readBytes)
                                i += 2
                                continue
                            } else if (workingBuffer[i] == COMMAND_SUBNEGOTIATION) {
                                i += parseSubNegotiation(workingBuffer, i + 1, readBytes)
                                continue
                            } else if (workingBuffer[i] != COMMAND_IAC) {
                                System.err.println("Unknown Telnet command: ${workingBuffer[i]}")
                            }
                        }
                        buffer[bytesInBuffer++] = workingBuffer[i]
                        i++
                    }
                }

                @Throws(IOException::class)
                private fun parseCommand(
                    buffer: ByteArray,
                    position: Int,
                    max: Int,
                ) {
                    if (position + 1 >= max) {
                        throw IllegalStateException(
                            "State error, we got a command signal from the remote telnet client but not enough characters available in the stream",
                        )
                    }
                    val command = buffer[position]
                    val value = buffer[position + 1]
                    when (command) {
                        COMMAND_DO, COMMAND_DONT -> {
                            if (value == OPTION_SUPPRESS_GO_AHEAD) {
                                negotiationState.isSuppressGoAhead = command == COMMAND_DO
                                eventListener?.requestReply(command == COMMAND_DO, value)
                            } else if (value == OPTION_EXTEND_ASCII) {
                                negotiationState.isExtendedAscii = command == COMMAND_DO
                                eventListener?.requestReply(command == COMMAND_DO, value)
                            } else {
                                negotiationState.onUnsupportedRequestCommand(command == COMMAND_DO, value)
                            }
                        }
                        COMMAND_WILL, COMMAND_WONT -> {
                            if (value == OPTION_ECHO) {
                                negotiationState.isClientEcho = command == COMMAND_WILL
                            } else if (value == OPTION_LINEMODE) {
                                negotiationState.isClientLineMode0 = command == COMMAND_WILL
                            } else if (value == OPTION_NAWS) {
                                negotiationState.isClientResizeNotification = command == COMMAND_WILL
                            } else {
                                negotiationState.onUnsupportedStateCommand(command == COMMAND_WILL, value)
                            }
                        }
                        else -> throw UnsupportedOperationException(
                            "No command handler implemented for ${TelnetProtocol.CODE_TO_NAME[command]}",
                        )
                    }
                }

                private fun parseSubNegotiation(
                    buffer: ByteArray,
                    position: Int,
                    max: Int,
                ): Int {
                    val originalPosition = position
                    var currentPosition = position
                    val operation = buffer[currentPosition++]
                    val outputBuffer = ByteArrayOutputStream()
                    while (currentPosition < max) {
                        val read = buffer[currentPosition]
                        if (read != COMMAND_IAC) {
                            outputBuffer.write(read.toInt())
                        } else {
                            if (currentPosition + 1 == max) {
                                throw IllegalStateException("State error, unexpected end of buffer when reading subnegotiation")
                            }
                            currentPosition++
                            if (buffer[currentPosition] == COMMAND_IAC) {
                                outputBuffer.write(COMMAND_IAC.toInt())
                            } else if (buffer[currentPosition] == COMMAND_SUBNEGOTIATION_END) {
                                parseSubNegotiation(operation, outputBuffer.toByteArray())
                                return currentPosition + 1 - originalPosition
                            }
                        }
                        currentPosition++
                    }
                    throw IllegalStateException(
                        "State error, unexpected end of buffer when reading subnegotiation, no IAC SE",
                    )
                }

                private fun parseSubNegotiation(
                    option: Byte,
                    additionalData: ByteArray,
                ) {
                    when (option) {
                        OPTION_NAWS ->
                            eventListener?.onResize(
                                convertTwoBytesToInt2(additionalData[1], additionalData[0]),
                                convertTwoBytesToInt2(additionalData[3], additionalData[2]),
                            )
                        OPTION_LINEMODE -> Unit
                        else -> negotiationState.onUnsupportedSubnegotiation(option, additionalData)
                    }
                }
            }

        companion object {
            private fun convertTwoBytesToInt2(
                b1: Byte,
                b2: Byte,
            ): Int {
                return ((b2.toInt() and 0xFF) shl 8) or (b1.toInt() and 0xFF)
            }
        }
    }
