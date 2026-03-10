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
import java.util.Arrays
import kotlin.collections.HashSet

/**
 * This class is used to keep a 'map' of the usable area and note where all the interact:ables are. It can then be used
 * to find the next interactable in any direction. It is used inside the GUI system to drive arrow key navigation.
 * @author Martin
 */
class InteractableLookupMap internal constructor(size: TerminalSize) {
    private val lookupMap: Array<IntArray> = Array(size.rows) { IntArray(size.columns) }
    private val interactables: MutableList<Interactable> = ArrayList()

    internal val size: TerminalSize
        get() {
            if (lookupMap.isEmpty()) {
                return TerminalSize.ZERO
            }
            return TerminalSize(lookupMap[0].size, lookupMap.size)
        }

    init {
        for (row in lookupMap) {
            Arrays.fill(row, -1)
        }
    }

    internal fun reset() {
        interactables.clear()
        for (row in lookupMap) {
            Arrays.fill(row, -1)
        }
    }

    fun add(interactable: Interactable) {
        val topLeft = interactable.toBasePane(TerminalPosition.TOP_LEFT_CORNER)!!
        val componentSize = interactable.size!!
        interactables.add(interactable)
        val index = interactables.size - 1
        for (y in topLeft.row until topLeft.row + componentSize.rows) {
            for (x in topLeft.column until topLeft.column + componentSize.columns) {
                if (y >= 0 && y < lookupMap.size && x >= 0 && x < lookupMap[y].size) {
                    lookupMap[y][x] = index
                }
            }
        }
    }

    fun getInteractableAt(position: TerminalPosition): Interactable? {
        if (position.row < 0 || position.column < 0) {
            return null
        }
        if (position.row >= lookupMap.size) {
            return null
        }
        if (position.column >= lookupMap[0].size) {
            return null
        }
        if (lookupMap[position.row][position.column] == -1) {
            return null
        }
        return interactables[lookupMap[position.row][position.column]]
    }

    fun findNextUp(interactable: Interactable?): Interactable? {
        return findNextUpOrDown(interactable!!, false)
    }

    fun findNextDown(interactable: Interactable?): Interactable? {
        return findNextUpOrDown(interactable!!, true)
    }

    private fun findNextUpOrDown(interactable: Interactable, isDown: Boolean): Interactable? {
        val directionTerm = if (isDown) 1 else -1
        var startPosition = interactable.cursorLocation
        if (startPosition == null) {
            startPosition = if (isDown) {
                TerminalPosition(0, interactable.size!!.rows - 1)
            } else {
                TerminalPosition.TOP_LEFT_CORNER
            }
        } else {
            startPosition = if (isDown) {
                startPosition.withRow(interactable.size!!.rows - 1)
            } else {
                startPosition.withRow(0)
            }
        }
        startPosition = interactable.toBasePane(startPosition)
        if (startPosition == null) {
            return null
        }

        val disqualified = getDisqualifiedInteractables(startPosition, true)
        val mapSize = size
        var maxShiftLeft = interactable.toBasePane(TerminalPosition.TOP_LEFT_CORNER)!!.column
        maxShiftLeft = kotlin.math.max(maxShiftLeft, 0)
        var maxShiftRight = interactable.toBasePane(TerminalPosition(interactable.size!!.columns - 1, 0))!!.column
        maxShiftRight = kotlin.math.min(maxShiftRight, mapSize.columns - 1)
        val maxShift = kotlin.math.max(startPosition.column - maxShiftLeft, maxShiftRight - startPosition.row)

        var searchRow = startPosition.row + directionTerm
        while (searchRow >= 0 && searchRow < mapSize.rows) {
            for (xShift in 0..maxShift) {
                for (modifier in intArrayOf(1, -1)) {
                    if (xShift == 0 && modifier == -1) {
                        break
                    }
                    val searchColumn = startPosition.column + (xShift * modifier)
                    if (searchColumn < maxShiftLeft || searchColumn > maxShiftRight) {
                        continue
                    }
                    val index = lookupMap[searchRow][searchColumn]
                    if (index != -1 && !disqualified.contains(interactables[index])) {
                        return interactables[index]
                    }
                }
            }
            searchRow += directionTerm
        }
        return null
    }

