package com.googlecode.lanterna.internal.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual class PlatformMutex actual constructor() {
    private val lock = ReentrantLock()

    actual fun <T> withLock(block: () -> T): T = lock.withLock(block)
}

actual class PlatformCountdownLatch actual constructor(initialCount: Int) {
    private val latch = CountDownLatch(initialCount)

    actual fun await() {
        latch.await()
    }

    actual fun await(timeoutMillis: Long): Boolean {
        if (timeoutMillis < 0) {
            return false
        }
        return latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
    }

    actual fun countDown() {
        latch.countDown()
    }
}

actual class PlatformThreadToken internal constructor(
    private val thread: Thread,
) {
    override fun equals(other: Any?): Boolean =
        other is PlatformThreadToken && other.thread === thread

    override fun hashCode(): Int = System.identityHashCode(thread)
}

actual fun currentThreadToken(): PlatformThreadToken = PlatformThreadToken(Thread.currentThread())

actual class PlatformThread actual constructor(
    name: String,
    block: () -> Unit,
) {
    private val thread: Thread = Thread(block, name)

    actual fun start() {
        thread.start()
    }
}

actual fun sleepCurrentThread(millis: Long) {
    if (millis <= 0) {
        return
    }
    try {
        Thread.sleep(millis)
    } catch (_: InterruptedException) {
        // ignore
    }
}
