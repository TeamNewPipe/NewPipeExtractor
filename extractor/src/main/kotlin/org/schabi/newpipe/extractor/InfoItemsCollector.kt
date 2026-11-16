/*
 * SPDX-FileCopyrightText: 2018-2023 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import java.util.Collections
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException

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

    protected fun addError(error: Exception) {
        errors.add(error)
    }

    protected fun addItem(item: I) {
        itemList.add(item)
    }

    override fun commit(extractor: E) {
        try {
            addItem(extract(extractor))
        } catch (_: FoundAdException) {
            // found an ad. Maybe a debug line could be placed here
        } catch (exception: ParsingException) {
            addError(exception)
        }
    }
}
