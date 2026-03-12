package com.googlecode.lanterna.bundle

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.ThemeStyle
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.util.Scanner

/**
 * To ensure our bundled default theme matches the theme definition file in resources
 */
class DefaultThemeTest {
    private val resourceDefinition: String?
        @Throws(IOException::class)
        get() {
            val classLoader = DefaultThemeTest::class.java.classLoader
            val resourceAsStream: InputStream =
                classLoader.getResourceAsStream("default-theme.properties")
                    ?: return null
            resourceAsStream.use { stream ->
                val scanner = Scanner(stream).useDelimiter("\\A")
                var definition = if (scanner.hasNext()) scanner.next() else ""
                // Normalize line endings to LF for deterministic assertions.
                definition = definition.replace("\r\n", "\n")
                return definition
            }
        }

    @Test
    @Throws(IOException::class)
    fun ensureDefaultThemeResourceExistsAndDefaultThemeIsUsable() {
        val resourceDefinition = resourceDefinition
        if (resourceDefinition != null) {
            Assert.assertTrue(resourceDefinition.contains("foreground = black"))
            Assert.assertTrue(resourceDefinition.contains("background = white"))
        }

        val defaultTheme = LanternaThemes.defaultTheme
        Assert.assertNotNull(defaultTheme)
        val defaultDefinition = defaultTheme!!.defaultDefinition
        Assert.assertNotNull(defaultDefinition)
        val normal = defaultDefinition!!.normal as ThemeStyle?
        Assert.assertNotNull(normal)
        Assert.assertEquals(TextColor.ANSI.BLACK, normal!!.foreground)
        Assert.assertEquals(TextColor.ANSI.WHITE, normal.background)
    }
}
