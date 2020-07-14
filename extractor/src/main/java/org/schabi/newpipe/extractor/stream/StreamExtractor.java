package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 10.08.18.
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
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Scrapes information from a video/audio streaming service (eg, YouTube).
 */
public abstract class StreamExtractor extends Extractor {

    public static final int NO_AGE_LIMIT = 0;

    public StreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    /**
     * The original textual date provided by the service. Should be used as a fallback if
     * {@link #getUploadDate()} isn't provided by the service, or it fails for some reason.
     *
     * <p>If the stream is a live stream, {@code null} should be returned.</p>
     *
     * @return The original textual date provided by the service, or {@code null}.
     * @throws ParsingException if there is an error in the extraction
     * @see #getUploadDate()
     */
    @Nullable
    public abstract String getTextualUploadDate() throws ParsingException;

    /**
     * A more general {@code Calendar} instance set to the date provided by the service.<br>
     * Implementations usually will just parse the date returned from the {@link #getTextualUploadDate()}.
     *
     * <p>If the stream is a live stream, {@code null} should be returned.</p>
     *
     * @return The date this item was uploaded, or {@code null}.
     * @throws ParsingException if there is an error in the extraction
     *                          or the extracted date couldn't be parsed.
     * @see #getTextualUploadDate()
     */
    @Nullable
    public abstract DateWrapper getUploadDate() throws ParsingException;

    /**
     * This will return the url to the thumbnail of the stream. Try to return the medium resolution here.
     *
     * @return The url of the thumbnail.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getThumbnailUrl() throws ParsingException;

    /**
     * This is the stream description.
     *
     * @return The description of the stream/video or Description.emptyDescription if the description is empty.
     * @throws ParsingException
     */
    @Nonnull
    public abstract Description getDescription() throws ParsingException;

    /**
     * Get the age limit.
     *
     * @return The age which limits the content or {@value NO_AGE_LIMIT} if there is no limit
     * @throws ParsingException if an error occurs while parsing
     */
    public abstract int getAgeLimit() throws ParsingException;

    /**
     * This should return the length of a video in seconds.
     *
     * @return The length of the stream in seconds.
     * @throws ParsingException
     */
    public abstract long getLength() throws ParsingException;

    /**
     * If the url you are currently handling contains a time stamp/seek, you can return the
     * position it represents here.
     * If the url has no time stamp simply return zero.
     *
     * @return the timestamp in seconds
     * @throws ParsingException
     */
    public abstract long getTimeStamp() throws ParsingException;

    /**
     * The count of how many people have watched the video/listened to the audio stream.
     * If the current stream has no view count or its not available simply return -1
     *
     * @return amount of views.
     * @throws ParsingException
     */
    public abstract long getViewCount() throws ParsingException;

    /**
     * The amount of likes a video/audio stream got.
     * If the current stream has no likes or its not available simply return -1
     *
     * @return the amount of likes the stream got
     * @throws ParsingException
     */
    public abstract long getLikeCount() throws ParsingException;

    /**
     * The amount of dislikes a video/audio stream got.
     * If the current stream has no dislikes or its not available simply return -1
     *
     * @return the amount of likes the stream got
     * @throws ParsingException
     */
    public abstract long getDislikeCount() throws ParsingException;

    /**
     * The Url to the page of the creator/uploader of the stream. This must not be a homepage,
     * but the page offered by the service the extractor handles. This url will be handled by the
     * {@link ChannelExtractor},
     * so be sure to implement that one before you return a value here, otherwise NewPipe will crash if one selects
     * this url.
     *
     * @return the url to the page of the creator/uploader of the stream or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getUploaderUrl() throws ParsingException;

    /**
     * The name of the creator/uploader of the stream.
     * If the name is not available you can simply return an empty string.
     *
     * @return the name of the creator/uploader of the stream or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getUploaderName() throws ParsingException;

    /**
     * The url to the image file/profile picture/avatar of the creator/uploader of the stream.
     * If the url is not available you can return an empty String.
     *
     * @return The url of the image file of the uploader or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getUploaderAvatarUrl() throws ParsingException;

    /**
     * The Url to the page of the sub-channel of the stream. This must not be a homepage,
     * but the page offered by the service the extractor handles. This url will be handled by the
     * {@link ChannelExtractor},
     * so be sure to implement that one before you return a value here, otherwise NewPipe will crash if one selects
     * this url.
     *
     * @return the url to the page of the sub-channel of the stream or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getSubChannelUrl() throws ParsingException;

    /**
     * The name of the sub-channel of the stream.
     * If the name is not available you can simply return an empty string.
     *
     * @return the name of the sub-channel of the stream or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getSubChannelName() throws ParsingException;

    /**
     * The url to the image file/profile picture/avatar of the sub-channel of the stream.
     * If the url is not available you can return an empty String.
     *
     * @return The url of the image file of the sub-channel or an empty String
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getSubChannelAvatarUrl() throws ParsingException;

    /**
     * Get the dash mpd url. If you don't know what a dash MPD is you can read about it
     * <a href="https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html">here</a>.
     *
     * @return the url as a string or an empty string
     * @throws ParsingException if an error occurs while reading
     */
    @Nonnull
    public abstract String getDashMpdUrl() throws ParsingException;

    /**
     * I am not sure if this is in use, and how this is used. However the frontend is missing support
     * for HLS streams. Prove me if I am wrong. Please open an
     * <a href="https://github.com/teamnewpipe/newpipe/issues">issue</a>,
     * or fix this description if you know whats up with this.
     *
     * @return The Url to the hls stream.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getHlsUrl() throws ParsingException;

    /**
     * This should return a list of available
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/stream/AudioStream.html">AudioStream</a>s
     * You can also return null or an empty list, however be aware that if you don't return anything
     * in getVideoStreams(), getVideoOnlyStreams() and getDashMpdUrl() either the Collector will handle this as
     * a failed extraction procedure.
     *
     * @return a list of audio only streams in the format of AudioStream
     * @throws IOException
     * @throws ExtractionException
     */
    public abstract List<AudioStream> getAudioStreams() throws IOException, ExtractionException;

