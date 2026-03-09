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
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

/**
 * Interactive tree component for Lanterna GUI.
 */
class Tree<V>(
    val root: TreeNode<V>,
    internal val columns: Int,
    internal val scrollWindowHeight: Int,
) : AbstractInteractableComponent<Tree<V>?>() {

    interface Listener<V> {
        fun onToggleChanged(treeNode: TreeNode<V>?)
    }

    var selectedNode: TreeNode<V> = root
        private set
    internal var scrollingNode: TreeNode<V> = root
        private set
    private var selectedNodeLevel: Int = 0
    private var selectedNodeDepth: Int = 0
    var isOverflowCircle: Boolean = false
    private var nodeSelectedConsumer: Consumer<TreeNode<V>?>? = null
    private val listeners = CopyOnWriteArrayList<Listener<V>>()

    init {
        root.isFocused = true
        recomputeCursorPosition()
    }

    override val size: TerminalSize
        get() = TerminalSize(columns, scrollWindowHeight)

    var isDisplayRoot: Boolean
        get() = root.isVisible
        set(displayRoot) {
            root.isVisible = displayRoot
            if (!displayRoot && root.children.isNotEmpty()) {
                selectNode(root.children.first())
            }
        }

    fun computeTreeDepth(): Int {
        if (!root.isExpanded || root.children.isEmpty()) {
            return 1
        }
        return root.lastDirectChildren.lastExpandedChildren.computeDepth()
    }

    override fun createDefaultRenderer(): InteractableRenderer<Tree<V>?> {
        val definition = themeDefinition
        val spacing = definition?.getIntegerProperty(
            DefaultTreeRenderer.TREE_LEVEL_INDENT,
            DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT,
        ) ?: DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT
        val displayBrackets = definition?.getBooleanProperty(DefaultTreeRenderer.DISPLAY_BRACKETS, true) ?: true
        val displayBlock = definition?.getBooleanProperty(DefaultTreeRenderer.DISPLAY_BLOCK, false) ?: false
        return DefaultTreeRenderer(spacing, displayBrackets, displayBlock)
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (isKeyboardActivationStroke(keyStroke)) {
            selectedNode.toggleExpanded()
            nodeSelectedConsumer?.accept(selectedNode)
            runOnGUIThreadIfExistsOtherwiseRunDirect(
                Runnable {
                    for (listener in listeners) {
                        listener.onToggleChanged(selectedNode)
                    }
                },
            )
            return Interactable.Result.HANDLED
        }

        if (keyStroke.keyType == KeyType.MOUSE_EVENT) {
            val mouseAction = keyStroke as MouseAction
            when (mouseAction.actionType) {
                MouseActionType.SCROLL_UP -> {
                    focusPrevNode()
                    return Interactable.Result.HANDLED
                }

                MouseActionType.SCROLL_DOWN -> {
                    focusNextNode()
                    return Interactable.Result.HANDLED
                }

                MouseActionType.CLICK_DOWN -> {
                    val result = super.handleKeyStroke(keyStroke)
                    val selectedDepth = getSelectedDepthByMouseAction(mouseAction)
                    val nodeAtDepth = scrollingNode.getNodeAtDepth(selectedDepth)
                    if (nodeAtDepth != null) {
                        nodeAtDepth.toggleExpanded()
                        selectNode(nodeAtDepth, keepScrollAnchor = true)
                    }
                    return result ?: Interactable.Result.HANDLED
                }

                else -> return super.handleKeyStroke(keyStroke)
            }
        }

        if (!keyStroke.isAltDown && !keyStroke.isCtrlDown && !keyStroke.isShiftDown) {
            return when (keyStroke.keyType) {
                KeyType.ARROW_DOWN -> {
                    focusNextNode()
                    Interactable.Result.HANDLED
                }

                KeyType.ARROW_UP -> {
                    focusPrevNode()
                    Interactable.Result.HANDLED
                }

                KeyType.TAB -> Interactable.Result.MOVE_FOCUS_NEXT
                KeyType.REVERSE_TAB -> Interactable.Result.MOVE_FOCUS_PREVIOUS
                KeyType.ARROW_RIGHT -> Interactable.Result.MOVE_FOCUS_RIGHT
                KeyType.ARROW_LEFT -> Interactable.Result.MOVE_FOCUS_LEFT
                KeyType.HOME -> {
                    selectFirstNode()
                    Interactable.Result.HANDLED
                }

                KeyType.END -> {
                    selectLastNode()
                    Interactable.Result.HANDLED
                }

                KeyType.PAGE_UP -> {
                    repeat(scrollWindowHeight) {
                        if (!focusPrevNode()) {
                            return@repeat
                        }
                    }
                    scrollingNode = selectedNode
                    Interactable.Result.HANDLED
                }

                KeyType.PAGE_DOWN -> {
                    repeat(scrollWindowHeight) {
                        if (!focusNextNode()) {
                            return@repeat
                        }
                    }
                    scrollingNode = selectedNode
                    Interactable.Result.HANDLED
                }

                else -> Interactable.Result.UNHANDLED
            }
        }

        return Interactable.Result.UNHANDLED
    }

    fun selectFirstNode() {
        val firstNode = if (isDisplayRoot) {
            root
        } else {
            root.children.firstOrNull() ?: root
        }
        selectNode(firstNode)
    }

    fun selectLastNode() {
        val lastNode = if (root.isExpanded && root.children.isNotEmpty()) {
            root.lastDirectChildren.lastExpandedChildren
        } else {
            root
        }
        selectNode(lastNode, keepScrollAnchor = false)
        updateScrollingNode()
    }

    protected fun getSelectedDepthByMouseAction(click: MouseAction): Int {
        val position = click.position ?: TerminalPosition.TOP_LEFT_CORNER
        val global = globalPosition ?: TerminalPosition.TOP_LEFT_CORNER
        return position.row - global.row
    }

    fun setNodeSelectedConsumer(nodeSelectedConsumer: Consumer<TreeNode<V>?>?) {
        this.nodeSelectedConsumer = nodeSelectedConsumer
    }

    @Synchronized
    fun addListener(listener: Listener<V>?): Tree<V> {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener)
        }
        return this
    }

    fun removeListener(listener: Listener<V>?): Tree<V> {
        if (listener != null) {
            listeners.remove(listener)
        }
        return this
    }

    private fun recomputeCursorPosition() {
        selectedNodeLevel = selectedNode.computeLevel()
        selectedNodeDepth = selectedNode.computeDepth()
    }

    private fun updateScrollingNode() {
        if (scrollingNode.previousNode === selectedNode) {
            scrollingNode = selectedNode
            return
        }

        var delta = scrollingNode.getDepthTo(selectedNode)
        while (delta >= scrollWindowHeight && scrollingNode.nextNode != null) {
            scrollingNode = scrollingNode.nextNode!!
            delta--
        }
    }

    private fun selectNode(node: TreeNode<V>, keepScrollAnchor: Boolean = false) {
        selectedNode.isFocused = false
        node.isFocused = true
        selectedNode = node
        if (!keepScrollAnchor) {
            scrollingNode = node
        }
        recomputeCursorPosition()
    }

    private fun focusNextNode(): Boolean {
        val nextNode = selectedNode.nextNode
        return when {
            nextNode == null && isOverflowCircle -> {
                selectFirstNode()
                false
            }

            nextNode != null -> {
                selectNode(nextNode, keepScrollAnchor = true)
                updateScrollingNode()
                true
            }

            else -> false
        }
    }

    private fun focusPrevNode(): Boolean {
        val previousNode = selectedNode.previousNode
        return when {
            previousNode == null && isOverflowCircle -> {
                selectLastNode()
                false
            }

            previousNode != null -> {
                selectNode(previousNode, keepScrollAnchor = true)
                updateScrollingNode()
                true
            }

            else -> false
        }
    }

    class DefaultTreeRenderer<V> @JvmOverloads constructor(
        private val indent: Int = DEFAULT_TREE_LEVEL_INDENT,
        private val displayBrackets: Boolean = true,
        private val displayBlock: Boolean = false,
    ) : InteractableRenderer<Tree<V>?> {
        private val verticalScrollBar = ScrollBar(Direction.VERTICAL)

        init {
            require(indent >= 0) { "Indent must be >= 0" }
        }

        override fun getCursorLocation(tree: Tree<V>?): TerminalPosition? {
            val activeTree = tree ?: return null
            val offset = if (displayBrackets) 0 else -1
            val row = activeTree.scrollingNode.getDepthTo(activeTree.selectedNode)
            return TerminalPosition(1 + indent * activeTree.selectedNodeLevel + offset, maxOf(0, row))
        }

        override fun getPreferredSize(tree: Tree<V>?): TerminalSize {
            val activeTree = tree ?: return TerminalSize.ZERO
            val bracketsSize = if (displayBrackets) 4 else 2
            val width = if (activeTree.root.isExpanded) {
                activeTree.root.getMaxExpandedWidth(0, bracketsSize)
            } else {
                4 + TerminalTextUtils.getColumnWidth(activeTree.root.getLabel())
            }
            val height = if (activeTree.root.isExpanded) activeTree.root.expandedLength else 1
            return TerminalSize(width, height)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, tree: Tree<V>?) {
            val activeGraphics = graphics ?: return
            val activeTree = tree ?: return
            val themeDefinition = activeTree.themeDefinition ?: return

            activeGraphics.applyThemeStyle(themeDefinition.normal)
            activeGraphics.fill(' ')

            var activeNode: TreeNode<V>? = activeTree.scrollingNode
            for (row in 0 until activeTree.scrollWindowHeight) {
                val node = activeNode ?: break
                drawTreeNode(activeGraphics, themeDefinition, node, node.computeLevel(), row, activeTree.columns - 3)
                activeNode = node.nextNode
            }

            val displayedItems = activeTree.computeTreeDepth()
            if (displayedItems > activeTree.scrollWindowHeight) {
                verticalScrollBar.onAdded(activeTree.parent)
                verticalScrollBar.setViewSize(activeTree.scrollWindowHeight)
                verticalScrollBar.setScrollMaximum(displayedItems)
                verticalScrollBar.setScrollPosition(maxOf(0, activeTree.selectedNodeDepth - activeTree.scrollWindowHeight))
                verticalScrollBar.draw(
                    activeGraphics.newTextGraphics(
                        TerminalPosition(activeTree.columns - 1, 0),
                        TerminalSize(1, (activeGraphics.size ?: TerminalSize.ZERO).rows),
                    ),
                )
            }
        }

        private fun drawTreeNode(
            graphics: TextGUIGraphics,
            themeDefinition: ThemeDefinition,
            treeNode: TreeNode<V>,
            level: Int,
            depth: Int,
            columns: Int,
        ): Int {
            var nextDepth = depth
            val offset = if (displayBrackets) 0 else -1

            graphics.applyThemeStyle(if (treeNode.isFocused) themeDefinition.active else themeDefinition.normal)

            val labelOffset = 4 + indent * level + offset * 2
            val rawLabel = treeNode.getLabel()
            val label = if (displayBlock) {
                getBlockLabel(labelOffset, rawLabel, columns, themeDefinition.getCharacter(DISPLAY_BLOCK_FILLER, '.'))
            } else {
                rawLabel
            }
            graphics.putString(labelOffset, depth, label)

            if (displayBrackets) {
                graphics.applyThemeStyle(if (treeNode.isFocused) themeDefinition.preLight else themeDefinition.normal)
                graphics.setCharacter(indent * level, depth, themeDefinition.getCharacter(LEFT_BRACKET, '['))
                graphics.setCharacter(2 + indent * level, depth, themeDefinition.getCharacter(RIGHT_BRACKET, ']'))
            }

            graphics.setCharacter(3 + indent * level + 2 * offset, depth, ' ')
            graphics.applyThemeStyle(if (treeNode.isFocused) themeDefinition.selected else themeDefinition.normal)
            graphics.setCharacter(1 + indent * level + offset, depth, getMarker(themeDefinition, treeNode))

            nextDepth++
            if (treeNode.isExpanded) {
                for (child in treeNode.children) {
                    if (child.isVisible) {
                        nextDepth = drawTreeNode(graphics, themeDefinition, child, level + 1, nextDepth, columns)
                    }
                }
            }

            return nextDepth
        }

        private fun getBlockLabel(labelOffset: Int, label: String, columns: Int, filler: Char): String {
            val fillSpace = columns - labelOffset - label.length
            if (fillSpace <= 0) {
                return label
            }
            return buildString(fillSpace + label.length) {
                repeat(fillSpace) {
                    append(filler)
                }
                append(label)
            }
        }

        private fun getMarker(themeDefinition: ThemeDefinition, treeNode: TreeNode<V>): Char {
            return when {
                treeNode.isExpanded -> themeDefinition.getCharacter(EXPANDED_MARKER, '<')
                treeNode.isLeaf -> themeDefinition.getCharacter(LEAF_MARKER, '.')
                else -> themeDefinition.getCharacter(COLLAPSED_MARKER, '>')
            }
        }

        companion object {
            const val DEFAULT_TREE_LEVEL_INDENT = 1

            const val LEFT_BRACKET = "LEFT_BRACKET"
            const val RIGHT_BRACKET = "RIGHT_BRACKET"
            const val EXPANDED_MARKER = "EXPANDED_MARKER"
            const val COLLAPSED_MARKER = "COLLAPSED_MARKER"
            const val LEAF_MARKER = "LEAF_MARKER"

            const val TREE_LEVEL_INDENT = "TREE_LEVEL_INDENT"
            const val DISPLAY_BRACKETS = "DISPLAY_BRACKETS"
            const val DISPLAY_BLOCK = "DISPLAY_BLOCK"
            const val DISPLAY_BLOCK_FILLER = "DISPLAY_BLOCK_FILLER"
        }
    }
}
