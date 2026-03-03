package com.googlecode.lanterna.gui2

/**
 * A Composite is a Container that contains only one (or zero) component. Normally it is a kind of decorator, like a
 * border, that wraps a single component for visualization purposes.
 *
 * @author Martin
 */
interface Composite {
    /**
     * Returns the component that this Composite is wrapping
     *
     * @return Component the composite is wrapping
     */
    fun getComponent(): Component?

    /**
     * Sets the component which is inside this Composite. If you call this method with null, it removes the component
     * wrapped by this Composite.
     *
     * @param component Component to wrap
     */
    fun setComponent(component: Component?)
}
