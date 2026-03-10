package com.googlecode.lanterna.graphics

import kotlin.reflect.KClass

internal actual fun themeClassHierarchyByName(className: String): List<String>? {
    val normalized = className.trim()
    if (normalized.isEmpty()) {
        return emptyList()
    }
    return listOf(normalized)
}

internal actual fun themeClassHierarchyByType(clazz: KClass<*>): List<String> {
    val className = clazz.qualifiedName ?: clazz.simpleName ?: return emptyList()
    return if (className == "kotlin.Any") emptyList() else listOf(className)
}

internal actual fun instantiateThemeClassByName(className: String): Any? {
    return null
}
