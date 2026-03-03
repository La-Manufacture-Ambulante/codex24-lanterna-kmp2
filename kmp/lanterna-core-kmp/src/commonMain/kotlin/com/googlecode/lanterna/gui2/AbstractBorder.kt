package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize

abstract class AbstractBorder : AbstractComposite<Border>(), Border {
    override fun setComponent(component: Component?) {
        super.setComponent(component)
        if (component != null) {
            component.setPosition(TerminalPosition.TOP_LEFT_CORNER)
        }
    }

    override fun getRenderer(): BorderRenderer {
        return super.getRenderer() as BorderRenderer
    }

    override fun setSize(size: TerminalSize?): Border {
        super.setSize(size)
        getComponent().setSize(getWrappedComponentSize(size))
        return self()
    }

    override fun getLayoutData(): LayoutData? {
        if (getComponent() == null) {
            return super.getLayoutData()
        }
        return getComponent().getLayoutData()
    }

    override fun setLayoutData(ld: LayoutData?): Border {
        if (getComponent() == null) {
            super.setLayoutData(ld)
        } else {
            getComponent().setLayoutData(ld)
        }
        return this
    }

    override fun toBasePane(position: TerminalPosition?): TerminalPosition? {
        val terminalPosition = super.toBasePane(position)
        if (terminalPosition == null) {
            return null
        }
        return terminalPosition.withRelative(getWrappedComponentTopLeftOffset())
    }

    override fun toGlobal(position: TerminalPosition?): TerminalPosition? {
        val terminalPosition = super.toGlobal(position)
        if (terminalPosition == null) {
            return null
        }
        return terminalPosition.withRelative(getWrappedComponentTopLeftOffset())
    }

    private fun getWrappedComponentTopLeftOffset(): TerminalPosition {
        return getRenderer().getWrappedComponentTopLeftOffset()
    }

    private fun getWrappedComponentSize(borderSize: TerminalSize?): TerminalSize {
        return getRenderer().getWrappedComponentSize(borderSize)
    }
}
