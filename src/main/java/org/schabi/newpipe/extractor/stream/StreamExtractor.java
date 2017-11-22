package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 10.08.15.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamExtractor.java is part of NewPipe.
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

import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Scrapes information from a video streaming service (eg, YouTube).
 */
public abstract class StreamExtractor extends Extractor {

    public StreamExtractor(StreamingService service, String url) throws IOException, ExtractionException {
        super(service, url);
        fetchPage();
    }

    @Override
    protected UrlIdHandler getUrlIdHandler() throws ParsingException {
        return getService().getStreamUrlIdHandler();
    }

    public abstract String getUploadDate() throws ParsingException;
    public abstract String getThumbnailUrl() throws ParsingException;
    public abstract String getDescription() throws ParsingException;
    public abstract int getAgeLimit() throws ParsingException;

    public abstract long getLength() throws ParsingException;
    public abstract long getTimeStamp() throws ParsingException;

    public abstract long getViewCount() throws ParsingException;
    public abstract long getLikeCount() throws ParsingException;
    public abstract long getDislikeCount() throws ParsingException;

    public abstract String getUploaderUrl() throws ParsingException;
    public abstract String getUploaderName() throws ParsingException;
    public abstract String getUploaderAvatarUrl() throws ParsingException;

    public abstract String getDashMpdUrl() throws ParsingException;
    public abstract List<AudioStream> getAudioStreams() throws IOException, ExtractionException;
    public abstract List<VideoStream> getVideoStreams() throws IOException, ExtractionException;
    public abstract List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException;
    public abstract HashMap<String, String[]> getSubtitles() throws IOException, ExtractionException, JsonParserException;

    public abstract StreamType getStreamType() throws ParsingException;
    public abstract StreamInfoItem getNextVideo() throws IOException, ExtractionException;
    public abstract StreamInfoItemCollector getRelatedVideos() throws IOException, ExtractionException;

    /**
     * Analyses the webpage's document and extracts any error message there might be.
     *
     * @return Error message; null if there is no error message.
     */
    public abstract String getErrorMessage();
}
