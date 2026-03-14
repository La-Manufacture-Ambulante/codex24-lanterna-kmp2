package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.CheckBox
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.gui2.Runnable
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.TextGUI
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import com.googlecode.lanterna.gui2.menu.Menu
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.gui2.menu.MenuItem
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

private const val DEFAULT_TITLE = "Lanterna KMP"
private const val DEFAULT_NOTES = "Tab through widgets.\nEnter activates buttons.\nEsc closes the demo."
private const val DEFAULT_THEME = "Ocean"

internal data class InteractiveWidgetDemoState(
    val title: String = DEFAULT_TITLE,
    val notes: String = DEFAULT_NOTES,
    val notificationsEnabled: Boolean = true,
    val theme: String = DEFAULT_THEME,
    val clicks: Int = 0,
    val lastAction: String = "Ready",
)

internal fun renderInteractiveWidgetSummary(state: InteractiveWidgetDemoState): String =
    buildString {
        appendLine("Input: ${state.title.ifBlank { "<empty>" }}")
        appendLine("Theme: ${state.theme}")
        appendLine("Notifications: ${if (state.notificationsEnabled) "enabled" else "muted"}")
        appendLine("Primary clicks: ${state.clicks}")
        append("Notes lines: ${countLines(state.notes)}")
    }

internal fun renderInteractiveWidgetMetrics(state: InteractiveWidgetDemoState): String =
    "Theme ${state.theme} | Dialog ${if (state.notificationsEnabled) "on" else "off"} | Clicks ${state.clicks} | Notes ${countLines(state.notes)}"

