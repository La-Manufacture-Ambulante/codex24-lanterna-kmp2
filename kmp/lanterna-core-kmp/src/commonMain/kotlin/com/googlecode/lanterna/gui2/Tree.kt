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

open class Tree<V>(
    root: TreeNode<V>?,
    private val columns: Int,
    private val scrollWindowHeight: Int
) : AbstractInteractableComponent<Tree<V>>() {

    interface Listener<V> {
        fun onToggleChanged(treeNode: TreeNode<V>?)
    }

    private val root: TreeNode<V>
    private var selectedNode: TreeNode<V>
    private var scrollingNode: TreeNode<V>

    private var selectedNodeLevel: Int = 0
    private var selectedNodeDepth: Int = 0

    private var overflowCircle: Boolean = false

    private var nodeSelectedConsumer: Consumer<TreeNode<V>>? = null
    private val listeners: MutableList<Listener<V>> = CopyOnWriteArrayList()

    init {
        if (root == null) {
            throw IllegalArgumentException("Root must not be null")
        }
        this.root = root
        this.selectedNode = root
        this.scrollingNode = root
        this.root.setFocused(true)
        recomputeCursorPosition()
    }

    open fun getRoot(): TreeNode<V> {
        return root
    }

    open fun computeTreeDepth(): Int {
        if (!root.isExpanded || root.children.isEmpty()) {
            return 1
        }
        return root.lastDirectChildren.lastExpandedChildren.computeDepth()
    }

    private fun recomputeCursorPosition() {
        this.selectedNodeLevel = selectedNode.computeLevel()
        this.selectedNodeDepth = selectedNode.computeDepth()
    }

    private fun getScrollingNode(): TreeNode<V> {
        return scrollingNode
    }

    private fun updateScrollingNode() {
        if (scrollingNode.previousNode == selectedNode) {
            scrollingNode = selectedNode
        } else {
            var delta = scrollingNode.getDepthTo(selectedNode)
            while (delta-- >= scrollWindowHeight) {
                scrollingNode = scrollingNode.nextNode
            }
        }
    }

    override fun getSize(): TerminalSize {
        return TerminalSize(150, scrollWindowHeight)
    }

    protected override fun createDefaultRenderer(): InteractableRenderer<Tree<V>> {
        val spacing = themeDefinition.getIntegerProperty(
            DefaultTreeRenderer.TREE_LEVEL_INDENT,
            DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT
        )
        val displayBrackets =
            themeDefinition.getBooleanProperty(DefaultTreeRenderer.DISPLAY_BRACKETS, true)
        val displayBblock =
            themeDefinition.getBooleanProperty(DefaultTreeRenderer.DISPLAY_BLOCK, false)
        return DefaultTreeRenderer(spacing, displayBrackets, displayBblock)
    }

    protected override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result {
        if (isKeyboardActivationStroke(keyStroke)) {
            selectedNode.toggleExpanded()
            if (nodeSelectedConsumer != null) {
                nodeSelectedConsumer?.accept(selectedNode)
            }
            runOnGUIThreadIfExistsOtherwiseRunDirect {
                for (listener in listeners) {
                    listener.onToggleChanged(selectedNode)
                }
            }
            return Interactable.Result.HANDLED
        } else if (keyStroke.keyType == KeyType.MOUSE_EVENT) {
            val mouseAction = keyStroke as MouseAction
            val actionType = mouseAction.actionType

            if (actionType == MouseActionType.SCROLL_UP) {
                focusPrevNode()
                return Interactable.Result.HANDLED
            }
            if (actionType == MouseActionType.SCROLL_DOWN) {
                focusNextNode()
                return Interactable.Result.HANDLED
            }

            val result = super.handleKeyStroke(keyStroke)
            val selectedDepth = getSelectedDepthByMouseAction(mouseAction)
            if (actionType == MouseActionType.CLICK_DOWN) {
                val nodeAtDepth = scrollingNode.getNodeAtDepth(selectedDepth)
                if (nodeAtDepth != null) {
                    nodeAtDepth.toggleExpanded()
                    selectedNode.setFocused(false)
                    nodeAtDepth.setFocused(true)
                    selectedNode = nodeAtDepth
                    recomputeCursorPosition()
                }
                return Interactable.Result.HANDLED
            }
            return result
        } else if (!keyStroke.isAltDown && !keyStroke.isCtrlDown && !keyStroke.isShiftDown) {
            when (keyStroke.keyType) {
                KeyType.ARROW_DOWN -> {
                    focusNextNode()
                    return Interactable.Result.HANDLED
                }

                KeyType.ARROW_UP -> {
                    focusPrevNode()
                    return Interactable.Result.HANDLED
                }

                KeyType.TAB -> return Interactable.Result.MOVE_FOCUS_NEXT
                KeyType.REVERSE_TAB -> return Interactable.Result.MOVE_FOCUS_PREVIOUS
                KeyType.ARROW_RIGHT -> return Interactable.Result.MOVE_FOCUS_RIGHT
                KeyType.ARROW_LEFT -> return Interactable.Result.MOVE_FOCUS_LEFT

                KeyType.HOME -> {
                    selectFirstNode()
                    return Interactable.Result.HANDLED
                }

                KeyType.END -> {
                    selectLastNode()
                    return Interactable.Result.HANDLED
                }

                KeyType.PAGE_UP -> {
                    for (i in 0 until scrollWindowHeight) {
                        if (!focusPrevNode()) {
                            break
                        }
                    }
                    scrollingNode = selectedNode
                    return Interactable.Result.HANDLED
                }

                KeyType.PAGE_DOWN -> {
                    for (i in 0 until scrollWindowHeight) {
                        if (!focusNextNode()) {
                            break
                        }
                    }
                    scrollingNode = selectedNode
                    return Interactable.Result.HANDLED
                }

                else -> return Interactable.Result.UNHANDLED
            }
        }
        return Interactable.Result.UNHANDLED
    }

    open fun selectFirstNode() {
        selectedNode.setFocused(false)
        val firstNode = if (isDisplayRoot()) root else root.children[0]
        firstNode.setFocused(true)
        this.selectedNode = firstNode
        this.scrollingNode = firstNode
        recomputeCursorPosition()
    }

    open fun selectLastNode() {
        selectedNode.setFocused(false)
        val lastNode = if (getRoot().isExpanded) root.lastDirectChildren.lastExpandedChildren else root
        lastNode.setFocused(true)
        this.selectedNode = lastNode
        updateScrollingNode()
        recomputeCursorPosition()
    }

    protected open fun getSelectedDepthByMouseAction(click: MouseAction): Int {
        return click.position.row - globalPosition.row
    }

    private fun focusNextNode(): Boolean {
        val nextNode = selectedNode.nextNode
        if (nextNode == null && overflowCircle) {
            selectFirstNode()
            return false
        } else if (nextNode != null) {
            selectedNode.setFocused(false)
            nextNode.setFocused(true)
            this.selectedNode = nextNode
            updateScrollingNode()
            recomputeCursorPosition()
            return true
        }
        return false
    }

    private fun focusPrevNode(): Boolean {
        val previousNode = selectedNode.previousNode
        if (previousNode == null && overflowCircle) {
            selectLastNode()
            return false
        } else if (previousNode != null) {
            selectedNode.setFocused(false)
            previousNode.setFocused(true)
            this.selectedNode = previousNode
            updateScrollingNode()
            recomputeCursorPosition()
            return true
        }
        return false
    }

    open fun isDisplayRoot(): Boolean {
        return getRoot().isVisible
    }

    open fun isOverflowCircle(): Boolean {
        return overflowCircle
    }

    open fun setOverflowCircle(overflowCircle: Boolean) {
        this.overflowCircle = overflowCircle
    }

    open fun setDisplayRoot(displayRoot: Boolean) {
        getRoot().setVisible(displayRoot)
        if (!getRoot().children.isEmpty()) {
            val treeNode = getRoot().children[0]
            treeNode.setFocused(true)
            this.selectedNode = treeNode
            this.scrollingNode = treeNode
            recomputeCursorPosition()
        }
    }

    open fun getSelectedNode(): TreeNode<V> {
        return selectedNode
    }

    open fun setNodeSelectedConsumer(nodeSelectedConsumer: Consumer<TreeNode<V>>?) {
        this.nodeSelectedConsumer = nodeSelectedConsumer
    }

    @Synchronized
    open fun addListener(listener: Listener<V>?): Tree<V> {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener)
        }
        return this
    }

    open fun removeListener(listener: Listener<V>?): Tree<V> {
        listeners.remove(listener)
        return this
    }

    open class DefaultTreeRenderer<V>(
        private val indent: Int,
        private val displayBrackets: Boolean,
        private val displayBlock: Boolean
    ) : InteractableRenderer<Tree<V>> {

        private val verticalScrollBar: ScrollBar

        init {
            if (indent < 0) {
                throw IllegalArgumentException("Indent must be >= 0")
            }
            this.verticalScrollBar = ScrollBar(Direction.VERTICAL)
        }

        constructor() : this(DEFAULT_TREE_LEVEL_INDENT, true, false)

        override fun getCursorLocation(tree: Tree<V>): TerminalPosition {
            val offset = if (displayBrackets) 0 else -1
            val row = tree.scrollingNode.getDepthTo(tree.selectedNode)
            return TerminalPosition(1 + indent * tree.selectedNodeLevel + offset, row)
        }

        override fun getPreferredSize(tree: Tree<V>): TerminalSize {
            val bracketsSize = if (displayBrackets) 4 else 2
            val width = if (tree.getRoot().isExpanded) {
                tree.getRoot().getMaxExpandedWidth(0, bracketsSize)
            } else {
                4 + TerminalTextUtils.getColumnWidth(tree.getRoot().label)
            }
            var height = 1
            if (tree.getRoot().isExpanded) {
                height = tree.getRoot().expandedLength
            }

            return TerminalSize(width, height)
        }

        override fun drawComponent(graphics: TextGUIGraphics, tree: Tree<V>) {
            graphics.applyThemeStyle(tree.themeDefinition.normal)
            graphics.fill(' ')
            val scrollPosition = Math.max(0, tree.selectedNodeDepth - tree.scrollWindowHeight)

            var activeNode = tree.getScrollingNode()
            for (i in 0 until tree.scrollWindowHeight) {
                val level = activeNode.computeLevel()
                drawTreeNode(graphics, tree.themeDefinition, activeNode, level, i, tree.columns - 3)
                activeNode = activeNode.nextNode ?: break
            }

            val displayedItems = tree.computeTreeDepth()
            if (displayedItems > tree.scrollWindowHeight) {
                verticalScrollBar.onAdded(tree.parent)
                verticalScrollBar.viewSize = tree.scrollWindowHeight
                verticalScrollBar.scrollMaximum = displayedItems
                verticalScrollBar.scrollPosition = scrollPosition
                verticalScrollBar.draw(
                    graphics.newTextGraphics(
                        TerminalPosition(tree.columns - 1, 0),
                        TerminalSize(1, graphics.size.rows)
                    )
                )
            }
        }

        private fun drawTreeNode(
            graphics: TextGUIGraphics,
            themeDefinition: ThemeDefinition,
            treeNode: TreeNode<V>,
            level: Int,
            depth: Int,
            columns: Int
        ): Int {
            val offset = if (displayBrackets) 0 else -1
            if (treeNode.isFocused) {
                graphics.applyThemeStyle(themeDefinition.active)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            val labelOffset = 4 + indent * level + offset * 2
            val label = if (displayBlock) {
                getBlockLabel(
                    labelOffset,
                    treeNode.label,
                    columns,
                    themeDefinition.getCharacter(DISPLAY_BLOCK_FILLER, '.')
                )
            } else {
                treeNode.label
            }
            graphics.putString(labelOffset, depth, label)

            if (displayBrackets) {
                if (treeNode.isFocused) {
                    graphics.applyThemeStyle(themeDefinition.preLight)
                } else {
                    graphics.applyThemeStyle(themeDefinition.normal)
                }
                graphics.setCharacter(indent * level, depth, themeDefinition.getCharacter(LEFT_BRACKET, '['))
                graphics.setCharacter(
                    2 + indent * level,
                    depth,
                    themeDefinition.getCharacter(RIGHT_BRACKET, ']')
                )
            }
            graphics.setCharacter(3 + indent * level + 2 * offset, depth, ' ')

            if (treeNode.isFocused) {
                graphics.applyThemeStyle(themeDefinition.selected)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            val marker = getMarker(themeDefinition, treeNode)
            graphics.setCharacter(1 + indent * level + offset, depth, marker)

            var newDepth = depth + 1
            if (treeNode.isExpanded) {
                for (child in treeNode.children) {
                    if (child.isVisible) {
                        newDepth =
                            drawTreeNode(graphics, themeDefinition, child, level + 1, newDepth, columns)
                    }
                }
            }

            return newDepth
        }

        private fun getBlockLabel(labelOffset: Int, label: String, columns: Int, filler: Char): String {
            val fillSpace = columns - labelOffset - label.length
            var result = label
            if (fillSpace > 0) {
                val sb = StringBuilder()
                for (i in 0 until fillSpace) {
                    sb.append(filler)
                }
                result = sb.append(result).toString()
            }
            return result
        }

        private fun getMarker(themeDefinition: ThemeDefinition, treeNode: TreeNode<V>): Char {
            var marker = themeDefinition.getCharacter(COLLAPSED_MARKER, '>')
            if (treeNode.isExpanded) {
                marker = themeDefinition.getCharacter(EXPANDED_MARKER, '<')
            } else if (treeNode.isLeaf) {
                marker = themeDefinition.getCharacter(LEAF_MARKER, '.')
            }
            return marker
        }

        companion object {
            @JvmField
            val DEFAULT_TREE_LEVEL_INDENT: Int = 1

            @JvmField
            val LEFT_BRACKET: String = "LEFT_BRACKET"

            @JvmField
            val RIGHT_BRACKET: String = "RIGHT_BRACKET"

            @JvmField
            val EXPANDED_MARKER: String = "EXPANDED_MARKER"

            @JvmField
            val COLLAPSED_MARKER: String = "COLLAPSED_MARKER"

            @JvmField
            val LEAF_MARKER: String = "LEAF_MARKER"

            @JvmField
            val TREE_LEVEL_INDENT: String = "TREE_LEVEL_INDENT"

            @JvmField
            val DISPLAY_BRACKETS: String = "DISPLAY_BRACKETS"

            @JvmField
            val DISPLAY_BLOCK: String = "DISPLAY_BLOCK"

            @JvmField
            val DISPLAY_BLOCK_FILLER: String = "DISPLAY_BLOCK_FILLER"
        }
    }
}
