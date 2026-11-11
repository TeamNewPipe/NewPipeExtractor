/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor.timeago

import java.time.temporal.ChronoUnit

/**
 * Holder for patterns related to a language
 */
public abstract class PatternsHolder internal constructor(
    public val wordSeparator: String,
    public val seconds: List<String>,
    public val minutes: List<String>,
    public val hours: List<String>,
    public val days: List<String>,
    public val weeks: List<String>,
    public val months: List<String>,
    public val years: List<String>
) {
    public val specialCases: Map<ChronoUnit, Map<String, Int>>
        field = mutableMapOf<ChronoUnit, Map<String, Int>>()

    public fun asMap(): Map<ChronoUnit, List<String>> = mapOf(
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
