package com.googlecode.lanterna.filesystem

actual class LanternaFile private constructor(
    private val rawPath: String,
    normalized: Boolean,
) {
    actual constructor(path: String) : this(normalizePath(path), true)

    actual constructor(parent: LanternaFile?, child: String) : this(
        when {
            parent == null -> normalizePath(child)
            child.isEmpty() -> parent.path
            parent.path.endsWith("\\") || parent.path.endsWith("/") -> normalizePath(parent.path + child)
            else -> normalizePath(parent.path + "\\" + child)
        },
        true,
    )

    actual val path: String
        get() = rawPath

    actual val name: String
        get() {
            val trimmed = rawPath.trimEnd('\\', '/')
            val slash = trimmed.lastIndexOfAny(charArrayOf('\\', '/'))
            return if (slash >= 0) trimmed.substring(slash + 1) else trimmed
        }

    actual val isAbsolute: Boolean
        get() {
            if (rawPath.length >= 3 && rawPath[1] == ':' && (rawPath[2] == '\\' || rawPath[2] == '/')) {
                return true
            }
            return rawPath.startsWith("\\\\") || rawPath.startsWith("/")
        }

    actual val absolutePath: String
        get() = if (isAbsolute) rawPath else normalizePath(".\\$rawPath")

    actual val absoluteFile: LanternaFile
        get() = LanternaFile(absolutePath)

    actual val parentFile: LanternaFile?
        get() {
            val trimmed = rawPath.trimEnd('\\', '/')
            val slash = trimmed.lastIndexOfAny(charArrayOf('\\', '/'))
            if (slash < 0) return null
            if (slash == 0) return LanternaFile(trimmed.substring(0, 1))
            return LanternaFile(trimmed.substring(0, slash))
        }

    // Mingw implementation only needs parity surface for now; filesystem probing is added separately.
    actual val isFile: Boolean
        get() = false

    actual val isDirectory: Boolean
        get() = false

    actual val isHidden: Boolean
        get() = name.startsWith(".")

    actual fun canRead(): Boolean = false

    actual fun listFiles(): Array<LanternaFile>? = null

    actual fun exists(): Boolean = false

    actual companion object {
        actual fun listRoots(): Array<LanternaFile> = arrayOf(LanternaFile("C:\\"))
    }
}

private fun normalizePath(path: String): String {
    if (path.isEmpty()) return "."
    return path.trimEnd('\\', '/').ifEmpty { "." }
}
