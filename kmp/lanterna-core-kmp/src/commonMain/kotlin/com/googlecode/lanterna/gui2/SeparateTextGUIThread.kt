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
import com.googlecode.lanterna.internal.concurrency.PlatformTaskHandle
import com.googlecode.lanterna.internal.concurrency.PlatformTaskRuntime
import com.googlecode.lanterna.internal.concurrency.PlatformThreadToken
import com.googlecode.lanterna.internal.concurrency.currentThreadToken
import com.googlecode.lanterna.internal.concurrency.sleepCurrentThread

/**
 * Default implementation of [TextGUIThread] that runs the GUI loop on a dedicated thread.
 */
class SeparateTextGUIThread private constructor(textGUI: TextGUI) :
    AbstractTextGUIThread(textGUI),
    AsynchronousTextGUIThread {
        private var _state: AsynchronousTextGUIThread.State = AsynchronousTextGUIThread.State.CREATED

        override var ownerThreadToken: PlatformThreadToken? = null
            private set

        private val waitLatch = PlatformCountdownLatch(1)
        private var runtimeTaskHandle: PlatformTaskHandle? = null

        override val state: AsynchronousTextGUIThread.State
            get() = _state

        override fun start() {
            if (_state != AsynchronousTextGUIThread.State.CREATED) {
                return
            }
            PlatformTaskRuntime.configureExecutionModeFromEnvironment()
            _state = AsynchronousTextGUIThread.State.STARTED
            runtimeTaskHandle = PlatformTaskRuntime.launch("LanternaGUI") { mainGUILoop() }
        }

        override fun stop() {
            if (_state != AsynchronousTextGUIThread.State.STARTED) {
                return
            }
            _state = AsynchronousTextGUIThread.State.STOPPING
        }

        override fun waitForStop() {
            waitLatch.await()
        }

        override fun waitForStop(timeoutMillis: Long): Boolean {
            val localHandle = runtimeTaskHandle
            return if (localHandle == null) {
                waitLatch.await(timeoutMillis)
            } else {
                localHandle.awaitCompletion(timeoutMillis)
            }
        }

        override fun invokeLater(task: GuiTask?) {
            if (_state != AsynchronousTextGUIThread.State.STARTED) {
                throw IllegalStateException(
                    "Cannot schedule $task for execution on the TextGUIThread because the thread is in $_state state",
                )
            }
            super.invokeLater(task)
        }

        private fun mainGUILoop() {
            ownerThreadToken = currentThreadToken()
            try {
                try {
                    textGUI.updateScreen()
                } catch (t: Throwable) {
                    if (exceptionHandlerRef?.onException(t) == true) {
                        stop()
                    }
                }

                while (_state == AsynchronousTextGUIThread.State.STARTED) {
                    val didWork = processEventsAndUpdate()
                    if (!didWork) {
                        sleepCurrentThread(1)
                    }
                }
            } catch (t: Throwable) {
                if (exceptionHandlerRef?.onException(t) == true) {
                    stop()
                }
            } finally {
                _state = AsynchronousTextGUIThread.State.STOPPED
                runtimeTaskHandle = null
                waitLatch.countDown()
            }
        }

        class Factory : TextGUIThreadFactory {
            override fun createTextGUIThread(textGUI: TextGUI?): TextGUIThread? {
                return textGUI?.let { SeparateTextGUIThread(it) }
            }
        }
    }
