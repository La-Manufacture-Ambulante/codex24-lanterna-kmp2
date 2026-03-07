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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.input.*

import java.util.*

/**
 * @author ginkoblongata
 */
 class SplitPanel/**
 * 
 */
     protected constructor(private val compA:Component?, private val compB:Component?, private val isHorizontal:Boolean):Panel() {
private val thumb:ImageComponent?
private var ratio = 0.5

 val isInvalid:Boolean
@Override
get() {
return super.isInvalid()
}

init{
thumb = makeThumb()
setLayoutManager(ScrollPanelLayoutManager())
setRatio(10, 10)

addComponent(compA)
addComponent(thumb)
addComponent(compB)
}

internal fun makeThumb():ImageComponent? {
val imageComponent = object:ImageComponent() {
internal var aSize:TerminalSize? = null
internal var bSize:TerminalSize? = null
internal var tSize:TerminalSize? = null
internal var down:TerminalPosition? = null
internal var drag:TerminalPosition? = null

@Override
 fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
val result:Result?
if (keyStroke is MouseAction)
{
result = handleMouseAction((keyStroke as MouseAction?)!!)
}
else
{
result = super.handleKeyStroke(keyStroke)
}// TODO: Implement keyboard based resizing
return result
}

private fun handleMouseAction(mouseAction:MouseAction):Result? {
if (mouseAction.isMouseDown())
{
aSize = compA!!.getSize()
bSize = compB!!.getSize()
tSize = thumb!!.getSize()
down = mouseAction.getPosition()
}
if (mouseAction.isMouseDrag())
{
drag = mouseAction.getPosition()

 // xxxxxxxxxxxxxxxxxxxxx
                    // this is a hack, should not be needed if the pane drag
                    // only on mouse down'd comp stuff was completely working
                    if (down == null)
{
down = drag
}
 // xxxxxxxxxxxxxxxxxxxxx

                    val delta = if (isHorizontal) drag!!.minus(down!!)!!.column else drag!!.minus(down!!)!!.row
 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                    if (isHorizontal)
{
val a = Math.max(1, tSize!!.columns + aSize!!.columns + delta)
val b = Math.max(1, bSize!!.columns - delta)
setRatio(a, b)
}
else
{
val a = Math.max(1, tSize!!.rows + aSize!!.rows + delta)
val b = Math.max(1, bSize!!.rows - delta)
setRatio(a, b)
}
 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                }
if (mouseAction.isMouseUp())
{
down = null
drag = null
}
return Result.HANDLED
}
}
return imageComponent
}

