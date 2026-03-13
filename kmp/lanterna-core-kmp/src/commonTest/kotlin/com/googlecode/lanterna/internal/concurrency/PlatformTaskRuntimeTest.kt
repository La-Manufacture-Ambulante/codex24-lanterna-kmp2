package com.googlecode.lanterna.internal.concurrency

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformTaskRuntimeTest {
    @AfterTest
    fun resetExecutionMode() {
        PlatformTaskRuntime.resetForTests()
    }

    @Test
    fun defaultExecutionModeIsThread() {
        assertEquals(PlatformExecutionMode.THREAD, PlatformTaskRuntime.executionMode())
    }

    @Test
    fun launchUsesThreadModeByDefault() {
        val latch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("thread-default") {
                latch.countDown()
            }

        assertTrue(latch.await(2_000))
        assertTrue(handle.awaitCompletion(2_000))
    }

    @Test
    fun launchUsesCoroutineModeWhenEnabled() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)

        val latch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("coroutine-enabled") {
                latch.countDown()
            }

        assertTrue(latch.await(2_000))
        assertTrue(handle.awaitCompletion(2_000))
    }

    @Test
    fun parseExecutionModeAcceptsKnownValues() {
        assertEquals(PlatformExecutionMode.THREAD, PlatformTaskRuntime.parseExecutionModeForTest("thread"))
        assertEquals(PlatformExecutionMode.COROUTINE, PlatformTaskRuntime.parseExecutionModeForTest("coroutine"))
        assertEquals(PlatformExecutionMode.COROUTINE, PlatformTaskRuntime.parseExecutionModeForTest("  CoRoUtInE  "))
    }

    @Test
    fun parseExecutionModeRejectsUnknownValues() {
        assertEquals(null, PlatformTaskRuntime.parseExecutionModeForTest(null))
        assertEquals(null, PlatformTaskRuntime.parseExecutionModeForTest(""))
        assertEquals(null, PlatformTaskRuntime.parseExecutionModeForTest("pthread"))
    }

    @Test
    fun configureExecutionModeFromEnvironmentEnablesCoroutineMode() {
        PlatformTaskRuntime.setEnvironmentLookupForTests { "coroutine" }
        PlatformTaskRuntime.configureExecutionModeFromEnvironment()

        assertEquals(PlatformExecutionMode.COROUTINE, PlatformTaskRuntime.executionMode())
    }

    @Test
    fun configureExecutionModeFromEnvironmentFallsBackToThreadForUnknownValue() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)
        PlatformTaskRuntime.setEnvironmentLookupForTests { "unexpected" }
        PlatformTaskRuntime.configureExecutionModeFromEnvironment()

        assertEquals(PlatformExecutionMode.THREAD, PlatformTaskRuntime.executionMode())
    }

    @Test
    fun awaitCompletionTimesOutWhenTaskStillRunningThenCompletes() {
        val releaseLatch = PlatformCountdownLatch(1)
        val startedLatch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("await-timeout") {
                startedLatch.countDown()
                releaseLatch.await()
            }

        assertTrue(startedLatch.await(2_000))
        assertFalse(handle.awaitCompletion(10))

        releaseLatch.countDown()
        assertTrue(handle.awaitCompletion(2_000))
    }
}
