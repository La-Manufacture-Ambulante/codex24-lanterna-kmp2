package com.googlecode.lanterna

import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Properties

typealias FocusChangeDirection = Interactable.FocusChangeDirection
typealias Hint = Window.Hint
typealias Result = Interactable.Result

val ARROW_UP: KeyType get() = KeyType.ARROW_UP
val ARROW_DOWN: KeyType get() = KeyType.ARROW_DOWN
val ARROW_LEFT: KeyType get() = KeyType.ARROW_LEFT
val ARROW_RIGHT: KeyType get() = KeyType.ARROW_RIGHT

val CHARACTER: KeyType get() = KeyType.CHARACTER
val ESCAPE: KeyType get() = KeyType.ESCAPE
val ENTER: KeyType get() = KeyType.ENTER
val TAB: KeyType get() = KeyType.TAB
val EOF: KeyType get() = KeyType.EOF
val BACKSPACE: KeyType get() = KeyType.BACKSPACE
val HANDLED: Interactable.Result get() = Interactable.Result.HANDLED

private fun String.cap(): String = replaceFirstChar { c -> c.uppercaseChar() }

private fun Any.findMethod(
    name: String,
    argCount: Int,
): Method? {
    var cls: Class<*>? = this.javaClass
    while (cls != null) {
        cls.declaredMethods.firstOrNull { it.name == name && it.parameterCount == argCount }?.let {
            it.isAccessible = true
            return it
        }
        cls = cls.superclass
    }
    return null
}

private fun Any.findField(name: String): Field? {
    var cls: Class<*>? = this.javaClass
    while (cls != null) {
        try {
            val field = cls.getDeclaredField(name)
            field.isAccessible = true
            return field
        } catch (_: NoSuchFieldException) {
            cls = cls.superclass
        }
    }
    return null
}

private fun Any.readMember(name: String): Any? {
    findMethod("get${name.cap()}", 0)?.let { return it.invoke(this) }
    findMethod("is${name.cap()}", 0)?.let { return it.invoke(this) }
    findMethod(name, 0)?.let { return it.invoke(this) }
    findField(name)?.let { return it.get(this) }
    findField("is${name.cap()}")?.let { return it.get(this) }
    throw NoSuchMethodException("No readable member '$name' on ${this.javaClass.name}")
}

private fun Any.writeMember(
    name: String,
    value: Any?,
) {
    findMethod("set${name.cap()}", 1)?.let {
        it.invoke(this, value)
        return
    }
    findMethod(name, 1)?.let {
        it.invoke(this, value)
        return
    }
    findField(name)?.let {
        it.set(this, value)
        return
    }
    throw NoSuchMethodException("No writable member '$name' on ${this.javaClass.name}")
}

private fun Any.callMember(
    name: String,
    vararg args: Any?,
): Any? {
    findMethod(name, args.size)?.let { return it.invoke(this, *args) }
    throw NoSuchMethodException("No method '$name/${args.size}' on ${this.javaClass.name}")
}

private fun Any.readAny(name: String): Any = readMember(name) ?: error("Null member '$name'")

private fun Any.readInt(name: String): Int = (readAny(name) as Number).toInt()

fun String.charAt(index: Int): Char = this[index]

fun CharSequence.charAt(index: Int): Char = this[index]

fun String.replaceAll(
    regex: String,
    replacement: String,
): String = replace(Regex(regex), replacement)

fun String.getBytes(): ByteArray = encodeToByteArray()

fun Number.intValue(): Int = toInt()

fun Any.getClass(): Class<*> = this.javaClass

operator fun Int.not(): Boolean = this == 0

operator fun Long.not(): Boolean = this == 0L

fun <K, V> Map<K, V>.keySet(): Set<K> = keys

fun Properties.keySet(): Set<Any?> = keys

fun Any.isCtrlDown(): Boolean = readAny("ctrlDown") as Boolean

fun Any.isAltDown(): Boolean = readAny("altDown") as Boolean

