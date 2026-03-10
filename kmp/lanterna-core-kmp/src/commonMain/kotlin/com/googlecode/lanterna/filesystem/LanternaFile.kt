package com.googlecode.lanterna.filesystem

/**
 * Cross-platform filesystem entry abstraction for common dialog code.
 *
 * This intentionally mirrors the minimal `java.io.File`-like API used by Lanterna dialogs instead of pulling in a
 * new public dependency such as Okio. If we decide to expose Okio paths later, we can add adapters without changing
 * this parity surface.
 */
expect class LanternaFile {
    constructor(path: String)
    constructor(parent: LanternaFile?, child: String)

    val path: String
    val name: String
    val isAbsolute: Boolean
    val absolutePath: String
    val absoluteFile: LanternaFile
    val parentFile: LanternaFile?
    val isFile: Boolean
    val isDirectory: Boolean
    val isHidden: Boolean

    fun canRead(): Boolean
    fun listFiles(): Array<LanternaFile>?
    fun exists(): Boolean

    companion object {
        fun listRoots(): Array<LanternaFile>
    }
}
