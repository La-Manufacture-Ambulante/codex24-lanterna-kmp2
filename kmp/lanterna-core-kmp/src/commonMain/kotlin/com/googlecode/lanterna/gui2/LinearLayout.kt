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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.IdentityHashMap
import java.util.stream.Collectors

/**
 * Simple layout manager the puts all components on a single line, either horizontally or vertically.
 */
open class LinearLayout : LayoutManager {
    /**
     * This enum type will decide the alignment of a component on the counter-axis, meaning the horizontal alignment on
     * vertical [LinearLayout]s and vertical alignment on horizontal [LinearLayout]s.
     */
    enum class Alignment {
        /**
         * The component will be placed to the left (for vertical layouts) or top (for horizontal layouts)
         */
        BEGINNING,

        /**
         * The component will be placed horizontally centered (for vertical layouts) or vertically centered (for
         * horizontal layouts)
         */
        CENTER,

        /**
         * The component will be placed to the right (for vertical layouts) or bottom (for horizontal layouts)
         */
        END,

        /**
         * The component will be forced to take up all the horizontal space (for vertical layouts) or vertical space
         * (for horizontal layouts)
         */
        FILL,
    }

    /**
     * This enum type will what to do with a component if the container has extra space to offer. This can happen if the
     * window runs in full screen or the window has been programmatically set to a fixed size, above the preferred size
     * of the window.
     */
    enum class GrowPolicy {
        /**
         * This is the default grow policy, the component will not become larger than the preferred size, even if the
         * container can offer more.
         */
        NONE,

        /**
         * With this grow policy, if the container has more space available then this component will be grown to fill
         * the extra space.
         */
        CAN_GROW,
    }

    private class LinearLayoutData(val alignment: Alignment, val growPolicy: GrowPolicy) : LayoutData

    companion object {
        /**
         * Creates a [LayoutData] for [LinearLayout] that assigns a component to a particular alignment on its
         * counter-axis, meaning the horizontal alignment on vertical [LinearLayout]s and vertical alignment on
         * horizontal [LinearLayout]s.
         * @param alignment Alignment to store in the [LayoutData] object
         * @return [LayoutData] object created for [LinearLayout]s with the specified alignment
         * @see Alignment
         */
        @JvmStatic
        fun createLayoutData(alignment: Alignment): LayoutData {
            return createLayoutData(alignment, GrowPolicy.NONE)
        }

        /**
         * Creates a [LayoutData] for [LinearLayout] that assigns a component to a particular alignment on its
         * counter-axis, meaning the horizontal alignment on vertical [LinearLayout]s and vertical alignment on
         * horizontal [LinearLayout]s.
         * @param alignment Alignment to store in the [LayoutData] object
         * @param growPolicy When policy to apply to the component if the parent container has more space available along
         *                   the main axis.
         * @return [LayoutData] object created for [LinearLayout]s with the specified alignment
         * @see Alignment
         */
        @JvmStatic
        fun createLayoutData(alignment: Alignment, growPolicy: GrowPolicy): LayoutData {
            return LinearLayoutData(alignment, growPolicy)
        }
    }

    private val direction: Direction
    private var spacing: Int
    private var changed: Boolean

    /**
     * Default constructor, creates a vertical [LinearLayout]
     */
    constructor() : this(Direction.VERTICAL)

    /**
     * Standard constructor that creates a [LinearLayout] with a specified direction to position the components on
     * @param direction Direction for this [Direction]
     */
    constructor(direction: Direction) {
        this.direction = direction
        this.spacing = if (direction == Direction.HORIZONTAL) 1 else 0
        this.changed = true
    }

    /**
     * Sets the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @param spacing Spacing between components, either in number of columns or rows depending on the direction
     * @return Itself
     */
    open fun setSpacing(spacing: Int): LinearLayout {
        this.spacing = spacing
        this.changed = true
        return this
    }

    /**
     * Returns the amount of empty space to put in between components. For horizontal layouts, this is number of columns
     * (by default 1) and for vertical layouts this is number of rows (by default 0).
     * @return Spacing between components, either in number of columns or rows depending on the direction
     */
    open fun getSpacing(): Int {
        return spacing
    }

    override fun getPreferredSize(components: MutableList<Component>): TerminalSize {
        val visibleComponents = components.stream().filter(Component::isVisible).collect(Collectors.toList())

        return if (direction == Direction.VERTICAL) {
            getPreferredSizeVertically(visibleComponents)
        } else {
            getPreferredSizeHorizontally(visibleComponents)
        }
    }

    private fun getPreferredSizeVertically(components: List<Component>): TerminalSize {
        var maxWidth = 0
        var height = 0
        for (component in components) {
            val preferredSize = component.preferredSize
            if (maxWidth < preferredSize.columns) {
                maxWidth = preferredSize.columns
            }
            height += preferredSize.rows
        }
        height += spacing * (components.size - 1)
        return TerminalSize(maxWidth, Math.max(0, height))
    }

