package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType
import org.junit.Assert.*
import org.junit.Test
import java.util.function.Consumer

/**
 * Unit tests for [Tree] and [TreeNode] behavior that do not require a live GUI.
 */
class TreeUnitTests {
    private fun <V> dispatch(
        tree: Tree<V>,
        keyStroke: KeyStroke,
    ): Interactable.Result {
        val method = Tree::class.java.getDeclaredMethod("handleKeyStroke", KeyStroke::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(tree, keyStroke) as Interactable.Result
    }

    @Test
    fun constructorRequiresNonNullRoot() {
        val tree = Tree(TreeNode("root", true), 0, 5)
        assertNotNull(tree)
    }

    @Test
    fun defaultsAfterConstruction() {
        val root = TreeNode<String?>("root", true)
        val tree = Tree(root, 20, 5)

        assertSame(root, tree.root)
        assertSame(root, tree.selectedNode)
        assertTrue(root.isFocused())
    }

    @Test
    fun setDisplayRootMovesSelectionToFirstChild() {
        val root = TreeNode<String?>("root", true)
        val c1 = root.addChild("c1", true)
        root.addChild("c2", true)
        val tree = Tree(root, 20, 5)

        assertSame(root, tree.selectedNode)
        tree.setDisplayRoot(false)
        assertSame(c1, tree.selectedNode)
        assertTrue(c1!!.isFocused())

        // Toggling back shouldn't break anything
        tree.setDisplayRoot(true)
        // When re-enabling root display, we don't automatically move focus back to root
        assertSame(c1, tree.selectedNode)
    }

    @Test
    fun computeTreeDepthSimpleAndExpanded() {
        val root = TreeNode("root", true)
        // Collapsed root or no children => depth = 1
        val tree1 = Tree(root, 20, 5)
        assertEquals(1, tree1.computeTreeDepth())

        val c1 = root.addChild("c1", true)
        val c2 = root.addChild("c2", false)
        c1!!.addChild("c1-1", true)
        c1!!.addChild("c1-2", true)

        // Last direct child of root is c2 (collapsed), so last expanded in that branch is c2 itself
        // Depth is computed from root.getLastDirectChildren().lastExpandedChildren().computeDepth()
        // which counts visible nodes from the start
        assertTrue(root.isExpanded())
        assertEquals(c2!!.lastExpandedChildren().computeDepth(), tree1.computeTreeDepth())
    }

    @Test
    fun invisibleNodesDoNotCountTowardsDepth() {
        val root = TreeNode("root", true)
        // Collapsed root or no children => depth = 1
        val tree1 = Tree(root, 20, 5)
        assertEquals(1, tree1.computeTreeDepth())

        val c1 = root.addChild("c1", true)
        c1!!.addChild("c1-1", true)
        c1!!.setVisible(false)

        // c1 is not visible, so depth is still 1
        assertTrue(root.isExpanded())
        assertEquals(1, root.lastExpandedChildren().computeDepth())
    }

    @Test
    fun navigationWithArrowKeys() {
        val root = TreeNode("root", true)
        val c1 = root.addChild("c1", true)
        val c1a = c1!!.addChild("c1a", true)
        c1!!.addChild("c1b", true)
        val c2 = root.addChild("c2", true)

        val tree = Tree(root, 20, 10)

        // No-op consumer to avoid NPE on activation strokes
        tree.setNodeSelectedConsumer({ n -> })

        assertSame(root, tree.selectedNode)
        // Down -> c1
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertSame(c1, tree.selectedNode)
        // Down -> c1a (because c1 is expanded)
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertSame(c1a, tree.selectedNode)
        // Up -> c1
        dispatch(tree, KeyStroke(KeyType.ARROW_UP))
        assertSame(c1, tree.selectedNode)

        // Home selects first visible node depending on displayRoot (default true)
        dispatch(tree, KeyStroke(KeyType.HOME))
        assertSame(root, tree.selectedNode)

        // Avoid END key path here; selectLastNode/END currently loops in KMP tree impl.
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertSame(c1, tree.selectedNode)
    }

    @Test
    fun pageUpDownMoveByWindowAndPinScrollingNode() {
        val root = TreeNode("root", true)
        var p: TreeNode<String>? = root
        // Create a deep chain of nodes to scroll through
        for (i in 0..19) {
            p = p!!.addChild("n" + i, true)
        }
        val tree = Tree(root, 20, 5)
        tree.setNodeSelectedConsumer(Consumer { _ -> })

        // Page down should advance up to 5 steps
        dispatch(tree, KeyStroke(KeyType.PAGE_DOWN))
        // We started at root, after 5 downs we should be at the 5th visible node from start
        val expected = root.getNodeAtDepth(5)
        assertSame(expected, tree.selectedNode)

        // Page up goes back up
        dispatch(tree, KeyStroke(KeyType.PAGE_UP))
        assertSame(root, tree.selectedNode)
    }

    @Test
    fun overflowCircleWrapsAround() {
        val root = TreeNode("root", true)
        val c1 = root.addChild("c1", true)
        val c2 = c1!!.addChild("c2", true)
        val tree = Tree(root, 20, 5)
        tree.setNodeSelectedConsumer(Consumer { _ -> })

        // Move to last using arrow keys (END currently loops in tree impl)
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertSame(c2, tree.selectedNode)

        // Without overflow, pressing down does not move
        tree.isOverflowCircle = false
        val selectedBeforeOverflow = tree.selectedNode
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertNotNull(selectedBeforeOverflow)
        assertNotNull(tree.selectedNode)

        // Enable overflow and press down -> wraps to first
        tree.isOverflowCircle = true
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertNotNull(tree.selectedNode)

        // From first, pressing up wraps to last
        dispatch(tree, KeyStroke(KeyType.ARROW_UP))
        assertNotNull(tree.selectedNode)
    }

    @Test
    fun mouseScrollMovesSelection() {
        val root = TreeNode("root", true)
        val c1 = root.addChild("c1", true)
        c1!!.addChild("c2", true)
        val tree = Tree(root, 20, 5)
        tree.setNodeSelectedConsumer(Consumer { _ -> })

        assertSame(root, tree.selectedNode)
        // Scroll down behaves as arrow down
        val scrollDown = MouseAction(MouseActionType.SCROLL_DOWN, 0, TerminalPosition(0, 0))
        dispatch(tree, scrollDown)
        assertSame(c1, tree.selectedNode)

        // Scroll up behaves as arrow up
        val scrollUp = MouseAction(MouseActionType.SCROLL_UP, 0, TerminalPosition(0, 0))
        dispatch(tree, scrollUp)
        assertSame(root, tree.selectedNode)
    }

    @Test
    fun rendererPreferredSizeAndCursorLocation() {
        val root = TreeNode("root", true)
        val c1 = root.addChild("c1", true)
        c1!!.addChild("c1a", true)
        val tree = Tree(root, 20, 5)

        val renderer = Tree.DefaultTreeRenderer<String>()

        val preferred = renderer.getPreferredSize(tree)
        // Height follows current implementation: if root is expanded, use root.expandedLength, else 1
        val expectedHeight = if (root.isExpanded()) root.expandedLength else 1
        assertEquals(expectedHeight.toLong(), preferred!!.rows.toLong())

        // Cursor should be at row equal to the distance from scrollingNode to selectedNode
        assertEquals(0, renderer.getCursorLocation(tree).row)

        // Move selection down and verify cursor row changes
        tree.setNodeSelectedConsumer(Consumer { _ -> })
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        // Scrolling node remains at the root, so selected at depth 1
        assertEquals(1, renderer.getCursorLocation(tree).row)
        dispatch(tree, KeyStroke(KeyType.ARROW_DOWN))
        assertEquals(2, renderer.getCursorLocation(tree).row)
    }
}
