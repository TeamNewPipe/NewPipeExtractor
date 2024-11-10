package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.defaultAlertsCheck;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.hasArtistOrVerifiedIconBadgeAttachment;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Shared functions for extracting YouTube channel pages and tabs.
 */
public final class YoutubeChannelHelper {

    private static final String BROWSE_ENDPOINT = "browseEndpoint";
    private static final String BROWSE_ID = "browseId";
    private static final String CAROUSEL_HEADER_RENDERER = "carouselHeaderRenderer";
    private static final String C4_TABBED_HEADER_RENDERER = "c4TabbedHeaderRenderer";
    private static final String CONTENT = "content";
    private static final String CONTENTS = "contents";
    private static final String HEADER = "header";
    private static final String PAGE_HEADER_VIEW_MODEL = "pageHeaderViewModel";
    private static final String TAB_RENDERER = "tabRenderer";
    private static final String TITLE = "title";
    private static final String TOPIC_CHANNEL_DETAILS_RENDERER = "topicChannelDetailsRenderer";

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

            final JsonObject browseEndpoint = endpoint.getObject(BROWSE_ENDPOINT);
            final String browseId = browseEndpoint.getString(BROWSE_ID, "");

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
                            .value(BROWSE_ID, id)
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

            final String browseId = endpoint.getObject(BROWSE_ENDPOINT)
                    .getString(BROWSE_ID, "");

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
    public static final class ChannelHeader implements Serializable {

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
             * This header returns only the channel's name and its avatar for system channels.
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

        public ChannelHeader(@Nonnull final JsonObject json, final HeaderType headerType) {
            this.json = json;
            this.headerType = headerType;
        }
    }

