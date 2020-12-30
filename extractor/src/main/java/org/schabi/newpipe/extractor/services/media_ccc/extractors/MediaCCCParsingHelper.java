package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
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
    private static JsonArray liveStreams = null;

    private MediaCCCParsingHelper() { }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate) throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }

    public static boolean isLiveStreamId(final String id) {
        final String pattern = "\\w+/\\w+";
        return Pattern.matches(pattern, id); // {conference_slug}/{room_slug}
    }

    public static JsonArray getLiveStreams(final Downloader downloader, final Localization localization) throws ExtractionException {
        if (liveStreams == null) {
            try {
                final String site = downloader.get("https://streaming.media.ccc.de/streams/v2.json",
                        localization).responseBody();
                liveStreams = JsonParser.array().from(site);
            } catch (IOException | ReCaptchaException e) {
                throw new ExtractionException("Could not get live stream JSON.", e);
            } catch (JsonParserException e) {
                throw new ExtractionException("Could not parse JSON.", e);
            }
        }
        return liveStreams;
    }
}
