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
import com.googlecode.lanterna.internal.compat.IdentityHashMap
import kotlin.collections.ArrayList

/**
 * Simple layout manager the puts all components on a single line, either horizontally or vertically.
 */
class LinearLayout constructor(
    private val direction: Direction = Direction.VERTICAL,
) : LayoutManager {
    enum class Alignment {
        BEGINNING,
        CENTER,
        END,
        FILL,
    }

    enum class GrowPolicy {
        NONE,
        CAN_GROW,
    }

    private class LinearLayoutData(
        val alignment: Alignment,
        val growPolicy: GrowPolicy,
    ) : LayoutData

    private var spacing: Int = if (direction == Direction.HORIZONTAL) 1 else 0
    private var changed: Boolean = true

    fun setSpacing(spacing: Int): LinearLayout {
        this.spacing = spacing
        this.changed = true
        return this
    }

    fun getSpacing(): Int = spacing

    override fun getPreferredSize(components: List<Component?>?): TerminalSize {
        val visibleComponents = components.orEmpty().filter { it?.isVisible == true }.map { it!! }
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
            val preferredSize = component.preferredSize ?: TerminalSize.ZERO
            if (maxWidth < preferredSize.columns) {
                maxWidth = preferredSize.columns
            }
            height += preferredSize.rows
        }
        height += spacing * (components.size - 1)
        return TerminalSize(maxWidth, kotlin.math.max(0, height))
    }

    private fun getPreferredSizeHorizontally(components: List<Component>): TerminalSize {
        var maxHeight = 0
        var width = 0
        for (component in components) {
            val preferredSize = component.preferredSize ?: TerminalSize.ZERO
            if (maxHeight < preferredSize.rows) {
                maxHeight = preferredSize.rows
            }
            width += preferredSize.columns
        }
        width += spacing * (components.size - 1)
        return TerminalSize(kotlin.math.max(0, width), maxHeight)
    }

    override fun hasChanged(): Boolean = changed

    override fun doLayout(
        area: TerminalSize?,
        components: List<Component?>?,
    ) {
        if (area == null) {
            changed = false
            return
        }
        val visibleComponents = components.orEmpty().filter { it?.isVisible == true }.map { it!! }
        if (direction == Direction.VERTICAL) {
            if (false) {
                doVerticalLayout(area, visibleComponents)
            } else {
                doFlexibleVerticalLayout(area, visibleComponents)
            }
        } else {
            if (false) {
                doHorizontalLayout(area, visibleComponents)
            } else {
                doFlexibleHorizontalLayout(area, visibleComponents)
            }
        }
        changed = false
    }

    @Deprecated("")
    private fun doVerticalLayout(
        area: TerminalSize,
        components: List<Component>,
    ) {
        var remainingVerticalSpace = area.rows
        val availableHorizontalSpace = area.columns
        for (component in components) {
            if (remainingVerticalSpace <= 0) {
                component.setPosition(TerminalPosition.TOP_LEFT_CORNER)
                component.setSize(TerminalSize.ZERO)
            } else {
                var alignment = Alignment.BEGINNING
                val layoutData = component.layoutData
                if (layoutData is LinearLayoutData) {
                    alignment = layoutData.alignment
                }

                val preferredSize = component.preferredSize ?: TerminalSize.ZERO
                var decidedSize =
                    TerminalSize(
                        kotlin.math.min(availableHorizontalSpace, preferredSize.columns),
                        kotlin.math.min(remainingVerticalSpace, preferredSize.rows),
                    )
                if (alignment == Alignment.FILL) {
                    decidedSize = decidedSize.withColumns(availableHorizontalSpace) ?: decidedSize
                    alignment = Alignment.BEGINNING
                }

                var position =
                    (component.position ?: TerminalPosition.TOP_LEFT_CORNER)
                        .withRow(area.rows - remainingVerticalSpace) ?: TerminalPosition.TOP_LEFT_CORNER
                position = when (alignment) {
                    Alignment.END -> position.withColumn(availableHorizontalSpace - decidedSize.columns)
                    Alignment.CENTER -> position.withColumn((availableHorizontalSpace - decidedSize.columns) / 2)
                    else -> position.withColumn(0)
                } ?: TerminalPosition.TOP_LEFT_CORNER
                component.setPosition(position)
                component.setSize((component.size ?: TerminalSize.ZERO).with(decidedSize))
                remainingVerticalSpace -= decidedSize.rows + spacing
            }
        }
    }

    private fun doFlexibleVerticalLayout(
        area: TerminalSize,
        components: List<Component>,
    ) {
        var availableVerticalSpace = area.rows
        val availableHorizontalSpace = area.columns
        val fittingMap = IdentityHashMap<Component, TerminalSize>()
        var totalRequiredVerticalSpace = 0

        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val preferredSize = component.preferredSize ?: TerminalSize.ZERO
            var fittingSize =
                TerminalSize(
                    kotlin.math.min(availableHorizontalSpace, preferredSize.columns),
                    preferredSize.rows,
                )
            if (alignment == Alignment.FILL) {
                fittingSize = fittingSize.withColumns(availableHorizontalSpace) ?: fittingSize
            }

            fittingMap[component] = fittingSize
            totalRequiredVerticalSpace += fittingSize.rows + spacing
        }
        if (components.isNotEmpty()) {
            totalRequiredVerticalSpace -= spacing
        }

        if (availableVerticalSpace < totalRequiredVerticalSpace) {
            val copyOfComponents = ArrayList(components)
            copyOfComponents.reverse()
            copyOfComponents.sortByDescending { fittingMap[it]?.rows ?: 0 }

            while (availableVerticalSpace < totalRequiredVerticalSpace) {
                val largestSize = fittingMap[copyOfComponents[0]]?.rows ?: 0
                for (largeComponent in copyOfComponents) {
                    val currentSize = fittingMap[largeComponent] ?: TerminalSize.ZERO
                    if (largestSize > currentSize.rows) {
                        break
                    }
                    fittingMap[largeComponent] = currentSize.withRelativeRows(-1) ?: TerminalSize.ZERO
                    totalRequiredVerticalSpace--
                    if (availableVerticalSpace >= totalRequiredVerticalSpace) {
                        break
                    }
                }
            }
        }

        if (availableVerticalSpace > totalRequiredVerticalSpace) {
            while (availableVerticalSpace > totalRequiredVerticalSpace) {
                var resizedOneComponent = false
                for (component in components) {
                    val layoutData = component.layoutData as? LinearLayoutData
                    val currentSize = fittingMap[component] ?: TerminalSize.ZERO
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap[component] = currentSize.withRelativeRows(1) ?: currentSize
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

            val decidedSize = fittingMap[component] ?: TerminalSize.ZERO
            var position =
                (component.position ?: TerminalPosition.TOP_LEFT_CORNER)
                    .withRow(topPosition) ?: TerminalPosition.TOP_LEFT_CORNER
            position = when (alignment) {
                Alignment.END -> position.withColumn(availableHorizontalSpace - decidedSize.columns)
                Alignment.CENTER -> position.withColumn((availableHorizontalSpace - decidedSize.columns) / 2)
                else -> position.withColumn(0)
            } ?: TerminalPosition.TOP_LEFT_CORNER
            component.setPosition((component.position ?: TerminalPosition.TOP_LEFT_CORNER).with(position))
            component.setSize((component.size ?: TerminalSize.ZERO).with(decidedSize))
            topPosition += decidedSize.rows + spacing
        }
    }

    @Deprecated("")
    private fun doHorizontalLayout(
        area: TerminalSize,
        components: List<Component>,
    ) {
        var remainingHorizontalSpace = area.columns
        val availableVerticalSpace = area.rows
        for (component in components) {
            if (remainingHorizontalSpace <= 0) {
                component.setPosition(TerminalPosition.TOP_LEFT_CORNER)
                component.setSize(TerminalSize.ZERO)
            } else {
                var alignment = Alignment.BEGINNING
                val layoutData = component.layoutData
                if (layoutData is LinearLayoutData) {
                    alignment = layoutData.alignment
                }

                val preferredSize = component.preferredSize ?: TerminalSize.ZERO
                var decidedSize =
                    TerminalSize(
                        kotlin.math.min(remainingHorizontalSpace, preferredSize.columns),
                        kotlin.math.min(availableVerticalSpace, preferredSize.rows),
                    )
                if (alignment == Alignment.FILL) {
                    decidedSize = decidedSize.withRows(availableVerticalSpace) ?: decidedSize
                    alignment = Alignment.BEGINNING
                }

                var position =
                    (component.position ?: TerminalPosition.TOP_LEFT_CORNER)
                        .withColumn(area.columns - remainingHorizontalSpace) ?: TerminalPosition.TOP_LEFT_CORNER
                position = when (alignment) {
                    Alignment.END -> position.withRow(availableVerticalSpace - decidedSize.rows)
                    Alignment.CENTER -> position.withRow((availableVerticalSpace - decidedSize.rows) / 2)
                    else -> position.withRow(0)
                } ?: TerminalPosition.TOP_LEFT_CORNER
                component.setPosition(position)
                component.setSize((component.size ?: TerminalSize.ZERO).with(decidedSize))
                remainingHorizontalSpace -= decidedSize.columns + spacing
            }
        }
    }

    private fun doFlexibleHorizontalLayout(
        area: TerminalSize,
        components: List<Component>,
    ) {
        val availableVerticalSpace = area.rows
        var availableHorizontalSpace = area.columns
        val fittingMap = IdentityHashMap<Component, TerminalSize>()
        var totalRequiredHorizontalSpace = 0

        for (component in components) {
            var alignment = Alignment.BEGINNING
            val layoutData = component.layoutData
            if (layoutData is LinearLayoutData) {
                alignment = layoutData.alignment
            }

            val preferredSize = component.preferredSize ?: TerminalSize.ZERO
            var fittingSize =
                TerminalSize(
                    preferredSize.columns,
                    kotlin.math.min(availableVerticalSpace, preferredSize.rows),
                )
            if (alignment == Alignment.FILL) {
                fittingSize = fittingSize.withRows(availableVerticalSpace) ?: fittingSize
            }

            fittingMap[component] = fittingSize
            totalRequiredHorizontalSpace += fittingSize.columns + spacing
        }
        if (components.isNotEmpty()) {
            totalRequiredHorizontalSpace -= spacing
        }

        if (availableHorizontalSpace < totalRequiredHorizontalSpace) {
            val copyOfComponents = ArrayList(components)
            copyOfComponents.reverse()
            copyOfComponents.sortByDescending { fittingMap[it]?.columns ?: 0 }

            while (availableHorizontalSpace < totalRequiredHorizontalSpace) {
                val largestSize = fittingMap[copyOfComponents[0]]?.columns ?: 0
                for (largeComponent in copyOfComponents) {
                    val currentSize = fittingMap[largeComponent] ?: TerminalSize.ZERO
                    if (largestSize > currentSize.columns) {
                        break
                    }
                    fittingMap[largeComponent] = currentSize.withRelativeColumns(-1) ?: TerminalSize.ZERO
                    totalRequiredHorizontalSpace--
                    if (availableHorizontalSpace >= totalRequiredHorizontalSpace) {
                        break
                    }
                }
            }
        }

        if (availableHorizontalSpace > totalRequiredHorizontalSpace) {
            while (availableHorizontalSpace > totalRequiredHorizontalSpace) {
                var resizedOneComponent = false
                for (component in components) {
                    val layoutData = component.layoutData as? LinearLayoutData
                    val currentSize = fittingMap[component] ?: TerminalSize.ZERO
                    if (layoutData != null && layoutData.growPolicy == GrowPolicy.CAN_GROW) {
                        fittingMap[component] = currentSize.withRelativeColumns(1) ?: currentSize
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

            val decidedSize = fittingMap[component] ?: TerminalSize.ZERO
            var position =
                (component.position ?: TerminalPosition.TOP_LEFT_CORNER)
                    .withColumn(leftPosition) ?: TerminalPosition.TOP_LEFT_CORNER
            position = when (alignment) {
                Alignment.END -> position.withRow(availableVerticalSpace - decidedSize.rows)
                Alignment.CENTER -> position.withRow((availableVerticalSpace - decidedSize.rows) / 2)
                else -> position.withRow(0)
            } ?: TerminalPosition.TOP_LEFT_CORNER
            component.setPosition((component.position ?: TerminalPosition.TOP_LEFT_CORNER).with(position))
            component.setSize((component.size ?: TerminalSize.ZERO).with(decidedSize))
            leftPosition += decidedSize.columns + spacing
        }
    }

    companion object {
        private const val USE_OLD_NON_FLEX_LAYOUT_PROPERTY =
            "com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout"

        fun createLayoutData(alignment: Alignment): LayoutData = createLayoutData(alignment, GrowPolicy.NONE)

        fun createLayoutData(
            alignment: Alignment,
            growPolicy: GrowPolicy,
        ): LayoutData = LinearLayoutData(alignment, growPolicy)
    }
}
