plugins {
    kotlin("multiplatform") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

ktlint {
    filter {
        exclude("**/src/commonMain/**")
    }
}

tasks.matching { it.name == "runKtlintCheckOverCommonMainSourceSet" || it.name == "ktlintCommonMainSourceSetCheck" }
    .configureEach {
        enabled = false
    }

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("net.java.dev.jna:jna:5.14.0")
                implementation("net.java.dev.jna:jna-platform:5.14.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
    }
}
