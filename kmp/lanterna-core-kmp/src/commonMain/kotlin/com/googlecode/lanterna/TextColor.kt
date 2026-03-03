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
package com.googlecode.lanterna

import java.awt.Color
import java.io.Serializable
import java.util.regex.Pattern

interface TextColor : Serializable {
    fun getForegroundSGRSequence(): ByteArray
    fun getBackgroundSGRSequence(): ByteArray
    fun getRed(): Int
    fun getGreen(): Int
    fun getBlue(): Int

    @java.lang.Deprecated
    fun toColor(): Color

    enum class ANSI(
        private val index: Int,
        private val bright: Boolean,
        private val red: Int,
        private val green: Int,
        private val blue: Int
    ) : TextColor {
        BLACK(0, false, 0, 0, 0),
        RED(1, false, 170, 0, 0),
        GREEN(2, false, 0, 170, 0),
        YELLOW(3, false, 170, 85, 0),
        BLUE(4, false, 0, 0, 170),
        MAGENTA(5, false, 170, 0, 170),
        CYAN(6, false, 0, 170, 170),
        WHITE(7, false, 170, 170, 170),
        DEFAULT(9, false, 0, 0, 0),
        BLACK_BRIGHT(0, true, 85, 85, 85),
        RED_BRIGHT(1, true, 255, 85, 85),
        GREEN_BRIGHT(2, true, 85, 255, 85),
        YELLOW_BRIGHT(3, true, 255, 255, 85),
        BLUE_BRIGHT(4, true, 85, 85, 255),
        MAGENTA_BRIGHT(5, true, 255, 85, 255),
        CYAN_BRIGHT(6, true, 85, 255, 255),
        WHITE_BRIGHT(7, true, 255, 255, 255);

        private val foregroundSGR: ByteArray = String.format("%d%d", if (bright) 9 else 3, index).toByteArray()
        private val backgroundSGR: ByteArray = String.format("%d%d", if (bright) 10 else 4, index).toByteArray()

        override fun getForegroundSGRSequence(): ByteArray {
            return foregroundSGR.clone()
        }

        override fun getBackgroundSGRSequence(): ByteArray {
            return backgroundSGR.clone()
        }

        fun isBright(): Boolean {
            return bright
        }

        override fun getRed(): Int {
            return red
        }

        override fun getGreen(): Int {
            return green
        }

        override fun getBlue(): Int {
            return blue
        }

        override fun toColor(): Color {
            return Color(getRed(), getGreen(), getBlue())
        }
    }

    class Indexed(colorIndex: Int) : TextColor {
        private val colorIndex: Int

        init {
            if (colorIndex > 255 || colorIndex < 0) {
                throw IllegalArgumentException(
                    "Cannot create a Color.Indexed with a color index of $colorIndex, must be in the range of 0-255"
                )
            }
            this.colorIndex = colorIndex
        }

        override fun getForegroundSGRSequence(): ByteArray {
            return ("38;5;$colorIndex").toByteArray()
        }

        override fun getBackgroundSGRSequence(): ByteArray {
            return ("48;5;$colorIndex").toByteArray()
        }

        override fun getRed(): Int {
            return COLOR_TABLE[colorIndex][0].toInt() and 0x000000ff
        }

        override fun getGreen(): Int {
            return COLOR_TABLE[colorIndex][1].toInt() and 0x000000ff
        }

        override fun getBlue(): Int {
            return COLOR_TABLE[colorIndex][2].toInt() and 0x000000ff
        }

        override fun toColor(): Color {
            return Color(getRed(), getGreen(), getBlue())
        }

        override fun toString(): String {
            return "{IndexedColor:$colorIndex}"
        }

        override fun hashCode(): Int {
            var hash = 3
            hash = 43 * hash + this.colorIndex
            return hash
        }

        override fun equals(obj: Any?): Boolean {
            if (obj == null) {
                return false
            }
            if (javaClass != obj.javaClass) {
                return false
            }
            val other = obj as Indexed
            return this.colorIndex == other.colorIndex
        }

