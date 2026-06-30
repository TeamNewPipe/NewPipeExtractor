/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.newpipe.nanojson)
    implementation(libs.google.jsr305)
    implementation(libs.jetbrains.kotlin.io)
    implementation(libs.jetbrains.kotlin.serialization.json)
}
