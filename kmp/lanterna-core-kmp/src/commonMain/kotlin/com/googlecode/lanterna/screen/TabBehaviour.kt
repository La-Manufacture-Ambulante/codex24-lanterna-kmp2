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
package com.googlecode.lanterna.screen

enum class TabBehaviour(private val replaceFactor: Int?, private val alignFactor: Int?) {
    IGNORE(null, null),
    CONVERT_TO_ONE_SPACE(1, null),
    CONVERT_TO_TWO_SPACES(2, null),
    CONVERT_TO_THREE_SPACES(3, null),
    CONVERT_TO_FOUR_SPACES(4, null),
    CONVERT_TO_EIGHT_SPACES(8, null),
    ALIGN_TO_COLUMN_4(null, 4),
    ALIGN_TO_COLUMN_8(null, 8),
    ;

    fun replaceTabs(
        string: String,
        columnIndex: Int,
    ): String {
        var result = string
        var tabPosition = result.indexOf('\t')
        while (tabPosition != -1) {
            val replacement = getTabReplacement(columnIndex + tabPosition)
            result = result.substring(0, tabPosition) + replacement + result.substring(tabPosition + 1)
            tabPosition += replacement.length
            tabPosition = result.indexOf('\t', tabPosition)
        }
        return result
    }

    fun getTabReplacement(columnIndex: Int): String {
        val replaceCount =
            when {
                replaceFactor != null -> replaceFactor
                alignFactor != null -> alignFactor - (columnIndex % alignFactor)
                else -> return "\t"
            }
        return buildString {
            repeat(replaceCount) {
                append(' ')
            }
        }
    }
}
