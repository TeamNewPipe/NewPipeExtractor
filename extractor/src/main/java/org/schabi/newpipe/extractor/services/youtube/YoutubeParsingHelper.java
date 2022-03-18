package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.HTTP;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonBuilder;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Created by Christian Schabesberger on 02.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeParsingHelper.java is part of NewPipe.
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

public final class YoutubeParsingHelper {

    private YoutubeParsingHelper() {
    }

    public static final String YOUTUBEI_V1_URL = "https://www.youtube.com/youtubei/v1/";

    private static final String HARDCODED_CLIENT_VERSION = "2.20210728.00.00";
    private static final String HARDCODED_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
    private static final String MOBILE_YOUTUBE_KEY = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w";
    private static final String MOBILE_YOUTUBE_CLIENT_VERSION = "16.29.38";
    private static String clientVersion;
    private static String key;

    private static final String[] HARDCODED_YOUTUBE_MUSIC_KEY =
            {"AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30", "67", "1.20210726.00.01"};
    private static String[] youtubeMusicKey;

    private static boolean keyAndVersionExtracted = false;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<Boolean> hardcodedClientVersionAndKeyValid = Optional.empty();

    private static Random numberGenerator = new Random();

    /**
     * <code>PENDING+</code> means that the user did not yet submit their choices.
     * Therefore, YouTube & Google should not track the user, because they did not give consent.
     * The three digits at the end can be random, but are required.
     */
    private static final String CONSENT_COOKIE_VALUE = "PENDING+";
    /**
     * Youtube <code>CONSENT</code> cookie. Should prevent redirect to consent.youtube.com
     */
    private static final String CONSENT_COOKIE = "CONSENT=" + CONSENT_COOKIE_VALUE;

    private static final String FEED_BASE_CHANNEL_ID =
            "https://www.youtube.com/feeds/videos.xml?channel_id=";
    private static final String FEED_BASE_USER = "https://www.youtube.com/feeds/videos.xml?user=";

    private static boolean isGoogleURL(final String url) {
        final String cachedUrl = extractCachedUrlIfNeeded(url);
        try {
            final URL u = new URL(cachedUrl);
            final String host = u.getHost();
            return host.startsWith("google.")
                    || host.startsWith("m.google.")
                    || host.startsWith("www.google.");
        } catch (final MalformedURLException e) {
            return false;
        }
    }

    public static boolean isYoutubeURL(@Nonnull final URL url) {
        final String host = url.getHost();
        return host.equalsIgnoreCase("youtube.com")
                || host.equalsIgnoreCase("www.youtube.com")
                || host.equalsIgnoreCase("m.youtube.com")
                || host.equalsIgnoreCase("music.youtube.com");
    }

    public static boolean isYoutubeServiceURL(@Nonnull final URL url) {
        final String host = url.getHost();
        return host.equalsIgnoreCase("www.youtube-nocookie.com")
                || host.equalsIgnoreCase("youtu.be");
    }

    public static boolean isHooktubeURL(@Nonnull final URL url) {
        final String host = url.getHost();
        return host.equalsIgnoreCase("hooktube.com");
    }

    public static boolean isInvidioURL(@Nonnull final URL url) {
        final String host = url.getHost();
        return host.equalsIgnoreCase("invidio.us")
                || host.equalsIgnoreCase("dev.invidio.us")
                || host.equalsIgnoreCase("www.invidio.us")
                || host.equalsIgnoreCase("redirect.invidious.io")
                || host.equalsIgnoreCase("invidious.snopyta.org")
                || host.equalsIgnoreCase("yewtu.be")
                || host.equalsIgnoreCase("tube.connect.cafe")
                || host.equalsIgnoreCase("tubus.eduvid.org")
                || host.equalsIgnoreCase("invidious.kavin.rocks")
                || host.equalsIgnoreCase("invidious-us.kavin.rocks")
                || host.equalsIgnoreCase("piped.kavin.rocks")
                || host.equalsIgnoreCase("invidious.site")
                || host.equalsIgnoreCase("vid.mint.lgbt")
                || host.equalsIgnoreCase("invidiou.site")
                || host.equalsIgnoreCase("invidious.fdn.fr")
                || host.equalsIgnoreCase("invidious.048596.xyz")
                || host.equalsIgnoreCase("invidious.zee.li")
                || host.equalsIgnoreCase("vid.puffyan.us")
                || host.equalsIgnoreCase("ytprivate.com")
                || host.equalsIgnoreCase("invidious.namazso.eu")
                || host.equalsIgnoreCase("invidious.silkky.cloud")
                || host.equalsIgnoreCase("invidious.exonip.de")
                || host.equalsIgnoreCase("inv.riverside.rocks")
                || host.equalsIgnoreCase("invidious.blamefran.net")
                || host.equalsIgnoreCase("invidious.moomoo.me")
                || host.equalsIgnoreCase("ytb.trom.tf")
                || host.equalsIgnoreCase("yt.cyberhost.uk")
                || host.equalsIgnoreCase("y.com.cm");
    }

    public static boolean isY2ubeURL(@Nonnull final URL url) {
        return url.getHost().equalsIgnoreCase("y2u.be");
    }

    /**
     * Parses the duration string of the video expecting ":" or "." as separators
     *
     * @return the duration in seconds
     * @throws ParsingException when more than 3 separators are found
     */
    public static int parseDurationString(@Nonnull final String input)
            throws ParsingException, NumberFormatException {
        // If time separator : is not detected, try . instead
        final String[] splitInput = input.contains(":")
                ? input.split(":")
                : input.split("\\.");

        String days = "0";
        String hours = "0";
        String minutes = "0";
        final String seconds;

        switch (splitInput.length) {
            case 4:
                days = splitInput[0];
                hours = splitInput[1];
                minutes = splitInput[2];
                seconds = splitInput[3];
                break;
            case 3:
                hours = splitInput[0];
                minutes = splitInput[1];
                seconds = splitInput[2];
                break;
            case 2:
                minutes = splitInput[0];
                seconds = splitInput[1];
                break;
            case 1:
                seconds = splitInput[0];
                break;
            default:
                throw new ParsingException("Error duration string with unknown format: " + input);
        }

        return ((convertDurationToInt(days) * 24
                + convertDurationToInt(hours)) * 60
                + convertDurationToInt(minutes)) * 60
                + convertDurationToInt(seconds);
    }

