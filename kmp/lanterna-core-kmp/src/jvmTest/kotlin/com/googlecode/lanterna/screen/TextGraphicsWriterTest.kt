package com.googlecode.lanterna.screen

import com.googlecode.lanterna.*
import java.io.IOException

import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextGraphicsWriter

 object TextGraphicsWriterTest {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val screen = TestTerminalFactory(args).createScreen()
screen.startScreen()

val writer = ScreenTextGraphics(screen)
writer.setForegroundColor(TextColor.ANSI.WHITE)
writer.setBackgroundColor(TextColor.ANSI.BLUE)
writer.fill(' ')
val tw = TextGraphicsWriter(writer)

var loremIpsum:String? = ("  Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod" + 
" tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim" + 
" veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid" + 
" ex ea commodi consequat. Quis aute iure reprehenderit in voluptate" + 
" velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint" + 
" obcaecat cupiditat non proident, sunt in culpa qui officia deserunt" + 
" mollit anim id est laborum.\n")
 // change all blanks behind full-stops or commas to underlined tabs:
        loremIpsum = loremIpsum!!.replace(Regex("([.,]) "), "$1\u001b[4m\t\u001b[24m")
 // each occurrence of "dolor" gets its own background:
        loremIpsum = loremIpsum!!.replace(Regex("(dolor)"), "\u001b[45m$1\u001b[49m")
 // each 'o' is turned yellow.
        loremIpsum = loremIpsum!!.replace(Regex("([o])"), "\u001b[1;33m$1\u001b[22;39m")

tw.putString("\u001b[m")
tw.wrapBehaviour = WrapBehaviour.SINGLE_LINE
writer.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4)
tw.putString("\n" + tw.wrapBehaviour + ":\n")
tw.putString(loremIpsum!!)

tw.wrapBehaviour = WrapBehaviour.CLIP
tw.putString("\n" + tw.wrapBehaviour + ":\n")
tw.putString(loremIpsum!!)

tw.wrapBehaviour = WrapBehaviour.CHAR
tw.putString("\n" + tw.wrapBehaviour + ":\n")
tw.putString(loremIpsum!!)

tw.wrapBehaviour = WrapBehaviour.WORD
tw.putString("\n" + tw.wrapBehaviour + ":\n")
tw.putString(loremIpsum!!)

tw.wrapBehaviour = WrapBehaviour.CLIP
writer.setTabBehaviour(TabBehaviour.IGNORE)
tw.putString("\n" + tw.wrapBehaviour + " + TabBehaviour.IGNORE:\n")
tw.putString(loremIpsum!!)

tw.putString("\u001b[m")
tw.isStyleable = false
tw.putString(tw.wrapBehaviour.toString() + " + Styleable turned off, so esc-sequences are visible:\n")
tw.putString(loremIpsum!!)

screen.refresh()
screen.readInput()
screen.stopScreen()
}
}
