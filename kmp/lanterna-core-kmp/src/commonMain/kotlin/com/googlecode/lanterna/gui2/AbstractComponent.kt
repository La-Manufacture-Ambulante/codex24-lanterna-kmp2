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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * AbstractComponent provides some good default behaviour for a `Component`, all components in Lanterna extends
 * from this class in some way. If you want to write your own component that isn't interactable or theme:able, you
 * probably want to extend from this class.
 * 
 * 
 * The way you want to declare your new `Component` is to pass in itself as the generic parameter, like this:
 * <pre>
 * `public class MyComponent extends AbstractComponent<MyComponent> {
 * ...
 * }
` * 
</pre> * 
 * This was, the component renderer will be correctly setup type-wise and you will need to do fewer typecastings when
 * you implement the drawing method your new component.
 * 
 * @author Martin
 * @param <T> Should always be itself, this value will be used for the `ComponentRenderer` declaration
</T> */
abstract class AbstractComponent<T : Component?>:Component {
/**
 * Manually set renderer
 */
    private var overrideRenderer:ComponentRenderer<T?>? = null
/**
 * If overrideRenderer is not set, this is used instead if not null, set by the theme
 */
    private var themeRenderer:ComponentRenderer<T?>? = null

/**
 * To keep track of the theme that created the themeRenderer, so we can reset it if the theme changes
 */
    private var themeRenderersTheme:Theme? = null

/**
 * If the theme had nothing for this component and no override is set, this is the third fallback
 */
    private var defaultRenderer:ComponentRenderer<T?>? = null

@get:Override
 var parent:Container? = null
private set
private var size:TerminalSize? = null
private var explicitPreferredSize:TerminalSize? = null   //This is keeping the value set by the user (if setPreferredSize() is used)
private var position:TerminalPosition? = null
private var themeOverride:Theme? = null
private var layoutData:LayoutData? = null
private var visible:Boolean = false
@get:Override
 var isInvalid:Boolean = false
private set

 // First try the override
 // Then try to create and return a renderer from the theme
 // Check if the theme has changed
 // Finally, fallback to the default renderer
 val renderer:ComponentRenderer<T?>?
@Override
@Synchronized get() {
if (overrideRenderer != null)
{
return overrideRenderer
}
val currentTheme = theme
if (((themeRenderer == null && basePane != null) || themeRenderer != null && currentTheme !== themeRenderersTheme))
{

themeRenderer = currentTheme!!.getDefinition(getClass()).getRenderer(selfClass())
if (themeRenderer != null)
{
themeRenderersTheme = currentTheme
}
}
if (themeRenderer != null)
{
return themeRenderer
}
if (defaultRenderer == null)
{
defaultRenderer = createDefaultRenderer()
if (defaultRenderer == null)
{
throw IllegalStateException(getClass() + " returned a null default renderer")
}
}
return defaultRenderer
}

 val preferredSize:TerminalSize?
@Override
get() {
if (explicitPreferredSize != null)
{
return explicitPreferredSize
}
else
{
return calculatePreferredSize()
}
}

 val globalPosition:TerminalPosition?
@Override
get() {
return toGlobal(TerminalPosition.TOP_LEFT_CORNER)
}

 val textGUI:TextGUI?
@Override
get() {
if (parent == null)
{
return null
}
return parent!!.getTextGUI()
}

 val theme:Theme?
@Override
@Synchronized get() {
if (themeOverride != null)
{
return themeOverride
}
else if (parent != null)
{
return parent!!.getTheme()
}
else if (basePane != null)
{
return basePane!!.getTheme()
}
else
{
return LanternaThemes.getDefaultTheme()
}
}

 val themeDefinition:ThemeDefinition?
@Override
get() {
return theme!!.getDefinition(getClass())
}

