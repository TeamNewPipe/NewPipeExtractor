/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor.timeago

import org.schabi.newpipe.extractor.timeago.generated.patternMap

/**
 * Helper to access patterns related to languages
 */
public object PatternsManager {

    /**
     * Returns a holder containing all the patterns for a given language
     */
    @JvmStatic
    public fun getPatterns(languageCode: String, countryCode: String?): PatternsHolder? {
        return patternMap[languageCode + if (countryCode.isNullOrEmpty()) "" else "_$countryCode"]
    }
}
