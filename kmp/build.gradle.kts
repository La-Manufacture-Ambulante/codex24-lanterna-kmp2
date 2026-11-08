plugins {
    kotlin("multiplatform") version "2.1.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    base
}

val releaseVersion = System.getenv("RELEASE_VERSION")?.takeIf { it.isNotBlank() }
val jitpackVersion = System.getenv("JITPACK_VERSION")?.takeIf { it.isNotBlank() }
val configuredVersion =
    releaseVersion
        ?: jitpackVersion
        ?: (findProperty("VERSION_NAME") as String?)?.takeIf { it.isNotBlank() }
        ?: "dev-SNAPSHOT"
val configuredGroup =
    (findProperty("GROUP") as String?)?.takeIf { it.isNotBlank() }
        ?: "com.github.La-Manufacture-Ambulante.codex24-lanterna-kmp2"

allprojects {
    group = configuredGroup
    version = configuredVersion
}

allprojects {
    repositories {
        mavenCentral()
    }
}
