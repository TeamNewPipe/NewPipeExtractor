/*
 * SPDX-FileCopyrightText: 2017-2023 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ParsingException

/**
 * The base interface for all extractors that extract [InfoItem]s.
 */
public interface InfoItemExtractor {

    /**
     * Extract the name of the [InfoItem] from the page.
     */
    @Throws(ParsingException::class)
    public fun getName(): String

    /**
     * Extract the URL of the [InfoItem] from the page.
     */
    @Throws(ParsingException::class)
    public fun getUrl(): String

    /**
     * Extract the thumbnails of the [InfoItem] from the page.
     */
    @Throws(ParsingException::class)
    public fun getThumbnails(): List<Image>
}
