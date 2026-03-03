package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import java.util.EnumSet

interface StyleSet<T : StyleSet<T>> {
    fun getBackgroundColor(): TextColor?

    fun setBackgroundColor(backgroundColor: TextColor?): T

    fun getForegroundColor(): TextColor?

    fun setForegroundColor(foregroundColor: TextColor?): T

    fun enableModifiers(modifiers: Array<out SGR?>?): T

    fun disableModifiers(modifiers: Array<out SGR?>?): T

    fun setModifiers(modifiers: EnumSet<SGR>?): T

    fun clearModifiers(): T

    fun getActiveModifiers(): EnumSet<SGR>?

    fun setStyleFrom(source: StyleSet<*>): T

    open class Set() : StyleSet<Set> {
        private var foregroundColor: TextColor? = null
        private var backgroundColor: TextColor? = null
        private val style: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)

        constructor(source: StyleSet<*>) : this() {
            setStyleFrom(source)
        }

        override fun getBackgroundColor(): TextColor? {
            return backgroundColor
        }

        override fun setBackgroundColor(backgroundColor: TextColor?): Set {
            this.backgroundColor = backgroundColor
            return this
        }

        override fun getForegroundColor(): TextColor? {
            return foregroundColor
        }

        override fun setForegroundColor(foregroundColor: TextColor?): Set {
            this.foregroundColor = foregroundColor
            return this
        }

        @Suppress("UNCHECKED_CAST")
        override fun enableModifiers(modifiers: Array<out SGR?>?): Set {
            val m = modifiers ?: throw NullPointerException()
            style.addAll(java.util.Arrays.asList(*m) as Collection<SGR>)
            return this
        }

        override fun disableModifiers(modifiers: Array<out SGR?>?): Set {
            val m = modifiers ?: throw NullPointerException()
            style.removeAll(java.util.Arrays.asList(*m))
            return this
        }

        override fun setModifiers(modifiers: EnumSet<SGR>?): Set {
            style.clear()
            style.addAll(modifiers ?: throw NullPointerException())
            return this
        }

        override fun clearModifiers(): Set {
            style.clear()
            return this
        }

        override fun getActiveModifiers(): EnumSet<SGR> {
            return EnumSet.copyOf(style)
        }

        override fun setStyleFrom(source: StyleSet<*>): Set {
            setBackgroundColor(source.getBackgroundColor())
            setForegroundColor(source.getForegroundColor())
            setModifiers(source.getActiveModifiers())
            return this
        }
    }
}
