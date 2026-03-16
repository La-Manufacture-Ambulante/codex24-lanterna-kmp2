import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
    jacoco
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvm()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val macosMain by creating {
            dependsOn(nativeMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
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

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set((findProperty("POM_NAME") as String?) ?: "Lanterna KMP Core")
            description.set((findProperty("POM_DESCRIPTION") as String?) ?: "Kotlin Multiplatform migration of Lanterna core APIs.")
            url.set((findProperty("POM_URL") as String?) ?: "https://github.com/La-Manufacture-Ambulante/codex24-lanterna-kmp2")
            licenses {
                license {
                    name.set((findProperty("POM_LICENSE_NAME") as String?) ?: "Apache License 2.0")
                    url.set((findProperty("POM_LICENSE_URL") as String?) ?: "https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set((findProperty("POM_DEVELOPER_ID") as String?) ?: "la-manufacture-ambulante")
                    name.set((findProperty("POM_DEVELOPER_NAME") as String?) ?: "La Manufacture Ambulante")
                }
            }
            scm {
                url.set((findProperty("POM_SCM_URL") as String?) ?: "https://github.com/La-Manufacture-Ambulante/codex24-lanterna-kmp2")
                connection.set(
                    (findProperty("POM_SCM_CONNECTION") as String?)
                        ?: "scm:git:https://github.com/La-Manufacture-Ambulante/codex24-lanterna-kmp2.git",
                )
                developerConnection.set(
                    (findProperty("POM_SCM_DEV_CONNECTION") as String?)
                        ?: "scm:git:ssh://git@github.com/La-Manufacture-Ambulante/codex24-lanterna-kmp2.git",
                )
            }
        }
    }

    val ossrhUrl = System.getenv("OSSRH_URL")?.takeIf { it.isNotBlank() }
    val ossrhUsername = System.getenv("OSSRH_USERNAME")?.takeIf { it.isNotBlank() }
    val ossrhPassword = System.getenv("OSSRH_PASSWORD")?.takeIf { it.isNotBlank() }

    if (ossrhUrl != null) {
        repositories {
            maven {
                name = "OSSRH"
                url = uri(ossrhUrl)
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")?.takeIf { it.isNotBlank() }
    val signingPassword = System.getenv("SIGNING_PASSWORD")?.takeIf { it.isNotBlank() }
    val isReleaseBuild = !version.toString().endsWith("SNAPSHOT")

    setRequired { isReleaseBuild && signingKey != null && signingPassword != null }
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications)
}

tasks.register<JacocoReport>("jvmTestCoverageReport") {
    dependsOn(tasks.named("jvmTest"))

    val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")

    classDirectories.setFrom(jvmMainCompilation.output.classesDirs)
    sourceDirectories.setFrom(jvmMainCompilation.allKotlinSourceSets.map { it.kotlin.sourceDirectories })
    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")) {
            include("jvmTest.exec", "jvmTest*.exec")
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
