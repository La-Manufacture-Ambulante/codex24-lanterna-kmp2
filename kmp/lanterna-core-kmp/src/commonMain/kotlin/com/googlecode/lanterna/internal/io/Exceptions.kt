package com.googlecode.lanterna.internal.io

open class IOException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

class EOFException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
