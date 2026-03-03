package com.googlecode.lanterna.gui2

/**
 * Factory class for creating [TextGUIThread] objects. This is used by [TextGUI] implementations to assign
 * their local [TextGUIThread] reference
 */
interface TextGUIThreadFactory {
    /**
     * Creates a new [TextGUIThread] objects
     * @param textGUI [TextGUI] this [TextGUIThread] should be associated with
     * @return The new [TextGUIThread]
     */
    fun createTextGUIThread(textGUI: TextGUI?): TextGUIThread?
}
