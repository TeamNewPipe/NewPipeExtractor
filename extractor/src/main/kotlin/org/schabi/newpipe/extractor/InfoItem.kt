/*
 * SPDX-FileCopyrightText: 2018-2023 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import java.io.Serializable

public abstract class InfoItem(
    public val infoType: InfoType,
    public val serviceId: Int,
    public val url: String,
    public val name: String
) : Serializable {

    public var thumbnails: List<Image> = emptyList()

    override fun toString(): String {
        return "${javaClass.simpleName}[url=\"$url\", name=\"$name\"]"
    }

    public enum class InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL,
        COMMENT
    }
}
