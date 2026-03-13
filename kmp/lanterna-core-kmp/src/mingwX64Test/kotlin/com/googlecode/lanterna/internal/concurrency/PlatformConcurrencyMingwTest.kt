package com.googlecode.lanterna.internal.concurrency

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlatformConcurrencyMingwTest {
    @Test
    fun threadTokenDiffersAcrossThreads() {
        val current = currentThreadToken()
        val latch = PlatformCountdownLatch(1)
        var worker: PlatformThreadToken? = null
        PlatformThread("mingw-token-worker") {
            worker = currentThreadToken()
            latch.countDown()
        }.start()
        assertTrue(latch.await(2_000))
        assertNotEquals(current, worker)
    }
}
