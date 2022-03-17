package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 04.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * AudioStream.java is part of NewPipe.
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

public class AudioStream extends Stream {
    private final int averageBitrate;

    // Fields for Dash
    private int itag;
    private int bitrate;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String quality;
    private String codec;

    /**
     * Create a new audio stream
     * @param url the url
     * @param format the format
     * @param averageBitrate the average bitrate
     */
    public AudioStream(final String url,
                       final MediaFormat format,
                       final int averageBitrate) {
        super(url, format);
        this.averageBitrate = averageBitrate;
    }

    /**
     * Create a new audio stream
     * @param url the url
     * @param itag the ItagItem of the Stream
     */
    public AudioStream(final String url, final ItagItem itag) {
        this(url, itag.getMediaFormat(), itag.avgBitrate);
        this.itag = itag.id;
        this.quality = itag.getQuality();
        this.bitrate = itag.getBitrate();
        this.initStart = itag.getInitStart();
        this.initEnd = itag.getInitEnd();
        this.indexStart = itag.getIndexStart();
        this.indexEnd = itag.getIndexEnd();
        this.codec = itag.getCodec();
    }

    @Override
    public boolean equalStats(final Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream
                && averageBitrate == ((AudioStream) cmp).averageBitrate;
    }

    /**
     * Get the average bitrate
     * @return the average bitrate or -1
     */
    public int getAverageBitrate() {
        return averageBitrate;
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

    public String getQuality() {
        return quality;
    }

    public String getCodec() {
        return codec;
    }
}
