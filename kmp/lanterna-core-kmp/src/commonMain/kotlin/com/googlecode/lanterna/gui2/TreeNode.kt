package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalTextUtils

/**
 * Model node used by [Tree] to represent hierarchical data.
 */
class TreeNode<V> @JvmOverloads constructor(
    val value: V,
    expanded: Boolean = true,
) {
    private var expandedBacking: Boolean = expanded
    private var focused: Boolean = false
    private var visible: Boolean = true
    var label: String = value?.toString() ?: ""
        set(value) {
            require(!value.contains("\n") && !value.contains("\r")) {
                "Multiline tree nodes are not supported"
            }
            field = value
        }

    var parent: TreeNode<V>? = null
    val children: MutableList<TreeNode<V>> = ArrayList()

    init {
        require(!label.contains("\n") && !label.contains("\r")) {
            "Multiline tree nodes are not supported"
        }
    }

    constructor(value: V, label: String?, expanded: Boolean) : this(value, expanded) {
        this.label = label ?: ""
    }

    fun addChild(node: TreeNode<V>?): TreeNode<V> {
        require(node != null) { "Node can't be null" }
        node.parent = this
        children.add(node)
        return node
    }

    fun addChild(value: V): TreeNode<V> = addChild(value, true)

    fun addChild(value: V, expanded: Boolean): TreeNode<V> {
        val node = TreeNode(value, expanded)
        return addChild(node)
    }

    fun removeChild(node: TreeNode<V>?) {
        if (node == null) {
            return
        }
        node.parent = null
        children.remove(node)
    }

    fun setExpanded(expanded: Boolean) {
        expandedBacking = expanded
    }

    fun isExpanded(): Boolean = children.isNotEmpty() && expandedBacking

    fun isFocused(): Boolean = focused

    fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    fun isVisible(): Boolean = visible

    fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    val expandedLength: Int
        get() {
            if (!expandedBacking) {
                return 0
            }
            var max = 0
            for (child in children) {
                if (child.isVisible()) {
                    max = maxOf(max, child.expandedLength)
                }
            }
            return max
        }

    fun getMaxExpandedWidth(level: Int, bracketSize: Int): Int {
        if (!expandedBacking) {
            return 0
        }

        var maxWidth = bracketSize + level + TerminalTextUtils.getColumnWidth(label)
        val nextLevel = level + 1
        for (child in children) {
            if (child.isVisible()) {
                maxWidth = maxOf(maxWidth, child.getMaxExpandedWidth(nextLevel, bracketSize))
            }
        }
        return maxWidth
    }

    val isLeaf: Boolean
        get() = children.isEmpty()

    fun toggleExpanded() {
        expandedBacking = !expandedBacking
    }

    fun lastExpandedChildren(): TreeNode<V> {
        if (children.isEmpty() || !expandedBacking) {
            return this
        }

        val lastVisibleChild = getLastVisibleChildren()
        if (lastVisibleChild != null) {
            return lastVisibleChild.lastExpandedChildren()
        }
        return this
    }

    fun computeLevel(): Int {
        var level = 0
        var previousLevelNode = parent
        while (previousLevelNode != null) {
            if (previousLevelNode.isVisible()) {
                level++
            }
            previousLevelNode = previousLevelNode.parent
        }
        return level
    }

    fun computeDepth(): Int {
        var depth = 1
        var previousNode = previousNode()
        while (previousNode != null && previousNode.isVisible()) {
            previousNode = previousNode.previousNode()
            depth++
        }
        return depth
    }

    fun getNodeAtDepth(depth: Int): TreeNode<V>? {
        var currentDepth = 0
        var currentNode: TreeNode<V>? = this
        while (currentDepth < depth && currentNode != null) {
            currentNode = currentNode.nextNode()
            currentDepth++
        }
        return currentNode
    }

    fun getDepthTo(targetNode: TreeNode<V>): Int {
        var currentDepth = 0
        var currentNode: TreeNode<V> = this
        while (currentNode.nextNode() != null && currentNode !== targetNode) {
            currentNode = currentNode.nextNode()!!
            currentDepth++
        }
        return if (currentNode === targetNode) currentDepth else -1
    }

    fun lastDirectChildren(): TreeNode<V> {
        return getLastVisibleChildren()
            ?: throw IndexOutOfBoundsException("Tree node has no visible children")
    }

    fun previousNode(): TreeNode<V>? {
        return parent?.getPreviousNode(this)
    }

    private fun getPreviousNode(treeNode: TreeNode<V>): TreeNode<V>? {
        val childIndex = children.indexOf(treeNode)
        val previousChildren = getLastVisibleChildren(childIndex - 1)
        return when {
            previousChildren != null -> previousChildren.lastExpandedChildren()
            isVisible() -> this
            parent != null -> parent?.getPreviousNode(this)
            else -> null
        }
    }

    fun nextNode(): TreeNode<V>? {
        val nextVisibleChildren = getFirstVisibleChildren()
        return if (isExpanded() && nextVisibleChildren != null) {
            nextVisibleChildren
        } else {
            parent?.getNextNode(this)
        }
    }

    private fun getNextNode(treeNode: TreeNode<V>): TreeNode<V>? {
        val childIndex = children.indexOf(treeNode)
        val nextChildren = getFirstVisibleChildren(childIndex + 1)
        return nextChildren ?: parent?.getNextNode(this)
    }

    private fun getFirstVisibleChildren(): TreeNode<V>? = getFirstVisibleChildren(0)

    private fun getFirstVisibleChildren(start: Int): TreeNode<V>? {
        for (index in start until children.size) {
            if (children[index].isVisible()) {
                return children[index]
            }
        }
        return null
    }

    private fun getLastVisibleChildren(): TreeNode<V>? = getLastVisibleChildren(children.size - 1)

    private fun getLastVisibleChildren(start: Int): TreeNode<V>? {
        var index = minOf(start, children.size - 1)
        while (index >= 0) {
            if (children[index].isVisible()) {
                return children[index]
            }
            index--
        }
        return null
    }
}
