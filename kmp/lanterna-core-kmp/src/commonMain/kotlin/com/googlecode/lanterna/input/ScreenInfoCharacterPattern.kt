package com.googlecode.lanterna.input

import com.googlecode.lanterna.TerminalPosition

open class ScreenInfoCharacterPattern : EscapeSequenceCharacterPattern() {
    init {
        useEscEsc = false // stdMap and finMap don't matter here.
    }

    protected override fun getKeyStrokeRaw(
        first: Char,
        num1: Int,
        num2: Int,
        last: Char,
        bEsc: Boolean
    ): KeyStroke? {
        if (first != '[' || last != 'R' || num1 == 0 || num2 == 0 || bEsc) {
            return null // nope
        }
        if (num1 == 1 && num2 <= 8) {
            return null // nope: much more likely it's an F3 with modifiers
        }
        val pos = TerminalPosition(num2, num1)
        return ScreenInfoAction(pos) // yep
    }

    companion object {
        @JvmStatic
        fun tryToAdopt(ks: KeyStroke?): ScreenInfoAction? {
            if (ks == null) {
                return null
            }
            return when (ks.keyType) {
                KeyType.CURSOR_LOCATION -> ks as ScreenInfoAction
                KeyType.F3 -> {
                    // reconstruct position from F3's modifiers.
                    if (ks is KeyStroke.RealF3) {
                        return null
                    }
                    val col = 1 +
                        (if (ks.isAltDown) EscapeSequenceCharacterPattern.ALT else 0) +
                        (if (ks.isCtrlDown) EscapeSequenceCharacterPattern.CTRL else 0) +
                        (if (ks.isShiftDown) EscapeSequenceCharacterPattern.SHIFT else 0)
                    val pos = TerminalPosition(col, 1)
                    ScreenInfoAction(pos)
                }
                else -> null
            }
        }
    }
}
