package com.googlecode.lanterna

class Symbols private constructor() {
    companion object {
        @JvmField val FACE_WHITE: Char = '\u263A'
        @JvmField val FACE_BLACK: Char = '\u263B'
        @JvmField val HEART: Char = '\u2665'
        @JvmField val CLUB: Char = '\u2663'
        @JvmField val DIAMOND: Char = '\u2666'
        @JvmField val SPADES: Char = '\u2660'
        @JvmField val BULLET: Char = '\u2022'
        @JvmField val INVERSE_BULLET: Char = '\u25D8'
        @JvmField val WHITE_CIRCLE: Char = '\u25CB'
        @JvmField val INVERSE_WHITE_CIRCLE: Char = '\u25D9'

        @JvmField val SOLID_SQUARE: Char = '\u25A0'
        @JvmField val SOLID_SQUARE_SMALL: Char = '\u25AA'
        @JvmField val OUTLINED_SQUARE: Char = '\u25A1'
        @JvmField val OUTLINED_SQUARE_SMALL: Char = '\u25AB'

        @JvmField val FEMALE: Char = '\u2640'
        @JvmField val MALE: Char = '\u2642'

        @JvmField val ARROW_UP: Char = '\u2191'
        @JvmField val ARROW_DOWN: Char = '\u2193'
        @JvmField val ARROW_RIGHT: Char = '\u2192'
        @JvmField val ARROW_LEFT: Char = '\u2190'

        @JvmField val BLOCK_SOLID: Char = '\u2588'
        @JvmField val BLOCK_DENSE: Char = '\u2593'
        @JvmField val BLOCK_MIDDLE: Char = '\u2592'
        @JvmField val BLOCK_SPARSE: Char = '\u2591'

        @JvmField val TRIANGLE_RIGHT_POINTING_BLACK: Char = '\u25BA'
        @JvmField val TRIANGLE_LEFT_POINTING_BLACK: Char = '\u25C4'
        @JvmField val TRIANGLE_UP_POINTING_BLACK: Char = '\u25B2'
        @JvmField val TRIANGLE_DOWN_POINTING_BLACK: Char = '\u25BC'

        @JvmField val TRIANGLE_RIGHT_POINTING_MEDIUM_BLACK: Char = '\u23F4'
        @JvmField val TRIANGLE_LEFT_POINTING_MEDIUM_BLACK: Char = '\u23F5'
        @JvmField val TRIANGLE_UP_POINTING_MEDIUM_BLACK: Char = '\u23F6'
        @JvmField val TRIANGLE_DOWN_POINTING_MEDIUM_BLACK: Char = '\u23F7'

        @JvmField val SINGLE_LINE_HORIZONTAL: Char = '\u2500'
        @JvmField val BOLD_SINGLE_LINE_HORIZONTAL: Char = '\u2501'
        @JvmField val BOLD_TO_NORMAL_SINGLE_LINE_HORIZONTAL: Char = '\u257E'
        @JvmField val BOLD_FROM_NORMAL_SINGLE_LINE_HORIZONTAL: Char = '\u257C'
        @JvmField val DOUBLE_LINE_HORIZONTAL: Char = '\u2550'
        @JvmField val SINGLE_LINE_VERTICAL: Char = '\u2502'
        @JvmField val BOLD_SINGLE_LINE_VERTICAL: Char = '\u2503'
        @JvmField val BOLD_TO_NORMAL_SINGLE_LINE_VERTICAL: Char = '\u257F'
        @JvmField val BOLD_FROM_NORMAL_SINGLE_LINE_VERTICAL: Char = '\u257D'
        @JvmField val DOUBLE_LINE_VERTICAL: Char = '\u2551'

        @JvmField val SINGLE_LINE_TOP_LEFT_CORNER: Char = '\u250C'
        @JvmField val DOUBLE_LINE_TOP_LEFT_CORNER: Char = '\u2554'
        @JvmField val SINGLE_LINE_TOP_RIGHT_CORNER: Char = '\u2510'
        @JvmField val DOUBLE_LINE_TOP_RIGHT_CORNER: Char = '\u2557'

        @JvmField val SINGLE_LINE_BOTTOM_LEFT_CORNER: Char = '\u2514'
        @JvmField val DOUBLE_LINE_BOTTOM_LEFT_CORNER: Char = '\u255A'
        @JvmField val SINGLE_LINE_BOTTOM_RIGHT_CORNER: Char = '\u2518'
        @JvmField val DOUBLE_LINE_BOTTOM_RIGHT_CORNER: Char = '\u255D'

        @JvmField val SINGLE_LINE_CROSS: Char = '\u253C'
        @JvmField val DOUBLE_LINE_CROSS: Char = '\u256C'
        @JvmField val DOUBLE_LINE_HORIZONTAL_SINGLE_LINE_CROSS: Char = '\u256A'
        @JvmField val DOUBLE_LINE_VERTICAL_SINGLE_LINE_CROSS: Char = '\u256B'

        @JvmField val SINGLE_LINE_T_UP: Char = '\u2534'
        @JvmField val SINGLE_LINE_T_DOWN: Char = '\u252C'
        @JvmField val SINGLE_LINE_T_RIGHT: Char = '\u251C'
        @JvmField val SINGLE_LINE_T_LEFT: Char = '\u2524'

        @JvmField val SINGLE_LINE_T_DOUBLE_UP: Char = '\u2568'
        @JvmField val SINGLE_LINE_T_DOUBLE_DOWN: Char = '\u2565'
        @JvmField val SINGLE_LINE_T_DOUBLE_RIGHT: Char = '\u255E'
        @JvmField val SINGLE_LINE_T_DOUBLE_LEFT: Char = '\u2561'

        @JvmField val DOUBLE_LINE_T_UP: Char = '\u2569'
        @JvmField val DOUBLE_LINE_T_DOWN: Char = '\u2566'
        @JvmField val DOUBLE_LINE_T_RIGHT: Char = '\u2560'
        @JvmField val DOUBLE_LINE_T_LEFT: Char = '\u2563'

        @JvmField val DOUBLE_LINE_T_SINGLE_UP: Char = '\u2567'
        @JvmField val DOUBLE_LINE_T_SINGLE_DOWN: Char = '\u2564'
        @JvmField val DOUBLE_LINE_T_SINGLE_RIGHT: Char = '\u255F'
        @JvmField val DOUBLE_LINE_T_SINGLE_LEFT: Char = '\u2562'
    }
}
