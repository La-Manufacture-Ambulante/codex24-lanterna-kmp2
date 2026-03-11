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

import java.util.Collections

/**
 * Contains the telnet protocol commands, although not a complete set.
 * @author Martin
 */
internal object TelnetProtocol {
    const val COMMAND_SUBNEGOTIATION_END: Byte = 0xf0.toByte() // SE
    const val COMMAND_NO_OPERATION: Byte = 0xf1.toByte() // NOP
    const val COMMAND_DATA_MARK: Byte = 0xf2.toByte() // DM
    const val COMMAND_BREAK: Byte = 0xf3.toByte() // BRK
    const val COMMAND_INTERRUPT_PROCESS: Byte = 0xf4.toByte() // IP
    const val COMMAND_ABORT_OUTPUT: Byte = 0xf5.toByte() // AO
    const val COMMAND_ARE_YOU_THERE: Byte = 0xf6.toByte() // AYT
    const val COMMAND_ERASE_CHARACTER: Byte = 0xf7.toByte() // EC
    const val COMMAND_ERASE_LINE: Byte = 0xf8.toByte() // WL
    const val COMMAND_GO_AHEAD: Byte = 0xf9.toByte() // GA
    const val COMMAND_SUBNEGOTIATION: Byte = 0xfa.toByte() // SB
    const val COMMAND_WILL: Byte = 0xfb.toByte()
    const val COMMAND_WONT: Byte = 0xfc.toByte()
    const val COMMAND_DO: Byte = 0xfd.toByte()
    const val COMMAND_DONT: Byte = 0xfe.toByte()
    const val COMMAND_IAC: Byte = 0xff.toByte()

    const val OPTION_TRANSMIT_BINARY: Byte = 0x00
    const val OPTION_ECHO: Byte = 0x01
    const val OPTION_SUPPRESS_GO_AHEAD: Byte = 0x03
    const val OPTION_STATUS: Byte = 0x05
    const val OPTION_TIMING_MARK: Byte = 0x06
    const val OPTION_NAOCRD: Byte = 0x0a
    const val OPTION_NAOHTS: Byte = 0x0b
    const val OPTION_NAOHTD: Byte = 0x0c
    const val OPTION_NAOFFD: Byte = 0x0d
    const val OPTION_NAOVTS: Byte = 0x0e
    const val OPTION_NAOVTD: Byte = 0x0f
    const val OPTION_NAOLFD: Byte = 0x10
    const val OPTION_EXTEND_ASCII: Byte = 0x01
    const val OPTION_TERMINAL_TYPE: Byte = 0x18
    const val OPTION_NAWS: Byte = 0x1f
    const val OPTION_TERMINAL_SPEED: Byte = 0x20
    const val OPTION_TOGGLE_FLOW_CONTROL: Byte = 0x21
    const val OPTION_LINEMODE: Byte = 0x22
    const val OPTION_AUTHENTICATION: Byte = 0x25

    val NAME_TO_CODE: Map<String, Byte> = createName2CodeMap()
    val CODE_TO_NAME: Map<Byte, String> = reverseMap(NAME_TO_CODE)

    private fun createName2CodeMap(): Map<String, Byte> {
        val result = linkedMapOf<String, Byte>()
        for (field in TelnetProtocol::class.java.declaredFields) {
            if (field.type != Byte::class.javaPrimitiveType && field.type != Byte::class.javaObjectType) {
                continue
            }
            if (!field.name.startsWith("COMMAND_") && !field.name.startsWith("OPTION_")) {
                continue
            }
            try {
                val namePart = field.name.substring(field.name.indexOf('_') + 1)
                result[namePart] = field.get(null) as Byte
            } catch (_: IllegalAccessException) {
            } catch (_: IllegalArgumentException) {
            }
        }
        return Collections.unmodifiableMap(result)
    }

    private fun <V, K> reverseMap(nameToCode: Map<K, V>): Map<V, K> {
        val result = linkedMapOf<V, K>()
        for ((key, value) in nameToCode) {
            result[value] = key
        }
        return Collections.unmodifiableMap(result)
    }
}
