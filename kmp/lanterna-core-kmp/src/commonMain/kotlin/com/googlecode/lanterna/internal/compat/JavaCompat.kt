package com.googlecode.lanterna.internal.compat

class EnumSet<E : Enum<E>> private constructor(
    private val delegate: LinkedHashSet<E>,
) : MutableSet<E> by delegate {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is EnumSet<*> -> delegate == other.delegate
            is Set<*> -> delegate == other
            else -> false
        }
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()

    companion object {
        fun <E : Enum<E>> noneOf(enumClass: kotlin.reflect.KClass<E>): EnumSet<E> {
            return EnumSet(linkedSetOf())
        }

        fun <E : Enum<E>> copyOf(source: Collection<out E>): EnumSet<E> {
            return EnumSet(LinkedHashSet(source))
        }
    }
}

object Character {
    fun toString(value: Char): String = value.toString()

    fun isISOControl(value: Char): Boolean = value.isISOControl()

    fun isWhitespace(value: Char): Boolean = value.isWhitespace()

    fun isSpaceChar(value: Char): Boolean = value.isWhitespace()

    fun isDigit(value: Char): Boolean = value.isDigit()

    fun digit(
        value: Char,
        radix: Int,
    ): Int = value.digitToIntOrNull(radix) ?: -1

    enum class UnicodeBlock {
        BASIC_LATIN,
        HIRAGANA,
        KATAKANA,
        KATAKANA_PHONETIC_EXTENSIONS,
        HANGUL_COMPATIBILITY_JAMO,
        HANGUL_JAMO,
        HANGUL_SYLLABLES,
        CJK_UNIFIED_IDEOGRAPHS,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
        CJK_COMPATIBILITY_FORMS,
        CJK_COMPATIBILITY_IDEOGRAPHS,
        CJK_RADICALS_SUPPLEMENT,
        CJK_SYMBOLS_AND_PUNCTUATION,
        ENCLOSED_CJK_LETTERS_AND_MONTHS,
        HALFWIDTH_AND_FULLWIDTH_FORMS,
        THAI,
        SPECIALS,
        ;

        companion object {
            fun of(value: Char): UnicodeBlock? {
                val code = value.code
                return when {
                    code in 0x0000..0x007F -> BASIC_LATIN
                    code in 0x0E00..0x0E7F -> THAI
                    code in 0x3040..0x309F -> HIRAGANA
                    code in 0x30A0..0x30FF -> KATAKANA
                    code in 0x31F0..0x31FF -> KATAKANA_PHONETIC_EXTENSIONS
                    code in 0x3130..0x318F -> HANGUL_COMPATIBILITY_JAMO
                    code in 0x1100..0x11FF -> HANGUL_JAMO
                    code in 0xAC00..0xD7AF -> HANGUL_SYLLABLES
                    code in 0x4E00..0x9FFF -> CJK_UNIFIED_IDEOGRAPHS
                    code in 0x3400..0x4DBF -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    code in 0x20000..0x2A6DF -> CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                    code in 0xFE30..0xFE4F -> CJK_COMPATIBILITY_FORMS
                    code in 0xF900..0xFAFF -> CJK_COMPATIBILITY_IDEOGRAPHS
                    code in 0x2E80..0x2EFF -> CJK_RADICALS_SUPPLEMENT
                    code in 0x3000..0x303F -> CJK_SYMBOLS_AND_PUNCTUATION
                    code in 0x3200..0x32FF -> ENCLOSED_CJK_LETTERS_AND_MONTHS
                    code in 0xFF00..0xFFEF -> HALFWIDTH_AND_FULLWIDTH_FORMS
                    code in 0xFFF0..0xFFFF -> SPECIALS
                    else -> BASIC_LATIN
                }
            }
        }
    }
}

object System {
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun currentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    fun identityHashCode(value: Any?): Int {
        return value?.hashCode() ?: 0
    }

    fun <T> arraycopy(
        src: Array<T>,
        srcPos: Int,
        dest: Array<T>,
        destPos: Int,
        length: Int,
    ) {
        var copied = 0
        while (copied < length) {
            dest[destPos + copied] = src[srcPos + copied]
            copied++
        }
    }
}

object Arrays {
    fun fill(
        array: IntArray,
        value: Int,
    ) {
        array.fill(value)
    }

    fun <T> fill(
        array: Array<T>,
        value: T,
    ) {
        array.fill(value)
    }

    fun <T> asList(vararg items: T): List<T> {
        return items.asList()
    }

    fun <T> sort(
        array: Array<T>,
        comparator: Comparator<in T>,
    ) {
        array.sortWith(comparator)
    }

    fun copyOf(
        array: CharArray,
        newSize: Int,
    ): CharArray {
        return array.copyOf(newSize)
    }

    fun equals(
        left: CharArray,
        right: CharArray,
    ): Boolean {
        return left.contentEquals(right)
    }

    fun hashCode(value: CharArray): Int {
        return value.contentHashCode()
    }
}

object Collections {
    fun <T> unmodifiableList(value: List<T>): List<T> {
        return value.toList()
    }

