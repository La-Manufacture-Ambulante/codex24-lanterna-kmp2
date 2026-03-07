package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalTextUtils

import java.util.ArrayList

/**
 * Model node used by [Tree] to represent hierarchical data.
 * 
 * 
 * A `TreeNode` contains a value and a label, a reference to its parent, and a list of
 * children. Nodes can be marked as expanded/collapsed and visible/hidden. Utility methods are
 * provided to navigate between visible nodes in depth-first order which is how [Tree]
 * renders and navigates.
 * 
 * 
 * @param <V> Type of the value associated with this node
</V> */
 class TreeNode<V> {

/**
 * Returns the value associated with this node.
 * 
 * @return The value; may be `null`
 */
     val value:V?
private var expanded:Boolean = false
/**
 * Returns whether this node currently has keyboard focus in a [Tree].
 * 
 * @return `true` if focused
 */
    /**
 * Sets whether this node currently has keyboard focus in a [Tree].
 * 
 * @param focused `true` if focused
 */
     var isFocused = false
/**
 * Returns whether this node is visible in the [Tree]. Invisible nodes are skipped
 * during rendering and navigation.
 * 
 * @return `true` if visible
 */
    /**
 * Sets whether this node should be visible in the [Tree].
 * 
 * @param visible `true` to make visible
 */
     var isVisible = true
private var label:String? = null

/**
 * Returns this node's parent, or `null` if it is a root node.
 * 
 * @return Parent node or `null`
 */
    /**
 * Sets this node's parent reference. Normally managed by [.addChild] and
 * [.removeChild].
 * 
 * @param parent New parent; may be `null`
 */
     var parent:TreeNode<V?>? = null
/**
 * Returns the mutable list of children for this node.
 * 
 * @return List of children (never `null`)
 */
     val children:List<TreeNode<V?>?>? = ArrayList()

/**
 * Returns whether this node is considered expanded and has at least one child.
 * 
 * @return `true` if expanded and non-empty, `false` otherwise
 */
    /**
 * Sets this node's expanded state. A node without children will be treated as collapsed
 * by [.isExpanded] regardless of this flag.
 * 
 * @param expanded `true` to mark as expanded
 */
     var isExpanded:Boolean
get() {
return !children.isEmpty() && expanded
}
set(expanded) {
this.expanded = expanded
}

/**
 * Computes how many rows of descendants this node contributes when expanded, considering
 * only visible children. Collapsed nodes contribute 0.
 * 
 * @return Number of visible rows contained under this node when expanded
 */
     val expandedLength:Int
get() {
if (!expanded)
{
return 0
}
var max = 0
for (child in children)
{
if (child!!.isVisible)
{
max = Math.max(max, child!!.expandedLength)
}
}
return max
}

/**
 * Returns whether this node has no children.
 * 
 * @return `true` if there are no children
 */
     val isLeaf:Boolean
get() {
return children.isEmpty()
}

/**
 * Returns the last visible descendant when following expanded branches, or this node if
 * there are no visible expanded descendants.
 * 
 * @return Last expanded visible descendant, or `this`
 */
     val lastExpandedChildren:TreeNode<V?>?
get() {
if (children.isEmpty() || !expanded)
{
return this
}

val lastVisibleChild = lastVisibleChildren
if (lastVisibleChild != null)
{
return lastVisibleChild!!.lastExpandedChildren
}
return this
}


/**
 * Returns the last direct child in the [.getChildren] list.
 * 
 * @return Last child node
 * @throws IndexOutOfBoundsException if there are no children
 */
     val lastDirectChildren:TreeNode<V?>?
get() {
return lastVisibleChildren
}

/**
 * Returns the previous visible node in a depth-first traversal, or `null` if this
 * is the first visible node.
 * 
 * @return Previous visible node or `null`
 */
     val previousNode:TreeNode<V?>?
get() {
if (parent != null)
{
return parent!!.getPreviousNode(this)
}
return null
}

/**
 * Returns the next visible node in a depth-first traversal, or `null` if this is the
 * last visible node.
 * 
 * @return Next visible node or `null`
 */
     val nextNode:TreeNode<V?>?
get() {
val nextVisibleChildren = firstVisibleChildren
if (isExpanded && nextVisibleChildren != null)
{
return nextVisibleChildren
}
else if (parent != null)
{
return parent!!.getNextNode(this)
}
return null
}

private val firstVisibleChildren:TreeNode<V?>?
get() {
return getFirstVisibleChildren(0)
}

private val lastVisibleChildren:TreeNode<V?>?
get() {
return getLastVisibleChildren(children.size() - 1)
}

/**
 * Creates a node with the label derived from `value.toString()` and the specified
 * expanded state.
 * 
 * @param value    Value held by this node; may be `null`
 * @param expanded `true` if the node should be initially expanded
 * @throws IllegalArgumentException if the generated label contains line breaks
 */
    @JvmOverloads  constructor(value:V?, expanded:Boolean = true) {
this.value = value
this.label = if (value == null) "" else value!!.toString()
if (label!!.contains("\n") || label!!.contains("\r"))
{
throw IllegalArgumentException("Multiline tree nodes are not supported")
}
this.expanded = expanded
}

/**
 * Creates a node with an explicit label and expanded state.
 * 
 * @param value    Value held by this node; may be `null`
 * @param label    Text displayed for this node; line breaks are not allowed
 * @param expanded `true` if the node should be initially expanded
 * @throws IllegalArgumentException if `label` contains line breaks
 */
     constructor(value:V?, label:String?, expanded:Boolean) {
this.value = value
this.label = if (label == null) "" else label
if (this.label!!.contains("\n") || this.label!!.contains("\r"))
{
throw IllegalArgumentException("Multiline tree nodes are not supported")
}
this.expanded = expanded
}

/**
 * Adds an existing node as a child of this node.
 * 
 * @param node Child node to add; must not be `null`
 * @return The same `node` instance for chaining
 * @throws IllegalArgumentException if `node` is `null`
 */
     fun addChild(node:TreeNode<V?>?):TreeNode<V?> {
if (node == null)
{
throw IllegalArgumentException("Node can't be null")
}
node!!.parent = this
children.add(node)
return node
}

/**
 * Creates and adds a child with the label derived from `value.toString()` and the
 * specified expanded state.
 * 
 * @param value    Value for the new child node; may be `null`
 * @param expanded `true` if the child should be initially expanded
 * @return The created child node
 */
    @JvmOverloads  fun addChild(value:V?, expanded:Boolean = true):TreeNode<V?>? {
val node = TreeNode<V?>(value, expanded)
return addChild(node)
}

/**
 * Removes a child node from this node. If the child is not present this is a no-op.
 * 
 * @param node Child node to remove; if `null` nothing happens
 */
     fun removeChild(node:TreeNode<V?>?) {
if (node == null)
{
return 
}
node!!.parent = null
children.remove(node)
}

/**
 * Returns the label displayed for this node.
 * 
 * @return Node label (never `null`)
 */
     fun getLabel():String? {
return label
}

/**
 * Sets the label displayed for this node. Line breaks are not allowed.
 * 
 * @param label New label; `null` is treated as an empty string
 */
     fun setLabel(label:String) {
if (label.contains("\n") || label.contains("\r"))
{
throw IllegalArgumentException("Multiline tree nodes are not supported")
}
this.label = label
}

/**
 * Computes the maximum width, in columns, needed to render this node and its visible
 * descendants when expanded.
 * 
 * @param level       Current indentation level (0 for root)
 * @param bracketSize Number of columns used by brackets/markers before the label
 * @return Maximum width in columns
 */
     fun getMaxExpandedWidth(level:Int, bracketSize:Int):Int {
if (!expanded)
{
return 0
}

var maxWith = bracketSize + level + TerminalTextUtils.getColumnWidth(label)
val nextLevel = level + 1
for (child in children)
{
if (child!!.isVisible)
{
maxWith = Math.max(maxWith, child!!.getMaxExpandedWidth(nextLevel, bracketSize))
}
}

return maxWith
}

/**
 * Toggles this node's expanded state.
 */
     fun toggleExpanded() {
this.expanded = !this.expanded
}

/**
 * Computes the number of visible ancestors of this node, effectively the indentation level
 * used when rendering in [Tree].
 * 
 * @return Level starting at 0 for the root
 */
     fun computeLevel():Int {
var level = 0
var prevLevelNode = parent
while (prevLevelNode != null)
{
if (prevLevelNode!!.isVisible)
{
level++
}
prevLevelNode = prevLevelNode!!.parent
}
return level
}

/**
 * Computes the 1-based depth index of this node in a depth-first, visible-only traversal,
 * counting from the first visible node above it.
 * 
 * @return Depth index (minimum 1)
 */
     fun computeDepth():Int {
var depth = 1
var prevNode = previousNode
while (prevNode != null && prevNode!!.isVisible)
{
prevNode = prevNode!!.previousNode
depth++
}
return depth
}

/**
 * Returns the node that is `depth` steps ahead of this node in a visible-only
 * depth-first traversal.
 * 
 * @param depth Number of steps to move forward (0 returns this node)
 * @return Target node or `null` if the traversal ends before reaching the depth
 */
     fun getNodeAtDepth(depth:Int):TreeNode<V?>? {
var currentDepth = 0
var currentNode:TreeNode<V?>? = this
while (currentDepth < depth && currentNode != null)
{
currentNode = currentNode!!.nextNode
currentDepth++
}
return currentNode
}

/**
 * Computes how many visible steps ahead the `targetNode` is from this node in a
 * depth-first traversal.
 * 
 * @param targetNode Node to measure distance to
 * @return Non-negative distance if reachable by moving forward, otherwise `-1`
 */
     fun getDepthTo(targetNode:TreeNode<V?>?):Int {
var currentDepth = 0
var currentNode:TreeNode<V?>? = this
while (currentNode!!.nextNode != null && currentNode !== targetNode)
{
currentNode = currentNode!!.nextNode
currentDepth++
}
return if (currentNode === targetNode) currentDepth else -1
}

private fun getPreviousNode(treeNode:TreeNode<V?>?):TreeNode<V?>? {
val childIndex = children.indexOf(treeNode)
val prevChildren = getLastVisibleChildren(childIndex - 1)
if (prevChildren != null)
{
return prevChildren!!.lastExpandedChildren
}
else if (isVisible)
{
return this
}
else if (parent != null)
{
return parent!!.getPreviousNode(treeNode)
}
return null
}

private fun getNextNode(treeNode:TreeNode<V?>?):TreeNode<V?>? {
val childIndex = children.indexOf(treeNode)
val nextChildren = getFirstVisibleChildren(childIndex + 1)
if (nextChildren != null)
{
return nextChildren
}
else if (parent != null)
{
return parent!!.getNextNode(this)
}
return null
}

private fun getFirstVisibleChildren(start:Int):TreeNode<V?>? {
for (i in start until children.size())
{
if (children.get(i).isVisible())
{
return children.get(i)
}
}
return null
}

private fun getLastVisibleChildren(start:Int):TreeNode<V?>? {
var start = start
start = Math.min(start, children.size() - 1)
for (i in start downTo 0)
{
if (children.get(i).isVisible())
{
return children.get(i)
}
}
return null
}
}/**
 * Creates an expanded node with the label derived from `value.toString()`.
 * 
 * @param value Value held by this node; may be `null`
 *//**
 * Convenience method to create and add an expanded child with the label derived from
 * `value.toString()`.
 * 
 * @param value Value for the new child node; may be `null`
 * @return The created child node
 */