    fun findNextLeft(interactable: Interactable?): Interactable? {
        return findNextLeftOrRight(interactable!!, false)
    }

    fun findNextRight(interactable: Interactable?): Interactable? {
        return findNextLeftOrRight(interactable!!, true)
    }

    private fun findNextLeftOrRight(interactable: Interactable, isRight: Boolean): Interactable? {
        val directionTerm = if (isRight) 1 else -1
        var startPosition = interactable.cursorLocation
        if (startPosition == null) {
            startPosition = if (isRight) {
                TerminalPosition(interactable.size!!.columns - 1, 0)
            } else {
                TerminalPosition.TOP_LEFT_CORNER
            }
        } else {
            startPosition = if (isRight) {
                startPosition.withColumn(interactable.size!!.columns - 1)
            } else {
                startPosition.withColumn(0)
            }
        }
        startPosition = interactable.toBasePane(startPosition)
        if (startPosition == null) {
            return null
        }

        val disqualified = getDisqualifiedInteractables(startPosition, false)
        val mapSize = size
        var maxShiftUp = interactable.toBasePane(TerminalPosition.TOP_LEFT_CORNER)!!.row
        maxShiftUp = kotlin.math.max(maxShiftUp, 0)
        var maxShiftDown = interactable.toBasePane(TerminalPosition(0, interactable.size!!.rows - 1))!!.row
        maxShiftDown = kotlin.math.min(maxShiftDown, mapSize.rows - 1)
        val maxShift = kotlin.math.max(startPosition.row - maxShiftUp, maxShiftDown - startPosition.row)

        var searchColumn = startPosition.column + directionTerm
        while (searchColumn >= 0 && searchColumn < mapSize.columns) {
            for (yShift in 0..maxShift) {
                for (modifier in intArrayOf(1, -1)) {
                    if (yShift == 0 && modifier == -1) {
                        break
                    }
                    val searchRow = startPosition.row + (yShift * modifier)
                    if (searchRow < maxShiftUp || searchRow > maxShiftDown) {
                        continue
                    }
                    val index = lookupMap[searchRow][searchColumn]
                    if (index != -1 && !disqualified.contains(interactables[index])) {
                        return interactables[index]
                    }
                }
            }
            searchColumn += directionTerm
        }
        return null
    }

    private fun getDisqualifiedInteractables(startPosition: TerminalPosition, scanHorizontally: Boolean): Set<Interactable> {
        var localStartPosition = startPosition
        val disqualified: MutableSet<Interactable> = HashSet()
        if (lookupMap.isEmpty()) {
            return disqualified
        }

        val mapSize = size

        if (localStartPosition.row < 0) {
            localStartPosition = localStartPosition.withRow(0)!!
        } else if (localStartPosition.row >= lookupMap.size) {
            localStartPosition = localStartPosition.withRow(lookupMap.size - 1)!!
        }
        if (localStartPosition.column < 0) {
            localStartPosition = localStartPosition.withColumn(0)!!
        } else if (localStartPosition.column >= lookupMap[localStartPosition.row].size) {
            localStartPosition = localStartPosition.withColumn(lookupMap[localStartPosition.row].size - 1)!!
        }

        if (scanHorizontally) {
            for (column in 0 until mapSize.columns) {
                val index = lookupMap[localStartPosition.row][column]
                if (index != -1) {
                    disqualified.add(interactables[index])
                }
            }
        } else {
            for (row in 0 until mapSize.rows) {
                val index = lookupMap[row][localStartPosition.column]
                if (index != -1) {
                    disqualified.add(interactables[index])
                }
            }
        }
        return disqualified
    }

    fun debug() {
        for (row in lookupMap) {
            for (value in row) {
                if (value >= 0) {
                    print(" ")
                }
                print(value)
            }
            println()
        }
        println()
    }
}
