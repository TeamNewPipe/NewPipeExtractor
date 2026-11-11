/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.timeago_generator

import kotlinx.serialization.json.Json

/**
 * Reusable configuration for JSON serialization
 */
internal val Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = true
}
