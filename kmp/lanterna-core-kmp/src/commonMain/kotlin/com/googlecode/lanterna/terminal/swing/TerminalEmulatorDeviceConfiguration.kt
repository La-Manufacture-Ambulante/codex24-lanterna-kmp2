/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.TextColor

/**
 * Object that encapsulates the configuration parameters for the terminal 'device' that a SwingTerminal is emulating.
 * This includes properties such as the shape of the cursor, the color of the cursor, how large scrollback is available
 * and if the cursor should blink or not.
 * @author martin
 */
class TerminalEmulatorDeviceConfiguration @JvmOverloads constructor(
    /**
     * How many lines of history should be saved so the user can scroll back to them?
     * @return Number of lines in the scrollback buffer
     */
    val lineBufferScrollbackSize: Int = 2000,
    /**
     * Returns the length of a 'blink', which is the interval time a character with the blink SGR enabled with be drawn
     * with foreground color and background color set to the same.
     * @return Milliseconds of a blink interval
     */
    val blinkLengthInMilliSeconds: Int = 500,
    /**
     * Style the text cursor should take.
     * @return Text cursor style
     * @see CursorStyle
     */
    val cursorStyle: CursorStyle? = CursorStyle.REVERSED,
    /**
     * What color to draw the text cursor color in.
     * @return Color of the text cursor
     */
    val cursorColor: TextColor? = TextColor.RGB(255, 255, 255),
    /**
     * Should the text cursor be blinking.
     * @return `true` if the text cursor should be blinking
     */
    @get:Suppress("BooleanMethodIsAlwaysInverted")
    val isCursorBlinking: Boolean = false,
    val isClipboardAvailable: Boolean = true,
) {

    /**
     * Copies the current configuration. The new object has the given value.
     * @param blinkLengthInMilliSeconds How many milliseconds does a 'blink' last
     * @return A copy of the current configuration with the changed value.
     */
    fun withBlinkLengthInMilliSeconds(blinkLengthInMilliSeconds: Int): TerminalEmulatorDeviceConfiguration {
        if (this.blinkLengthInMilliSeconds == blinkLengthInMilliSeconds) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            isCursorBlinking,
            isClipboardAvailable,
        )
    }

    /**
     * Copies the current configuration. The new object has the given value.
     * @param lineBufferScrollbackSize How many lines of scrollback buffer should the terminal save?
     * @return A copy of the current configuration with the changed value.
     */
    fun withLineBufferScrollbackSize(lineBufferScrollbackSize: Int): TerminalEmulatorDeviceConfiguration {
        if (this.lineBufferScrollbackSize == lineBufferScrollbackSize) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            isCursorBlinking,
            isClipboardAvailable,
        )
    }

    /**
     * Copies the current configuration. The new object has the given value.
     * @param cursorStyle Style of the terminal text cursor
     * @return A copy of the current configuration with the changed value.
     */
    fun withCursorStyle(cursorStyle: CursorStyle?): TerminalEmulatorDeviceConfiguration {
        if (this.cursorStyle == cursorStyle) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            isCursorBlinking,
            isClipboardAvailable,
        )
    }

    /**
     * Copies the current configuration. The new object has the given value.
     * @param cursorColor Color of the terminal text cursor
     * @return A copy of the current configuration with the changed value.
     */
    fun withCursorColor(cursorColor: TextColor?): TerminalEmulatorDeviceConfiguration {
        if (this.cursorColor == cursorColor) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            isCursorBlinking,
            isClipboardAvailable,
        )
    }

    /**
     * Copies the current configuration. The new object has the given value.
     * @param cursorBlinking Should the terminal text cursor blink?
     * @return A copy of the current configuration with the changed value.
     */
    fun withCursorBlinking(cursorBlinking: Boolean): TerminalEmulatorDeviceConfiguration {
        if (isCursorBlinking == cursorBlinking) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            cursorBlinking,
            isClipboardAvailable,
        )
    }

    /**
     * Copies the current configuration. The new object has the given value.
     * @param clipboardAvailable Should the terminal support pasting text from the clipboard?
     * @return A copy of the current configuration with the changed value.
     */
    fun withClipboardAvailable(clipboardAvailable: Boolean): TerminalEmulatorDeviceConfiguration {
        if (isClipboardAvailable == clipboardAvailable) {
            return this
        }
        return TerminalEmulatorDeviceConfiguration(
            lineBufferScrollbackSize,
            blinkLengthInMilliSeconds,
            cursorStyle,
            cursorColor,
            isCursorBlinking,
            clipboardAvailable,
        )
    }

    /**
     * Different cursor styles supported by SwingTerminal.
     */
    enum class CursorStyle {
        /**
         * The cursor is drawn by inverting the front- and background colors of the cursor position.
         */
        REVERSED,

        /**
         * The cursor is drawn by using the cursor color as the background color for the character at the cursor position.
         */
        FIXED_BACKGROUND,

        /**
         * The cursor is rendered as a thick horizontal line at the bottom of the character.
         */
        UNDER_BAR,

        /**
         * The cursor is rendered as a left-side aligned vertical line.
         */
        VERTICAL_BAR,
    }

    companion object {
        /**
         * Static reference to the default terminal device configuration.
         * @return A terminal device configuration object with all settings set to default
         */
        val default: TerminalEmulatorDeviceConfiguration
            get() = TerminalEmulatorDeviceConfiguration()
    }
}
