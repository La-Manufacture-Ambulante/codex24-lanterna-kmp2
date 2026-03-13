package com.googlecode.lanterna.internal.concurrency

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    private var currentMode: PlatformExecutionMode = PlatformExecutionMode.THREAD

    fun executionMode(): PlatformExecutionMode = currentMode

    fun setExecutionMode(mode: PlatformExecutionMode) {
        currentMode = mode
    }

    fun launch(
        name: String,
        block: () -> Unit,
    ): PlatformTaskHandle {
        return when (currentMode) {
            PlatformExecutionMode.THREAD -> launchThread(name, block)
            PlatformExecutionMode.COROUTINE -> launchCoroutine(name, block)
        }
    }

    internal fun resetForTests() {
        currentMode = PlatformExecutionMode.THREAD
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
}
