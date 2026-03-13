package com.googlecode.lanterna.internal.concurrency

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.nanosleep
import platform.posix.time_t
import platform.posix.timespec
import kotlin.time.TimeSource

actual class PlatformMutex actual constructor() {
    actual fun <T> withLock(block: () -> T): T = block()
}

actual class PlatformCountdownLatch actual constructor(
    initialCount: Int,
) {
    private var count: Int = if (initialCount < 0) 0 else initialCount

    actual fun await() {
        while (count > 0) {
            sleepCurrentThread(1)
        }
    }

    actual fun await(timeoutMillis: Long): Boolean {
        if (count <= 0) {
            return true
        }
        if (timeoutMillis < 0) {
            return false
        }
        val deadline = nowMillis() + timeoutMillis
        while (count > 0 && nowMillis() < deadline) {
            sleepCurrentThread(1)
        }
        return count <= 0
    }

    actual fun countDown() {
        if (count > 0) {
            count -= 1
        }
    }
}

actual class PlatformThreadToken internal constructor(
    private val id: Long,
) {
    override fun equals(other: Any?): Boolean = (other as? PlatformThreadToken)?.id == id

    override fun hashCode(): Int = id.hashCode()
}

actual fun currentThreadToken(): PlatformThreadToken = PlatformThreadToken(0L)

actual class PlatformThread actual constructor(
    name: String,
    block: () -> Unit,
) {
    private val runBlock: () -> Unit = block
    private var started: Boolean = false

    actual fun start() {
        if (started) {
            throw IllegalStateException("Thread already started")
        }
        started = true
        runBlock()
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun sleepCurrentThread(millis: Long) {
    if (millis <= 0) {
        return
    }
    memScoped {
        val request = alloc<timespec>()
        request.tv_sec = (millis / 1000).convert<time_t>()
        request.tv_nsec = ((millis % 1000) * 1_000_000).convert()
        nanosleep(request.ptr, null)
    }
}

private val monotonicStart = TimeSource.Monotonic.markNow()

private fun nowMillis(): Long = monotonicStart.elapsedNow().inWholeMilliseconds
