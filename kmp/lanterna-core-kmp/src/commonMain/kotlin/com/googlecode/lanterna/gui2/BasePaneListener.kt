package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.input.KeyStroke
import java.util.concurrent.atomic.AtomicBoolean

interface BasePaneListener<T : BasePane> {
    fun onInput(basePane: T?, keyStroke: KeyStroke?, deliverEvent: AtomicBoolean?)

    fun onUnhandledInput(basePane: T?, keyStroke: KeyStroke?, hasBeenHandled: AtomicBoolean?)
}
