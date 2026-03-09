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
 * 
 * 
 * A `Tree` displays a hierarchical set of [TreeNode] instances and lets the user
 * navigate with keyboard and mouse, expand/collapse branches, and select a node. The component
 * renders using a simple text representation with optional brackets and a marker indicating
 * collapsed, expanded, or leaf state. Scrolling is supported when the visible area is smaller
 * than the number of visible nodes.
 * 
 * 
 * @param <V> Type of the value stored in each [TreeNode]
</V> */
 class Tree<V>/**
 * Creates a new `Tree` bound to a root node.
 * 
 * @param root               Root node of the tree; must not be `null`
 * @param columns            Width, in terminal columns, available for drawing the tree
 * @param scrollWindowHeight Height, in rows, of the scroll window (number of rows to display)
 * @throws IllegalArgumentException if `root` is `null`
 */
    (/**
 * Returns the root node of this tree.
 * 
 * @return Root [TreeNode]
 */
     val root:TreeNode<V?>?, private val columns:Int, private val scrollWindowHeight:Int):AbstractInteractableComponent<Tree<V?>?>() {
/**
 * Returns the currently selected (focused) node.
 * 
 * @return Selected [TreeNode]
 */
     var selectedNode:TreeNode<V?>? = null
private set
private var scrollingNode:TreeNode<V?>? = null

private var selectedNodeLevel:Int = 0
private var selectedNodeDepth:Int = 0

/**
 * Returns whether navigation wraps around when moving past the last/first node.
 * 
 * @return `true` if navigation overflows in a circle, `false` otherwise
 */
    /**
 * Enables or disables circular navigation when moving beyond the first or last node.
 * 
 * @param overflowCircle `true` to wrap around, `false` to stop at ends
 */
     var isOverflowCircle = false

private var nodeSelectedConsumer:Consumer<TreeNode<V?>?>? = null
private val listeners = CopyOnWriteArrayList()

 val size:TerminalSize
@Override
get() {
return TerminalSize(150, scrollWindowHeight)
}

/**
 * Returns whether the root node is displayed (visible) as part of the tree.
 * 
 * @return `true` if the root is visible, `false` otherwise
 */
    /**
 * Shows or hides the root node. When hidden, focus moves to the first visible child
 * if one exists.
 * 
 * @param displayRoot `true` to show the root node, `false` to hide it
 */
     var isDisplayRoot:Boolean
get() {
return root!!.isVisible()
}
set(displayRoot) {
root!!.setVisible(displayRoot)
if (!root!!.getChildren().isEmpty())
{
val treeNode = root!!.getChildren().get(0)
treeNode!!.setFocused(true)
this.selectedNode = treeNode
this.scrollingNode = treeNode
recomputeCursorPosition()
}
}

/**
 * Listener interface that can be attached to the `Tree` in order to be notified on user actions
 */
     interface Listener<V> {
/**
 * Called by the `Tree` when the user changes the toggle state of one item
 * @param treeNode that has been changed
 */
         fun onToggleChanged(treeNode:TreeNode<V?>?) 
}

init{
if (root == null) throw IllegalArgumentException("Root must not be null")
this.selectedNode = root
this.scrollingNode = root
this.root!!.setFocused(true)
recomputeCursorPosition()
}

/**
 * Computes the number of visible rows required to render the tree from the top-most
 * visible node to the last visible descendant, taking expansion state into account.
 * 
 * @return Total visible depth of the tree (at least 1)
 */
     fun computeTreeDepth():Int {
if (!root!!.isExpanded() || root!!.getChildren().isEmpty())
{
return 1
}
return root!!.getLastDirectChildren().getLastExpandedChildren().computeDepth()
}

private fun recomputeCursorPosition() {
this.selectedNodeLevel = selectedNode!!.computeLevel()
this.selectedNodeDepth = selectedNode!!.computeDepth()
}

private fun updateScrollingNode() {
if (scrollingNode!!.getPreviousNode() === selectedNode)
{
scrollingNode = selectedNode
}
else
{
var delta = scrollingNode!!.getDepthTo(selectedNode)
while (delta-- >= scrollWindowHeight)
{
scrollingNode = scrollingNode!!.getNextNode()
}
}
}

@Override
protected fun createDefaultRenderer():InteractableRenderer<Tree<V?>?>? {
val spacing = getThemeDefinition().getIntegerProperty(DefaultTreeRenderer.TREE_LEVEL_INDENT, DefaultTreeRenderer.DEFAULT_TREE_LEVEL_INDENT)
val displayBrackets = getThemeDefinition().getBooleanProperty(DefaultTreeRenderer.DISPLAY_BRACKETS, true)
val displayBblock = getThemeDefinition().getBooleanProperty(DefaultTreeRenderer.DISPLAY_BLOCK, false)
return DefaultTreeRenderer<Object?>(spacing, displayBrackets, displayBblock)
}

@Override
protected fun handleKeyStroke(keyStroke:KeyStroke?):Result? {
if (isKeyboardActivationStroke(keyStroke))
{
selectedNode!!.toggleExpanded()
if (nodeSelectedConsumer != null)
{
nodeSelectedConsumer!!.accept(selectedNode)
}
runOnGUIThreadIfExistsOtherwiseRunDirect({ for (listener in listeners)
{
listener!!.onToggleChanged(selectedNode)
} })
return Result.HANDLED
}
else if (keyStroke!!.getKeyType() === KeyType.MOUSE_EVENT)
{
val mouseAction = keyStroke as MouseAction?
val actionType = mouseAction!!.getActionType()

if (actionType === MouseActionType.SCROLL_UP)
{
focusPrevNode()
return Result.HANDLED
}
if (actionType === MouseActionType.SCROLL_DOWN)
{
focusNextNode()
return Result.HANDLED
}

val result = super.handleKeyStroke(keyStroke)
val selectedDepth = getSelectedDepthByMouseAction(mouseAction!!)
if (actionType === MouseActionType.CLICK_DOWN)
{
val nodeAtDepth = scrollingNode!!.getNodeAtDepth(selectedDepth)
if (nodeAtDepth != null)
{
nodeAtDepth!!.toggleExpanded()
selectedNode!!.setFocused(false)
nodeAtDepth!!.setFocused(true)
selectedNode = nodeAtDepth
recomputeCursorPosition()
}
return Result.HANDLED
}
return result
}
else if (!keyStroke!!.isAltDown() && !keyStroke!!.isCtrlDown() && !keyStroke!!.isShiftDown())
{
when (keyStroke!!.getKeyType()) {
ARROW_DOWN -> {
focusNextNode()
return Result.HANDLED
}
ARROW_UP -> {
focusPrevNode()
return Result.HANDLED
}
TAB -> return Result.MOVE_FOCUS_NEXT
REVERSE_TAB -> return Result.MOVE_FOCUS_PREVIOUS
ARROW_RIGHT -> return Result.MOVE_FOCUS_RIGHT
ARROW_LEFT -> return Result.MOVE_FOCUS_LEFT
HOME -> {
selectFirstNode()
return Result.HANDLED
}
END -> {
selectLastNode()
return Result.HANDLED
}

PAGE_UP -> {
for (i in 0 until scrollWindowHeight)
{
if (!focusPrevNode())
{
break
}
}
scrollingNode = selectedNode
return Result.HANDLED
}

PAGE_DOWN -> {
for (i in 0 until scrollWindowHeight)
{
if (!focusNextNode())
{
break
}
}
scrollingNode = selectedNode
return Result.HANDLED
}
else -> return Result.UNHANDLED
}
}
return Result.UNHANDLED
}

/**
 * Moves focus/selection to the first visible node.
 */
     fun selectFirstNode() {
selectedNode!!.setFocused(false)
val firstNode = if (isDisplayRoot) root else root!!.getChildren().get(0)
firstNode!!.setFocused(true)
this.selectedNode = firstNode
this.scrollingNode = firstNode
recomputeCursorPosition()
}

/**
 * Moves focus/selection to the last visible node.
 */
     fun selectLastNode() {
selectedNode!!.setFocused(false)
val lastNode = if (root!!.isExpanded()) root!!.getLastDirectChildren().getLastExpandedChildren() else root
lastNode!!.setFocused(true)
this.selectedNode = lastNode
updateScrollingNode()
recomputeCursorPosition()
}

/**
 * By converting [TerminalPosition]s to
 * [.toGlobal] gets index clicked on by mouse action.
 * 
 * @return index of an item that was clicked on with [MouseAction]
 */
    protected fun getSelectedDepthByMouseAction(click:MouseAction):Int {
return click.getPosition().getRow() - getGlobalPosition().getRow()
}

private fun focusNextNode():Boolean {
val nextNode = selectedNode!!.getNextNode()
if (nextNode == null && isOverflowCircle)
{
selectFirstNode()
return false
}
else if (nextNode != null)
{
selectedNode!!.setFocused(false)
nextNode!!.setFocused(true)
this.selectedNode = nextNode
updateScrollingNode()
recomputeCursorPosition()
return true
}
return false
}

private fun focusPrevNode():Boolean {
val previousNode = selectedNode!!.getPreviousNode()
if (previousNode == null && isOverflowCircle)
{
selectLastNode()
return false
}
else if (previousNode != null)
{
selectedNode!!.setFocused(false)
previousNode!!.setFocused(true)
this.selectedNode = previousNode
updateScrollingNode()
recomputeCursorPosition()
return true
}
return false
}

/**
 * Sets a consumer that will be invoked when a node is activated (for example by the
 * keyboard activation key or mouse click). The consumer receives the currently selected
 * node. This does not affect the internal expand/collapse behavior.
 * 
 * @param nodeSelectedConsumer Callback to invoke on node activation; may be `null`
 */
     fun setNodeSelectedConsumer(nodeSelectedConsumer:Consumer<TreeNode<V?>?>?) {
this.nodeSelectedConsumer = nodeSelectedConsumer
}

/**
 * Adds a new listener to the `Tree` that will be called on certain user actions
 * @param listener Listener to attach to this `Tree`
 * @return Itself
 */
    @Synchronized  fun addListener(listener:Listener<V?>?):Tree<V?> {
if (listener != null && !listeners.contains(listener))
{
listeners.add(listener)
}
return this
}

/**
 * Removes a listener from this `Tree` so that if it had been added earlier, it will no longer be
 * called on user actions
 * @param listener Listener to remove from this `Tree`
 * @return Itself
 */
     fun removeListener(listener:Listener<V?>?):Tree<V?> {
listeners.remove(listener)
return this
}

/**
 * Default renderer for [Tree]. It draws optional brackets, an expand/collapse/leaf
 * marker, and the node label with a configurable indent per level. It also manages an
 * optional vertical scrollbar when needed.
 * 
 * @param <V> Type of the value stored in each [TreeNode]
</V> */
     class DefaultTreeRenderer<V>/**
 * Creates a renderer with custom settings.
 * 
 * @param indent          Number of columns to indent per tree level (must be `>= 0`)
 * @param displayBrackets Whether to draw square brackets around the marker
 * @param displayBlock    Whether to fill the line up to the label with a block/filler
 * @throws IllegalArgumentException if `indent` is negative
 */
         @JvmOverloads  constructor(private val indent:Int = DEFAULT_TREE_LEVEL_INDENT, private val displayBrackets:Boolean = true, private val displayBlock:Boolean = false):InteractableRenderer<Tree<V?>?> {

private val verticalScrollBar:ScrollBar?

init{
if (indent < 0)
{
throw IllegalArgumentException("Indent must be >= 0")
}
this.verticalScrollBar = ScrollBar(Direction.VERTICAL)
}

@Override
 fun getCursorLocation(tree:Tree<V?>):TerminalPosition {
val offset = if (displayBrackets) 0 else -1
val row = tree.scrollingNode!!.getDepthTo(tree.selectedNode)
return TerminalPosition(1 + indent * tree.selectedNodeLevel + offset, row)
}

@Override
 fun getPreferredSize(tree:Tree<V?>):TerminalSize {
val bracketsSize = if (displayBrackets) 4 else 2
val width = if (tree.root!!.isExpanded())
tree.root!!.getMaxExpandedWidth(0, bracketsSize)
else
4 + TerminalTextUtils.getColumnWidth(tree.root!!.getLabel())
var height = 1
if (tree.root!!.isExpanded())
{
height = tree.root!!.getExpandedLength()
}

return TerminalSize(width, height)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics, tree:Tree<V?>) {
graphics.applyThemeStyle(tree.getThemeDefinition().getNormal())
graphics.fill(' ')
val scrollPosition = Math.max(0, tree.selectedNodeDepth - tree.scrollWindowHeight)

var activeNode = tree.scrollingNode
for (i in 0 until tree.scrollWindowHeight)
{
val level = activeNode!!.computeLevel()
drawTreeNode(graphics, tree.getThemeDefinition(), activeNode!!, level, i, tree.columns - 3)
activeNode = activeNode!!.getNextNode()
if (activeNode == null)
{
break
}
}

val displayedItems = tree.computeTreeDepth()
if (displayedItems > tree.scrollWindowHeight)
{
verticalScrollBar!!.onAdded(tree.getParent())
verticalScrollBar!!.setViewSize(tree.scrollWindowHeight)
verticalScrollBar!!.setScrollMaximum(displayedItems)
verticalScrollBar!!.setScrollPosition(scrollPosition)
verticalScrollBar!!.draw(graphics.newTextGraphics(
TerminalPosition(tree.columns - 1, 0), 
TerminalSize(1, graphics.getSize().getRows())))
}
}

private fun drawTreeNode(graphics:TextGUIGraphics?, themeDefinition:ThemeDefinition?, treeNode:TreeNode<V?>, level:Int, depth:Int, columns:Int):Int {
var depth = depth
val offset = if (displayBrackets) 0 else -1
if (treeNode.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
val labelOffset = 4 + indent * level + offset * 2
val label = if (displayBlock) getBlockLabel(labelOffset, treeNode.getLabel(), columns, themeDefinition!!.getCharacter(DISPLAY_BLOCK_FILLER, '.')) else treeNode.getLabel()
graphics!!.putString(labelOffset, depth, label)

if (displayBrackets)
{
if (treeNode.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
graphics!!.setCharacter(indent * level, depth, themeDefinition!!.getCharacter(LEFT_BRACKET, '['))
graphics!!.setCharacter(2 + indent * level, depth, themeDefinition!!.getCharacter(RIGHT_BRACKET, ']'))
}
graphics!!.setCharacter(3 + indent * level + 2 * offset, depth, ' ')

if (treeNode.isFocused())
{
graphics!!.applyThemeStyle(themeDefinition!!.getSelected())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
val marker = getMarker(themeDefinition!!, treeNode)
graphics!!.setCharacter(1 + indent * level + offset, depth, marker)

depth = depth + 1
if (treeNode.isExpanded())
{
for (child in treeNode.getChildren())
{
if (child!!.isVisible())
{
depth = drawTreeNode(graphics, themeDefinition, child!!, level + 1, depth, columns)
}
}
}

return depth
}

private fun getBlockLabel(labelOffset:Int, label:String, columns:Int, filler:Char):String? {
var label = label
val fillSpace = columns - labelOffset - label.length()
if (fillSpace > 0)
{
val sb = StringBuilder()
for (i in 0 until fillSpace)
{
sb.append(filler)
}
label = sb.append(label).toString()
}
return label
}

private fun getMarker(themeDefinition:ThemeDefinition, treeNode:TreeNode<V?>):Char {
var marker = themeDefinition.getCharacter(COLLAPSED_MARKER, '>')
if (treeNode.isExpanded())
{
marker = themeDefinition.getCharacter(EXPANDED_MARKER, '<')
}
else if (treeNode.isLeaf())
{
marker = themeDefinition.getCharacter(LEAF_MARKER, '.')
}
return marker
}

companion object {

 val DEFAULT_TREE_LEVEL_INDENT = 1

 val LEFT_BRACKET = "LEFT_BRACKET"
 val RIGHT_BRACKET = "RIGHT_BRACKET"
 val EXPANDED_MARKER = "EXPANDED_MARKER"
 val COLLAPSED_MARKER = "COLLAPSED_MARKER"
 val LEAF_MARKER = "LEAF_MARKER"

 val TREE_LEVEL_INDENT = "TREE_LEVEL_INDENT"

 val DISPLAY_BRACKETS = "DISPLAY_BRACKETS"
 val DISPLAY_BLOCK = "DISPLAY_BLOCK"
 val DISPLAY_BLOCK_FILLER = "DISPLAY_BLOCK_FILLER"
}
}/**
 * Creates a renderer with default settings.
 */
}
