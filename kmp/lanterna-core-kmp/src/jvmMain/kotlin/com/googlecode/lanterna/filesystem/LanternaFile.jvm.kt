package com.googlecode.lanterna.filesystem

actual class LanternaFile private constructor(
    internal val delegate: java.io.File,
) {
    actual constructor(path: String) : this(java.io.File(path))

    actual constructor(parent: LanternaFile?, child: String) : this(
        if (parent == null) java.io.File(child) else java.io.File(parent.delegate, child),
    )

    actual val path: String
        get() = delegate.path

    actual val name: String
        get() = delegate.name

    actual val isAbsolute: Boolean
        get() = delegate.isAbsolute

    actual val absolutePath: String
        get() = delegate.absolutePath

    actual val absoluteFile: LanternaFile
        get() = LanternaFile(delegate.absoluteFile)

    actual val parentFile: LanternaFile?
        get() = delegate.parentFile?.let { LanternaFile(it) }

    actual val isFile: Boolean
        get() = delegate.isFile

    actual val isDirectory: Boolean
        get() = delegate.isDirectory

    actual val isHidden: Boolean
        get() = delegate.isHidden

    actual fun canRead(): Boolean = delegate.canRead()

    actual fun listFiles(): Array<LanternaFile>? {
        return delegate.listFiles()?.map { LanternaFile(it) }?.toTypedArray()
    }

    actual fun exists(): Boolean = delegate.exists()

    actual companion object {
        actual fun listRoots(): Array<LanternaFile> {
            return java.io.File.listRoots().map { LanternaFile(it) }.toTypedArray()
        }
    }
}
