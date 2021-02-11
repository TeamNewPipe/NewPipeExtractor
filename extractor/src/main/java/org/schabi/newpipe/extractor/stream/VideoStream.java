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

public class VideoStream extends Stream {
    public final String resolution;
    public final boolean isVideoOnly;

    // Fields for Dash
    public int bitrate;
    public int initStart;
    public int initEnd;
    public int indexStart;
    public int indexEnd;
    public int width;
    public int height;
    public String codec;

    public VideoStream(String url, MediaFormat format, String resolution) {
        this(url, format, resolution, false);
    }

    public VideoStream(String url, MediaFormat format, String resolution, boolean isVideoOnly) {
        super(url, format);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
    }

    public VideoStream(String url, MediaFormat format, String resolution, boolean isVideoOnly, int bitrate, int initStart, int initEnd, int indexStart, int indexEnd, String codec, int width, int height) {
        super(url, format);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
        this.bitrate = bitrate;
        this.initStart = initStart;
        this.initEnd = initEnd;
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
        this.codec = codec;
        this.height = height;
        this.width = width;
    }

    public VideoStream(String url, String torrentUrl, MediaFormat format, String resolution) {
        this(url, torrentUrl, format, resolution, false);
    }

    public VideoStream(String url, String torrentUrl, MediaFormat format, String resolution, boolean isVideoOnly) {
        super(url, torrentUrl, format);
        this.resolution = resolution;
        this.isVideoOnly = isVideoOnly;
    }

    @Override
    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof VideoStream &&
                resolution.equals(((VideoStream) cmp).resolution) &&
                isVideoOnly == ((VideoStream) cmp).isVideoOnly;
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

    public String getCodec() {
        return codec;
    }
}
