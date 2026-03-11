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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.TextColor
import java.awt.Color
import java.util.Objects

@Suppress("WeakerAccess")
class TerminalEmulatorPalette(
    private val defaultColor: Color,
    private val defaultBrightColor: Color,
    private val defaultBackgroundColor: Color,
    private val normalBlack: Color,
    private val brightBlack: Color,
    private val normalRed: Color,
    private val brightRed: Color,
    private val normalGreen: Color,
    private val brightGreen: Color,
    private val normalYellow: Color,
    private val brightYellow: Color,
    private val normalBlue: Color,
    private val brightBlue: Color,
    private val normalMagenta: Color,
    private val brightMagenta: Color,
    private val normalCyan: Color,
    private val brightCyan: Color,
    private val normalWhite: Color,
    private val brightWhite: Color,
) {
    fun get(
        color: TextColor.ANSI,
        isForeground: Boolean,
        useBrightTones: Boolean,
    ): Color {
        return if (useBrightTones) {
            when (color) {
                TextColor.ANSI.BLACK, TextColor.ANSI.BLACK_BRIGHT -> brightBlack
                TextColor.ANSI.BLUE, TextColor.ANSI.BLUE_BRIGHT -> brightBlue
                TextColor.ANSI.CYAN, TextColor.ANSI.CYAN_BRIGHT -> brightCyan
                TextColor.ANSI.DEFAULT -> if (isForeground) defaultBrightColor else defaultBackgroundColor
                TextColor.ANSI.GREEN, TextColor.ANSI.GREEN_BRIGHT -> brightGreen
                TextColor.ANSI.MAGENTA, TextColor.ANSI.MAGENTA_BRIGHT -> brightMagenta
                TextColor.ANSI.RED, TextColor.ANSI.RED_BRIGHT -> brightRed
                TextColor.ANSI.WHITE, TextColor.ANSI.WHITE_BRIGHT -> brightWhite
                TextColor.ANSI.YELLOW, TextColor.ANSI.YELLOW_BRIGHT -> brightYellow
            }
        } else {
            when (color) {
                TextColor.ANSI.BLACK -> normalBlack
                TextColor.ANSI.BLACK_BRIGHT -> brightBlack
                TextColor.ANSI.BLUE -> normalBlue
                TextColor.ANSI.BLUE_BRIGHT -> brightBlue
                TextColor.ANSI.CYAN -> normalCyan
                TextColor.ANSI.CYAN_BRIGHT -> brightCyan
                TextColor.ANSI.DEFAULT -> if (isForeground) defaultColor else defaultBackgroundColor
                TextColor.ANSI.GREEN -> normalGreen
                TextColor.ANSI.GREEN_BRIGHT -> brightGreen
                TextColor.ANSI.MAGENTA -> normalMagenta
                TextColor.ANSI.MAGENTA_BRIGHT -> brightMagenta
                TextColor.ANSI.RED -> normalRed
                TextColor.ANSI.RED_BRIGHT -> brightRed
                TextColor.ANSI.WHITE -> normalWhite
                TextColor.ANSI.WHITE_BRIGHT -> brightWhite
                TextColor.ANSI.YELLOW -> normalYellow
                TextColor.ANSI.YELLOW_BRIGHT -> brightYellow
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TerminalEmulatorPalette) {
            return false
        }
        return Objects.equals(defaultColor, other.defaultColor) &&
            Objects.equals(defaultBrightColor, other.defaultBrightColor) &&
            Objects.equals(defaultBackgroundColor, other.defaultBackgroundColor) &&
            Objects.equals(normalBlack, other.normalBlack) &&
            Objects.equals(brightBlack, other.brightBlack) &&
            Objects.equals(normalRed, other.normalRed) &&
            Objects.equals(brightRed, other.brightRed) &&
            Objects.equals(normalGreen, other.normalGreen) &&
            Objects.equals(brightGreen, other.brightGreen) &&
            Objects.equals(normalYellow, other.normalYellow) &&
            Objects.equals(brightYellow, other.brightYellow) &&
            Objects.equals(normalBlue, other.normalBlue) &&
            Objects.equals(brightBlue, other.brightBlue) &&
            Objects.equals(normalMagenta, other.normalMagenta) &&
            Objects.equals(brightMagenta, other.brightMagenta) &&
            Objects.equals(normalCyan, other.normalCyan) &&
            Objects.equals(brightCyan, other.brightCyan) &&
            Objects.equals(normalWhite, other.normalWhite) &&
            Objects.equals(brightWhite, other.brightWhite)
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 47 * hash + defaultColor.hashCode()
        hash = 47 * hash + defaultBrightColor.hashCode()
        hash = 47 * hash + defaultBackgroundColor.hashCode()
        hash = 47 * hash + normalBlack.hashCode()
        hash = 47 * hash + brightBlack.hashCode()
        hash = 47 * hash + normalRed.hashCode()
        hash = 47 * hash + brightRed.hashCode()
        hash = 47 * hash + normalGreen.hashCode()
        hash = 47 * hash + brightGreen.hashCode()
        hash = 47 * hash + normalYellow.hashCode()
        hash = 47 * hash + brightYellow.hashCode()
        hash = 47 * hash + normalBlue.hashCode()
        hash = 47 * hash + brightBlue.hashCode()
        hash = 47 * hash + normalMagenta.hashCode()
        hash = 47 * hash + brightMagenta.hashCode()
        hash = 47 * hash + normalCyan.hashCode()
        hash = 47 * hash + brightCyan.hashCode()
        hash = 47 * hash + normalWhite.hashCode()
        hash = 47 * hash + brightWhite.hashCode()
        return hash
    }

    override fun toString(): String {
        return "SwingTerminalPalette{" +
            "defaultColor=$defaultColor" +
            ", defaultBrightColor=$defaultBrightColor" +
            ", defaultBackgroundColor=$defaultBackgroundColor" +
            ", normalBlack=$normalBlack" +
            ", brightBlack=$brightBlack" +
            ", normalRed=$normalRed" +
            ", brightRed=$brightRed" +
            ", normalGreen=$normalGreen" +
            ", brightGreen=$brightGreen" +
            ", normalYellow=$normalYellow" +
            ", brightYellow=$brightYellow" +
            ", normalBlue=$normalBlue" +
            ", brightBlue=$brightBlue" +
            ", normalMagenta=$normalMagenta" +
            ", brightMagenta=$brightMagenta" +
            ", normalCyan=$normalCyan" +
            ", brightCyan=$brightCyan" +
            ", normalWhite=$normalWhite" +
            ", brightWhite=$brightWhite" +
            '}'
    }

    companion object {
        val GNOME_TERMINAL =
            TerminalEmulatorPalette(
                Color(211, 215, 207),
                Color(238, 238, 236),
                Color(46, 52, 54),
                Color(46, 52, 54),
                Color(85, 87, 83),
                Color(204, 0, 0),
                Color(239, 41, 41),
                Color(78, 154, 6),
                Color(138, 226, 52),
                Color(196, 160, 0),
                Color(252, 233, 79),
                Color(52, 101, 164),
                Color(114, 159, 207),
                Color(117, 80, 123),
                Color(173, 127, 168),
                Color(6, 152, 154),
                Color(52, 226, 226),
                Color(211, 215, 207),
                Color(238, 238, 236),
            )

        val STANDARD_VGA =
            TerminalEmulatorPalette(
                Color(170, 170, 170),
                Color(255, 255, 255),
                Color(0, 0, 0),
                Color(0, 0, 0),
                Color(85, 85, 85),
                Color(170, 0, 0),
                Color(255, 85, 85),
                Color(0, 170, 0),
                Color(85, 255, 85),
                Color(170, 85, 0),
                Color(255, 255, 85),
                Color(0, 0, 170),
                Color(85, 85, 255),
                Color(170, 0, 170),
                Color(255, 85, 255),
                Color(0, 170, 170),
                Color(85, 255, 255),
                Color(170, 170, 170),
                Color(255, 255, 255),
            )

        val WINDOWS_XP_COMMAND_PROMPT =
            TerminalEmulatorPalette(
                Color(192, 192, 192),
                Color(255, 255, 255),
                Color(0, 0, 0),
                Color(0, 0, 0),
                Color(128, 128, 128),
                Color(128, 0, 0),
                Color(255, 0, 0),
                Color(0, 128, 0),
                Color(0, 255, 0),
                Color(128, 128, 0),
                Color(255, 255, 0),
                Color(0, 0, 128),
                Color(0, 0, 255),
                Color(128, 0, 128),
                Color(255, 0, 255),
                Color(0, 128, 128),
                Color(0, 255, 255),
                Color(192, 192, 192),
                Color(255, 255, 255),
            )

        val MAC_OS_X_TERMINAL_APP =
            TerminalEmulatorPalette(
                Color(203, 204, 205),
                Color(233, 235, 235),
                Color(0, 0, 0),
                Color(0, 0, 0),
                Color(129, 131, 131),
                Color(194, 54, 33),
                Color(252, 57, 31),
                Color(37, 188, 36),
                Color(49, 231, 34),
                Color(173, 173, 39),
                Color(234, 236, 35),
                Color(73, 46, 225),
                Color(88, 51, 255),
                Color(211, 56, 211),
                Color(249, 53, 248),
                Color(51, 187, 200),
                Color(20, 240, 240),
                Color(203, 204, 205),
                Color(233, 235, 235),
            )

        val PUTTY =
            TerminalEmulatorPalette(
                Color(187, 187, 187),
                Color(255, 255, 255),
                Color(0, 0, 0),
                Color(0, 0, 0),
                Color(85, 85, 85),
                Color(187, 0, 0),
                Color(255, 85, 85),
                Color(0, 187, 0),
                Color(85, 255, 85),
                Color(187, 187, 0),
                Color(255, 255, 85),
                Color(0, 0, 187),
                Color(85, 85, 255),
                Color(187, 0, 187),
                Color(255, 85, 255),
                Color(0, 187, 187),
                Color(85, 255, 255),
                Color(187, 187, 187),
                Color(255, 255, 255),
            )

        val XTERM =
            TerminalEmulatorPalette(
                Color(229, 229, 229),
                Color(255, 255, 255),
                Color(0, 0, 0),
                Color(0, 0, 0),
                Color(127, 127, 127),
                Color(205, 0, 0),
                Color(255, 0, 0),
                Color(0, 205, 0),
                Color(0, 255, 0),
                Color(205, 205, 0),
                Color(255, 255, 0),
                Color(0, 0, 238),
                Color(92, 92, 255),
                Color(205, 0, 205),
                Color(255, 0, 255),
                Color(0, 205, 205),
                Color(0, 255, 255),
                Color(229, 229, 229),
                Color(255, 255, 255),
            )

        val DEFAULT = GNOME_TERMINAL
    }
}
