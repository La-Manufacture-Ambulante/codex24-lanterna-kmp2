package com.googlecode.lanterna.gui2

/**
 * Simple AbstractWindow implementation that you can use as a building block when creating new windows without having
 * to create new classes.
 *
 * @author Martin
 */
class BasicWindow : AbstractWindow {

    /**
     * Default constructor, creates a new window with no title
     */
    constructor() : super()

    /**
     * This constructor creates a window with a specific title, that is (probably) going to be displayed in the window
     * decoration
     * @param title Title of the window
     */
    constructor(title: String?) : super(title)
}
