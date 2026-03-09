package com.googlecode.lanterna.gui2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TreeUnitTests {
    @Test
    fun constructorInitializesRootSelection() {
        val root = TreeNode("root", true)
        val tree = Tree(root, 20, 5)
        assertSame(root, tree.root)
        assertSame(root, tree.selectedNode)
        assertEquals(20, tree.size.columns)
    }
}
