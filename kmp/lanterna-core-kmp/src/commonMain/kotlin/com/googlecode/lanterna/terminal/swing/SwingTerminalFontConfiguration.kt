package com.googlecode.lanterna.terminal.swing

import java.awt.Font

/**
 * Font configuration class for [SwingTerminal] that is extending from [AWTTerminalFontConfiguration]
 */
open class SwingTerminalFontConfiguration(
    useAntiAliasing: Boolean,
    boldMode: AWTTerminalFontConfiguration.BoldMode?,
    fontsInOrderOfPriority: Array<out Font?>?
) : AWTTerminalFontConfiguration(useAntiAliasing, boldMode, fontsInOrderOfPriority) {

    companion object {
        /**
         * This is the default font settings that will be used if you don't specify anything
         * @return A [SwingTerminal] font configuration object with default values set up
         */
        @JvmStatic
        fun getDefault(): SwingTerminalFontConfiguration {
            return newInstance(
                AWTTerminalFontConfiguration.filterMonospaced(
                    AWTTerminalFontConfiguration.selectDefaultFont(AWTTerminalFontConfiguration.DEFAULT_FONT_SIZE)
                )
            )
        }

        /**
         * Returns the default font settings except for a custom font size to use.
         * @param fontSize Size of the font
         * @return An [SwingTerminal] font configuration object with default values set up
         */
        @JvmStatic
        fun getDefaultOfSize(fontSize: Int): SwingTerminalFontConfiguration {
            return newInstance(
                AWTTerminalFontConfiguration.filterMonospaced(
                    AWTTerminalFontConfiguration.selectDefaultFont(fontSize)
                )
            )
        }

        /**
         * Creates a new font configuration from a list of fonts in order of priority. This works by having the terminal
         * attempt to draw each character with the fonts in the order they are specified in and stop once we find a font
         * that can actually draw the character. For ASCII characters, it's very likely that the first font will always be
         * used.
         * @param fontsInOrderOfPriority Fonts to use when drawing text, in order of priority
         * @return Font configuration built from the font list
         */
        @JvmStatic
        fun newInstance(fontsInOrderOfPriority: Array<out Font?>?): SwingTerminalFontConfiguration {
            return SwingTerminalFontConfiguration(
                true,
                AWTTerminalFontConfiguration.BoldMode.EVERYTHING_BUT_SYMBOLS,
                fontsInOrderOfPriority
            )
        }
    }
}
