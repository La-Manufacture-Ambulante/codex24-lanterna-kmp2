package com.googlecode.lanterna.input

import com.googlecode.lanterna.internal.compat.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InputDecoderEnterTest {
    @Test
    fun standaloneCarriageReturnDecodesAsEnter() {
        val decoder = InputDecoder(StringReader("\r"))
        decoder.addProfile(DefaultKeyDecodingProfile())

        assertEquals(KeyType.ENTER, decoder.getNextCharacter(false)?.keyType)
        assertNull(decoder.getNextCharacter(false))
    }

    @Test
    fun macStyleCarriageReturnNullDecodesAsSingleEnter() {
        val decoder = InputDecoder(StringReader("\r\u0000"))
        decoder.addProfile(DefaultKeyDecodingProfile())

        assertEquals(KeyType.ENTER, decoder.getNextCharacter(false)?.keyType)
        assertNull(decoder.getNextCharacter(false))
    }
}
