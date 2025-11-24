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

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}

subprojects {
    // https://discuss.gradle.org/t/best-approach-gradle-multi-module-project-generate-just-one-global-javadoc/18657/21
    // Fixes unknown tag @implNote; the other two were added precautionary
    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).apply {
            encoding = Charsets.UTF_8.toString()
            links = listOf("https://docs.oracle.com/javase/11/docs/api/")
            tags = listOf(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    // Test logging setup
    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
