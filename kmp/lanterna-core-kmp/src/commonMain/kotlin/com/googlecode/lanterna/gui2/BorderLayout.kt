/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize

/**
 * BorderLayout imitates AWT BorderLayout.
 */
class BorderLayout : LayoutManager {
    enum class Location : LayoutData {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    override fun getPreferredSize(components: List<Component?>?): TerminalSize {
        val layout = makeLookupMap(components ?: emptyList())
        fun row(location: Location): Int = layout[location]?.preferredSize?.rows ?: 0
        fun col(location: Location): Int = layout[location]?.preferredSize?.columns ?: 0

        val preferredHeight =
            row(Location.TOP) +
                maxOf(row(Location.LEFT), maxOf(row(Location.CENTER), row(Location.RIGHT))) +
                row(Location.BOTTOM)

        val preferredWidth = maxOf(
            col(Location.LEFT) + col(Location.CENTER) + col(Location.RIGHT),
            maxOf(col(Location.TOP), col(Location.BOTTOM)),
        )
        return TerminalSize(preferredWidth, preferredHeight)
    }

    override fun doLayout(area: TerminalSize?, components: List<Component?>?) {
        if (area == null || components == null) {
            return
        }
        val layout = makeLookupMap(components)
        var availableHorizontalSpace = area.columns
        var availableVerticalSpace = area.rows

        var topComponentHeight = 0
        var leftComponentWidth = 0

        if (layout.containsKey(Location.TOP)) {
            val topComponent = layout[Location.TOP]!!
            topComponentHeight = minOf(topComponent.preferredSize?.rows ?: 0, availableVerticalSpace)
            topComponent.setPosition(TerminalPosition.TOP_LEFT_CORNER)
            topComponent.setSize(TerminalSize(availableHorizontalSpace, topComponentHeight))
            availableVerticalSpace -= topComponentHeight
        }

        if (layout.containsKey(Location.BOTTOM)) {
            val bottomComponent = layout[Location.BOTTOM]!!
            val bottomComponentHeight = minOf(bottomComponent.preferredSize?.rows ?: 0, availableVerticalSpace)
            bottomComponent.setPosition(TerminalPosition(0, area.rows - bottomComponentHeight))
            bottomComponent.setSize(TerminalSize(availableHorizontalSpace, bottomComponentHeight))
            availableVerticalSpace -= bottomComponentHeight
        }

        if (layout.containsKey(Location.LEFT)) {
            val leftComponent = layout[Location.LEFT]!!
            leftComponentWidth = minOf(leftComponent.preferredSize?.columns ?: 0, availableHorizontalSpace)
            leftComponent.setPosition(TerminalPosition(0, topComponentHeight))
            leftComponent.setSize(TerminalSize(leftComponentWidth, availableVerticalSpace))
            availableHorizontalSpace -= leftComponentWidth
        }

        if (layout.containsKey(Location.RIGHT)) {
            val rightComponent = layout[Location.RIGHT]!!
            val rightComponentWidth = minOf(rightComponent.preferredSize?.columns ?: 0, availableHorizontalSpace)
            rightComponent.setPosition(TerminalPosition(area.columns - rightComponentWidth, topComponentHeight))
            rightComponent.setSize(TerminalSize(rightComponentWidth, availableVerticalSpace))
            availableHorizontalSpace -= rightComponentWidth
        }

        if (layout.containsKey(Location.CENTER)) {
            val centerComponent = layout[Location.CENTER]!!
            centerComponent.setPosition(TerminalPosition(leftComponentWidth, topComponentHeight))
            centerComponent.setSize(TerminalSize(availableHorizontalSpace, availableVerticalSpace))
        }

        for (component in components) {
            if (component != null && component.isVisible && !layout.containsValue(component)) {
                component.setPosition(TerminalPosition.TOP_LEFT_CORNER)
                component.setSize(TerminalSize.ZERO)
            }
        }
    }

    private fun makeLookupMap(components: List<Component?>): MutableMap<Location, Component> {
        val map = linkedMapOf<Location, Component>()
        val unassignedComponents = mutableListOf<Component>()
        for (component in components) {
            if (component == null || !component.isVisible) {
                continue
            }
            if (component.layoutData is Location) {
                map[component.layoutData as Location] = component
            } else {
                unassignedComponents.add(component)
            }
        }

        for (component in unassignedComponents) {
            for (location in AUTO_ASSIGN_ORDER) {
                if (!map.containsKey(location)) {
                    map[location] = component
                    break
                }
            }
        }

        return map
    }

    override fun hasChanged(): Boolean {
        return false
    }

    companion object {
        private val AUTO_ASSIGN_ORDER = listOf(
            Location.CENTER,
            Location.TOP,
            Location.BOTTOM,
            Location.LEFT,
            Location.RIGHT,
        )
    }
}
