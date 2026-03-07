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
import java.util.IdentityHashMap
import java.util.Collections
import java.util.stream.Collectors

/**
 * Simple layout manager the puts all components on a single line, either horizontally or vertically.
 */
 class LinearLayout/**
 * Standard constructor that creates a `LinearLayout` with a specified direction to position the components on
 * @param direction Direction for this `Direction`
 */
     @JvmOverloads  constructor(private val direction:Direction? = Direction.VERTICAL):LayoutManager {
private var spacing:Int = 0
private var changed:Boolean = false
/**
 * This enum type will decide the alignment of a component on the counter-axis, meaning the horizontal alignment on
 * vertical `LinearLayout`s and vertical alignment on horizontal `LinearLayout`s.
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
        FILL
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
        CAN_GROW
}

private class LinearLayoutData(private val alignment:Alignment?, private val growPolicy:GrowPolicy?):LayoutData

init{
this.spacing = if (direction === Direction.HORIZONTAL) 1 else 0
this.changed = true
}

/**
 * Sets the amount of empty space to put in between components. For horizontal layouts, this is number of columns
 * (by default 1) and for vertical layouts this is number of rows (by default 0).
 * @param spacing Spacing between components, either in number of columns or rows depending on the direction
 * @return Itself
 */
     fun setSpacing(spacing:Int):LinearLayout {
this.spacing = spacing
this.changed = true
return this
}

/**
 * Returns the amount of empty space to put in between components. For horizontal layouts, this is number of columns
 * (by default 1) and for vertical layouts this is number of rows (by default 0).
 * @return Spacing between components, either in number of columns or rows depending on the direction
 */
     fun getSpacing():Int {
return spacing
}

@Override
 fun getPreferredSize(components:List<Component?>?):TerminalSize {
var components = components
 // Filter out invisible components
        components = components!!.stream().filter(???({ Component.isVisible() })).collect(Collectors.toList())

if (direction === Direction.VERTICAL)
{
return getPreferredSizeVertically(components!!)
}
else
{
return getPreferredSizeHorizontally(components!!)
}
}

private fun getPreferredSizeVertically(components:List<Component?>):TerminalSize {
var maxWidth = 0
var height = 0
for (component in components)
{
val preferredSize = component!!.getPreferredSize()
if (maxWidth < preferredSize!!.columns)
{
maxWidth = preferredSize!!.columns
}
height += preferredSize!!.rows
}
height += spacing * (components.size() - 1)
return TerminalSize(maxWidth, Math.max(0, height))
}

private fun getPreferredSizeHorizontally(components:List<Component?>):TerminalSize {
var maxHeight = 0
var width = 0
for (component in components)
{
val preferredSize = component!!.getPreferredSize()
if (maxHeight < preferredSize!!.rows)
{
maxHeight = preferredSize!!.rows
}
width += preferredSize!!.columns
}
width += spacing * (components.size() - 1)
return TerminalSize(Math.max(0, width), maxHeight)
}

@Override
 fun hasChanged():Boolean {
return changed
}

@Override
 fun doLayout(area:TerminalSize?, components:List<Component?>?) {
var components = components
 // Filter out invisible components
        components = components!!.stream().filter(???({ Component.isVisible() })).collect(Collectors.toList())

if (direction === Direction.VERTICAL)
{
if (Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout"))
{
doVerticalLayout(area!!, components!!)
}
else
{
doFlexibleVerticalLayout(area!!, components!!)
}
}
else
{
if (Boolean.getBoolean("com.googlecode.lanterna.gui2.LinearLayout.useOldNonFlexLayout"))
{
doHorizontalLayout(area!!, components!!)
}
else
{
doFlexibleHorizontalLayout(area!!, components!!)
}
}
this.changed = false
}

@Deprecated
private fun doVerticalLayout(area:TerminalSize, components:List<Component?>) {
var remainingVerticalSpace = area.rows
val availableHorizontalSpace = area.columns
for (component in components)
{
if (remainingVerticalSpace <= 0)
{
component!!.setPosition(TerminalPosition.TOP_LEFT_CORNER)
component!!.setSize(TerminalSize.ZERO)
}
else
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val preferredSize = component!!.getPreferredSize()
var decidedSize:TerminalSize? = TerminalSize(
Math.min(availableHorizontalSpace, preferredSize!!.columns), 
Math.min(remainingVerticalSpace, preferredSize!!.rows))
if (alignment == Alignment.FILL)
{
decidedSize = decidedSize!!.withColumns(availableHorizontalSpace)
alignment = Alignment.BEGINNING
}

var position = component!!.getPosition()
position = position!!.withRow(area.rows - remainingVerticalSpace)
when (alignment) {
LinearLayout.Alignment.END -> position = position!!.withColumn(availableHorizontalSpace - decidedSize!!.columns)
LinearLayout.Alignment.CENTER -> position = position!!.withColumn((availableHorizontalSpace - decidedSize!!.columns) / 2)
LinearLayout.Alignment.BEGINNING -> position = position!!.withColumn(0)
else -> position = position!!.withColumn(0)
}
component!!.setPosition(position)
component!!.setSize(component!!.getSize().with(decidedSize))
remainingVerticalSpace -= decidedSize!!.rows + spacing
}
}
}

private fun doFlexibleVerticalLayout(area:TerminalSize, components:List<Component?>) {
var availableVerticalSpace = area.rows
val availableHorizontalSpace = area.columns
val fittingMap = IdentityHashMap()
var totalRequiredVerticalSpace = 0

for (component in components)
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val preferredSize = component!!.getPreferredSize()
var fittingSize:TerminalSize? = TerminalSize(
Math.min(availableHorizontalSpace, preferredSize!!.columns), 
preferredSize!!.rows)
if (alignment == Alignment.FILL)
{
fittingSize = fittingSize!!.withColumns(availableHorizontalSpace)
}

fittingMap.put(component, fittingSize)
totalRequiredVerticalSpace += fittingSize!!.rows + spacing
}
if (!components.isEmpty())
{
 // Remove the last spacing
            totalRequiredVerticalSpace -= spacing
}

 // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableVerticalSpace < totalRequiredVerticalSpace)
{
val copyOfComponents = ArrayList(components)
Collections.reverse(copyOfComponents)
copyOfComponents.sort({ o1, o2-> 
 // Reverse sort
                -Integer.compare(fittingMap.get(o1).getRows(), fittingMap.get(o2).getRows()) })

while (availableVerticalSpace < totalRequiredVerticalSpace)
{
val largestSize = fittingMap.get(copyOfComponents.get(0)).getRows()
for (largeComponent in copyOfComponents)
{
val currentSize = fittingMap.get(largeComponent)
if (largestSize > currentSize!!.rows)
{
break
}
fittingMap.put(largeComponent, currentSize!!.withRelativeRows(-1))
totalRequiredVerticalSpace--
if (availableHorizontalSpace >= totalRequiredVerticalSpace)
{
break
}
}
}
}

 // If we have more space available than we need, grow components to fill
        if (availableVerticalSpace > totalRequiredVerticalSpace)
{
var resizedOneComponent = false
while (availableVerticalSpace > totalRequiredVerticalSpace)
{
for (component in components)
{
val layoutData = component!!.getLayoutData() as LinearLayoutData
val currentSize = fittingMap.get(component)
if (layoutData != null && layoutData!!.growPolicy == GrowPolicy.CAN_GROW)
{
fittingMap.put(component, currentSize!!.withRelativeRows(1))
availableVerticalSpace--
resizedOneComponent = true
}
if (availableVerticalSpace <= totalRequiredVerticalSpace)
{
break
}
}
if (!resizedOneComponent)
{
break
}
}
}

 // Assign the sizes and positions
        var topPosition = 0
for (component in components)
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val decidedSize = fittingMap.get(component)
var position = component!!.getPosition()
position = position!!.withRow(topPosition)
when (alignment) {
LinearLayout.Alignment.END -> position = position!!.withColumn(availableHorizontalSpace - decidedSize!!.columns)
LinearLayout.Alignment.CENTER -> position = position!!.withColumn((availableHorizontalSpace - decidedSize!!.columns) / 2)
LinearLayout.Alignment.BEGINNING -> position = position!!.withColumn(0)
else -> position = position!!.withColumn(0)
}
component!!.setPosition(component!!.getPosition().with(position))
component!!.setSize(component!!.getSize().with(decidedSize))
topPosition += decidedSize!!.rows + spacing
}
}

