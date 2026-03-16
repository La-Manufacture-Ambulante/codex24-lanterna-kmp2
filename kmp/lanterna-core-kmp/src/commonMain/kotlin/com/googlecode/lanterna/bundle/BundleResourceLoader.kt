package com.googlecode.lanterna.bundle

internal expect object BundleResourceLoader {
    fun loadTextResource(resourcePath: String): String?
}
