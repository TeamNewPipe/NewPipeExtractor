package org.schabi.newpipe.extractor.services.soundcloud;

import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public final class SoundcloudParsingHelper {
    private static String clientId;
    public static final String SOUNDCLOUD_API_V2_URL = "https://api-v2.soundcloud.com/";

    private SoundcloudParsingHelper() {
    }

    public static synchronized String clientId() throws ExtractionException, IOException {
        if (!isNullOrEmpty(clientId)) {
            return clientId;
        }

        final Downloader dl = NewPipe.getDownloader();

        final Response download = dl.get("https://soundcloud.com");
        final String responseBody = download.responseBody();
        final String clientIdPattern = ",client_id:\"(.*?)\"";

        final Document doc = Jsoup.parse(responseBody);
        final Elements possibleScripts = doc.select(
                "script[src*=\"sndcdn.com/assets/\"][src$=\".js\"]");
        // The one containing the client id will likely be the last one
        Collections.reverse(possibleScripts);

        final var headers = Map.of("Range", List.of("bytes=0-50000"));

        for (final Element element : possibleScripts) {
            final String srcUrl = element.attr("src");
            if (!isNullOrEmpty(srcUrl)) {
                try {
                    clientId = Parser.matchGroup1(clientIdPattern, dl.get(srcUrl, headers)
                            .responseBody());
                    return clientId;
                } catch (final RegexException ignored) {
                    // Ignore it and proceed to try searching other script
                }
            }
        }

        // Officially give up
        throw new ExtractionException("Couldn't extract client id");
    }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (final DateTimeParseException e1) {
            try {
                return OffsetDateTime.parse(textualUploadDate, DateTimeFormatter
                        .ofPattern("yyyy/MM/dd HH:mm:ss +0000"));
            } catch (final DateTimeParseException e2) {
                throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\""
                        + ", " + e1.getMessage(), e2);
            }
        }
    }

    /**
     * Call the endpoint "/resolve" of the API.<p>
     * <p>
     * See https://developers.soundcloud.com/docs/api/reference#resolve
     */
    public static JsonObject resolveFor(@Nonnull final Downloader downloader, final String url)
            throws IOException, ExtractionException {
        final String apiUrl = SOUNDCLOUD_API_V2_URL + "resolve"
                + "?url=" + Utils.encodeUrlUtf8(url)
                + "&client_id=" + clientId();

        try {
            final String response = downloader.get(apiUrl, SoundCloud.getLocalization())
                    .responseBody();
            return JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }

    /**
     * Fetch the embed player with the apiUrl and return the canonical url (like the permalink_url
     * from the json API).
     *
     * @return the url resolved
     */
    public static String resolveUrlWithEmbedPlayer(final String apiUrl) throws IOException,
            ReCaptchaException {

        final String response = NewPipe.getDownloader().get("https://w.soundcloud.com/player/?url="
                + Utils.encodeUrlUtf8(apiUrl), SoundCloud.getLocalization()).responseBody();

        return Jsoup.parse(response).select("link[rel=\"canonical\"]").first()
                .attr("abs:href");
    }

    /**
     * Fetch the widget API with the url and return the id (like the id from the json API).
     *
     * @return the resolved id
     */
    public static String resolveIdWithWidgetApi(final String urlString) throws IOException,
            ParsingException {
        // Remove the tailing slash from URLs due to issues with the SoundCloud API
        String fixedUrl = urlString;
        if (fixedUrl.charAt(fixedUrl.length() - 1) == '/') {
            fixedUrl = fixedUrl.substring(0, fixedUrl.length() - 1);
        }
        // Make URL lower case and remove m. and www. if it exists.
        // Without doing this, the widget API does not recognize the URL.
        fixedUrl = Utils.removeMAndWWWFromUrl(fixedUrl.toLowerCase());

        final URL url;
        try {
            url = Utils.stringToURL(fixedUrl);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is not valid");
        }

        try {
            final String widgetUrl = "https://api-widget.soundcloud.com/resolve?url="
                    + Utils.encodeUrlUtf8(url.toString())
                    + "&format=json&client_id=" + SoundcloudParsingHelper.clientId();
            final String response = NewPipe.getDownloader().get(widgetUrl,
                    SoundCloud.getLocalization()).responseBody();
            final JsonObject o = JsonParser.object().from(response);
            return String.valueOf(JsonUtils.getValue(o, "id"));
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON response", e);
        } catch (final ExtractionException e) {
            throw new ParsingException(
                    "Could not resolve id with embedded player. ClientId not extracted", e);
        }
    }

    /**
     * Fetch the users from the given API and commit each of them to the collector.
     * <p>
     * This differ from {@link #getUsersFromApi(ChannelInfoItemsCollector, String)} in the sense
     * that they will always get MIN_ITEMS or more.
     *
     * @param minItems the method will return only when it have extracted that many items
     *                 (equal or more)
     */
    public static String getUsersFromApiMinItems(final int minItems,
                                                 final ChannelInfoItemsCollector collector,
                                                 final String apiUrl) throws IOException,
            ReCaptchaException, ParsingException {
        String nextPageUrl = SoundcloudParsingHelper.getUsersFromApi(collector, apiUrl);

        while (!nextPageUrl.isEmpty() && collector.getItems().size() < minItems) {
            nextPageUrl = SoundcloudParsingHelper.getUsersFromApi(collector, nextPageUrl);
        }

        return nextPageUrl;
    }

    /**
     * Fetch the user items from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */
    public static String getUsersFromApi(final ChannelInfoItemsCollector collector,
                                         final String apiUrl) throws IOException,
            ReCaptchaException, ParsingException {
        final String response = NewPipe.getDownloader().get(apiUrl, SoundCloud.getLocalization())
                .responseBody();
        final JsonObject responseObject;

        try {
            responseObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        final JsonArray responseCollection = responseObject.getArray("collection");
        for (final Object o : responseCollection) {
            if (o instanceof JsonObject) {
                final JsonObject object = (JsonObject) o;
                collector.commit(new SoundcloudChannelInfoItemExtractor(object));
            }
        }

        String nextPageUrl;
        try {
            nextPageUrl = responseObject.getString("next_href");
            if (!nextPageUrl.contains("client_id=")) {
                nextPageUrl += "&client_id=" + SoundcloudParsingHelper.clientId();
            }
        } catch (final Exception ignored) {
            nextPageUrl = "";
        }

        return nextPageUrl;
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     * <p>
     * This differ from {@link #getStreamsFromApi(StreamInfoItemsCollector, String)} in the sense
     * that they will always get MIN_ITEMS or more items.
     *
     * @param minItems the method will return only when it have extracted that many items
     *                 (equal or more)
     */
    public static String getStreamsFromApiMinItems(final int minItems,
                                                   final StreamInfoItemsCollector collector,
                                                   final String apiUrl) throws IOException,
            ReCaptchaException, ParsingException {
        String nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl);

        while (!nextPageUrl.isEmpty() && collector.getItems().size() < minItems) {
            nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, nextPageUrl);
        }

        return nextPageUrl;
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */
    public static String getStreamsFromApi(final StreamInfoItemsCollector collector,
                                           final String apiUrl,
                                           final boolean charts) throws IOException,
            ReCaptchaException, ParsingException {
        final Response response = NewPipe.getDownloader().get(apiUrl, SoundCloud
                .getLocalization());
        if (response.responseCode() >= 400) {
            throw new IOException("Could not get streams from API, HTTP " + response
                    .responseCode());
        }

        final JsonObject responseObject;
        try {
            responseObject = JsonParser.object().from(response.responseBody());
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        final JsonArray responseCollection = responseObject.getArray("collection");
        for (final Object o : responseCollection) {
            if (o instanceof JsonObject) {
                final JsonObject object = (JsonObject) o;
                collector.commit(new SoundcloudStreamInfoItemExtractor(charts
                        ? object.getObject("track") : object));
            }
        }

        String nextPageUrl;
        try {
            nextPageUrl = responseObject.getString("next_href");
            if (!nextPageUrl.contains("client_id=")) {
                nextPageUrl += "&client_id=" + SoundcloudParsingHelper.clientId();
            }
        } catch (final Exception ignored) {
            nextPageUrl = "";
        }

        return nextPageUrl;
    }

    public static String getStreamsFromApi(final StreamInfoItemsCollector collector,
                                           final String apiUrl) throws ReCaptchaException,
            ParsingException, IOException {
        return getStreamsFromApi(collector, apiUrl, false);
    }

    @Nonnull
    public static String getUploaderUrl(final JsonObject object) {
        final String url = object.getObject("user").getString("permalink_url", "");
        return replaceHttpWithHttps(url);
    }

    @Nonnull
    public static String getAvatarUrl(final JsonObject object) {
        final String url = object.getObject("user").getString("avatar_url", "");
        return replaceHttpWithHttps(url);
    }

    public static String getUploaderName(final JsonObject object) {
        return object.getObject("user").getString("username", "");
    }
}
