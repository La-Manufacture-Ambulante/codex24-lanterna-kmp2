package com.googlecode.lanterna.filesystem

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import platform.posix.R_OK
import platform.posix.S_IFDIR
import platform.posix.S_IFMT
import platform.posix.S_IFREG
import platform.posix.access
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.getcwd
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.stat

actual class LanternaFile private constructor(
    private val rawPath: String,
    @Suppress("UNUSED_PARAMETER") normalized: Boolean,
) {
    actual constructor(path: String) : this(normalizePath(path), true)

    actual constructor(parent: LanternaFile?, child: String) : this(
        when {
            parent == null -> normalizePath(child)
            child.startsWith("/") -> normalizePath(child)
            parent.path.endsWith("/") -> normalizePath(parent.path + child)
            else -> normalizePath(parent.path + "/" + child)
        },
        true,
    )

    actual val path: String
        get() = rawPath

    actual val name: String
        get() {
            val trimmed = if (rawPath == "/") rawPath else rawPath.trimEnd('/')
            return trimmed.substringAfterLast('/', "")
        }

    actual val isAbsolute: Boolean
        get() = rawPath.startsWith("/")

    actual val absolutePath: String
        get() {
            if (isAbsolute) {
                return rawPath
            }
            val cwd = currentWorkingDirectory() ?: return rawPath
            return if (rawPath == ".") cwd else normalizePath("$cwd/$rawPath")
        }

    actual val absoluteFile: LanternaFile
        get() = LanternaFile(absolutePath)

    actual val parentFile: LanternaFile?
        get() {
            val trimmed = if (rawPath == "/") rawPath else rawPath.trimEnd('/')
            if (trimmed == "/") {
                return null
            }
            val separator = trimmed.lastIndexOf('/')
            return when {
                separator < 0 -> null
                separator == 0 -> LanternaFile("/")
                else -> LanternaFile(trimmed.substring(0, separator))
            }
        }

    actual val isFile: Boolean
        get() = modeMatches(pathMode(rawPath), S_IFREG.toULong())

    actual val isDirectory: Boolean
        get() = modeMatches(pathMode(rawPath), S_IFDIR.toULong())

    actual val isHidden: Boolean
        get() {
            val currentName = name
            return currentName.startsWith(".") && currentName != "." && currentName != ".."
        }

    actual fun canRead(): Boolean = access(rawPath, R_OK) == 0

    @OptIn(ExperimentalForeignApi::class)
    actual fun listFiles(): Array<LanternaFile>? {
        if (!isDirectory) {
            return null
        }
        val directory = opendir(rawPath) ?: return null
        return try {
            val entries = mutableListOf<LanternaFile>()
            while (true) {
                val pointer = readdir(directory) ?: break
                val entry = pointer.pointed
                val childName = entry.name()
                if (childName == "." || childName == "..") {
                    continue
                }
                entries += LanternaFile(this, childName)
            }
            entries.toTypedArray()
        } finally {
            closedir(directory)
        }
    }

    actual fun exists(): Boolean = access(rawPath, F_OK) == 0

    actual companion object {
        actual fun listRoots(): Array<LanternaFile> = arrayOf(LanternaFile("/"))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun currentWorkingDirectory(): String? =
    memScoped {
        val bufferSize = 4096
        val buffer = allocArray<ByteVar>(bufferSize)
        getcwd(buffer, bufferSize.toULong())?.toKString()
    }

private fun normalizePath(path: String): String {
    if (path.isEmpty()) {
        return "."
    }
    if (path == "/") {
        return path
    }
    return path.trimEnd('/').ifEmpty { "/" }
}

@OptIn(ExperimentalForeignApi::class)
private fun pathMode(path: String): ULong? =
    memScoped {
        val metadata = alloc<stat>()
        if (stat(path, metadata.ptr) != 0) {
            null
        } else {
            metadata.st_mode.toULong()
        }
    }

private fun modeMatches(
    mode: ULong?,
    expectedType: ULong,
): Boolean {
    if (mode == null) {
        return false
    }
    return (mode and S_IFMT.toULong()) == expectedType
}

@OptIn(ExperimentalForeignApi::class)
private fun dirent.name(): String = d_name.toKString()
