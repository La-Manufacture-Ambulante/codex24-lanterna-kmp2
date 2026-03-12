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

package com.googlecode.lanterna

import java.util.HashSet

/**
 * This program will print all ACS symbols to standard out, it's a good test
 * to see if your terminal emulator supports these UTF-8 characters or not.
 * @author Martin
 */
object TestACS {
    private val NEW_LINE_AFTER =
        object : HashSet<String?>() {
            init {
                add("MALE")
                add("ARROW_LEFT")
                add("BLOCK_SPARSE")
                add("DOUBLE_LINE_VERTICAL")
                add("DOUBLE_LINE_TOP_RIGHT_CORNER")
                add("DOUBLE_LINE_BOTTOM_RIGHT_CORNER")
                add("DOUBLE_LINE_CROSS")
                add("SINGLE_LINE_T_LEFT")
                add("SINGLE_LINE_T_DOUBLE_LEFT")
                add("DOUBLE_LINE_T_LEFT")
                add("DOUBLE_LINE_T_SINGLE_LEFT")
            }
        }

    fun main(args: Array<String?>?) {
        for (field in Symbols::class.java!!.getFields()) {
            field!!.setAccessible(true)
            try {
                System.out.printf(
                    "%1\$s = %2\$s%n%3\$s",
                    field!!.getName(),
                    field!!.get(null),
                    if (NEW_LINE_AFTER.contains(field!!.getName())) System.lineSeparator() else "",
                )
            } catch (e: IllegalAccessException) {
                e!!.printStackTrace()
            }
        }
    }
}