fun Any.isDoubleWidth(): Boolean = readAny("doubleWidth") as Boolean

val Any.length: Int get() = runCatching { readInt("length") }.getOrElse { ((callMember("length") ?: 0) as Number).toInt() }
val Any.size: Int get() = runCatching { readInt("size") }.getOrElse { ((callMember("size") ?: 0) as Number).toInt() }

fun Any.getActive(): Any = readAny("active")

fun Any.getActiveWindow(): Any = readAny("activeWindow")

fun Any.getAndResetDirtyCells(): Any = readAny("andResetDirtyCells")

fun Any.getBackgroundPane(): Any = readAny("backgroundPane")

fun Any.getBufferLineCount(): Int = readInt("bufferLineCount")

fun Any.getCharacter(): Any = readAny("character")

fun Any.getCharacterString(): Any = readAny("characterString")

fun Any.getCheckedItem(): Any = readAny("checkedItem")

fun Any.getChildCount(): Int = readInt("childCount")

fun Any.getChildren(): Any = readAny("children")

fun Any.getChildrenList(): Any = readAny("childrenList")

fun Any.getColumn(): Int = readInt("column")

fun Any.getColumnCount(): Int = readInt("columnCount")

fun Any.getColumns(): Int = readInt("columns")

fun Any.getCursorBufferPosition(): TerminalPosition = readAny("cursorBufferPosition") as TerminalPosition

fun Any.getCursorPosition(): TerminalPosition = readAny("cursorPosition") as TerminalPosition

fun Any.getDecoratedSize(): Any = readAny("decoratedSize")

fun Any.getDefault(): Any = readAny("default")

fun Any.getDefaultDefinition(): Any = readAny("defaultDefinition")

fun Any.getDirtyCells(): Any = readAny("dirtyCells")

fun Any.getExpandedLength(): Int = readInt("expandedLength")

fun Any.getGlobalPosition(): Any = readAny("globalPosition")

fun Any.getGUIThread(): Any = readAny("guiThread")

fun Any.getHints(): Any = readAny("hints")

fun Any.getInputStream(): Any = readAny("inputStream")

fun Any.getKeyType(): KeyType? = readMember("keyType") as KeyType?

fun Any.getLabel(): Any = readAny("label")

fun Any.getLastExpandedChildren(): Any = readAny("lastExpandedChildren")

fun Any.getLayoutData(): Any = readAny("layoutData")

fun Any.getMessage(): String? = readMember("message") as String?

fun Any.getNormal(): ThemeStyle = readAny("normal") as ThemeStyle

fun Any.getPosition(): TerminalPosition = readAny("position") as TerminalPosition

fun Any.getPreferredSize(): Any = readAny("preferredSize")

fun Any.getRegisteredThemes(): Any = readAny("registeredThemes")

fun Any.getRemoteSocketAddress(): Any = readAny("remoteSocketAddress")

fun Any.getRenderer(): Any = readAny("renderer")

fun Any.getRow(): Int = readInt("row")

fun Any.getRowCount(): Int = readInt("rowCount")

fun Any.getRows(): Int = readInt("rows")

fun Any.getRoot(): Any = readAny("root")

fun Any.getRuntime(): Any = readAny("runtime")

fun Any.getSelectedItem(): Any = readAny("selectedItem")

fun Any.getSelectedNode(): Any = readAny("selectedNode")

fun Any.getSize(): TerminalSize = readAny("size") as TerminalSize

fun Any.getTabBehaviour(): Any = readAny("tabBehaviour")

fun Any.getTerminalSize(): TerminalSize = readAny("terminalSize") as TerminalSize

fun Any.getText(): String = readAny("text") as String

fun Any.getDefinition(): Any = readAny("definition")

fun Any.getTextGUI(): Any = readAny("textGUI")

fun Any.getTextOrDefault(): Any = readAny("textOrDefault")

fun Any.getTextOrDefault(value: Any?): Any = callMember("getTextOrDefault", value) ?: ""

