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

/**
 * Native resource loading is environment-driven (`LANTERNA_RESOURCE_DIR` / `LANTERNA_RESOURCE_PATHS`) with a small
 * relative-path fallback, avoiding hard-coded build-layout probing.
 */
internal actual object BundleResourceLoader {
    @OptIn(ExperimentalForeignApi::class)
    actual fun loadTextResource(resourcePath: String): String? {
        val normalizedPath = normalizeResourcePath(resourcePath) ?: return null
        val searchPaths = linkedSetOf<String>()

        val resourceDir = getenv("LANTERNA_RESOURCE_DIR")?.toKString()
        if (!resourceDir.isNullOrBlank()) {
            searchPaths += resolveResourcePath(resourceDir, normalizedPath)
        }
        val resourcePathList = getenv("LANTERNA_RESOURCE_PATHS")?.toKString()
        if (!resourcePathList.isNullOrBlank()) {
            resourcePathList.split(':')
                .filter { it.isNotBlank() }
                .forEach { searchPaths += resolveResourcePath(it, normalizedPath) }
        }

        searchPaths += normalizedPath
        searchPaths += "./$normalizedPath"

        for (candidate in searchPaths) {
            val loaded = readUtf8File(candidate)
            if (loaded != null) {
                return loaded
            }
        }
        return null
    }
}

private fun normalizeResourcePath(resourcePath: String): String? {
    if (resourcePath.isBlank()) {
        return null
    }
    return resourcePath.trimStart('/').takeIf { it.isNotBlank() }
}

private fun resolveResourcePath(
    basePath: String,
    normalizedPath: String,
): String {
    val trimmedBasePath = basePath.trim()
    return if (trimmedBasePath.endsWith("/")) {
        "$trimmedBasePath$normalizedPath"
    } else {
        "$trimmedBasePath/$normalizedPath"
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
            val readCount =
                chunk.usePinned { pinned ->
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
