package com.googlecode.lanterna.input

import com.googlecode.lanterna.internal.compat.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals

class InputDecoderTest {
    @Test
    fun loneCarriageReturnDecodesAsEnter() {
        val decoder = InputDecoder(StringReader("\r"))
        decoder.addProfile(DefaultKeyDecodingProfile())

        val keyStroke = decoder.getNextCharacter(blockingIO = true)

        assertEquals(KeyType.ENTER, keyStroke?.keyType)
    }

    @Test
    fun carriageReturnNulSequenceStillDecodesAsEnter() {
        val decoder = InputDecoder(StringReader("\r\u0000"))
        decoder.addProfile(DefaultKeyDecodingProfile())

        val keyStroke = decoder.getNextCharacter(blockingIO = true)

        assertEquals(KeyType.ENTER, keyStroke?.keyType)
    }
}