    /**
     * Get a channel header it if exists.
     *
     * @param channelResponse a full channel JSON response
     * @return a {@link ChannelHeader} or {@code null} if no supported header has been found
     */
    @Nullable
    public static ChannelHeader getChannelHeader(
            @Nonnull final JsonObject channelResponse) {
        final JsonObject header = channelResponse.getObject(HEADER);

        if (header.has(C4_TABBED_HEADER_RENDERER)) {
            return Optional.of(header.getObject(C4_TABBED_HEADER_RENDERER))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.C4_TABBED))
                    .orElse(null);
        } else if (header.has(CAROUSEL_HEADER_RENDERER)) {
            return header.getObject(CAROUSEL_HEADER_RENDERER)
                    .getArray(CONTENTS)
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(item -> item.has(TOPIC_CHANNEL_DETAILS_RENDERER))
                    .findFirst()
                    .map(item -> item.getObject(TOPIC_CHANNEL_DETAILS_RENDERER))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.CAROUSEL))
                    .orElse(null);
        } else if (header.has("pageHeaderRenderer")) {
            return Optional.of(header.getObject("pageHeaderRenderer"))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.PAGE))
                    .orElse(null);
        } else if (header.has("interactiveTabbedHeaderRenderer")) {
            return Optional.of(header.getObject("interactiveTabbedHeaderRenderer"))
                    .map(json -> new ChannelHeader(json,
                            ChannelHeader.HeaderType.INTERACTIVE_TABBED))
                    .orElse(null);
        }

        return null;
    }

    /**
     * Check if a channel is verified by using its header.
     *
     * <p>
     * The header is mandatory, so the verified status of age-restricted channels with a
     * {@code channelAgeGateRenderer} cannot be checked.
     * </p>
     *
     * @param channelHeader the {@link ChannelHeader} of a non age-restricted channel
     * @return whether the channel is verified
     */
    public static boolean isChannelVerified(@Nonnull final ChannelHeader channelHeader) {
        switch (channelHeader.headerType) {
            // carouselHeaderRenderers do not contain any verification badges
            // Since they are only shown on YouTube internal channels or on channels of large
            // organizations broadcasting live events, we can assume the channel to be verified
            case CAROUSEL:
                return true;
            case PAGE:
                final JsonObject pageHeaderViewModel = channelHeader.json.getObject(CONTENT)
                        .getObject(PAGE_HEADER_VIEW_MODEL);

                final boolean hasCircleOrMusicIcon = hasArtistOrVerifiedIconBadgeAttachment(
                        pageHeaderViewModel.getObject(TITLE)
                                .getObject("dynamicTextViewModel")
                                .getObject("text")
                                .getArray("attachmentRuns"));
                if (!hasCircleOrMusicIcon && pageHeaderViewModel.getObject("image")
                        .has("contentPreviewImageViewModel")) {
                    // If a pageHeaderRenderer has no object in which a check verified may be
                    // contained and if it has a contentPreviewImageViewModel, it should mean
                    // that the header is coming from a system channel, which we can assume to
                    // be verified
                    return true;
                }

                return hasCircleOrMusicIcon;
            case INTERACTIVE_TABBED:
                // If the header has an autoGenerated property, it should mean that the channel has
                // been auto generated by YouTube: we can assume the channel to be verified in this
                // case
                return channelHeader.json.has("autoGenerated");
            default:
                return YoutubeParsingHelper.isVerified(channelHeader.json.getArray("badges"));
        }
    }

    /**
     * Get the ID of a channel from its response.
     *
     * <p>
     * For {@link ChannelHeader.HeaderType#C4_TABBED c4TabbedHeaderRenderer} and
     * {@link ChannelHeader.HeaderType#CAROUSEL carouselHeaderRenderer} channel headers, the ID is
     * get from the header.
     * </p>
     *
     * <p>
     * For other headers or if it cannot be got, the ID from the {@code channelMetadataRenderer}
     * in the channel response is used.
     * </p>
     *
     * <p>
     * If the ID cannot still be get, the fallback channel ID, if provided, will be used.
     * </p>
     *
     * @param channelHeader     the channel header
     * @param fallbackChannelId the fallback channel ID, which can be null
     * @return the ID of the channel
     * @throws ParsingException if the channel ID cannot be got from the channel header, the
     * channel response and the fallback channel ID
     */
    @Nonnull
    public static String getChannelId(
            @Nullable final ChannelHeader channelHeader,
            @Nonnull final JsonObject jsonResponse,
            @Nullable final String fallbackChannelId) throws ParsingException {
        if (channelHeader != null) {
            switch (channelHeader.headerType) {
                case C4_TABBED:
                    final String channelId = channelHeader.json.getObject(HEADER)
                            .getObject(C4_TABBED_HEADER_RENDERER)
                            .getString("channelId", "");
                    if (!isNullOrEmpty(channelId)) {
                        return channelId;
                    }
                    final String navigationC4TabChannelId = channelHeader.json
                            .getObject("navigationEndpoint")
                            .getObject(BROWSE_ENDPOINT)
                            .getString(BROWSE_ID);
                    if (!isNullOrEmpty(navigationC4TabChannelId)) {
                        return navigationC4TabChannelId;
                    }
                    break;
                case CAROUSEL:
                    final String navigationCarouselChannelId = channelHeader.json.getObject(HEADER)
                            .getObject(CAROUSEL_HEADER_RENDERER)
                            .getArray(CONTENTS)
                            .stream()
                            .filter(JsonObject.class::isInstance)
                            .map(JsonObject.class::cast)
                            .filter(item -> item.has(TOPIC_CHANNEL_DETAILS_RENDERER))
                            .findFirst()
                            .orElse(new JsonObject())
                            .getObject(TOPIC_CHANNEL_DETAILS_RENDERER)
                            .getObject("navigationEndpoint")
                            .getObject(BROWSE_ENDPOINT)
                            .getString(BROWSE_ID);
                    if (!isNullOrEmpty(navigationCarouselChannelId)) {
                        return navigationCarouselChannelId;
                    }
                    break;
                default:
                    break;
            }
        }

        final String externalChannelId = jsonResponse.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString("externalChannelId");
        if (!isNullOrEmpty(externalChannelId)) {
            return externalChannelId;
        }

        if (!isNullOrEmpty(fallbackChannelId)) {
            return fallbackChannelId;
        } else {
            throw new ParsingException("Could not get channel ID");
        }
    }

    @Nonnull
    public static String getChannelName(@Nullable final ChannelHeader channelHeader,
                                        @Nullable final JsonObject channelAgeGateRenderer,
                                        @Nonnull final JsonObject jsonResponse)
            throws ParsingException {
        if (channelAgeGateRenderer != null) {
            final String title = channelAgeGateRenderer.getString("channelTitle");
            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get channel name");
            }
            return title;
        }

        final String metadataRendererTitle = jsonResponse.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString(TITLE);
        if (!isNullOrEmpty(metadataRendererTitle)) {
            return metadataRendererTitle;
        }

        return Optional.ofNullable(channelHeader)
                .map(header -> {
                    final JsonObject channelJson = header.json;
                    switch (header.headerType) {
                        case PAGE:
                            return channelJson.getObject(CONTENT)
                                    .getObject(PAGE_HEADER_VIEW_MODEL)
                                    .getObject(TITLE)
                                    .getObject("dynamicTextViewModel")
                                    .getObject("text")
                                    .getString(CONTENT, channelJson.getString("pageTitle"));
                        case CAROUSEL:
                        case INTERACTIVE_TABBED:
                            return getTextFromObject(channelJson.getObject(TITLE));
                        case C4_TABBED:
                        default:
                            return channelJson.getString(TITLE);
                    }
                })
                // The channel name from a microformatDataRenderer may be different from the one
                // displayed, especially for auto-generated channels, depending on the language
                // requested for the interface (hl parameter of InnerTube requests' payload)
                .or(() -> Optional.ofNullable(jsonResponse.getObject("microformat")
                        .getObject("microformatDataRenderer")
                        .getString(TITLE)))
                .orElseThrow(() -> new ParsingException("Could not get channel name"));
    }

    @Nullable
    public static JsonObject getChannelAgeGateRenderer(@Nonnull final JsonObject jsonResponse) {
        return jsonResponse.getObject(CONTENTS)
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .flatMap(tab -> tab.getObject(TAB_RENDERER)
                        .getObject(CONTENT)
                        .getObject("sectionListRenderer")
                        .getArray(CONTENTS)
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .filter(content -> content.has("channelAgeGateRenderer"))
                .map(content -> content.getObject("channelAgeGateRenderer"))
                .findFirst()
                .orElse(null);
    }
}
