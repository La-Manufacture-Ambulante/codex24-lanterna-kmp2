package com.googlecode.lanterna.graphics

import kotlin.reflect.KClass

internal actual fun themeClassHierarchyByName(className: String): List<String>? {
    return runCatching {
        val clazz = Class.forName(className)
        collectHierarchy(clazz)
    }.getOrNull()
}

internal actual fun themeClassHierarchyByType(clazz: KClass<*>): List<String> {
    return runCatching {
        val className = clazz.qualifiedName ?: clazz.simpleName ?: return emptyList()
        val javaClass = Class.forName(className)
        collectHierarchy(javaClass)
    }.getOrElse {
        listOfNotNull(clazz.qualifiedName ?: clazz.simpleName)
    }
}

internal actual fun instantiateThemeClassByName(className: String): Any? {
    return try {
        Class.forName(className).getDeclaredConstructor().newInstance()
    } catch (e: ReflectiveOperationException) {
        throw RuntimeException(e)
    }
}

private fun collectHierarchy(clazz: Class<*>): List<String> {
    val hierarchy = ArrayDeque<String>()
    var current: Class<*>? = clazz
    while (current != null && current != Any::class.java) {
        hierarchy.addFirst(current.name)
        current = current.superclass
    }
    return hierarchy.toList()
}
