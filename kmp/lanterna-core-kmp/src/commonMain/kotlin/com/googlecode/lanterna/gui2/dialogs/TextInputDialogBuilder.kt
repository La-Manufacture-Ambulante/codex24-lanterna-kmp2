package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.internal.compat.Pattern

/**
 * Dialog builder for the [TextInputDialog] class.
 */
class TextInputDialogBuilder : AbstractDialogBuilder<TextInputDialogBuilder, TextInputDialog>("TextInputDialog") {
    private var initialContent: String? = ""
    private var textBoxSize: TerminalSize? = null
    private var validator: TextInputDialogResultValidator? = null
    private var passwordInput: Boolean = false

    override fun self(): TextInputDialogBuilder = this

    override fun buildDialog(): TextInputDialog {
        var size = textBoxSize
        if ((initialContent == null || initialContent?.trim().isNullOrEmpty()) && size == null) {
            size = TerminalSize(40, 1)
        }
        return TextInputDialog(getTitle(), getDescription(), size, initialContent, validator, passwordInput)
    }

    fun setInitialContent(initialContent: String?): TextInputDialogBuilder {
        this.initialContent = initialContent
        return this
    }

    fun getInitialContent(): String? = initialContent

    fun setTextBoxSize(textBoxSize: TerminalSize?): TextInputDialogBuilder {
        this.textBoxSize = textBoxSize
        return this
    }

    fun getTextBoxSize(): TerminalSize? = textBoxSize

    fun setValidator(validator: TextInputDialogResultValidator?): TextInputDialogBuilder {
        this.validator = validator
        return this
    }

    fun getValidator(): TextInputDialogResultValidator? = validator

    fun setValidationPattern(pattern: Pattern, errorMessage: String?): TextInputDialogBuilder {
        return setValidator(
            object : TextInputDialogResultValidator {
                override fun validate(content: String?): String? {
                    val matcher = pattern.matcher(content ?: "")
                    if (!matcher.matches()) {
                        return errorMessage ?: "Invalid input"
                    }
                    return null
                }
            },
        )
    }

    fun setPasswordInput(passwordInput: Boolean): TextInputDialogBuilder {
        this.passwordInput = passwordInput
        return this
    }

    fun isPasswordInput(): Boolean = passwordInput
}
