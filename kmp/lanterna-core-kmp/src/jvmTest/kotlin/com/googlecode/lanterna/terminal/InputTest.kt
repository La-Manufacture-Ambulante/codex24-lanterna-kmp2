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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.*
import java.io.*

/**
 * Use this program to see what the terminal emulator is sending through stdin; byte for byte
 */
object InputTest {
    @Throws(IOException::class)
    fun main(args: Array<String?>) {
        var useReader = false
        var privateMode = false
        for (parameter in args) {
            if ("--mouse-click".equals(parameter)) {
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '0'.toByte(), 'h'.toByte())
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '5'.toByte(), 'h'.toByte())
                Runtime.getRuntime().addShutdownHook(
                    Thread({
                        try {
                            writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '0'.toByte(), 'l'.toByte())
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }),
                )
            } else if ("--mouse-drag".equals(parameter)) {
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '2'.toByte(), 'h'.toByte())
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '5'.toByte(), 'h'.toByte())
                Runtime.getRuntime().addShutdownHook(
                    Thread({
                        try {
                            writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '2'.toByte(), 'l'.toByte())
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }),
                )
            } else if ("--mouse-move".equals(parameter)) {
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '3'.toByte(), 'h'.toByte())
                writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '5'.toByte(), 'h'.toByte())
                Runtime.getRuntime().addShutdownHook(
                    Thread({
                        try {
                            writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '0'.toByte(), '3'.toByte(), 'l'.toByte())
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }),
                )
            } else if ("--reader".equals(parameter)) {
                useReader = true
            } else if ("--cbreak".equals(parameter)) {
                exec("sh", "-c", "stty -icanon < /dev/tty")
                Runtime.getRuntime().addShutdownHook(
                    Thread({
                        try {
                            exec("sh", "-c", "stty icanon < /dev/tty")
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }),
                )
            } else if ("--no-echo".equals(parameter)) {
                exec("sh", "-c", "stty -echo < /dev/tty")
                Runtime.getRuntime().addShutdownHook(
                    Thread({
                        try {
                            exec("sh", "-c", "stty echo < /dev/tty")
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }),
                )
            } else if ("--private".equals(parameter)) {
                privateMode = true
            } else {
                System.err.println("Unknown parameter " + parameter!!)
                return
            }
        }
        if (privateMode) {
            writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '4'.toByte(), '9'.toByte(), 'h'.toByte())
            Runtime.getRuntime().addShutdownHook(
                object : Thread("RestoreTerminal") {
                    override fun run() {
                        try {
                            writeCSISequenceToTerminal('?'.toByte(), '1'.toByte(), '0'.toByte(), '4'.toByte(), '9'.toByte(), 'l'.toByte())
                        } catch (e: IOException) {
                            e!!.printStackTrace()
                        }
                    }
                },
            )
        }
        if (useReader) {
            val reader = InputStreamReader(System.`in`)
            while (true) {
                val inChar = reader.read()
                if (inChar == -1) {
                    break
                }
                System.out.println(formatData(inChar))
            }
        } else {
            while (true) {
                val inByte = System.`in`.read()
                if (inByte == -1) {
                    break
                }
                System.out.println(formatData(inByte))
            }
        }
    }

    private fun formatData(inByte: Int): String {
        var charString = Character.toString(inByte.toChar())
        if (Character.isISOControl(inByte)) {
            charString = "<control character>"
        }
        return "$inByte (0x${Integer.toString(inByte, 16)}, b${Integer.toString(inByte, 2)}, '$charString')"
    }

    @Throws(IOException::class)
    private fun writeCSISequenceToTerminal(vararg bytes: Byte) {
        System.out.write(byteArrayOf(0x1b.toByte(), '['.toByte()))
        System.out.write(bytes)
        System.out.flush()
    }

    @Throws(IOException::class)
    private fun exec(vararg cmd: String?): String? {
        val pb = ProcessBuilder(*cmd.filterNotNull().toTypedArray())
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
        while (true) {
            val line = reader.readLine() ?: break
            builder.append(line)
        }
        reader.close()
        return builder.toString()
    }
}
