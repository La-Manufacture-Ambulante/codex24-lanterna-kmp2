package com.googlecode.lanterna.terminal.swing

/**
 * This enum stored various ways the AWTTerminalFrame and SwingTerminalFrame can automatically close (hide and dispose)
 * themselves when a certain condition happens.
 */
enum class TerminalEmulatorAutoCloseTrigger {
    /**
     * Close the frame when exiting from private mode
     */
    CLOSE_ON_EXIT_PRIVATE_MODE,

    /**
     * Close if the user presses ESC key on the keyboard
     */
    CLOSE_ON_ESCAPE
}
