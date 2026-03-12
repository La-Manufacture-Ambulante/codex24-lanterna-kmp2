package com.googlecode.lanterna.internal.concurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class PlatformConcurrencyTest {
    @Test
    fun currentThreadTokenIsStableOnCurrentThread() {
        val first = currentThreadToken()
        val second = currentThreadToken()
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun countdownLatchTimesOutWithoutCountDown() {
        val latch = PlatformCountdownLatch(1)
        assertFalse(latch.await(40))
    }

    @Test
    fun platformThreadRunsBlock() {
        val latch = PlatformCountdownLatch(1)
        PlatformThread("platform-thread-runs") {
            latch.countDown()
        }.start()
        assertTrue(latch.await(2_000))
    }

    @Test
    fun countdownLatchReleasesFromAnotherThread() {
        val latch = PlatformCountdownLatch(1)
        PlatformThread("platform-thread-release") {
            sleepCurrentThread(20)
            latch.countDown()
        }.start()
        assertTrue(latch.await(2_000))
    }

    @Test
    fun startingPlatformThreadTwiceThrows() {
        val latch = PlatformCountdownLatch(1)
        val thread = PlatformThread("platform-thread-single-start") {
            latch.countDown()
        }
        thread.start()
        assertTrue(latch.await(2_000))
        val thrown =
            try {
            thread.start()
            null
            } catch (error: Throwable) {
                error
            }
        if (thrown == null) {
            fail("Expected second PlatformThread.start() call to fail, but it succeeded")
        }
    }

    @Test
    fun mutexProtectsCriticalSection() {
        val mutex = PlatformMutex()
        var value = 0
        mutex.withLock {
            value += 1
        }
        assertEquals(1, value)
    }
}
