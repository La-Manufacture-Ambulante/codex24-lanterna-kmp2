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
import java.io.IOException
import java.util.Collections

 class LinearLayoutTest:TestBase() {

fun init(textGUI:WindowBasedTextGUI) {
val window = BasicWindow("Linear layout test")
val mainPanel = Panel()
val labelPanel = Panel()
val linearLayout = LinearLayout(Direction.VERTICAL)
linearLayout.setSpacing(1)
labelPanel.setLayoutManager(linearLayout)

for (i in 0..4)
{
Label("LABEL COMPONENT")
.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.BEGINNING, LinearLayout.GrowPolicy.CAN_GROW))!!
.addTo(labelPanel)
}
mainPanel.addComponent(labelPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.BEGINNING, LinearLayout.GrowPolicy.CAN_GROW)))

Separator(Direction.HORIZONTAL)
.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL))!!
.addTo(mainPanel)

mainPanel.addComponent(Panels.horizontal(
Button("Add", { Label("LABEL COMPONENT").addTo(labelPanel) }), 
Button("Spacing", { linearLayout.setSpacing(if (linearLayout.getSpacing() == 1) 0 else 1) }), 
Button("Toggle Hide Odd #", { toggleVisibleOnOddNumberLabels(labelPanel) }), 
Button("Expand", { window.setHints(Collections.singletonList(Window.Hint.EXPANDED)) }), 
Button("Collapse", { window.setHints(Collections.emptySet()) }), 
Button("Close", Runnable { window.close() })
))

window.component = mainPanel
textGUI.addWindow(window)
}

internal fun toggleVisibleOnOddNumberLabels(panel:Panel) {
for (i in 0 until panel.childCount)
{
if ((i + 1) % 2 == 1)
{
val component = panel.childrenList?.get(i) ?: continue
component.setVisible(!component.isVisible)
}
}
}

companion object {
@Throws(InterruptedException::class, IOException::class)
 fun main(args:Array<String?>?) {
LinearLayoutTest().run(args)
}
}
}
