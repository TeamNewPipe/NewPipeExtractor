package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;
import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.*;

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

public class YoutubeParsingHelper {

    private YoutubeParsingHelper() {
    }

    /**
     * The official youtube app supports intents in this format, where after the ':' is the videoId.
     * Accordingly there are other apps sharing streams in this format.
     */
    public final static String BASE_YOUTUBE_INTENT_URL = "vnd.youtube";

    private static final String HARDCODED_CLIENT_VERSION = "2.20200214.04.00";
    private static String clientVersion;

    private static final String[] HARDCODED_YOUTUBE_MUSIC_KEYS = {"AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30", "67", "0.1"};
    private static String[] youtubeMusicKeys;

    private static final String FEED_BASE_CHANNEL_ID = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    private static final String FEED_BASE_USER = "https://www.youtube.com/feeds/videos.xml?user=";

    private static final String[] RECAPTCHA_DETECTION_SELECTORS = {
            "form[action*=\"/das_captcha\"]",
            "input[name*=\"action_recaptcha_verify\"]"
    };

    public static Document parseAndCheckPage(final String url, final Response response) throws ReCaptchaException {
        final Document document = Jsoup.parse(response.responseBody(), url);

        for (String detectionSelector : RECAPTCHA_DETECTION_SELECTORS) {
            if (!document.select(detectionSelector).isEmpty()) {
                throw new ReCaptchaException("reCAPTCHA challenge requested (detected with selector: \"" + detectionSelector + "\")", url);
            }
        }

        return document;
    }

