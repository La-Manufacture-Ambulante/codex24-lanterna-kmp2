package com.googlecode.lanterna.internal.concurrency

internal expect fun platformEnvironmentVariable(name: String): String?

internal expect fun platformSystemProperty(name: String): String?
