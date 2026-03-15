package com.googlecode.lanterna.internal.concurrency

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformEnvironmentVariable(name: String): String? {
    return getenv(name)?.toKString()
}

internal actual fun platformSystemProperty(name: String): String? {
    return null
}
