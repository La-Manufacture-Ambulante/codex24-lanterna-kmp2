package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.CheckBox
import com.googlecode.lanterna.gui2.ComboBox
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.ProgressBar
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.gui2.Runnable
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.TextGUI
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

private const val DEFAULT_TITLE = "Lanterna KMP"
private const val DEFAULT_NOTES = "Tab to widgets.\nEnter activates focus."
private const val DEFAULT_THEME = "Ocean"
private const val DEFAULT_PRESET = "Review"
private const val DEFAULT_PROGRESS = 25

internal data class InteractiveWidgetPreset(
    val label: String,
    val title: String,
    val notes: String,
    val progress: Int,
)

private val INTERACTIVE_WIDGET_PRESETS =
    listOf(
        InteractiveWidgetPreset(
            label = "Review",
            title = "Lanterna KMP",
            notes = "Tab to widgets.\nEnter activates focus.",
            progress = 25,
        ),
        InteractiveWidgetPreset(
            label = "Preview",
            title = "Theme Preview",
            notes = "Switch palettes.\nOpen dialogs for details.",
            progress = 50,
        ),
        InteractiveWidgetPreset(
            label = "Release",
            title = "Release Checklist",
            notes = "Verify native build.\nConfirm help and demo flows.",
            progress = 75,
        ),
    )

internal data class InteractiveWidgetDemoState(
    val title: String = DEFAULT_TITLE,
    val notes: String = DEFAULT_NOTES,
    val notificationsEnabled: Boolean = true,
    val theme: String = DEFAULT_THEME,
    val preset: String = DEFAULT_PRESET,
    val progress: Int = DEFAULT_PROGRESS,
    val clicks: Int = 0,
    val lastAction: String = "Ready",
)

internal fun renderInteractiveWidgetSummary(state: InteractiveWidgetDemoState): String =
    buildString {
        appendLine("Input: ${state.title.ifBlank { "<empty>" }}")
        appendLine("Preset: ${state.preset}")
        appendLine("Theme: ${state.theme}")
        appendLine("Progress: ${state.progress}%")
        appendLine("Primary clicks: ${state.clicks}")
        append("Notes lines: ${countLines(state.notes)}")
    }

internal fun renderInteractiveWidgetMetrics(state: InteractiveWidgetDemoState): String =
    buildString {
        append("Preset ${state.preset} | Theme ${state.theme} | Dialog ")
        append(if (state.notificationsEnabled) "on" else "off")
        append(" | ${state.progress}% | Clicks ${state.clicks}")
    }

internal fun resolveInteractiveWidgetPreset(label: String): InteractiveWidgetPreset =
    INTERACTIVE_WIDGET_PRESETS.firstOrNull { it.label == label } ?: INTERACTIVE_WIDGET_PRESETS.first()

private fun nextTheme(currentTheme: String): String =
    when (currentTheme) {
        "Ocean" -> "Amber"
        "Amber" -> "Graphite"
        else -> "Ocean"
    }

internal fun createInteractiveWidgetTheme(themeName: String): Theme =
    when (themeName) {
        "Amber" ->
            SimpleTheme.makeTheme(
                activeIsBold = true,
                baseForeground = TextColor.ANSI.BLACK,
                baseBackground = TextColor.ANSI.YELLOW,
                editableForeground = TextColor.ANSI.BLACK,
                editableBackground = TextColor.ANSI.YELLOW_BRIGHT,
                selectedForeground = TextColor.ANSI.YELLOW_BRIGHT,
                selectedBackground = TextColor.ANSI.RED,
                guiBackground = TextColor.ANSI.YELLOW_BRIGHT,
            )
        "Graphite" ->
            SimpleTheme.makeTheme(
                activeIsBold = true,
                baseForeground = TextColor.ANSI.WHITE,
                baseBackground = TextColor.ANSI.BLACK_BRIGHT,
                editableForeground = TextColor.ANSI.WHITE_BRIGHT,
                editableBackground = TextColor.ANSI.BLACK,
                selectedForeground = TextColor.ANSI.WHITE_BRIGHT,
                selectedBackground = TextColor.ANSI.BLUE_BRIGHT,
                guiBackground = TextColor.ANSI.BLACK,
            )
        else ->
            SimpleTheme.makeTheme(
                activeIsBold = true,
                baseForeground = TextColor.ANSI.WHITE_BRIGHT,
                baseBackground = TextColor.ANSI.BLUE,
                editableForeground = TextColor.ANSI.WHITE_BRIGHT,
                editableBackground = TextColor.ANSI.CYAN,
                selectedForeground = TextColor.ANSI.WHITE_BRIGHT,
                selectedBackground = TextColor.ANSI.BLUE_BRIGHT,
                guiBackground = TextColor.ANSI.BLUE_BRIGHT,
            )
    }

