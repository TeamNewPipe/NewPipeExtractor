/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.timeago_generator

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

fun main() {
    val maxSeconds = 59; var currentSeconds = 0
    val maxMinutes = 59; var currentMinutes = 0
    val maxHours = 23; var currentHours = 0
    val maxDays = 6; var currentDays = 0
    val maxWeeks = 4; var currentWeeks = 0
    val maxMonths = 11; var currentMonths = 0
    val maxYears = 12; var currentYears = 0

    val categories = listOf("seconds", "minutes", "hours", "days", "weeks", "months", "years")
    val regex = Regex("\\D")

    categories.forEach { name ->
        val filePath = Path("timeago-parser/raw/times/$name.json")
        val jsonObject = Json.parseToJsonElement(
            SystemFileSystem.source(filePath).buffered().readString()
        ).jsonObject

        jsonObject.map { (key, value) -> key to value.jsonObject }.forEach { (key, value) ->
            val size = value.keys.size
            if (size >= 80) {
                when (name) {
                    "seconds" -> currentSeconds++
                    "minutes" -> currentMinutes++
                    "hours" -> currentHours++
                    "days" -> currentDays++
                    "weeks" -> currentWeeks++
                    "months" -> currentMonths++
                    "years" -> currentYears++
                }
            } else {
                System.err.println("Missing some units in: $name → $key (current size = $size)")
            }

            val number = key.replace(regex, "")
            value.forEach { (lang, langValue) ->
                val langValue = langValue.toString()
                val langValueNumber = langValue.replace(regex, "")

                if (langValueNumber != number) {
                    val msg = if (langValueNumber.isEmpty()) "doesn't contain number" else "different number"
                    System.out.printf("%-20s[!]   %22s: %10s   = %s \n", key, msg, lang, langValue)
                }
            }
        }
    }

    println("\n\nHow many:\n")

    printResult("seconds", currentSeconds, maxSeconds)
    printResult("minutes", currentMinutes, maxMinutes)
    printResult("hours", currentHours, maxHours)
    printResult("days", currentDays, maxDays)
    printResult("weeks", currentWeeks, maxWeeks)
    printResult("months", currentMonths, maxMonths)
    printResult("years", currentYears, maxYears)
}

fun printResult(category: String, current: Int, expected: Int) {
    when (current) {
        expected -> println("$category: $current")
        else -> println("[!] missing $category: $current")
    }
}
