package com.googlecode.lanterna.internal.concurrency

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import platform.posix.CLOCK_REALTIME
import platform.posix.ETIMEDOUT
import platform.posix.clock_gettime
import platform.posix.nanosleep
import platform.posix.pthread_cond_broadcast
import platform.posix.pthread_cond_destroy
import platform.posix.pthread_cond_init
import platform.posix.pthread_cond_t
import platform.posix.pthread_cond_timedwait
import platform.posix.pthread_cond_wait
import platform.posix.pthread_create
import platform.posix.pthread_detach
import platform.posix.pthread_equal
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_self
import platform.posix.pthread_t
import platform.posix.pthread_tVar
import platform.posix.timespec
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalForeignApi::class)
actual class PlatformMutex actual constructor() {
    private val native = NativeMutex()

    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(native) { it.dispose() }

    actual fun <T> withLock(block: () -> T): T {
        pthread_mutex_lock(native.mutex.ptr)
        return try {
            block()
        } finally {
            pthread_mutex_unlock(native.mutex.ptr)
        }
    }
}

actual class PlatformCountdownLatch actual constructor(
    initialCount: Int,
) {
    private val native = NativeLatch(initialCount)

    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(native) { it.dispose() }

    actual fun await() {
        native.await()
    }

    actual fun await(timeoutMillis: Long): Boolean {
        if (timeoutMillis < 0) {
            return false
        }
        return native.await(timeoutMillis)
    }

    actual fun countDown() {
        native.countDown()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class PlatformThreadToken internal constructor(
    private val threadId: pthread_t?,
) {
    override fun equals(other: Any?): Boolean {
        val otherToken = other as? PlatformThreadToken ?: return false
        val thisThread = threadId ?: return false
        val otherThread = otherToken.threadId ?: return false
        return pthread_equal(thisThread, otherThread) != 0
    }

    override fun hashCode(): Int = threadId?.hashCode() ?: 0
}

@OptIn(ExperimentalForeignApi::class)
actual fun currentThreadToken(): PlatformThreadToken = PlatformThreadToken(pthread_self())

@OptIn(ExperimentalForeignApi::class)
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

        val payload = StableRef.create(runBlock).asCPointer()
        memScoped {
            val threadId = alloc<pthread_tVar>()
            val result =
                pthread_create(
                    threadId.ptr,
                    null,
                    THREAD_ENTRY,
                    payload,
                )
            if (result != 0) {
                payload.asStableRef<() -> Unit>().dispose()
                started = false
                throw IllegalStateException("pthread_create failed: $result")
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun sleepCurrentThread(millis: Long) {
    if (millis <= 0) {
        return
    }
    memScoped {
        val request = alloc<timespec>()
        request.tv_sec = (millis / 1000).convert()
        request.tv_nsec = ((millis % 1000) * 1_000_000).convert()
        nanosleep(request.ptr, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private val THREAD_ENTRY =
    staticCFunction<COpaquePointer?, COpaquePointer?> { argument ->
        val selfThread = pthread_self()
        if (selfThread != null) {
            pthread_detach(selfThread)
        }
        if (argument == null) {
            return@staticCFunction null
        }
        val blockRef = argument.asStableRef<() -> Unit>()
        try {
            blockRef.get().invoke()
        } finally {
            blockRef.dispose()
        }
        null
    }

@OptIn(ExperimentalForeignApi::class)
private class NativeMutex {
    val mutex: pthread_mutex_t = nativeHeap.alloc<pthread_mutex_t>()

    init {
        pthread_mutex_init(mutex.ptr, null)
    }

    fun dispose() {
        pthread_mutex_destroy(mutex.ptr)
        nativeHeap.free(mutex.rawPtr)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class NativeLatch(initialCount: Int) {
    val mutex: pthread_mutex_t = nativeHeap.alloc<pthread_mutex_t>()
    val condition: pthread_cond_t = nativeHeap.alloc<pthread_cond_t>()
    private var count: Int = if (initialCount < 0) 0 else initialCount

    init {
        pthread_mutex_init(mutex.ptr, null)
        pthread_cond_init(condition.ptr, null)
    }

    fun await() {
        pthread_mutex_lock(mutex.ptr)
        try {
            while (count > 0) {
                pthread_cond_wait(condition.ptr, mutex.ptr)
            }
        } finally {
            pthread_mutex_unlock(mutex.ptr)
        }
    }

    fun await(timeoutMillis: Long): Boolean {
        pthread_mutex_lock(mutex.ptr)
        try {
            if (count <= 0) {
                return true
            }
            memScoped {
                val deadline = alloc<timespec>()
                clock_gettime(CLOCK_REALTIME.convert(), deadline.ptr)
                addMillis(deadline, timeoutMillis)

                while (count > 0) {
                    val waitResult = pthread_cond_timedwait(condition.ptr, mutex.ptr, deadline.ptr)
                    if (waitResult == ETIMEDOUT) {
                        break
                    }
                }
            }
            return count <= 0
        } finally {
            pthread_mutex_unlock(mutex.ptr)
        }
    }

    fun countDown() {
        pthread_mutex_lock(mutex.ptr)
        try {
            if (count > 0) {
                count -= 1
                if (count == 0) {
                    pthread_cond_broadcast(condition.ptr)
                }
            }
        } finally {
            pthread_mutex_unlock(mutex.ptr)
        }
    }

    fun dispose() {
        pthread_cond_destroy(condition.ptr)
        pthread_mutex_destroy(mutex.ptr)
        nativeHeap.free(condition.rawPtr)
        nativeHeap.free(mutex.rawPtr)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun addMillis(
    time: timespec,
    timeoutMillis: Long,
) {
    val secondDelta = timeoutMillis / 1000
    val nanoDelta = (timeoutMillis % 1000) * 1_000_000
    val mergedNanos = time.tv_nsec.toLong() + nanoDelta
    time.tv_sec = (time.tv_sec.toLong() + secondDelta + (mergedNanos / 1_000_000_000L)).convert()
    time.tv_nsec = (mergedNanos % 1_000_000_000L).convert()
}