 val basePane:BasePane?
@Override
get() {
if (parent == null)
{
return null
}
return parent!!.getBasePane()
}
/**
 * Default constructor
 */
    init{
size = TerminalSize.ZERO
position = TerminalPosition.TOP_LEFT_CORNER
explicitPreferredSize = null
layoutData = null
visible = true
isInvalid = true
parent = null
overrideRenderer = null
themeRenderer = null
themeRenderersTheme = null
defaultRenderer = null
}

/**
 * When you create a custom component, you need to implement this method and return a Renderer which is responsible
 * for taking care of sizing the component, rendering it and choosing where to place the cursor (if Interactable).
 * This value is intended to be overridden by custom themes.
 * @return Renderer to use when sizing and drawing this component
 */
    protected abstract fun createDefaultRenderer():ComponentRenderer<T?>? 

/**
 * Takes a `Runnable` and immediately executes it if this is called on the designated GUI thread, otherwise
 * schedules it for later invocation.
 * @param runnable `Runnable` to execute on the GUI thread
 */
    protected fun runOnGUIThreadIfExistsOtherwiseRunDirect(runnable:Runnable?) {
if (textGUI != null && textGUI!!.getGUIThread() != null)
{
textGUI!!.getGUIThread().invokeLater(runnable)
}
else
{
runnable!!.run()
}
}

/**
 * Explicitly sets the `ComponentRenderer` to be used when drawing this component. This will override whatever
 * the current theme is suggesting or what the default renderer is. If you call this with `null`, the override
 * is cleared.
 * @param renderer `ComponentRenderer` to be used when drawing this component
 * @return Itself
 */
     fun setRenderer(renderer:ComponentRenderer<T?>?):T? {
this.overrideRenderer = renderer
return self()
}

@Override
@JvmStatic  fun invalidate() {
isInvalid = true
}

@Override
@Synchronized  fun setSize(size:TerminalSize?):T? {
this.size = size
return self()
}

@Override
 fun getSize():TerminalSize? {
return size
}

@Override
@Synchronized  fun setPreferredSize(explicitPreferredSize:TerminalSize?):T? {
this.explicitPreferredSize = explicitPreferredSize
return self()
}

@Override
 fun isVisible():Boolean {
return visible
}

@Override
 fun setVisible(visible:Boolean):T? {
if (this.visible != visible)
{
this.visible = visible
if (visible)
{
 // This component is now visible, so mark it as invalid so it will be redrawn
                invalidate()
}
else
{
val parent = parent
if (parent != null)
{
 // This component is now invisible, so mark the parent container as needing to be redrawn
                    parent!!.invalidate()
}
}
}
return self()
}

/**
 * Invokes the component renderer's size calculation logic and returns the result. This value represents the
 * preferred size and isn't necessarily what it will eventually be assigned later on.
 * @return Size that the component renderer believes the component should be
 */
    @Synchronized protected fun calculatePreferredSize():TerminalSize? {
return renderer!!.getPreferredSize(self())
}

@Override
@Synchronized  fun setPosition(position:TerminalPosition?):T? {
this.position = position
return self()
}

@Override
 fun getPosition():TerminalPosition? {
return position
}

@Override
@Synchronized  fun draw(graphics:TextGUIGraphics) {
 //Delegate drawing the component to the renderer
        setSize(graphics.getSize())
onBeforeDrawing()
renderer!!.drawComponent(graphics, self())
onAfterDrawing(graphics)
isInvalid = false
}

/**
 * This method is called just before the component's renderer is invoked for the drawing operation. You can use this
 * hook to do some last-minute adjustments to the component, as an alternative to coding it into the renderer
 * itself. The component should have the correct size and position at this point, if you call `getSize()` and
 * `getPosition()`.
 */
    @JvmStatic protected fun onBeforeDrawing() {
 //No operation by default
    }

/**
 * This method is called immediately after the component's renderer has finished the drawing operation. You can use
 * this hook to do some post-processing if you need, as an alternative to coding it into the renderer. The
 * `TextGUIGraphics` supplied is the same that was fed into the renderer.
 * @param graphics Graphics object you can use to manipulate the appearance of the component
 */
    @SuppressWarnings("EmptyMethod")
protected fun onAfterDrawing(graphics:TextGUIGraphics?) {
 //No operation by default
    }

@Override
@Synchronized  fun setLayoutData(data:LayoutData?):T? {
if (layoutData !== data)
{
layoutData = data
invalidate()
}
return self()
}

@Override
 fun getLayoutData():LayoutData? {
return layoutData
}

@Override
 fun hasParent(parent:Container?):Boolean {
if (this.parent == null)
{
return false
}
var recursiveParent = this.parent
while (recursiveParent != null)
{
if (recursiveParent === parent)
{
return true
}
recursiveParent = recursiveParent!!.getParent()
}
return false
}

@Override
@Synchronized  fun setTheme(theme:Theme?):Component? {
themeOverride = theme
invalidate()
return this
}

@Override
 fun isInside(container:Container?):Boolean {
var test:Component? = this
while (test!!.getParent() != null)
{
if (test!!.getParent() === container)
{
return true
}
test = test!!.getParent()
}
return false
}

@Override
 fun toBasePane(position:TerminalPosition?):TerminalPosition? {
val parent = parent
if (parent == null)
{
return null
}
return parent!!.toBasePane(getPosition()!!.withRelative(position!!))
}

@Override
 fun toGlobal(position:TerminalPosition?):TerminalPosition? {
val parent = parent
if (parent == null)
{
return null
}
return parent!!.toGlobal(getPosition()!!.withRelative(position!!))
}

@Override
@Synchronized  fun withBorder(border:Border):Border? {
border.setComponent(this)
return border
}

@Override
@Synchronized  fun addTo(panel:Panel):T? {
panel.addComponent(this)
return self()
}

@Override
@Synchronized  fun onAdded(container:Container?) {
if (parent !== container && parent != null)
{
 // first inform current parent:
            parent!!.removeComponent(this)
}
parent = container
}

@Override
@Synchronized  fun onRemoved(container:Container?) {
if (parent === container)
{
parent = null
themeRenderer = null
}
else
{
throw IllegalStateException(this + " is not " + container + "'s child.")
}
}

/**
 * This is a little hack to avoid doing typecasts all over the place when having to return `T`. Credit to
 * avl42 for this one!
 * @return Itself, but as type T
 */
    @SuppressWarnings("unchecked")
protected fun self():T {
return this as T
}

@SuppressWarnings("unchecked")
private fun selfClass():Class<T?>? {
return getClass() as Class<T?>
}
}