fun createInteractiveWidgetDemoWindow(
    textGUI: WindowBasedTextGUI,
    onExit: () -> Unit = {},
): BasicWindow {
    var state = InteractiveWidgetDemoState()
    var syncingView = false

    val statusLabel = Label("Status: ${state.lastAction}")
    val metricsLabel = Label(renderInteractiveWidgetMetrics(state)).setLabelWidth(64)
    val inputBox = TextBox(TerminalSize(24, 1), state.title)
    val notesBox = TextBox(TerminalSize(24, 2), state.notes, TextBox.Style.MULTI_LINE).setCaretWarp(true)
    val notificationsBox = CheckBox("Show dialog on run").setChecked(state.notificationsEnabled)
    val themeList = RadioBoxList<String>(TerminalSize(18, 3))
    val presetBox = ComboBox(INTERACTIVE_WIDGET_PRESETS.map { it.label })
    val progressBar = ProgressBar(0, 100, 24).setLabelFormat("%3.0f%%").setValue(state.progress)
    val quickActions = ActionListBox(TerminalSize(24, 3))
    val window = BasicWindow("Lanterna KMP Interactive Widget Demo")

    themeList.addItem("Ocean")
    themeList.addItem("Amber")
    themeList.addItem("Graphite")
    themeList.checkedItem = state.theme
    presetBox.setReadOnly(true)
    presetBox.setSelectedItem(state.preset)
    textGUI.theme = createInteractiveWidgetTheme(state.theme)

    fun syncState(message: String? = null) {
        if (message != null) {
            state = state.copy(lastAction = message)
        }
        statusLabel.setText("Status: ${state.lastAction}")
        metricsLabel.setText(renderInteractiveWidgetMetrics(state))
        progressBar.setValue(state.progress)
    }

    fun applyReset(message: String) {
        syncingView = true
        try {
            state = InteractiveWidgetDemoState(lastAction = message)
            textGUI.theme = createInteractiveWidgetTheme(state.theme)
            inputBox.setText(state.title)
            notesBox.setText(state.notes)
            notificationsBox.setChecked(state.notificationsEnabled)
            themeList.checkedItem = state.theme
            presetBox.setSelectedItem(state.preset)
            progressBar.setValue(state.progress)
        } finally {
            syncingView = false
        }
        syncState()
    }

    fun applyPreset(
        presetLabel: String,
        message: String,
    ) {
        val preset = resolveInteractiveWidgetPreset(presetLabel)
        syncingView = true
        try {
            state =
                state.copy(
                    preset = preset.label,
                    title = preset.title,
                    notes = preset.notes,
                    progress = preset.progress,
                    lastAction = message,
                )
            inputBox.setText(state.title)
            notesBox.setText(state.notes)
            presetBox.setSelectedItem(state.preset)
            progressBar.setValue(state.progress)
        } finally {
            syncingView = false
        }
        syncState()
    }

    fun advanceProgress(
        step: Int,
        message: String,
    ) {
        state = state.copy(progress = (state.progress + step).coerceAtMost(100))
        syncState(message)
    }

    fun showStatusDialog() {
        MessageDialog.showMessageDialog(
            textGUI,
            "Widget status",
            buildString {
                appendLine(renderInteractiveWidgetSummary(state))
                append("Notifications: ${if (state.notificationsEnabled) "enabled" else "muted"}")
            },
            MessageDialogButton.OK,
        )
    }

    fun showPrimaryActionResult() {
        state =
            state.copy(
                clicks = state.clicks + 1,
                progress = (state.progress + 10).coerceAtMost(100),
            )
        syncState("Primary action ran (${state.clicks})")
        if (state.notificationsEnabled) {
            MessageDialog.showMessageDialog(
                textGUI,
                "Primary action",
                buildString {
                    appendLine("Input: ${state.title.ifBlank { "<empty>" }}")
                    appendLine("Preset: ${state.preset}")
                    appendLine("Theme: ${state.theme}")
                    appendLine("Progress: ${state.progress}%")
                    append("Clicks: ${state.clicks}")
                },
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

    fun showDemoDialog() {
        MessageDialog.showMessageDialog(
            textGUI,
            "Demo actions",
            buildString {
                appendLine("Use Tab to move between inputs and actions.")
                appendLine("Widgets on-screen: Theme radios, Preset combo, Progress bar, Quick actions.")
                append("Enter activates the focused control.")
            },
            MessageDialogButton.OK,
        )
    }

    fun cycleTheme() {
        val nextTheme = nextTheme(state.theme)
        textGUI.theme = createInteractiveWidgetTheme(nextTheme)
        themeList.checkedItem = nextTheme
        state = state.copy(theme = nextTheme)
        syncState("Theme switched to $nextTheme")
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
                textGUI.theme = createInteractiveWidgetTheme(checkedTheme)
                if (!syncingView) {
                    syncState("Theme switched to $checkedTheme")
                }
            }
        },
    )

    presetBox.addListener(
        object : ComboBox.Listener {
            override fun onSelectionChanged(
                selectedIndex: Int,
                previousSelection: Int,
                changedByUserInteraction: Boolean,
            ) {
                val selectedPreset = presetBox.getSelectedItem() ?: return
                if (!syncingView && selectedPreset != state.preset) {
                    applyPreset(
                        selectedPreset,
                        if (changedByUserInteraction) "Loaded $selectedPreset preset" else "Preset updated",
                    )
                }
            }
        },
    )

    val leftPanelContent = Panel(GridLayout(1).setVerticalSpacing(0))
    leftPanelContent.addComponent(Label("Title"))
    leftPanelContent.addComponent(inputBox, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(Label("Theme"))
    leftPanelContent.addComponent(themeList, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(Label("Preset"))
    leftPanelContent.addComponent(presetBox, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(notificationsBox, GridLayout.createHorizontallyFilledLayoutData())
    leftPanelContent.addComponent(Label("Notes"))
    leftPanelContent.addComponent(notesBox, GridLayout.createHorizontallyFilledLayoutData())
    val leftPanel = leftPanelContent.withBorder(Borders.singleLine("Inputs"))

    val primaryButton = Button("Run", Runnable { showPrimaryActionResult() })
    val resetButton = Button("Reset", Runnable { applyReset("Reset demo") })
    val themeButton = Button("Theme", Runnable { cycleTheme() })
    val demoButton = Button("Demo", Runnable { showDemoDialog() })
    val helpButton = Button("Help", Runnable { showAboutDialog() })
    val quitButton =
        Button(
            "Quit",
            Runnable {
                onExit()
                window.close()
            },
        )

    val buttonGrid = Panel(GridLayout(2).setHorizontalSpacing(1).setVerticalSpacing(0))
    buttonGrid.addComponent(primaryButton)
    buttonGrid.addComponent(resetButton)
    buttonGrid.addComponent(themeButton)
    buttonGrid.addComponent(demoButton)
    buttonGrid.addComponent(helpButton)
    buttonGrid.addComponent(quitButton)

    quickActions.addItem("Run primary action", Runnable { showPrimaryActionResult() })
    quickActions.addItem("Advance progress", Runnable { advanceProgress(15, "Advanced progress") })
    quickActions.addItem("Open status dialog", Runnable { showStatusDialog() })

    val rightPanelContent = Panel(LinearLayout(Direction.VERTICAL))
    rightPanelContent.addComponent(Label("Actions"))
    rightPanelContent.addComponent(buttonGrid)
    rightPanelContent.addComponent(progressBar.withBorder(Borders.singleLine("Progress")))
    rightPanelContent.addComponent(quickActions.withBorder(Borders.singleLine("Quick actions")))
    val rightPanel = rightPanelContent.withBorder(Borders.singleLine("Actions"))

    val footerPanelContent = Panel(LinearLayout(Direction.VERTICAL))
    footerPanelContent.addComponent(statusLabel)
    footerPanelContent.addComponent(metricsLabel)
    val footerPanel = footerPanelContent.withBorder(Borders.singleLine("Status"))

    val contentPanel = Panel(GridLayout(2).setHorizontalSpacing(1).setVerticalSpacing(1))
    contentPanel.addComponent(leftPanel, GridLayout.createHorizontallyFilledLayoutData())
    contentPanel.addComponent(rightPanel, GridLayout.createHorizontallyFilledLayoutData())
    contentPanel.addComponent(footerPanel, GridLayout.createHorizontallyFilledLayoutData(2))

    window.component = contentPanel
    window.setHints(listOf(Window.Hint.EXPANDED))
    window.setCloseWindowWithEscape(true)
    window.focusedInteractable = inputBox

    textGUI.addListener(
        object : TextGUI.Listener {
            override fun onUnhandledKeyStroke(
                textGUI: TextGUI?,
                keyStroke: KeyStroke?,
            ): Boolean {
                when {
                    keyStroke?.keyType == KeyType.EOF -> {
                        onExit()
                        window.close()
                        return true
                    }
                    keyStroke == KeyStroke.fromString("<a-d>") -> {
                        showDemoDialog()
                        return true
                    }
                    keyStroke == KeyStroke.fromString("<a-h>") -> {
                        showAboutDialog()
                        return true
                    }
                    keyStroke == KeyStroke.fromString("<a-t>") -> {
                        cycleTheme()
                        return true
                    }
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
