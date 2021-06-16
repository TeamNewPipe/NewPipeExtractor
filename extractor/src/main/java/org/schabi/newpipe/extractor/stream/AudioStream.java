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

public class AudioStream extends Stream {
    public final int averageBitrate;

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
    public AudioStream(final String id,
                       final String url,
                       final MediaFormat format,
                       final int averageBitrate) {
        this(id, url, true, format, DeliveryMethod.PROGRESSIVE_HTTP, averageBitrate);
    }

    /**
     * Create a new audio stream
     * @param id the ID which uniquely identifies the file, e.g. for YouTube this would be the itag
     * @param content the content or URL, depending on whether isUrl is true
     * @param isUrl whether content is the URL or the actual content of e.g. a DASH manifest
     * @param format the format
     * @param deliveryMethod the delivery method
     * @param averageBitrate the average bitrate
     */
    public AudioStream(final String id,
                       final String content,
                       final boolean isUrl,
                       final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final int averageBitrate) {
        super(id, content, isUrl, format, deliveryMethod);
        this.averageBitrate = averageBitrate;
    }

    public AudioStream(final String id,
                       final String content,
                       final boolean isUrl,
                       final MediaFormat format,
                       final DeliveryMethod deliveryMethod,
                       final int averageBitrate,
                       @Nonnull final ItagItem itag) {
        super(id, content, isUrl, format, deliveryMethod);
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

    public AudioStream(final String id, final String url, @Nonnull final ItagItem itag) {
        this(id, url, itag.getMediaFormat(), itag.avgBitrate);
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
     * Get the average bitrate.
     * @return the average bitrate
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
