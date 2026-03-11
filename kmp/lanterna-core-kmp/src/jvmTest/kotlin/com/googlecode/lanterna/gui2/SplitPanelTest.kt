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
import com.googlecode.lanterna.bundle.*
import com.googlecode.lanterna.graphics.*

/**
 * 
 * @author ginkoblongata
 */
 class SplitPanelTest:TestBase() {

fun init(textGUI:WindowBasedTextGUI) {
val window = BasicWindow("SplitPanelTest")
window.theme = LanternaThemes.getRegisteredTheme("businessmachine")

val left = makeImageComponent(IMAGE_X!!)
val right = makeImageComponent(IMAGE_Y!!)
 //SplitPanel splitH = SplitPanel.ofHorizontal(left.withBorder(Borders.singleLine("left")), right.withBorder(Borders.singleLine("right")));
        val splitH = SplitPanel.ofHorizontal(left, right)
splitH.setPreferredSize(TerminalSize(40, 40))
splitH.setRatio(45, 35)

val top = makeImageComponent(IMAGE_Y!!)
val bottom = makeImageComponent(IMAGE_Z!!)
 //SplitPanel splitV = SplitPanel.ofVertical(top.withBorder(Borders.singleLine("top")), bottom.withBorder(Borders.singleLine("bottom")));
        val splitV = SplitPanel.ofVertical(top, bottom)
splitV.setPreferredSize(TerminalSize(40, 40))
splitV.setRatio(20, 80)

val mainPanel = Panel()
mainPanel.setLayoutManager(GridLayout(2))
val splitboth = SplitPanel.ofHorizontal(splitH.withBorder(Borders.singleLine("horiontal split")), splitV.withBorder(Borders.singleLine("vertical split")))
mainPanel.addComponent(splitboth)

window.component = mainPanel
textGUI.addWindow(window)
}

internal fun makeImageComponent(image:Array<String>):ImageComponent {
val imageComponent = ImageComponent()
val imageSize = TerminalSize(image[0].length, image.size)
val textImage = BasicTextImage(imageSize)

for (row in image.indices)
{
fillImageLine(textImage, row, image[row])
}

imageComponent.setTextImage(textImage)
return imageComponent
}

internal fun fillImageLine(textImage:TextImage?, row:Int, line:String) {
for (x in 0 until line.length)
{
val c = line[x]
val textCharacter = TextCharacter(c)
textImage!!.setCharacterAt(x, row, textCharacter)
}
}

companion object {
@Throws(Exception::class)
 fun main(args:Array<String?>?) {
SplitPanelTest().run(args)
}

internal var IMAGE_X:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     XXXXXXX       XXXXXXX     xx", "xx     X:::::X       X:::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     X::::::X     X::::::X     xx", "xx     XXX:::::X   X:::::XXX     xx", "xx        X:::::X X:::::X        xx", "xx         X:::::X:::::X         xx", "xx          X:::::::::X          xx", "xx          X:::::::::X          xx", "xx         X:::::X:::::X         xx", "xx        X:::::X X:::::X        xx", "xx     XXX:::::X   X:::::XXX     xx", "xx     X::::::X     X::::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     XXXXXXX       XXXXXXX     xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")

internal var IMAGE_Y:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     YYYYYYY       YYYYYYY     xx", "xx     Y:::::Y       Y:::::Y     xx", "xx     Y:::::Y       Y:::::Y     xx", "xx     Y::::::Y     Y::::::Y     xx", "xx     YYY:::::Y   Y:::::YYY     xx", "xx        Y:::::Y Y:::::Y        xx", "xx         Y:::::Y:::::Y         xx", "xx          Y:::::::::Y          xx", "xx           Y:::::::Y           xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx         YYYY:::::YYYY         xx", "xx         Y:::::::::::Y         xx", "xx         YYYYYYYYYYYYY         xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")


internal var IMAGE_Z:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     ZZZZZZZZZZZZZZZZZZZ       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::ZZZZZZZZ:::::Z        xx", "xx     ZZZZZ     Z:::::Z         xx", "xx             Z:::::Z           xx", "xx            Z:::::Z            xx", "xx           Z:::::Z             xx", "xx          Z:::::Z              xx", "xx         Z:::::Z               xx", "xx        Z:::::Z                xx", "xx     ZZZ:::::Z     ZZZZZ       xx", "xx     Z::::::ZZZZZZZZ:::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     ZZZZZZZZZZZZZZZZZZZ       xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")
}
}