    fun <T> unmodifiableSet(value: Set<T>): Set<T> {
        return value.toSet()
    }

    fun <T> singleton(value: T): Set<T> {
        return setOf(value)
    }

    fun <T> singletonList(value: T): List<T> {
        return listOf(value)
    }

    fun <T> reverse(value: MutableList<T>) {
        value.reverse()
    }
}

object Objects {
    fun equals(
        left: Any?,
        right: Any?,
    ): Boolean {
        return left == right
    }

    fun hash(vararg values: Any?): Int {
        return values.contentHashCode()
    }
}

interface Queue<E> : MutableCollection<E> {
    fun poll(): E?

    fun offer(element: E): Boolean
}

class LinkedList<E> private constructor(
    private val delegate: MutableList<E>,
) : MutableList<E> by delegate, Queue<E> {
    constructor() : this(mutableListOf())
    constructor(values: Collection<E>) : this(values.toMutableList())

    override fun poll(): E? {
        return if (delegate.isEmpty()) null else delegate.removeAt(0)
    }

    override fun offer(element: E): Boolean {
        return delegate.add(element)
    }

    fun addFirst(element: E) {
        delegate.add(0, element)
    }

    fun removeFirst(): E {
        return delegate.removeAt(0)
    }

    val first: E
        get() = delegate.first()

    val last: E
        get() = delegate.last()
}

class TreeSet<E> private constructor(
    private val delegate: MutableSet<E>,
) : MutableSet<E> by delegate {
    constructor() : this(linkedSetOf())
    constructor(values: Collection<E>) : this(linkedSetOf<E>().apply { addAll(values) })
}

class TreeMap<K, V>(
    comparator: Any? = null,
) : MutableMap<K, V> by linkedMapOf()

class Properties {
    private val entries = linkedMapOf<String, String>()

    fun getProperty(
        key: String?,
        defaultValue: String? = null,
    ): String? {
        if (key == null) {
            return defaultValue
        }
        return entries[key] ?: defaultValue
    }

    fun setProperty(
        key: String?,
        value: String?,
    ): Any? {
        if (key != null && value != null) {
            entries[key] = value
        }
        return value
    }

    fun stringPropertyNames(): Set<String> {
        return entries.keys.toSet()
    }

    fun keys(): Iterator<String> {
        return entries.keys.iterator()
    }

    fun load(source: Any?) {
        val raw =
            when (source) {
                is String -> source
                is StringReader -> source.readText()
                else -> return
            }
        for (line in raw.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                continue
            }
            val separator = trimmed.indexOf('=')
            if (separator <= 0) {
                continue
            }
            val key = trimmed.substring(0, separator).trim()
            val value = trimmed.substring(separator + 1).trim()
            entries[key] = value
        }
    }
}

data class Locale(
    val language: String = "en",
) {
    companion object {
        fun getDefault(): Locale = Locale("en")
    }
}

class IdentityHashMap<K, V> : MutableMap<K, V> by linkedMapOf()

class WeakHashMap<K, V> : MutableMap<K, V> by linkedMapOf()

class WeakReference<T : Any>(
    private var value: T?,
) {
    fun get(): T? = value

    fun clear() {
        value = null
    }
}

class CopyOnWriteArrayList<E> : MutableList<E> by mutableListOf() {
    fun addIfAbsent(element: E): Boolean {
        if (contains(element)) {
            return false
        }
        add(element)
        return true
    }
}

class ConcurrentHashMap<K, V> : MutableMap<K, V> by linkedMapOf() {
    fun putIfAbsent(
        key: K,
        value: V,
    ): V? {
        if (containsKey(key)) {
            return get(key)
        }
        put(key, value)
        return null
    }
}

class AtomicBoolean(initialValue: Boolean) {
    private var value = initialValue

    fun get(): Boolean = value

    fun set(newValue: Boolean) {
        value = newValue
    }

    fun compareAndSet(
        expect: Boolean,
        update: Boolean,
    ): Boolean {
        if (value == expect) {
            value = update
            return true
        }
        return false
    }
}

class AtomicInteger(initialValue: Int) {
    private var value = initialValue

    fun get(): Int = value

    fun set(newValue: Int) {
        value = newValue
    }

    fun incrementAndGet(): Int {
        value += 1
        return value
    }

    fun decrementAndGet(): Int {
        value -= 1
        return value
    }
}

interface BlockingQueue<E> : MutableCollection<E> {
    fun poll(): E?

    fun take(): E
}

class LinkedBlockingQueue<E> : BlockingQueue<E> {
    private val values = ArrayDeque<E>()

    override val size: Int
        get() = values.size

    override fun add(element: E): Boolean {
        values.addLast(element)
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        for (value in elements) {
            changed = add(value) || changed
        }
        return changed
    }

    override fun clear() {
        values.clear()
    }

    override fun iterator(): MutableIterator<E> {
        return values.iterator()
    }

    override fun remove(element: E): Boolean {
        return values.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return values.removeAll(elements.toSet())
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return values.retainAll(elements.toSet())
    }

