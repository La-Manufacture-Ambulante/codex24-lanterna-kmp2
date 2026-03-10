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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextImage
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen

import java.io.EOFException
import java.io.IOException
import java.util.Arrays
import java.util.EnumSet
import java.util.concurrent.atomic.AtomicBoolean

 object FullScreenTextGUITest {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
val screen = TestTerminalFactory(args).setInitialTerminalSize(TerminalSize(80, 25)).createScreen()
screen!!.startScreen()

val stop = AtomicBoolean(false)
val textGUI = MultiWindowTextGUI(screen)
textGUI.addListener({ textGUI1, key->
if (key!!.getKeyType() === KeyType.ESCAPE)
{
stop.set(true)
return@textGUI.addListener true
}
false })
try
{
textGUI.getBackgroundPane().setComponent(BIOS())
while (!stop.get())
{
if (!textGUI.getGUIThread().processEventsAndUpdate())
{
Thread.sleep(1)
}
}
}
catch (ignore:EOFException) {
 // Terminal closed
        }
finally
{
screen!!.stopScreen()
}
}

private class BIOS private constructor():Panel() {
private val background:TextImage?
private val helpLabel:Label?

init{
setLayoutManager(AbsoluteLayout())
background = createBackground()

helpLabel = Label("")
helpLabel!!.setForegroundColor(TextColor.ANSI.YELLOW)
helpLabel!!.setBackgroundColor(TextColor.ANSI.BLUE)
helpLabel!!.addStyle(SGR.BOLD)

val button1 = BIOSButton("Standard Lanterna Features", "Time, Date, Type...")
val button2 = BIOSButton("Advanced Lanterna Features", "Well, what could this possibly be?")
val button3 = BIOSButton("Advanced Terminal Features", "As you can see, I can change the description here")
val button4 = BIOSButton("Unintegrated Peripherals", "Joystick, VirtualBoy, Coffee Machines, ...")
val button5 = BIOSButton("Power Management Setup", "Terminal energy-saving mode?")
val button6 = BIOSButton("Non-PnP/ISA Configurations", "Going back to the '80s")
val button7 = BIOSButton("Terminal Health Status", "Monitor pixel consistency and feedback latency")
val button8 = BIOSButton("Frequency/Current Control", "To overclock your terminal; NOT covered by warranty!")
val button9 = BIOSButton("Load Fail-Safe Defaults", "Restore everything back")
val button10 = BIOSButton("Load Optimized Defaults", "And still you play the sycophant and revel in my pain")
val button11 = BIOSButton("Set Supervisor Password", "This is an outright fabrication")
val button12 = BIOSButton("Set User Password", "What would you even need this for?")
val button13 = BIOSButton("Save & Exit Setup", "...and then you can have some cake!")
val button14 = BIOSButton("Exit Without Saving", "僕の事が思い出せなくても泣かないでね")

button1.setSize(TerminalSize(35, 1))
button1.setPosition(TerminalPosition(3, 3))
button2.setSize(TerminalSize(35, 1))
button2.setPosition(TerminalPosition(3, 5))
button3.setSize(TerminalSize(35, 1))
button3.setPosition(TerminalPosition(3, 7))
button4.setSize(TerminalSize(35, 1))
button4.setPosition(TerminalPosition(3, 9))
button5.setSize(TerminalSize(35, 1))
button5.setPosition(TerminalPosition(3, 11))
button6.setSize(TerminalSize(35, 1))
button6.setPosition(TerminalPosition(3, 13))
button7.setSize(TerminalSize(35, 1))
button7.setPosition(TerminalPosition(3, 15))

button8.setSize(TerminalSize(35, 1))
button8.setPosition(TerminalPosition(43, 3))
button9.setSize(TerminalSize(35, 1))
button9.setPosition(TerminalPosition(43, 5))
button10.setSize(TerminalSize(35, 1))
button10.setPosition(TerminalPosition(43, 7))
button11.setSize(TerminalSize(35, 1))
button11.setPosition(TerminalPosition(43, 9))
button12.setSize(TerminalSize(35, 1))
button12.setPosition(TerminalPosition(43, 11))
button13.setSize(TerminalSize(35, 1))
button13.setPosition(TerminalPosition(43, 13))
button14.setSize(TerminalSize(35, 1))
button14.setPosition(TerminalPosition(43, 15))

helpLabel!!.setPosition(TerminalPosition(2, 22))
helpLabel!!.setSize(TerminalSize(76, 1))
addComponent(helpLabel)
for (button in Arrays.asList(button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13, button14))
{
addComponent(button)
}
addComponent(button14)
}

private fun createBackground():TextImage {
val image = BasicTextImage(80, 25)
val graphics = image.newTextGraphics()
graphics!!.setForegroundColor(TextColor.ANSI.WHITE)
graphics!!.setBackgroundColor(TextColor.ANSI.BLUE)
graphics!!.fill(' ')

graphics!!.enableModifiers(SGR.BOLD)

graphics!!.putString(7, 0, "Reminds you of some BIOS, doesn't it?")
graphics!!.setCharacter(0, 1, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER)
graphics!!.drawLine(1, 1, 78, 1, Symbols.DOUBLE_LINE_HORIZONTAL)
graphics!!.setCharacter(79, 1, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER)
graphics!!.drawLine(79, 2, 79, 23, Symbols.DOUBLE_LINE_VERTICAL)
graphics!!.setCharacter(79, 24, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER)
graphics!!.drawLine(1, 24, 78, 24, Symbols.DOUBLE_LINE_HORIZONTAL)
graphics!!.setCharacter(0, 24, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER)
graphics!!.drawLine(0, 2, 0, 23, Symbols.DOUBLE_LINE_VERTICAL)

graphics!!.setCharacter(0, 17, Symbols.DOUBLE_LINE_T_SINGLE_RIGHT)
graphics!!.drawLine(1, 17, 78, 17, Symbols.SINGLE_LINE_HORIZONTAL)
graphics!!.setCharacter(79, 17, Symbols.DOUBLE_LINE_T_SINGLE_LEFT)
graphics!!.setCharacter(40, 17, Symbols.SINGLE_LINE_T_UP)
graphics!!.drawLine(40, 2, 40, 16, Symbols.SINGLE_LINE_VERTICAL)
graphics!!.setCharacter(40, 1, Symbols.DOUBLE_LINE_T_SINGLE_DOWN)

graphics!!.setCharacter(0, 20, Symbols.DOUBLE_LINE_T_SINGLE_RIGHT)
graphics!!.drawLine(1, 20, 78, 20, Symbols.SINGLE_LINE_HORIZONTAL)
graphics!!.setCharacter(79, 20, Symbols.DOUBLE_LINE_T_SINGLE_LEFT)

graphics!!.putString(2, 18, "Esc : Quit")
graphics!!.putString(42, 18, (Symbols.ARROW_UP + " " + Symbols.ARROW_DOWN + " " + Symbols.ARROW_RIGHT + " " + 
Symbols.ARROW_LEFT + "   : Select Item"))
graphics!!.putString(2, 19, "F10 : Save & Exit Setup")
return image
}

@Override
protected fun createDefaultRenderer():ComponentRenderer<Panel?> {
val panelRenderer = super.createDefaultRenderer() as DefaultPanelRenderer

 // Turn off clearing the main area since we'll be using a custom renderer below to prepare the background
            panelRenderer!!.setFillAreaBeforeDrawingComponents(false)

return object:ComponentRenderer<Panel?>() {
@Override
 fun getPreferredSize(component:Panel?):TerminalSize {
return TerminalSize(80, 24)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:Panel?) {
 //Clear all data
                    graphics!!.setBackgroundColor(TextColor.ANSI.BLACK).fill(' ')

 //Draw the background image
                    graphics!!.drawImage(TerminalPosition.TOP_LEFT_CORNER, background)

 //Then draw all the child components
                    panelRenderer!!.drawComponent(graphics, this@BIOS)
}
}
}

private inner class BIOSButton(label:String?, private val description:String?):Button(label) {

init{
setRenderer(newRenderer())
}

@Override
protected fun afterEnterFocus(direction:FocusChangeDirection?, previouslyInFocus:Interactable?) {
helpLabel!!.setText(description)
}

private fun newRenderer():ButtonRenderer {
return object:ButtonRenderer() {
@Override
 fun getCursorLocation(component:Button?):TerminalPosition? {
return null
}

@Override
 fun getPreferredSize(component:Button?):TerminalSize {
return TerminalSize(TerminalTextUtils.getColumnWidth(getLabel()), 1)
}

@Override
 fun drawComponent(graphics:TextGUIGraphics?, component:Button?) {
graphics!!.setBackgroundColor(TextColor.ANSI.BLUE)
graphics!!.fill(' ')
if (isFocused())
{
graphics!!.setForegroundColor(TextColor.ANSI.WHITE)
graphics!!.setBackgroundColor(TextColor.ANSI.RED)
}
else
{
graphics!!.setForegroundColor(TextColor.ANSI.YELLOW)
graphics!!.setBackgroundColor(TextColor.ANSI.BLUE)
}
graphics!!.setModifiers(EnumSet.of(SGR.BOLD))
graphics!!.putString(0, 0, "  " + getLabel())
}
}
}
}
}
}
