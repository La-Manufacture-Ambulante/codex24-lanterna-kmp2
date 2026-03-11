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

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

import org.junit.Test

import org.junit.Assert.*

 class KeyTest {

@Test
  fun testFromVim() {
kotlin.run {
val k = KeyStroke.fromString("a")
assertEquals(KeyType.CHARACTER, k!!.keyType)
assertEquals(Character('a'), k.character)
assertFalse(k.isCtrlDown)
assertFalse(k.isAltDown)
}
kotlin.run {
val k = KeyStroke.fromString("<c-a>")
assertEquals(KeyType.CHARACTER, k!!.keyType)
assertEquals(Character('a'), k.character)
assertTrue(k.isCtrlDown)
assertFalse(k.isAltDown)
}
kotlin.run {
val k = KeyStroke.fromString("<a-a>")
assertEquals(KeyType.CHARACTER, k!!.keyType)
assertEquals(Character('a'), k.character)
assertFalse(k.isCtrlDown)
assertTrue(k.isAltDown)
}
kotlin.run {
val k = KeyStroke.fromString("<c-a-a>")
assertEquals(k!!.keyType, KeyType.CHARACTER)
assertEquals(Character('a'), k.character)
assertTrue(k.isCtrlDown)
assertTrue(k.isAltDown)
}
assertEquals(KeyType.REVERSE_TAB, KeyStroke.fromString("<s-tab>").keyType)
assertEquals(KeyType.REVERSE_TAB, KeyStroke.fromString("<S-tab>").keyType)
assertEquals(KeyType.REVERSE_TAB, KeyStroke.fromString("<S-Tab>").keyType)
assertEquals(KeyType.ENTER, KeyStroke.fromString("<cr>").keyType)
assertEquals(KeyType.PAGE_UP, KeyStroke.fromString("<PageUp>").keyType)
}

}
