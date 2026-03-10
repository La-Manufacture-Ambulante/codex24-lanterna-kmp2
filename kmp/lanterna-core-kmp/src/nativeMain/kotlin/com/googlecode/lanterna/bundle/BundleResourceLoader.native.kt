package com.googlecode.lanterna.bundle

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.getenv
import platform.posix.open
import platform.posix.read

internal actual object BundleResourceLoader {
    @OptIn(ExperimentalForeignApi::class)
    actual fun loadTextResource(resourcePath: String): String? {
        if (resourcePath.isBlank()) {
            return null
        }
        val normalizedPath = resourcePath.trimStart('/')
        val searchPaths = linkedSetOf<String>()

        val resourceDir = getenv("LANTERNA_RESOURCE_DIR")?.toKString()
        if (!resourceDir.isNullOrBlank()) {
            searchPaths += "$resourceDir/$normalizedPath"
        }
        val resourcePathList = getenv("LANTERNA_RESOURCE_PATHS")?.toKString()
        if (!resourcePathList.isNullOrBlank()) {
            resourcePathList.split(':')
                .filter { it.isNotBlank() }
                .forEach { searchPaths += "${it.trimEnd('/')}/$normalizedPath" }
        }

        searchPaths += normalizedPath
        searchPaths += "./$normalizedPath"
        searchPaths += "src/main/resources/$normalizedPath"
        searchPaths += "../src/main/resources/$normalizedPath"
        searchPaths += "../../src/main/resources/$normalizedPath"
        searchPaths += "lanterna-mabe02/src/main/resources/$normalizedPath"

        for (candidate in searchPaths) {
            val loaded = readUtf8File(candidate)
            if (loaded != null) {
                return loaded
            }
        }
        return null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readUtf8File(path: String): String? {
    val descriptor = open(path, O_RDONLY)
    if (descriptor < 0) {
        return null
    }
    return try {
        val bytes = ArrayList<Byte>()
        val chunk = ByteArray(4096)
        while (true) {
            val readCount = chunk.usePinned { pinned ->
                read(descriptor, pinned.addressOf(0), chunk.size.convert())
            }
            if (readCount < 0) {
                return null
            }
            if (readCount == 0L) {
                break
            }
            val length = readCount.toInt()
            for (index in 0 until length) {
                bytes += chunk[index]
            }
        }
        bytes.toByteArray().decodeToString()
    } finally {
        close(descriptor)
    }
}