    public static boolean isYoutubeURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("youtube.com") || host.equalsIgnoreCase("www.youtube.com")
                || host.equalsIgnoreCase("m.youtube.com") || host.equalsIgnoreCase("music.youtube.com");
    }

    public static boolean isYoutubeServiceURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("www.youtube-nocookie.com") || host.equalsIgnoreCase("youtu.be");
    }

    public static boolean isHooktubeURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("hooktube.com");
    }

    public static boolean isInvidioURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("invidio.us")
                || host.equalsIgnoreCase("dev.invidio.us")
                || host.equalsIgnoreCase("www.invidio.us")
                || host.equalsIgnoreCase("invidious.snopyta.org")
                || host.equalsIgnoreCase("fi.invidious.snopyta.org")
                || host.equalsIgnoreCase("yewtu.be")
                || host.equalsIgnoreCase("invidious.ggc-project.de")
                || host.equalsIgnoreCase("yt.maisputain.ovh")
                || host.equalsIgnoreCase("invidious.13ad.de")
                || host.equalsIgnoreCase("invidious.toot.koeln")
                || host.equalsIgnoreCase("invidious.fdn.fr")
                || host.equalsIgnoreCase("watch.nettohikari.com")
                || host.equalsIgnoreCase("invidious.snwmds.net")
                || host.equalsIgnoreCase("invidious.snwmds.org")
                || host.equalsIgnoreCase("invidious.snwmds.com")
                || host.equalsIgnoreCase("invidious.sunsetravens.com")
                || host.equalsIgnoreCase("invidious.gachirangers.com");
    }

    /**
     * Parses the duration string of the video expecting ":" or "." as separators
     * @return the duration in seconds
     * @throws ParsingException when more than 3 separators are found
     */
    public static int parseDurationString(final String input)
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

        return ((Integer.parseInt(Utils.removeNonDigitCharacters(days)) * 24
                + Integer.parseInt(Utils.removeNonDigitCharacters(hours))) * 60
                + Integer.parseInt(Utils.removeNonDigitCharacters(minutes))) * 60
                + Integer.parseInt(Utils.removeNonDigitCharacters(seconds));
    }

    public static String getFeedUrlFrom(final String channelIdOrUser) {
        if (channelIdOrUser.startsWith("user/")) {
            return FEED_BASE_USER + channelIdOrUser.replace("user/", "");
        } else if (channelIdOrUser.startsWith("channel/")) {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser.replace("channel/", "");
        } else {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser;
        }
    }

    public static Calendar parseDateFrom(String textualUploadDate) throws ParsingException {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(textualUploadDate);
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }

        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(date);
        return uploadDate;
    }

    public static JsonObject getInitialData(String html) throws ParsingException {
        try {
            String initialData = Parser.matchGroup1("window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});", html);
            return JsonParser.object().from(initialData);
        } catch (JsonParserException | Parser.RegexException e) {
            throw new ParsingException("Could not get ytInitialData", e);
        }
    }

    public static boolean isHardcodedClientVersionValid() throws IOException, ExtractionException {
        final String url = "https://www.youtube.com/results?search_query=test&pbj=1";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
        headers.put("X-YouTube-Client-Version",
                Collections.singletonList(HARDCODED_CLIENT_VERSION));
        final String response = getDownloader().get(url, headers).responseBody();

        return response.length() > 50; // ensure to have a valid response
    }

    /**
     * Get the client version from a page
     * @return
     * @throws ParsingException
     */
    public static String getClientVersion() throws IOException, ExtractionException {
        if (!isNullOrEmpty(clientVersion)) return clientVersion;
        if (isHardcodedClientVersionValid()) return clientVersion = HARDCODED_CLIENT_VERSION;

        final String url = "https://www.youtube.com/results?search_query=test";
        final String html = getDownloader().get(url).responseBody();
        JsonObject initialData = getInitialData(html);
        JsonArray serviceTrackingParams = initialData.getObject("responseContext").getArray("serviceTrackingParams");
        String shortClientVersion = null;

        // try to get version from initial data first
        for (Object service : serviceTrackingParams) {
            JsonObject s = (JsonObject) service;
            if (s.getString("service").equals("CSI")) {
                JsonArray params = s.getArray("params");
                for (Object param : params) {
                    JsonObject p = (JsonObject) param;
                    String key = p.getString("key");
                    if (key != null && key.equals("cver")) {
                        return clientVersion = p.getString("value");
                    }
                }
            } else if (s.getString("service").equals("ECATCHER")) {
                // fallback to get a shortened client version which does not contain the last two digits
                JsonArray params = s.getArray("params");
                for (Object param : params) {
                    JsonObject p = (JsonObject) param;
                    String key = p.getString("key");
                    if (key != null && key.equals("client.version")) {
                        shortClientVersion = p.getString("value");
                    }
                }
            }
        }

        String contextClientVersion;
        String[] patterns = {
                "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"",
                "innertube_context_client_version\":\"([0-9\\.]+?)\"",
                "client.version=([0-9\\.]+)"
        };
        for (String pattern : patterns) {
            try {
                contextClientVersion = Parser.matchGroup1(pattern, html);
                if (!isNullOrEmpty(contextClientVersion)) {
                    return clientVersion = contextClientVersion;
                }
            } catch (Exception ignored) {
            }
        }

        if (shortClientVersion != null) {
            return clientVersion = shortClientVersion;
        }

        throw new ParsingException("Could not get client version");
    }

    public static boolean areHardcodedYoutubeMusicKeysValid() throws IOException, ReCaptchaException {
        final String url = "https://music.youtube.com/youtubei/v1/search?alt=json&key=" + HARDCODED_YOUTUBE_MUSIC_KEYS[0];

        // @formatter:off
        byte[] json = JsonWriter.string()
            .object()
                .object("context")
                    .object("client")
                        .value("clientName", "WEB_REMIX")
                        .value("clientVersion", HARDCODED_YOUTUBE_MUSIC_KEYS[2])
                        .value("hl", "en")
                        .value("gl", "GB")
                        .array("experimentIds").end()
                        .value("experimentsToken", "")
                        .value("utcOffsetMinutes", 0)
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
                .value("query", "test")
                .value("params", "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D")
            .end().done().getBytes("UTF-8");
        // @formatter:on

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList(HARDCODED_YOUTUBE_MUSIC_KEYS[1]));
        headers.put("X-YouTube-Client-Version", Collections.singletonList(HARDCODED_YOUTUBE_MUSIC_KEYS[2]));
        headers.put("Origin", Collections.singletonList("https://music.youtube.com"));
        headers.put("Referer", Collections.singletonList("music.youtube.com"));
        headers.put("Content-Type", Collections.singletonList("application/json"));

        String response = getDownloader().post(url, headers, json).responseBody();

        return response.length() > 50; // ensure to have a valid response
    }

    public static String[] getYoutubeMusicKeys() throws IOException, ReCaptchaException, Parser.RegexException {
        if (youtubeMusicKeys != null && youtubeMusicKeys.length == 3) return youtubeMusicKeys;
        if (areHardcodedYoutubeMusicKeysValid()) return youtubeMusicKeys = HARDCODED_YOUTUBE_MUSIC_KEYS;

        final String url = "https://music.youtube.com/";
        final String html = getDownloader().get(url).responseBody();

        String key;
        try {
            key = Parser.matchGroup1("INNERTUBE_API_KEY\":\"([0-9a-zA-Z_-]+?)\"", html);
        } catch (Parser.RegexException e) {
            key = Parser.matchGroup1("innertube_api_key\":\"([0-9a-zA-Z_-]+?)\"", html);
        }

        final String clientName = Parser.matchGroup1("INNERTUBE_CONTEXT_CLIENT_NAME\":([0-9]+?),", html);

        String clientVersion;
        try {
            clientVersion = Parser.matchGroup1("INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"", html);
        } catch (Parser.RegexException e) {
            try {
                clientVersion = Parser.matchGroup1("INNERTUBE_CLIENT_VERSION\":\"([0-9\\.]+?)\"", html);
            } catch (Parser.RegexException ee) {
                clientVersion = Parser.matchGroup1("innertube_context_client_version\":\"([0-9\\.]+?)\"", html);
            }
        }

        return youtubeMusicKeys = new String[]{key, clientName, clientVersion};
    }

    public static String getUrlFromNavigationEndpoint(JsonObject navigationEndpoint) throws ParsingException {
        if (navigationEndpoint.has("urlEndpoint")) {
            String internUrl = navigationEndpoint.getObject("urlEndpoint").getString("url");
            if (internUrl.startsWith("/redirect?")) {
                // q parameter can be the first parameter
                internUrl = internUrl.substring(10);
                String[] params = internUrl.split("&");
                for (String param : params) {
                    if (param.split("=")[0].equals("q")) {
                        String url;
                        try {
                            url = URLDecoder.decode(param.split("=")[1], "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return null;
                        }
                        return url;
                    }
                }
            } else if (internUrl.startsWith("http")) {
                return internUrl;
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

            throw new ParsingException("canonicalBaseUrl is null and browseId is not a channel (\"" + browseEndpoint + "\")");
        } else if (navigationEndpoint.has("watchEndpoint")) {
            StringBuilder url = new StringBuilder();
            url.append("https://www.youtube.com/watch?v=").append(navigationEndpoint.getObject("watchEndpoint").getString("videoId"));
            if (navigationEndpoint.getObject("watchEndpoint").has("playlistId"))
                url.append("&amp;list=").append(navigationEndpoint.getObject("watchEndpoint").getString("playlistId"));
            if (navigationEndpoint.getObject("watchEndpoint").has("startTimeSeconds"))
                url.append("&amp;t=").append(navigationEndpoint.getObject("watchEndpoint").getInt("startTimeSeconds"));
            return url.toString();
        } else if (navigationEndpoint.has("watchPlaylistEndpoint")) {
            return "https://www.youtube.com/playlist?list=" +
                    navigationEndpoint.getObject("watchPlaylistEndpoint").getString("playlistId");
        }
        return null;
    }

    /**
     * Get the text from a JSON object that has either a simpleText or a runs array.
     * @param textObject JSON object to get the text from
     * @param html       whether to return HTML, by parsing the navigationEndpoint
     * @return text in the JSON object or {@code null}
     */
    public static String getTextFromObject(JsonObject textObject, boolean html) throws ParsingException {
        if (isNullOrEmpty(textObject)) return null;

        if (textObject.has("simpleText")) return textObject.getString("simpleText");

        if (textObject.getArray("runs").isEmpty()) return null;

        StringBuilder textBuilder = new StringBuilder();
        for (Object textPart : textObject.getArray("runs")) {
            String text = ((JsonObject) textPart).getString("text");
            if (html && ((JsonObject) textPart).has("navigationEndpoint")) {
                String url = getUrlFromNavigationEndpoint(((JsonObject) textPart).getObject("navigationEndpoint"));
                if (!isNullOrEmpty(url)) {
                    textBuilder.append("<a href=\"").append(url).append("\">").append(text).append("</a>");
                    continue;
                }
            }
            textBuilder.append(text);
        }

        String text = textBuilder.toString();

        if (html) {
            text = text.replaceAll("\\n", "<br>");
            text = text.replaceAll("  ", " &nbsp;");
        }

        return text;
    }

    public static String getTextFromObject(JsonObject textObject) throws ParsingException {
        return getTextFromObject(textObject, false);
    }

    public static String fixThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl.startsWith("//")) {
            thumbnailUrl = thumbnailUrl.substring(2);
        }

        if (thumbnailUrl.startsWith(HTTP)) {
            thumbnailUrl = Utils.replaceHttpWithHttps(thumbnailUrl);
        } else if (!thumbnailUrl.startsWith(HTTPS)) {
            thumbnailUrl = "https://" + thumbnailUrl;
        }

        return thumbnailUrl;
    }

    public static String getValidJsonResponseBody(final Response response)
            throws ParsingException, MalformedURLException {
        if (response.responseCode() == 404) {
            throw new ContentNotAvailableException("Not found" +
                    " (\"" + response.responseCode() + " " + response.responseMessage() + "\")");
        }

        final String responseBody = response.responseBody();
        if (responseBody.length() < 50) { // ensure to have a valid response
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
            throw new ParsingException("Got HTML document, expected JSON response" +
                    " (latest url was: \"" + response.latestUrl() + "\")");
        }

        return responseBody;
    }

    public static JsonArray getJsonResponse(final String url, final Localization localization)
            throws IOException, ExtractionException {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
        headers.put("X-YouTube-Client-Version", Collections.singletonList(getClientVersion()));
        final Response response = getDownloader().get(url, headers, localization);

        final String responseBody = getValidJsonResponseBody(response);

        try {
            return JsonParser.array().from(responseBody);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }
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
    public static void defaultAlertsCheck(final JsonObject initialData) throws ParsingException {
        final JsonArray alerts = initialData.getArray("alerts");
        if (!isNullOrEmpty(alerts)) {
            final JsonObject alertRenderer = alerts.getObject(0).getObject("alertRenderer");
            final String alertText = getTextFromObject(alertRenderer.getObject("text"));
            final String alertType = alertRenderer.getString("type", EMPTY_STRING);
            if (alertType.equalsIgnoreCase("ERROR")) {
                throw new ContentNotAvailableException("Got error: \"" + alertText + "\"");
            }
        }
    }
}
