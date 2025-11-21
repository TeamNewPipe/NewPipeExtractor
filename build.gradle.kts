/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.google.protobuf) apply false
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    version = "v0.24.8"
    group = "com.github.TeamNewPipe"

    afterEvaluate {
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
        }
    }
}

subprojects {

    // sourcesJar task
    val sourcesJar by tasks.registering(Jar::class) {
        dependsOn("classes")
        archiveClassifier.set("sources")
        from(provider { the<JavaPluginExtension>().sourceSets["main"].allSource })
    }

    // Prevent .proto files ending up in JARs
    tasks.withType<Jar>().configureEach {
        exclude("**/*.proto")
        includeEmptyDirs = false
    }

    // Test logging setup
    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    // Register sources JAR as artifact
    artifacts {
        add("archives", sourcesJar)
    }
}

// https://discuss.gradle.org/t/best-approach-gradle-multi-module-project-generate-just-one-global-javadoc/18657/21
tasks.register<Javadoc>("aggregatedJavadocs") {
    title = "${project.name} ${project.version}"
    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)

    (options as StandardJavadocDocletOptions).apply {
        encoding = Charsets.UTF_8.toString()
        links = listOf("https://docs.oracle.com/javase/11/docs/api/")
        tags = listOf(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    subprojects.forEach { subProject ->
        subProject.tasks.withType<Javadoc>().forEach { javadocTask ->
            source = javadocTask.source
            classpath += javadocTask.classpath
            excludes += javadocTask.excludes
            includes += javadocTask.includes
        }
    }
}
