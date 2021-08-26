package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 04.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * VideoStream.java is part of NewPipe Extractor.
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

public class VideoStream extends Stream {
    private final String resolution;
    private final boolean isVideoOnly;
    // Fields for DASH
    private int itag;
    private int bitrate;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private int width;
    private int height;
    private int fps;
    private String quality;
    private String codec;
    @Nullable private ItagItem itagItem;

    /**
     * Create a new video stream.
     *
     * @param id          the ID which uniquely identifies the stream, e.g. for YouTube this would
     *                    be the itag
     * @param url         the URL of the stream
     * @param format      the {@link MediaFormat} used by the stream, which can be null
     * @param resolution  the resolution of the stream
     * @param isVideoOnly whether the stream is video-only
     */
    public VideoStream(final String id,
                       final String url,
                       @Nullable final MediaFormat format,
                       final String resolution,
                       final boolean isVideoOnly) {
        this(id, url, true, format, DeliveryMethod.PROGRESSIVE_HTTP, resolution, isVideoOnly,
                null);
    }

    /**
     * Create a new video stream.
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param content        the content or the URL of the stream, depending on whether isUrl is
     *                       true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat} used by the stream, which can be null
     * @param deliveryMethod the {@link DeliveryMethod} of the stream
     * @param resolution     the resolution of the stream
     * @param isVideoOnly    whether the stream is video-only
     * @param baseUrl        the base URL of the stream (see {@link Stream#getBaseUrl()} for more
     *                       information)
     */
    public VideoStream(final String id,
                       final String content,
                       final boolean isUrl,
                       @Nullable final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final String resolution,
                       final boolean isVideoOnly,
                       @Nullable final String baseUrl) {
        super(id, content, isUrl, format, deliveryMethod, baseUrl);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
    }

    /**
     * Create a new video stream.
     *
     * @param id             the ID which uniquely identifies the stream, e.g. for YouTube this
     *                       would be the itag
     * @param content        the content or the URL of the stream, depending on whether isUrl is
     *                       true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat} used by the stream, which can be null
     * @param deliveryMethod the {@link DeliveryMethod} of the stream
     * @param resolution     the resolution of the stream
     * @param isVideoOnly    whether the stream is video-only
     * @param itag           the {@link ItagItem} corresponding to the stream, which cannot be null
     * @param baseUrl        the base URL of the stream (see {@link Stream#getBaseUrl()} for more
     *                       information)
     */
    public VideoStream(final String id,
                       final String content,
                       final boolean isUrl,
                       @Nullable final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final String resolution,
                       final boolean isVideoOnly,
                       @Nonnull final ItagItem itag,
                       @Nullable final String baseUrl) {
        super(id, content, isUrl, format, deliveryMethod, baseUrl);
        this.itagItem = itag;
        this.itag = itag.id;
        this.bitrate = itag.getBitrate();
        this.initStart = itag.getInitStart();
        this.initEnd = itag.getInitEnd();
        this.indexStart = itag.getIndexStart();
        this.indexEnd = itag.getIndexEnd();
        this.codec = itag.getCodec();
        this.height = itag.getHeight();
        this.width = itag.getWidth();
        this.quality = itag.getQuality();
        this.fps = itag.fps;
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
    }

    /**
     * Create a new video stream.
     * <p>
     * The media format and the resolution will be set by using respectively
     * {@link ItagItem#getMediaFormat()} and {@link ItagItem#resolutionString}.
     * </p>
     *
     * @param id          the ID which uniquely identifies the stream, e.g. for YouTube this would
     *                    be the itag
     * @param url         the URL of the stream
     * @param isVideoOnly whether the stream is video-only
     * @param itag        the {@link ItagItem} corresponding to the stream, which cannot be null
     */
    public VideoStream(final String id,
                       final String url,
                       final boolean isVideoOnly,
                       @Nonnull final ItagItem itag) {
        this(id, url, itag.getMediaFormat(), itag.resolutionString, isVideoOnly);
        this.itagItem = itag;
        this.itag = itag.id;
        this.bitrate = itag.getBitrate();
        this.initStart = itag.getInitStart();
        this.initEnd = itag.getInitEnd();
        this.indexStart = itag.getIndexStart();
        this.indexEnd = itag.getIndexEnd();
        this.codec = itag.getCodec();
        this.height = itag.getHeight();
        this.width = itag.getWidth();
        this.quality = itag.getQuality();
        this.fps = itag.fps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalStats(final Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof VideoStream
                && resolution.equals(((VideoStream) cmp).resolution)
                && isVideoOnly == ((VideoStream) cmp).isVideoOnly;
    }

    /**
     * Get the video resolution.
     *
     * @return the video resolution
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Return if the stream is video-only.
     * <p>
     * Video-only streams have no audio.
     * </p>
     *
     * @return {@code true} if this stream is video-only, {@code false} otherwise
     */
    public boolean isVideoOnly() {
        return isVideoOnly;
    }

    /**
     * Get the itag of the stream.
     *
     * @return the number of the {@link ItagItem} passed in the constructor of the stream.
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
     * constructor of the
     * stream.
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
     * Get the width of the video stream.
     *
     * @return the width set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the video stream.
     *
     * @return the height set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the frames per second of the video stream.
     *
     * @return the frames per second set from the {@link ItagItem} passed in the constructor of the
     * stream.
     */
    public int getFps() {
        return fps;
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
}
