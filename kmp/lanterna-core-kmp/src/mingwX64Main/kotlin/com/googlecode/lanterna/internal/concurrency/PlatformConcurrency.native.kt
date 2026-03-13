package com.googlecode.lanterna.internal.concurrency

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import platform.windows.CONDITION_VARIABLE
import platform.windows.CRITICAL_SECTION
import platform.windows.CloseHandle
import platform.windows.CreateThread
import platform.windows.DeleteCriticalSection
import platform.windows.ERROR_TIMEOUT
import platform.windows.EnterCriticalSection
import platform.windows.GetCurrentThreadId
import platform.windows.GetLastError
import platform.windows.INFINITE
import platform.windows.InitializeConditionVariable
import platform.windows.InitializeCriticalSection
import platform.windows.LeaveCriticalSection
import platform.windows.Sleep
import platform.windows.SleepConditionVariableCS
import platform.windows.WakeAllConditionVariable
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import kotlin.time.TimeSource

@OptIn(ExperimentalForeignApi::class)
actual class PlatformMutex actual constructor() {
    private val native = NativeCriticalSection()

    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(native) { it.dispose() }

    actual fun <T> withLock(block: () -> T): T {
        EnterCriticalSection(native.criticalSection.ptr)
        return try {
            block()
        } finally {
            LeaveCriticalSection(native.criticalSection.ptr)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
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

actual class PlatformThreadToken internal constructor(
    private val id: UInt,
) {
    override fun equals(other: Any?): Boolean = (other as? PlatformThreadToken)?.id == id

    override fun hashCode(): Int = id.hashCode()
}

actual fun currentThreadToken(): PlatformThreadToken = PlatformThreadToken(GetCurrentThreadId())

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
        val handle = CreateThread(null, 0u, THREAD_ENTRY, payload, 0u, null)
        if (handle == null) {
            payload.asStableRef<() -> Unit>().dispose()
            started = false
            throw IllegalStateException("CreateThread failed")
        }
        CloseHandle(handle)
    }
}

actual fun sleepCurrentThread(millis: Long) {
    if (millis <= 0) {
        return
    }
    Sleep(millis.toUInt())
}

@OptIn(ExperimentalForeignApi::class)
private val THREAD_ENTRY =
    staticCFunction<COpaquePointer?, UInt> { argument ->
        if (argument == null) {
            return@staticCFunction 0u
        }
        val blockRef = argument.asStableRef<() -> Unit>()
        try {
            blockRef.get().invoke()
        } finally {
            blockRef.dispose()
        }
        0u
    }

@OptIn(ExperimentalForeignApi::class)
private class NativeCriticalSection {
    val criticalSection: CRITICAL_SECTION = nativeHeap.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(criticalSection.ptr)
    }

    fun dispose() {
        DeleteCriticalSection(criticalSection.ptr)
        nativeHeap.free(criticalSection.rawPtr)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class NativeLatch(initialCount: Int) {
    private val lock = NativeCriticalSection()
    private val condition: CONDITION_VARIABLE = nativeHeap.alloc<CONDITION_VARIABLE>()
    private var count: Int = if (initialCount < 0) 0 else initialCount

    init {
        InitializeConditionVariable(condition.ptr)
    }

    fun await() {
        EnterCriticalSection(lock.criticalSection.ptr)
        try {
            while (count > 0) {
                SleepConditionVariableCS(condition.ptr, lock.criticalSection.ptr, INFINITE)
            }
        } finally {
            LeaveCriticalSection(lock.criticalSection.ptr)
        }
    }

    fun await(timeoutMillis: Long): Boolean {
        EnterCriticalSection(lock.criticalSection.ptr)
        try {
            if (count <= 0) {
                return true
            }
            var remaining = timeoutMillis
            val mark = TimeSource.Monotonic.markNow()
            while (count > 0) {
                val waited =
                    SleepConditionVariableCS(
                        condition.ptr,
                        lock.criticalSection.ptr,
                        remaining.coerceAtMost(UInt.MAX_VALUE.toLong()).convert(),
                    )
                if (waited == 0 && GetLastError() == ERROR_TIMEOUT.toUInt()) {
                    break
                }
                val elapsed = mark.elapsedNow().inWholeMilliseconds
                remaining = timeoutMillis - elapsed
                if (remaining <= 0) {
                    break
                }
            }
            return count <= 0
        } finally {
            LeaveCriticalSection(lock.criticalSection.ptr)
        }
    }

    fun countDown() {
        EnterCriticalSection(lock.criticalSection.ptr)
        try {
            if (count > 0) {
                count -= 1
                if (count == 0) {
                    WakeAllConditionVariable(condition.ptr)
                }
            }
        } finally {
            LeaveCriticalSection(lock.criticalSection.ptr)
        }
    }

    fun dispose() {
        lock.dispose()
        nativeHeap.free(condition.rawPtr)
    }
}
