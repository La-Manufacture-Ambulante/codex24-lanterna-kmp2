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
    private val columns: Int,
    private val scrollWindowHeight: Int,
) : AbstractInteractableComponent<Tree<V>>() {
    interface Listener<V> {
        fun onToggleChanged(treeNode: TreeNode<V>)
    }

    var selectedNode: TreeNode<V> = root
        private set

    private var scrollingNode: TreeNode<V> = root
    private var selectedNodeLevel: Int = 0
    private var selectedNodeDepth: Int = 0
    var isOverflowCircle: Boolean = false
    private var nodeSelectedConsumer: Consumer<TreeNode<V>>? = null
    private val listeners = CopyOnWriteArrayList<Listener<V>>()

    init {
        root.setFocused(true)
        recomputeCursorPosition()
    }

    fun computeTreeDepth(): Int {
        if (!root.isExpanded() || root.children.isEmpty()) {
            return 1
        }
        return root.lastDirectChildren().lastExpandedChildren().computeDepth()
    }

    private fun recomputeCursorPosition() {
        selectedNodeLevel = selectedNode.computeLevel()
        selectedNodeDepth = selectedNode.computeDepth()
    }

    private fun getScrollingNode(): TreeNode<V> = scrollingNode

    private fun updateScrollingNode() {
        if (scrollingNode.previousNode() === selectedNode) {
            scrollingNode = selectedNode
        } else {
            var delta = scrollingNode.getDepthTo(selectedNode)
            while (delta-- >= scrollWindowHeight) {
                scrollingNode = scrollingNode.nextNode()
                    ?: throw IllegalStateException("Unexpected end of tree while updating scrolling node")
            }
        }
    }

    override fun createDefaultRenderer(): InteractableRenderer<Tree<V>?>? {
        val activeThemeDefinition = themeDefinition
        val spacing =
            activeThemeDefinition?.getIntegerProperty(
                DefaultTreeRenderer.TREE_LEVEL_INDENT,
                DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT,
            ) ?: DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT
        val displayBrackets =
            activeThemeDefinition?.getBooleanProperty(
                DefaultTreeRenderer.DISPLAY_BRACKETS,
                true,
            ) ?: true
        val displayBlock =
            activeThemeDefinition?.getBooleanProperty(
                DefaultTreeRenderer.DISPLAY_BLOCK,
                false,
            ) ?: false
        return DefaultTreeRenderer<V>(spacing, displayBrackets, displayBlock)
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result {
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

            val result = super.handleKeyStroke(keyStroke) ?: Interactable.Result.UNHANDLED
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
        selectedNode.setFocused(false)
        val firstNode = if (isDisplayRoot) root else root.children[0]
        firstNode.setFocused(true)
        selectedNode = firstNode
        scrollingNode = firstNode
        recomputeCursorPosition()
    }

    fun selectLastNode() {
        selectedNode.setFocused(false)
        val lastNode = if (root.isExpanded()) root.lastDirectChildren().lastExpandedChildren() else root
        lastNode.setFocused(true)
        selectedNode = lastNode
        updateScrollingNode()
        recomputeCursorPosition()
    }

    protected fun getSelectedDepthByMouseAction(click: MouseAction): Int {
        val clickRow = click.position?.row ?: 0
        val globalRow = globalPosition?.row ?: 0
        return clickRow - globalRow
    }

    private fun focusNextNode(): Boolean {
        val nextNode = selectedNode.nextNode()
        if (nextNode == null && isOverflowCircle) {
            selectFirstNode()
            return false
        } else if (nextNode != null) {
            selectedNode.setFocused(false)
            nextNode.setFocused(true)
            selectedNode = nextNode
            updateScrollingNode()
            recomputeCursorPosition()
            return true
        }
        return false
    }

    private fun focusPrevNode(): Boolean {
        val previousNode = selectedNode.previousNode()
        if (previousNode == null && isOverflowCircle) {
            selectLastNode()
            return false
        } else if (previousNode != null) {
            selectedNode.setFocused(false)
            previousNode.setFocused(true)
            selectedNode = previousNode
            updateScrollingNode()
            recomputeCursorPosition()
            return true
        }
        return false
    }

    fun setDisplayRoot(displayRoot: Boolean) {
        root.setVisible(displayRoot)
        if (root.children.isNotEmpty()) {
            val treeNode = root.children[0]
            treeNode.setFocused(true)
            selectedNode = treeNode
            scrollingNode = treeNode
            recomputeCursorPosition()
        }
    }

    val isDisplayRoot: Boolean
        get() = root.isVisible()

    fun setNodeSelectedConsumer(nodeSelectedConsumer: Consumer<TreeNode<V>>?) {
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
        listeners.remove(listener)
        return this
    }

    override fun calculatePreferredSize(): TerminalSize {
        return renderer?.getPreferredSize(this) ?: TerminalSize.ZERO
    }

    class DefaultTreeRenderer<V>(
        private val indent: Int = DEFAULT_TREE_LEVEL_INDENT,
        private val displayBrackets: Boolean = true,
        private val displayBlock: Boolean = false,
    ) : InteractableRenderer<Tree<V>?> {
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

        private val verticalScrollBar = ScrollBar(Direction.VERTICAL)

        init {
            require(indent >= 0) { "Indent must be >= 0" }
        }

        override fun getCursorLocation(component: Tree<V>?): TerminalPosition {
            val tree = component ?: return TerminalPosition.TOP_LEFT_CORNER
            val offset = if (displayBrackets) 0 else -1
            val row = tree.getScrollingNode().getDepthTo(tree.selectedNode)
            return TerminalPosition(1 + indent * tree.selectedNodeLevel + offset, row)
        }

        override fun getPreferredSize(component: Tree<V>?): TerminalSize {
            val tree = component ?: return TerminalSize.ZERO
            val bracketsSize = if (displayBrackets) 4 else 2
            val width =
                if (tree.root.isExpanded()) {
                    tree.root.getMaxExpandedWidth(0, bracketsSize)
                } else {
                    4 + TerminalTextUtils.getColumnWidth(tree.root.label)
                }
            var height = 1
            if (tree.root.isExpanded()) {
                height = tree.root.expandedLength
            }
            return TerminalSize(width, height)
        }

        override fun drawComponent(
            graphics: TextGUIGraphics?,
            component: Tree<V>?,
        ) {
            val activeGraphics = graphics ?: return
            val tree = component ?: return
            val activeThemeDefinition = tree.themeDefinition ?: return
            activeGraphics.applyThemeStyle(activeThemeDefinition.normal)
            activeGraphics.fill(' ')
            val scrollPosition = maxOf(0, tree.selectedNodeDepth - tree.scrollWindowHeight)

            var activeNode: TreeNode<V>? = tree.getScrollingNode()
            for (i in 0 until tree.scrollWindowHeight) {
                if (activeNode == null) {
                    break
                }
                val level = activeNode.computeLevel()
                drawTreeNode(activeGraphics, activeThemeDefinition, activeNode, level, i, tree.columns - 3)
                activeNode = activeNode.nextNode()
            }

            val displayedItems = tree.computeTreeDepth()
            if (displayedItems > tree.scrollWindowHeight) {
                verticalScrollBar.onAdded(tree.parent)
                verticalScrollBar.setViewSize(tree.scrollWindowHeight)
                verticalScrollBar.setScrollMaximum(displayedItems)
                verticalScrollBar.setScrollPosition(scrollPosition)
                verticalScrollBar.draw(
                    activeGraphics.newTextGraphics(
                        TerminalPosition(tree.columns - 1, 0),
                        TerminalSize(1, activeGraphics.size?.rows ?: 0),
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
            val offset = if (displayBrackets) 0 else -1
            if (treeNode.isFocused()) {
                graphics.applyThemeStyle(themeDefinition.active)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            val labelOffset = 4 + indent * level + offset * 2
            val label =
                if (displayBlock) {
                    getBlockLabel(
                        labelOffset,
                        treeNode.label,
                        columns,
                        themeDefinition.getCharacter(DISPLAY_BLOCK_FILLER, '.'),
                    )
                } else {
                    treeNode.label
                }
            graphics.putString(labelOffset, depth, label)

            if (displayBrackets) {
                if (treeNode.isFocused()) {
                    graphics.applyThemeStyle(themeDefinition.preLight)
                } else {
                    graphics.applyThemeStyle(themeDefinition.normal)
                }
                graphics.setCharacter(indent * level, depth, themeDefinition.getCharacter(LEFT_BRACKET, '['))
                graphics.setCharacter(2 + indent * level, depth, themeDefinition.getCharacter(RIGHT_BRACKET, ']'))
            }
            graphics.setCharacter(3 + indent * level + 2 * offset, depth, ' ')

            if (treeNode.isFocused()) {
                graphics.applyThemeStyle(themeDefinition.selected)
            } else {
                graphics.applyThemeStyle(themeDefinition.normal)
            }
            graphics.setCharacter(1 + indent * level + offset, depth, getMarker(themeDefinition, treeNode))

            var nextDepth = depth + 1
            if (treeNode.isExpanded()) {
                for (child in treeNode.children) {
                    if (child.isVisible()) {
                        nextDepth = drawTreeNode(graphics, themeDefinition, child, level + 1, nextDepth, columns)
                    }
                }
            }
            return nextDepth
        }

        private fun getBlockLabel(
            labelOffset: Int,
            label: String,
            columns: Int,
            filler: Char,
        ): String {
            val fillSpace = columns - labelOffset - label.length
            if (fillSpace > 0) {
                val sb = StringBuilder()
                repeat(fillSpace) {
                    sb.append(filler)
                }
                sb.append(label)
                return sb.toString()
            }
            return label
        }

        private fun getMarker(
            themeDefinition: ThemeDefinition,
            treeNode: TreeNode<V>,
        ): Char {
            return when {
                treeNode.isLeaf -> themeDefinition.getCharacter(LEAF_MARKER, ' ')
                treeNode.isExpanded() -> themeDefinition.getCharacter(EXPANDED_MARKER, '-')
                else -> themeDefinition.getCharacter(COLLAPSED_MARKER, '+')
            }
        }
    }
}