@Deprecated
private fun doHorizontalLayout(area:TerminalSize, components:List<Component?>) {
var remainingHorizontalSpace = area.columns
val availableVerticalSpace = area.rows
for (component in components)
{
if (remainingHorizontalSpace <= 0)
{
component!!.setPosition(TerminalPosition.TOP_LEFT_CORNER)
component!!.setSize(TerminalSize.ZERO)
}
else
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val preferredSize = component!!.getPreferredSize()
var decidedSize:TerminalSize? = TerminalSize(
Math.min(remainingHorizontalSpace, preferredSize!!.columns), 
Math.min(availableVerticalSpace, preferredSize!!.rows))
if (alignment == Alignment.FILL)
{
decidedSize = decidedSize!!.withRows(availableVerticalSpace)
alignment = Alignment.BEGINNING
}

var position = component!!.getPosition()
position = position!!.withColumn(area.columns - remainingHorizontalSpace)
when (alignment) {
LinearLayout.Alignment.END -> position = position!!.withRow(availableVerticalSpace - decidedSize!!.rows)
LinearLayout.Alignment.CENTER -> position = position!!.withRow((availableVerticalSpace - decidedSize!!.rows) / 2)
LinearLayout.Alignment.BEGINNING -> position = position!!.withRow(0)
else -> position = position!!.withRow(0)
}
component!!.setPosition(position)
component!!.setSize(component!!.getSize().with(decidedSize))
remainingHorizontalSpace -= decidedSize!!.columns + spacing
}
}
}

