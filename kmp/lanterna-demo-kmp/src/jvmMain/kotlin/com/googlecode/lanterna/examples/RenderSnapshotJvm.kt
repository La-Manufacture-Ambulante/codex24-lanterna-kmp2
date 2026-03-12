package com.googlecode.lanterna.examples

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val outputDir = File(System.getProperty("demo.outputDir", "build/reports/demos/jvm")).apply {
        mkdirs()
    }
    val target = System.getProperty("demo.target", "jvm")

    File(outputDir, "snapshot.txt").writeText(RenderScene.asText(target))
    File(outputDir, "snapshot.svg").writeText(RenderScene.asSvg(target))
    writePng(outputDir = outputDir, target = target)

    println("Wrote demo snapshots to ${outputDir.absolutePath}")
}

private fun writePng(outputDir: File, target: String) {
    val lines = RenderScene.lines(target)
    val cellWidth = 11
    val cellHeight = 19
    val paddingX = 14
    val paddingY = 16
    val widthPx = (lines.maxOfOrNull { it.length } ?: 1) * cellWidth + (paddingX * 2)
    val heightPx = lines.size * cellHeight + (paddingY * 2)
    val image = BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    try {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = Color(0x10, 0x18, 0x20)
        graphics.fillRect(0, 0, widthPx, heightPx)

        graphics.font = Font(Font.MONOSPACED, Font.PLAIN, 14)
        graphics.color = Color(0xD8, 0xF3, 0xDC)
        lines.forEachIndexed { index, line ->
            val baseline = paddingY + (index * cellHeight) + 14
            graphics.drawString(line, paddingX, baseline)
        }
    } finally {
        graphics.dispose()
    }
    ImageIO.write(image, "png", File(outputDir, "snapshot.png"))
}
