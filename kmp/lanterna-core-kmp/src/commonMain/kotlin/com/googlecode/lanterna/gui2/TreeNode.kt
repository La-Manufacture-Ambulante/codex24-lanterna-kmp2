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

import com.googlecode.lanterna.TerminalTextUtils

/**
 * Model node used by [Tree] to represent hierarchical data.
 */
class TreeNode<V> @JvmOverloads constructor(
    val value: V?,
    expanded: Boolean = true,
) {
    private val childrenBacking = mutableListOf<TreeNode<V>>()
    private var expandedBacking: Boolean = expanded
    private var labelBacking: String = value?.toString() ?: ""

    var isFocused: Boolean = false
    var isVisible: Boolean = true
    var parent: TreeNode<V>? = null
        private set

    init {
        require(!labelBacking.contains('\n') && !labelBacking.contains('\r')) {
            "Multiline tree nodes are not supported"
        }
    }

    constructor(value: V?, label: String?, expanded: Boolean) : this(value, expanded) {
        setLabel(label ?: "")
    }

    val children: List<TreeNode<V>>
        get() = childrenBacking

    var isExpanded: Boolean
        get() = childrenBacking.isNotEmpty() && expandedBacking
        set(expanded) {
            expandedBacking = expanded
        }

    val expandedLength: Int
        get() {
            if (!isExpanded) {
                return 0
            }
            var max = 0
            for (child in childrenBacking) {
                if (child.isVisible) {
                    max = maxOf(max, child.expandedLength)
                }
            }
            return max + 1
        }

    val isLeaf: Boolean
        get() = childrenBacking.isEmpty()

    val lastExpandedChildren: TreeNode<V>
        get() {
            if (!isExpanded) {
                return this
            }
            val lastVisibleChild = lastVisibleChildren ?: return this
            return lastVisibleChild.lastExpandedChildren
        }

    val lastDirectChildren: TreeNode<V>
        get() = lastVisibleChildren ?: throw IndexOutOfBoundsException("Node has no visible children")

    val previousNode: TreeNode<V>?
        get() = parent?.getPreviousNode(this)

    val nextNode: TreeNode<V>?
        get() {
            val nextVisibleChild = firstVisibleChildren
            return if (isExpanded && nextVisibleChild != null) {
                nextVisibleChild
            } else {
                parent?.getNextNode(this)
            }
        }

    private val firstVisibleChildren: TreeNode<V>?
        get() = getFirstVisibleChildren(0)

    private val lastVisibleChildren: TreeNode<V>?
        get() = getLastVisibleChildren(childrenBacking.size - 1)

    fun addChild(node: TreeNode<V>?): TreeNode<V> {
        requireNotNull(node) { "Node can't be null" }
        node.parent = this
        childrenBacking.add(node)
        return node
    }

    @JvmOverloads
    fun addChild(value: V?, expanded: Boolean = true): TreeNode<V> {
        return addChild(TreeNode(value, expanded))
    }

    fun removeChild(node: TreeNode<V>?) {
        if (node == null) {
            return
        }
        if (childrenBacking.remove(node)) {
            node.parent = null
        }
    }

    fun getLabel(): String {
        return labelBacking
    }

    fun setLabel(label: String) {
        require(!label.contains('\n') && !label.contains('\r')) {
            "Multiline tree nodes are not supported"
        }
        labelBacking = label
    }

    fun getMaxExpandedWidth(level: Int, bracketSize: Int): Int {
        if (!isExpanded) {
            return 0
        }

        var maxWidth = bracketSize + level + TerminalTextUtils.getColumnWidth(labelBacking)
        val nextLevel = level + 1
        for (child in childrenBacking) {
            if (child.isVisible) {
                maxWidth = maxOf(maxWidth, child.getMaxExpandedWidth(nextLevel, bracketSize))
            }
        }
        return maxWidth
    }

    fun toggleExpanded() {
        expandedBacking = !expandedBacking
    }

    fun computeLevel(): Int {
        var level = 0
        var current = parent
        while (current != null) {
            if (current.isVisible) {
                level++
            }
            current = current.parent
        }
        return level
    }

    fun computeDepth(): Int {
        var depth = 1
        var current = previousNode
        while (current != null && current.isVisible) {
            depth++
            current = current.previousNode
        }
        return depth
    }

    fun getNodeAtDepth(depth: Int): TreeNode<V>? {
        var currentDepth = 0
        var currentNode: TreeNode<V>? = this
        while (currentDepth < depth && currentNode != null) {
            currentNode = currentNode.nextNode
            currentDepth++
        }
        return currentNode
    }

    fun getDepthTo(targetNode: TreeNode<V>?): Int {
        var currentDepth = 0
        var currentNode: TreeNode<V>? = this
        while (currentNode != null && currentNode !== targetNode) {
            currentNode = currentNode.nextNode
            currentDepth++
        }
        return if (currentNode === targetNode) currentDepth else -1
    }

    private fun getPreviousNode(treeNode: TreeNode<V>?): TreeNode<V>? {
        val childIndex = childrenBacking.indexOf(treeNode)
        val previousChild = getLastVisibleChildren(childIndex - 1)
        return when {
            previousChild != null -> previousChild.lastExpandedChildren
            isVisible -> this
            else -> parent?.getPreviousNode(this)
        }
    }

    private fun getNextNode(treeNode: TreeNode<V>): TreeNode<V>? {
        val childIndex = childrenBacking.indexOf(treeNode)
        val nextChild = getFirstVisibleChildren(childIndex + 1)
        return nextChild ?: parent?.getNextNode(this)
    }

    private fun getFirstVisibleChildren(start: Int): TreeNode<V>? {
        if (start < 0) {
            return null
        }
        for (index in start until childrenBacking.size) {
            val child = childrenBacking[index]
            if (child.isVisible) {
                return child
            }
        }
        return null
    }

    private fun getLastVisibleChildren(start: Int): TreeNode<V>? {
        var index = minOf(start, childrenBacking.size - 1)
        while (index >= 0) {
            val child = childrenBacking[index]
            if (child.isVisible) {
                return child
            }
            index--
        }
        return null
    }
}
