package com.googlecode.lanterna.bundle

internal actual object BundleResourceLoader {
    actual fun loadTextResource(resourcePath: String): String? {
        val stream = LanternaThemes::class.java.classLoader?.getResourceAsStream(resourcePath) ?: return null
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
