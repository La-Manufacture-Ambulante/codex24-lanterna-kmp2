package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.ScreenTranslator
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.screen.WrapBehaviour
import com.googlecode.lanterna.internal.compat.EnumSet

class TextGraphicsWriter(private val backend: TextGraphics) : StyleSet<TextGraphicsWriter?>, ScreenTranslator {
    var cursorPosition: TerminalPosition = TerminalPosition(0, 0)

    override var foregroundColor: TextColor? = null
    override var backgroundColor: TextColor? = null

    private val style = EnumSet.noneOf(SGR::class)

    override val activeModifiers: EnumSet<SGR>
        get() = EnumSet.copyOf(style)

    var wrapBehaviour: WrapBehaviour = WrapBehaviour.WORD
    var isStyleable: Boolean = true

    private data class WordPart(
        val word: String,
        val wordLen: Int,
        val style: StyleSet.Set,
    )

    private val chunkQueue = ArrayList<WordPart>()

    init {
        setStyleFrom(backend)
    }

    fun putString(string: String): TextGraphicsWriter {
        val wordPart = StringBuilder()
        val originalStyle = StyleSet.Set(backend)
        backend.setStyleFrom(this)

        var wordLen = 0
        var i = 0
        while (i < string.length) {
            val ch = string[i]
            when (ch) {
                '\n' -> {
                    flush(wordPart, wordLen)
                    wordLen = 0
                    linefeed(-1)
                }

                '\t' -> {
                    flush(wordPart, wordLen)
                    wordLen = 0
                    if (backend.tabBehaviour != TabBehaviour.IGNORE) {
                        val repl = backend.tabBehaviour?.getTabReplacement(cursorPosition.column) ?: ""
                        for (j in repl.indices) {
                            backend.setCharacter(cursorPosition.withRelativeColumn(j), repl[j])
                        }
                        cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(repl.length))
                    } else {
                        linefeed(2)
                        putControlChar(ch)
                    }
                }

                '\u001b' -> {
                    if (isStyleable) {
                        stash(wordPart, wordLen)
                        val seq = requireNotNull(TerminalTextUtils.getANSIControlSequenceAt(string, i))
                        TerminalTextUtils.updateModifiersFromCSICode(seq, this, originalStyle)
                        backend.setStyleFrom(this)
                        i += seq.length - 1
                    } else {
                        flush(wordPart, wordLen)
                        wordLen = 0
                        linefeed(2)
                        putControlChar(ch)
                    }
                }

                else -> {
                    when {
                        com.googlecode.lanterna.internal.compat.Character.isISOControl(ch) -> {
                            flush(wordPart, wordLen)
                            wordLen = 0
                            linefeed(1)
                            putControlChar(ch)
                        }

                        com.googlecode.lanterna.internal.compat.Character.isWhitespace(ch) -> {
                            flush(wordPart, wordLen)
                            wordLen = 0
                            backend.setCharacter(cursorPosition, ch)
                            cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(1))
                        }

                        TerminalTextUtils.isCharCJK(ch) -> {
                            flush(wordPart, wordLen)
                            wordLen = 0
                            linefeed(2)
                            backend.setCharacter(cursorPosition, ch)
                            cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(2))
                        }

                        else -> {
                            if (wrapBehaviour.keepWords()) {
                                wordPart.append(ch)
                                wordLen++
                            } else {
                                linefeed(1)
                                backend.setCharacter(cursorPosition, ch)
                                cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(1))
                            }
                        }
                    }
                }
            }
            linefeed(wordLen)
            i++
        }

        flush(wordPart, wordLen)
        backend.setStyleFrom(originalStyle)
        return this
    }

    private fun linefeed(lenToFit: Int) {
        val curCol = cursorPosition.column
        val backendSize = backend.size ?: return
        val spaceLeft = backendSize.columns - curCol
        if (wrapBehaviour.allowLineFeed()) {
            val wantWrap = curCol > 0 && lenToFit > spaceLeft
            if (lenToFit < 0 || (wantWrap && wrapBehaviour.autoWrap())) {
                cursorPosition = requireNotNull(cursorPosition.withColumn(0)?.withRelativeRow(1))
            }
        } else if (lenToFit < 0) {
            putControlChar('\n')
        }
    }

    fun putControlChar(ch: Char) {
        val subst = when (ch) {
            '\u001b' -> '['
            '\u001c' -> '\\'
            '\u001d' -> ']'
            '\u001e' -> '^'
            '\u001f' -> '_'
            '\u007f' -> '?'
            else -> {
                if (ch.code <= 26) {
                    (ch.code + '@'.code).toChar()
                } else {
                    backend.setCharacter(cursorPosition, ch)
                    cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(1))
                    return
                }
            }
        }

        val active = activeModifiers
        if (active.contains(SGR.REVERSE)) {
            active.remove(SGR.REVERSE)
        } else {
            active.add(SGR.REVERSE)
        }
        var tc = TextCharacter('^', foregroundColor, backgroundColor, active)
        backend.setCharacter(cursorPosition, tc)
        cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(1))
        tc = tc.withCharacter(subst)
        backend.setCharacter(cursorPosition, tc)
        cursorPosition = requireNotNull(cursorPosition.withRelativeColumn(1))
    }

    private fun stash(word: StringBuilder, wordLen: Int) {
        if (word.isNotEmpty()) {
            val chunk = WordPart(word.toString(), wordLen, StyleSet.Set(this))
            chunkQueue.add(chunk)
            word.setLength(0)
        }
    }

    private fun flush(word: StringBuilder, wordLen: Int) {
        stash(word, wordLen)
        if (chunkQueue.isEmpty()) {
            return
        }
        val row = cursorPosition.row
        val col = cursorPosition.column
        var offset = 0
        for (chunk in chunkQueue) {
            backend.setStyleFrom(chunk.style)
            backend.putString(col + offset, row, chunk.word)
            offset = chunk.wordLen
        }
        chunkQueue.clear()
        cursorPosition = requireNotNull(cursorPosition.withColumn(col + offset))
        backend.setStyleFrom(this)
    }

    override fun setForegroundColor(foregroundColor: TextColor?): TextGraphicsWriter {
        this.foregroundColor = foregroundColor
        return this
    }

    override fun setBackgroundColor(backgroundColor: TextColor?): TextGraphicsWriter {
        this.backgroundColor = backgroundColor
        return this
    }

    override fun enableModifiers(vararg modifiers: SGR?): TextGraphicsWriter {
        style.addAll(listOf(*modifiers).filterNotNull())
        return this
    }

    override fun disableModifiers(vararg modifiers: SGR?): TextGraphicsWriter {
        style.removeAll(listOf(*modifiers).filterNotNull().toSet())
        return this
    }

    override fun setModifiers(modifiers: EnumSet<SGR>?): TextGraphicsWriter {
        style.clear()
        if (modifiers != null) {
            style.addAll(modifiers)
        }
        return this
    }

    override fun clearModifiers(): TextGraphicsWriter {
        style.clear()
        return this
    }

    override fun setStyleFrom(source: StyleSet<*>?): TextGraphicsWriter {
        if (source != null) {
            setBackgroundColor(source.backgroundColor)
            setForegroundColor(source.foregroundColor)
            setModifiers(source.activeModifiers)
        }
        return this
    }

    override fun toScreenPosition(pos: TerminalPosition?): TerminalPosition? {
        return backend.toScreenPosition(pos ?: cursorPosition)
    }
}
