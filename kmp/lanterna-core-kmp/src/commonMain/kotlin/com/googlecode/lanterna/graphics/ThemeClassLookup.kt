package com.googlecode.lanterna.graphics

import kotlin.reflect.KClass

internal expect fun themeClassHierarchyByName(className: String): List<String>?

internal expect fun themeClassHierarchyByType(clazz: KClass<*>): List<String>

internal expect fun instantiateThemeClassByName(className: String): Any?
