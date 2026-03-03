package com.googlecode.lanterna.gui2.dialogs

/**
 * Interface to implement for custom validation of text input in a `TextInputDialog`
 * @author Martin
 */
interface TextInputDialogResultValidator {
    /**
     * Tests the content in the text box if it is valid or not
     * @param content Current content of the text box
     * @return `null` if the content is valid, or an error message explaining what's wrong with the content
     * otherwise
     */
    fun validate(content: String?): String?
}
