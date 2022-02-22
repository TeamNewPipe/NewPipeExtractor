package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 04.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * AudioStream.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class AudioStream extends Stream {
    private static final long serialVersionUID = 271188551264661410L;

    public static final int UNKNOWN_BITRATE = -1;

    /**
     * An integer to represent that the itag id returned is not available (only for YouTube, this
     * should never happen) or not applicable (for other services than YouTube).
     *
     * <p>
     * An itag should not have a negative value so {@code -1} is used for this constant.
     * </p>
     */
    public static final int ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE = -1;

    /** @deprecated Use {@link #getAverageBitrate()} instead. */
    @Deprecated
    public final int averageBitrate;

    // Fields for DASH
    private int itag = ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE;
    private int bitrate;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String quality;
    private String codec;
    @Nullable private ItagItem itagItem;

    /**
     * Create a new audio stream.
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param url            the URL of the stream
     * @param format         the {@link MediaFormat} used by the stream, which can be null
     * @param averageBitrate the average bitrate of the stream (which can be unknown, see
     *                       {@link #UNKNOWN_BITRATE})
     */
    public AudioStream(final String id,
                       final String url,
                       @Nullable final MediaFormat format,
                       final int averageBitrate) {
        this(id, url, true, format, DeliveryMethod.PROGRESSIVE_HTTP, averageBitrate);
    }

    /**
     * Create a new audio stream.
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param content        the content or the URL of the stream, depending on whether isUrl is
     *                       true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat} used by the stream, which can be null
     * @param deliveryMethod the {@link DeliveryMethod} of the stream
     * @param averageBitrate the average bitrate of the stream (which can be unknown, see
     *                       {@link #UNKNOWN_BITRATE})
     */
    public AudioStream(final String id,
                       final String content,
                       final boolean isUrl,
                       @Nullable final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final int averageBitrate) {
        super(id, content, isUrl, format, deliveryMethod, null);
        this.averageBitrate = averageBitrate;
    }

    /**
     * Create a new audio stream.
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param content        the content or the URL of the stream, depending on whether isUrl is
     *                       true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat} used by the stream, which can be null
     * @param deliveryMethod the {@link DeliveryMethod} of the stream
     * @param averageBitrate the average bitrate of the stream (which can be unknown, see
     *                       {@link #UNKNOWN_BITRATE})
     * @param itag           the {@link ItagItem} corresponding to the stream, which cannot be null
     * @param baseUrl        the base URL of the stream (see {@link Stream#getBaseUrl()} for more
     *                       information)
     */
    public AudioStream(final String id,
                       final String content,
                       final boolean isUrl,
                       @Nullable final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final int averageBitrate,
                       @Nonnull final ItagItem itag,
                       @Nullable final String baseUrl) {
        super(id, content, isUrl, format, deliveryMethod, baseUrl);
        this.itagItem = itag;
        this.itag = itag.id;
        this.quality = itag.getQuality();
        this.bitrate = itag.getBitrate();
        this.initStart = itag.getInitStart();
        this.initEnd = itag.getInitEnd();
        this.indexStart = itag.getIndexStart();
        this.indexEnd = itag.getIndexEnd();
        this.codec = itag.getCodec();
        this.averageBitrate = averageBitrate;
    }

    /**
     * Create a new audio stream.
     * <p>
     * The average bitrate will be set by using {@link ItagItem#avgBitrate}.
     * </p>
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param url            the URL of the stream
     * @param itag           the {@link ItagItem} corresponding to the stream, which cannot be null
     */
    public AudioStream(final String id,
                       final String url,
                       @Nonnull final ItagItem itag) {
        this(id, url, itag.getMediaFormat(), itag.avgBitrate);
        this.itagItem = itag;
        this.itag = itag.id;
        this.quality = itag.getQuality();
        this.bitrate = itag.getBitrate();
        this.initStart = itag.getInitStart();
        this.initEnd = itag.getInitEnd();
        this.indexStart = itag.getIndexStart();
        this.indexEnd = itag.getIndexEnd();
        this.codec = itag.getCodec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalStats(final Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream
                && averageBitrate == ((AudioStream) cmp).averageBitrate;
    }

    /**
     * Get the average bitrate of the stream.
     *
     * @return the average bitrate or {@link #UNKNOWN_BITRATE} if it is unknown
     */
    public int getAverageBitrate() {
        return averageBitrate;
    }

    /**
     * Get the itag identifier of the stream.
     *
     * <p>
     * Always equals to {@link #ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE} for other streams than the
     * ones of the YouTube service.
     * </p>
     *
     * @return the number of the {@link ItagItem} passed in the constructor of the audio stream.
     */
    public int getItag() {
        return itag;
    }

    /**
     * Get the bitrate of the stream.
     *
     * @return the bitrate set from the {@link ItagItem} passed in the constructor of the stream.
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Get the initialization start of the stream.
     *
     * @return the initialization start value set from the {@link ItagItem} passed in the
     * constructor of the stream.
     */
    public int getInitStart() {
        return initStart;
    }

    /**
     * Get the initialization end of the stream.
     *
     * @return the initialization end value set from the {@link ItagItem} passed in the constructor
     * of the stream.
     */
    public int getInitEnd() {
        return initEnd;
    }

    /**
     * Get the index start of the stream.
     *
     * @return the index start value set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getIndexStart() {
        return indexStart;
    }

    /**
     * Get the index end of the stream.
     *
     * @return the index end value set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getIndexEnd() {
        return indexEnd;
    }

    /**
     * Get the quality of the stream.
     *
     * @return the quality label set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public String getQuality() {
        return quality;
    }

    /**
     * Get the codec of the stream.
     *
     * @return the codec set from the {@link ItagItem} passed in the constructor of the stream.
     */
    public String getCodec() {
        return codec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ItagItem getItagItem() {
        return itagItem;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        final AudioStream audioStream = (AudioStream) obj;
        return averageBitrate == audioStream.averageBitrate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), averageBitrate);
    }
}
