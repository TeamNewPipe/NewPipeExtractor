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
    public int average_bitrate = -1;

    // Fields for Dash
    private int bitrate;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String codec;

    /**
     * Create a new audio stream
     * @param url the url
     * @param format the format
     * @param averageBitrate the average bitrate
     */
    public AudioStream(String url, MediaFormat format, int averageBitrate) {
        super(url, format);
        this.average_bitrate = averageBitrate;
    }

    /**
     * Create a new audio stream
     * @param url the url
     * @param itag the ItagItem to use
     */
    public AudioStream(String url, ItagItem itag) {
        super(url, itag.getMediaFormat());
        this.average_bitrate = itag.avgBitrate;
        this.bitrate = itag.bitrate;
        this.initStart = itag.initStart;
        this.initEnd = itag.initEnd;
        this.indexStart = itag.indexStart;
        this.indexEnd = itag.indexEnd;
        this.codec = itag.codec;
    }

    @Override
    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream &&
                average_bitrate == ((AudioStream) cmp).average_bitrate;
    }

    /**
     * Get the average bitrate
     * @return the average bitrate or -1
     */
    public int getAverageBitrate() {
        return average_bitrate;
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

    public String getCodec() {
        return codec;
    }
}
