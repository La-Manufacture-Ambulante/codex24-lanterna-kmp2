package com.googlecode.lanterna.terminal.ansi

import java.lang.reflect.Field
import java.util.Collections
import java.util.HashMap
import java.util.Map

internal class TelnetProtocol private constructor() {
    companion object {
        @JvmField val COMMAND_SUBNEGOTIATION_END: Byte = 0xf0.toByte() // SE
        @JvmField val COMMAND_NO_OPERATION: Byte = 0xf1.toByte() // NOP
        @JvmField val COMMAND_DATA_MARK: Byte = 0xf2.toByte() // DM
        @JvmField val COMMAND_BREAK: Byte = 0xf3.toByte() // BRK
        @JvmField val COMMAND_INTERRUPT_PROCESS: Byte = 0xf4.toByte() // IP
        @JvmField val COMMAND_ABORT_OUTPUT: Byte = 0xf5.toByte() // AO
        @JvmField val COMMAND_ARE_YOU_THERE: Byte = 0xf6.toByte() // AYT
        @JvmField val COMMAND_ERASE_CHARACTER: Byte = 0xf7.toByte() // EC
        @JvmField val COMMAND_ERASE_LINE: Byte = 0xf8.toByte() // WL
        @JvmField val COMMAND_GO_AHEAD: Byte = 0xf9.toByte() // GA
        @JvmField val COMMAND_SUBNEGOTIATION: Byte = 0xfa.toByte() // SB
        @JvmField val COMMAND_WILL: Byte = 0xfb.toByte()
        @JvmField val COMMAND_WONT: Byte = 0xfc.toByte()
        @JvmField val COMMAND_DO: Byte = 0xfd.toByte()
        @JvmField val COMMAND_DONT: Byte = 0xfe.toByte()
        @JvmField val COMMAND_IAC: Byte = 0xff.toByte()

        @JvmField val OPTION_TRANSMIT_BINARY: Byte = 0x00.toByte()
        @JvmField val OPTION_ECHO: Byte = 0x01.toByte()
        @JvmField val OPTION_SUPPRESS_GO_AHEAD: Byte = 0x03.toByte()
        @JvmField val OPTION_STATUS: Byte = 0x05.toByte()
        @JvmField val OPTION_TIMING_MARK: Byte = 0x06.toByte()
        @JvmField val OPTION_NAOCRD: Byte = 0x0a.toByte()
        @JvmField val OPTION_NAOHTS: Byte = 0x0b.toByte()
        @JvmField val OPTION_NAOHTD: Byte = 0x0c.toByte()
        @JvmField val OPTION_NAOFFD: Byte = 0x0d.toByte()
        @JvmField val OPTION_NAOVTS: Byte = 0x0e.toByte()
        @JvmField val OPTION_NAOVTD: Byte = 0x0f.toByte()
        @JvmField val OPTION_NAOLFD: Byte = 0x10.toByte()
        @JvmField val OPTION_EXTEND_ASCII: Byte = 0x01.toByte()
        @JvmField val OPTION_TERMINAL_TYPE: Byte = 0x18.toByte()
        @JvmField val OPTION_NAWS: Byte = 0x1f.toByte()
        @JvmField val OPTION_TERMINAL_SPEED: Byte = 0x20.toByte()
        @JvmField val OPTION_TOGGLE_FLOW_CONTROL: Byte = 0x21.toByte()
        @JvmField val OPTION_LINEMODE: Byte = 0x22.toByte()
        @JvmField val OPTION_AUTHENTICATION: Byte = 0x25.toByte()

        @JvmField val NAME_TO_CODE: Map<String, Byte> = createName2CodeMap()
        @JvmField val CODE_TO_NAME: Map<Byte, String> = reverseMap(NAME_TO_CODE)

        private fun createName2CodeMap(): Map<String, Byte> {
            val result: MutableMap<String, Byte> = HashMap()
            for (field: Field in TelnetProtocol::class.java.declaredFields) {
                if (field.type != java.lang.Byte.TYPE ||
                    (!field.name.startsWith("COMMAND_") && !field.name.startsWith("OPTION_"))
                ) {
                    continue
                }
                try {
                    val namePart = field.name.substring(field.name.indexOf("_") + 1)
                    result[namePart] = field.get(null) as Byte
                } catch (ignored: IllegalAccessException) {
                } catch (ignored: IllegalArgumentException) {
                }
            }
            return Collections.unmodifiableMap(result)
        }

        private fun <V, K> reverseMap(n2c: Map<K, V>): Map<V, K> {
            val result: MutableMap<V, K> = HashMap()
            for (e: Map.Entry<K, V> in n2c.entries) {
                result[e.value] = e.key
            }
            return Collections.unmodifiableMap(result)
        }
    }
}
