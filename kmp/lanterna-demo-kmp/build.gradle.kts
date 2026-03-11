plugins {
    kotlin("multiplatform") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":lanterna-core-kmp"))
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation(project(":lanterna-core-kmp"))
            }
        }
        val jvmTest by getting
    }
}
