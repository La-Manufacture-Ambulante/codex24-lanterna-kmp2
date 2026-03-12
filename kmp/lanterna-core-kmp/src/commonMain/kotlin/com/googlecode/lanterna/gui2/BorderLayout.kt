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
import java.util.ArrayList
import java.util.Collections
import java.util.EnumMap

/**
 * BorderLayout imitates the BorderLayout class from AWT, allowing you to add a center component with optional
 * components around it in top, bottom, left and right locations. The edge components will be sized at their preferred
 * size and the center component will take up whatever remains.
 * @author martin
 */
class BorderLayout : LayoutManager {
    /**
     * This type is what you use as the layout data for components added to a panel using `BorderLayout` for its
     * layout manager. This values specified where inside the panel the component should be added.
     */
    enum class Location : LayoutData {
        /**
         * The component with this value as its layout data will occupy the center space, whatever is remaining after
         * the other components (if any) have allocated their space.
         */
        CENTER,

        /**
         * The component with this value as its layout data will occupy the left side of the container, attempting to
         * allocate the preferred width of the component and at least the preferred height, but could be more depending
         * on the other components added.
         */
        LEFT,

        /**
         * The component with this value as its layout data will occupy the right side of the container, attempting to
         * allocate the preferred width of the component and at least the preferred height, but could be more depending
         * on the other components added.
         */
        RIGHT,

        /**
         * The component with this value as its layout data will occupy the top side of the container, attempting to
         * allocate the preferred height of the component and at least the preferred width, but could be more depending
         * on the other components added.
         */
        TOP,

        /**
         * The component with this value as its layout data will occupy the bottom side of the container, attempting to
         * allocate the preferred height of the component and at least the preferred width, but could be more depending
         * on the other components added.
         */
        BOTTOM,
    }

    override fun getPreferredSize(components: List<Component?>?): TerminalSize {
        val layout = makeLookupMap(components ?: emptyList())
        val preferredHeight =
            (if (layout.containsKey(Location.TOP)) layout[Location.TOP]!!.preferredSize?.rows ?: 0 else 0) +
                maxOf(
                    if (layout.containsKey(Location.LEFT)) layout[Location.LEFT]!!.preferredSize?.rows ?: 0 else 0,
                    maxOf(
                        if (layout.containsKey(Location.CENTER)) layout[Location.CENTER]!!.preferredSize?.rows ?: 0 else 0,
                        if (layout.containsKey(Location.RIGHT)) layout[Location.RIGHT]!!.preferredSize?.rows ?: 0 else 0,
                    ),
                ) +
                (if (layout.containsKey(Location.BOTTOM)) layout[Location.BOTTOM]!!.preferredSize?.rows ?: 0 else 0)

        val preferredWidth =
            maxOf(
                (if (layout.containsKey(Location.LEFT)) layout[Location.LEFT]!!.preferredSize?.columns ?: 0 else 0) +
                    (if (layout.containsKey(Location.CENTER)) layout[Location.CENTER]!!.preferredSize?.columns ?: 0 else 0) +
                    (if (layout.containsKey(Location.RIGHT)) layout[Location.RIGHT]!!.preferredSize?.columns ?: 0 else 0),
                maxOf(
                    if (layout.containsKey(Location.TOP)) layout[Location.TOP]!!.preferredSize?.columns ?: 0 else 0,
                    if (layout.containsKey(Location.BOTTOM)) layout[Location.BOTTOM]!!.preferredSize?.columns ?: 0 else 0,
                ),
            )
        return TerminalSize(preferredWidth, preferredHeight)
    }

    override fun doLayout(
        area: TerminalSize?,
        components: List<Component?>?,
    ) {
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

    private fun makeLookupMap(components: List<Component?>): EnumMap<Location, Component> {
        val map = EnumMap<Location, Component>(Location::class.java)
        val unassignedComponents = ArrayList<Component>()
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
        // When components don't have a location, we'll assign an available location based on this order.
        private val AUTO_ASSIGN_ORDER =
            Collections.unmodifiableList(
                listOf(Location.CENTER, Location.TOP, Location.BOTTOM, Location.LEFT, Location.RIGHT),
            )
    }
}
