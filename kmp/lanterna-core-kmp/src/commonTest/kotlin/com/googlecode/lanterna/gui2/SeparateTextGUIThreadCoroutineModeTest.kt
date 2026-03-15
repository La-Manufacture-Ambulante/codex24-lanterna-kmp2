package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.internal.concurrency.PlatformExecutionMode
import com.googlecode.lanterna.internal.concurrency.PlatformTaskRuntime
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.screen.Screen
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SeparateTextGUIThreadCoroutineModeTest {
    @AfterTest
    fun resetRuntime() {
        PlatformTaskRuntime.resetForTests()
    }

    @Test
    fun separateTextGuiThreadStartsAndStopsInCoroutineMode() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)

        val textGui = FakeTextGUI()
        val thread = SeparateTextGUIThread.Factory().createTextGUIThread(textGui) as AsynchronousTextGUIThread

        thread.start()
        thread.invokeAndWait { thread.stop() }

        assertTrue(thread.waitForStop(2_000))
        assertEquals(AsynchronousTextGUIThread.State.STOPPED, thread.state)
        assertNotNull(thread.ownerThreadToken)
        assertTrue(textGui.processInputCount > 0)
    }

    @Test
    fun separateTextGuiThreadUsesCoroutineModeFromEnvironmentConfiguration() {
        PlatformTaskRuntime.setPropertyLookupForTests { null }
        PlatformTaskRuntime.setEnvironmentLookupForTests { "coroutine" }

        val textGui = FakeTextGUI()
        val thread = SeparateTextGUIThread.Factory().createTextGUIThread(textGui) as AsynchronousTextGUIThread

        thread.start()
        thread.invokeAndWait { thread.stop() }

        assertTrue(thread.waitForStop(2_000))
        assertEquals(AsynchronousTextGUIThread.State.STOPPED, thread.state)
        assertNotNull(thread.ownerThreadToken)
        assertTrue(textGui.processInputCount > 0)
        assertEquals(PlatformExecutionMode.COROUTINE, PlatformTaskRuntime.executionMode())
    }

    @Test
    fun separateTextGuiThreadPrefersPropertyConfigurationOverEnvironment() {
        PlatformTaskRuntime.setPropertyLookupForTests { "thread" }
        PlatformTaskRuntime.setEnvironmentLookupForTests { "coroutine" }

        val textGui = FakeTextGUI()
        val thread = SeparateTextGUIThread.Factory().createTextGUIThread(textGui) as AsynchronousTextGUIThread

        thread.start()
        thread.invokeAndWait { thread.stop() }

        assertTrue(thread.waitForStop(2_000))
        assertEquals(AsynchronousTextGUIThread.State.STOPPED, thread.state)
        assertNotNull(thread.ownerThreadToken)
        assertTrue(textGui.processInputCount > 0)
        assertEquals(PlatformExecutionMode.THREAD, PlatformTaskRuntime.executionMode())
    }

    private class FakeTextGUI : TextGUI {
        override var theme: Theme? = null

        override val screen: Screen? = null

        override val focusedInteractable: Interactable? = null

        override val guiThread: TextGUIThread? = null

        override val isPendingUpdate: Boolean = false

        var processInputCount: Int = 0
            private set

        @Throws(IOException::class)
        override fun processInput(): Boolean {
            processInputCount += 1
            return false
        }

        @Throws(IOException::class)
        override fun updateScreen() {
            // no-op
        }

        override fun setVirtualScreenEnabled(virtualScreenEnabled: Boolean) {
            // no-op
        }

        override fun addListener(listener: TextGUI.Listener?) {
            // no-op
        }

        override fun removeListener(listener: TextGUI.Listener?) {
            // no-op
        }
    }
}
