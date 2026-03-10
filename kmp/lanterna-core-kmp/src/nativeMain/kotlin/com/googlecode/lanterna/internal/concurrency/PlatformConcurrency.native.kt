package com.googlecode.lanterna.internal.concurrency

actual class PlatformMutex actual constructor() {
    actual fun <T> withLock(block: () -> T): T = block()
}

actual class PlatformCountdownLatch actual constructor(
    initialCount: Int,
) {
    private var count: Int = if (initialCount < 0) 0 else initialCount

    actual fun await() {
        // Placeholder native implementation; replaced by true primitive in a later slice.
    }

    actual fun await(timeoutMillis: Long): Boolean {
        return count == 0 || timeoutMillis >= 0
    }

    actual fun countDown() {
        if (count > 0) {
            count -= 1
        }
    }
}

actual class PlatformThreadToken internal constructor(
    private val id: Int,
)

actual fun currentThreadToken(): PlatformThreadToken = PlatformThreadToken(0)

actual class PlatformThread actual constructor(
    name: String,
    block: () -> Unit,
) {
    private val runBlock: () -> Unit = block

    actual fun start() {
        // Placeholder native implementation; replaced by true primitive in a later slice.
        runBlock()
    }
}

actual fun sleepCurrentThread(millis: Long) {
    // Placeholder native implementation.
}
