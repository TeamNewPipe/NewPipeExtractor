/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    alias(libs.plugins.google.protobuf) apply false
}

allprojects {
    apply(plugin = "java-library")

    version = "v0.25.0"

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
		options.release = 11
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
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
}

tasks.register<Javadoc>("aggregatedJavadocs") {
    group = "documentation"
    description = "Generates aggregated Javadocs for all subprojects."
    (options as StandardJavadocDocletOptions).apply {
        encoding = Charsets.UTF_8.toString()
        links = listOf("https://docs.oracle.com/javase/11/docs/api/")
        title = "NewPipe Extractor ${rootProject.version}"
        tags = listOf(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    dependsOn(subprojects.map { it.tasks.named("classes") })

    subprojects.forEach { proj ->
        proj.extensions.findByType(JavaPluginExtension::class.java)?.sourceSets?.getByName("main")?.let { main ->
            source(main.allJava)
            classpath += main.output + proj.configurations.getByName("compileClasspath")
        }
    }
}
