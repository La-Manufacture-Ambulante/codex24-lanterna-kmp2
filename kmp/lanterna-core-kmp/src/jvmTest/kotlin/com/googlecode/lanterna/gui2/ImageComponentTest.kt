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
import com.googlecode.lanterna.input.*

 class ImageComponentTest:TestBase() {

internal class ExampleController {
 var selectedImageComponent:ImageComponent? = null
 fun setSelectedImage(image:TextImage?) {
selectedImageComponent!!.setTextImage(image)
}
}

fun init(textGUI:WindowBasedTextGUI) {
val window = BasicWindow("ImageComponentTest")
window.setTheme(LanternaThemes.getRegisteredTheme("conqueror"))

val controller = ExampleController()
controller.selectedImageComponent = makeImageComponent(controller, IMAGE_BLANK!!)

val imageComponentX = makeImageComponent(controller, IMAGE_X!!)
val imageComponentY = makeImageComponent(controller, IMAGE_Y!!)
val imageComponentZ = makeImageComponent(controller, IMAGE_Z!!)



val mainPanel = Panel()
mainPanel.setLayoutManager(GridLayout(2))
mainPanel.addComponent(imageComponentX.withBorder(Borders.singleLine("x")))
mainPanel.addComponent(imageComponentY.withBorder(Borders.singleLine("y")))
mainPanel.addComponent(imageComponentZ.withBorder(Borders.singleLine("z")))
mainPanel.addComponent(controller.selectedImageComponent!!.withBorder(Borders.singleLine("selection")))


window.setComponent(mainPanel)
textGUI.addWindow(window)
}


internal fun makeImageComponent(controller:ExampleController?, image:Array<String>):ImageComponent {
val imageSize = TerminalSize(image[0].length, image.size)
val textImage = BasicTextImage(imageSize)
for (row in image.indices)
{
fillImageLine(textImage, row, image[row])
}

val imageComponent = object:ImageComponent() {
public override fun handleKeyStroke(keyStroke:KeyStroke):Interactable.Result {
if (isMouseDown(keyStroke))
{
controller!!.setSelectedImage(textImage)
return Interactable.Result.HANDLED
}
return super.handleKeyStroke(keyStroke) ?: Interactable.Result.UNHANDLED
}
}

imageComponent.setTextImage(textImage)
return imageComponent
}

internal fun fillImageLine(textImage:TextImage?, row:Int, line:String) {
for (x in 0 until line.length)
{
val c = line.charAt(x)
val textCharacter = TextCharacter(c)
textImage!!.setCharacterAt(x, row, textCharacter)
}
}

companion object {
@Throws(Exception::class)
 fun main(args:Array<String?>?) {
ImageComponentTest().run(args)
}

internal var IMAGE:Array<String>? = arrayOf("-====================================================-", "xx                                                  xx", "xx  X                                            X  xx", "xx                                                  xx", "xx    .d8b.  d8888b.  .o88b.                        xx", "xx   d8' `8b 88  `8D d8P  Y8                        xx", "xx   88ooo88 88oooY' 8P            asdfasdf         xx", "xx   88~~~88 88~~~b. 8b                             xx", "xx   88   88 88   8D Y8b  d8              1234      xx", "xx   YP   YP Y8888P'  `Y88P'                        xx", "xx                                 asdfasdf         xx", "xx                                                  xx", "xx   db    db db    db d88888D                      xx", "xx   `8b  d8' `8b  d8' YP  d8'                      xx", "xx    `8bd8'   `8bd8'     d8'          xxxxxxx      xx", "xx    .dPYb.     88      d8'           x     x      xx", "xx   .8P  Y8.    88     d8' db         x     x      xx", "xx   YP    YP    YP    d88888P         x     x      xx", "xx                                     xxxxxxx      xx", "xx  X                                            X  xx", "xx                                                  xx", "-====================================================-")

internal var IMAGE_BLANK:Array<String>? = arrayOf("-=================================-", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "x                                 x", "-=================================-")

internal var IMAGE_X:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     XXXXXXX       XXXXXXX     xx", "xx     X:::::X       X:::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     X::::::X     X::::::X     xx", "xx     XXX:::::X   X:::::XXX     xx", "xx        X:::::X X:::::X        xx", "xx         X:::::X:::::X         xx", "xx          X:::::::::X          xx", "xx          X:::::::::X          xx", "xx         X:::::X:::::X         xx", "xx        X:::::X X:::::X        xx", "xx     XXX:::::X   X:::::XXX     xx", "xx     X::::::X     X::::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     X:::::X       X:::::X     xx", "xx     XXXXXXX       XXXXXXX     xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")

internal var IMAGE_Y:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     YYYYYYY       YYYYYYY     xx", "xx     Y:::::Y       Y:::::Y     xx", "xx     Y:::::Y       Y:::::Y     xx", "xx     Y::::::Y     Y::::::Y     xx", "xx     YYY:::::Y   Y:::::YYY     xx", "xx        Y:::::Y Y:::::Y        xx", "xx         Y:::::Y:::::Y         xx", "xx          Y:::::::::Y          xx", "xx           Y:::::::Y           xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx            Y:::::Y            xx", "xx         YYYY:::::YYYY         xx", "xx         Y:::::::::::Y         xx", "xx         YYYYYYYYYYYYY         xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")


internal var IMAGE_Z:Array<String>? = arrayOf("-=================================-", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "xx     ZZZZZZZZZZZZZZZZZZZ       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::ZZZZZZZZ:::::Z        xx", "xx     ZZZZZ     Z:::::Z         xx", "xx             Z:::::Z           xx", "xx            Z:::::Z            xx", "xx           Z:::::Z             xx", "xx          Z:::::Z              xx", "xx         Z:::::Z               xx", "xx        Z:::::Z                xx", "xx     ZZZ:::::Z     ZZZZZ       xx", "xx     Z::::::ZZZZZZZZ:::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     Z:::::::::::::::::Z       xx", "xx     ZZZZZZZZZZZZZZZZZZZ       xx", "xx                               xx", "xx  X                         X  xx", "xx                               xx", "-=================================-")
}
}