    private fun getPreferredSizeHorizontally(components: List<Component>): TerminalSize {
        var maxHeight = 0
        var width = 0
        for (component in components) {
            val preferredSize = component.preferredSize
            if (maxHeight < preferredSize.rows) {
                maxHeight = preferredSize.rows
            }
            width += preferredSize.columns
        }
        width += spacing * (components.size - 1)
        return TerminalSize(Math.max(0, width), maxHeight)
    }

    override fun hasChanged(): Boolean {
        return changed
    }

    override fun doLayout(area: TerminalSize, components: MutableList<Component>) {
        val visibleComponents = components.stream().filter(Component::isVisible).collect(Collectors.toList())

        if (direction == Direction.VERTICAL) {
            if (java.lang.Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout")) {
                doVerticalLayout(area, visibleComponents)
            } else {
                doFlexibleVerticalLayout(area, visibleComponents)
            }
        } else {
            if (java.lang.Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout")) {
                doHorizontalLayout(area, visibleComponents)
            } else {
                doFlexibleHorizontalLayout(area, visibleComponents)
            }
        }
        this.changed = false
    }

    @Deprecated("Deprecated in Java")
    private fun doVerticalLayout(area: TerminalSize, components: List<Component>) {
        var remainingVerticalSpace = area.rows
        val availableHorizontalSpace = area.columns
        for (component in components) {
            if (remainingVerticalSpace <= 0) {
                component.position = TerminalPosition.TOP_LEFT_CORNER
                component.size = TerminalSize.ZERO
            } else {
                var alignment = Alignment.BEGINNING
                val layoutData = component.layoutData
                if (layoutData is LinearLayoutData) {
                    alignment = layoutData.alignment
                }

                val preferredSize = component.preferredSize
                var decidedSize = TerminalSize(
                    Math.min(availableHorizontalSpace, preferredSize.columns),
                    Math.min(remainingVerticalSpace, preferredSize.rows)
                )
                if (alignment == Alignment.FILL) {
                    decidedSize = decidedSize.withColumns(availableHorizontalSpace)
                    alignment = Alignment.BEGINNING
                }

                var position = component.position
                position = position.withRow(area.rows - remainingVerticalSpace)
                when (alignment) {
                    Alignment.END -> position = position.withColumn(availableHorizontalSpace - decidedSize.columns)
                    Alignment.CENTER -> position = position.withColumn((availableHorizontalSpace - decidedSize.columns) / 2)
                    Alignment.BEGINNING, Alignment.FILL -> position = position.withColumn(0)
                }
                component.position = position
                component.size = component.size.with(decidedSize)
                remainingVerticalSpace -= decidedSize.rows + spacing
            }
        }
    }

    private fun doFlexibleVerticalLayout(area: TerminalSize, components: List<Component>) {
        var availableVerticalSpace = area.rows
        val availableHorizontalSpace = area.columns
        val fittingMap: IdentityHashMap<Component, TerminalSize> = IdentityHashMap()
        var totalRequiredVerticalSpace = 0

        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val preferredSize = component.preferredSize
            var fittingSize = TerminalSize(
                Math.min(availableHorizontalSpace, preferredSize.columns),
                preferredSize.rows
            )
            if (alignment == Alignment.FILL) {
                fittingSize = fittingSize.withColumns(availableHorizontalSpace)
            }

            fittingMap[component] = fittingSize
            totalRequiredVerticalSpace += fittingSize.rows + spacing
        }
        if (components.isNotEmpty()) {
            totalRequiredVerticalSpace -= spacing
        }

        if (availableVerticalSpace < totalRequiredVerticalSpace) {
            val copyOfComponents: MutableList<Component> = ArrayList(components)
            Collections.reverse(copyOfComponents)
            copyOfComponents.sortWith(Comparator { o1, o2 ->
                -Integer.compare(fittingMap.get(o1).rows, fittingMap.get(o2).rows)
            })

            while (availableVerticalSpace < totalRequiredVerticalSpace) {
                val largestSize = fittingMap.get(copyOfComponents[0]).rows
                for (largeComponent in copyOfComponents) {
                    val currentSize = fittingMap.get(largeComponent)
                    if (largestSize > currentSize.rows) {
                        break
                    }
                    fittingMap[largeComponent] = currentSize.withRelativeRows(-1)
                    totalRequiredVerticalSpace--
                    if (availableHorizontalSpace >= totalRequiredVerticalSpace) {
                        break
                    }
                }
            }
        }

