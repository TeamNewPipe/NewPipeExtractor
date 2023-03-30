package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Shared functions for extracting YouTube channel pages and tabs.
 */
public final class YouTubeChannelHelper {
    private YouTubeChannelHelper() {
    }

    /**
     * Take a YouTube channel ID or URL path, resolve it if necessary and return a channel ID.
     * @param idOrPath YouTube channel ID or URL path
     * @return YouTube Channel ID
     */
    public static String resolveChannelId(final String idOrPath)
            throws ExtractionException, IOException {
        final String[] channelId = idOrPath.split("/");

        if (channelId[0].startsWith("UC")) {
            return channelId[0];
        }

        // If the url is an URL which is not a /channel URL, we need to use the
        // navigation/resolve_url endpoint of the InnerTube API to get the channel id. Otherwise,
        // we couldn't get information about the channel associated with this URL, if there is one.
        if (!channelId[0].equals("channel")) {
            final byte[] body = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(
                            Localization.DEFAULT, ContentCountry.DEFAULT)
                            .value("url", "https://www.youtube.com/" + idOrPath)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = YoutubeParsingHelper.getJsonPostResponse(
                    "navigation/resolve_url", body, Localization.DEFAULT);

            checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final JsonObject browseEndpoint = endpoint.getObject("browseEndpoint");
            final String browseId = browseEndpoint.getString("browseId", "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                return browseId;
            }
        }
        return channelId[1];
    }

    /**
     * Response data object for
     * {@link #getChannelResponse(String, String, Localization, ContentCountry)}
     */
    public static final class ChannelResponseData {
        public final JsonObject responseJson;
        public final String channelId;

        private ChannelResponseData(final JsonObject responseJson, final String channelId) {
            this.responseJson = responseJson;
            this.channelId = channelId;
        }
    }

    /**
     * Fetch YouTube channel data.
     * <p>Parameter list:</p>
     * <ul>
     *     <li>Videos: {@code EgZ2aWRlb3PyBgQKAjoA}</li>
     *     <li>Shorts: {@code EgZzaG9ydHPyBgUKA5oBAA%3D%3D}</li>
     *     <li>Livestreams: {@code EgdzdHJlYW1z8gYECgJ6AA%3D%3D}</li>
     *     <li>Playlists: {@code EglwbGF5bGlzdHMgAQ%3D%3D}</li>
     *     <li>Info: {@code EgVhYm91dPIGBAoCEgA%3D}</li>
     * </ul>
     *
     * @param channelId YouTube channel ID
     * @param params Parameters to specify the YouTube channel tab
     * @param loc YouTube localization
     * @param country YouTube content country
     * @return Channel response data
     */
    public static ChannelResponseData getChannelResponse(final String channelId,
                                                         final String params,
                                                         final Localization loc,
                                                         final ContentCountry country)
            throws ExtractionException, IOException {
        String id = channelId;
        JsonObject ajaxJson = null;

        int level = 0;
        while (level < 3) {
            final byte[] body = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(
                            loc, country)
                            .value("browseId", id)
                            .value("params", params) // Equal to videos
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = YoutubeParsingHelper.getJsonPostResponse(
                    "browse", body, loc);

            checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final String browseId = endpoint.getObject("browseEndpoint").getString("browseId",
                    "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                id = browseId;
                level++;
            } else {
                ajaxJson = jsonResponse;
                break;
            }
        }

        if (ajaxJson == null) {
            throw new ExtractionException("Got no channel response");
        }

        YoutubeParsingHelper.defaultAlertsCheck(ajaxJson);

        return new ChannelResponseData(ajaxJson, id);
    }

    /**
     * Assert that a channel JSON response does not contain a 404 error.
     * @param jsonResponse channel JSON response
     * @throws ContentNotAvailableException if the channel was not found
     */
    private static void checkIfChannelResponseIsValid(@Nonnull final JsonObject jsonResponse)
            throws ContentNotAvailableException {
        if (!isNullOrEmpty(jsonResponse.getObject("error"))) {
            final JsonObject errorJsonObject = jsonResponse.getObject("error");
            final int errorCode = errorJsonObject.getInt("code");
            if (errorCode == 404) {
                throw new ContentNotAvailableException("This channel doesn't exist.");
            } else {
                throw new ContentNotAvailableException("Got error:\""
                        + errorJsonObject.getString("status") + "\": "
                        + errorJsonObject.getString("message"));
            }
        }
    }
}
