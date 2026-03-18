package com.googlecode.lanterna.internal.concurrency

internal actual fun platformEnvironmentVariable(name: String): String? {
    return java.lang.System.getenv(name)
}

internal actual fun platformSystemProperty(name: String): String? {
    return java.lang.System.getProperty(name)
}
