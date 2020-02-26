package org.schabi.newpipe.extractor.services.youtube.linkHandler;


import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Parser;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;

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

    private static final String HARDCODED_CLIENT_VERSION = "2.20200214.04.00";
    private static String clientVersion;


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
        return host.equalsIgnoreCase("invidio.us") || host.equalsIgnoreCase("dev.invidio.us") || host.equalsIgnoreCase("www.invidio.us") || host.equalsIgnoreCase("invidious.snopyta.org") || host.equalsIgnoreCase("de.invidious.snopyta.org") || host.equalsIgnoreCase("fi.invidious.snopyta.org") || host.equalsIgnoreCase("vid.wxzm.sx") || host.equalsIgnoreCase("invidious.kabi.tk") || host.equalsIgnoreCase("invidiou.sh") || host.equalsIgnoreCase("www.invidiou.sh") || host.equalsIgnoreCase("no.invidiou.sh") || host.equalsIgnoreCase("invidious.enkirton.net") || host.equalsIgnoreCase("tube.poal.co") || host.equalsIgnoreCase("invidious.13ad.de") || host.equalsIgnoreCase("yt.elukerio.org");
    }

    public static long parseDurationString(String input)
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
        return ((((Long.parseLong(days) * 24)
                + Long.parseLong(hours) * 60)
                + Long.parseLong(minutes)) * 60)
                + Long.parseLong(seconds);
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

    /**
     * Get the client version from a page
     * @return
     * @throws ParsingException
     */
    public static String getClientVersion() throws ParsingException {
        if (clientVersion != null && !clientVersion.isEmpty()) return clientVersion;

        // Test if hard-coded client version is valid
        try {
            final String url = "https://www.youtube.com/results?search_query=test&pbj=1";

            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-YouTube-Client-Name", Collections.singletonList("1"));
            headers.put("X-YouTube-Client-Version",
                    Collections.singletonList(HARDCODED_CLIENT_VERSION));
            final String response = getDownloader().get(url, headers).responseBody();
            if (response.length() > 50) { // ensure to have a valid response
                clientVersion = HARDCODED_CLIENT_VERSION;
                return clientVersion;
            }
        } catch (Exception ignored) {}

        // Try extracting it from YouTube's website otherwise
        try {
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
                            clientVersion = p.getString("value");
                            return clientVersion;
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
                    if (contextClientVersion != null && !contextClientVersion.isEmpty()) {
                        clientVersion = contextClientVersion;
                        return clientVersion;
                    }
                } catch (Exception ignored) {
                }
            }

            if (shortClientVersion != null) {
                clientVersion = shortClientVersion;
                return clientVersion;
            }
        } catch (Exception ignored) {}

        throw new ParsingException("Could not get client version");
    }
}
