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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TextCharacter
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.font.FontRenderContext
import java.awt.geom.Rectangle2D
import java.lang.reflect.Modifier
import java.util.Collections

/**
 * This class encapsulates the font information used by an [AWTTerminal]. By customizing this class, you can
 * choose which fonts are going to be used by an [AWTTerminal] component and some other related settings.
 */
open class AWTTerminalFontConfiguration
    protected constructor(
        private val useAntiAliasing: Boolean,
        private val boldMode: BoldMode,
        vararg fontsInOrderOfPriority: Font,
    ) {
        private val fontPriority: MutableList<Font> = fontsInOrderOfPriority.toMutableList()
        val fontWidth: Int
        val fontHeight: Int

        enum class BoldMode {
            EVERYTHING,
            EVERYTHING_BUT_SYMBOLS,
            NOTHING,
        }

        init {
            require(fontPriority.isNotEmpty()) {
                "Must pass in a valid list of fonts to SwingTerminalFontConfiguration"
            }
            fontWidth = getFontWidth(fontPriority[0])
            fontHeight = getFontHeight(fontPriority[0])

            for (font in fontPriority) {
                require(isFontMonospaced(font)) { "Font $font isn't monospaced!" }
            }

            for (i in 1 until fontPriority.size) {
                var font = fontPriority[i]
                while (getFontWidth(font) > fontWidth || getFontHeight(font) > fontHeight) {
                    val newSize = font.size2D - 0.5f
                    if (newSize < 0.01f) {
                        throw IllegalStateException(
                            "Unable to shrink font ${i + 1} to fit the size of highest priority font ${fontPriority[0]}",
                        )
                    }
                    font = font.deriveFont(newSize)
                    fontPriority[i] = font
                }
            }
        }

        fun isAntiAliased(): Boolean = useAntiAliasing

        internal fun getFontForCharacter(character: TextCharacter): Font {
            var normalFont = getFontForCharacter(character.characterString)
            if (
                boldMode == BoldMode.EVERYTHING ||
                (boldMode == BoldMode.EVERYTHING_BUT_SYMBOLS && isNotASymbol(character.characterString[0]))
            ) {
                if (character.isBold) {
                    normalFont = normalFont.deriveFont(Font.BOLD)
                }
            }
            if (character.isItalic) {
                normalFont = normalFont.deriveFont(Font.ITALIC)
            }
            return normalFont
        }

        private fun getFontForCharacter(string: String): Font {
            for (font in fontPriority) {
                if (font.canDisplayUpTo(string) == -1) {
                    return font
                }
            }
            return fontPriority[0]
        }

        internal fun getFontWidth(font: Font): Int = font.getStringBounds("W", fontRenderContext).width.toInt()

        internal fun getFontHeight(font: Font): Int = font.getStringBounds("W", fontRenderContext).height.toInt()

        private val fontRenderContext: FontRenderContext
            get() =
                FontRenderContext(
                    null,
                    if (useAntiAliasing) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
                    RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT,
                )

        private fun isNotASymbol(character: Char): Boolean = !SYMBOLS_CACHE.contains(character)

        companion object {
            const val DEFAULT_FONT_SIZE: Int = 14

            private val MONOSPACE_CHECK_OVERRIDE: Set<String> =
                Collections.unmodifiableSet(
                    linkedSetOf(
                        "VL Gothic Regular",
                        "NanumGothic",
                        "WenQuanYi Zen Hei Mono",
                        "WenQuanYi Zen Hei",
                        "AR PL UMing TW",
                        "AR PL UMing HK",
                        "AR PL UMing CN",
                    ),
                )

            private val SYMBOLS_CACHE: MutableSet<Char> =
                linkedSetOf<Char>().apply {
                    for (field in Symbols::class.java.fields) {
                        if (
                            field.type == Char::class.javaPrimitiveType &&
                            Modifier.isFinal(field.modifiers) &&
                            Modifier.isStatic(field.modifiers)
                        ) {
                            try {
                                add(field.getChar(null))
                            } catch (_: IllegalArgumentException) {
                                // Should never happen.
                            } catch (_: IllegalAccessException) {
                                // Should never happen.
                            }
                        }
                    }
                }

            val default: AWTTerminalFontConfiguration
                get() = newInstance(*filterMonospaced(*selectDefaultFont(DEFAULT_FONT_SIZE)))

            fun getDefaultOfSize(fontSize: Int): AWTTerminalFontConfiguration {
                return newInstance(*filterMonospaced(*selectDefaultFont(fontSize)))
            }

            fun newInstance(vararg fontsInOrderOfPriority: Font): AWTTerminalFontConfiguration {
                return AWTTerminalFontConfiguration(true, BoldMode.EVERYTHING_BUT_SYMBOLS, *fontsInOrderOfPriority)
            }

            fun filterMonospaced(vararg fonts: Font): Array<Font> {
                val result = ArrayList<Font>(fonts.size)
                for (font in fonts) {
                    if (isFontMonospaced(font)) {
                        result.add(font)
                    }
                }
                return result.toTypedArray()
            }

            @JvmStatic
            protected fun selectDefaultFont(): Array<Font> = selectDefaultFont(DEFAULT_FONT_SIZE)

            @JvmStatic
            protected fun selectDefaultFont(fontSize: Int): Array<Font> {
                val osName = System.getProperty("os.name", "").lowercase()
                return when {
                    osName.contains("win") -> getDefaultWindowsFonts(fontSize).toTypedArray()
                    osName.contains("linux") -> getDefaultLinuxFonts(fontSize).toTypedArray()
                    else -> getDefaultFonts(fontSize).toTypedArray()
                }
            }

            private fun getDefaultWindowsFonts(fontSize: Int): List<Font> {
                val adjustedFontSize = getAdjustedFontSize(fontSize)
                return listOf(
                    Font("Courier New", Font.PLAIN, adjustedFontSize),
                    Font("Monospaced", Font.PLAIN, adjustedFontSize),
                )
            }

            private fun getDefaultLinuxFonts(fontSize: Int): List<Font> {
                val adjustedFontSize = getAdjustedFontSize(fontSize)
                return listOf(
                    Font("DejaVu Sans Mono", Font.PLAIN, adjustedFontSize),
                    Font("Monospaced", Font.PLAIN, adjustedFontSize),
                    Font("Ubuntu Mono", Font.PLAIN, adjustedFontSize),
                    Font("FreeMono", Font.PLAIN, adjustedFontSize),
                    Font("Liberation Mono", Font.PLAIN, adjustedFontSize),
                    Font("VL Gothic Regular", Font.PLAIN, adjustedFontSize),
                    Font("NanumGothic", Font.PLAIN, adjustedFontSize),
                    Font("WenQuanYi Zen Hei Mono", Font.PLAIN, adjustedFontSize),
                    Font("WenQuanYi Zen Hei", Font.PLAIN, adjustedFontSize),
                    Font("AR PL UMing TW", Font.PLAIN, adjustedFontSize),
                    Font("AR PL UMing HK", Font.PLAIN, adjustedFontSize),
                    Font("AR PL UMing CN", Font.PLAIN, adjustedFontSize),
                )
            }

            private fun getDefaultFonts(fontSize: Int): List<Font> {
                val adjustedFontSize = getAdjustedFontSize(fontSize)
                return listOf(Font("Monospaced", Font.PLAIN, adjustedFontSize))
            }

            private fun getAdjustedFontSize(fontSize: Int): Int {
                val javaVersion = System.getProperty("java.version", "1").split(".")
                return if (
                    System.getProperty("os.name", "").startsWith("Windows") &&
                    javaVersion.firstOrNull()?.toIntOrNull()?.let { it >= 9 } == true
                ) {
                    fontSize
                } else {
                    getHPIAdjustedFontSize(fontSize)
                }
            }

            private fun getHPIAdjustedFontSize(baseFontSize: Int): Int {
                val toolkit = Toolkit.getDefaultToolkit()
                if (toolkit.screenResolution >= 110) {
                    return toolkit.screenResolution / (baseFontSize / 2) + 1
                }
                val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
                val width = graphicsEnvironment.maximumWindowBounds.width
                return when {
                    width > 4096 -> baseFontSize * 4
                    width > 2560 -> baseFontSize * 2
                    else -> baseFontSize
                }
            }

            private fun isFontMonospaced(font: Font): Boolean {
                if (MONOSPACE_CHECK_OVERRIDE.contains(font.name)) {
                    return true
                }
                val fontRenderContext =
                    FontRenderContext(
                        null,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
                        RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT,
                    )
                val iBounds: Rectangle2D = font.getStringBounds("i", fontRenderContext)
                val mBounds: Rectangle2D = font.getStringBounds("W", fontRenderContext)
                return iBounds.width == mBounds.width
            }
        }
    }
