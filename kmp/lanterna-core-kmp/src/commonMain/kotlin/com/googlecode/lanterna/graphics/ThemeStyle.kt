package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import java.util.EnumSet

/**
 * ThemeStyle is the lowest entry in the theme hierarchy, containing the actual colors and SGRs to use. When drawing a
 * component, you would pick out a [ThemeDefinition] that applies to the whole component and then choose to
 * activate individual [ThemeStyle]s when drawing the different parts of the component.
 *
 * @author Martin
 */
interface ThemeStyle {
    /**
     * Returns the foreground color associated with this style
     * @return foreground color associated with this style
     */
    fun getForeground(): TextColor?

    /**
     * Returns the background color associated with this style
     * @return background color associated with this style
     */
    fun getBackground(): TextColor?

    /**
     * Returns the set of SGR flags associated with this style. This `EnumSet` is either unmodifiable or a copy so
     * altering it will not change the theme in any way.
     * @return SGR flags associated with this style
     */
    fun getSGRs(): EnumSet<SGR>?
}