private fun doFlexibleHorizontalLayout(area:TerminalSize, components:List<Component?>) {
val availableVerticalSpace = area.rows
var availableHorizontalSpace = area.columns
val fittingMap = IdentityHashMap()
var totalRequiredHorizontalSpace = 0

for (component in components)
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val preferredSize = component!!.getPreferredSize()
var fittingSize:TerminalSize? = TerminalSize(
preferredSize!!.columns, 
Math.min(availableVerticalSpace, preferredSize!!.rows))
if (alignment == Alignment.FILL)
{
fittingSize = fittingSize!!.withRows(availableVerticalSpace)
}

fittingMap.put(component, fittingSize)
totalRequiredHorizontalSpace += fittingSize!!.columns + spacing
}
if (!components.isEmpty())
{
 // Remove the last spacing
            totalRequiredHorizontalSpace -= spacing
}

 // If we can't fit everything, trim the down the size of the largest components until it fits
        if (availableHorizontalSpace < totalRequiredHorizontalSpace)
{
val copyOfComponents = ArrayList(components)
Collections.reverse(copyOfComponents)
copyOfComponents.sort({ o1, o2-> 
 // Reverse sort
                -Integer.compare(fittingMap.get(o1).getColumns(), fittingMap.get(o2).getColumns()) })

while (availableHorizontalSpace < totalRequiredHorizontalSpace)
{
val largestSize = fittingMap.get(copyOfComponents.get(0)).getColumns()
for (largeComponent in copyOfComponents)
{
val currentSize = fittingMap.get(largeComponent)
if (largestSize > currentSize!!.columns)
{
break
}
fittingMap.put(largeComponent, currentSize!!.withRelativeColumns(-1))
totalRequiredHorizontalSpace--
if (availableHorizontalSpace >= totalRequiredHorizontalSpace)
{
break
}
}
}
}

 // If we have more space available than we need, grow components to fill
        if (availableHorizontalSpace > totalRequiredHorizontalSpace)
{
var resizedOneComponent = false
while (availableHorizontalSpace > totalRequiredHorizontalSpace)
{
for (component in components)
{
val layoutData = component!!.getLayoutData() as LinearLayoutData
val currentSize = fittingMap.get(component)
if (layoutData != null && layoutData!!.growPolicy == GrowPolicy.CAN_GROW)
{
fittingMap.put(component, currentSize!!.withRelativeColumns(1))
availableHorizontalSpace--
resizedOneComponent = true
}
if (availableHorizontalSpace <= totalRequiredHorizontalSpace)
{
break
}
}
if (!resizedOneComponent)
{
break
}
}
}

 // Assign the sizes and positions
        var leftPosition = 0
for (component in components)
{
var alignment:Alignment? = Alignment.BEGINNING
val layoutData = component!!.getLayoutData()
if (layoutData is LinearLayoutData)
{
alignment = (layoutData as LinearLayoutData).alignment
}

val decidedSize = fittingMap.get(component)
var position = component!!.getPosition()
position = position!!.withColumn(leftPosition)
when (alignment) {
LinearLayout.Alignment.END -> position = position!!.withRow(availableVerticalSpace - decidedSize!!.rows)
LinearLayout.Alignment.CENTER -> position = position!!.withRow((availableVerticalSpace - decidedSize!!.rows) / 2)
LinearLayout.Alignment.BEGINNING -> position = position!!.withRow(0)
else -> position = position!!.withRow(0)
}
component!!.setPosition(component!!.getPosition().with(position))
component!!.setSize(component!!.getSize().with(decidedSize))
leftPosition += decidedSize!!.columns + spacing
}
}

companion object {

/**
 * Creates a `LayoutData` for `LinearLayout` that assigns a component to a particular alignment on its
 * counter-axis, meaning the horizontal alignment on vertical `LinearLayout`s and vertical alignment on
 * horizontal `LinearLayout`s.
 * @param alignment Alignment to store in the `LayoutData` object
 * @param growPolicy When policy to apply to the component if the parent container has more space available along
 * the main axis.
 * @return `LayoutData` object created for `LinearLayout`s with the specified alignment
 * @see Alignment
 */
    @JvmOverloads  fun createLayoutData(alignment:Alignment?, growPolicy:GrowPolicy? = GrowPolicy.NONE):LayoutData {
return LinearLayoutData(alignment, growPolicy)
}
}
}/**
 * Creates a `LayoutData` for `LinearLayout` that assigns a component to a particular alignment on its
 * counter-axis, meaning the horizontal alignment on vertical `LinearLayout`s and vertical alignment on
 * horizontal `LinearLayout`s.
 * @param alignment Alignment to store in the `LayoutData` object
 * @return `LayoutData` object created for `LinearLayout`s with the specified alignment
 * @see Alignment
 *//**
 * Default constructor, creates a vertical `LinearLayout`
 */