        companion object {
            private val COLOR_TABLE: Array<ByteArray> = arrayOf(
                byteArrayOf(0.toByte(), 0.toByte(), 0.toByte()),
                byteArrayOf(170.toByte(), 0.toByte(), 0.toByte()),
                byteArrayOf(0.toByte(), 170.toByte(), 0.toByte()),
                byteArrayOf(170.toByte(), 85.toByte(), 0.toByte()),
                byteArrayOf(0.toByte(), 0.toByte(), 170.toByte()),
                byteArrayOf(170.toByte(), 0.toByte(), 170.toByte()),
                byteArrayOf(0.toByte(), 170.toByte(), 170.toByte()),
                byteArrayOf(170.toByte(), 170.toByte(), 170.toByte()),
                byteArrayOf(85.toByte(), 85.toByte(), 85.toByte()),
                byteArrayOf(255.toByte(), 85.toByte(), 85.toByte()),
                byteArrayOf(85.toByte(), 255.toByte(), 85.toByte()),
                byteArrayOf(255.toByte(), 255.toByte(), 85.toByte()),
                byteArrayOf(85.toByte(), 85.toByte(), 255.toByte()),
                byteArrayOf(255.toByte(), 85.toByte(), 255.toByte()),
                byteArrayOf(85.toByte(), 255.toByte(), 255.toByte()),
                byteArrayOf(255.toByte(), 255.toByte(), 255.toByte()),

                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0x00.toByte(), 0xff.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0x5f.toByte(), 0xff.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0x87.toByte(), 0xff.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0xaf.toByte(), 0xff.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0xd7.toByte(), 0xff.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0x00.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0x5f.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0x87.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0xaf.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0xd7.toByte(), 0xff.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0x00.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0x5f.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0x87.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xaf.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xd7.toByte()),
                byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xff.toByte()),

                byteArrayOf(0x08.toByte(), 0x08.toByte(), 0x08.toByte()),
                byteArrayOf(0x12.toByte(), 0x12.toByte(), 0x12.toByte()),
                byteArrayOf(0x1c.toByte(), 0x1c.toByte(), 0x1c.toByte()),
                byteArrayOf(0x26.toByte(), 0x26.toByte(), 0x26.toByte()),
                byteArrayOf(0x30.toByte(), 0x30.toByte(), 0x30.toByte()),
                byteArrayOf(0x3a.toByte(), 0x3a.toByte(), 0x3a.toByte()),
                byteArrayOf(0x44.toByte(), 0x44.toByte(), 0x44.toByte()),
                byteArrayOf(0x4e.toByte(), 0x4e.toByte(), 0x4e.toByte()),
                byteArrayOf(0x58.toByte(), 0x58.toByte(), 0x58.toByte()),
                byteArrayOf(0x62.toByte(), 0x62.toByte(), 0x62.toByte()),
                byteArrayOf(0x6c.toByte(), 0x6c.toByte(), 0x6c.toByte()),
                byteArrayOf(0x76.toByte(), 0x76.toByte(), 0x76.toByte()),
                byteArrayOf(0x80.toByte(), 0x80.toByte(), 0x80.toByte()),
                byteArrayOf(0x8a.toByte(), 0x8a.toByte(), 0x8a.toByte()),
                byteArrayOf(0x94.toByte(), 0x94.toByte(), 0x94.toByte()),
                byteArrayOf(0x9e.toByte(), 0x9e.toByte(), 0x9e.toByte()),
                byteArrayOf(0xa8.toByte(), 0xa8.toByte(), 0xa8.toByte()),
                byteArrayOf(0xb2.toByte(), 0xb2.toByte(), 0xb2.toByte()),
                byteArrayOf(0xbc.toByte(), 0xbc.toByte(), 0xbc.toByte()),
                byteArrayOf(0xc6.toByte(), 0xc6.toByte(), 0xc6.toByte()),
                byteArrayOf(0xd0.toByte(), 0xd0.toByte(), 0xd0.toByte()),
                byteArrayOf(0xda.toByte(), 0xda.toByte(), 0xda.toByte()),
                byteArrayOf(0xe4.toByte(), 0xe4.toByte(), 0xe4.toByte()),
                byteArrayOf(0xee.toByte(), 0xee.toByte(), 0xee.toByte())
            )

