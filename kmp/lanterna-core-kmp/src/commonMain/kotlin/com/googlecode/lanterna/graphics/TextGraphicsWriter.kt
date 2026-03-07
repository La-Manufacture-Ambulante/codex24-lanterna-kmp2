package com.googlecode.lanterna.graphics

import java.util.ArrayList
import java.util.Arrays
import java.util.EnumSet

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.ScreenTranslator
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.screen.WrapBehaviour

 class TextGraphicsWriter(private val backend:TextGraphics?):StyleSet<TextGraphicsWriter?>, ScreenTranslator {
/**
 * @return the cursor position
 */
    /**
 * @param cursorPosition the cursor position to set
 */
     var cursorPosition:TerminalPosition? = null
private var foregroundColor:TextColor? = null
private var backgroundColor:TextColor? = null
private val style = EnumSet.noneOf(SGR::class.java)
/**
 * @return the wrapBehaviour
 */
    /**
 * @param wrapBehaviour the wrapBehaviour to set
 */
     var wrapBehaviour = WrapBehaviour.WORD
/**
 * @return whether styles in strings are handled.
 */
    /**
 * @param styleable whether styles in strings should be handled.
 */
     var isStyleable = true

private val chunk_queue = ArrayList()

 val activeModifiers:EnumSet<SGR?>?
@Override
get() {
return EnumSet.copyOf(style)
}

init{
setStyleFrom(backend!!)
cursorPosition = TerminalPosition(0, 0)
}

 fun putString(string:String):TextGraphicsWriter {
val wordpart = StringBuilder()
val originalStyle = StyleSet.Set(backend)
backend!!.setStyleFrom(this)

var wordlen = 0 // the whole column-length of the word.
var i = 0
while (i < string.length())
{
val ch = string.charAt(i)
when (ch) {
'\n' -> {
flush(wordpart, wordlen)
wordlen = 0
linefeed(-1) // -1 means explicit.
}
'\t' -> {
flush(wordpart, wordlen)
wordlen = 0
if (backend!!.getTabBehaviour() !== TabBehaviour.IGNORE)
{
val repl = backend!!.getTabBehaviour()
.getTabReplacement(cursorPosition!!.column)
for (j in 0 until repl!!.length())
{
backend!!.setCharacter(cursorPosition!!.withRelativeColumn(j), repl!!.charAt(j))
}
cursorPosition = cursorPosition!!.withRelativeColumn(repl!!.length())
}
else
{
linefeed(2)
putControlChar(ch)
}
}
'\u001b' -> if (isStyleable)
{
stash(wordpart, wordlen)
val seq = TerminalTextUtils.getANSIControlSequenceAt(string, i)
TerminalTextUtils.updateModifiersFromCSICode(seq, this, originalStyle)
backend!!.setStyleFrom(this)
i += seq!!.length() - 1
}
else
{
flush(wordpart, wordlen)
wordlen = 0
linefeed(2)
putControlChar(ch)
}
else -> if (Character.isISOControl(ch))
{
flush(wordpart, wordlen)
wordlen = 0
linefeed(1)
putControlChar(ch)
}
else if (Character.isWhitespace(ch))
{
flush(wordpart, wordlen)
wordlen = 0
backend!!.setCharacter(cursorPosition, ch)
cursorPosition = cursorPosition!!.withRelativeColumn(1)
}
else if (TerminalTextUtils.isCharCJK(ch))
{
flush(wordpart, wordlen)
wordlen = 0
linefeed(2)
backend!!.setCharacter(cursorPosition, ch)
cursorPosition = cursorPosition!!.withRelativeColumn(2)
}
else
{
if (wrapBehaviour!!.keepWords())
{
 // TODO: if at end of line despite starting at col 0, then split word.
                        wordpart.append(ch)
wordlen++
}
else
{
linefeed(1)
backend!!.setCharacter(cursorPosition, ch)
cursorPosition = cursorPosition!!.withRelativeColumn(1)
}
}
}
linefeed(wordlen)
i++
}
flush(wordpart, wordlen)
backend!!.setStyleFrom(originalStyle)
return this
}
private fun linefeed(lenToFit:Int) {
val curCol = cursorPosition!!.column
val spaceLeft = backend!!.getSize().getColumns() - curCol
if (wrapBehaviour!!.allowLineFeed())
{
val wantWrap = curCol > 0 && lenToFit > spaceLeft
if (lenToFit < 0 || (wantWrap && wrapBehaviour!!.autoWrap()))
{
 // TODO: clear to end of current line?
                cursorPosition = cursorPosition!!.withColumn(0)!!.withRelativeRow(1)
}
}
else
{
if (lenToFit < 0)
{ // encode explicit line feed
putControlChar('\n')
}
}
}
 fun putControlChar(ch:Char) {
val subst:Char
when (ch) {
'\u001b' -> subst = '['
'\u001c' -> subst = '\\'
'\u001d' -> subst = ']'
'\u001e' -> subst = '^'
'\u001f' -> subst = '_'
'\u007f' -> subst = '?'
else -> if (ch.toInt() <= 26)
{
subst = (ch + '@').toChar()
}
else
{ // normal character - or 0x80-0x9F
 // just write it out, anyway:
                backend!!.setCharacter(cursorPosition, ch)
cursorPosition = cursorPosition!!.withRelativeColumn(1)
return 
}
}
val style = activeModifiers
if (style!!.contains(SGR.REVERSE))
{
style!!.remove(SGR.REVERSE)
}
else
{
style!!.add(SGR.REVERSE)
}
var tc:TextCharacter? = TextCharacter('^', 
getForegroundColor(), getBackgroundColor(), style)
backend!!.setCharacter(cursorPosition, tc)
cursorPosition = cursorPosition!!.withRelativeColumn(1)
tc = tc!!.withCharacter(subst)
backend!!.setCharacter(cursorPosition, tc)
cursorPosition = cursorPosition!!.withRelativeColumn(1)
}
 // A word (a sequence of characters that is kept together when word-wrapping)
    // may consist of differently styled parts. This class describes one such
    // part.
    private class WordPart internal constructor(private val word:String?, private val wordlen:Int, style:StyleSet<*>?):StyleSet.Set() {

init{
setStyleFrom(style!!)
}
}

private fun stash(word:StringBuilder, wordlen:Int) {
if (word.length() > 0)
{
val chunk = WordPart(word.toString(), wordlen, this)
chunk_queue.add(chunk)
 // for convenience the StringBuilder is reset:
            word.setLength(0)
}
}
private fun flush(word:StringBuilder?, wordlen:Int) {
stash(word!!, wordlen)
if (chunk_queue.isEmpty())
{
return 
}
val row = cursorPosition!!.row
val col = cursorPosition!!.column
var offset = 0
for (chunk in chunk_queue)
{
backend!!.setStyleFrom(chunk)
backend!!.putString(col + offset, row, chunk!!.word)
offset = chunk!!.wordlen
}
chunk_queue.clear() // they're done.
 // set cursor right behind the word:
        cursorPosition = cursorPosition!!.withColumn(col + offset)
backend!!.setStyleFrom(this)
}

/**
 * @return the foreground color
 */
     fun getForegroundColor():TextColor? {
return foregroundColor
}

/**
 * @param foreground the foreground color to set
 */
     fun setForegroundColor(foreground:TextColor?):TextGraphicsWriter {
this.foregroundColor = foreground
return this
}

/**
 * @return the background color
 */
     fun getBackgroundColor():TextColor? {
return backgroundColor
}

/**
 * @param background the background color to set
 */
     fun setBackgroundColor(background:TextColor?):TextGraphicsWriter {
this.backgroundColor = background
return this
}

@Override
 fun enableModifiers(vararg modifiers:SGR?):TextGraphicsWriter {
style!!.addAll(Arrays.asList(modifiers))
return this
}

@Override
 fun disableModifiers(vararg modifiers:SGR?):TextGraphicsWriter {
style!!.removeAll(Arrays.asList(modifiers))
return this
}

@Override
 fun setModifiers(modifiers:EnumSet<SGR?>?):TextGraphicsWriter {
style!!.clear()
style!!.addAll(modifiers)
return this
}

@Override
 fun clearModifiers():TextGraphicsWriter {
style!!.clear()
return this
}

@Override
 fun setStyleFrom(source:StyleSet<*>):TextGraphicsWriter {
setBackgroundColor(source.getBackgroundColor())
setForegroundColor(source.getForegroundColor())
setModifiers(source.getActiveModifiers())
return this
}

/**
 * Translates a position into screen coordinates. If `null` is specified for position,
 * the cursor location is translated to screen coordinates.
 * 
 * @param pos position to translate, pass `null` for [.getCursorPosition].
 * @return screen (absolute) position.
 */
    @Override
 fun toScreenPosition(pos:TerminalPosition?):TerminalPosition? {
return backend!!.toScreenPosition(if (pos != null) pos else cursorPosition)
}
}
