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
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashSet

/**
 * This class encapsulates the font information used by an [AWTTerminal]. By customizing this class, you can
 * choose which fonts are going to be used by an [AWTTerminal] component and some other related settings.
 * @author martin
 */
 class AWTTerminalFontConfiguration @SuppressWarnings("WeakerAccess")
 protected constructor(/**
 * Returns `true` if anti-aliasing has been enabled, `false` otherwise
 * @return `true` if anti-aliasing has been enabled, `false` otherwise
 */
     val isAntiAliased:Boolean, private val boldMode:BoldMode?, vararg fontsInOrderOfPriority:Font?) {

private val fontPriority:List<Font?>?
/**
 * Returns the horizontal size in pixels of the fonts configured
 * @return Horizontal size in pixels of the fonts configured
 */
     val fontWidth:Int
/**
 * Returns the vertical size in pixels of the fonts configured
 * @return Vertical size in pixels of the fonts configured
 */
     val fontHeight:Int

private val fontRenderContext:FontRenderContext
get() {
return FontRenderContext(null, 
if (isAntiAliased)
RenderingHints.VALUE_TEXT_ANTIALIAS_ON
else
RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, 
RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)
}

/**
 * Controls how the SGR bold will take effect when enabled on a character. Mainly this is controlling if the
 * character should be rendered with a bold font or not. The reason for this is that some characters, notably the
 * lines and double-lines in defined in Symbol, usually doesn't look very good with bold font when you try to
 * construct a GUI.
 */
     enum class BoldMode {
/**
 * All characters with SGR Bold enabled will be rendered using a bold font
 */
        EVERYTHING, 
/**
 * All characters with SGR Bold enabled, except for the characters defined as constants in Symbols class, will
 * be rendered using a bold font
 */
        EVERYTHING_BUT_SYMBOLS, 
/**
 * Bold font will not be used for characters with SGR bold enabled
 */
        NOTHING
}

init{
if (fontsInOrderOfPriority == null || fontsInOrderOfPriority!!.size == 0)
{
throw IllegalArgumentException("Must pass in a valid list of fonts to SwingTerminalFontConfiguration")
}
this.fontPriority = ArrayList(Arrays.asList(fontsInOrderOfPriority))
this.fontWidth = getFontWidth(fontPriority!!.get(0))
this.fontHeight = getFontHeight(fontPriority!!.get(0))

 //Make sure all the fonts are monospace
        for (font in fontPriority!!)
{
if (!isFontMonospaced(font!!))
{
throw IllegalArgumentException("Font " + font + " isn't monospaced!")
}
}

 //Make sure all lower-priority fonts are less or equal in width and height, shrink if necessary
        for (i in 1 until fontPriority!!.size())
{
var font = fontPriority!!.get(i)
while (getFontWidth(font!!) > fontWidth || getFontHeight(font!!) > fontHeight)
{
val newSize = font!!.getSize2D() - 0.5f
if (newSize < 0.01)
{
throw IllegalStateException("Unable to shrink font " + (i + 1) + " to fit the size of highest priority font " + fontPriority!!.get(0))
}
font = font!!.deriveFont(newSize)
fontPriority!!.set(i, font)
}
}
}

/**
 * Given a certain character, return the font to use for drawing it. The method will go through all fonts passed in
 * to this [AWTTerminalFontConfiguration] in the order of priority specified and chose the first font which is
 * capable of drawing `character`. If no such font is found, the normal fonts is returned (and probably won't
 * be able to draw the character).
 * @param character Character to find a font for
 * @return Font which the `character` should be drawn using
 */
    internal fun getFontForCharacter(character:TextCharacter):Font? {
var normalFont:Font? = getFontForCharacter(character.getCharacterString())
if (boldMode == BoldMode.EVERYTHING || (boldMode == BoldMode.EVERYTHING_BUT_SYMBOLS && isNotASymbol(character.getCharacterString().charAt(0))))
{
if (character.isBold())
{
normalFont = normalFont!!.deriveFont(Font.BOLD)
}
}
if (character.isItalic())
{
normalFont = normalFont!!.deriveFont(Font.ITALIC)
}
return normalFont
}

private fun getFontForCharacter(string:String?):Font? {
for (font in fontPriority!!)
{
if (font!!.canDisplayUpTo(string) === -1)
{
return font
}
}
 //No available font here, what to do...?
        return fontPriority!!.get(0)
}

private fun getFontWidth(font:Font):Int {
return font.getStringBounds("W", fontRenderContext).getWidth() as Int
}

private fun getFontHeight(font:Font):Int {
return font.getStringBounds("W", fontRenderContext).getHeight() as Int
}

private fun isNotASymbol(character:Char):Boolean {
return !SYMBOLS_CACHE.contains(character)
}

companion object {
/**
 * The default font size used unless overridden
 */
     val DEFAULT_FONT_SIZE = 14

private val MONOSPACE_CHECK_OVERRIDE = Collections.unmodifiableSet(HashSet(Arrays.asList(
"VL Gothic Regular", 
"NanumGothic", 
"WenQuanYi Zen Hei Mono", 
"WenQuanYi Zen Hei", 
"AR PL UMing TW", 
"AR PL UMing HK", 
"AR PL UMing CN"
)))

private fun getDefaultWindowsFonts(fontSize:Int):List<Font?>? {
val adjustedFontSize = getAdjustedFontSize(fontSize)
return Collections.unmodifiableList(Arrays.asList(
Font("Courier New", Font.PLAIN, adjustedFontSize), //Monospaced can look pretty bad on Windows, so let's override it
                Font("Monospaced", Font.PLAIN, adjustedFontSize)))
}

private fun getDefaultLinuxFonts(fontSize:Int):List<Font?>? {
val adjustedFontSize = getAdjustedFontSize(fontSize)
return Collections.unmodifiableList(Arrays.asList(
Font("DejaVu Sans Mono", Font.PLAIN, adjustedFontSize), 
Font("Monospaced", Font.PLAIN, adjustedFontSize), 
 //Below, these should be redundant (Monospaced is supposed to catch-all)
                // but Java 6 seems to have issues with finding monospaced fonts sometimes
                Font("Ubuntu Mono", Font.PLAIN, adjustedFontSize), 
Font("FreeMono", Font.PLAIN, adjustedFontSize), 
Font("Liberation Mono", Font.PLAIN, adjustedFontSize), 
Font("VL Gothic Regular", Font.PLAIN, adjustedFontSize), 
Font("NanumGothic", Font.PLAIN, adjustedFontSize), 
Font("WenQuanYi Zen Hei Mono", Font.PLAIN, adjustedFontSize), 
Font("WenQuanYi Zen Hei", Font.PLAIN, adjustedFontSize), 
Font("AR PL UMing TW", Font.PLAIN, adjustedFontSize), 
Font("AR PL UMing HK", Font.PLAIN, adjustedFontSize), 
Font("AR PL UMing CN", Font.PLAIN, adjustedFontSize)))
}

private fun getDefaultFonts(fontSize:Int):List<Font?>? {
val adjustedFontSize = getAdjustedFontSize(fontSize)
return Collections.unmodifiableList(Collections.singletonList(
Font("Monospaced", Font.PLAIN, adjustedFontSize)))
}

 // Here we check the screen resolution on the primary monitor and make a guess at if it's high-DPI or not
    private fun getAdjustedFontSize(fontSize:Int):Int {
val baseFontSize = fontSize
val javaVersion = System.getProperty("java.version", "1").split("\\.")
if (System.getProperty("os.name", "").startsWith("Windows") && Integer.parseInt(javaVersion!![0]) >= 9)
{
 // Java 9+ reports itself as HiDPI-unaware on Windows and will be scaled by the OS
            // Keep in mind that Java 8 and earlier reports itself as version 1.X.0_YYY
            return baseFontSize
}
else
{
return getHPIAdjustedFontSize(baseFontSize)
}
}

private fun getHPIAdjustedFontSize(baseFontSize:Int):Int {
if (Toolkit.getDefaultToolkit().getScreenResolution() >= 110)
{
 // Rely on DPI if it is a high value.
            return Toolkit.getDefaultToolkit().getScreenResolution() / (baseFontSize / 2) + 1
}
else
{
 // Otherwise try to guess it from the monitor size:
            // If the width is wider than Full HD (1080p, or 1920x1080), then assume it's high-DPI.
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
if (ge!!.getMaximumWindowBounds().getWidth() > 4096)
{
return baseFontSize * 4
}
else if (ge!!.getMaximumWindowBounds().getWidth() > 2560)
{
return baseFontSize * 2
}
else
{
return baseFontSize
}
}
}

/**
 * Returns the default font to use depending on the platform
 * @param fontSize The size of the fonts to use
 * @return Default font to use, system-dependent
 */
    @JvmOverloads protected fun selectDefaultFont(fontSize:Int = DEFAULT_FONT_SIZE):Array<Font?>? {
val osName = System.getProperty("os.name", "").toLowerCase()
if (osName!!.contains("win"))
{
val windowsFonts = getDefaultWindowsFonts(fontSize)
return windowsFonts!!.toArray(arrayOfNulls<Font?>(0))
}
else if (osName!!.contains("linux"))
{
val linuxFonts = getDefaultLinuxFonts(fontSize)
return linuxFonts!!.toArray(arrayOfNulls<Font?>(0))
}
else
{
val defaultFonts = getDefaultFonts(fontSize)
return defaultFonts!!.toArray(arrayOfNulls<Font?>(0))
}
}

/**
 * This is the default font settings that will be used if you don't specify anything
 * @return An [AWTTerminal] font configuration object with default values set up
 */
     val default:AWTTerminalFontConfiguration
get() {
return newInstance(*filterMonospaced(*selectDefaultFont(DEFAULT_FONT_SIZE)!!))
}

/**
 * Returns the default font settings except for a custom font size to use.
 * @param fontSize Size of the font
 * @return An [AWTTerminal] font configuration object with default values set up
 */
     fun getDefaultOfSize(fontSize:Int):AWTTerminalFontConfiguration {
return newInstance(*filterMonospaced(*selectDefaultFont(fontSize)!!))
}

/**
 * Given an array of fonts, returns another array with only the ones that are monospaced. The fonts in the result
 * will have the same order as in which they came in. A font is considered monospaced if the width of 'i' and 'W' is
 * the same.
 * @param fonts Fonts to filter monospaced fonts from
 * @return Array with the fonts from the input parameter that were monospaced
 */
     fun filterMonospaced(vararg fonts:Font?):Array<Font?>? {
val result = ArrayList(fonts.size)
for (font in fonts)
{
if (isFontMonospaced(font!!))
{
result.add(font)
}
}
return result.toArray(arrayOfNulls<Font?>(0))
}

/**
 * Creates a new font configuration from a list of fonts in order of priority. This works by having the terminal
 * attempt to draw each character with the fonts in the order they are specified in and stop once we find a font
 * that can actually draw the character. For ASCII characters, it's very likely that the first font will always be
 * used.
 * @param fontsInOrderOfPriority Fonts to use when drawing text, in order of priority
 * @return Font configuration built from the font list
 */
    @SuppressWarnings("WeakerAccess")
 fun newInstance(vararg fontsInOrderOfPriority:Font?):AWTTerminalFontConfiguration {
return AWTTerminalFontConfiguration(true, BoldMode.EVERYTHING_BUT_SYMBOLS, *fontsInOrderOfPriority)
}

private fun isFontMonospaced(font:Font):Boolean {
if (MONOSPACE_CHECK_OVERRIDE!!.contains(font.getName()))
{
return true
}
val frc = FontRenderContext(null, 
RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, 
RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)
val iBounds = font.getStringBounds("i", frc)
val mBounds = font.getStringBounds("W", frc)
return iBounds!!.getWidth() === mBounds!!.getWidth()
}


private val SYMBOLS_CACHE = HashSet()
init{
for (field in Symbols::class.java!!.getFields())
{
if ((field!!.getType() === Char::class.javaPrimitiveType && 
(field!!.getModifiers() and Modifier.FINAL) !== 0 && 
(field!!.getModifiers() and Modifier.STATIC) !== 0))
{
try
{
SYMBOLS_CACHE.add(field!!.getChar(null))
}
catch (ignore:IllegalArgumentException) {
 //Should never happen!
                }
catch (ignore:IllegalAccessException) {}

}
}
}
}
}/**
 * Returns the default font to use depending on the platform
 * @return Default font to use, system-dependent
 */
