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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.dialogs.DialogWindow
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder
import com.googlecode.lanterna.internal.compat.Pattern
import java.io.IOException
import java.util.Random

class DynamicGridLayoutTest : TestBase() {
    private val randomColor: TextColor
        get() = GOOD_COLORS[RANDOM.nextInt(GOOD_COLORS.size)]

    @Override
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Grid layout test")

        val mainPanel = Panel()
        mainPanel.setLayoutManager(LinearLayout(Direction.VERTICAL).setSpacing(1))

        val gridPanel = Panel()
        val gridLayout = newGridLayout(4)
        gridPanel.setLayoutManager(gridLayout)

        for (i in 0..15) {
            gridPanel.addComponent(EmptySpace(randomColor, TerminalSize(4, 1)))
        }

        val controlPanel = Panel()
        controlPanel.setLayoutManager(LinearLayout(Direction.HORIZONTAL))
        controlPanel.addComponent(Button("Add Component", { onAddComponent(textGUI, gridPanel) }))
        controlPanel.addComponent(Button("Modify Component", { onModifyComponent(textGUI, gridPanel) }))
        controlPanel.addComponent(Button("Modify Grid", { onModifyGrid(textGUI, gridPanel.getLayoutManager() as GridLayout) }))
        controlPanel.addComponent(Button("Reset Grid", { onResetGrid(textGUI, gridPanel) }))
        controlPanel.addComponent(Button("Exit", Runnable({ window.close() })))

        mainPanel.addComponent(gridPanel)
        mainPanel.addComponent(
            Separator(Direction.HORIZONTAL)
                .setLayoutData(
                    LinearLayout.createLayoutData(LinearLayout.Alignment.FILL),
                ),
        )
        mainPanel.addComponent(controlPanel)

