package com.googlecode.lanterna.input

import com.googlecode.lanterna.TerminalPosition

/**
 * ScreenInfoAction, a KeyStroke in disguise, this class contains the reported position of the screen cursor.
 */
open class ScreenInfoAction(position: TerminalPosition?) : KeyStroke(KeyType.CURSOR_LOCATION) {
    private val position: TerminalPosition? = position

    /**
     * The location of the mouse cursor when this event was generated.
     * @return Location of the mouse cursor
     */
    open fun getPosition(): TerminalPosition? {
        return position
    }

    override fun toString(): String {
        return "ScreenInfoAction{position=" + position + '}'
    }
}
