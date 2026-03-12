package com.googlecode.lanterna.bundle

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

internal actual object BundleResourceLoader {
    actual fun loadTextResource(resourcePath: String): String? {
        val normalizedPath = normalizeResourcePath(resourcePath) ?: return null

        val classpathStream = LanternaThemes::class.java.classLoader?.getResourceAsStream(normalizedPath)
        if (classpathStream != null) {
            return classpathStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }

        val candidates = linkedSetOf<String>()
        val resourceDir = System.getenv("LANTERNA_RESOURCE_DIR")
        if (!resourceDir.isNullOrBlank()) {
            candidates += resolveResourcePath(resourceDir, normalizedPath)
        }
        val resourcePathList = System.getenv("LANTERNA_RESOURCE_PATHS")
        if (!resourcePathList.isNullOrBlank()) {
            resourcePathList.split(File.pathSeparatorChar)
                .filter { it.isNotBlank() }
                .forEach { candidates += resolveResourcePath(it, normalizedPath) }
        }

        candidates += normalizedPath
        candidates += "./$normalizedPath"

        for (candidate in candidates) {
            val path = Paths.get(candidate)
            if (!Files.isRegularFile(path)) {
                continue
            }
            return Files.readString(path, StandardCharsets.UTF_8)
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
