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

/**
 * This is an abstract base interface for terminal color definitions.
 *
 * Since there are different ways of specifying terminal colors, all with different ranges of adoption,
 * this makes it possible to program against an implementation-agnostic color definition.
 *
 * @author Martin
 */
interface TextColor {
    val foregroundSGRSequence: ByteArray?
    val backgroundSGRSequence: ByteArray?
    val red: Int
    val green: Int
    val blue: Int

    @Deprecated("Not available in common code")
    fun toColor(): Any? = null

    enum class ANSI(
        private val index: Int,
        val isBright: Boolean,
        override val red: Int,
        override val green: Int,
        override val blue: Int,
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
        WHITE_BRIGHT(7, true, 255, 255, 255),
        ;

        override val foregroundSGRSequence: ByteArray
            get() = "${if (isBright) 9 else 3}$index".encodeToByteArray()

        override val backgroundSGRSequence: ByteArray
            get() = "${if (isBright) 10 else 4}$index".encodeToByteArray()
    }

    class Indexed(private val colorIndex: Int) : TextColor {
        init {
            require(colorIndex in 0..255) {
                "Cannot create TextColor.Indexed with index $colorIndex, must be in range 0..255"
            }
        }

        override val foregroundSGRSequence: ByteArray
            get() = "38;5;$colorIndex".encodeToByteArray()

        override val backgroundSGRSequence: ByteArray
            get() = "48;5;$colorIndex".encodeToByteArray()

        override val red: Int
            get() = rgbForIndex(colorIndex).first

        override val green: Int
            get() = rgbForIndex(colorIndex).second

        override val blue: Int
            get() = rgbForIndex(colorIndex).third

        override fun equals(other: Any?): Boolean {
            return other is Indexed && other.colorIndex == colorIndex
        }

        override fun hashCode(): Int = colorIndex

        override fun toString(): String = "{IndexedColor:$colorIndex}"

        companion object {
            private val cubeLevels = intArrayOf(0, 95, 135, 175, 215, 255)
            private val ansiPalette = arrayOf(
                Triple(0, 0, 0),
                Triple(170, 0, 0),
                Triple(0, 170, 0),
                Triple(170, 85, 0),
                Triple(0, 0, 170),
                Triple(170, 0, 170),
                Triple(0, 170, 170),
                Triple(170, 170, 170),
                Triple(85, 85, 85),
                Triple(255, 85, 85),
                Triple(85, 255, 85),
                Triple(255, 255, 85),
                Triple(85, 85, 255),
                Triple(255, 85, 255),
                Triple(85, 255, 255),
                Triple(255, 255, 255),
            )

            private fun rgbForIndex(index: Int): Triple<Int, Int, Int> {
                if (index < ansiPalette.size) {
                    return ansiPalette[index]
                }
                if (index in 16..231) {
                    val cubeIndex = index - 16
                    val r = cubeIndex / 36
                    val g = (cubeIndex % 36) / 6
                    val b = cubeIndex % 6
                    return Triple(cubeLevels[r], cubeLevels[g], cubeLevels[b])
                }
                val gray = 8 + (index - 232) * 10
                return Triple(gray, gray, gray)
            }

            fun fromRGB(red: Int, green: Int, blue: Int): Indexed {
                require(red in 0..255 && green in 0..255 && blue in 0..255) {
                    "fromRGB components must all be in range 0..255"
                }
                val cubeR = ((red / 255.0) * 5.0).toInt()
                val cubeG = ((green / 255.0) * 5.0).toInt()
                val cubeB = ((blue / 255.0) * 5.0).toInt()
                val cubeIndex = cubeB + (6 * cubeG) + (36 * cubeR) + 16
                val fromCube = Indexed(cubeIndex)
                val avg = (red + green + blue) / 3
                val fromGray = fromGreyRamp(avg)

                val cubeDistance = sq(red - fromCube.red) + sq(green - fromCube.green) + sq(blue - fromCube.blue)
                val grayDistance = sq(red - fromGray.red) + sq(green - fromGray.green) + sq(blue - fromGray.blue)
                return if (cubeDistance <= grayDistance) fromCube else fromGray
            }

            fun fromGreyRamp(gray: Int): Indexed {
                require(gray in 0..255) { "fromGreyRamp value must be in range 0..255" }
                val slot = ((gray / 255.0) * 23.0).toInt()
                return Indexed(232 + slot)
            }

            private fun sq(value: Int): Int = value * value
        }
    }

    class RGB(
        override val red: Int,
        override val green: Int,
        override val blue: Int,
    ) : TextColor {
        init {
            require(red in 0..255 && green in 0..255 && blue in 0..255) {
                "Cannot create TextColor.RGB with components outside range 0..255"
            }
        }

        override val foregroundSGRSequence: ByteArray
            get() = "38;2;$red;$green;$blue".encodeToByteArray()

        override val backgroundSGRSequence: ByteArray
            get() = "48;2;$red;$green;$blue".encodeToByteArray()

        override fun equals(other: Any?): Boolean {
            return other is RGB && other.red == red && other.green == green && other.blue == blue
        }

        override fun hashCode(): Int {
            var hash = 7
            hash = 53 * hash + red
            hash = 53 * hash + green
            hash = 53 * hash + blue
            return hash
        }

        override fun toString(): String = "{RGB:$red,$green,$blue}"
    }

    object Factory {
        private val indexedPattern = Regex("^#[0-9]{1,3}$")
        private val rgbPattern = Regex("^#[0-9a-fA-F]{6}$")

        fun fromString(value: String?): TextColor {
            val safe = value?.trim().orEmpty()
            if (safe.isEmpty()) {
                return ANSI.DEFAULT
            }

            if (rgbPattern.matches(safe)) {
                val r = safe.substring(1, 3).toInt(16)
                val g = safe.substring(3, 5).toInt(16)
                val b = safe.substring(5, 7).toInt(16)
                return RGB(r, g, b)
            }
            if (indexedPattern.matches(safe)) {
                val index = safe.substring(1).toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid indexed color: $safe")
                return Indexed(index)
            }

            try {
                return ANSI.valueOf(safe.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown color definition \"$safe\"", e)
            }
        }
    }
}
