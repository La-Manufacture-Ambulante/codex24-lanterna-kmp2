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

/**
 * This class specifies the palette of colors the terminal will use for the normally available 8 + 1 ANSI colors but
 * also their 'bright' versions with are normally enabled through bold mode. There are several palettes available, all
 * based on popular terminal emulators. All colors are defined in the AWT format.
 * @author Martin
 */
@Suppress("WeakerAccess")
open class TerminalEmulatorPalette(
    private val defaultColor: Color?,
    private val defaultBrightColor: Color?,
    private val defaultBackgroundColor: Color?,
    private val normalBlack: Color?,
    private val brightBlack: Color?,
    private val normalRed: Color?,
    private val brightRed: Color?,
    private val normalGreen: Color?,
    private val brightGreen: Color?,
    private val normalYellow: Color?,
    private val brightYellow: Color?,
    private val normalBlue: Color?,
    private val brightBlue: Color?,
    private val normalMagenta: Color?,
    private val brightMagenta: Color?,
    private val normalCyan: Color?,
    private val brightCyan: Color?,
    private val normalWhite: Color?,
    private val brightWhite: Color?
) {
    /**
     * Returns the AWT color from this palette given an ANSI color and two hints for if we are looking for a background
     * color and if we want to use the bright version.
     * @param color Which ANSI color we want to extract
     * @param isForeground Is this color we extract going to be used as a background color?
     * @param useBrightTones If true, we should return the bright version of the color
     * @return AWT color extracted from this palette for the input parameters
     */
    open fun get(color: TextColor.ANSI, isForeground: Boolean, useBrightTones: Boolean): Color? {
        if (useBrightTones) {
            when (color) {
                TextColor.ANSI.BLACK, TextColor.ANSI.BLACK_BRIGHT -> return brightBlack
                TextColor.ANSI.BLUE, TextColor.ANSI.BLUE_BRIGHT -> return brightBlue
                TextColor.ANSI.CYAN, TextColor.ANSI.CYAN_BRIGHT -> return brightCyan
                TextColor.ANSI.DEFAULT -> return if (isForeground) defaultBrightColor else defaultBackgroundColor
                TextColor.ANSI.GREEN, TextColor.ANSI.GREEN_BRIGHT -> return brightGreen
                TextColor.ANSI.MAGENTA, TextColor.ANSI.MAGENTA_BRIGHT -> return brightMagenta
                TextColor.ANSI.RED, TextColor.ANSI.RED_BRIGHT -> return brightRed
                TextColor.ANSI.WHITE, TextColor.ANSI.WHITE_BRIGHT -> return brightWhite
                TextColor.ANSI.YELLOW, TextColor.ANSI.YELLOW_BRIGHT -> return brightYellow
            }
        } else {
            when (color) {
                TextColor.ANSI.BLACK -> return normalBlack
                TextColor.ANSI.BLACK_BRIGHT -> return brightBlack
                TextColor.ANSI.BLUE -> return normalBlue
                TextColor.ANSI.BLUE_BRIGHT -> return brightBlue
                TextColor.ANSI.CYAN -> return normalCyan
                TextColor.ANSI.CYAN_BRIGHT -> return brightCyan
                TextColor.ANSI.DEFAULT -> return if (isForeground) defaultColor else defaultBackgroundColor
                TextColor.ANSI.GREEN -> return normalGreen
                TextColor.ANSI.GREEN_BRIGHT -> return brightGreen
                TextColor.ANSI.MAGENTA -> return normalMagenta
                TextColor.ANSI.MAGENTA_BRIGHT -> return brightMagenta
                TextColor.ANSI.RED -> return normalRed
                TextColor.ANSI.RED_BRIGHT -> return brightRed
                TextColor.ANSI.WHITE -> return normalWhite
                TextColor.ANSI.WHITE_BRIGHT -> return brightWhite
                TextColor.ANSI.YELLOW -> return normalYellow
                TextColor.ANSI.YELLOW_BRIGHT -> return brightYellow
            }
        }
        throw IllegalArgumentException("Unknown text color $color")
    }

    @Suppress("SimplifiableIfStatement")
    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as TerminalEmulatorPalette
        if (!Objects.equals(this.defaultColor, other.defaultColor)) {
            return false
        }
        if (!Objects.equals(this.defaultBrightColor, other.defaultBrightColor)) {
            return false
        }
        if (!Objects.equals(this.defaultBackgroundColor, other.defaultBackgroundColor)) {
            return false
        }
        if (!Objects.equals(this.normalBlack, other.normalBlack)) {
            return false
        }
        if (!Objects.equals(this.brightBlack, other.brightBlack)) {
            return false
        }
        if (!Objects.equals(this.normalRed, other.normalRed)) {
            return false
        }
        if (!Objects.equals(this.brightRed, other.brightRed)) {
            return false
        }
        if (!Objects.equals(this.normalGreen, other.normalGreen)) {
            return false
        }
        if (!Objects.equals(this.brightGreen, other.brightGreen)) {
            return false
        }
        if (!Objects.equals(this.normalYellow, other.normalYellow)) {
            return false
        }
        if (!Objects.equals(this.brightYellow, other.brightYellow)) {
            return false
        }
        if (!Objects.equals(this.normalBlue, other.normalBlue)) {
            return false
        }
        if (!Objects.equals(this.brightBlue, other.brightBlue)) {
            return false
        }
        if (!Objects.equals(this.normalMagenta, other.normalMagenta)) {
            return false
        }
        if (!Objects.equals(this.brightMagenta, other.brightMagenta)) {
            return false
        }
        if (!Objects.equals(this.normalCyan, other.normalCyan)) {
            return false
        }
        if (!Objects.equals(this.brightCyan, other.brightCyan)) {
            return false
        }
        if (!Objects.equals(this.normalWhite, other.normalWhite)) {
            return false
        }
        return Objects.equals(this.brightWhite, other.brightWhite)
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 47 * hash + (if (this.defaultColor != null) this.defaultColor.hashCode() else 0)
        hash = 47 * hash + (if (this.defaultBrightColor != null) this.defaultBrightColor.hashCode() else 0)
        hash = 47 * hash + (if (this.defaultBackgroundColor != null) this.defaultBackgroundColor.hashCode() else 0)
        hash = 47 * hash + (if (this.normalBlack != null) this.normalBlack.hashCode() else 0)
        hash = 47 * hash + (if (this.brightBlack != null) this.brightBlack.hashCode() else 0)
        hash = 47 * hash + (if (this.normalRed != null) this.normalRed.hashCode() else 0)
        hash = 47 * hash + (if (this.brightRed != null) this.brightRed.hashCode() else 0)
        hash = 47 * hash + (if (this.normalGreen != null) this.normalGreen.hashCode() else 0)
        hash = 47 * hash + (if (this.brightGreen != null) this.brightGreen.hashCode() else 0)
        hash = 47 * hash + (if (this.normalYellow != null) this.normalYellow.hashCode() else 0)
        hash = 47 * hash + (if (this.brightYellow != null) this.brightYellow.hashCode() else 0)
        hash = 47 * hash + (if (this.normalBlue != null) this.normalBlue.hashCode() else 0)
        hash = 47 * hash + (if (this.brightBlue != null) this.brightBlue.hashCode() else 0)
        hash = 47 * hash + (if (this.normalMagenta != null) this.normalMagenta.hashCode() else 0)
        hash = 47 * hash + (if (this.brightMagenta != null) this.brightMagenta.hashCode() else 0)
        hash = 47 * hash + (if (this.normalCyan != null) this.normalCyan.hashCode() else 0)
        hash = 47 * hash + (if (this.brightCyan != null) this.brightCyan.hashCode() else 0)
        hash = 47 * hash + (if (this.normalWhite != null) this.normalWhite.hashCode() else 0)
        hash = 47 * hash + (if (this.brightWhite != null) this.brightWhite.hashCode() else 0)
        return hash
    }

    override fun toString(): String {
        return "SwingTerminalPalette{" +
            "defaultColor=" + defaultColor +
            ", defaultBrightColor=" + defaultBrightColor +
            ", defaultBackgroundColor=" + defaultBackgroundColor +
            ", normalBlack=" + normalBlack +
            ", brightBlack=" + brightBlack +
            ", normalRed=" + normalRed +
            ", brightRed=" + brightRed +
            ", normalGreen=" + normalGreen +
            ", brightGreen=" + brightGreen +
            ", normalYellow=" + normalYellow +
            ", brightYellow=" + brightYellow +
            ", normalBlue=" + normalBlue +
            ", brightBlue=" + brightBlue +
            ", normalMagenta=" + normalMagenta +
            ", brightMagenta=" + brightMagenta +
            ", normalCyan=" + normalCyan +
            ", brightCyan=" + brightCyan +
            ", normalWhite=" + normalWhite +
            ", brightWhite=" + brightWhite + '}'
    }

    companion object {
        /**
         * Values taken from gnome-terminal on Ubuntu
         */
        @JvmField
        val GNOME_TERMINAL: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(238, 238, 236)
        )

        /**
         * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
         * wikipedia</a>, these are supposed to be the standard VGA palette.
         */
        @JvmField
        val STANDARD_VGA: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(255, 255, 255)
        )

        /**
         * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
         * wikipedia</a>, these are supposed to be what Windows XP cmd is using.
         */
        @JvmField
        val WINDOWS_XP_COMMAND_PROMPT: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(255, 255, 255)
        )

        /**
         * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
         * wikipedia</a>, these are supposed to be what terminal.app on MacOSX is using.
         */
        @JvmField
        val MAC_OS_X_TERMINAL_APP: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(233, 235, 235)
        )

        /**
         * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
         * wikipedia</a>, these are supposed to be what putty is using.
         */
        @JvmField
        val PUTTY: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(255, 255, 255)
        )

        /**
         * Values taken from <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">
         * wikipedia</a>, these are supposed to be what xterm is using.
         */
        @JvmField
        val XTERM: TerminalEmulatorPalette = TerminalEmulatorPalette(
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
            Color(255, 255, 255)
        )

        /**
         * Default colors the SwingTerminal is using if you don't specify anything
         */
        @JvmField
        val DEFAULT: TerminalEmulatorPalette = GNOME_TERMINAL
    }
}
