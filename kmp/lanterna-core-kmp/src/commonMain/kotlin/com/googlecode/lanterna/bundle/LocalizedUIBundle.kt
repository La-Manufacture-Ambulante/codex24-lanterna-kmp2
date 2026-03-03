package com.googlecode.lanterna.bundle

import java.util.Locale

/**
 * This class permits to get easily localized strings about the UI.
 * @author silveryocha
 */
class LocalizedUIBundle private constructor(bundleName: String?) : BundleLocator(bundleName) {
    companion object {
        private val MY_BUNDLE = LocalizedUIBundle("multilang.lanterna-ui")

        @JvmStatic
        fun get(key: String?, parameters: Array<out String?>?): String? {
            return get(Locale.getDefault(), key, parameters)
        }

        @JvmStatic
        fun get(locale: Locale?, key: String?, parameters: Array<out String?>?): String? {
            return MY_BUNDLE.getBundleKeyValue(locale, key, parameters as Array<out Any?>?)
        }
    }
}
