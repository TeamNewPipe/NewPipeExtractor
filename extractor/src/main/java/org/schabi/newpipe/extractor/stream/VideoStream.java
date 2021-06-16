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

public class VideoStream extends Stream {
    private final String resolution;
    private final boolean isVideoOnly;

    // Fields for Dash
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

    public VideoStream(final String id,
                       final String url,
                       final MediaFormat format,
                       final String resolution,
                       final boolean isVideoOnly) {
        this(id, url, true, format, DeliveryMethod.PROGRESSIVE_HTTP, resolution, isVideoOnly);
    }

    public VideoStream(final String id,
                       final String content,
                       final boolean isUrl,
                       final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final String resolution,
                       final boolean isVideoOnly) {
        super(id, content, isUrl, format, deliveryMethod);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
    }

    public VideoStream(final String id,
                       final String content,
                       final boolean isUrl,
                       final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final String resolution,
                       final boolean isVideoOnly,
                       @Nonnull final ItagItem itag) {
        super(id, content, isUrl, format, deliveryMethod);
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

    public VideoStream(final String id,
                       final String url,
                       final boolean isVideoOnly,
                       @Nonnull final ItagItem itag) {
        this(id, url, itag.getMediaFormat(), itag.resolutionString, isVideoOnly);
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

    @Override
    public boolean equalStats(final Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof VideoStream
                && resolution.equals(((VideoStream) cmp).resolution)
                && isVideoOnly == ((VideoStream) cmp).isVideoOnly;
    }

    /**
     * Get the video resolution
     *
     * @return the video resolution
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Check if the video is video only.
     * <p>
     * Video only streams have no audio
     *
     * @return {@code true} if this stream is video only
     */
    public boolean isVideoOnly() {
        return isVideoOnly;
    }

    public int getItag() {
        return itag;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getInitStart() {
        return initStart;
    }

    public int getInitEnd() {
        return initEnd;
    }

    public int getIndexStart() {
        return indexStart;
    }

    public int getIndexEnd() {
        return indexEnd;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public String getQuality() {
        return quality;
    }

    public String getCodec() {
        return codec;
    }
}