    /**
     * This should return a list of available
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/stream/VideoStream.html">VideoStream</a>s
     * Be aware this is the list of video streams which do contain an audio stream.
     * You can also return null or an empty list, however be aware that if you don't return anything
     * in getAudioStreams(), getVideoOnlyStreams() and getDashMpdUrl() either the Collector will handle this as
     * a failed extraction procedure.
     *
     * @return a list of combined video and streams in the format of AudioStream
     * @throws IOException
     * @throws ExtractionException
     */
    public abstract List<VideoStream> getVideoStreams() throws IOException, ExtractionException;

    /**
     * This should return a list of available
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/stream/VideoStream.html">VideoStream</a>s.
     * Be aware this is the list of video streams which do NOT contain an audio stream.
     * You can also return null or an empty list, however be aware that if you don't return anything
     * in getAudioStreams(), getVideoStreams() and getDashMpdUrl() either the Collector will handle this as
     * a failed extraction procedure.
     *
     * @return a list of video and streams in the format of AudioStream
     * @throws IOException
     * @throws ExtractionException
     */
    public abstract List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException;

    /**
     * This will return a list of available
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/stream/Subtitles.html">Subtitles</a>s.
     * If no subtitles are available an empty list can returned.
     *
     * @return a list of available subtitles or an empty list
     * @throws IOException
     * @throws ExtractionException
     */
    @Nonnull
    public abstract List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException;

    /**
     * This will return a list of available
     * <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/org/schabi/newpipe/extractor/stream/Subtitles.html">Subtitles</a>s.
     * given by a specific type.
     * If no subtitles in that specific format are available an empty list can returned.
     *
     * @param format the media format by which the subtitles should be filtered
     * @return a list of available subtitles or an empty list
     * @throws IOException
     * @throws ExtractionException
     */
    @Nonnull
    public abstract List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException;

    /**
     * Get the <a href="https://teamnewpipe.github.io/NewPipeExtractor/javadoc/">StreamType</a>.
     *
     * @return the type of the stream
     * @throws ParsingException
     */
    public abstract StreamType getStreamType() throws ParsingException;

    /**
     * Should return a list of streams related to the current handled. Many services show suggested
     * streams. If you don't like suggested streams you should implement them anyway since they can
     * be disabled by the user later in the frontend. The first related stream might be what was
     * previously known as a next stream.
     * If related streams aren't available simply return {@code null}.
     *
     * @return a list of InfoItems showing the related videos/streams
     * @throws IOException
     * @throws ExtractionException
     */
    public abstract StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException;

    /**
     * Should return a list of Frameset object that contains preview of stream frames
     *
     * @return list of preview frames or empty list if frames preview is not supported or not found for specified stream
     * @throws ExtractionException
     */
    @Nonnull
    public List<Frameset> getFrames() throws ExtractionException {
        return Collections.emptyList();
    }

    /**
     * Should analyse the webpage's document and extracts any error message there might be.
     *
     * @return Error message; null if there is no error message.
     */
    public abstract String getErrorMessage();

    //////////////////////////////////////////////////////////////////
    ///  Helper
    //////////////////////////////////////////////////////////////////

    /**
     * Override this function if the format of time stamp in the url is not the same format as that form youtube.
     * Honestly I don't even know the time stamp format of YouTube.
     *
     * @param regexPattern
     * @return the time stamp/seek for the video in seconds
     * @throws ParsingException
     */
    protected long getTimestampSeconds(String regexPattern) throws ParsingException {
        String timeStamp;
        try {
            timeStamp = Parser.matchGroup1(regexPattern, getOriginalUrl());
        } catch (Parser.RegexException e) {
            // catch this instantly since a url does not necessarily have a timestamp

            // -2 because the testing system will consequently know that the regex failed
            // not good, I know
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
        }
    }

    /**
     * The host of the stream (Eg. peertube.cpy.re).
     * If the host is not available, or if the service doesn't use
     * a federated system, but a centralised system,
     * you can simply return an empty string.
     *
     * @return the host of the stream or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getHost() throws ParsingException;

    /**
     * The privacy of the stream (Eg. Public, Private, Unlisted…).
     * If the privacy is not available you can simply return an empty string.
     *
     * @return the privacy of the stream or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getPrivacy() throws ParsingException;

    /**
     * The name of the category of the stream.
     * If the category is not available you can simply return an empty string.
     *
     * @return the category of the stream or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getCategory() throws ParsingException;

    /**
     * The name of the licence of the stream.
     * If the licence is not available you can simply return an empty string.
     *
     * @return the licence of the stream or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getLicence() throws ParsingException;

    /**
     * The locale language of the stream.
     * If the language is not available you can simply return null.
     * If the language is provided by a language code, you can return
     * new Locale(language_code);
     *
     * @return the locale language of the stream or null.
     * @throws ParsingException
     */
    @Nullable
    public abstract Locale getLanguageInfo() throws ParsingException;

    /**
     * The list of tags of the stream.
     * If the tag list is not available you can simply return an empty list.
     *
     * @return the list of tags of the stream or an empty list.
     * @throws ParsingException
     */
    @Nonnull
    public abstract List<String> getTags() throws ParsingException;

    /**
     * The support information of the stream.
     * see: https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37
     * (support button).
     * If the support information are not available,
     * you can simply return an empty String.
     *
     * @return the support information of the stream or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getSupportInfo() throws ParsingException;
}
