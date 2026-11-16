/*
 * SPDX-FileCopyrightText: 2017-2023 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import java.util.Collections
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.ExtractorLogger

public abstract class InfoItemsCollector<I : InfoItem, E : InfoItemExtractor> @JvmOverloads constructor(
    public val serviceId: Int,
    public val comparator: Comparator<I>? = null
) : Collector<I, E> {

    private val itemList = mutableListOf<I>()
    private val errors = mutableListOf<Throwable>()

    override fun getItems(): List<I> {
        if (comparator != null) {
            itemList.sortWith(comparator)
        }
        return Collections.unmodifiableList(itemList)
    }

    override fun getErrors(): List<Throwable> {
        return errors
    }

    override fun reset() {
        itemList.clear()
        errors.clear()
    }

    /**
     * Add an error to the list of errors which occurred during the extraction of [InfoItem]s.
     */
    protected fun addError(error: Exception) {
        errors.add(error)
    }

    /**
     * Add an [InfoItem] to the list of collected items.
     */
    protected fun addItem(item: I) {
        itemList.add(item)
    }

    override fun commit(extractor: E) {
        try {
            addItem(extract(extractor))
        } catch (exception: FoundAdException) {
            ExtractorLogger.d(TAG, "FoundAdvertisement {}", exception)
        } catch (exception: ParsingException) {
            addError(exception)
        }
    }

    private companion object {
        private const val TAG = "InfoItemsCollector"
    }
}
