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
package com.googlecode.lanterna.gui2

import java.util.concurrent.TimeUnit

interface AsynchronousTextGUIThread : TextGUIThread {
    fun start()
    fun stop()

    @Throws(InterruptedException::class)
    fun waitForStop()

    @Throws(InterruptedException::class)
    fun waitForStop(time: Long, unit: TimeUnit?)

    val state: State?

    enum class State {
        CREATED,
        STARTED,
        STOPPING,
        STOPPED,
    }
}
