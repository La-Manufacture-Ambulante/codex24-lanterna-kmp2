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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Default implementation of TextGUIThread, this class runs the GUI event processing on a dedicated thread. The GUI
 * needs to be explicitly started in order for the event processing loop to begin, so you must call `start()`
 * for this. The GUI thread will stop if `stop()` is called, the input stream returns EOF or an exception is
 * thrown from inside the event handling loop.
 * <p>
 * Here is an example of how to use this `TextGUIThread`:
 * <pre>
 *     `MultiWindowTextGUI textGUI = new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
 *     // ... add components ...
 *     ((AsynchronousTextGUIThread)textGUI.getGUIThread()).start();
 *     // ... this thread will continue while the GUI runs on a separate thread ...`
 * </pre>
 * @see TextGUIThread
 * @see SameTextGUIThread
 * @author Martin
 */
class SeparateTextGUIThread private constructor(textGUI: TextGUI) :
    AbstractTextGUIThread(textGUI),
    AsynchronousTextGUIThread {
        @Volatile
        override var state: AsynchronousTextGUIThread.State? = AsynchronousTextGUIThread.State.CREATED
            private set

        private val waitLatch = CountDownLatch(1)

        override val thread: Thread =
            object : Thread("LanternaGUI") {
                override fun run() {
                    mainGUILoop()
                }
            }

        override fun start() {
            thread.start()
            state = AsynchronousTextGUIThread.State.STARTED
        }

        override fun stop() {
            if (state != AsynchronousTextGUIThread.State.STARTED) {
                return
            }
            state = AsynchronousTextGUIThread.State.STOPPING
        }

        @Throws(InterruptedException::class)
        override fun waitForStop() {
            waitLatch.await()
        }

        @Throws(InterruptedException::class)
        override fun waitForStop(
            time: Long,
            unit: TimeUnit?,
        ) {
            waitLatch.await(time, unit)
        }

        override fun invokeLater(runnable: Runnable?) {
            if (state != AsynchronousTextGUIThread.State.STARTED) {
                throw IllegalStateException(
                    "Cannot schedule $runnable for execution on the TextGUIThread because the thread is in $state state",
                )
            }
            super.invokeLater(runnable)
        }

        private fun mainGUILoop() {
            try {
                try {
                    textGUI.updateScreen()
                } catch (e: IOException) {
                    exceptionHandlerRef?.onIOException(e)
                } catch (e: RuntimeException) {
                    exceptionHandlerRef?.onRuntimeException(e)
                }

                while (state == AsynchronousTextGUIThread.State.STARTED) {
                    try {
                        if (!processEventsAndUpdate()) {
                            try {
                                Thread.sleep(1)
                            } catch (_: InterruptedException) {
                                // Ignore and continue loop processing.
                            }
                        }
                    } catch (e: EOFException) {
                        stop()
                        if (textGUI is WindowBasedTextGUI) {
                            for (window in textGUI.windows.orEmpty()) {
                                window?.close()
                            }
                        }
                        break
                    } catch (e: IOException) {
                        if (exceptionHandlerRef?.onIOException(e) == true) {
                            stop()
                            break
                        }
                    } catch (e: RuntimeException) {
                        if (exceptionHandlerRef?.onRuntimeException(e) == true) {
                            stop()
                            break
                        }
                    }
                }
            } finally {
                state = AsynchronousTextGUIThread.State.STOPPED
                waitLatch.countDown()
            }
        }

        class Factory : TextGUIThreadFactory {
            override fun createTextGUIThread(textGUI: TextGUI?): TextGUIThread? {
                return SeparateTextGUIThread(textGUI!!)
            }
        }
    }
