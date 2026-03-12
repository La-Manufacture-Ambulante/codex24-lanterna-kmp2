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

import java.io.EOFException
import java.io.IOException
import java.util.Queue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

/**
 * Abstract implementation of [TextGUIThread] with common loop/task logic.
 */
abstract class AbstractTextGUIThread protected constructor(protected val textGUI: TextGUI) : TextGUIThread {
    protected val customTasks: Queue<Runnable> = LinkedBlockingQueue()
    protected var exceptionHandlerRef: TextGUIThread.ExceptionHandler? =
        object : TextGUIThread.ExceptionHandler {
            override fun onIOException(e: IOException?): Boolean {
                e?.printStackTrace()
                return true
            }

            override fun onRuntimeException(e: RuntimeException?): Boolean {
                e?.printStackTrace()
                return true
            }
        }

    override fun invokeLater(runnable: Runnable?) {
        if (runnable != null) {
            customTasks.add(runnable)
        }
    }

    override fun setExceptionHandler(exceptionHandler: TextGUIThread.ExceptionHandler?) {
        if (exceptionHandler == null) {
            throw IllegalArgumentException("Cannot call setExceptionHandler(null)")
        }
        this.exceptionHandlerRef = exceptionHandler
    }

    @Synchronized
    @Throws(IOException::class)
    override fun processEventsAndUpdate(): Boolean {
        if (thread != Thread.currentThread()) {
            throw IllegalStateException("Calling processEventAndUpdate outside of GUI thread")
        }
        try {
            textGUI.processInput()
            while (!customTasks.isEmpty()) {
                val runnable = customTasks.poll()
                runnable?.run()
            }
            if (textGUI.isPendingUpdate) {
                textGUI.updateScreen()
                return true
            }
            return false
        } catch (e: EOFException) {
            throw e
        } catch (e: IOException) {
            val handler = exceptionHandlerRef
            if (handler != null) {
                handler.onIOException(e)
            } else {
                throw e
            }
        } catch (e: RuntimeException) {
            val handler = exceptionHandlerRef
            if (handler != null) {
                handler.onRuntimeException(e)
            } else {
                throw e
            }
        }
        return true
    }

    @Throws(IllegalStateException::class, InterruptedException::class)
    override fun invokeAndWait(runnable: Runnable?) {
        val guiThread = thread
        if (runnable == null) {
            return
        }
        if (guiThread == null || Thread.currentThread() == guiThread) {
            runnable.run()
        } else {
            val countDownLatch = CountDownLatch(1)
            invokeLater(
                Runnable {
                    try {
                        runnable.run()
                    } finally {
                        countDownLatch.countDown()
                    }
                },
            )
            countDownLatch.await()
        }
    }
}