internal inner class ScrollPanelLayoutManager:LayoutManager {

 var hasChanged:Boolean = false
init{
hasChanged = true
}


@Override
 fun getPreferredSize(components:List<Component?>?):TerminalSize {
val sizeA = compA!!.getPreferredSize()
val aWidth = sizeA!!.columns
val aHeight = sizeA!!.rows
val sizeB = compB!!.getPreferredSize()
val bWidth = sizeB!!.columns
val bHeight = sizeB!!.rows

val tWidth = thumb!!.getPreferredSize().getColumns()
val tHeight = thumb!!.getPreferredSize().getRows()

if (isHorizontal)
{
return TerminalSize(aWidth + tWidth + bWidth, Math.max(aHeight, Math.max(tHeight, bHeight)))
}
else
{
return TerminalSize(Math.max(aWidth, Math.max(tWidth, bWidth)), aHeight + tHeight + bHeight)
}
}

@Override
 fun doLayout(area:TerminalSize?, components:List<Component?>?) {
val size = getSize()

 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            // TODO: themed
            val length = if (isHorizontal) size!!.rows else size!!.columns
val tsize = TerminalSize(if (isHorizontal) 1 else length, if (!isHorizontal) 1 else length)
val textImage = BasicTextImage(tsize)
val theme = getTheme()
val themeDefinition = theme!!.getDefaultDefinition()
val themeStyle = themeDefinition!!.getNormal()

var thumbRenderer = TextCharacter.fromCharacter(
if (isHorizontal) Symbols.SINGLE_LINE_VERTICAL else Symbols.SINGLE_LINE_HORIZONTAL, 
themeStyle!!.getForeground(), 
themeStyle!!.getBackground())
if (thumb!!.isFocused())
{
thumbRenderer = thumbRenderer!!.withModifier(SGR.BOLD)
}

textImage.setAll(thumbRenderer)
thumb!!.setTextImage(textImage)
 // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

            val tWidth = thumb!!.getPreferredSize().getColumns()
val tHeight = thumb!!.getPreferredSize().getRows()

var w = size!!.columns
var h = size!!.rows

if (isHorizontal)
{
w -= tWidth
}
else
{
h -= tHeight
}

val compAPrevSize = compA!!.getSize()
val compBPrevSize = compB!!.getSize()
val thumbPrevSize = thumb!!.getSize()
val compAPrevPos = compA!!.getPosition()
val compBPrevPos = compB!!.getPosition()
val thumbPrevPos = thumb!!.getPosition()

if (isHorizontal)
{
val leftWidth = Math.max(0, (w * ratio).toInt())
val leftHeight = Math.max(0, Math.min(compA!!.getPreferredSize().getRows(), h))

val rightWidth = Math.max(0, w - leftWidth)
val rightHeight = Math.max(0, Math.min(compB!!.getPreferredSize().getRows(), h))

compA!!.setSize(TerminalSize(leftWidth, leftHeight))
thumb!!.setSize(thumb!!.getPreferredSize())
compB!!.setSize(TerminalSize(rightWidth, rightHeight))

compA!!.setPosition(TerminalPosition(0, 0))
thumb!!.setPosition(TerminalPosition(leftWidth, h / 2 - tHeight / 2))
compB!!.setPosition(TerminalPosition(leftWidth + tWidth, 0))
}
else
{
val leftWidth = Math.max(0, Math.min(compA!!.getPreferredSize().getColumns(), w))
val leftHeight = Math.max(0, (h * ratio).toInt())

val rightWidth = Math.max(0, Math.min(compB!!.getPreferredSize().getColumns(), w))
val rightHeight = Math.max(0, h - leftHeight)

compA!!.setSize(TerminalSize(leftWidth, leftHeight))
thumb!!.setSize(thumb!!.getPreferredSize())
compB!!.setSize(TerminalSize(rightWidth, rightHeight))

compA!!.setPosition(TerminalPosition(0, 0))
thumb!!.setPosition(TerminalPosition(w / 2 - tWidth / 2, leftHeight))
compB!!.setPosition(TerminalPosition(0, leftHeight + tHeight))
}

hasChanged = (!compAPrevPos!!.equals(compA!!.getPosition()) || 
!compAPrevSize!!.equals(compA!!.getSize()) || 
!compBPrevPos!!.equals(compB!!.getPosition()) || 
!compBPrevSize!!.equals(compB!!.getSize()) || 
!thumbPrevPos!!.equals(thumb!!.getPosition()) || 
!thumbPrevSize!!.equals(thumb!!.getSize()))
}

@Override
 fun hasChanged():Boolean {
return hasChanged
}
}

 /*
     * Use whatever sizing.
     *
     *
     */
     fun setRatio(left:Int, right:Int) {
if (left == 0 || right == 0)
{
ratio = 0.5
}
else
{
val total = Math.abs(left) + Math.abs(right)
ratio = left.toDouble() / total.toDouble()
}
}

 fun setThumbVisible(visible:Boolean) {
thumb!!.setVisible(visible)

if (visible)
{
this.setPreferredSize(null)
}
else
{
thumb!!.setPreferredSize(TerminalSize(1, 1))
}
}

companion object {

 fun ofHorizontal(left:Component?, right:Component?):SplitPanel {
val split = SplitPanel(left, right, true)
return split
}

 fun ofVertical(top:Component?, bottom:Component?):SplitPanel {
val split = SplitPanel(top, bottom, false)
return split
}
}
}

