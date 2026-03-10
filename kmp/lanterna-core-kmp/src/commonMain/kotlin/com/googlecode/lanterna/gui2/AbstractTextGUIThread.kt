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

import com.googlecode.lanterna.internal.concurrency.PlatformCountdownLatch
import com.googlecode.lanterna.internal.concurrency.PlatformMutex

/**
 * Abstract implementation of [TextGUIThread] with common loop/task logic.
 */
abstract class AbstractTextGUIThread protected constructor(
    protected val textGUI: TextGUI,
) : TextGUIThread {
    private val taskLock = PlatformMutex()
    private val customTasks: ArrayDeque<GuiTask> = ArrayDeque()

    protected var exceptionHandlerRef: TextGUIThread.ExceptionHandler? = object : TextGUIThread.ExceptionHandler {
        override fun onException(error: Throwable): Boolean {
            error.printStackTrace()
            return true
        }
    }

    override fun invokeLater(task: GuiTask?) {
        if (task == null) {
            return
        }
        taskLock.withLock {
            customTasks.addLast(task)
        }
    }

    override fun setExceptionHandler(exceptionHandler: TextGUIThread.ExceptionHandler?) {
        requireNotNull(exceptionHandler) { "Cannot call setExceptionHandler(null)" }
        this.exceptionHandlerRef = exceptionHandler
    }

    override fun processEventsAndUpdate(): Boolean {
        if (!isCallingThread()) {
            throw IllegalStateException("Calling processEventsAndUpdate outside of GUI thread")
        }

        return try {
            textGUI.processInput()
            drainQueuedTasks()
            if (textGUI.isPendingUpdate) {
                textGUI.updateScreen()
                true
            } else {
                false
            }
        } catch (t: Throwable) {
            val shouldStop = exceptionHandlerRef?.onException(t)
            if (shouldStop == null) {
                throw t
            }
            true
        }
    }

    override fun invokeAndWait(task: GuiTask?) {
        if (task == null) {
            return
        }
        if (isCallingThread()) {
            task()
            return
        }

        val latch = PlatformCountdownLatch(1)
        invokeLater {
            try {
                task()
            } finally {
                latch.countDown()
            }
        }
        latch.await()
    }

    private fun drainQueuedTasks() {
        while (true) {
            val task = taskLock.withLock {
                if (customTasks.isEmpty()) {
                    null
                } else {
                    customTasks.removeFirst()
                }
            }
            task?.invoke() ?: return
        }
    }
}
