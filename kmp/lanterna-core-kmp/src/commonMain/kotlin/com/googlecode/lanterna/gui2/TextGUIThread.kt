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

import com.googlecode.lanterna.internal.concurrency.PlatformThreadToken
import com.googlecode.lanterna.internal.concurrency.currentThreadToken

typealias GuiTask = () -> Unit

/**
 * Represents the thread expected to run the event/input/update loop for a [TextGUI].
 *
 * There are mainly two implementations:
 * [SameTextGUIThread] where the caller thread drives the loop, and
 * [SeparateTextGUIThread] where the loop runs on a dedicated thread.
 *
 * @author Martin
 */
interface TextGUIThread {
    val ownerThreadToken: PlatformThreadToken?

    fun isCallingThread(): Boolean {
        val owner = ownerThreadToken ?: return false
        return owner == currentThreadToken()
    }

    /**
     * Invokes custom code on the GUI thread. Even if the current thread is the GUI thread, the code is queued and
     * executed after the current event-processing cycle.
     *
     * @throws IllegalStateException If the GUI thread is not running
     */
    @Throws(IllegalStateException::class)
    fun invokeLater(task: GuiTask?)

    /**
     * Runs one event/input/update cycle.
     *
     * @return `true` if there was anything to process or the GUI updated, otherwise `false`
     */
    fun processEventsAndUpdate(): Boolean

    /**
     * Schedules custom code on the GUI thread and waits for completion.
     * If called from the GUI thread itself, the task executes immediately.
     *
     * @throws IllegalStateException If the GUI thread is not running
     */
    @Throws(IllegalStateException::class)
    fun invokeAndWait(task: GuiTask?)

    /**
     * Updates the exception handler used by this TextGUI thread.
     */
    fun setExceptionHandler(exceptionHandler: ExceptionHandler?)

    /**
     * Defines an exception handler used by the GUI event loop.
     * Returning `true` indicates the GUI thread should terminate.
     */
    interface ExceptionHandler {
        /**
         * @return true when the event thread should terminate.
         */
        fun onException(error: Throwable): Boolean
    }
}
