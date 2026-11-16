/*
 * SPDX-FileCopyrightText: 2018 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ParsingException

/**
 * Collectors are used to simplify the collection of information from extractors
 * @param I Item's type
 * @param E Extractor's type
 */
public interface Collector<I, E> {

    /**
     * Try to add an extractor to the collection
     */
    public fun commit(extractor: E)

    /**
     * Try to extract the item from an extractor without adding it to the collection
     * @throws ParsingException thrown if there is an error extracting the **required** fields
     */
    @Throws(ParsingException::class)
    public fun extract(extractor: E): I

    /**
     * Get all items
     */
    public fun getItems(): List<I>

    /**
     * Get all errors
     */
    public fun getErrors(): List<Throwable>

    /**
     * Reset all collected items and errors
     */
    public fun reset()
}
