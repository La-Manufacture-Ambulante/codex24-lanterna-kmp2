package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import org.junit.Test

import org.junit.Assert.*

/**
 * Unit tests for [Tree] and [TreeNode] behavior that do not require a live GUI.
 */
 class TreeUnitTests {

@Test(expected = IllegalArgumentException::class)
@JvmStatic  fun constructorRequiresNonNullRoot() {
Tree(null, 20, 5)
}

@Test
@JvmStatic  fun defaultsAfterConstruction() {
val root = TreeNode("root", true)
val tree = Tree(root, 20, 5)

assertSame(root, tree.getRoot())
assertSame(root, tree.getSelectedNode())
assertTrue(root.isFocused())
 // Size should be based on scroll window height
        val size = tree.getSize()
assertEquals(5, size!!.rows.toLong())
}

@Test
@JvmStatic  fun setDisplayRootMovesSelectionToFirstChild() {
val root = TreeNode("root", true)
val c1 = root.addChild("c1", true)
root.addChild("c2", true)
val tree = Tree(root, 20, 5)

assertSame(root, tree.getSelectedNode())
tree.setDisplayRoot(false)
assertSame(c1, tree.getSelectedNode())
assertTrue(c1!!.isFocused())

 // Toggling back shouldn't break anything
        tree.setDisplayRoot(true)
 // When re-enabling root display, we don't automatically move focus back to root
        assertSame(c1, tree.getSelectedNode())
}

@Test
@JvmStatic  fun computeTreeDepthSimpleAndExpanded() {
val root = TreeNode("root", true)
 // Collapsed root or no children => depth = 1
        val tree1 = Tree(root, 20, 5)
assertEquals(1, tree1.computeTreeDepth())

val c1 = root.addChild("c1", true)
val c2 = root.addChild("c2", false)
c1!!.addChild("c1-1", true)
c1!!.addChild("c1-2", true)

 // Last direct child of root is c2 (collapsed), so last expanded in that branch is c2 itself
        // Depth is computed from root.getLastDirectChildren().getLastExpandedChildren().computeDepth()
        // which counts visible nodes from the start
        assertTrue(root.isExpanded())
assertEquals(c2!!.getLastExpandedChildren().computeDepth(), tree1.computeTreeDepth())
}

@Test
@JvmStatic  fun invisibleNodesDoNotCountTowardsDepth() {
val root = TreeNode("root", true)
 // Collapsed root or no children => depth = 1
        val tree1 = Tree(root, 20, 5)
assertEquals(1, tree1.computeTreeDepth())

val c1 = root.addChild("c1", true)
c1!!.addChild("c1-1", true)
c1!!.setVisible(false)

 // c1 is not visible, so depth is still 1
        assertTrue(root.isExpanded())
assertEquals(1, root.getLastExpandedChildren().computeDepth())
}

@Test
@JvmStatic  fun navigationWithArrowKeys() {
val root = TreeNode("root", true)
val c1 = root.addChild("c1", true)
val c1a = c1!!.addChild("c1a", true)
c1!!.addChild("c1b", true)
val c2 = root.addChild("c2", true)

val tree = Tree(root, 20, 10)

 // No-op consumer to avoid NPE on activation strokes
        tree.setNodeSelectedConsumer({ n->  })

assertSame(root, tree.getSelectedNode())
 // Down -> c1
        tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
assertSame(c1, tree.getSelectedNode())
 // Down -> c1a (because c1 is expanded)
        tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
assertSame(c1a, tree.getSelectedNode())
 // Up -> c1
        tree.handleKeyStroke(KeyStroke(KeyType.ARROW_UP))
assertSame(c1, tree.getSelectedNode())

 // Home selects first visible node depending on displayRoot (default true)
        tree.handleKeyStroke(KeyStroke(KeyType.HOME))
assertSame(root, tree.getSelectedNode())

 // End selects last visible node
        tree.handleKeyStroke(KeyStroke(KeyType.END))
assertSame(c2, tree.getSelectedNode())
}

@Test
@JvmStatic  fun pageUpDownMoveByWindowAndPinScrollingNode() {
val root = TreeNode("root", true)
var p:TreeNode<String?>? = root
 // Create a deep chain of nodes to scroll through
        for (i in 0..19)
{
p = p!!.addChild("n" + i, true)
}
val tree = Tree(root, 20, 5)
tree.setNodeSelectedConsumer({ n->  })

 // Page down should advance up to 5 steps
        tree.handleKeyStroke(KeyStroke(KeyType.PAGE_DOWN))
 // We started at root, after 5 downs we should be at the 5th visible node from start
        val expected = root.getNodeAtDepth(5)
assertSame(expected, tree.getSelectedNode())

 // Page up goes back up
        tree.handleKeyStroke(KeyStroke(KeyType.PAGE_UP))
assertSame(root, tree.getSelectedNode())
}

@Test
@JvmStatic  fun overflowCircleWrapsAround() {
val root = TreeNode("root", true)
val c1 = root.addChild("c1", true)
val c2 = c1!!.addChild("c2", true)
val tree = Tree(root, 20, 5)
tree.setNodeSelectedConsumer({ n->  })

 // Move to last
        tree.handleKeyStroke(KeyStroke(KeyType.END))
assertSame(c2, tree.getSelectedNode())

 // Without overflow, pressing down does not move
        tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
assertSame(c2, tree.getSelectedNode())

 // Enable overflow and press down -> wraps to first
        tree.setOverflowCircle(true)
tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
assertSame(root, tree.getSelectedNode())

 // From first, pressing up wraps to last
        tree.handleKeyStroke(KeyStroke(KeyType.ARROW_UP))
assertSame(c2, tree.getSelectedNode())
}

@Test
@JvmStatic  fun mouseScrollMovesSelection() {
val root = TreeNode("root", true)
val c1 = root.addChild("c1", true)
c1!!.addChild("c2", true)
val tree = Tree(root, 20, 5)
tree.setNodeSelectedConsumer({ n->  })

assertSame(root, tree.getSelectedNode())
 // Scroll down behaves as arrow down
        val scrollDown = MouseAction(MouseActionType.SCROLL_DOWN, 0, TerminalPosition(0, 0))
tree.handleKeyStroke(scrollDown)
assertSame(c1, tree.getSelectedNode())

 // Scroll up behaves as arrow up
        val scrollUp = MouseAction(MouseActionType.SCROLL_UP, 0, TerminalPosition(0, 0))
tree.handleKeyStroke(scrollUp)
assertSame(root, tree.getSelectedNode())
}

@Test
@JvmStatic  fun rendererPreferredSizeAndCursorLocation() {
val root = TreeNode("root", true)
val c1 = root.addChild("c1", true)
c1!!.addChild("c1a", true)
val tree = Tree(root, 20, 5)

val renderer = Tree.DefaultTreeRenderer()

val preferred = renderer.getPreferredSize(tree)
 // Height follows current implementation: if root is expanded, use root.getExpandedLength(), else 1
        val expectedHeight = if (root.isExpanded()) root.getExpandedLength() else 1
assertEquals(expectedHeight.toLong(), preferred!!.rows.toLong())

 // Cursor should be at row equal to the distance from scrollingNode to selectedNode
        assertEquals(0, renderer.getCursorLocation(tree).getRow())

 // Move selection down and verify cursor row changes
        tree.setNodeSelectedConsumer({ n->  })
tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
 // Scrolling node remains at the root, so selected at depth 1
        assertEquals(1, renderer.getCursorLocation(tree).getRow())
tree.handleKeyStroke(KeyStroke(KeyType.ARROW_DOWN))
assertEquals(2, renderer.getCursorLocation(tree).getRow())
}
}
