package com.googlecode.lanterna.examples

import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getenv
import platform.posix.system

@OptIn(ExperimentalForeignApi::class)
fun nativeSnapshotMain() {
    val outputDir = getenv("DEMO_OUTPUT_DIR")?.toKString() ?: "build/reports/demos/native"
    val target = getenv("DEMO_TARGET")?.toKString() ?: "native"
    val runSmoke = getenv("DEMO_LANTERNA_SMOKE")?.toKString() == "1"

    system("mkdir -p \"$outputDir\"")
    writeTextFile("$outputDir/snapshot.txt", RenderScene.asText(target))
    writeTextFile("$outputDir/snapshot.svg", RenderScene.asSvg(target))
    if (runSmoke) {
        runNativeLanternaSmoke()
    }

    println("Wrote native demo snapshots to $outputDir")
}

@OptIn(ExperimentalForeignApi::class)
private fun writeTextFile(path: String, content: String) {
    val handle = fopen(path, "w")
        ?: error("Unable to open output file: $path")
    try {
        fputs(content, handle)
    } finally {
        fclose(handle)
    }
}

private fun runNativeLanternaSmoke() {
    val terminal = DefaultTerminalFactory().createTerminal()
    terminal.enterPrivateMode()
    try {
        terminal.clearScreen()
        terminal.setCursorPosition(0, 0)
        terminal.putString("Lanterna native smoke test")
        terminal.flush()
    } finally {
        terminal.exitPrivateMode()
        terminal.close()
    }
}
