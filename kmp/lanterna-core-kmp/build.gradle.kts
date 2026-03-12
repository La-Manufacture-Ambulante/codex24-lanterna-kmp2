import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    kotlin("multiplatform") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
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

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jvmTestCoverageReport") {
    dependsOn(tasks.named("jvmTest"))

    val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")

    classDirectories.setFrom(jvmMainCompilation.output.classesDirs)
    sourceDirectories.setFrom(jvmMainCompilation.allKotlinSourceSets.map { it.kotlin.sourceDirectories })
    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")) {
            include("jvmTest.exec", "jvmTest*.exec")
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
