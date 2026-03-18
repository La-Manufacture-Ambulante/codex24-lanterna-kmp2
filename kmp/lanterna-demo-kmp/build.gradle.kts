import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

kotlin {
    jvm()
    linuxX64()
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "com.alaeri.snapshots.nativeSnapshotMain"
                baseName = "lanterna-snapshot-demo"
            }
            executable("interactiveWidgetDemo") {
                entryPoint = "com.googlecode.lanterna.examples.interactiveWidgetDemoMain"
                baseName = "lanterna-interactive-widget-demo"
            }
            executable("keyInputRepro") {
                entryPoint = "com.alaeri.keyInputReproMain"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":lanterna-core-kmp"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosX64Test by getting {
            dependsOn(nativeTest)
        }
        val macosArm64Main by getting {
            dependsOn(nativeMain)
        }
        val macosArm64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")

tasks.register<JavaExec>("jvmRenderSnapshot") {
    group = "verification"
    description = "Renders a deterministic JVM demo snapshot (PNG/SVG/TXT) for CI artifacts."
    dependsOn(tasks.named("jvmMainClasses"))

    classpath(
        jvmMainCompilation.output.allOutputs,
        jvmMainCompilation.runtimeDependencyFiles,
    )
    mainClass.set("com.googlecode.lanterna.examples.RenderSnapshotJvmKt")

    val outputDir = layout.buildDirectory.dir("reports/demos/jvm")
    doFirst {
        outputDir.get().asFile.mkdirs()
    }
    systemProperty("demo.outputDir", outputDir.get().asFile.absolutePath)
    systemProperty("demo.target", "jvm-${System.getProperty("os.name")}")
}

tasks.register<JavaExec>("jvmInteractiveWidgetDemo") {
    group = "application"
    description = "Runs the interactive widget demo on the JVM target."
    dependsOn(tasks.named("jvmMainClasses"))

    classpath(
        jvmMainCompilation.output.allOutputs,
        jvmMainCompilation.runtimeDependencyFiles,
    )
    mainClass.set("com.googlecode.lanterna.examples.InteractiveWidgetDemoJvmKt")
}

tasks.register<JavaExec>("runKeyInputReproJvm") {
    group = "application"
    description = "Runs the key-input repro app on JVM."
    dependsOn(tasks.named("jvmMainClasses"))

    classpath(
        jvmMainCompilation.output.allOutputs,
        jvmMainCompilation.runtimeDependencyFiles,
    )
    mainClass.set("com.alaeri.KeyInputReproJvmMainKt")
    standardInput = System.`in`
}
