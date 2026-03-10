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
package com.googlecode.lanterna.input

import com.googlecode.lanterna.input.CharacterPattern.Matching
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

/**
 * Used to read the input stream character by character and generate `Key` objects to be put in the input queue.
 *
 * @author Martin, Andreas
 */
class InputDecoder(
    /**
     * Reader to read characters from, wrapped by a [BufferedReader].
     */
    source: Reader?
) {
    private val source: Reader = BufferedReader(requireNotNull(source) { "source" })
    private val bytePatterns: MutableList<CharacterPattern> = ArrayList()
    private val currentMatching: MutableList<Char> = ArrayList()
    private var seenEOF: Boolean = false
    private var timeoutUnits: Int = 0

    /**
     * Returns a collection of all patterns registered in this InputDecoder.
     * @return Collection of patterns in the InputDecoder
     */
    @get:Synchronized
    val patterns: Collection<CharacterPattern>
        get() {
            synchronized(bytePatterns) {
                return ArrayList(bytePatterns)
            }
        }

    /**
     * Adds another key decoding profile to this InputDecoder, which means all patterns from the profile will be used
     * when decoding input.
     * @param profile Profile to add
     */
    fun addProfile(profile: KeyDecodingProfile) {
        for (pattern in profile.patterns) {
            synchronized(bytePatterns) {
                // If an equivalent pattern already exists, remove it first.
                bytePatterns.remove(pattern)
                bytePatterns.add(pattern)
            }
        }
    }

    /**
     * Removes one pattern from the list of patterns in this InputDecoder.
     * @param pattern Pattern to remove
     * @return `true` if the supplied pattern was found and was removed, otherwise `false`
     */
    fun removePattern(pattern: CharacterPattern?): Boolean {
        synchronized(bytePatterns) {
            return bytePatterns.remove(pattern)
        }
    }

    /**
     * Sets the number of 1/4-second units for how long to try to get further input
     * to complete an escape-sequence for a special Key.
     *
     * Negative numbers are mapped to 0 (no wait at all), and unreasonably high
     * values are mapped to a maximum of 240 (1 minute).
     * @param units New timeout to use, in 250ms units
     */
    fun setTimeoutUnits(units: Int) {
        timeoutUnits = when {
            units < 0 -> 0
            units > 240 -> 240
            else -> units
        }
    }

    /**
     * Queries the current timeoutUnits value. One unit is 1/4 second.
     * @return The timeout this InputDecoder will use when waiting for additional input, in units of 1/4 seconds
     */
    fun getTimeoutUnits(): Int {
        return timeoutUnits
    }

    /**
     * Reads and decodes the next key stroke from the input stream.
     * @param blockingIO If set to `true`, the call will not return until it has read at least one [KeyStroke]
     * @return Key stroke read from the input stream, or `null` if none
     * @throws IOException If there was an I/O error when reading from the input stream
     */
    @Synchronized
    @Throws(IOException::class)
    fun getNextCharacter(blockingIO: Boolean): KeyStroke? {
        var bestMatch: KeyStroke? = null
        var bestLen = 0
        var curLen = 0

        while (true) {
            if (curLen < currentMatching.size) {
                // (Re-)consume characters previously read.
                curLen++
            } else {
                // If we already have a bestMatch but a chance for a longer match
                // then poll for the configured number of timeout units.
                // It would be much better if we could just read with a timeout,
                // but lacking that, we wait 1/4s units and check for readiness.
                if (bestMatch != null) {
                    var timeout = getTimeoutUnits()
                    while (timeout > 0 && !source.ready()) {
                        try {
                            timeout--
                            Thread.sleep(250)
                        } catch (_: InterruptedException) {
                            timeout = 0
                        }
                    }
                }

                // If input is available, read without waiting.
                // Otherwise, for blocking reads with no best match yet, wait for more input.
                if (source.ready() || (blockingIO && bestMatch == null)) {
                    val readChar = source.read()
                    if (readChar == -1) {
                        seenEOF = true
                        if (currentMatching.isEmpty()) {
                            return KeyStroke(KeyType.EOF)
                        }
                        break
                    }
                    currentMatching.add(readChar.toChar())
                    curLen++
                } else {
                    // No more available input at this time.
                    if (bestMatch != null) {
                        break
                    }
                    return null
                }
            }

            val curSub = currentMatching.subList(0, curLen)
            val matching = getBestMatch(curSub)

            // Full match found.
            if (matching.fullMatch != null) {
                bestMatch = matching.fullMatch
                bestLen = curLen

                if (!matching.partialMatch) {
                    break
                }
                continue
            }
            // No full match yet, but there is still potential.
            else if (matching.partialMatch) {
                continue
            }
            // No longer match possible at this point.
            else {
                if (bestMatch != null) {
                    // There was already a previous full match, use it.
                    break
                }
                // Invalid input: remove failed prefix and retry finding a KeyStroke.
                curSub.clear() // Alternative would be: currentMatching.removeAt(0)
                curLen = 0
                continue
            }
        }

        // Did we find anything? Otherwise return null.
        if (bestMatch == null) {
            if (seenEOF) {
                currentMatching.clear()
                return KeyStroke(KeyType.EOF)
            }
            return null
        }

        val bestSub = currentMatching.subList(0, bestLen)
        bestSub.clear() // Remove matched characters from input.
        return bestMatch
    }

    private fun getBestMatch(characterSequence: List<Char>?): Matching {
        var partialMatch = false
        var bestMatch: KeyStroke? = null
        synchronized(bytePatterns) {
            for (pattern in bytePatterns) {
                val res = pattern.match(characterSequence)
                if (res != null) {
                    if (res.partialMatch) {
                        partialMatch = true
                    }
                    if (res.fullMatch != null) {
                        bestMatch = res.fullMatch
                    }
                }
            }
        }
        return Matching(partialMatch, bestMatch)
    }
}