        if (availableVerticalSpace > totalRequiredVerticalSpace) {
            var resizedOneComponent = false
            while (availableVerticalSpace > totalRequiredVerticalSpace) {
                for (component in components) {
                    val layoutData = component.layoutData as LinearLayoutData?
                    val currentSize = fittingMap.get(component)
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap[component] = currentSize.withRelativeRows(1)
                        availableVerticalSpace--
                        resizedOneComponent = true
                    }
                    if (availableVerticalSpace <= totalRequiredVerticalSpace) {
                        break
                    }
                }
                if (!resizedOneComponent) {
                    break
                }
            }
        }

        var topPosition = 0
        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val decidedSize = fittingMap.get(component)
            var position = component.position
            position = position.withRow(topPosition)
            when (alignment) {
                Alignment.END -> position = position.withColumn(availableHorizontalSpace - decidedSize.columns)
                Alignment.CENTER -> position = position.withColumn((availableHorizontalSpace - decidedSize.columns) / 2)
                Alignment.BEGINNING, Alignment.FILL -> position = position.withColumn(0)
            }
            component.position = component.position.with(position)
            component.size = component.size.with(decidedSize)
            topPosition += decidedSize.rows + spacing
        }
    }

    @Deprecated("Deprecated in Java")
    private fun doHorizontalLayout(area: TerminalSize, components: List<Component>) {
        var remainingHorizontalSpace = area.columns
        val availableVerticalSpace = area.rows
        for (component in components) {
            if (remainingHorizontalSpace <= 0) {
                component.position = TerminalPosition.TOP_LEFT_CORNER
                component.size = TerminalSize.ZERO
            } else {
                var alignment = Alignment.BEGINNING
                val layoutData = component.layoutData
                if (layoutData is LinearLayoutData) {
                    alignment = layoutData.alignment
                }

                val preferredSize = component.preferredSize
                var decidedSize = TerminalSize(
                    Math.min(remainingHorizontalSpace, preferredSize.columns),
                    Math.min(availableVerticalSpace, preferredSize.rows)
                )
                if (alignment == Alignment.FILL) {
                    decidedSize = decidedSize.withRows(availableVerticalSpace)
                    alignment = Alignment.BEGINNING
                }

                var position = component.position
                position = position.withColumn(area.columns - remainingHorizontalSpace)
                when (alignment) {
                    Alignment.END -> position = position.withRow(availableVerticalSpace - decidedSize.rows)
                    Alignment.CENTER -> position = position.withRow((availableVerticalSpace - decidedSize.rows) / 2)
                    Alignment.BEGINNING, Alignment.FILL -> position = position.withRow(0)
                }
                component.position = position
                component.size = component.size.with(decidedSize)
                remainingHorizontalSpace -= decidedSize.columns + spacing
            }
        }
    }

    private fun doFlexibleHorizontalLayout(area: TerminalSize, components: List<Component>) {
        val availableVerticalSpace = area.rows
        var availableHorizontalSpace = area.columns
        val fittingMap: IdentityHashMap<Component, TerminalSize> = IdentityHashMap()
        var totalRequiredHorizontalSpace = 0

        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val preferredSize = component.preferredSize
            var fittingSize = TerminalSize(
                preferredSize.columns,
                Math.min(availableVerticalSpace, preferredSize.rows)
            )
            if (alignment == Alignment.FILL) {
                fittingSize = fittingSize.withRows(availableVerticalSpace)
            }

            fittingMap[component] = fittingSize
            totalRequiredHorizontalSpace += fittingSize.columns + spacing
        }
        if (components.isNotEmpty()) {
            totalRequiredHorizontalSpace -= spacing
        }

        if (availableHorizontalSpace < totalRequiredHorizontalSpace) {
            val copyOfComponents: MutableList<Component> = ArrayList(components)
            Collections.reverse(copyOfComponents)
            copyOfComponents.sortWith(Comparator { o1, o2 ->
                -Integer.compare(fittingMap.get(o1).columns, fittingMap.get(o2).columns)
            })

            while (availableHorizontalSpace < totalRequiredHorizontalSpace) {
                val largestSize = fittingMap.get(copyOfComponents[0]).columns
                for (largeComponent in copyOfComponents) {
                    val currentSize = fittingMap.get(largeComponent)
                    if (largestSize > currentSize.columns) {
                        break
                    }
                    fittingMap[largeComponent] = currentSize.withRelativeColumns(-1)
                    totalRequiredHorizontalSpace--
                    if (availableHorizontalSpace >= totalRequiredHorizontalSpace) {
                        break
                    }
                }
            }
        }

        if (availableHorizontalSpace > totalRequiredHorizontalSpace) {
            var resizedOneComponent = false
            while (availableHorizontalSpace > totalRequiredHorizontalSpace) {
                for (component in components) {
                    val layoutData = component.layoutData as LinearLayoutData?
                    val currentSize = fittingMap.get(component)
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap[component] = currentSize.withRelativeColumns(1)
                        availableHorizontalSpace--
                        resizedOneComponent = true
                    }
                    if (availableHorizontalSpace <= totalRequiredHorizontalSpace) {
                        break
                    }
                }
                if (!resizedOneComponent) {
                    break
                }
            }
        }

        var leftPosition = 0
        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val decidedSize = fittingMap.get(component)
            var position = component.position
            position = position.withColumn(leftPosition)
            when (alignment) {
                Alignment.END -> position = position.withRow(availableVerticalSpace - decidedSize.rows)
                Alignment.CENTER -> position = position.withRow((availableVerticalSpace - decidedSize.rows) / 2)
                Alignment.BEGINNING, Alignment.FILL -> position = position.withRow(0)
            }
            component.position = component.position.with(position)
            component.size = component.size.with(decidedSize)
            leftPosition += decidedSize.columns + spacing
        }
    }
}
