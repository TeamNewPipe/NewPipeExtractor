/*
 * SPDX-FileCopyrightText: 2018 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ParsingException

/**
 * Collectors are used to simplify the collection of information from extractors
 * @param I the type of the item that is collected
 * @param E the extractor type that is used to extract the item
 */
public interface Collector<I, E> {

    /**
     * The [extractor] is used to extract [I] and add it to the collection of items.
     */
    public fun commit(extractor: E)

    /**
     * Try to extract the item from an extractor without adding it to the collection
     * @throws ParsingException thrown if there is an error extracting the **required** fields
     */
    @Throws(ParsingException::class)
    public fun extract(extractor: E): I

    /**
     * Get all collected items
     */
    public fun getItems(): List<I>

    /**
     * Get all errors which occurred during the extraction of [I]s
     */
    public fun getErrors(): List<Throwable>

    /**
     * Reset all collected items and errors
     */
    public fun reset()
}
