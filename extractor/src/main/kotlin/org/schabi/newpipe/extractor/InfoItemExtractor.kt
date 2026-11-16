/*
 * SPDX-FileCopyrightText: 2018-2023 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ParsingException

public interface InfoItemExtractor {
    @Throws(ParsingException::class)
    public fun getName(): String

    @Throws(ParsingException::class)
    public fun getUrl(): String

    @Throws(ParsingException::class)
    public fun getThumbnails(): List<Image>
}
