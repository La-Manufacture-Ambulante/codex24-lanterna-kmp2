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
    mingwX64()

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "com.googlecode.lanterna.examples.nativeSnapshotMain"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":lanterna-core-kmp"))
            }
        }
        val commonTest by getting
        val jvmMain by getting
        val jvmTest by getting
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
