/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor.timeago

import java.time.temporal.ChronoUnit

/**
 * Holder for patterns related to a language
 */
abstract class PatternsHolder internal constructor(
    val wordSeparator: String,
    val seconds: List<String>,
    val minutes: List<String>,
    val hours: List<String>,
    val days: List<String>,
    val weeks: List<String>,
    val months: List<String>,
    val years: List<String>
) {
    val specialCases: Map<ChronoUnit, Map<String, Int>>
        field = mutableMapOf<ChronoUnit, Map<String, Int>>()

    fun asMap() = mapOf(
        ChronoUnit.SECONDS to seconds,
        ChronoUnit.MINUTES to minutes,
        ChronoUnit.HOURS to hours,
        ChronoUnit.DAYS to days,
        ChronoUnit.WEEKS to weeks,
        ChronoUnit.MONTHS to months,
        ChronoUnit.YEARS to years
    )

    protected fun putSpecialCase(unit: ChronoUnit, caseText: String, caseAmount: Int) {
        specialCases.putIfAbsent(unit, mapOf(caseText to caseAmount))
    }
}