    override fun contains(element: E): Boolean {
        return values.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return values.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    override fun poll(): E? {
        return values.removeFirstOrNull()
    }

    override fun take(): E {
        return values.removeFirstOrNull() ?: throw IllegalStateException("Queue is empty")
    }
}

class InterruptedException(message: String? = null) : Exception(message)

enum class TimeUnit {
    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    ;

    fun toMillis(duration: Long): Long {
        return when (this) {
            NANOSECONDS -> duration / 1_000_000L
            MICROSECONDS -> duration / 1_000L
            MILLISECONDS -> duration
            SECONDS -> duration * 1_000L
            MINUTES -> duration * 60_000L
            HOURS -> duration * 3_600_000L
            DAYS -> duration * 86_400_000L
        }
    }
}

open class TimerTask {
    open fun run() {}

    open fun cancel(): Boolean = true
}

class Timer {
    constructor()
    constructor(
        name: String,
    )

    fun scheduleAtFixedRate(
        task: TimerTask?,
        delay: Long,
        period: Long,
    ) {
        task?.run()
    }

    fun cancel() {}
}

fun interface Consumer<T> {
    fun accept(value: T)
}

class Pattern private constructor(
    private val regex: Regex,
) {
    fun matcher(input: CharSequence): Matcher {
        return Matcher(regex, input.toString())
    }

    companion object {
        fun compile(pattern: String): Pattern {
            return Pattern(Regex(pattern))
        }
    }
}

class Matcher internal constructor(
    private val regex: Regex,
    private val input: String,
) {
    private var result: MatchResult? = null

    fun matches(): Boolean {
        result = regex.matchEntire(input)
        return result != null
    }

    fun find(): Boolean {
        result = regex.find(input)
        return result != null
    }

    fun group(index: Int): String {
        val groups = result?.groups ?: error("No match result available")
        return groups[index]?.value ?: error("No group at index $index")
    }

    fun groupCount(): Int {
        val groups = result?.groups ?: return 0
        return groups.size - 1
    }
}

object Integer {
    fun parseInt(value: String?): Int = value?.toIntOrNull() ?: 0

    fun toString(value: Int): String = value.toString()

    fun valueOf(value: String?): Int = value?.toIntOrNull() ?: 0
}

object JBoolean {
    fun parseBoolean(value: String?): Boolean = value?.toBoolean() ?: false

    fun toString(value: Boolean): String = value.toString()
}

class BigInteger constructor(
    private val value: String,
) {
    override fun toString(): String = value

    fun intValue(): Int = value.toInt()

    fun longValue(): Long = value.toLong()

    companion object {
        fun valueOf(value: Long): BigInteger = BigInteger(value.toString())
    }
}

class File private constructor(
    private val rawPath: String,
    normalized: Boolean,
) {
    constructor(path: String) : this(if (path.isEmpty()) "." else path, true)

    constructor(parent: File?, child: String) : this(
        when {
            parent == null -> child
            parent.path.endsWith("/") -> parent.path + child
            else -> parent.path + "/" + child
        },
        true,
    )

    val path: String
        get() = rawPath

    val name: String
        get() = rawPath.substringAfterLast('/').ifBlank { rawPath }

    val isAbsolute: Boolean
        get() = rawPath.startsWith("/") || rawPath.contains(':')

    val absolutePath: String
        get() = rawPath

    val absoluteFile: File
        get() = this

    val parentFile: File?
        get() {
            val parent = rawPath.substringBeforeLast('/', "")
            return if (parent.isEmpty()) null else File(parent)
        }

    val isFile: Boolean
        get() = !rawPath.endsWith("/")

    val isDirectory: Boolean
        get() = rawPath.endsWith("/")

    val exists: Boolean
        get() = true

    val isHidden: Boolean
        get() = name.startsWith(".")

    fun canRead(): Boolean = true

    fun listFiles(): Array<File>? = emptyArray()

    fun exists(): Boolean = exists

    companion object {
        fun listRoots(): Array<File> = arrayOf(File("/"))
    }
}

class FileInputStream(
    val file: File,
) {
    constructor(path: String) : this(File(path))
}

abstract class Reader {
    abstract fun read(): Int

    open fun ready(): Boolean = true
}

class BufferedReader(
    private val delegate: Reader?,
) : Reader() {
    override fun read(): Int = delegate?.read() ?: -1

    override fun ready(): Boolean = delegate?.ready() ?: false
}

class StringReader(
    private val text: String,
) : Reader() {
    private var index: Int = 0

    fun readText(): String = text

    override fun read(): Int {
        if (index >= text.length) {
            return -1
        }
        return text[index++].code
    }

    override fun ready(): Boolean = index < text.length
}

class LambdaReader(
    private val onRead: () -> Int,
    private val onReady: () -> Boolean = { true },
) : Reader() {
    override fun read(): Int = onRead()

    override fun ready(): Boolean = onReady()
}

interface Closeable {
    @Throws(com.googlecode.lanterna.internal.io.IOException::class)
    fun close()
}

inline fun <T> synchronizedCompat(
    lock: Any?,
    block: () -> T,
): T {
    return block()
}
