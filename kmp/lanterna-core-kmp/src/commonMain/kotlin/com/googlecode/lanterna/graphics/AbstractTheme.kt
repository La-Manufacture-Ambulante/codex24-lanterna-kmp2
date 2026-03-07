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
package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import com.googlecode.lanterna.gui2.WindowShadowRenderer

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Abstract [Theme] implementation that manages a hierarchical tree of theme nodes ties to Class objects.
 * Sub-classes will inherit their theme properties from super-class definitions, the java.lang.Object class is
 * considered the root of the tree and as such is the fallback for all other classes.
 * 
 * 
 * You normally use this class through [PropertyTheme], which is the default implementation bundled with Lanterna.
 * @author Martin
 */
abstract class AbstractTheme protected constructor(@get:Override
 val windowPostRenderer:WindowPostRenderer?, 
@get:Override
 val windowDecorationRenderer:WindowDecorationRenderer?):Theme {

private val rootNode:ThemeTreeNode?

 val defaultDefinition:ThemeDefinition?
@Override
get() {
return DefinitionImpl(rootNode)
}

init{

this.rootNode = ThemeTreeNode(Object::class.java, null)

rootNode!!.foregroundMap!!.put(STYLE_NORMAL, TextColor.ANSI.WHITE)
rootNode!!.backgroundMap!!.put(STYLE_NORMAL, TextColor.ANSI.BLACK)
classloadStandardRenderersForGraal()
}

private fun classloadStandardRenderersForGraal() {
 // This will make graal know about these classes which would otherwise only
        // be loaded through reflection
        WindowShadowRenderer::class.java!!.toString()
Button.DefaultButtonRenderer::class.java!!.toString()
Button.FlatButtonRenderer::class.java!!.toString()
Button.BorderedButtonRenderer::class.java!!.toString()
}

protected fun addStyle(definition:String?, style:String?, value:String?):Boolean {
val node = getNode(definition)
if (node == null)
{
return false
}
node!!.apply(style, value)
return true
}

private fun getNode(definition:String?):ThemeTreeNode? {
try
{
if (definition == null || definition!!.trim().isEmpty())
{
return getNode(Object::class.java)
}
else
{
return getNode(Class.forName(definition))
}
}
catch (e:ClassNotFoundException) {
return null
}

}

private fun getNode(definition:Class<*>?):ThemeTreeNode? {
if (definition === Object::class.java)
{
return rootNode
}
val parent = getNode(definition!!.getSuperclass())
if (parent!!.childMap!!.containsKey(definition))
{
return parent!!.childMap!!.get(definition)
}

val node = ThemeTreeNode(definition, parent)
parent!!.childMap!!.put(definition, node)
return node
}

@Override
 fun getDefinition(clazz:Class<*>?):ThemeDefinition? {
var clazz = clazz
val hierarchy = LinkedList()
while (clazz != null && clazz !== Object::class.java)
{
hierarchy.addFirst(clazz)
clazz = clazz!!.getSuperclass()
}

var node = rootNode
for (aClass in hierarchy)
{
if (node!!.childMap!!.containsKey(aClass))
{
node = node!!.childMap!!.get(aClass)
}
else
{
break
}
}
return DefinitionImpl(node)
}

/**
 * Returns a list of redundant theme entries in this theme. A redundant entry means that it doesn't need to be
 * specified because there is a parent node in the hierarchy which has the same property so if the redundant entry
 * wasn't there, the parent node would be picked up and the end result would be the same.
 * @return List of redundant theme entries
 */
     fun findRedundantDeclarations():List<String?>? {
val result = ArrayList()
for (node in rootNode!!.childMap!!.values())
{
findRedundantDeclarations(result, node!!)
}
Collections.sort(result)
return result
}

private fun findRedundantDeclarations(result:List<String?>?, node:ThemeTreeNode) {
for (style in node.foregroundMap!!.keySet())
{
var formattedStyle:String? = "[" + style + "]"
if (formattedStyle!!.length() === 2)
{
formattedStyle = ""
}
val color = node.foregroundMap!!.get(style)
val colorFromParent = StyleImpl(node.parent, style).foreground
if (color!!.equals(colorFromParent))
{
result!!.add(node.clazz!!.getName() + ".foreground" + formattedStyle)
}
}
for (style in node.backgroundMap!!.keySet())
{
var formattedStyle:String? = "[" + style + "]"
if (formattedStyle!!.length() === 2)
{
formattedStyle = ""
}
val color = node.backgroundMap!!.get(style)
val colorFromParent = StyleImpl(node.parent, style).background
if (color!!.equals(colorFromParent))
{
result!!.add(node.clazz!!.getName() + ".background" + formattedStyle)
}
}
for (style in node.sgrMap!!.keySet())
{
var formattedStyle:String? = "[" + style + "]"
if (formattedStyle!!.length() === 2)
{
formattedStyle = ""
}
val sgrs = node.sgrMap!!.get(style)
val sgrsFromParent = StyleImpl(node.parent, style).sgRs
if (sgrs!!.equals(sgrsFromParent))
{
result!!.add(node.clazz!!.getName() + ".sgr" + formattedStyle)
}
}

for (childNode in node.childMap!!.values())
{
findRedundantDeclarations(result, childNode!!)
}
}

private inner class DefinitionImpl(internal val node:ThemeTreeNode?):ThemeDefinition {

 val normal:ThemeStyle
@Override
get() {
return StyleImpl(node, STYLE_NORMAL)
}

 val preLight:ThemeStyle
@Override
get() {
return StyleImpl(node, STYLE_PRELIGHT)
}

 val selected:ThemeStyle
@Override
get() {
return StyleImpl(node, STYLE_SELECTED)
}

 val active:ThemeStyle
@Override
get() {
return StyleImpl(node, STYLE_ACTIVE)
}

 val insensitive:ThemeStyle
@Override
get() {
return StyleImpl(node, STYLE_INSENSITIVE)
}

 val isCursorVisible:Boolean
@Override
get() {
val cursorVisible = node!!.cursorVisible
if (cursorVisible == null)
{
if (node === rootNode)
{
return true
}
else
{
return DefinitionImpl(node!!.parent).isCursorVisible
}
}
return cursorVisible!!
}

@Override
 fun getCustom(name:String?):ThemeStyle {
return StyleImpl(node, name)
}

@Override
 fun getCustom(name:String?, defaultValue:ThemeStyle?):ThemeStyle? {
var customStyle:ThemeStyle? = getCustom(name)
if (customStyle == null)
{
customStyle = defaultValue
}
return customStyle
}

@Override
 fun getCharacter(name:String?, fallback:Char):Char {
val character = node!!.characterMap!!.get(name)
if (character == null)
{
if (node === rootNode)
{
return fallback
}
else
{
return DefinitionImpl(node!!.parent).getCharacter(name, fallback)
}
}
return character!!.toChar()
}

@Override
 fun getIntegerProperty(name:String?, defaultValue:Int):Int {
val propertyValue = node!!.propertyMap!!.get(name)
if (propertyValue == null)
{
if (node === rootNode)
{
return defaultValue
}
else
{
return DefinitionImpl(node!!.parent).getIntegerProperty(name, defaultValue)
}
}
return Integer.parseInt(propertyValue)
}

@Override
 fun getBooleanProperty(name:String?, defaultValue:Boolean):Boolean {
val propertyValue = node!!.propertyMap!!.get(name)
if (propertyValue == null)
{
if (node === rootNode)
{
return defaultValue
}
else
{
return DefinitionImpl(node!!.parent).getBooleanProperty(name, defaultValue)
}
}
return Boolean.parseBoolean(propertyValue)
}

@SuppressWarnings("unchecked")
@Override
 fun <T : Component?> getRenderer(type:Class<T?>?):ComponentRenderer<T?>? {
val rendererClass = node!!.renderer
if (rendererClass == null)
{
if (node === rootNode)
{
return null
}
else
{
return DefinitionImpl(node!!.parent).getRenderer<Component?>(type)
}
}
return instanceByClassName(rendererClass) as ComponentRenderer<T?>?
}
}

private inner class StyleImpl private constructor(private val styleNode:ThemeTreeNode?, private val name:String?):ThemeStyle {

 val foreground:TextColor?
@Override
get() {
var node = styleNode
while (node != null)
{
if (node!!.foregroundMap!!.containsKey(name))
{
return node!!.foregroundMap!!.get(name)
}
node = node!!.parent
}
var fallback = rootNode!!.foregroundMap!!.get(STYLE_NORMAL)
if (fallback == null)
{
fallback = TextColor.ANSI.WHITE
}
return fallback
}

 val background:TextColor?
@Override
get() {
var node = styleNode
while (node != null)
{
if (node!!.backgroundMap!!.containsKey(name))
{
return node!!.backgroundMap!!.get(name)
}
node = node!!.parent
}
var fallback = rootNode!!.backgroundMap!!.get(STYLE_NORMAL)
if (fallback == null)
{
fallback = TextColor.ANSI.BLACK
}
return fallback
}

 val sgRs:EnumSet<SGR?>?
@Override
get() {
var node = styleNode
while (node != null)
{
if (node!!.sgrMap!!.containsKey(name))
{
return EnumSet.copyOf(node!!.sgrMap!!.get(name))
}
node = node!!.parent
}
var fallback = rootNode!!.sgrMap!!.get(STYLE_NORMAL)
if (fallback == null)
{
fallback = EnumSet.noneOf(SGR::class.java)
}
return EnumSet.copyOf(fallback)
}
}

private class ThemeTreeNode private constructor(private val clazz:Class<*>?, private val parent:ThemeTreeNode?) {
private val childMap:Map<Class<*>?, ThemeTreeNode?>?
private val foregroundMap:Map<String?, TextColor?>?
private val backgroundMap:Map<String?, TextColor?>?
private val sgrMap:Map<String?, EnumSet<SGR?>?>?
private val characterMap:Map<String?, Character?>?
private val propertyMap:Map<String?, String?>?
private var cursorVisible:Boolean? = null
private var renderer:String? = null

init{
this.childMap = HashMap()
this.foregroundMap = HashMap()
this.backgroundMap = HashMap()
this.sgrMap = HashMap()
this.characterMap = HashMap()
this.propertyMap = HashMap()
this.cursorVisible = true
this.renderer = null
}

private fun apply(style:String?, value:String?) {
var value = value
value = value!!.trim()
val matcher = STYLE_FORMAT!!.matcher(style)
if (!matcher!!.matches())
{
throw IllegalArgumentException("Unknown style declaration: " + style!!)
}
val styleComponent = matcher!!.group(1)
val group = if (matcher!!.groupCount() > 2) matcher!!.group(3) else null
when (styleComponent!!.toLowerCase().trim()) {
"foreground" -> foregroundMap!!.put(getCategory(group), parseValue(value))
"background" -> backgroundMap!!.put(getCategory(group), parseValue(value))
"sgr" -> sgrMap!!.put(getCategory(group), parseSGR(value))
"char" -> characterMap!!.put(getCategory(group), if (value!!.isEmpty()) ' ' else value!!.charAt(0))
"cursor" -> cursorVisible = Boolean.parseBoolean(value)
"property" -> propertyMap!!.put(getCategory(group), if (value!!.isEmpty()) null else value!!.trim())
"renderer" -> renderer = if (value!!.trim().isEmpty()) null else value!!.trim()
"postrenderer", "windowdecoration" -> {}
else -> throw IllegalArgumentException("Unknown style component \"" + styleComponent + "\" in style \"" + style + "\"")
}// Don't do anything with this now, we might use it later
}

private fun parseValue(value:String?):TextColor? {
return TextColor.Factory.fromString(value)
}

private fun parseSGR(value:String?):EnumSet<SGR?>? {
var value = value
value = value!!.trim()
val sgrEntries = value!!.split(",")
val sgrSet = EnumSet.noneOf(SGR::class.java)
for (entry in sgrEntries!!)
{
entry = entry!!.trim().toUpperCase()
if (!entry!!.isEmpty())
{
try
{
sgrSet!!.add(SGR.valueOf(entry!!))
}
catch (e:IllegalArgumentException) {
throw IllegalArgumentException("Unknown SGR code \"" + entry + "\"", e)
}

}
}
return sgrSet
}

private fun getCategory(group:String?):String? {
if (group == null)
{
return STYLE_NORMAL
}
for (style in Arrays.asList(STYLE_ACTIVE, STYLE_INSENSITIVE, STYLE_PRELIGHT, STYLE_NORMAL, STYLE_SELECTED))
{
if (group!!.toUpperCase().equals(style))
{
return style
}
}
return group
}
}

companion object {
private val STYLE_NORMAL = ""
private val STYLE_PRELIGHT = "PRELIGHT"
private val STYLE_SELECTED = "SELECTED"
private val STYLE_ACTIVE = "ACTIVE"
private val STYLE_INSENSITIVE = "INSENSITIVE"
private val STYLE_FORMAT = Pattern.compile("([a-zA-Z]+)(\\[([a-zA-Z0-9-_]+)])?")

protected fun instanceByClassName(className:String?):Object? {
if (className == null || className!!.trim().isEmpty())
{
return null
}
try
{
return Class.forName(className).newInstance()
}
catch (e:InstantiationException) {
throw RuntimeException(e)
}
catch (e:IllegalAccessException) {
throw RuntimeException(e)
}
catch (e:ClassNotFoundException) {
throw RuntimeException(e)
}

}
}
}
