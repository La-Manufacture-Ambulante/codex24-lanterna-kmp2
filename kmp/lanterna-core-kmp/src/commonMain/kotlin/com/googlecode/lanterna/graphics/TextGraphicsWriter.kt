package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.ScreenTranslator
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.screen.WrapBehaviour
import java.util.ArrayList
import java.util.Arrays
import java.util.EnumSet

open class TextGraphicsWriter(private val backend: TextGraphics) : StyleSet<TextGraphicsWriter>, ScreenTranslator {
    private var cursorPosition: TerminalPosition
    private var foregroundColor: TextColor? = null
    private var backgroundColor: TextColor? = null
    private val style: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)
    private var wrapBehaviour: WrapBehaviour = WrapBehaviour.WORD
    private var styleable: Boolean = true

    init {
        setStyleFrom(backend)
        cursorPosition = TerminalPosition(0, 0)
    }

    open fun putString(string: String): TextGraphicsWriter {
        val wordpart = StringBuilder()
        val originalStyle = StyleSet.Set(backend)
        backend.setStyleFrom(this)

        var wordlen = 0
        var i = 0
        while (i < string.length) {
            val ch = string[i]
            when (ch) {
                '\n' -> {
                    flush(wordpart, wordlen)
                    wordlen = 0
                    linefeed(-1)
                }
                '\t' -> {
                    flush(wordpart, wordlen)
                    wordlen = 0
                    if (backend.tabBehaviour != TabBehaviour.IGNORE) {
                        val repl = backend.tabBehaviour.getTabReplacement(cursorPosition.column)
                        for (j in repl.indices) {
                            backend.setCharacter(cursorPosition.withRelativeColumn(j), repl[j])
                        }
                        cursorPosition = cursorPosition.withRelativeColumn(repl.length)
                    } else {
                        linefeed(2)
                        putControlChar(ch)
                    }
                }
                '\u001B' -> {
                    if (isStyleable()) {
                        stash(wordpart, wordlen)
                        val seq = TerminalTextUtils.getANSIControlSequenceAt(string, i)
                        TerminalTextUtils.updateModifiersFromCSICode(seq, this, originalStyle)
                        backend.setStyleFrom(this)
                        i += seq.length - 1
                    } else {
                        flush(wordpart, wordlen)
                        wordlen = 0
                        linefeed(2)
                        putControlChar(ch)
                    }
                }
                else -> {
                    if (Character.isISOControl(ch)) {
                        flush(wordpart, wordlen)
                        wordlen = 0
                        linefeed(1)
                        putControlChar(ch)
                    } else if (Character.isWhitespace(ch)) {
                        flush(wordpart, wordlen)
                        wordlen = 0
                        backend.setCharacter(cursorPosition, ch)
                        cursorPosition = cursorPosition.withRelativeColumn(1)
                    } else if (TerminalTextUtils.isCharCJK(ch)) {
                        flush(wordpart, wordlen)
                        wordlen = 0
                        linefeed(2)
                        backend.setCharacter(cursorPosition, ch)
                        cursorPosition = cursorPosition.withRelativeColumn(2)
                    } else {
                        if (wrapBehaviour.keepWords()) {
                            wordpart.append(ch)
                            wordlen++
                        } else {
                            linefeed(1)
                            backend.setCharacter(cursorPosition, ch)
                            cursorPosition = cursorPosition.withRelativeColumn(1)
                        }
                    }
                }
            }
            linefeed(wordlen)
            i++
        }
        flush(wordpart, wordlen)
        backend.setStyleFrom(originalStyle)
        return this
    }

    private fun linefeed(lenToFit: Int) {
        val curCol = cursorPosition.column
        val spaceLeft = backend.size.columns - curCol
        if (wrapBehaviour.allowLineFeed()) {
            val wantWrap = curCol > 0 && lenToFit > spaceLeft
            if (lenToFit < 0 || (wantWrap && wrapBehaviour.autoWrap())) {
                cursorPosition = cursorPosition.withColumn(0).withRelativeRow(1)
            }
        } else {
            if (lenToFit < 0) {
                putControlChar('\n')
            }
        }
    }

    open fun putControlChar(ch: Char) {
        val subst: Char = when (ch) {
            '\u001B' -> '['
            '\u001C' -> '\\'
            '\u001D' -> ']'
            '\u001E' -> '^'
            '\u001F' -> '_'
            '\u007F' -> '?'
            else -> {
                if (ch.code <= 26) {
                    (ch.code + '@'.code).toChar()
                } else {
                    backend.setCharacter(cursorPosition, ch)
                    cursorPosition = cursorPosition.withRelativeColumn(1)
                    return
                }
            }
        }
        val style = getActiveModifiers()
        if (style.contains(SGR.REVERSE)) {
            style.remove(SGR.REVERSE)
        } else {
            style.add(SGR.REVERSE)
        }
        var tc = TextCharacter('^', getForegroundColor(), getBackgroundColor(), style)
        backend.setCharacter(cursorPosition, tc)
        cursorPosition = cursorPosition.withRelativeColumn(1)
        tc = tc.withCharacter(subst)
        backend.setCharacter(cursorPosition, tc)
        cursorPosition = cursorPosition.withRelativeColumn(1)
    }

    private class WordPart(word: String, val wordlen: Int, style: StyleSet<*>) : StyleSet.Set() {
        val word: String = word

        init {
            setStyleFrom(style)
        }
    }

    private val chunk_queue: MutableList<WordPart> = ArrayList()

    private fun stash(word: StringBuilder, wordlen: Int) {
        if (word.length > 0) {
            val chunk = WordPart(word.toString(), wordlen, this)
            chunk_queue.add(chunk)
            word.setLength(0)
        }
    }

    private fun flush(word: StringBuilder, wordlen: Int) {
        stash(word, wordlen)
        if (chunk_queue.isEmpty()) {
            return
        }
        val row = cursorPosition.row
        val col = cursorPosition.column
        var offset = 0
        for (chunk in chunk_queue) {
            backend.setStyleFrom(chunk)
            backend.putString(col + offset, row, chunk.word)
            offset = chunk.wordlen
        }
        chunk_queue.clear()
        cursorPosition = cursorPosition.withColumn(col + offset)
        backend.setStyleFrom(this)
    }

    open fun getCursorPosition(): TerminalPosition {
        return cursorPosition
    }

    open fun setCursorPosition(cursorPosition: TerminalPosition) {
        this.cursorPosition = cursorPosition
    }

    override fun getForegroundColor(): TextColor? {
        return foregroundColor
    }

    open fun setForegroundColor(foreground: TextColor?): TextGraphicsWriter {
        this.foregroundColor = foreground
        return this
    }

    override fun getBackgroundColor(): TextColor? {
        return backgroundColor
    }

    open fun setBackgroundColor(background: TextColor?): TextGraphicsWriter {
        this.backgroundColor = background
        return this
    }

    override fun enableModifiers(modifiers: Array<out SGR>?): TextGraphicsWriter {
        val arr = modifiers ?: throw NullPointerException()
        @Suppress("UNCHECKED_CAST")
        style.addAll(Arrays.asList(*arr) as Collection<SGR>)
        return this
    }

    override fun disableModifiers(modifiers: Array<out SGR>?): TextGraphicsWriter {
        val arr = modifiers ?: throw NullPointerException()
        style.removeAll(Arrays.asList(*arr))
        return this
    }

    override fun setModifiers(modifiers: EnumSet<SGR>): TextGraphicsWriter {
        style.clear()
        style.addAll(modifiers)
        return this
    }

    override fun clearModifiers(): TextGraphicsWriter {
        style.clear()
        return this
    }

    override fun getActiveModifiers(): EnumSet<SGR> {
        return EnumSet.copyOf(style)
    }

    override fun setStyleFrom(source: StyleSet<*>): TextGraphicsWriter {
        setBackgroundColor(source.backgroundColor)
        setForegroundColor(source.foregroundColor)
        setModifiers(source.activeModifiers)
        return this
    }

    open fun getWrapBehaviour(): WrapBehaviour {
        return wrapBehaviour
    }

    open fun setWrapBehaviour(wrapBehaviour: WrapBehaviour) {
        this.wrapBehaviour = wrapBehaviour
    }

    open fun isStyleable(): Boolean {
        return styleable
    }

    open fun setStyleable(styleable: Boolean) {
        this.styleable = styleable
    }

    override fun toScreenPosition(pos: TerminalPosition?): TerminalPosition {
        return backend.toScreenPosition(if (pos != null) pos else getCursorPosition())
    }
}
