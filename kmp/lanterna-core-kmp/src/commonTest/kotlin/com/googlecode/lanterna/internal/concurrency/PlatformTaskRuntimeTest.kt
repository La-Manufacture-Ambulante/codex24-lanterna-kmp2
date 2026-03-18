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
    fun configureExecutionModeFromRuntimeConfigUsesPropertyOverride() {
        PlatformTaskRuntime.setPropertyLookupForTests { "thread" }
        PlatformTaskRuntime.setEnvironmentLookupForTests { "coroutine" }
        PlatformTaskRuntime.configureExecutionModeFromEnvironment()

        assertEquals(PlatformExecutionMode.THREAD, PlatformTaskRuntime.executionMode())
    }

    @Test
    fun configureExecutionModeFromEnvironmentFallsBackToThreadForUnknownValue() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)
        PlatformTaskRuntime.setPropertyLookupForTests { null }
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

    @Test
    fun backoffWaitUsesThreadWaitActionInThreadMode() {
        var threadWaitCount = 0
        var coroutineWaitCount = 0
        PlatformTaskRuntime.setThreadWaitActionForTests {
            threadWaitCount += 1
        }
        PlatformTaskRuntime.setCoroutineWaitActionForTests {
            coroutineWaitCount += 1
        }

        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.THREAD)
        PlatformTaskRuntime.backoffWait(5)

        assertEquals(1, threadWaitCount)
        assertEquals(0, coroutineWaitCount)
    }

    @Test
    fun backoffWaitUsesCoroutineWaitActionInCoroutineMode() {
        var threadWaitCount = 0
        var coroutineWaitCount = 0
        PlatformTaskRuntime.setThreadWaitActionForTests {
            threadWaitCount += 1
        }
        PlatformTaskRuntime.setCoroutineWaitActionForTests {
            coroutineWaitCount += 1
        }

        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)
        PlatformTaskRuntime.backoffWait(5)

        assertEquals(0, threadWaitCount)
        assertEquals(1, coroutineWaitCount)
    }

    @Test
    fun cancelIsBestEffortInThreadModeAndDoesNotForceCompletion() {
        val startedLatch = PlatformCountdownLatch(1)
        val releaseLatch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("thread-cancel") {
                startedLatch.countDown()
                releaseLatch.await()
            }

        assertTrue(startedLatch.await(2_000))
        handle.cancel()
        assertFalse(handle.awaitCompletion(20))

        releaseLatch.countDown()
        assertTrue(handle.awaitCompletion(2_000))
    }

    @Test
    fun cancelAfterCompletionIsSafeInCoroutineMode() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)
        val completedLatch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("coroutine-cancel-after-complete") {
                completedLatch.countDown()
            }

        assertTrue(completedLatch.await(2_000))
        assertTrue(handle.awaitCompletion(2_000))
        handle.cancel()
        assertTrue(handle.awaitCompletion(20))
    }

    @Test
    fun cooperativeCancelCheckpointStopsLoopInThreadMode() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.THREAD)
        val startedLatch = PlatformCountdownLatch(1)
        val stoppedLatch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("thread-cooperative-cancel") {
                startedLatch.countDown()
                while (PlatformTaskRuntime.cooperativeCancelCheckpoint()) {
                    PlatformTaskRuntime.backoffWait(1)
                }
                stoppedLatch.countDown()
            }

        assertTrue(startedLatch.await(2_000))
        handle.cancel()
        assertTrue(stoppedLatch.await(2_000))
        assertTrue(handle.awaitCompletion(2_000))
    }

    @Test
    fun cooperativeCancelCheckpointStopsLoopInCoroutineMode() {
        PlatformTaskRuntime.setExecutionMode(PlatformExecutionMode.COROUTINE)
        val startedLatch = PlatformCountdownLatch(1)
        val stoppedLatch = PlatformCountdownLatch(1)
        val handle =
            PlatformTaskRuntime.launch("coroutine-cooperative-cancel") {
                startedLatch.countDown()
                while (PlatformTaskRuntime.cooperativeCancelCheckpoint()) {
                    PlatformTaskRuntime.backoffWait(1)
                }
                stoppedLatch.countDown()
            }

        assertTrue(startedLatch.await(2_000))
        handle.cancel()
        assertTrue(stoppedLatch.await(2_000))
        assertTrue(handle.awaitCompletion(2_000))
    }
}
