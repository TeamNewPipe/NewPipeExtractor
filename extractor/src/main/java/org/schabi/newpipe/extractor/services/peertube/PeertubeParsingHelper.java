package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSepiaStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public final class PeertubeParsingHelper {
    public static final String START_KEY = "start";
    public static final String COUNT_KEY = "count";
    public static final int ITEMS_PER_PAGE = 12;
    public static final String START_PATTERN = "start=(\\d*)";

    private PeertubeParsingHelper() {
    }

    public static void validate(final JsonObject json) throws ContentNotAvailableException {
        final String error = json.getString("error");
        if (!Utils.isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.ofInstant(Instant.parse(textualUploadDate), ZoneOffset.UTC);
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }

    public static Page getNextPage(final String prevPageUrl, final long total) {
        final String prevStart;
        try {
            prevStart = Parser.matchGroup1(START_PATTERN, prevPageUrl);
        } catch (final Parser.RegexException e) {
            return null;
        }
        if (Utils.isBlank(prevStart)) {
            return null;
        }

        final long nextStart;
        try {
            nextStart = Long.parseLong(prevStart) + ITEMS_PER_PAGE;
        } catch (final NumberFormatException e) {
            return null;
        }

        if (nextStart >= total) {
            return null;
        } else {
            return new Page(prevPageUrl.replace(
                    START_KEY + "=" + prevStart, START_KEY + "=" + nextStart));
        }
    }

    public static void collectStreamsFrom(final InfoItemsCollector collector,
                                          final JsonObject json,
                                          final String baseUrl) throws ParsingException {
        collectStreamsFrom(collector, json, baseUrl, false);
    }

    /**
     * Collect stream from json with collector
     *
     * @param collector the collector used to collect information
     * @param json      the file to retrieve data from
     * @param baseUrl   the base Url of the instance
     * @param sepia     if we should use PeertubeSepiaStreamInfoItemExtractor
     */
    public static void collectStreamsFrom(final InfoItemsCollector collector,
                                          final JsonObject json,
                                          final String baseUrl,
                                          final boolean sepia) throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (final Exception e) {
            throw new ParsingException("Unable to extract list info", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                JsonObject item = (JsonObject) c;

                // PeerTube playlists have the stream info encapsulated in an "video" object
                if (item.has("video")) {
                    item = item.getObject("video");
                }
                final boolean isPlaylistInfoItem = item.has("videosLength");
                final boolean isChannelInfoItem = item.has("followersCount");

                final InfoItemExtractor extractor;
                if (sepia) {
                    extractor = new PeertubeSepiaStreamInfoItemExtractor(item, baseUrl);
                } else if (isPlaylistInfoItem) {
                    extractor = new PeertubePlaylistInfoItemExtractor(item, baseUrl);
                } else if (isChannelInfoItem) {
                    extractor = new PeertubeChannelInfoItemExtractor(item, baseUrl);
                } else {
                    extractor = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                }
                collector.commit(extractor);
            }
        }
    }

}