            @JvmStatic
            fun fromRGB(red: Int, green: Int, blue: Int): Indexed {
                if (red < 0 || red > 255) {
                    throw IllegalArgumentException("fromRGB: red is outside of valid range (0-255)")
                }
                if (green < 0 || green > 255) {
                    throw IllegalArgumentException("fromRGB: green is outside of valid range (0-255)")
                }
                if (blue < 0 || blue > 255) {
                    throw IllegalArgumentException("fromRGB: blue is outside of valid range (0-255)")
                }

                val rescaledRed = ((red.toDouble() / 255.0) * 5.0).toInt()
                val rescaledGreen = ((green.toDouble() / 255.0) * 5.0).toInt()
                val rescaledBlue = ((blue.toDouble() / 255.0) * 5.0).toInt()

                val index = rescaledBlue + (6 * rescaledGreen) + (36 * rescaledRed) + 16
                val fromColorCube = Indexed(index)
                val fromGreyRamp = fromGreyRamp((red + green + blue) / 3)

                val coloredDistance =
                    ((red - fromColorCube.getRed()) * (red - fromColorCube.getRed())) +
                        ((green - fromColorCube.getGreen()) * (green - fromColorCube.getGreen())) +
                        ((blue - fromColorCube.getBlue()) * (blue - fromColorCube.getBlue()))
                val greyDistance =
                    ((red - fromGreyRamp.getRed()) * (red - fromGreyRamp.getRed())) +
                        ((green - fromGreyRamp.getGreen()) * (green - fromGreyRamp.getGreen())) +
                        ((blue - fromGreyRamp.getBlue()) * (blue - fromGreyRamp.getBlue()))
                return if (coloredDistance < greyDistance) {
                    fromColorCube
                } else {
                    fromGreyRamp
                }
            }

            private fun fromGreyRamp(intensity: Int): Indexed {
                val rescaled = ((intensity.toDouble() / 255.0) * 23.0).toInt() + 232
                return Indexed(rescaled)
            }
        }
    }

    class RGB(r: Int, g: Int, b: Int) : TextColor {
        private val red: Int
        private val green: Int
        private val blue: Int

        init {
            if (r < 0 || r > 255) {
                throw IllegalArgumentException("RGB: r is outside of valid range (0-255)")
            }
            if (g < 0 || g > 255) {
                throw IllegalArgumentException("RGB: g is outside of valid range (0-255)")
            }
            if (b < 0 || b > 255) {
                throw IllegalArgumentException("RGB: b is outside of valid range (0-255)")
            }
            this.red = r
            this.green = g
            this.blue = b
        }

        override fun getForegroundSGRSequence(): ByteArray {
            return ("38;2;" + getRed() + ";" + getGreen() + ";" + getBlue()).toByteArray()
        }

        override fun getBackgroundSGRSequence(): ByteArray {
            return ("48;2;" + getRed() + ";" + getGreen() + ";" + getBlue()).toByteArray()
        }

        override fun getRed(): Int {
            return red
        }

        override fun getGreen(): Int {
            return green
        }

        override fun getBlue(): Int {
            return blue
        }

        override fun toColor(): Color {
            return Color(getRed(), getGreen(), getBlue())
        }

        override fun toString(): String {
            return "{RGB:" + getRed() + "," + getGreen() + "," + getBlue() + "}"
        }

        override fun hashCode(): Int {
            var hash = 7
            hash = 29 * hash + red
            hash = 29 * hash + green
            hash = 29 * hash + blue
            return hash
        }

        override fun equals(obj: Any?): Boolean {
            if (obj == null) {
                return false
            }
            if (javaClass != obj.javaClass) {
                return false
            }
            val other = obj as RGB
            return this.red == other.red &&
                this.green == other.green &&
                this.blue == other.blue
        }

        companion object {
            @JvmStatic
            fun fromAWTColor(awtColor: Color): RGB {
                return RGB(awtColor.red, awtColor.green, awtColor.blue)
            }
        }
    }

    class Factory private constructor() {
        companion object {
            private val INDEXED_COLOR: Pattern = Pattern.compile("#[0-9]{1,3}")
            private val RGB_COLOR: Pattern = Pattern.compile("#[0-9a-fA-F]{6}")

            @JvmStatic
            fun fromString(value: String?): TextColor? {
                if (value == null) {
                    return null
                }
                val trimmed = value.trim()
                if (RGB_COLOR.matcher(trimmed).matches()) {
                    val r = Integer.parseInt(trimmed.substring(1, 3), 16)
                    val g = Integer.parseInt(trimmed.substring(3, 5), 16)
                    val b = Integer.parseInt(trimmed.substring(5, 7), 16)
                    return TextColor.RGB(r, g, b)
                } else if (INDEXED_COLOR.matcher(trimmed).matches()) {
                    val index = Integer.parseInt(trimmed.substring(1))
                    return TextColor.Indexed(index)
                }
                try {
                    return TextColor.ANSI.valueOf(trimmed.toUpperCase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Unknown color definition \"$trimmed\"", e)
                }
            }
        }
    }
}
