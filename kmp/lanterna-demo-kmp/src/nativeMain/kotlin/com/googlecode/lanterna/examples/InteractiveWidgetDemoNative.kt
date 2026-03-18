package com.googlecode.lanterna.examples

import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.MouseCaptureMode

fun main() {
    interactiveWidgetDemoMain()
}

fun interactiveWidgetDemoMain() {
    val screen =
        DefaultTerminalFactory()
            .setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE)
            .createScreen()

    try {
        screen.startScreen()
        val textGUI = createInteractiveWidgetDemoTextGUI(screen)
        textGUI.addWindowAndWait(createInteractiveWidgetDemoWindow(textGUI))
    } finally {
        screen.stopScreen()
    }
}
