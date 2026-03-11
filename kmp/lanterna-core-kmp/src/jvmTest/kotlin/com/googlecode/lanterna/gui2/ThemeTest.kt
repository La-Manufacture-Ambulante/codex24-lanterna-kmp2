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
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import java.io.IOException
import java.util.ArrayList
import java.util.Collections

class ThemeTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val mainSelectionWindow = BasicWindow("Theme Tests")
        val mainSelector = ActionListBox()
        mainSelector.addItem("Component test", { runComponentTest(textGUI) })
        mainSelector.addItem("Multi-theme test", { runMultiThemeTest(textGUI) })
        mainSelector.addItem("Make custom theme", { runCustomTheme(textGUI) })
        mainSelector.addItem("Exit", Runnable { mainSelectionWindow.close() })
        mainSelectionWindow.component = mainSelector
        mainSelectionWindow.setHints(Collections.singletonList(Window.Hint.CENTERED))

        textGUI.addWindow(mainSelectionWindow)
    }

    private fun runComponentTest(textGUI: WindowBasedTextGUI?) {
        val componentTestChooser = BasicWindow("Component test")
        componentTestChooser.setHints(Collections.singletonList(Window.Hint.CENTERED))

        val mainPanel = Panel()
        mainPanel.addComponent(Label("Choose component:                     "))
        mainPanel.addComponent(EmptySpace())
        val componentTestDialogs =
            arrayOf<ThemedComponentTestDialog?>(
                ThemedComponentTestDialog(
                    textGUI, "ActionListBox",
                    ActionListBox(TerminalSize(15, 5))
                        .addItem(NullRunnable("Item #1"))!!
                        .addItem(NullRunnable("Item #2"))!!
                        .addItem(NullRunnable("Item #3"))!!
                        .addItem(NullRunnable("Item #4"))!!
                        .addItem(NullRunnable("Item #5"))!!
                        .addItem(NullRunnable("Item #6"))!!
                        .addItem(NullRunnable("Item #7"))!!
                        .addItem(NullRunnable("Item #8"))!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "AnimatedLabel",
                    AnimatedLabel("First Frame")
                        .addFrame("Second Frame")
                        .addFrame("Third Frame")
                        .addFrame("Last Frame"),
                ),
                ThemedComponentTestDialog(
                    textGUI, "Borders",
                    Panel()
                        .setLayoutManager(GridLayout(4))
                        .addComponent(EmptySpace(TerminalSize(4, 2)).withBorder(Borders.singleLine()))!!
                        .addComponent(EmptySpace(TerminalSize(4, 2)).withBorder(Borders.singleLineBevel()))!!
                        .addComponent(EmptySpace(TerminalSize(4, 2)).withBorder(Borders.doubleLine()))!!
                        .addComponent(EmptySpace(TerminalSize(4, 2)).withBorder(Borders.doubleLineBevel()))!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "Button",
                    Button("This is a button"),
                ),
                ThemedComponentTestDialog(
                    textGUI, "CheckBox",
                    CheckBox("This is a checkbox"),
                ),
                ThemedComponentTestDialog(
                    textGUI, "CheckBoxList",
                    CheckBoxList<String?>(TerminalSize(15, 5))
                        .addItem("Item #1")!!
                        .addItem("Item #2")!!
                        .addItem("Item #3")!!
                        .addItem("Item #4")!!
                        .addItem("Item #5")!!
                        .addItem("Item #6")!!
                        .addItem("Item #7")!!
                        .addItem("Item #8")!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "ComboBox",
                    Panel()
                        .addComponent(
                            ComboBox<Any?>("Editable", "Item #2", "Item #3", "Item #4", "Item #5", "Item #6", "Item #7")
                                .setReadOnly(false)
                                .setPreferredSize(TerminalSize(12, 1)),
                        )!!
                        .addComponent(EmptySpace())!!
                        .addComponent(
                            ComboBox<Any?>("Read-only", "Item #2", "Item #3", "Item #4", "Item #5", "Item #6", "Item #7")
                                .setReadOnly(true)
                                .setPreferredSize(TerminalSize(12, 1)),
                        )!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "Label",
                    Label("This is a label"),
                ),
                ThemedComponentTestDialog(
                    textGUI, "RadioBoxList",
                    RadioBoxList<String?>(TerminalSize(15, 5))
                        .addItem("Item #1")!!
                        .addItem("Item #2")!!
                        .addItem("Item #3")!!
                        .addItem("Item #4")!!
                        .addItem("Item #5")!!
                        .addItem("Item #6")!!
                        .addItem("Item #7")!!
                        .addItem("Item #8")!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "ProgressBar",
                    ProgressBar(0, 100, 24)
                        .setLabelFormat("%2.0f%%")
                        .setValue(26),
                ),
                ThemedComponentTestDialog(
                    textGUI, "ScrollBar",
                    Panel()
                        .setLayoutManager(GridLayout(2))
                        .addComponent(ScrollBar(Direction.HORIZONTAL).setPreferredSize(TerminalSize(6, 1)))!!
                        .addComponent(ScrollBar(Direction.VERTICAL).setPreferredSize(TerminalSize(1, 6)))!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "Separator",
                    Panel()
                        .setLayoutManager(GridLayout(2))
                        .addComponent(Separator(Direction.HORIZONTAL).setPreferredSize(TerminalSize(6, 1)))!!
                        .addComponent(Separator(Direction.VERTICAL).setPreferredSize(TerminalSize(1, 6)))!!,
                ),
                ThemedComponentTestDialog(
                    textGUI, "Table",
                    Table<String?>("Column #1", "Column #2", "Column #3")
                        .setTableModel(
                            TableModel<String?>("Column #1", "Column #2", "Column #3")
                                .addRow("Row #1", "Row #1", "Row #1")
                                .addRow("Row #2", "Row #2", "Row #2")
                                .addRow("Row #3", "Row #3", "Row #3")
                                .addRow("Row #4", "Row #4", "Row #4"),
                        ),
                ),
                ThemedComponentTestDialog(
                    textGUI, "TextBox",
                    Panel()
                        .addComponent(
                            Panels.horizontal(
                                TextBox("Single-line text box")
                                    .setPreferredSize(TerminalSize(15, 1)),
                                TextBox("Single-line read-only")
                                    .setPreferredSize(TerminalSize(15, 1))!!
                                    .setReadOnly(true),
                            ),
                        )!!
                        .addComponent(EmptySpace())!!
                        .addComponent(
                            Panels.horizontal(
                                TextBox(TerminalSize(15, 5), "Multi\nline\ntext\nbox\nHere is a very long line that doesn't fit")
                                    .setVerticalFocusSwitching(false),
                                TextBox(TerminalSize(15, 5), ("Multi\nline\nread-only\ntext\nbox\n" + "Here is a very long line that doesn't fit"))
                                    .setReadOnly(true),
                            ),
                        )!!,
                ),
            )
        val listBox = ActionListBox(TerminalSize(15, 7)).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.CENTER))
        for (themedComponentTestDialog in componentTestDialogs) {
            listBox!!.addItem(themedComponentTestDialog)
        }
        mainPanel.addComponent(listBox)
        mainPanel.addComponent(EmptySpace())
        mainPanel.addComponent(
            Button(
                LocalizedString.Close!!.toString(),
                Runnable {
                    componentTestChooser.close()
                },
            ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.END)),
        )

        componentTestChooser.component = mainPanel
        textGUI!!.addWindowAndWait(componentTestChooser)
    }

    private class ThemedComponentTestDialog(private val textGUI: WindowBasedTextGUI?, private val label: String?, private val embeddedComponent: Component?) : Runnable {
        private val borderedComponent: Component?

        init {

            val componentPanel = Panel()
            componentPanel.setLayoutManager(
                GridLayout(1)
                    .setBottomMarginSize(1)
                    .setTopMarginSize(1)
                    .setLeftMarginSize(2)
                    .setRightMarginSize(2),
            )
            componentPanel.addComponent(embeddedComponent!!.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER)))
            this.borderedComponent = componentPanel.withBorder(Borders.singleLine(label ?: ""))

            if (embeddedComponent is AnimatedLabel) {
                (embeddedComponent as AnimatedLabel).startAnimation(917)
            } else if (embeddedComponent is ProgressBar) {
                val progressBarAdvanceTimer =
                    Thread({
                        val progressBar = embeddedComponent as ProgressBar?
                        while (true) {
                            try {
                                Thread.sleep(100)
                                if (progressBar!!.getValue() == progressBar!!.getMax()) {
                                    Thread.sleep(1000)
                                    progressBar!!.setValue(0)
                                }
                                if (progressBar!!.getValue() == 0) {
                                    Thread.sleep(1000)
                                }
                                progressBar!!.setValue(progressBar!!.getValue() + 1)
                            } catch (e: InterruptedException) {
                                throw RuntimeException(e)
                            }
                        }
                    }, "ProgressBar #" + System.identityHashCode(embeddedComponent))
                progressBarAdvanceTimer.setDaemon(true)
                progressBarAdvanceTimer.start()
            }
        }

        override fun run() {
            val componentWindow = BasicWindow()
            componentWindow.setHints(Collections.singletonList(Window.Hint.CENTERED))
            componentWindow.title = "Themed Component"

            val mainPanel = Panel()
            mainPanel.setLayoutManager(GridLayout(2))
            mainPanel.addComponent(borderedComponent!!.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)))

            val actionListBox = ActionListBox()
            for (themeName in LanternaThemes.registeredThemes as Collection<String>) {
                actionListBox.addItem(themeName, { borderedComponent!!.setTheme(LanternaThemes.getRegisteredTheme(themeName)) })
            }
            val actionsBorder =
                actionListBox
                    .withBorder(Borders.doubleLine("Change theme:"))!!
            actionsBorder.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER))
            mainPanel.addComponent(actionsBorder)

            val closeButton = Button(LocalizedString.Close!!.toString(), Runnable { componentWindow.close() })
            closeButton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.END))
            mainPanel.addComponent(closeButton)

            componentWindow.component = mainPanel
            closeButton.takeFocus()
            textGUI!!.addWindowAndWait(componentWindow)
        }

        override fun toString(): String {
            return label ?: ""
        }
    }

    private class NullRunnable(private val label: String?) : Runnable {
        override fun run() {}

        override fun toString(): String {
            return label ?: ""
        }
    }

    private fun runMultiThemeTest(textGUI: WindowBasedTextGUI) {
        @Suppress("UNCHECKED_CAST")
        val themes = ArrayList(LanternaThemes.registeredThemes as Collection<String>)
        val windowThemeIndex = intArrayOf(themes.indexOf("bigsnake"), themes.indexOf("conqueror"))
        val window1 = BasicWindow("Theme: bigsnake")
        window1.setHints(Collections.singletonList(Window.Hint.FIXED_POSITION))
        window1.theme = LanternaThemes.getRegisteredTheme(themes.get(windowThemeIndex[0]))
        window1.position = TerminalPosition(2, 1)

        val window2 = BasicWindow("Theme: conqueror")
        window2.setHints(Collections.singletonList(Window.Hint.FIXED_POSITION))
        window2.theme = LanternaThemes.getRegisteredTheme(themes.get(windowThemeIndex[1]))
        window2.position = TerminalPosition(30, 1)

        val leftHolder = Panel().setPreferredSize(TerminalSize(15, 4))
        val rightHolder = Panel().setPreferredSize(TerminalSize(15, 4))
        val layoutManager = GridLayout(1)
        leftHolder!!.setLayoutManager(layoutManager)
        rightHolder!!.setLayoutManager(layoutManager)

        val exampleButton = Button("Example")
        exampleButton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, true))
        exampleButton.setEnabled(false)
        leftHolder!!.addComponent(exampleButton)

        val leftWindowActionBox =
            ActionListBox()
                .addItem("Move button to right", { rightHolder!!.addComponent(exampleButton) })!!
                .addItem("Override button theme", {
                    val actionListDialogBuilder = ActionListDialogBuilder()
                    actionListDialogBuilder.setTitle("Choose theme for the button")
                    for (theme in themes) {
                        actionListDialogBuilder.addAction(theme, { exampleButton.setTheme(LanternaThemes.getRegisteredTheme(theme)) })
                    }
                    actionListDialogBuilder.addAction("Clear override", { exampleButton.setTheme(null) })
                    actionListDialogBuilder.build()!!.showDialog(textGUI)
                })!!
                .addItem("Cycle window theme", {
                    windowThemeIndex[0]++
                    if (windowThemeIndex[0] >= themes.size) {
                        windowThemeIndex[0] = 0
                    }
                    val themeName = themes.get(windowThemeIndex[0])
                    window1.theme = LanternaThemes.getRegisteredTheme(themeName)
                    window1.title = "Theme: " + themeName!!
                })!!
                .addItem("Switch active window", { textGUI.setActiveWindow(window2) })!!
                .addItem("Exit", {
                    window1.close()
                    window2.close()
                })
        window1.component =
            Panels.vertical(
                leftHolder!!.withBorder(Borders.singleLine()),
                leftWindowActionBox,
            )

        val rightWindowActionBox =
            ActionListBox()
                .addItem("Move button to left", { leftHolder!!.addComponent(exampleButton) })!!
                .addItem("Override button theme", {
                    val actionListDialogBuilder = ActionListDialogBuilder()
                    actionListDialogBuilder.setTitle("Choose theme for the button")
                    for (theme in themes) {
                        actionListDialogBuilder.addAction(theme, { exampleButton.setTheme(LanternaThemes.getRegisteredTheme(theme)) })
                    }
                    actionListDialogBuilder.addAction("Clear override", { exampleButton.setTheme(null) })
                    actionListDialogBuilder.build()!!.showDialog(textGUI)
                })!!
                .addItem("Cycle window theme", {
                    windowThemeIndex[1]++
                    if (windowThemeIndex[1] >= themes.size) {
                        windowThemeIndex[1] = 0
                    }
                    val themeName = themes.get(windowThemeIndex[1])
                    window2.theme = LanternaThemes.getRegisteredTheme(themeName)
                    window2.title = "Theme: " + themeName!!
                })!!
                .addItem("Switch active window", { textGUI.setActiveWindow(window1) })!!
                .addItem("Exit", {
                    window1.close()
                    window2.close()
                })
        window2.component =
            Panels.vertical(
                rightHolder!!.withBorder(Borders.singleLine()),
                rightWindowActionBox,
            )

        window1.focusedInteractable = leftWindowActionBox
        window2.focusedInteractable = rightWindowActionBox

        textGUI.addWindow(window1)
        textGUI.addWindow(window2)
        textGUI.setActiveWindow(window1)
    }

    private fun runCustomTheme(textGUI: WindowBasedTextGUI) {
        val customThemeCreator = BasicWindow("Custom Theme")
        customThemeCreator.setHints(Collections.singletonList(Window.Hint.CENTERED))

        val mainPanel = Panel()
        mainPanel.addComponent(Label("Choose colors:"))

        val colorTable = Panel(GridLayout(2))
        colorTable.addComponent(Label("Base foreground:"))
        val baseForeground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        colorTable.addComponent(baseForeground)
        colorTable.addComponent(Label("Base background:"))
        val baseBackground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        baseBackground.setSelectedIndex(7)
        colorTable.addComponent(baseBackground)
        colorTable.addComponent(Label("Editable foreground:"))
        val editableForeground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        editableForeground.setSelectedIndex(7)
        colorTable.addComponent(editableForeground)
        colorTable.addComponent(Label("Editable background:"))
        val editableBackground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        editableBackground.setSelectedIndex(4)
        colorTable.addComponent(editableBackground)
        colorTable.addComponent(Label("Selected foreground:"))
        val selectedForeground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        selectedForeground.setSelectedIndex(7)
        colorTable.addComponent(selectedForeground)
        colorTable.addComponent(Label("Selected background:"))
        val selectedBackground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        selectedBackground.setSelectedIndex(4)
        colorTable.addComponent(selectedBackground)
        colorTable.addComponent(Label("GUI background:"))
        val guiBackground = ComboBox<TextColor.ANSI?>(*TextColor.ANSI.values())
        guiBackground.setSelectedIndex(4)
        colorTable.addComponent(guiBackground)
        val activeIsBoxCheck = CheckBox("Active content is bold").setChecked(true)

        mainPanel.addComponent(EmptySpace())
        mainPanel.addComponent(colorTable)
        mainPanel.addComponent(activeIsBoxCheck)
        mainPanel.addComponent(EmptySpace())

        val okButton =
            Button(LocalizedString.OK!!.toString(), {
                val theme =
                    SimpleTheme.makeTheme(
                        activeIsBoxCheck.isChecked(),
                        baseForeground.getSelectedItem(),
                        baseBackground.getSelectedItem(),
                        editableForeground.getSelectedItem(),
                        editableBackground.getSelectedItem(),
                        selectedForeground.getSelectedItem(),
                        selectedBackground.getSelectedItem(),
                        guiBackground.getSelectedItem(),
                    )
                textGUI.theme = theme
                customThemeCreator.close()
            })
        val cancelButton = Button(LocalizedString.Cancel!!.toString(), Runnable { customThemeCreator.close() })
        mainPanel.addComponent(
            Panels.horizontal(
                okButton,
                cancelButton,
            ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.END)),
        )

        customThemeCreator.component = mainPanel
        okButton.takeFocus()
        textGUI.addWindowAndWait(customThemeCreator)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            ThemeTest().run(args)
        }
    }
}
