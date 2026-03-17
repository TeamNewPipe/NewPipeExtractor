/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val ciSigningKey: String? = System.getenv("PGP_PRIVATE_SIGNING_KEY")
val ciSigningPassword: String? = System.getenv("PGP_PRIVATE_SIGNING_KEY_PASSWORD")
val shouldSignCIRelease: Boolean
    get() = !ciSigningKey.isNullOrEmpty() && !ciSigningPassword.isNullOrEmpty()

val lastCommitHash: String = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }.get()

plugins {
    alias(libs.plugins.google.protobuf)
    checkstyle
    `maven-publish`
    signing
}

java {
    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    main {
        java.srcDir("../timeago-parser/src/main/java")
    }
}

// Protobuf files would uselessly end up in the JAR otherwise, see
// https://github.com/google/protobuf-gradle-plugin/issues/390
tasks.jar {
    exclude("**/*.proto")
    includeEmptyDirs = false
}

tasks.test {
    // Test logging setup
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }

    // Pass on downloader type to tests for different CI jobs. See DownloaderFactory.java and ci.yml
    if (System.getProperties().containsKey("downloader")) {
        systemProperty("downloader", System.getProperty("downloader"))
    }
    useJUnitPlatform()
    dependsOn(tasks.checkstyleMain) // run checkstyle when testing
}

// https://checkstyle.org/#JRE_and_JDK
tasks.withType<Checkstyle>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

checkstyle {
    configDirectory = rootProject.file("checkstyle")
    isIgnoreFailures = false
    isShowViolations = true
    toolVersion = libs.versions.checkstyle.get()
}

// Exclude Protobuf generated files from Checkstyle
tasks.checkstyleMain {
    exclude(
        "org/schabi/newpipe/extractor/services/youtube/protos",
        "org/schabi/newpipe/extractor/timeago"
    )
}

tasks.checkstyleTest {
    isEnabled = false // do not checkstyle test files
}

dependencies {
    implementation(libs.newpipe.nanojson)
    implementation(libs.jsoup)
    implementation(libs.google.jsr305)
    implementation(libs.google.protobuf)

    implementation(libs.mozilla.rhino.core)
    implementation(libs.mozilla.rhino.engine)

    checkstyle(libs.puppycrawl.checkstyle)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)

    testImplementation(libs.squareup.okhttp)
    testImplementation(libs.google.gson)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.lib.get()}"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                named("java") {
                    option("lite")
                }
            }
        }
    }
}

// Run "./gradlew publishReleasePublicationToLocalRepository" to generate release JARs locally
publishing {
    publications {
        val mavenGroupId = "net.newpipe"
        val mavenArtifactId = "extractor"
        fun MavenPublication.setupPOM() = pom {
            name = "NewPipe Extractor"
            description = "A library for extracting data from streaming websites, used in NewPipe"
            url = "https://github.com/TeamNewPipe/NewPipeExtractor"

            licenses {
                license {
                    name = "GNU GENERAL PUBLIC LICENSE, Version 3"
                    url = "https://www.gnu.org/licenses/gpl-3.0.txt"
                }
            }

            scm {
                url = "https://github.com/TeamNewPipe/NewPipeExtractor"
                connection = "scm:git:git@github.com:TeamNewPipe/NewPipeExtractor.git"
                developerConnection = "scm:git:git@github.com:TeamNewPipe/NewPipeExtractor.git"
            }

            developers {
                developer {
                    id = "newpipe"
                    name = "Team NewPipe"
                    email = "team@newpipe.net"
                }
            }
        }

        create<MavenPublication>("release") {
            groupId = mavenGroupId
            artifactId = mavenArtifactId
            version = rootProject.version.toString()

            afterEvaluate {
                from(components["java"])
            }

            setupPOM()
        }
        create<MavenPublication>("snapshot") {
            groupId = mavenGroupId
            artifactId = mavenArtifactId
            version = "$lastCommitHash-SNAPSHOT"

            afterEvaluate {
                from(components["java"])
            }

            setupPOM()
        }
        repositories {
            maven {
                name = "sonatype"
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                credentials {
                    username = System.getenv("SONATYPE_MAVEN_CENTRAL_USERNAME")
                    password = System.getenv("SONATYPE_MAVEN_CENTRAL_PASSWORD")
                }
            }
            maven {
                name = "local"
                url = uri(layout.buildDirectory.dir("maven"))
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(ciSigningKey, ciSigningPassword)
    sign(publishing.publications["snapshot"])
}

tasks.withType<Sign> {
    onlyIf("Signing credentials are present (only used for maven central)") { shouldSignCIRelease }
}
