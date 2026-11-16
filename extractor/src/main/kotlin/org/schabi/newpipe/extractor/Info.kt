/*
 * SPDX-FileCopyrightText: 2017-2026 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import java.io.Serializable
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.utils.ExtractorLogger

/**
 * The base class for information that is represented through an object.
 */
public abstract class Info(
    /**
     * The id of the service this Info object belongs to
     * @see StreamingService
     */
    public val serviceId: Int,

    /**
     * The id of this Info object
     *
     * e.g. YouTube: https://www.youtube.com/watch?v=RER5qCTzZ7 -> RER5qCTzZ7
     */
    public val id: String,

    /**
     * Different from the [originalUrl] in the sense that it *may* be set as a cleaned URL.
     * @see LinkHandler.getUrl
     * @see Extractor.getOriginalUrl
     */
    public val url: String,

    /**
     * The URL used to start the extraction of this [Info] object.
     * @see Extractor.getOriginalUrl
     */
    public var originalUrl: String,

    public val name: String
) : Serializable {

    public constructor(serviceId: Int, linkHandler: LinkHandler, name: String) : this(
        serviceId = serviceId,
        id = linkHandler.id,
        url = linkHandler.url,
        originalUrl = linkHandler.originalUrl,
        name = name
    )

    /**
     * A list of errors which occurred during the extraction of this [Info] object.
     */
    public val errors: List<Throwable>
        field = mutableListOf()

    public val service: StreamingService
        get() = try {
            NewPipe.getService(serviceId)
        } catch (exception: ExtractionException) {
            // this should be unreachable, as serviceId certainly refers to a valid service
            throw RuntimeException("Info object has invalid service id", exception)
        }

    init {
        ExtractorLogger.d(TAG, "Base Created {}", this)
    }

    /**
     * Add an error to the list of errors.
     */
    public fun addError(throwable: Throwable) {
        errors.add(throwable)
    }

    /**
     * Add a list of errors to the list of errors.
     */
    public fun addAllErrors(throwable: List<Throwable>) {
        errors.addAll(throwable)
    }

    override fun toString(): String {
        val ifDifferentString = if (url == originalUrl) "" else " (originalUrl=\"$originalUrl\")"
        return "${javaClass.simpleName}[url=\"$url\"$ifDifferentString, name=\"$name\"]"
    }

    private companion object {
        private const val TAG = "Info"
    }
}
