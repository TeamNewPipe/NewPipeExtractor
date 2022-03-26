package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class MediaCCCParsingHelper {
    // {conference_slug}/{room_slug}
    private static final Pattern LIVE_STREAM_ID_PATTERN = Pattern.compile("\\w+/\\w+");
    private static JsonArray liveStreams = null;

    private MediaCCCParsingHelper() { }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }

    /**
     * Check whether an id is a live stream id
     * @param id the {@code id} to check
     * @return returns {@code true} if the {@code id} is formatted like
     *         {@code {conference_slug}/{room_slug}}; {@code false} otherwise
     */
    public static boolean isLiveStreamId(final String id) {
        return LIVE_STREAM_ID_PATTERN.matcher(id).find();
    }

    /**
     * Get currently available live streams from
     * <a href="https://streaming.media.ccc.de/streams/v2.json">
     *     https://streaming.media.ccc.de/streams/v2.json</a>.
     * Use this method to cache requests, because they can get quite big.
     * TODO: implement better caching policy (max-age: 3 min)
     * @param downloader The downloader to use for making the request
     * @param localization The localization to be used. Will most likely be ignored.
     * @return {@link JsonArray} containing current conferences and info about their rooms and
     *         streams.
     * @throws ExtractionException if the data could not be fetched or the retrieved data could not
     *                             be parsed to a {@link JsonArray}
     */
    public static JsonArray getLiveStreams(final Downloader downloader,
                                           final Localization localization)
            throws ExtractionException {
        if (liveStreams == null) {
            try {
                final String site = downloader.get("https://streaming.media.ccc.de/streams/v2.json",
                        localization).responseBody();
                liveStreams = JsonParser.array().from(site);
            } catch (final IOException | ReCaptchaException e) {
                throw new ExtractionException("Could not get live stream JSON.", e);
            } catch (final JsonParserException e) {
                throw new ExtractionException("Could not parse JSON.", e);
            }
        }
        return liveStreams;
    }
}
