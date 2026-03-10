package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.TerminalSize
import kotlin.collections.ArrayList
import java.util.Arrays

/**
 * Dialog builder for the [ActionListDialog] class.
 */
class ActionListDialogBuilder : AbstractDialogBuilder<ActionListDialogBuilder, ActionListDialog>("ActionListDialogBuilder") {
    private val actions: MutableList<Runnable> = ArrayList()
    private var listBoxSize: TerminalSize? = null
    private var canCancel: Boolean = true
    private var closeAutomatically: Boolean = true

    override fun self(): ActionListDialogBuilder = this

    override fun buildDialog(): ActionListDialog {
        return ActionListDialog(
            getTitle(),
            getDescription(),
            listBoxSize,
            canCancel,
            closeAutomatically,
            actions,
        )
    }

    fun setListBoxSize(listBoxSize: TerminalSize?): ActionListDialogBuilder {
        this.listBoxSize = listBoxSize
        return this
    }

    fun getListBoxSize(): TerminalSize? = listBoxSize

    fun setCanCancel(canCancel: Boolean): ActionListDialogBuilder {
        this.canCancel = canCancel
        return this
    }

    fun isCanCancel(): Boolean = canCancel

    fun addAction(label: String?, action: Runnable): ActionListDialogBuilder {
        return addAction(
            object : Runnable {
                override fun toString(): String {
                    return label ?: ""
                }

                override fun run() {
                    action.run()
                }
            },
        )
    }

    fun addAction(action: Runnable): ActionListDialogBuilder {
        actions.add(action)
        return this
    }

    fun addActions(vararg actions: Runnable): ActionListDialogBuilder {
        this.actions.addAll(Arrays.asList(*actions))
        return this
    }

    fun getActions(): List<Runnable> = ArrayList(actions)

    fun setCloseAutomaticallyOnAction(closeAutomatically: Boolean): ActionListDialogBuilder {
        this.closeAutomatically = closeAutomatically
        return this
    }
}
