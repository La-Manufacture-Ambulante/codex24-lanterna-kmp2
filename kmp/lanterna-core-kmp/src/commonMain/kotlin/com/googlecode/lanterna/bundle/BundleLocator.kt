package com.googlecode.lanterna.bundle

import com.googlecode.lanterna.internal.compat.Locale

/**
 * Minimal cross-platform bundle locator used by localized UI labels.
 * JVM keeps richer resource handling in later slices; this common version stays compile-safe.
 */
abstract class BundleLocator protected constructor(
    private val bundleName: String?,
) {
    protected fun getBundleKeyValue(
        locale: Locale?,
        key: String?,
        vararg parameters: Any?,
    ): String? {
        val safeBundle = bundleName ?: return null
        val safeKey = key ?: return null
        val localized = bundles[safeBundle]?.get(locale?.language)?.get(safeKey)
            ?: bundles[safeBundle]?.get(DEFAULT_LANGUAGE)?.get(safeKey)
            ?: return null
        return applyParameters(localized, parameters)
    }

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
        private val bundles: MutableMap<String, MutableMap<String, MutableMap<String, String>>> = linkedMapOf()

        fun register(
            bundleName: String,
            language: String,
            entries: Map<String, String>,
        ) {
            val languageMap = bundles.getOrPut(bundleName) { linkedMapOf() }
            val keyMap = languageMap.getOrPut(language) { linkedMapOf() }
            keyMap.putAll(entries)
        }

        private fun applyParameters(template: String, parameters: Array<out Any?>): String {
            var resolved = template
            parameters.forEachIndexed { index, value ->
                resolved = resolved.replace("{$index}", value?.toString() ?: "")
            }
            return resolved
        }
    }
}
