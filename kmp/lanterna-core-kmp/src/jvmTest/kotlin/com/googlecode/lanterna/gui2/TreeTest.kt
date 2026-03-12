package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import java.io.IOException

class TreeTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("TreeTest")

        val layout = BorderLayout()
        val basePanel = Panel()
        basePanel.setLayoutManager(layout)

        val bracketTree = createTestTree()
        bracketTree.setDisplayRoot(false)
        bracketTree.isOverflowCircle = true
        bracketTree.setNodeSelectedConsumer({ node -> System.out.println("Selected leaf node: " + node!!.label) })

        val bracketTreePanel = Panel()
        bracketTreePanel.addComponent(bracketTree)
        bracketTreePanel.setPreferredSize(TerminalSize(35, 15))
        basePanel.addComponent(bracketTreePanel, BorderLayout.Location.LEFT)

        val noBracketTree = createTestTree()
        val theme = SimpleTheme(TextColor.ANSI.BLUE, TextColor.ANSI.WHITE)
        val definition = theme.defaultDefinition
        definition.setActive(TextColor.ANSI.RED, TextColor.ANSI.CYAN)
        definition.setCharacter(Tree.DefaultTreeRenderer.LEAF_MARKER, '*')
        definition.setCharacter(Tree.DefaultTreeRenderer.DISPLAY_BLOCK_FILLER, '.')
        definition.setIntegerProperty(Tree.DefaultTreeRenderer.TREE_LEVEL_INDENT, 2)
        definition.setBooleanProperty(Tree.DefaultTreeRenderer.DISPLAY_BRACKETS, false)
        definition.setBooleanProperty(Tree.DefaultTreeRenderer.DISPLAY_BLOCK, true)
        noBracketTree.setTheme(theme)
        noBracketTree.setNodeSelectedConsumer({ node -> System.out.println("Selected leaf node: " + node!!.label) })

        val noBracketTreePanel = Panel()
        noBracketTreePanel.addComponent(noBracketTree)
        noBracketTreePanel.setPreferredSize(TerminalSize(35, 15))

        basePanel.addComponent(bracketTreePanel.withBorder(Borders.singleLine("Bracket Tree")), BorderLayout.Location.LEFT)
        basePanel.addComponent(noBracketTreePanel.withBorder(Borders.singleLine("No bracket in block Tree")), BorderLayout.Location.RIGHT)

        window.component = basePanel
        textGUI.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            TreeTest().run(arrayOf<String?>("--mouse-click"))
        }

        private fun createTestTree(): Tree<String> {
            val root = TreeNode("root", true)
            val child1 = root.addChild("child_1", false)
            val child2 = root.addChild("child_2")

            root.addChild("child_3", true)
            root.addChild("child_44", true)

            val child4 = child1!!.addChild("child_4", true)
            child4!!.addChild("child_5", true)
            child4!!.addChild("child_6", true)
            child4!!.addChild("child_7", true)

            var transitiveParent = child4
            for (i in 9..19) {
                transitiveParent = transitiveParent!!.addChild("child_" + i, true)
                if (transitiveParent!!.label == "child_9") {
                    transitiveParent!!.setVisible(false)
                    transitiveParent!!.label = "child_9 (hidden)"
                }
            }

            transitiveParent = child2
            for (i in 20..29) {
                transitiveParent = transitiveParent!!.addChild("child_" + i, true)
            }

            return Tree(root, 35, 15)
        }
    }
}
