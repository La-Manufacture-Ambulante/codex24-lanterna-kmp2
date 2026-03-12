package com.googlecode.lanterna.examples

object RenderScene {
    const val WIDTH = 52
    const val HEIGHT = 18

    fun lines(target: String): List<String> {
        val canvas = CharMatrix(WIDTH, HEIGHT, ' ')

        canvas.drawBox(0, 0, WIDTH, HEIGHT, '#')
        canvas.write(2, 2, "Lanterna KMP Render Demo")
        canvas.write(2, 4, "Target: $target")
        canvas.write(2, 6, "The quick brown fox jumps over")
        canvas.write(2, 7, "the lazy dog 0123456789 !@#$%^&*()")
        canvas.write(2, 10, "ASCII border and fixed text layout")
        canvas.write(2, 12, "for deterministic CI snapshot checks.")
        canvas.drawBox(1, HEIGHT - 4, WIDTH - 2, 3, '-')
        canvas.write(3, HEIGHT - 3, "lanterna-demo-kmp snapshot")

        return canvas.toLines()
    }

    fun asText(target: String): String = lines(target).joinToString(separator = "\n", postfix = "\n")

    fun asSvg(target: String): String {
        val rows = lines(target)
        val maxLen = rows.maxOfOrNull { it.length } ?: 1
        val cellWidth = 10
        val cellHeight = 18
        val paddingX = 12
        val paddingY = 20
        val widthPx = maxLen * cellWidth + (paddingX * 2)
        val heightPx = rows.size * cellHeight + (paddingY * 2)
        val baselineOffset = 14

        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine(
                """<svg xmlns="http://www.w3.org/2000/svg" width="$widthPx" height="$heightPx" viewBox="0 0 $widthPx $heightPx">""",
            )
            appendLine("""  <rect x="0" y="0" width="$widthPx" height="$heightPx" fill="#101820"/>""")
            appendLine("""  <g font-family="monospace" font-size="14" fill="#d8f3dc">""")
            rows.forEachIndexed { rowIndex, row ->
                val y = paddingY + (rowIndex * cellHeight) + baselineOffset
                appendLine("""    <text x="$paddingX" y="$y">${escapeXml(row)}</text>""")
            }
            appendLine("""  </g>""")
            appendLine("""</svg>""")
        }
    }

    private fun escapeXml(value: String): String =
        buildString(value.length) {
            value.forEach { ch ->
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(ch)
                }
            }
        }
}

private class CharMatrix(
    private val width: Int,
    private val height: Int,
    fill: Char,
) {
    private val rows: Array<CharArray> = Array(height) { CharArray(width) { fill } }

    fun write(
        x: Int,
        y: Int,
        text: String,
    ) {
        if (y !in 0 until height) {
            return
        }
        val row = rows[y]
        var col = x
        text.forEach { ch ->
            if (col in 0 until width) {
                row[col] = ch
            }
            col += 1
        }
    }

    fun drawBox(
        x: Int,
        y: Int,
        boxWidth: Int,
        boxHeight: Int,
        border: Char,
    ) {
        if (boxWidth <= 1 || boxHeight <= 1) {
            return
        }
        val maxX = x + boxWidth - 1
        val maxY = y + boxHeight - 1
        for (col in x..maxX) {
            set(col, y, border)
            set(col, maxY, border)
        }
        for (row in y..maxY) {
            set(x, row, border)
            set(maxX, row, border)
        }
    }

    private fun set(
        x: Int,
        y: Int,
        value: Char,
    ) {
        if (x !in 0 until width || y !in 0 until height) {
            return
        }
        rows[y][x] = value
    }

    fun toLines(): List<String> = rows.map { it.concatToString() }
}
