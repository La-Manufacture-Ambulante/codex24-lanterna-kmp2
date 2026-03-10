package com.googlecode.lanterna.filesystem

actual class LanternaFile private constructor(
    private val rawPath: String,
    @Suppress("UNUSED_PARAMETER") normalized: Boolean,
) {
    actual constructor(path: String) : this(if (path.isEmpty()) "." else path, true)

    actual constructor(parent: LanternaFile?, child: String) : this(
        when {
            parent == null -> child
            parent.path.endsWith("/") -> parent.path + child
            else -> parent.path + "/" + child
        },
        true,
    )

    actual val path: String
        get() = rawPath

    actual val name: String
        get() = rawPath.substringAfterLast('/').ifBlank { rawPath }

    actual val isAbsolute: Boolean
        get() = rawPath.startsWith("/") || rawPath.contains(':')

    actual val absolutePath: String
        get() = rawPath

    actual val absoluteFile: LanternaFile
        get() = this

    actual val parentFile: LanternaFile?
        get() {
            val parent = rawPath.substringBeforeLast('/', "")
            return if (parent.isEmpty()) null else LanternaFile(parent)
        }

    actual val isFile: Boolean
        get() = !rawPath.endsWith("/")

    actual val isDirectory: Boolean
        get() = rawPath.endsWith("/")

    actual val isHidden: Boolean
        get() = name.startsWith(".")

    actual fun canRead(): Boolean = true

    actual fun listFiles(): Array<LanternaFile>? = emptyArray()

    actual fun exists(): Boolean = true

    actual companion object {
        actual fun listRoots(): Array<LanternaFile> = arrayOf(LanternaFile("/"))
    }
}