        window.component = mainPanel
        textGUI.addWindow(window)
    }

    private fun onModifyGrid(
        textGUI: WindowBasedTextGUI,
        gridLayout: GridLayout?,
    ) {
        val gridLayoutEditor = GridLayoutEditor(gridLayout!!)
        gridLayoutEditor.showDialog(textGUI)
    }

    private fun onAddComponent(
        textGUI: WindowBasedTextGUI,
        gridPanel: Panel?,
    ) {
        val componentType =
            ListSelectDialog.showDialog(
                textGUI,
                "Add Component",
                "Select component to add",
                *SelectableComponentType.values(),
            )
        if (componentType == null) {
            return
        }
        var component: Component? = null
        when (componentType) {
            SelectableComponentType.Block, SelectableComponentType.TextBox -> {
                val sizeString =
                    TextInputDialogBuilder()
                        .setInitialContent(if (componentType == SelectableComponentType.Block) "4x1" else "16x1")
                        .setTitle("Add $componentType")
                        .setDescription("Enter size of " + componentType + " (<columns>x<rows>)")
                        .setValidationPattern(Pattern.compile("[0-9]+x[0-9]+"), "Invalid format, please use <columns>x<rows>")
                        .build()
                        .showDialog(textGUI)
                if (sizeString == null) {
                    return
                }
                val size = TerminalSize(Integer.parseInt(sizeString.split("x")[0]), Integer.parseInt(sizeString.split("x")[1]))
                component = if (componentType == SelectableComponentType.Block) EmptySpace(randomColor, size) else TextBox(size)
            }

            SelectableComponentType.Label -> {
                val text = TextInputDialog.showDialog(textGUI, "Add $componentType", "Enter the text of the new Label", "Label")
                component = Label(text ?: "")
            }
        }
        gridPanel?.addComponent(component)
    }

    private fun onModifyComponent(
        textGUI: WindowBasedTextGUI,
        panel: Panel,
    ) {
        val components = panel.children.toTypedArray()
        val component = ListSelectDialog.showDialog(textGUI, "Modify Component", "Select component to modify", 10, *components)
        if (component == null) {
            return
        }

        val gridLayoutDataEditor = GridLayoutDataEditor(component)
        gridLayoutDataEditor.showDialog(textGUI)
    }

    private fun onResetGrid(
        textGUI: WindowBasedTextGUI,
        gridPanel: Panel?,
    ) {
        val columns = TextInputDialog.showNumberDialog(textGUI, "Reset Grid", "Reset grid to how many columns?", "4")
        if (columns == null) {
            return
        }
        val prepopulate =
            TextInputDialog.showNumberDialog(
                textGUI,
                "Reset Grid",
                "Pre-populate grid with how many dummy components?",
                columns!!.toString(),
            )
        gridPanel?.removeAllComponents()
        gridPanel?.setLayoutManager(newGridLayout(columns.intValue()))

        for (i in 0 until (prepopulate?.intValue() ?: 0)) {
            gridPanel?.addComponent(EmptySpace(randomColor, TerminalSize(4, 1)))
        }
    }

    private fun newGridLayout(columns: Int): GridLayout {
        val gridLayout = GridLayout(columns)
        gridLayout.setTopMarginSize(1)
        gridLayout.setVerticalSpacing(1)
        gridLayout.setHorizontalSpacing(1)
        return gridLayout
    }

    private enum class SelectableComponentType {
        Block,
        Label,
        TextBox,
    }

    private class GridLayoutEditor(gridLayout: GridLayout) : DialogWindow("GridLayoutData Editor") {
        init {

            val numberPattern = Pattern.compile("[0-9]+")

            val contentPane = Panel()
            contentPane.setLayoutManager(GridLayout(2))
            contentPane.addComponent(Label("Horizontal spacing:"))
            val textBoxHorizontalSpacing = TextBox()
            textBoxHorizontalSpacing.setText(gridLayout.getHorizontalSpacing().toString())
            textBoxHorizontalSpacing.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxHorizontalSpacing)

            contentPane.addComponent(Label("Vertical spacing:"))
            val textBoxVerticalSpacing = TextBox()
            textBoxVerticalSpacing.setText(gridLayout.getVerticalSpacing().toString())
            textBoxVerticalSpacing.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxVerticalSpacing)

            contentPane.addComponent(Label("Left margin:"))
            val textBoxLeftMargin = TextBox()
            textBoxLeftMargin.setText(gridLayout.getLeftMarginSize().toString())
            textBoxLeftMargin.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxLeftMargin)

            contentPane.addComponent(Label("Right margin:"))
            val textBoxRightMargin = TextBox()
            textBoxRightMargin.setText(gridLayout.getRightMarginSize().toString())
            textBoxRightMargin.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxRightMargin)

            contentPane.addComponent(Label("Top margin:"))
            val textBoxTopMargin = TextBox()
            textBoxTopMargin.setText(gridLayout.getTopMarginSize().toString())
            textBoxTopMargin.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxTopMargin)

            contentPane.addComponent(Label("Bottom margin:"))
            val textBoxBottomMargin = TextBox()
            textBoxBottomMargin.setText(gridLayout.getBottomMarginSize().toString())
            textBoxBottomMargin.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxBottomMargin)

            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )
            contentPane.addComponent(
                Separator(Direction.HORIZONTAL).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )
            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )

            val okButton =
                Button("OK", {
                    gridLayout.setHorizontalSpacing(Integer.parseInt(textBoxHorizontalSpacing.getTextOrDefault("0")))
                    gridLayout.setVerticalSpacing(Integer.parseInt(textBoxVerticalSpacing.getTextOrDefault("0")))
                    gridLayout.setLeftMarginSize(Integer.parseInt(textBoxLeftMargin.getTextOrDefault("0")))
                    gridLayout.setRightMarginSize(Integer.parseInt(textBoxRightMargin.getTextOrDefault("0")))
                    gridLayout.setTopMarginSize(Integer.parseInt(textBoxTopMargin.getTextOrDefault("0")))
                    gridLayout.setBottomMarginSize(Integer.parseInt(textBoxBottomMargin.getTextOrDefault("0")))
                    close()
                })
            val cancelButton = Button("Cancel", Runnable({ this.close() }))

            contentPane.addComponent(
                Panels.horizontal(okButton, cancelButton)
                    .setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2)),
            )
            component = contentPane
        }
    }

    private class GridLayoutDataEditor(componentToEdit: Component) : DialogWindow("GridLayoutData Editor") {
        init {

            var gridLayoutData: GridLayout.GridLayoutData? = componentToEdit.layoutData as GridLayout.GridLayoutData
            if (gridLayoutData == null) {
                gridLayoutData =
                    GridLayout.createLayoutData(
                        GridLayout.Alignment.BEGINNING,
                        GridLayout.Alignment.BEGINNING,
                    ) as GridLayout.GridLayoutData
            }

            val contentPane = Panel()
            contentPane.setLayoutManager(GridLayout(2))
            contentPane.addComponent(Label("Horizontal alignment:"))
            val radioBoxesHorizontalAlignment = RadioBoxList<GridLayout.Alignment>()
            radioBoxesHorizontalAlignment.addItem(GridLayout.Alignment.BEGINNING)
            radioBoxesHorizontalAlignment.addItem(GridLayout.Alignment.CENTER)
            radioBoxesHorizontalAlignment.addItem(GridLayout.Alignment.END)
            radioBoxesHorizontalAlignment.addItem(GridLayout.Alignment.FILL)
            radioBoxesHorizontalAlignment.checkedItem = gridLayoutData!!.horizontalAlignment
            contentPane.addComponent(radioBoxesHorizontalAlignment)

            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )

            contentPane.addComponent(Label("Vertical alignment:"))
            val radioBoxesVerticalAlignment = RadioBoxList<GridLayout.Alignment>()
            radioBoxesVerticalAlignment.addItem(GridLayout.Alignment.BEGINNING)
            radioBoxesVerticalAlignment.addItem(GridLayout.Alignment.CENTER)
            radioBoxesVerticalAlignment.addItem(GridLayout.Alignment.END)
            radioBoxesVerticalAlignment.addItem(GridLayout.Alignment.FILL)
            radioBoxesVerticalAlignment.checkedItem = gridLayoutData!!.verticalAlignment
            contentPane.addComponent(radioBoxesVerticalAlignment)

            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )

            contentPane.addComponent(Label("Grab extra horizontal space:"))
            val checkBoxGrabExtraHorizontalSpace = CheckBox("")
            checkBoxGrabExtraHorizontalSpace.setChecked(gridLayoutData!!.grabExtraHorizontalSpace)
            contentPane.addComponent(checkBoxGrabExtraHorizontalSpace)

            contentPane.addComponent(Label("Grab extra vertical space:"))
            val checkBoxGrabExtraVerticalSpace = CheckBox("")
            checkBoxGrabExtraVerticalSpace.setChecked(gridLayoutData!!.grabExtraVerticalSpace)
            contentPane.addComponent(checkBoxGrabExtraVerticalSpace)

            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )

            val numberPattern = Pattern.compile("[1-9][0-9]*")

            contentPane.addComponent(Label("Horizontal span:"))
            val textBoxHorizontalSpan = TextBox(TerminalSize(5, 1), gridLayoutData!!.horizontalSpan.toString())
            textBoxHorizontalSpan.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxHorizontalSpan)

            contentPane.addComponent(Label("Vertical span:"))
            val textBoxVerticalSpan = TextBox(TerminalSize(5, 1), gridLayoutData!!.verticalSpan.toString())
            textBoxVerticalSpan.setValidationPattern(numberPattern)
            contentPane.addComponent(textBoxVerticalSpan)

            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )
            contentPane.addComponent(
                Separator(Direction.HORIZONTAL).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )
            contentPane.addComponent(
                EmptySpace(TerminalSize.ONE).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)),
            )

            val okButton =
                Button("OK", {
                    val horizontalAlignment =
                        radioBoxesHorizontalAlignment.checkedItem as? GridLayout.Alignment
                            ?: GridLayout.Alignment.BEGINNING
                    val verticalAlignment =
                        radioBoxesVerticalAlignment.checkedItem as? GridLayout.Alignment
                            ?: GridLayout.Alignment.BEGINNING
                    componentToEdit.setLayoutData(
                        GridLayout.createLayoutData(
                            horizontalAlignment,
                            verticalAlignment,
                            checkBoxGrabExtraHorizontalSpace.isChecked(),
                            checkBoxGrabExtraVerticalSpace.isChecked(),
                            Integer.parseInt(textBoxHorizontalSpan.getTextOrDefault("1")),
                            Integer.parseInt(textBoxVerticalSpan.getTextOrDefault("1")),
                        ),
                    )
                    close()
                })
            val cancelButton = Button("Cancel", Runnable({ this.close() }))

            contentPane.addComponent(
                Panels.horizontal(okButton, cancelButton)
                    .setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2)),
            )
            this.component = contentPane
        }
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            DynamicGridLayoutTest().run(args)
        }

        private val GOOD_COLORS =
            arrayOf(
                TextColor.ANSI.RED,
                TextColor.ANSI.BLUE,
                TextColor.ANSI.CYAN,
                TextColor.ANSI.GREEN,
                TextColor.ANSI.MAGENTA,
                TextColor.ANSI.YELLOW,
            )
        private val RANDOM = Random()
    }
}
