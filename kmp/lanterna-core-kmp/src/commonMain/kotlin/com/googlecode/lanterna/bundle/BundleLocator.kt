package com.googlecode.lanterna.bundle

import com.googlecode.lanterna.internal.compat.Properties
import com.googlecode.lanterna.internal.compat.StringReader
import com.googlecode.lanterna.internal.compat.Locale

/**
 * This class permits to deal easily with bundles.
 * @author silveryocha
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
        val requestedLanguage = normalizeLanguage(locale?.language)
        val localized = getBundleValue(safeBundle, requestedLanguage, safeKey)
            ?: if (requestedLanguage != DEFAULT_LANGUAGE) {
                getBundleValue(safeBundle, DEFAULT_LANGUAGE, safeKey)
            } else {
                null
            }
        if (localized == null) {
            return null
        }
        return applyParameters(localized, parameters)
    }

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
        private val registeredBundles: MutableMap<String, MutableMap<String, MutableMap<String, String>>> = linkedMapOf()
        private val loadedBundles: MutableMap<String, MutableMap<String, Map<String, String>>> = linkedMapOf()

        fun register(
            bundleName: String,
            language: String,
            entries: Map<String, String>,
        ) {
            val languageMap = registeredBundles.getOrPut(bundleName) { linkedMapOf() }
            val keyMap = languageMap.getOrPut(language) { linkedMapOf() }
            keyMap.putAll(entries)
        }

        private fun normalizeLanguage(language: String?): String {
            return language?.takeIf { it.isNotBlank() } ?: DEFAULT_LANGUAGE
        }

        private fun getBundleValue(bundleName: String, language: String, key: String): String? {
            val normalizedLanguage = normalizeLanguage(language)
            return registeredBundles[bundleName]?.get(normalizedLanguage)?.get(key)
                ?: loadBundle(bundleName, normalizedLanguage)[key]
        }

        private fun loadBundle(bundleName: String, language: String): Map<String, String> {
            val bundleCache = loadedBundles.getOrPut(bundleName) { linkedMapOf() }
            return bundleCache.getOrPut(language) {
                loadBundleFromResource(bundleName, language) ?: emptyMap()
            }
        }

        private fun loadBundleFromResource(bundleName: String, language: String): Map<String, String>? {
            val resourcePath = toResourcePath(bundleName, language)
            val raw = BundleResourceLoader.loadTextResource(resourcePath) ?: return null
            return parseProperties(raw)
        }

        private fun toResourcePath(bundleName: String, language: String): String {
            val basePath = bundleName.replace('.', '/')
            return if (language == DEFAULT_LANGUAGE) {
                "$basePath.properties"
            } else {
                "${basePath}_${language}.properties"
            }
        }

        private fun parseProperties(raw: String): Map<String, String> {
            val properties = Properties()
            properties.load(StringReader(raw))
            val values = linkedMapOf<String, String>()
            for (name in properties.stringPropertyNames()) {
                val value = properties.getProperty(name) ?: continue
                values[name] = value
            }
            return values
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
