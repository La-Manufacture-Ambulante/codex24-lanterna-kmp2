package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import java.io.IOException

class TreeTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("TreeTest")
        val root = TreeNode("root", true)
        root.addChild("child", true)
        val tree = Tree(root, 35, 15)
        val panel = Panel()
        panel.addComponent(tree)
        window.component = panel
        textGUI?.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            TreeTest().run(args)
        }
    }
}
