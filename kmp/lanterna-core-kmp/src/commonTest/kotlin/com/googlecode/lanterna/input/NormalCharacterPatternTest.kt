package com.googlecode.lanterna.input

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NormalCharacterPatternTest {
    @Test
    fun asciiCharactersAreRecognizedAsPrintable() {
        val key = NormalCharacterPattern().match(listOf('a'))?.fullMatch
        assertEquals(KeyStroke('a', false, false), key)
    }

    @Test
    fun specialsBlockCharacterIsRejected() {
        val key = NormalCharacterPattern().match(listOf('\uFFF0'))?.fullMatch
        assertNull(key)
    }
}
