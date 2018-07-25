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

import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.Subtitles;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Scrapes information from a video streaming service (eg, YouTube).
 */
public abstract class StreamExtractor extends Extractor {

    public static final int NO_AGE_LIMIT = 0;

    public StreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    public abstract String getUploadDate() throws ParsingException;
    @Nonnull
    public abstract String getThumbnailUrl() throws ParsingException;
    @Nonnull
    public abstract String getDescription() throws ParsingException;

    /**
     * Get the age limit
     * @return The age which limits the content or {@value NO_AGE_LIMIT} if there is no limit
     * @throws ParsingException if an error occurs while parsing
     */
    public abstract int getAgeLimit() throws ParsingException;

    public abstract long getLength() throws ParsingException;
    public abstract long getTimeStamp() throws ParsingException;
    protected long getTimestampSeconds(String regexPattern) throws ParsingException {
        String timeStamp;
        try {
            timeStamp = Parser.matchGroup1(regexPattern, getOriginalUrl());
        } catch (Parser.RegexException e) {
            // catch this instantly since an url does not necessarily have to have a time stamp

            // -2 because well the testing system will then know its the regex that failed :/
            // not good i know
            return -2;
        }

        if (!timeStamp.isEmpty()) {
            try {
                String secondsString = "";
                String minutesString = "";
                String hoursString = "";
                try {
                    secondsString = Parser.matchGroup1("(\\d{1,3})s", timeStamp);
                    minutesString = Parser.matchGroup1("(\\d{1,3})m", timeStamp);
                    hoursString = Parser.matchGroup1("(\\d{1,3})h", timeStamp);
                } catch (Exception e) {
                    //it could be that time is given in another method
                    if (secondsString.isEmpty() //if nothing was got,
                            && minutesString.isEmpty()//treat as unlabelled seconds
                            && hoursString.isEmpty()) {
                        secondsString = Parser.matchGroup1("t=(\\d+)", timeStamp);
                    }
                }

                int seconds = secondsString.isEmpty() ? 0 : Integer.parseInt(secondsString);
                int minutes = minutesString.isEmpty() ? 0 : Integer.parseInt(minutesString);
                int hours = hoursString.isEmpty() ? 0 : Integer.parseInt(hoursString);

                //don't trust BODMAS!
                return seconds + (60 * minutes) + (3600 * hours);
                //Log.d(TAG, "derived timestamp value:"+ret);
                //the ordering varies internationally
            } catch (ParsingException e) {
                throw new ParsingException("Could not get timestamp.", e);
            }
        } else {
            return 0;
        }};

    public abstract long getViewCount() throws ParsingException;
    public abstract long getLikeCount() throws ParsingException;
    public abstract long getDislikeCount() throws ParsingException;

    @Nonnull
    public abstract String getUploaderUrl() throws ParsingException;
    @Nonnull
    public abstract String getUploaderName() throws ParsingException;
    @Nonnull
    public abstract String getUploaderAvatarUrl() throws ParsingException;

    /**
     * Get the dash mpd url
     * @return the url as a string or an empty string
     * @throws ParsingException if an error occurs while reading
     */
    @Nonnull public abstract String getDashMpdUrl() throws ParsingException;
    @Nonnull public abstract String getHlsUrl() throws ParsingException;
    public abstract List<AudioStream> getAudioStreams() throws IOException, ExtractionException;
    public abstract List<VideoStream> getVideoStreams() throws IOException, ExtractionException;
    public abstract List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException;

    @Nonnull
    public abstract List<Subtitles> getSubtitlesDefault() throws IOException, ExtractionException;
    @Nonnull
    public abstract List<Subtitles> getSubtitles(SubtitlesFormat format) throws IOException, ExtractionException;

    public abstract StreamType getStreamType() throws ParsingException;
    public abstract StreamInfoItem getNextVideo() throws IOException, ExtractionException;
    public abstract StreamInfoItemsCollector getRelatedVideos() throws IOException, ExtractionException;

    /**
     * Analyses the webpage's document and extracts any error message there might be.
     *
     * @return Error message; null if there is no error message.
     */
    public abstract String getErrorMessage();
}
