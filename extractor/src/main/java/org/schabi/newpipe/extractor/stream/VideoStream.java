package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 04.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * VideoStream.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

public class VideoStream extends Stream {
    public final String resolution;
    public final boolean isVideoOnly;

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

    public VideoStream(final String url, final MediaFormat format, final String resolution) {
        this(url, format, resolution, false);
    }

    public VideoStream(final String url,
                       final MediaFormat format,
                       final String resolution,
                       final boolean isVideoOnly) {
        this(url, null, format, resolution, isVideoOnly);
    }

    public VideoStream(final String url, final boolean isVideoOnly, final ItagItem itag) {
        this(url, itag.getMediaFormat(), itag.resolutionString, isVideoOnly);
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

    public VideoStream(final String url,
                       final String torrentUrl,
                       final MediaFormat format,
                       final String resolution) {
        this(url, torrentUrl, format, resolution, false);
    }

    public VideoStream(final String url,
                       final String torrentUrl,
                       final MediaFormat format,
                       final String resolution,
                       final boolean isVideoOnly) {
        super(url, torrentUrl, format);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
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
     * @return {@code true} if this stream is vid
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
