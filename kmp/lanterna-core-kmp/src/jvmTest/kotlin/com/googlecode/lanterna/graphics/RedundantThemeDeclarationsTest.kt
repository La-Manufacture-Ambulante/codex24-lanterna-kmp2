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
package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.*
import com.googlecode.lanterna.bundle.LanternaThemes
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.util.Collections

@Ignore("Theme registry/resource parity is pending in KMP runtime")
class RedundantThemeDeclarationsTest {
    @Test
    fun noThemeDeclarationsAreRedundant() {
        for (theme in LanternaThemes.registeredThemes.filterNotNull()) {
            val registeredTheme = LanternaThemes.getRegisteredTheme(theme)
            if (registeredTheme !is PropertyTheme) {
                continue
            }
            val redundantDeclarations = (registeredTheme as PropertyTheme).findRedundantDeclarations()
            try {
                Assert.assertEquals(Collections.emptyList<String>(), redundantDeclarations)
            } catch (e: AssertionError) {
                System.out.println("Redundant definitions in theme '" + theme + "':")
                for (declaration in redundantDeclarations.orEmpty()) {
                    System.out.println(declaration)
                }
                throw e
            }
        }
    }
}
