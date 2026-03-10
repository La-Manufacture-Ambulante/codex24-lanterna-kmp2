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
 * Copyright (C) 2017 Bruno Eberhard
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */
package com.googlecode.lanterna.gui2.menu

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Container
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.InteractableLookupMap
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A menu bar offering drop-down menus.
 */
@Suppress("SuspiciousMethodCalls")
open class MenuBar : AbstractComponent<MenuBar?>(), Container {
    companion object {
        private const val EXTRA_PADDING = 0
    }

    private val menus: MutableList<Menu> = CopyOnWriteArrayList()

    fun add(menu: Menu): MenuBar {
        menus.add(menu)
        menu.onAdded(this)
        return this
    }

    override val childCount: Int
        get() = menuCount

    override val childrenList: List<Component?>
        get() = ArrayList(menus)

    override val children: Collection<Component?>
        get() = childrenList

    override fun containsComponent(component: Component?): Boolean {
        return menus.contains(component)
    }

    @Synchronized
    override fun removeComponent(component: Component?): Boolean {
        val hadMenu = menus.remove(component)
        if (hadMenu) {
            component?.onRemoved(this)
        }
        return hadMenu
    }

    @Synchronized
    override fun nextFocus(fromThis: Interactable?): Interactable? {
        if (menus.isEmpty()) {
            return null
        } else if (fromThis == null) {
            return menus[0]
        } else if (!menus.contains(fromThis) || menus.indexOf(fromThis) == menus.size - 1) {
            return null
        }
        return menus[menus.indexOf(fromThis) + 1]
    }

    override fun previousFocus(fromThis: Interactable?): Interactable? {
        if (menus.isEmpty()) {
            return null
        } else if (fromThis == null) {
            return menus[menus.size - 1]
        } else if (!menus.contains(fromThis) || menus.indexOf(fromThis) == 0) {
            return null
        }
        return menus[menus.indexOf(fromThis) - 1]
    }

    override fun handleInput(key: KeyStroke?): Boolean {
        return false
    }

    fun getMenu(index: Int): Menu? {
        return menus[index]
    }

    val menuCount: Int
        get() = menus.size

    override fun createDefaultRenderer(): ComponentRenderer<MenuBar?> {
        return DefaultMenuBarRenderer()
    }

    @Synchronized
    override fun updateLookupMap(interactableLookupMap: InteractableLookupMap?) {
        for (menu in menus) {
            interactableLookupMap?.add(menu)
        }
    }

    override fun toBasePane(position: TerminalPosition?): TerminalPosition? {
        return position
    }

    open val isEmptyMenuBar: Boolean
        get() = false

    inner class DefaultMenuBarRenderer : ComponentRenderer<MenuBar?> {
        override fun getPreferredSize(menuBar: MenuBar?): TerminalSize {
            var maxHeight = 1
            var totalWidth = EXTRA_PADDING
            if (menuBar != null) {
                for (i in 0 until menuBar.menuCount) {
                    val menu = menuBar.getMenu(i) ?: continue
                    val preferredSize = menu.preferredSize ?: continue
                    maxHeight = kotlin.math.max(maxHeight, preferredSize.rows)
                    totalWidth += preferredSize.columns
                }
            }
            totalWidth += EXTRA_PADDING
            return TerminalSize(totalWidth, maxHeight)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, menuBar: MenuBar?) {
            val activeGraphics = graphics ?: return
            val activeMenuBar = menuBar ?: return

            activeGraphics.applyThemeStyle(themeDefinition?.normal)
            activeGraphics.fill(' ')

            var leftPosition = EXTRA_PADDING
            val size = activeGraphics.size ?: TerminalSize.ZERO
            var remainingSpace = size.columns - EXTRA_PADDING
            for (i in 0 until activeMenuBar.menuCount) {
                if (remainingSpace <= 0) {
                    break
                }
                val menu = activeMenuBar.getMenu(i) ?: continue
                val preferredSize = menu.preferredSize ?: continue
                val menuPosition = menu.position ?: TerminalPosition.TOP_LEFT_CORNER
                val finalPosition =
                    menuPosition.withColumn(leftPosition)?.withRow(0) ?: TerminalPosition(leftPosition, 0)
                menu.setPosition(finalPosition)
                val finalWidth = kotlin.math.min(preferredSize.columns, remainingSpace)
                val menuSize = menu.size ?: TerminalSize.ZERO
                val finalSize =
                    menuSize.withColumns(finalWidth)?.withRows(size.rows) ?: TerminalSize(finalWidth, size.rows)
                menu.setSize(finalSize)
                remainingSpace -= finalWidth + EXTRA_PADDING
                leftPosition += finalWidth + EXTRA_PADDING
                val componentGraphics = activeGraphics.newTextGraphics(finalPosition, finalSize)
                menu.draw(componentGraphics)
            }
        }
    }
}
