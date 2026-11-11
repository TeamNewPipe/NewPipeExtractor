/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.timeago_generator

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.buffered
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun main() {
    val overview = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()
    val units = listOf("seconds", "minutes", "hours", "days", "weeks", "months", "years")

    units.forEach { unit ->
        val jsonPath = Path("timeago-parser/raw/times/$unit.json")
        val jsonObject = Json.parseToJsonElement(
            SystemFileSystem.source(jsonPath).buffered().readString()
        ).jsonObject

        jsonObject.map { (_, value) -> value.jsonObject }.forEach { value ->
            value.forEach { (language, timeTranslation) ->
                overview.getOrPut(language) { mutableMapOf() }
                    .getOrPut(unit) { mutableSetOf() }
                    .add(timeTranslation.jsonPrimitive.content)
            }
        }
    }

    SystemFileSystem.sink(Path("timeago-parser/raw/overview.json")).buffered().use { sink ->
        sink.writeString(Json.encodeToString(overview))
    }
}
