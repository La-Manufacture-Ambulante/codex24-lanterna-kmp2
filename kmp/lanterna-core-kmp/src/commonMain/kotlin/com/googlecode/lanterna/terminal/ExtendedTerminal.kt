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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.graphics.Scrollable
import com.googlecode.lanterna.internal.io.IOException

interface ExtendedTerminal : Terminal, Scrollable {
    @Throws(IOException::class)
    fun setTerminalSize(
        columns: Int,
        rows: Int,
    )

    @Throws(IOException::class)
    fun setTitle(title: String?)

    @Throws(IOException::class)
    fun pushTitle()

    @Throws(IOException::class)
    fun popTitle()

    @Throws(IOException::class)
    fun iconify()

    @Throws(IOException::class)
    fun deiconify()

    @Throws(IOException::class)
    fun maximize()

    @Throws(IOException::class)
    fun unmaximize()

    @Throws(IOException::class)
    fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?)
}
