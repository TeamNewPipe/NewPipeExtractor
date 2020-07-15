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

public class AudioStream extends Stream {
    private final int averageBitrate;

    /**
     * Create a new audio stream
     * @param id the ID which uniquely identifies the file, e.g. for YouTube this would be the itag
     * @param url the URL
     * @param format the format
     * @param averageBitrate the average bitrate
     */
    public AudioStream(final String id, final String url, final MediaFormat format, final int averageBitrate) {
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
    public AudioStream(final String id, final String content, final boolean isUrl, final MediaFormat format, final DeliveryMethod deliveryMethod, final int averageBitrate) {
        super(id, content, isUrl, format, deliveryMethod);
        this.averageBitrate = averageBitrate;
    }

    @Override
    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream &&
                averageBitrate == ((AudioStream) cmp).averageBitrate;
    }

    /**
     * Get the average bitrate
     * @return the average bitrate or -1
     */
    public int getAverageBitrate() {
        return averageBitrate;
    }
}
