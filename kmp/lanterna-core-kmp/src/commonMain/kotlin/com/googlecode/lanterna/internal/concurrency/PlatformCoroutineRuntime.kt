package com.googlecode.lanterna.internal.concurrency

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

enum class PlatformExecutionMode {
    THREAD,
    COROUTINE,
}

class PlatformTaskHandle internal constructor(
    private val cancelAction: () -> Unit,
    private val awaitAction: (Long) -> Boolean,
) {
    fun cancel() {
        cancelAction()
    }

    fun awaitCompletion(timeoutMillis: Long): Boolean {
        if (timeoutMillis < 0) {
            return false
        }
        return awaitAction(timeoutMillis)
    }
}

object PlatformTaskRuntime {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val runtimeStateMutex = PlatformMutex()

    private const val EXECUTION_MODE_SYSTEM_PROPERTY = "lanterna.execution.mode"
    private const val EXECUTION_MODE_ENVIRONMENT_VARIABLE = "LANTERNA_EXECUTION_MODE"

    private val defaultThreadWaitAction: (Long) -> Unit = ::sleepCurrentThread
    private val defaultCoroutineWaitAction: suspend (Long) -> Unit = { delay(it) }
    private val defaultPropertyLookup: (String) -> String? = ::platformSystemProperty
    private val defaultEnvironmentLookup: (String) -> String? = ::platformEnvironmentVariable
    private var threadWaitAction: (Long) -> Unit = defaultThreadWaitAction
    private var coroutineWaitAction: suspend (Long) -> Unit = defaultCoroutineWaitAction
    private var propertyLookup: (String) -> String? = defaultPropertyLookup
    private var environmentLookup: (String) -> String? = defaultEnvironmentLookup

    private var currentMode: PlatformExecutionMode = resolvedModeFromRuntimeConfig() ?: PlatformExecutionMode.THREAD

    fun executionMode(): PlatformExecutionMode = runtimeStateMutex.withLock { currentMode }

    fun setExecutionMode(mode: PlatformExecutionMode) {
        runtimeStateMutex.withLock {
            currentMode = mode
        }
    }

    fun configureExecutionModeFromEnvironment() {
        runtimeStateMutex.withLock {
            currentMode = resolvedModeFromRuntimeConfig() ?: PlatformExecutionMode.THREAD
        }
    }

    fun launch(
        name: String,
        block: () -> Unit,
    ): PlatformTaskHandle {
        val mode =
            runtimeStateMutex.withLock {
                currentMode
            }
        return when (mode) {
            PlatformExecutionMode.THREAD -> launchThread(name, block)
            PlatformExecutionMode.COROUTINE -> launchCoroutine(name, block)
        }
    }

    fun backoffWait(millis: Long) {
        if (millis <= 0) {
            return
        }
        val mode =
            runtimeStateMutex.withLock {
                currentMode
            }
        when (mode) {
            PlatformExecutionMode.THREAD -> {
                try {
                    threadWaitAction(millis)
                } catch (_: Throwable) {
                    // Keep legacy InputDecoder behavior: ignore interrupted/backoff exceptions.
                }
            }

            PlatformExecutionMode.COROUTINE -> {
                try {
                    runBlocking {
                        coroutineWaitAction(millis)
                    }
                } catch (_: Throwable) {
                    // Keep legacy InputDecoder behavior: ignore interrupted/backoff exceptions.
                }
            }
        }
    }

    internal fun resetForTests() {
        runtimeStateMutex.withLock {
            currentMode = PlatformExecutionMode.THREAD
            threadWaitAction = defaultThreadWaitAction
            coroutineWaitAction = defaultCoroutineWaitAction
            propertyLookup = defaultPropertyLookup
            environmentLookup = defaultEnvironmentLookup
        }
    }

    internal fun shutdownForTests() {
        coroutineScope.cancel()
    }

    private fun launchThread(
        name: String,
        block: () -> Unit,
    ): PlatformTaskHandle {
        val done = PlatformCountdownLatch(1)
        val thread =
            PlatformThread(name) {
                try {
                    block()
                } finally {
                    done.countDown()
                }
            }
        thread.start()
        return PlatformTaskHandle(cancelAction = {}, awaitAction = { timeout -> done.await(timeout) })
    }

    private fun launchCoroutine(
        name: String,
        block: () -> Unit,
    ): PlatformTaskHandle {
        val job: Job =
            coroutineScope.launch(CoroutineName(name)) {
                block()
            }
        return PlatformTaskHandle(
            cancelAction = { job.cancel() },
            awaitAction = { timeout -> awaitJobCompletion(job, timeout) },
        )
    }

    private fun awaitJobCompletion(
        job: Job,
        timeoutMillis: Long,
    ): Boolean {
        if (job.isCompleted) {
            return true
        }
        if (timeoutMillis == 0L) {
            return false
        }
        return runBlocking {
            withTimeoutOrNull(timeoutMillis) {
                job.join()
                true
            } ?: false
        }
    }

    internal fun parseExecutionModeForTest(raw: String?): PlatformExecutionMode? {
        return parseExecutionMode(raw)
    }

    internal fun setEnvironmentLookupForTests(lookup: (String) -> String?) {
        runtimeStateMutex.withLock {
            environmentLookup = lookup
        }
    }

    internal fun setPropertyLookupForTests(lookup: (String) -> String?) {
        runtimeStateMutex.withLock {
            propertyLookup = lookup
        }
    }

    internal fun setThreadWaitActionForTests(action: (Long) -> Unit) {
        runtimeStateMutex.withLock {
            threadWaitAction = action
        }
    }

    internal fun setCoroutineWaitActionForTests(action: suspend (Long) -> Unit) {
        runtimeStateMutex.withLock {
            coroutineWaitAction = action
        }
    }

    private fun resolvedModeFromRuntimeConfig(): PlatformExecutionMode? {
        return parseExecutionMode(propertyLookup(EXECUTION_MODE_SYSTEM_PROPERTY))
            ?: parseExecutionMode(environmentLookup(EXECUTION_MODE_ENVIRONMENT_VARIABLE))
    }

    private fun parseExecutionMode(raw: String?): PlatformExecutionMode? {
        val normalized = raw?.trim()?.lowercase() ?: return null
        return when (normalized) {
            "thread" -> PlatformExecutionMode.THREAD
            "coroutine" -> PlatformExecutionMode.COROUTINE
            else -> null
        }
    }
}