fun createInteractiveWidgetDemoWindow(
    textGUI: WindowBasedTextGUI,
    onExit: () -> Unit = {},
): BasicWindow {
    var state = InteractiveWidgetDemoState()
    var syncingView = false

    val statusLabel = Label("Status: ${state.lastAction}")
    val metricsLabel = Label(renderInteractiveWidgetMetrics(state)).setLabelWidth(64)
    val summaryLabel = Label(renderInteractiveWidgetSummary(state)).setLabelWidth(22)
    val inputBox = TextBox(TerminalSize(24, 1), state.title)
    val notesBox = TextBox(TerminalSize(24, 3), state.notes, TextBox.Style.MULTI_LINE).setCaretWarp(true)
    val notificationsBox = CheckBox("Show dialog on run").setChecked(state.notificationsEnabled)
    val themeList = RadioBoxList<String>(TerminalSize(18, 3))
    val actionList = ActionListBox(TerminalSize(22, 3))
    val window = BasicWindow("Lanterna KMP Interactive Widget Demo")

    themeList.addItem("Ocean")
    themeList.addItem("Amber")
    themeList.addItem("Graphite")
    themeList.checkedItem = state.theme

    fun syncState(message: String? = null) {
        if (message != null) {
            state = state.copy(lastAction = message)
        }
        statusLabel.setText("Status: ${state.lastAction}")
        metricsLabel.setText(renderInteractiveWidgetMetrics(state))
        summaryLabel.setText(renderInteractiveWidgetSummary(state))
    }

    fun applyReset(message: String) {
        syncingView = true
        try {
            state = InteractiveWidgetDemoState(lastAction = message)
            inputBox.setText(state.title)
            notesBox.setText(state.notes)
            notificationsBox.setChecked(state.notificationsEnabled)
            themeList.checkedItem = state.theme
        } finally {
            syncingView = false
        }
        syncState()
    }

    fun showPrimaryActionResult() {
        state = state.copy(clicks = state.clicks + 1)
        syncState("Primary action ran (${state.clicks})")
        if (state.notificationsEnabled) {
            MessageDialog.showMessageDialog(
                textGUI,
                "Primary action",
                "Input: ${state.title.ifBlank { "<empty>" }}\nTheme: ${state.theme}\nClicks: ${state.clicks}",
                MessageDialogButton.OK,
            )
        }
    }

    fun showAboutDialog() {
        MessageDialog.showMessageDialog(
            textGUI,
            "Interactive widget demo",
            "Ported into the new KMP repo using gui2 widgets already available on this branch.",
            MessageDialogButton.OK,
        )
    }

    inputBox.setTextChangeListener(
        object : TextBox.TextChangeListener {
            override fun onTextChanged(
                newText: String,
                changedByUserInteraction: Boolean,
            ) {
                state = state.copy(title = newText)
                if (!syncingView) {
                    syncState(
                        if (changedByUserInteraction) {
                            "Edited title (${newText.length} chars)"
                        } else {
                            "Updated title"
                        },
                    )
                }
            }
        },
    )

    notesBox.setTextChangeListener(
        object : TextBox.TextChangeListener {
            override fun onTextChanged(
                newText: String,
                changedByUserInteraction: Boolean,
            ) {
                state = state.copy(notes = newText)
                if (!syncingView) {
                    syncState(
                        if (changedByUserInteraction) {
                            "Edited notes (${countLines(newText)} lines)"
                        } else {
                            "Updated notes"
                        },
                    )
                }
            }
        },
    )

    notificationsBox.addListener(
        object : CheckBox.Listener {
            override fun onStatusChanged(checked: Boolean) {
                state = state.copy(notificationsEnabled = checked)
                if (!syncingView) {
                    syncState("Notifications ${if (checked) "enabled" else "muted"}")
                }
            }
        },
    )

    themeList.addListener(
        object : RadioBoxList.Listener {
            override fun onSelectionChanged(
                selectedIndex: Int,
                previousSelection: Int,
            ) {
                val checkedTheme = themeList.checkedItem ?: return
                state = state.copy(theme = checkedTheme)
                if (!syncingView) {
                    syncState("Theme switched to $checkedTheme")
                }
            }
        },
    )

    actionList.addItem("Run primary action", Runnable { showPrimaryActionResult() })
    actionList.addItem("Reset demo", Runnable { applyReset("Reset demo") })
    actionList.addItem("About this port", Runnable { showAboutDialog() })

    val leftPanelContent = Panel(GridLayout(1).setVerticalSpacing(0))
    leftPanelContent.addComponent(Label("Title"))
    leftPanelContent.addComponent(inputBox, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(Label("Theme"))
    leftPanelContent.addComponent(themeList, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(notificationsBox, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(Label("Notes"))
    leftPanelContent.addComponent(notesBox, GridLayout.createHorizontallyFilledLayoutData())
    val leftPanel = leftPanelContent.withBorder(Borders.singleLine("Inputs"))

    val primaryButton = Button("Run", Runnable { showPrimaryActionResult() })
    val resetButton = Button("Reset", Runnable { applyReset("Reset demo") })
    val quitButton =
        Button(
            "Quit",
            Runnable {
                onExit()
                window.close()
            },
        )

    val buttonRow = Panel(LinearLayout(Direction.HORIZONTAL))
    buttonRow.addComponent(primaryButton)
    buttonRow.addComponent(resetButton)
    buttonRow.addComponent(quitButton)

    val rightPanelContent = Panel(LinearLayout(Direction.VERTICAL))
    rightPanelContent.addComponent(buttonRow)
    rightPanelContent.addComponent(actionList)
    rightPanelContent.addComponent(
        summaryLabel.withBorder(Borders.singleLine("Live summary")),
    )
    val rightPanel = rightPanelContent.withBorder(Borders.singleLine("Actions"))

    val footerPanelContent = Panel(LinearLayout(Direction.VERTICAL))
    footerPanelContent.addComponent(statusLabel)
    footerPanelContent.addComponent(metricsLabel)
    val footerPanel = footerPanelContent.withBorder(Borders.singleLine("Status"))

    val contentPanel = Panel(GridLayout(2).setHorizontalSpacing(1).setVerticalSpacing(1))
    contentPanel.addComponent(
        Label("Old interactive widget demo, rebuilt on the new KMP repo and sized for a normal terminal."),
        GridLayout.createHorizontallyFilledLayoutData(2),
    )
    contentPanel.addComponent(leftPanel, GridLayout.createHorizontallyFilledLayoutData())
    contentPanel.addComponent(rightPanel, GridLayout.createHorizontallyFilledLayoutData())
    contentPanel.addComponent(footerPanel, GridLayout.createHorizontallyFilledLayoutData(2))

    val menuBar = MenuBar()
    menuBar.add(
        Menu("Demo")
            .add(MenuItem("Run primary action", Runnable { showPrimaryActionResult() }))
            .add(MenuItem("Reset", Runnable { applyReset("Reset demo") }))
            .add(MenuItem("Exit", Runnable {
                onExit()
                window.close()
            })),
    )
    menuBar.add(
        Menu("Help")
            .add(MenuItem("About", Runnable { showAboutDialog() })),
    )

    window.component = contentPanel
    window.menuBar = menuBar
    window.setHints(listOf(Window.Hint.EXPANDED))
    window.setCloseWindowWithEscape(true)

    textGUI.addListener(
        object : TextGUI.Listener {
            override fun onUnhandledKeyStroke(
                textGUI: TextGUI?,
                keyStroke: KeyStroke?,
            ): Boolean {
                if (keyStroke?.keyType == KeyType.EOF) {
                    onExit()
                    window.close()
                    return true
                }
                return false
            }
        },
    )

    syncState()
    return window
}

fun createInteractiveWidgetDemoTextGUI(screen: com.googlecode.lanterna.screen.Screen): MultiWindowTextGUI {
    val textGUI = MultiWindowTextGUI(screen)
    textGUI.isEOFWhenNoWindows = true
    return textGUI
}

private fun countLines(text: String): Int = if (text.isEmpty()) 1 else text.lines().size
