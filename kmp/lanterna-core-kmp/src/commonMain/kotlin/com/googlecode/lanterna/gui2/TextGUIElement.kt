package com.googlecode.lanterna.gui2

/**
 * This interface is the base part in the Lanterna Text GUI component hierarchy
 * @author Martin
 */
interface TextGUIElement {
    /**
     * Draws the GUI element using the supplied TextGUIGraphics object. This is the main method to implement when you
     * want to create your own GUI components.
     * @param graphics Graphics object to use when drawing the component
     */
    fun draw(graphics: TextGUIGraphics?)

    /**
     * Checks if this element (or any of its child components, if any) has signaled that what it's currently displaying
     * is out of date and needs re-drawing.
     * @return `true` if the component is invalid and needs redrawing, `false` otherwise
     */
    fun isInvalid(): Boolean
}
