package com.googlecode.lanterna.internal.concurrency

expect class PlatformMutex() {
    fun <T> withLock(block: () -> T): T
}

expect class PlatformCountdownLatch(initialCount: Int = 1) {
    fun await()

    fun await(timeoutMillis: Long): Boolean

    fun countDown()
}

expect class PlatformThreadToken

expect fun currentThreadToken(): PlatformThreadToken

expect class PlatformThread(name: String, block: () -> Unit) {
    fun start()
}

expect fun sleepCurrentThread(millis: Long)
