package com.googlecode.lanterna.examples

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

    system("mkdir -p \"$outputDir\"")
    writeTextFile("$outputDir/snapshot.txt", RenderScene.asText(target))
    writeTextFile("$outputDir/snapshot.svg", RenderScene.asSvg(target))

    println("Wrote native demo snapshots to $outputDir")
}

@OptIn(ExperimentalForeignApi::class)
private fun writeTextFile(
    path: String,
    content: String,
) {
    val handle =
        fopen(path, "w")
            ?: error("Unable to open output file: $path")
    try {
        fputs(content, handle)
    } finally {
        fclose(handle)
    }
}
