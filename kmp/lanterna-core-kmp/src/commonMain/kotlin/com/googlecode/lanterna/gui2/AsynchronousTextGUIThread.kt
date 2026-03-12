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
    /**
     * Starts the AsynchronousTextGUIThread, typically meaning that the event processing loop will start.
     */
    fun start()

    /**
     * Requests that the AsynchronousTextGUIThread stops, typically meaning that the event processing loop will exit
     */
    fun stop()

    /**
     * Blocks until the GUI loop has stopped
     * @throws InterruptedException In case this thread was interrupted while waiting for the GUI thread to exit
     */
    @Throws(InterruptedException::class)
    fun waitForStop()

    /**
     * Blocks until the GUI loop has stopped
     * @throws InterruptedException In case this thread was interrupted while waiting for the GUI thread to exit
     */
    @Throws(InterruptedException::class)
    fun waitForStop(
        time: Long,
        unit: TimeUnit?,
    )

    /**
     * Returns the current status of this GUI thread
     * @return Current status of the GUI thread
     */
    val state: State?

    /**
     * Enum representing the states of the GUI thread life-cycle
     */
    enum class State {
        /**
         * The instance has been created but not yet started
         */
        CREATED,

        /**
         * The thread has started an is running
         */
        STARTED,

        /**
         * The thread is trying to stop but is still running
         */
        STOPPING,

        /**
         * The thread has stopped
         */
        STOPPED,
    }
}
