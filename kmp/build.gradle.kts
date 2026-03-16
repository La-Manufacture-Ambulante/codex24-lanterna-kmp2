plugins {
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
