package com.googlecode.lanterna.bundle

import org.junit.Assert
import org.junit.Test

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Field
import java.util.Scanner

/**
 * To ensure our bundled default theme matches the theme definition file in resources
 */
 class DefaultThemeTest {

private val embeddedDefinition:String?
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
get() {
val definitionField = DefaultTheme::class.java!!.getDeclaredField("definition")
definitionField!!.setAccessible(true)
return definitionField!!.get(null) as String
}

private// https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
 // Normalize line endings to LF
 val resourceDefinition:String?
@Throws(IOException::class)
get() {
val classLoader = DefaultThemeTest::class.java!!.getClassLoader()
var resourceAsStream:InputStream? = null
try
{
resourceAsStream = classLoader!!.getResourceAsStream("default-theme.properties")
if (resourceAsStream == null)
{
resourceAsStream = FileInputStream("src/main/resources/default-theme.properties")
}
val s = Scanner(resourceAsStream).useDelimiter("\\A")
var definition:String? = if (s!!.hasNext()) s!!.next() else ""
definition = definition!!.replace("\r\n", "\n")

return definition
}

finally
{
if (resourceAsStream != null)
{
resourceAsStream!!.close()
}
}
}
@Test
@Throws(NoSuchFieldException::class, IllegalAccessException::class, IOException::class)
@JvmStatic  fun ensureResourceFileDefaultTestIsTheSameAsTheEmbeddedTest() {
val embeddedDefinition = embeddedDefinition
val resourceDefinition = resourceDefinition
Assert.assertEquals(resourceDefinition, embeddedDefinition)
}
}
