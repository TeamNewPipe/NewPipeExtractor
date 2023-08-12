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
import java.util.Optional;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.defaultAlertsCheck;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Shared functions for extracting YouTube channel pages and tabs.
 */
public final class YoutubeChannelHelper {
    private YoutubeChannelHelper() {
    }

    /**
     * Take a YouTube channel ID or URL path, resolve it if necessary and return a channel ID.
     *
     * @param idOrPath a YouTube channel ID or URL path
     * @return a YouTube channel ID
     * @throws IOException if a channel resolve request failed
     * @throws ExtractionException if a channel resolve request response could not be parsed or is
     * invalid
     */
    @Nonnull
    public static String resolveChannelId(@Nonnull final String idOrPath)
            throws ExtractionException, IOException {
        final String[] channelId = idOrPath.split("/");

        if (channelId[0].startsWith("UC")) {
            return channelId[0];
        }

        // If the URL is not a /channel URL, we need to use the navigation/resolve_url endpoint of
        // the InnerTube API to get the channel id. If this fails or if the URL is not a /channel
        // URL, then no information about the channel associated with this URL was found,
        // so the unresolved url will be returned.
        if (!channelId[0].equals("channel")) {
            final byte[] body = JsonWriter.string(
                    prepareDesktopJsonBuilder(Localization.DEFAULT, ContentCountry.DEFAULT)
                            .value("url", "https://www.youtube.com/" + idOrPath)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse(
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

        // return the unresolved URL
        return channelId[1];
    }

    /**
     * Response data object for {@link #getChannelResponse(String, String, Localization,
     * ContentCountry)}, after any redirection in the allowed redirects count ({@code 3}).
     */
    public static final class ChannelResponseData {

        /**
         * The channel response as a JSON object, after all redirects.
         */
        @Nonnull
        public final JsonObject jsonResponse;

        /**
         * The channel ID after all redirects.
         */
        @Nonnull
        public final String channelId;

        private ChannelResponseData(@Nonnull final JsonObject jsonResponse,
                                    @Nonnull final String channelId) {
            this.jsonResponse = jsonResponse;
            this.channelId = channelId;
        }
    }

    /**
     * Fetch a YouTube channel tab response, using the given channel ID and tab parameters.
     *
     * <p>
     * Redirections to other channels are supported to up to 3 redirects, which could happen for
     * instance for localized channels or for auto-generated ones. For instance, there are three IDs
     * of the auto-generated "Movies and Shows" channel, i.e. {@code UCuJcl0Ju-gPDoksRjK1ya-w},
     * {@code UChBfWrfBXL9wS6tQtgjt_OQ} and {@code UCok7UTQQEP1Rsctxiv3gwSQ}, and they all redirect
     * to the {@code UClgRkhTL3_hImCAmdLfDE4g} one.
     * </p>
     *
     * @param channelId    a valid YouTube channel ID
     * @param parameters   the parameters to specify the YouTube channel tab; if invalid ones are
     *                     specified, YouTube should return the {@code Home} tab
     * @param localization the {@link Localization} to use
     * @param country      the {@link ContentCountry} to use
     * @return a {@link ChannelResponseData channel response data}
     * @throws IOException if a channel request failed
     * @throws ExtractionException if a channel request response could not be parsed or is invalid
     */
    @Nonnull
    public static ChannelResponseData getChannelResponse(@Nonnull final String channelId,
                                                         @Nonnull final String parameters,
                                                         @Nonnull final Localization localization,
                                                         @Nonnull final ContentCountry country)
            throws ExtractionException, IOException {
        String id = channelId;
        JsonObject ajaxJson = null;

        int level = 0;
        while (level < 3) {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                                    localization, country)
                            .value("browseId", id)
                            .value("params", parameters)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse(
                    "browse", body, localization);

            checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final String browseId = endpoint.getObject("browseEndpoint")
                    .getString("browseId", "");

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
            throw new ExtractionException("Got no channel response after 3 redirects");
        }

        defaultAlertsCheck(ajaxJson);

        return new ChannelResponseData(ajaxJson, id);
    }

    /**
     * Assert that a channel JSON response does not contain an {@code error} JSON object.
     *
     * @param jsonResponse a channel JSON response
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

    /**
     * A channel header response.
     *
     * <p>
     * This class allows the distinction between a classic header and a carousel one, used for
     * auto-generated ones like the gaming or music topic channels and for big events such as the
     * Coachella music festival, which have a different data structure and do not return the same
     * properties.
     * </p>
     */
    public static final class ChannelHeader {

        /**
         * Types of supported YouTube channel headers.
         */
        public enum HeaderType {

            /**
             * A {@code c4TabbedHeaderRenderer} channel header type.
             *
             * <p>
             * This header is returned on the majority of channels and contains the channel's name,
             * its banner and its avatar and its subscriber count in most cases.
             * </p>
             */
            C4_TABBED,

            /**
             * An {@code interactiveTabbedHeaderRenderer} channel header type.
             *
             * <p>
             * This header is returned for gaming topic channels, and only contains the channel's
             * name, its banner and a poster as its "avatar".
             * </p>
             */
            INTERACTIVE_TABBED,

            /**
             * A {@code carouselHeaderRenderer} channel header type.
             *
             * <p>
             * This header returns only the channel's name, its avatar and its subscriber count.
             * </p>
             */
            CAROUSEL,

            /**
             * A {@code pageHeaderRenderer} channel header type.
             *
             * <p>
             * This header returns only the channel's name and its avatar.
             * </p>
             */
            PAGE
        }

        /**
         * The channel header JSON response.
         */
        @Nonnull
        public final JsonObject json;

        /**
         * The type of the channel header.
         *
         * <p>
         * See the documentation of the {@link HeaderType} class for more details.
         * </p>
         */
        public final HeaderType headerType;

        private ChannelHeader(@Nonnull final JsonObject json, final HeaderType headerType) {
            this.json = json;
            this.headerType = headerType;
        }
    }

    /**
     * Get a channel header as an {@link Optional} it if exists.
     *
     * @param channelResponse a full channel JSON response
     * @return an {@link Optional} containing a {@link ChannelHeader} or an empty {@link Optional}
     * if no supported header has been found
     */
    @Nonnull
    public static Optional<ChannelHeader> getChannelHeader(
            @Nonnull final JsonObject channelResponse) {
        final JsonObject header = channelResponse.getObject("header");

        if (header.has("c4TabbedHeaderRenderer")) {
            return Optional.of(header.getObject("c4TabbedHeaderRenderer"))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.C4_TABBED));
        } else if (header.has("carouselHeaderRenderer")) {
            return header.getObject("carouselHeaderRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(item -> item.has("topicChannelDetailsRenderer"))
                    .findFirst()
                    .map(item -> item.getObject("topicChannelDetailsRenderer"))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.CAROUSEL));
        } else if (header.has("pageHeaderRenderer")) {
            return Optional.of(header.getObject("pageHeaderRenderer"))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.PAGE));
        } else if (header.has("interactiveTabbedHeaderRenderer")) {
            return Optional.of(header.getObject("interactiveTabbedHeaderRenderer"))
                    .map(json -> new ChannelHeader(json,
                            ChannelHeader.HeaderType.INTERACTIVE_TABBED));
        } else {
            return Optional.empty();
        }
    }
}
