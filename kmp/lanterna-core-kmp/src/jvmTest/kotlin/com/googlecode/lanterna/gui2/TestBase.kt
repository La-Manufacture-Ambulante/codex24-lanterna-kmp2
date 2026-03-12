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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.screen.Screen
import java.io.IOException
import java.lang.reflect.Method

/**
 * Some common code for the GUI tests to get a text system up and running on a separate thread
 *
 * These GUI integration tests intentionally live in `jvmTest`: they depend on `TestTerminalFactory` and JVM terminal
 * behavior that is not available as deterministic `commonTest` coverage yet.
 * @author Martin
 */
abstract class TestBase {
    @Throws(IOException::class, InterruptedException::class)
    internal fun run(args: Array<String?>?) {
        val screen = TestTerminalFactory(args).createScreen()!!
        screen.startScreen()
        val textGUI = invokeCreateTextGUI(screen)
        val theme = extractTheme(args ?: emptyArray())
        if (theme != null) {
            textGUI.theme = LanternaThemes.getRegisteredTheme(theme)
        }
        textGUI.setBlockingIO(false)
        textGUI.isEOFWhenNoWindows = true

        try {
            invokeInit(textGUI)
            val guiThread = textGUI.guiThread as AsynchronousTextGUIThread
            guiThread.start()
            invokeAfterGUIThreadStarted(textGUI)
            guiThread.waitForStop()
        } finally {
            screen.stopScreen()
        }
    }

    private fun extractTheme(args: Array<String?>): String? {
        for (i in args.indices) {
            if (args[i] == "--theme" && i + 1 < args.size) {
                return args[i + 1]
            }
        }
        return null
    }

    private fun findHook(name: String): Method? {
        var cls: Class<*>? = javaClass
        while (cls != null && cls != TestBase::class.java) {
            cls.declaredMethods.firstOrNull { it.name == name && it.parameterCount == 1 }?.let {
                it.isAccessible = true
                return it
            }
            cls = cls.superclass
        }
        return null
    }

    private fun invokeCreateTextGUI(screen: Screen): MultiWindowTextGUI {
        val method = findHook("createTextGUI")
        if (method != null) {
            val result = method.invoke(this, screen)
            if (result is MultiWindowTextGUI) {
                return result
            }
        }
        return MultiWindowTextGUI(SeparateTextGUIThread.Factory(), screen)
    }

    private fun invokeInit(textGUI: WindowBasedTextGUI) {
        findHook("init")?.invoke(this, textGUI)
    }

    private fun invokeAfterGUIThreadStarted(textGUI: WindowBasedTextGUI) {
        findHook("afterGUIThreadStarted")?.invoke(this, textGUI)
    }
}