fun Any.getTheme(): ThemeStyle = readAny("theme") as ThemeStyle

fun Any.getWrapBehaviour(): Any = readAny("wrapBehaviour")

fun Any.getWindows(): Any = readAny("windows")

fun Any.getRed(): Int = readInt("red")

fun Any.getGreen(): Int = readInt("green")

fun Any.getBlue(): Int = readInt("blue")

fun Any.setCheckedItem(value: Any?) = writeMember("checkedItem", value)

fun Any.setCharacter(value: Any?) = writeMember("character", value)

fun Any.setCharacter(vararg values: Any?) = callMember("setCharacter", *values)

fun Any.setComponent(value: Any?) = writeMember("component", value)

fun Any.setCursorPosition(value: Any?) = writeMember("cursorPosition", value)

fun Any.setDecoratedSize(value: Any?) = writeMember("decoratedSize", value)

fun Any.setEOFWhenNoWindows(value: Any?) = writeMember("eofWhenNoWindows", value)

fun Any.setExceptionHandler(value: Any?) = writeMember("exceptionHandler", value)

fun Any.setIntegerProperty(
    key: Any?,
    value: Any?,
) = callMember("setIntegerProperty", key, value)

fun Any.setBooleanProperty(
    key: Any?,
    value: Any?,
) = callMember("setBooleanProperty", key, value)

fun Any.setLabel(value: Any?) = writeMember("label", value)

fun Any.setMenuBar(value: Any?) = writeMember("menuBar", value)

fun Any.setOverflowCircle(value: Any?) = writeMember("overflowCircle", value)

fun Any.setPosition(value: Any?) = writeMember("position", value)

fun Any.setScrollBarsHidden(value: Any?) = writeMember("scrollBarsHidden", value)

fun Any.setStyleable(value: Any?) = writeMember("styleable", value)

fun Any.setTabBehaviour(value: Any?) = writeMember("tabBehaviour", value)

fun Any.setText(value: Any?) = writeMember("text", value)

fun Any.setTheme(value: Any?) = writeMember("theme", value)

fun Any.setTitle(value: Any?) = writeMember("title", value)

fun Any.setViewLeftColumn(value: Any?) = writeMember("viewLeftColumn", value)

fun Any.setAllowPartialColumn(value: Any?) = writeMember("allowPartialColumn", value)

fun Any.setActive(value: Any?) = writeMember("active", value)

fun Any.setValidationPattern(value: Any?) = writeMember("validationPattern", value)

fun Any.setVisible(value: Any?) = writeMember("visible", value)

fun Any.setWrapBehaviour(value: Any?) = writeMember("wrapBehaviour", value)

fun Any.flush() {
    runCatching { callMember("flush") }.onFailure { runCatching { callMember("refresh") } }
}

fun Any.read(): Any? = callMember("read")

fun Any.addComponent(component: Any?): Any? = callMember("addComponent", component)

fun Any.addRow(vararg values: Any?): Any? = callMember("addRow", *values)

fun Any.addColumn(vararg values: Any?): Any? = callMember("addColumn", *values)

fun Any.removeRow(vararg values: Any?): Any? = callMember("removeRow", *values)

fun Any.removeColumn(vararg values: Any?): Any? = callMember("removeColumn", *values)

fun Any.withBorder(border: Any?): Any = runCatching { callMember("withBorder", border) }.getOrNull() ?: this

fun Any.withRelative(value: Any?): Any = runCatching { callMember("withRelative", value) }.getOrNull() ?: this

fun Any.withRelative(vararg values: Any?): Any = runCatching { callMember("withRelative", *values) }.getOrNull() ?: this

fun Any.withLineBufferScrollbackSize(value: Int): Any =
    runCatching {
        callMember("withLineBufferScrollbackSize", value)
    }.getOrNull() ?: this

fun Any.computeDepth(vararg values: Any?): Any? = callMember("computeDepth", *values)

fun Any.start() {
    runCatching { callMember("start") }.onFailure { runCatching { callMember("startScreen") } }
}
