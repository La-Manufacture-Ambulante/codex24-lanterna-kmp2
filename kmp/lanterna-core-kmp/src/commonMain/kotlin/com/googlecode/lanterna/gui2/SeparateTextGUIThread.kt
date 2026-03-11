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
import com.googlecode.lanterna.internal.concurrency.PlatformThread
import com.googlecode.lanterna.internal.concurrency.PlatformThreadToken
import com.googlecode.lanterna.internal.concurrency.currentThreadToken
import com.googlecode.lanterna.internal.concurrency.sleepCurrentThread

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

    private var _state: AsynchronousTextGUIThread.State = AsynchronousTextGUIThread.State.CREATED

    override var ownerThreadToken: PlatformThreadToken? = null
        private set

    private val waitLatch = PlatformCountdownLatch(1)

    private val thread = PlatformThread("LanternaGUI") {
        mainGUILoop()
    }

    override val state: AsynchronousTextGUIThread.State
        get() = _state

    override fun start() {
        if (_state != AsynchronousTextGUIThread.State.CREATED) {
            return
        }
        _state = AsynchronousTextGUIThread.State.STARTED
        thread.start()
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
        return waitLatch.await(timeoutMillis)
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
            waitLatch.countDown()
        }
    }

    class Factory : TextGUIThreadFactory {
        override fun createTextGUIThread(textGUI: TextGUI?): TextGUIThread? {
            return SeparateTextGUIThread(textGUI!!)
        }
    }
}
