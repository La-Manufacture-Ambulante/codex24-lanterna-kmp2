package com.googlecode.lanterna.input

import java.util.List

/**
 * Character pattern that matches one character as one KeyStroke with the character that was read
 *
 * @author Martin, Andreas
 */
open class NormalCharacterPattern : CharacterPattern {
    override fun match(seq: List<Char?>): Matching? {
        if (seq.size != 1) {
            return null // nope
        }
        val ch = seq[0] ?: throw NullPointerException()
        return if (isPrintableChar(ch)) {
            val ks = KeyStroke(ch, false, false)
            Matching(ks)
        } else {
            null // nope
        }
    }

    companion object {
        /**
         * From http://stackoverflow.com/questions/220547/printable-char-in-java
         * @param c character to test
         * @return True if this is a 'normal', printable character, false otherwise
         */
        private fun isPrintableChar(c: Char): Boolean {
            if (Character.isISOControl(c)) {
                return false
            }
            val block: Character.UnicodeBlock? = Character.UnicodeBlock.of(c)
            return block != null && block != Character.UnicodeBlock.SPECIALS
        }
    }
}
