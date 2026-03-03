package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.Window
import java.util.Collections
import java.util.HashSet
import java.util.Objects
import java.util.Set

/**
 * Abstract class for dialog building, containing much shared code between different kinds of dialogs
 * @param <B> The real type of the builder class
 * @param <T> Type of dialog this builder is building
 * @author Martin
 */
abstract class AbstractDialogBuilder<B, T : DialogWindow>(title: String?) {
    protected var title: String?
    protected var description: String?
    protected var extraWindowHints: Set<Window.Hint>?

    /**
     * Default constructor for a dialog builder
     * @param title Title to assign to the dialog
     */
    init {
        this.title = title
        this.description = null
        this.extraWindowHints = Collections.singleton(Window.Hint.CENTERED)
    }

    /**
     * Changes the title of the dialog
     * @param title New title
     * @return Itself
     */
    open fun setTitle(title: String?): B? {
        var newTitle = title
        if (newTitle == null) {
            newTitle = ""
        }
        this.title = newTitle
        return self()
    }

    /**
     * Returns the title that the built dialog will have
     * @return Title that the built dialog will have
     */
    open fun getTitle(): String? {
        return title
    }

    /**
     * Changes the description of the dialog
     * @param description New description
     * @return Itself
     */
    open fun setDescription(description: String?): B? {
        this.description = description
        return self()
    }

    /**
     * Returns the description that the built dialog will have
     * @return Description that the built dialog will have
     */
    open fun getDescription(): String? {
        return description
    }

    /**
     * Assigns a set of extra window hints that you want the built dialog to have
     * @param extraWindowHints Window hints to assign to the window in addition to the ones the builder will put
     * @return Itself
     */
    open fun setExtraWindowHints(extraWindowHints: Set<Window.Hint>?): B? {
        this.extraWindowHints = extraWindowHints
        return self()
    }

    /**
     * Returns the list of extra window hints that will be assigned to the window when built
     * @return List of extra window hints that will be assigned to the window when built
     */
    open fun getExtraWindowHints(): Set<Window.Hint>? {
        return extraWindowHints
    }

    /**
     * Helper method for casting this to {@code type} parameter {@code B}
     * @return {@code this} as {@code B}
     */
    protected abstract fun self(): B?

    /**
     * Builds the dialog according to the builder implementation
     * @return New dialog object
     */
    protected abstract fun buildDialog(): T?

    /**
     * Builds a new dialog following the specifications of this builder
     * @return New dialog built following the specifications of this builder
     */
    fun build(): T? {
        val dialog = buildDialog()
        val extraWindowHints = Objects.requireNonNull(this.extraWindowHints)
        if (!extraWindowHints.isEmpty()) {
            val nonNullDialog = Objects.requireNonNull(dialog)
            val combinedHints: MutableSet<Window.Hint> = HashSet(nonNullDialog.getHints())
            combinedHints.addAll(extraWindowHints)
            nonNullDialog.setHints(combinedHints)
        }
        return dialog
    }
}