    /**
     * Tries to convert a duration string to an integer without throwing an exception.
     * <br/>
     * Helper method for {@link #parseDurationString(String)}.
     * <br/>
     * Note: This method is also used as a workaround for NewPipe#8034 (YT shorts no longer
     * display any duration in channels).
     *
     * @param input The string to process
     * @return The converted integer or 0 if the conversion failed.
     */
    private static int convertDurationToInt(final String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        final String clearedInput = Utils.removeNonDigitCharacters(input);
        try {
            return Integer.parseInt(clearedInput);
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }

    @Nonnull
    public static String getFeedUrlFrom(@Nonnull final String channelIdOrUser) {
        if (channelIdOrUser.startsWith("user/")) {
            return FEED_BASE_USER + channelIdOrUser.replace("user/", "");
        } else if (channelIdOrUser.startsWith("channel/")) {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser.replace("channel/", "");
        } else {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser;
        }
    }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (final DateTimeParseException e) {
            try {
                return LocalDate.parse(textualUploadDate).atStartOfDay().atOffset(ZoneOffset.UTC);
            } catch (final DateTimeParseException e1) {
                throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"",
                        e1);
            }
        }
    }

    /**
     * Checks if the given playlist id is a YouTube Mix (auto-generated playlist)
     * Ids from a YouTube Mix start with "RD"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Mix
     */
    public static boolean isYoutubeMixId(@Nonnull final String playlistId) {
        return playlistId.startsWith("RD")
                && !isYoutubeMusicMixId(playlistId);
    }

    /**
     * Checks if the given playlist id is a YouTube My Mix (auto-generated playlist)
     * Ids from a YouTube My Mix start with "RDMM"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube My Mix
     */
    public static boolean isYoutubeMyMixId(@Nonnull final String playlistId) {
        return playlistId.startsWith("RDMM");
    }

    /**
     * Checks if the given playlist id is a YouTube Music Mix (auto-generated playlist)
     * Ids from a YouTube Music Mix start with "RDAMVM" or "RDCLAK"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Music Mix
     */
    public static boolean isYoutubeMusicMixId(@Nonnull final String playlistId) {
        return playlistId.startsWith("RDAMVM") || playlistId.startsWith("RDCLAK");
    }

    /**
     * Checks if the given playlist id is a YouTube Channel Mix (auto-generated playlist)
     * Ids from a YouTube channel Mix start with "RDCM"
     *
     * @return Whether given id belongs to a YouTube Channel Mix
     */
    public static boolean isYoutubeChannelMixId(@Nonnull final String playlistId) {
        return playlistId.startsWith("RDCM");
    }

    /**
     * Checks if the given playlist id is a YouTube Genre Mix (auto-generated playlist)
     * Ids from a YouTube Genre Mix start with "RDGMEM"
     *
     * @return Whether given id belongs to a YouTube Genre Mix
     */
    public static boolean isYoutubeGenreMixId(@Nonnull final String playlistId) {
        return playlistId.startsWith("RDGMEM");
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the {@link PlaylistInfo.PlaylistType} extracted from the playlistId (mix playlist
     *         types included)
     * @throws ParsingException if the playlistId is null or empty, if the playlistId is not a mix,
     *                          if it is a mix but it's not based on a specific stream (this is the
     *                          case for channel or genre mixes)
     */
    @Nonnull
    public static String extractVideoIdFromMixId(final String playlistId)
            throws ParsingException {
        if (isNullOrEmpty(playlistId)) {
            throw new ParsingException("Video id could not be determined from empty playlist id");

        } else if (isYoutubeMyMixId(playlistId)) {
            return playlistId.substring(4);

        } else if (isYoutubeMusicMixId(playlistId)) {
            return playlistId.substring(6);

        } else if (isYoutubeChannelMixId(playlistId)) {
            // Channel mixes are of the form RMCM{channelId}, so videoId can't be determined
            throw new ParsingException("Video id could not be determined from channel mix id: "
                    + playlistId);

        } else if (isYoutubeGenreMixId(playlistId)) {
            // Genre mixes are of the form RDGMEM{garbage}, so videoId can't be determined
            throw new ParsingException("Video id could not be determined from genre mix id: "
                    + playlistId);

        } else if (isYoutubeMixId(playlistId)) { // normal mix
            if (playlistId.length() != 13) {
                // Stream YouTube mixes are of the form RD{videoId}, but if videoId is not exactly
                // 11 characters then it can't be a video id, hence we are dealing with a different
                // type of mix (e.g. genre mixes handled above, of the form RDGMEM{garbage})
                throw new ParsingException("Video id could not be determined from mix id: "
                    + playlistId);
            }
            return playlistId.substring(2);

        } else { // not a mix
            throw new ParsingException("Video id could not be determined from playlist id: "
                    + playlistId);
        }
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the {@link PlaylistInfo.PlaylistType} extracted from the playlistId (mix playlist
     *         types included)
     * @throws ParsingException if the playlistId is null or empty
     */
    @Nonnull
    public static PlaylistInfo.PlaylistType extractPlaylistTypeFromPlaylistId(
            final String playlistId) throws ParsingException {
        if (isNullOrEmpty(playlistId)) {
            throw new ParsingException("Could not extract playlist type from empty playlist id");
        } else if (isYoutubeMusicMixId(playlistId)) {
            return PlaylistInfo.PlaylistType.MIX_MUSIC;
        } else if (isYoutubeChannelMixId(playlistId)) {
            return PlaylistInfo.PlaylistType.MIX_CHANNEL;
        } else if (isYoutubeGenreMixId(playlistId)) {
            return PlaylistInfo.PlaylistType.MIX_GENRE;
        } else if (isYoutubeMixId(playlistId)) { // normal mix
            // Either a normal mix based on a stream, or a "my mix" (still based on a stream).
            // NOTE: if YouTube introduces even more types of mixes that still start with RD,
            // they will default to this, even though they might not be based on a stream.
            return PlaylistInfo.PlaylistType.MIX_STREAM;
        } else {
            // not a known type of mix: just consider it a normal playlist
            return PlaylistInfo.PlaylistType.NORMAL;
        }
    }

    /**
     * @param playlistUrl the playlist url to parse
     * @return the {@link PlaylistInfo.PlaylistType} extracted from the playlistUrl's list param
     *         (mix playlist types included)
     * @throws ParsingException if the playlistUrl is malformed, if has no list param or if the list
     *                          param is empty
     */
    public static PlaylistInfo.PlaylistType extractPlaylistTypeFromPlaylistUrl(
            final String playlistUrl) throws ParsingException {
        try {
            return extractPlaylistTypeFromPlaylistId(
                    Utils.getQueryValue(Utils.stringToURL(playlistUrl), "list"));
        } catch (final MalformedURLException e) {
            throw new ParsingException("Could not extract playlist type from malformed url", e);
        }
    }

    public static JsonObject getInitialData(final String html) throws ParsingException {
        try {
            try {
                final String initialData = Parser.matchGroup1(
                        "window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});", html);
                return JsonParser.object().from(initialData);
            } catch (final Parser.RegexException e) {
                final String initialData = Parser.matchGroup1(
                        "var\\s*ytInitialData\\s*=\\s*(\\{.*?\\});", html);
                return JsonParser.object().from(initialData);
            }
        } catch (final JsonParserException | Parser.RegexException e) {
            throw new ParsingException("Could not get ytInitialData", e);
        }
    }

    public static boolean areHardcodedClientVersionAndKeyValid()
            throws IOException, ExtractionException {
        if (hardcodedClientVersionAndKeyValid.isPresent()) {
            return hardcodedClientVersionAndKeyValid.get();
        }
        // @formatter:off
        final byte[] body = JsonWriter.string()
            .object()
                .object("context")
                    .object("client")
                        .value("hl", "en-GB")
                        .value("gl", "GB")
                        .value("clientName", "WEB")
                        .value("clientVersion", HARDCODED_CLIENT_VERSION)
                    .end()
                .object("user")
                    .value("lockedSafetyMode", false)
                .end()
                .value("fetchLiveState", true)
                .end()
            .end().done().getBytes(UTF_8);
        // @formatter:on

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
        headers.put("X-YouTube-Client-Version",
                Collections.singletonList(HARDCODED_CLIENT_VERSION));

        // This endpoint is fetched by the YouTube website to get the items of its main menu and is
        // pretty lightweight (around 30kB)
        final Response response = getDownloader().post(YOUTUBEI_V1_URL + "guide?key="
                        + HARDCODED_KEY, headers, body);
        final String responseBody = response.responseBody();
        final int responseCode = response.responseCode();

        hardcodedClientVersionAndKeyValid = Optional.of(responseBody.length() > 5000
                && responseCode == 200); // Ensure to have a valid response
        return hardcodedClientVersionAndKeyValid.get();
    }

    private static void extractClientVersionAndKey() throws IOException, ExtractionException {
        // Don't extract the client version and the InnerTube key if it has been already extracted
        if (keyAndVersionExtracted) {
            return;
        }

        // Don't provide a search term in order to have a smaller response
        final String url = "https://www.youtube.com/results?search_query=&ucbcb=1";
        final Map<String, List<String>> headers = new HashMap<>();
        addCookieHeader(headers);
        final String html = getDownloader().get(url, headers).responseBody();
        final JsonObject initialData = getInitialData(html);
        final JsonArray serviceTrackingParams = initialData.getObject("responseContext")
                .getArray("serviceTrackingParams");
        String shortClientVersion = null;

        // Try to get version from initial data first
        for (final Object service : serviceTrackingParams) {
            final JsonObject s = (JsonObject) service;
            if (s.getString("service").equals("CSI")) {
                final JsonArray params = s.getArray("params");
                for (final Object param : params) {
                    final JsonObject p = (JsonObject) param;
                    final String paramKey = p.getString("key");
                    if (paramKey != null && paramKey.equals("cver")) {
                        clientVersion = p.getString("value");
                    }
                }
            } else if (s.getString("service").equals("ECATCHER")) {
                // Fallback to get a shortened client version which does not contain the last two
                // digits
                final JsonArray params = s.getArray("params");
                for (final Object param : params) {
                    final JsonObject p = (JsonObject) param;
                    final String paramKey = p.getString("key");
                    if (paramKey != null && paramKey.equals("client.version")) {
                        shortClientVersion = p.getString("value");
                    }
                }
            }
        }

        String contextClientVersion;
        final String[] patterns = {
                "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"",
                "innertube_context_client_version\":\"([0-9\\.]+?)\"",
                "client.version=([0-9\\.]+)"
        };
        for (final String pattern : patterns) {
            try {
                contextClientVersion = Parser.matchGroup1(pattern, html);
                if (!isNullOrEmpty(contextClientVersion)) {
                    clientVersion = contextClientVersion;
                    break;
                }
            } catch (final Parser.RegexException ignored) {
            }
        }

        if (!isNullOrEmpty(clientVersion) && !isNullOrEmpty(shortClientVersion)) {
            clientVersion = shortClientVersion;
        }

        try {
            key = Parser.matchGroup1("INNERTUBE_API_KEY\":\"([0-9a-zA-Z_-]+?)\"", html);
        } catch (final Parser.RegexException e1) {
            try {
                key = Parser.matchGroup1("innertubeApiKey\":\"([0-9a-zA-Z_-]+?)\"", html);
            } catch (final Parser.RegexException e2) {
                throw new ParsingException("Could not extract client version and key");
            }
        }
        keyAndVersionExtracted = true;
    }

    /**
     * Get the client version
     */
    public static String getClientVersion() throws IOException, ExtractionException {
        if (!isNullOrEmpty(clientVersion)) {
            return clientVersion;
        }
        if (areHardcodedClientVersionAndKeyValid()) {
            clientVersion = HARDCODED_CLIENT_VERSION;
            return clientVersion;
        }

        extractClientVersionAndKey();
        return clientVersion;
    }

    /**
     * Get the key
     */
    public static String getKey() throws IOException, ExtractionException {
        if (!isNullOrEmpty(key)) {
            return key;
        }
        if (areHardcodedClientVersionAndKeyValid()) {
            key = HARDCODED_KEY;
            return key;
        }

        extractClientVersionAndKey();
        return key;
    }

    /**
     * <p>
     * <b>Only use in tests.</b>
     * </p>
     *
     * <p>
     * Quick-and-dirty solution to reset global state in between test classes.
     * </p>
     * <p>
     * This is needed for the mocks because in order to reach that state a network request has to
     * be made. If the global state is not reset and the RecordingDownloader is used,
     * then only the first test class has that request recorded. Meaning running the other
     * tests with mocks will fail, because the mock is missing.
     * </p>
     */
    public static void resetClientVersionAndKey() {
        clientVersion = null;
        key = null;
    }

    /**
     * <p>
     * <b>Only use in tests.</b>
     * </p>
     */
    public static void setNumberGenerator(final Random random) {
        numberGenerator = random;
    }

    public static boolean isHardcodedYoutubeMusicKeyValid() throws IOException,
            ReCaptchaException {
        final String url =
                "https://music.youtube.com/youtubei/v1/music/get_search_suggestions?alt=json&key="
                + HARDCODED_YOUTUBE_MUSIC_KEY[0];

        // @formatter:off
        final byte[] json = JsonWriter.string()
            .object()
                .object("context")
                    .object("client")
                        .value("clientName", "WEB_REMIX")
                        .value("clientVersion", HARDCODED_YOUTUBE_MUSIC_KEY[2])
                        .value("hl", "en-GB")
                        .value("gl", "GB")
                        .array("experimentIds").end()
                        .value("experimentsToken", EMPTY_STRING)
                        .object("locationInfo").end()
                        .object("musicAppInfo").end()
                    .end()
                    .object("capabilities").end()
                    .object("request")
                        .array("internalExperimentFlags").end()
                        .object("sessionIndex").end()
                    .end()
                    .object("activePlayers").end()
                    .object("user")
                        .value("enableSafetyMode", false)
                    .end()
                .end()
                .value("input", "")
            .end().done().getBytes(UTF_8);
        // @formatter:on

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList(
                HARDCODED_YOUTUBE_MUSIC_KEY[1]));
        headers.put("X-YouTube-Client-Version", Collections.singletonList(
                HARDCODED_YOUTUBE_MUSIC_KEY[2]));
        headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
        headers.put("Referer", Collections.singletonList("music.youtube.com"));
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final Response response = getDownloader().post(url, headers, json);
        // Ensure to have a valid response
        return response.responseBody().length() > 500 && response.responseCode() == 200;
    }

    public static String[] getYoutubeMusicKey() throws IOException, ReCaptchaException,
            Parser.RegexException {
        if (youtubeMusicKey != null && youtubeMusicKey.length == 3) {
            return youtubeMusicKey;
        }
        if (isHardcodedYoutubeMusicKeyValid()) {
            youtubeMusicKey = HARDCODED_YOUTUBE_MUSIC_KEY;
            return youtubeMusicKey;
        }

        final String url = "https://music.youtube.com/";
        final Map<String, List<String>> headers = new HashMap<>();
        addCookieHeader(headers);
        final String html = getDownloader().get(url, headers).responseBody();

        String innertubeApiKey;
        try {
            innertubeApiKey = Parser.matchGroup1("INNERTUBE_API_KEY\":\"([0-9a-zA-Z_-]+?)\"", html);
        } catch (final Parser.RegexException e) {
            innertubeApiKey = Parser.matchGroup1("innertube_api_key\":\"([0-9a-zA-Z_-]+?)\"", html);
        }

        final String innertubeClientName
                = Parser.matchGroup1("INNERTUBE_CONTEXT_CLIENT_NAME\":([0-9]+?),", html);

        String innertubeClientVersion;
        try {
            innertubeClientVersion = Parser.matchGroup1(
                    "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"", html);
        } catch (final Parser.RegexException e) {
            try {
                innertubeClientVersion = Parser.matchGroup1(
                        "INNERTUBE_CLIENT_VERSION\":\"([0-9\\.]+?)\"", html);
            } catch (final Parser.RegexException ee) {
                innertubeClientVersion = Parser.matchGroup1(
                        "innertube_context_client_version\":\"([0-9\\.]+?)\"", html);
            }
        }

        youtubeMusicKey = new String[]{
                innertubeApiKey,
                innertubeClientName,
                innertubeClientVersion
        };
        return youtubeMusicKey;
    }

    @Nullable
    public static String getUrlFromNavigationEndpoint(@Nonnull final JsonObject navigationEndpoint)
            throws ParsingException {
        if (navigationEndpoint.has("urlEndpoint")) {
            String internUrl = navigationEndpoint.getObject("urlEndpoint").getString("url");
            if (internUrl.startsWith("https://www.youtube.com/redirect?")) {
                // remove https://www.youtube.com part to fall in the next if block
                internUrl = internUrl.substring(23);
            }

            if (internUrl.startsWith("/redirect?")) {
                // q parameter can be the first parameter
                internUrl = internUrl.substring(10);
                final String[] params = internUrl.split("&");
                for (final String param : params) {
                    if (param.split("=")[0].equals("q")) {
                        try {
                            return URLDecoder.decode(param.split("=")[1], UTF_8);
                        } catch (final UnsupportedEncodingException e) {
                            return null;
                        }
                    }
                }
            } else if (internUrl.startsWith("http")) {
                return internUrl;
            } else if (internUrl.startsWith("/channel") || internUrl.startsWith("/user")
                    || internUrl.startsWith("/watch")) {
                return "https://www.youtube.com" + internUrl;
            }
        } else if (navigationEndpoint.has("browseEndpoint")) {
            final JsonObject browseEndpoint = navigationEndpoint.getObject("browseEndpoint");
            final String canonicalBaseUrl = browseEndpoint.getString("canonicalBaseUrl");
            final String browseId = browseEndpoint.getString("browseId");

            // All channel ids are prefixed with UC
            if (browseId != null && browseId.startsWith("UC")) {
                return "https://www.youtube.com/channel/" + browseId;
            }

            if (!isNullOrEmpty(canonicalBaseUrl)) {
                return "https://www.youtube.com" + canonicalBaseUrl;
            }

            throw new ParsingException("canonicalBaseUrl is null and browseId is not a channel (\""
                    + browseEndpoint + "\")");
        } else if (navigationEndpoint.has("watchEndpoint")) {
            final StringBuilder url = new StringBuilder();
            url.append("https://www.youtube.com/watch?v=").append(navigationEndpoint
                    .getObject("watchEndpoint").getString("videoId"));
            if (navigationEndpoint.getObject("watchEndpoint").has("playlistId")) {
                url.append("&list=").append(navigationEndpoint.getObject("watchEndpoint")
                        .getString("playlistId"));
            }
            if (navigationEndpoint.getObject("watchEndpoint").has("startTimeSeconds")) {
                url.append("&amp;t=").append(navigationEndpoint.getObject("watchEndpoint")
                        .getInt("startTimeSeconds"));
            }
            return url.toString();
        } else if (navigationEndpoint.has("watchPlaylistEndpoint")) {
            return "https://www.youtube.com/playlist?list="
                    + navigationEndpoint.getObject("watchPlaylistEndpoint").getString("playlistId");
        }
        return null;
    }

    /**
     * Get the text from a JSON object that has either a simpleText or a runs array.
     *
     * @param textObject JSON object to get the text from
     * @param html       whether to return HTML, by parsing the navigationEndpoint
     * @return text in the JSON object or {@code null}
     */
    @Nullable
    public static String getTextFromObject(final JsonObject textObject, final boolean html)
            throws ParsingException {
        if (isNullOrEmpty(textObject)) {
            return null;
        }

        if (textObject.has("simpleText")) {
            return textObject.getString("simpleText");
        }

        if (textObject.getArray("runs").isEmpty()) {
            return null;
        }

        final StringBuilder textBuilder = new StringBuilder();
        for (final Object textPart : textObject.getArray("runs")) {
            final String text = ((JsonObject) textPart).getString("text");
            if (html && ((JsonObject) textPart).has("navigationEndpoint")) {
                final String url = getUrlFromNavigationEndpoint(((JsonObject) textPart)
                        .getObject("navigationEndpoint"));
                if (!isNullOrEmpty(url)) {
                    textBuilder.append("<a href=\"").append(url).append("\">").append(text)
                            .append("</a>");
                    continue;
                }
            }
            textBuilder.append(text);
        }

        String text = textBuilder.toString();

        if (html) {
            text = text.replaceAll("\\n", "<br>");
            text = text.replaceAll(" {2}", " &nbsp;");
        }

        return text;
    }

    @Nullable
    public static String getTextFromObject(final JsonObject textObject) throws ParsingException {
        return getTextFromObject(textObject, false);
    }

    @Nullable
    public static String getTextAtKey(@Nonnull final JsonObject jsonObject, final String theKey)
            throws ParsingException {
        if (jsonObject.isString(theKey)) {
            return jsonObject.getString(theKey);
        } else {
            return getTextFromObject(jsonObject.getObject(theKey));
        }
    }

    public static String fixThumbnailUrl(@Nonnull final String thumbnailUrl) {
        String result = thumbnailUrl;
        if (result.startsWith("//")) {
            result = result.substring(2);
        }

        if (result.startsWith(HTTP)) {
            result = Utils.replaceHttpWithHttps(result);
        } else if (!result.startsWith(HTTPS)) {
            result = "https://" + result;
        }

        return result;
    }

    public static String getThumbnailUrlFromInfoItem(final JsonObject infoItem)
            throws ParsingException {
        // TODO: Don't simply get the first item, but look at all thumbnails and their resolution
        try {
            return fixThumbnailUrl(infoItem.getObject("thumbnail").getArray("thumbnails")
                    .getObject(0).getString("url"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Nonnull
    public static String getValidJsonResponseBody(@Nonnull final Response response)
            throws ParsingException, MalformedURLException {
        if (response.responseCode() == 404) {
            throw new ContentNotAvailableException("Not found"
                    + " (\"" + response.responseCode() + " " + response.responseMessage() + "\")");
        }

        final String responseBody = response.responseBody();
        if (responseBody.length() < 50) { // Ensure to have a valid response
            throw new ParsingException("JSON response is too short");
        }

        // Check if the request was redirected to the error page.
        final URL latestUrl = new URL(response.latestUrl());
        if (latestUrl.getHost().equalsIgnoreCase("www.youtube.com")) {
            final String path = latestUrl.getPath();
            if (path.equalsIgnoreCase("/oops") || path.equalsIgnoreCase("/error")) {
                throw new ContentNotAvailableException("Content unavailable");
            }
        }

        final String responseContentType = response.getHeader("Content-Type");
        if (responseContentType != null
                && responseContentType.toLowerCase().contains("text/html")) {
            throw new ParsingException("Got HTML document, expected JSON response"
                    + " (latest url was: \"" + response.latestUrl() + "\")");
        }

        return responseBody;
    }

    public static Response getResponse(final String url, final Localization localization)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        addYouTubeHeaders(headers);

        final Response response = getDownloader().get(url, headers, localization);
        getValidJsonResponseBody(response);

        return response;
    }

    public static JsonObject getJsonPostResponse(final String endpoint,
                                                 final byte[] body,
                                                 final Localization localization)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        addClientInfoHeaders(headers);
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final Response response = getDownloader().post(YOUTUBEI_V1_URL + endpoint + "?key="
                + getKey(), headers, body, localization);

        return JsonUtils.toJsonObject(getValidJsonResponseBody(response));
    }

    public static JsonObject getJsonMobilePostResponse(final String endpoint,
                                                       final byte[] body,
                                                       @Nonnull final ContentCountry
                                                               contentCountry,
                                                       final Localization localization)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList("application/json"));
        // Spoofing an Android 11 device with the hardcoded version of the Android app
        headers.put("User-Agent", Collections.singletonList("com.google.android.youtube/"
                + MOBILE_YOUTUBE_CLIENT_VERSION + "Linux; U; Android 11; "
                + contentCountry.getCountryCode() + ") gzip"));
        headers.put("x-goog-api-format-version", Collections.singletonList("2"));

        final Response response = getDownloader().post(
                "https://youtubei.googleapis.com/youtubei/v1/" + endpoint + "?key="
                        + MOBILE_YOUTUBE_KEY, headers, body, localization);

        return JsonUtils.toJsonObject(getValidJsonResponseBody(response));
    }

    public static JsonArray getJsonResponse(final String url, final Localization localization)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        addYouTubeHeaders(headers);

        final Response response = getDownloader().get(url, headers, localization);

        return JsonUtils.toJsonArray(getValidJsonResponseBody(response));
    }

    public static JsonArray getJsonResponse(@Nonnull final Page page,
                                            final Localization localization)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        addYouTubeHeaders(headers);

        final Response response = getDownloader().get(page.getUrl(), headers, localization);

        return JsonUtils.toJsonArray(getValidJsonResponseBody(response));
    }

    @Nonnull
    public static JsonBuilder<JsonObject> prepareDesktopJsonBuilder(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry)
            throws IOException, ExtractionException {
        // @formatter:off
        return JsonObject.builder()
                .object("context")
                    .object("client")
                        .value("hl", localization.getLocalizationCode())
                        .value("gl", contentCountry.getCountryCode())
                        .value("clientName", "WEB")
                        .value("clientVersion", getClientVersion())
                    .end()
                    .object("user")
                        // TO DO: provide a way to enable restricted mode with:
                        // .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
                    .end()
                .end();
        // @formatter:on
    }

    @Nonnull
    public static JsonBuilder<JsonObject> prepareAndroidMobileJsonBuilder(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry) {
        // @formatter:off
        return JsonObject.builder()
                .object("context")
                    .object("client")
                        .value("clientName", "ANDROID")
                        .value("clientVersion", MOBILE_YOUTUBE_CLIENT_VERSION)
                        .value("hl", localization.getLocalizationCode())
                        .value("gl", contentCountry.getCountryCode())
                    .end()
                    .object("user")
                        // TO DO: provide a way to enable restricted mode with:
                        // .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
                    .end()
                .end();
        // @formatter:on
    }

    @Nonnull
    public static JsonBuilder<JsonObject> prepareDesktopEmbedVideoJsonBuilder(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId) throws IOException, ExtractionException {
        // @formatter:off
        return JsonObject.builder()
                .object("context")
                    .object("client")
                        .value("hl", localization.getLocalizationCode())
                        .value("gl", contentCountry.getCountryCode())
                        .value("clientName", "WEB")
                        .value("clientVersion", getClientVersion())
                        .value("clientScreen", "EMBED")
                    .end()
                    .object("thirdParty")
                        .value("embedUrl", "https://www.youtube.com/watch?v=" + videoId)
                    .end()
                    .object("user")
                        // TO DO: provide a way to enable restricted mode with:
                        // .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
                    .end()
                .end()
                .value("videoId", videoId);
        // @formatter:on
    }

    @Nonnull
    public static JsonBuilder<JsonObject> prepareAndroidMobileEmbedVideoJsonBuilder(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId) {
        // @formatter:off
        return JsonObject.builder()
                .object("context")
                    .object("client")
                        .value("clientName", "ANDROID")
                        .value("clientVersion", MOBILE_YOUTUBE_CLIENT_VERSION)
                        .value("clientScreen", "EMBED")
                        .value("hl", localization.getLocalizationCode())
                        .value("gl", contentCountry.getCountryCode())
                    .end()
                    .object("thirdParty")
                        .value("embedUrl", "https://www.youtube.com/watch?v=" + videoId)
                    .end()
                    .object("user")
                        // TO DO: provide a way to enable restricted mode with:
                        // .value("enableSafetyMode", boolean)
                        .value("lockedSafetyMode", false)
                    .end()
                .end()
                .value("videoId", videoId);
        // @formatter:on
    }

    @Nonnull
    public static byte[] createPlayerBodyWithSts(final Localization localization,
                                                 final ContentCountry contentCountry,
                                                 final String videoId,
                                                 final boolean withThirdParty,
                                                 @Nullable final String sts)
            throws IOException, ExtractionException {
        if (withThirdParty) {
            // @formatter:off
            return JsonWriter.string(prepareDesktopEmbedVideoJsonBuilder(
                    localization, contentCountry, videoId)
                    .object("playbackContext")
                        .object("contentPlaybackContext")
                            .value("signatureTimestamp", sts)
                        .end()
                    .end()
                    .done())
                    .getBytes(UTF_8);
            // @formatter:on
        } else {
            // @formatter:off
            return JsonWriter.string(prepareDesktopJsonBuilder(localization, contentCountry)
                    .value("videoId", videoId)
                    .object("playbackContext")
                        .object("contentPlaybackContext")
                            .value("signatureTimestamp", sts)
                        .end()
                    .end()
                    .done())
                    .getBytes(UTF_8);
            // @formatter:on
        }
    }

    /**
     * Add required headers and cookies to an existing headers Map.
     * @see #addClientInfoHeaders(Map)
     * @see #addCookieHeader(Map)
     */
    public static void addYouTubeHeaders(final Map<String, List<String>> headers)
            throws IOException, ExtractionException {
        addClientInfoHeaders(headers);
        addCookieHeader(headers);
    }

    /**
     * Add the <code>X-YouTube-Client-Name</code>, <code>X-YouTube-Client-Version</code>,
     * <code>Origin</code>, and <code>Referer</code> headers.
     * @param headers The headers which should be completed
     */
    public static void addClientInfoHeaders(@Nonnull final Map<String, List<String>> headers)
            throws IOException, ExtractionException {
        headers.computeIfAbsent("Origin", k -> Collections.singletonList(
                "https://www.youtube.com"));
        headers.computeIfAbsent("Referer", k -> Collections.singletonList(
                "https://www.youtube.com"));
        headers.computeIfAbsent("X-YouTube-Client-Name", k -> Collections.singletonList("1"));
        if (headers.get("X-YouTube-Client-Version") == null) {
            headers.put("X-YouTube-Client-Version", Collections.singletonList(getClientVersion()));
        }
    }

    /**
     * Add the <code>CONSENT</code> cookie to prevent redirect to <code>consent.youtube.com</code>
     * @see #CONSENT_COOKIE
     * @param headers the headers which should be completed
     */
    public static void addCookieHeader(@Nonnull final Map<String, List<String>> headers) {
        if (headers.get("Cookie") == null) {
            headers.put("Cookie", Collections.singletonList(generateConsentCookie()));
        } else {
            headers.get("Cookie").add(generateConsentCookie());
        }
    }

    @Nonnull
    public static String generateConsentCookie() {
        final int statusCode = 100 + numberGenerator.nextInt(900);
        return CONSENT_COOKIE + statusCode;
    }

    public static String extractCookieValue(final String cookieName,
                                            @Nonnull final Response response) {
        final List<String> cookies = response.responseHeaders().get("set-cookie");
        if (cookies == null) {
            return EMPTY_STRING;
        }

        String result = EMPTY_STRING;
        for (final String cookie : cookies) {
            final int startIndex = cookie.indexOf(cookieName);
            if (startIndex != -1) {
                result = cookie.substring(startIndex + cookieName.length() + "=".length(),
                        cookie.indexOf(";", startIndex));
            }
        }
        return result;
    }

    /**
     * Shared alert detection function, multiple endpoints return the error similarly structured.
     * <p>
     * Will check if the object has an alert of the type "ERROR".
     * </p>
     *
     * @param initialData the object which will be checked if an alert is present
     * @throws ContentNotAvailableException if an alert is detected
     */
    public static void defaultAlertsCheck(@Nonnull final JsonObject initialData)
            throws ParsingException {
        final JsonArray alerts = initialData.getArray("alerts");
        if (!isNullOrEmpty(alerts)) {
            final JsonObject alertRenderer = alerts.getObject(0).getObject("alertRenderer");
            final String alertText = getTextFromObject(alertRenderer.getObject("text"));
            final String alertType = alertRenderer.getString("type", EMPTY_STRING);
            if (alertType.equalsIgnoreCase("ERROR")) {
                if (alertText != null && alertText.contains("This account has been terminated")) {
                    if (alertText.contains("violation") || alertText.contains("violating")
                            || alertText.contains("infringement")) {
                        // Possible error messages:
                        // "This account has been terminated for a violation of YouTube's Terms of
                        //     Service."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy prohibiting hate speech."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy prohibiting content designed to harass, bully or
                        //     threaten."
                        // "This account has been terminated due to multiple or severe violations
                        //     of YouTube's policy against spam, deceptive practices and misleading
                        //     content or other Terms of Service violations."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy on nudity or sexual content."
                        // "This account has been terminated for violating YouTube's Community
                        //     Guidelines."
                        // "This account has been terminated because we received multiple
                        //     third-party claims of copyright infringement regarding material that
                        //     the user posted."
                        // "This account has been terminated because it is linked to an account that
                        //     received multiple third-party claims of copyright infringement."
                        throw new AccountTerminatedException(alertText,
                                AccountTerminatedException.Reason.VIOLATION);
                    } else {
                        throw new AccountTerminatedException(alertText);
                    }
                }
                throw new ContentNotAvailableException("Got error: \"" + alertText + "\"");
            }
        }
    }

    @Nonnull
    public static List<MetaInfo> getMetaInfo(@Nonnull final JsonArray contents)
            throws ParsingException {
        final List<MetaInfo> metaInfo = new ArrayList<>();
        for (final Object content : contents) {
            final JsonObject resultObject = (JsonObject) content;
            if (resultObject.has("itemSectionRenderer")) {
                for (final Object sectionContentObject
                        : resultObject.getObject("itemSectionRenderer").getArray("contents")) {

                    final JsonObject sectionContent = (JsonObject) sectionContentObject;
                    if (sectionContent.has("infoPanelContentRenderer")) {
                        metaInfo.add(getInfoPanelContent(sectionContent
                                .getObject("infoPanelContentRenderer")));
                    }
                    if (sectionContent.has("clarificationRenderer")) {
                        metaInfo.add(getClarificationRendererContent(sectionContent
                                .getObject("clarificationRenderer")
                        ));
                    }

                }
            }
        }
        return metaInfo;
    }

    @Nonnull
    private static MetaInfo getInfoPanelContent(@Nonnull final JsonObject infoPanelContentRenderer)
            throws ParsingException {
        final MetaInfo metaInfo = new MetaInfo();
        final StringBuilder sb = new StringBuilder();
        for (final Object paragraph : infoPanelContentRenderer.getArray("paragraphs")) {
            if (sb.length() != 0) {
                sb.append("<br>");
            }
            sb.append(YoutubeParsingHelper.getTextFromObject((JsonObject) paragraph));
        }
        metaInfo.setContent(new Description(sb.toString(), Description.HTML));
        if (infoPanelContentRenderer.has("sourceEndpoint")) {
            final String metaInfoLinkUrl = YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                    infoPanelContentRenderer.getObject("sourceEndpoint"));
            try {
                metaInfo.addUrl(new URL(Objects.requireNonNull(extractCachedUrlIfNeeded(
                        metaInfoLinkUrl))));
            } catch (final NullPointerException | MalformedURLException e) {
                throw new ParsingException("Could not get metadata info URL", e);
            }

            final String metaInfoLinkText = YoutubeParsingHelper.getTextFromObject(
                    infoPanelContentRenderer.getObject("inlineSource"));
            if (isNullOrEmpty(metaInfoLinkText)) {
                throw new ParsingException("Could not get metadata info link text.");
            }
            metaInfo.addUrlText(metaInfoLinkText);
        }

        return metaInfo;
    }

    @Nonnull
    private static MetaInfo getClarificationRendererContent(
            @Nonnull final JsonObject clarificationRenderer) throws ParsingException {
        final MetaInfo metaInfo = new MetaInfo();

        final String title = YoutubeParsingHelper.getTextFromObject(clarificationRenderer
                .getObject("contentTitle"));
        final String text = YoutubeParsingHelper.getTextFromObject(clarificationRenderer
                .getObject("text"));
        if (title == null || text == null) {
            throw new ParsingException("Could not extract clarification renderer content");
        }
        metaInfo.setTitle(title);
        metaInfo.setContent(new Description(text, Description.PLAIN_TEXT));

        if (clarificationRenderer.has("actionButton")) {
            final JsonObject actionButton = clarificationRenderer.getObject("actionButton")
                    .getObject("buttonRenderer");
            try {
                final String url = YoutubeParsingHelper.getUrlFromNavigationEndpoint(actionButton
                        .getObject("command"));
                metaInfo.addUrl(new URL(Objects.requireNonNull(extractCachedUrlIfNeeded(url))));
            } catch (final NullPointerException | MalformedURLException e) {
                throw new ParsingException("Could not get metadata info URL", e);
            }

            final String metaInfoLinkText = YoutubeParsingHelper.getTextFromObject(
                    actionButton.getObject("text"));
            if (isNullOrEmpty(metaInfoLinkText)) {
                throw new ParsingException("Could not get metadata info link text.");
            }
            metaInfo.addUrlText(metaInfoLinkText);
        }

        if (clarificationRenderer.has("secondaryEndpoint") && clarificationRenderer
                .has("secondarySource")) {
            final String url = getUrlFromNavigationEndpoint(clarificationRenderer
                    .getObject("secondaryEndpoint"));
            // Ignore Google URLs, because those point to a Google search about "Covid-19"
            if (url != null && !isGoogleURL(url)) {
                try {
                    metaInfo.addUrl(new URL(url));
                    final String description = getTextFromObject(clarificationRenderer
                            .getObject("secondarySource"));
                    metaInfo.addUrlText(description == null ? url : description);
                } catch (final MalformedURLException e) {
                    throw new ParsingException("Could not get metadata info secondary URL", e);
                }
            }
        }

        return metaInfo;
    }

    /**
     * Sometimes, YouTube provides URLs which use Google's cache. They look like
     * {@code https://webcache.googleusercontent.com/search?q=cache:CACHED_URL}
     *
     * @param url the URL which might refer to the Google's webcache
     * @return the URL which is referring to the original site
     */
    public static String extractCachedUrlIfNeeded(final String url) {
        if (url == null) {
            return null;
        }
        if (url.contains("webcache.googleusercontent.com")) {
            return url.split("cache:")[1];
        }
        return url;
    }

    public static boolean isVerified(final JsonArray badges) {
        if (Utils.isNullOrEmpty(badges)) {
            return false;
        }

        for (final Object badge : badges) {
            final String style = ((JsonObject) badge).getObject("metadataBadgeRenderer")
                    .getString("style");
            if (style != null && (style.equals("BADGE_STYLE_TYPE_VERIFIED")
                    || style.equals("BADGE_STYLE_TYPE_VERIFIED_ARTIST"))) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    public static String unescapeDocument(@Nonnull final String doc) {
        return doc
                .replaceAll("\\\\x22", "\"")
                .replaceAll("\\\\x7b", "{")
                .replaceAll("\\\\x7d", "}")
                .replaceAll("\\\\x5b", "[")
                .replaceAll("\\\\x5d", "]");
    }
}
