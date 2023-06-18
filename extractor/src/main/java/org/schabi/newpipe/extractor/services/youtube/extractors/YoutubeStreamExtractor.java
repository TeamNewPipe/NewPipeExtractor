/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) Christian Schabesberger 2019 <chris.schabesberger@mailbox.org>
 * YoutubeStreamExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.ItagItem.APPROX_DURATION_MS_UNKNOWN;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.CONTENT_LENGTH_UNKNOWN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CPN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.createDesktopPlayerBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.generateContentPlaybackNonce;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.generateTParameter;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonAndroidPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonIosPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getAttributedDescription;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareAndroidMobileJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareIosMobileJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.PrivateContentException;
import org.schabi.newpipe.extractor.exceptions.YoutubeMusicPremiumContentException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeThrottlingDecrypter;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.LocaleCompat;
import org.schabi.newpipe.extractor.utils.Pair;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeStreamExtractor extends StreamExtractor {
    /*//////////////////////////////////////////////////////////////////////////
    // Exceptions
    //////////////////////////////////////////////////////////////////////////*/

    public static class DeobfuscateException extends ParsingException {
        DeobfuscateException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /*////////////////////////////////////////////////////////////////////////*/

    @Nullable
    private static String cachedDeobfuscationCode = null;
    @Nullable
    private static String sts = null;
    @Nullable
    private static String playerCode = null;

    private static boolean isAndroidClientFetchForced = false;
    private static boolean isIosClientFetchForced = false;

    private JsonObject playerResponse;
    private JsonObject nextResponse;

    @Nullable
    private JsonObject html5StreamingData;
    @Nullable
    private JsonObject androidStreamingData;
    @Nullable
    private JsonObject iosStreamingData;

    private JsonObject videoPrimaryInfoRenderer;
    private JsonObject videoSecondaryInfoRenderer;
    private JsonObject playerMicroFormatRenderer;
    private int ageLimit = -1;
    private StreamType streamType;

    // We need to store the contentPlaybackNonces because we need to append them to videoplayback
    // URLs (with the cpn parameter).
    // Also because a nonce should be unique, it should be different between clients used, so
    // three different strings are used.
    private String html5Cpn;
    private String androidCpn;
    private String iosCpn;

    public YoutubeStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Impl
    //////////////////////////////////////////////////////////////////////////*/

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        String title;

        // Try to get the video's original title, which is untranslated
        title = playerResponse.getObject("videoDetails").getString("title");

        if (isNullOrEmpty(title)) {
            title = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("title"));

            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get name");
            }
        }

        return title;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (!playerMicroFormatRenderer.getString("uploadDate", "").isEmpty()) {
            return playerMicroFormatRenderer.getString("uploadDate");
        } else if (!playerMicroFormatRenderer.getString("publishDate", "").isEmpty()) {
            return playerMicroFormatRenderer.getString("publishDate");
        }

        final JsonObject liveDetails = playerMicroFormatRenderer.getObject(
                "liveBroadcastDetails");
        if (!liveDetails.getString("endTimestamp", "").isEmpty()) {
            // an ended live stream
            return liveDetails.getString("endTimestamp");
        } else if (!liveDetails.getString("startTimestamp", "").isEmpty()) {
            // a running live stream
            return liveDetails.getString("startTimestamp");
        } else if (getStreamType() == StreamType.LIVE_STREAM) {
            // this should never be reached, but a live stream without upload date is valid
            return null;
        }

        final String videoPrimaryInfoRendererDateText =
                getTextFromObject(getVideoPrimaryInfoRenderer().getObject("dateText"));

        if (videoPrimaryInfoRendererDateText != null) {
            if (videoPrimaryInfoRendererDateText.startsWith("Premiered")) {
                final String time = videoPrimaryInfoRendererDateText.substring(13);

                try { // Premiered 20 hours ago
                    final TimeAgoParser timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(
                            Localization.fromLocalizationCode("en"));
                    final OffsetDateTime parsedTime = timeAgoParser.parse(time).offsetDateTime();
                    return DateTimeFormatter.ISO_LOCAL_DATE.format(parsedTime);
                } catch (final Exception ignored) {
                }

                try { // Premiered Feb 21, 2020
                    final LocalDate localDate = LocalDate.parse(time,
                            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));
                    return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
                } catch (final Exception ignored) {
                }

                try { // Premiered on 21 Feb 2020
                    final LocalDate localDate = LocalDate.parse(time,
                            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
                    return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
                } catch (final Exception ignored) {
                }
            }

            try {
                // TODO: this parses English formatted dates only, we need a better approach to
                //  parse the textual date
                final LocalDate localDate = LocalDate.parse(videoPrimaryInfoRendererDateText,
                        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
                return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
            } catch (final Exception e) {
                throw new ParsingException("Could not get upload date", e);
            }
        }

        throw new ParsingException("Could not get upload date");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();

        if (isNullOrEmpty(textualUploadDate)) {
            return null;
        }

        return new DateWrapper(YoutubeParsingHelper.parseDateFrom(textualUploadDate), true);
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        assertPageFetched();
        try {
            final JsonArray thumbnails = playerResponse
                    .getObject("videoDetails")
                    .getObject("thumbnail")
                    .getArray("thumbnails");
            // the last thumbnail is the one with the highest resolution
            final String url = thumbnails
                    .getObject(thumbnails.size() - 1)
                    .getString("url");

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnail url");
        }

    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        assertPageFetched();
        // Description with more info on links
        final String videoSecondaryInfoRendererDescription = getTextFromObject(
                getVideoSecondaryInfoRenderer().getObject("description"),
                true);
        if (!isNullOrEmpty(videoSecondaryInfoRendererDescription)) {
            return new Description(videoSecondaryInfoRendererDescription, Description.HTML);
        }

        final String attributedDescription = getAttributedDescription(
                getVideoSecondaryInfoRenderer().getObject("attributedDescription"));
        if (!isNullOrEmpty(attributedDescription)) {
            return new Description(attributedDescription, Description.HTML);
        }

        String description = playerResponse.getObject("videoDetails")
                .getString("shortDescription");
        if (description == null) {
            final JsonObject descriptionObject = playerMicroFormatRenderer.getObject("description");
            description = getTextFromObject(descriptionObject);
        }

        // Raw non-html description
        return new Description(description, Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        if (ageLimit != -1) {
            return ageLimit;
        }

        final boolean ageRestricted = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer")
                .getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .stream()
                // Only JsonObjects allowed
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .flatMap(metadataRow -> metadataRow
                        .getObject("metadataRowRenderer")
                        .getArray("contents")
                        .stream()
                        // Only JsonObjects allowed
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .flatMap(content -> content
                        .getArray("runs")
                        .stream()
                        // Only JsonObjects allowed
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .map(run -> run.getString("text", ""))
                .anyMatch(rowText -> rowText.contains("Age-restricted"));

        ageLimit = ageRestricted ? 18 : NO_AGE_LIMIT;
        return ageLimit;
    }

    @Override
    public long getLength() throws ParsingException {
        assertPageFetched();

        try {
            final String duration = playerResponse
                    .getObject("videoDetails")
                    .getString("lengthSeconds");
            return Long.parseLong(duration);
        } catch (final Exception e) {
            return getDurationFromFirstAdaptiveFormat(Arrays.asList(
                    html5StreamingData, androidStreamingData, iosStreamingData));
        }
    }

    private int getDurationFromFirstAdaptiveFormat(@Nonnull final List<JsonObject> streamingDatas)
            throws ParsingException {
        for (final JsonObject streamingData : streamingDatas) {
            final JsonArray adaptiveFormats = streamingData.getArray(ADAPTIVE_FORMATS);
            if (adaptiveFormats.isEmpty()) {
                continue;
            }

            final String durationMs = adaptiveFormats.getObject(0)
                    .getString("approxDurationMs");
            try {
                return Math.round(Long.parseLong(durationMs) / 1000f);
            } catch (final NumberFormatException ignored) {
            }
        }

        throw new ParsingException("Could not get duration");
    }

    /**
     * Attempts to parse (and return) the offset to start playing the video from.
     *
     * @return the offset (in seconds), or 0 if no timestamp is found.
     */
    @Override
    public long getTimeStamp() throws ParsingException {
        final long timestamp =
                getTimestampSeconds("((#|&|\\?)t=\\d*h?\\d*m?\\d+s?)");

        if (timestamp == -2) {
            // Regex for timestamp was not found
            return 0;
        }
        return timestamp;
    }

    @Override
    public long getViewCount() throws ParsingException {
        String views = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("viewCount")
                .getObject("videoViewCountRenderer").getObject("viewCount"));

        if (isNullOrEmpty(views)) {
            views = playerResponse.getObject("videoDetails").getString("viewCount");

            if (isNullOrEmpty(views)) {
                throw new ParsingException("Could not get view count");
            }
        }

        if (views.toLowerCase().contains("no views")) {
            return 0;
        }

        return Long.parseLong(Utils.removeNonDigitCharacters(views));
    }

    @Override
    public long getLikeCount() throws ParsingException {
        assertPageFetched();

        // If ratings are not allowed, there is no like count available
        if (!playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
            return -1;
        }

        String likesString = "";

        try {
            final JsonArray topLevelButtons = getVideoPrimaryInfoRenderer()
                    .getObject("videoActions")
                    .getObject("menuRenderer")
                    .getArray("topLevelButtons");

            // Try first with the new video actions buttons data structure
            JsonObject likeToggleButtonRenderer = topLevelButtons.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(button -> button.getObject("segmentedLikeDislikeButtonRenderer")
                            .getObject("likeButton")
                            .getObject("toggleButtonRenderer"))
                    .filter(toggleButtonRenderer -> !isNullOrEmpty(toggleButtonRenderer))
                    .findFirst()
                    .orElse(null);

            // Use the old video actions buttons data structure if the new one isn't returned
            if (likeToggleButtonRenderer == null) {
                /*
                In the old video actions buttons data structure, there are 3 ways to detect whether
                a button is the like button, using its toggleButtonRenderer:
                - checking whether toggleButtonRenderer.targetId is equal to watch-like;
                - checking whether toggleButtonRenderer.defaultIcon.iconType is equal to LIKE;
                - checking whether
                  toggleButtonRenderer.toggleButtonSupportedData.toggleButtonIdData.id
                  is equal to TOGGLE_BUTTON_ID_TYPE_LIKE.
                */
                likeToggleButtonRenderer = topLevelButtons.stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .map(topLevelButton -> topLevelButton.getObject("toggleButtonRenderer"))
                        .filter(toggleButtonRenderer -> toggleButtonRenderer.getString("targetId")
                                .equalsIgnoreCase("watch-like")
                                || toggleButtonRenderer.getObject("defaultIcon")
                                .getString("iconType")
                                .equalsIgnoreCase("LIKE")
                                || toggleButtonRenderer.getObject("toggleButtonSupportedData")
                                .getObject("toggleButtonIdData")
                                .getString("id")
                                .equalsIgnoreCase("TOGGLE_BUTTON_ID_TYPE_LIKE"))
                        .findFirst()
                        .orElseThrow(() -> new ParsingException(
                                "The like button is missing even though ratings are enabled"));
            }

            // Use one of the accessibility strings available (this one has the same path as the
            // one used for comments' like count extraction)
            likesString = likeToggleButtonRenderer.getObject("accessibilityData")
                    .getObject("accessibilityData")
                    .getString("label");

            // Use the other accessibility string available which contains the exact like count
            if (likesString == null) {
                likesString = likeToggleButtonRenderer.getObject("accessibility")
                        .getString("label");
            }

            // Last method: use the defaultText's accessibility data, which contains the exact like
            // count too, except when it is equal to 0, where a localized string is returned instead
            if (likesString == null) {
                likesString = likeToggleButtonRenderer.getObject("defaultText")
                        .getObject("accessibility")
                        .getObject("accessibilityData")
                        .getString("label");
            }

            // If ratings are allowed and the likes string is null, it means that we couldn't
            // extract the (real) like count from accessibility data
            if (likesString == null) {
                throw new ParsingException("Could not get like count from accessibility data");
            }

            // This check only works with English localizations!
            if (likesString.toLowerCase().contains("no likes")) {
                return 0;
            }

            return Integer.parseInt(Utils.removeNonDigitCharacters(likesString));
        } catch (final NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + likesString + "\" as an Integer",
                    nfe);
        } catch (final Exception e) {
            throw new ParsingException("Could not get like count", e);
        }
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        assertPageFetched();

        // Don't use the id in the videoSecondaryRenderer object to get real id of the uploader
        // The difference between the real id of the channel and the displayed id is especially
        // visible for music channels and autogenerated channels.
        final String uploaderId = playerResponse.getObject("videoDetails").getString("channelId");
        if (!isNullOrEmpty(uploaderId)) {
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl("channel/" + uploaderId);
        }

        throw new ParsingException("Could not get uploader url");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        assertPageFetched();

        // Don't use the name in the videoSecondaryRenderer object to get real name of the uploader
        // The difference between the real name of the channel and the displayed name is especially
        // visible for music channels and autogenerated channels.
        final String uploaderName = playerResponse.getObject("videoDetails").getString("author");
        if (isNullOrEmpty(uploaderName)) {
            throw new ParsingException("Could not get uploader name");
        }

        return uploaderName;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return YoutubeParsingHelper.isVerified(
                getVideoSecondaryInfoRenderer()
                        .getObject("owner")
                        .getObject("videoOwnerRenderer")
                        .getArray("badges"));
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        assertPageFetched();

        final String url = getVideoSecondaryInfoRenderer()
                .getObject("owner")
                .getObject("videoOwnerRenderer")
                .getObject("thumbnail")
                .getArray("thumbnails")
                .getObject(0)
                .getString("url");

        if (isNullOrEmpty(url)) {
            if (ageLimit == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get uploader avatar URL");
            }

            return "";
        }

        return fixThumbnailUrl(url);
    }

    @Override
    public long getUploaderSubscriberCount() throws ParsingException {
        final JsonObject videoOwnerRenderer = JsonUtils.getObject(videoSecondaryInfoRenderer,
                "owner.videoOwnerRenderer");
        if (!videoOwnerRenderer.has("subscriberCountText")) {
            return UNKNOWN_SUBSCRIBER_COUNT;
        }
        try {
            return Utils.mixedNumberWordToLong(getTextFromObject(videoOwnerRenderer
                    .getObject("subscriberCountText")));
        } catch (final NumberFormatException e) {
            throw new ParsingException("Could not get uploader subscriber count", e);
        }
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        assertPageFetched();

        // There is no DASH manifest available in the iOS clients and the DASH manifest of the
        // Android client doesn't contain all available streams (mainly the WEBM ones)
        return getManifestUrl(
                "dash",
                Arrays.asList(html5StreamingData, androidStreamingData));
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();

        // Return HLS manifest of the iOS client first because on livestreams, the HLS manifest
        // returned has separated audio and video streams
        // Also, on videos, non-iOS clients don't have an HLS manifest URL in their player response
        return getManifestUrl(
                "hls",
                Arrays.asList(iosStreamingData, html5StreamingData, androidStreamingData));
    }

    @Nonnull
    private static String getManifestUrl(@Nonnull final String manifestType,
                                         @Nonnull final List<JsonObject> streamingDataObjects) {
        final String manifestKey = manifestType + "ManifestUrl";

        return streamingDataObjects.stream()
                .filter(Objects::nonNull)
                .map(streamingDataObject -> streamingDataObject.getString(manifestKey))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        assertPageFetched();
        return getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.AUDIO,
                getAudioStreamBuilderHelper(), "audio");
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();
        return getItags(FORMATS, ItagItem.ItagType.VIDEO,
                getVideoStreamBuilderHelper(false), "video");
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        assertPageFetched();
        return getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.VIDEO_ONLY,
                getVideoStreamBuilderHelper(true), "video-only");
    }

    /**
     * Try to decrypt a streaming URL and fall back to the given URL, because decryption may fail
     * if YouTube changes break something.
     *
     * <p>
     * This way a breaking change from YouTube does not result in a broken extractor.
     * </p>
     *
     * @param streamingUrl the streaming URL to decrypt with {@link YoutubeThrottlingDecrypter}
     * @param videoId      the video ID to use when extracting JavaScript player code, if needed
     */
    private String tryDecryptUrl(final String streamingUrl, final String videoId) {
        try {
            return YoutubeThrottlingDecrypter.apply(streamingUrl, videoId);
        } catch (final ParsingException e) {
            return streamingUrl;
        }
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() throws ParsingException {
        return getSubtitles(MediaFormat.TTML);
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) throws ParsingException {
        assertPageFetched();

        // We cannot store the subtitles list because the media format may change
        final List<SubtitlesStream> subtitlesToReturn = new ArrayList<>();
        final JsonObject renderer = playerResponse.getObject("captions")
                .getObject("playerCaptionsTracklistRenderer");
        final JsonArray captionsArray = renderer.getArray("captionTracks");
        // TODO: use this to apply auto translation to different language from a source language
        // final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages");

        for (int i = 0; i < captionsArray.size(); i++) {
            final String languageCode = captionsArray.getObject(i).getString("languageCode");
            final String baseUrl = captionsArray.getObject(i).getString("baseUrl");
            final String vssId = captionsArray.getObject(i).getString("vssId");

            if (languageCode != null && baseUrl != null && vssId != null) {
                final boolean isAutoGenerated = vssId.startsWith("a.");
                final String cleanUrl = baseUrl
                        // Remove preexisting format if exists
                        .replaceAll("&fmt=[^&]*", "")
                        // Remove translation language
                        .replaceAll("&tlang=[^&]*", "");

                subtitlesToReturn.add(new SubtitlesStream.Builder()
                        .setContent(cleanUrl + "&fmt=" + format.getSuffix(), true)
                        .setMediaFormat(format)
                        .setLanguageCode(languageCode)
                        .setAutoGenerated(isAutoGenerated)
                        .build());
            }
        }

        return subtitlesToReturn;
    }

    @Override
    public StreamType getStreamType() {
        assertPageFetched();

        return streamType;
    }

    private void setStreamType() {
        if (playerResponse.getObject("playabilityStatus").has("liveStreamability")) {
            streamType = StreamType.LIVE_STREAM;
        } else if (playerResponse.getObject("videoDetails").getBoolean("isPostLiveDvr", false)) {
            streamType = StreamType.POST_LIVE_STREAM;
        } else {
            streamType = StreamType.VIDEO_STREAM;
        }
    }

    @Nullable
    @Override
    public MultiInfoItemsCollector getRelatedItems() throws ExtractionException {
        assertPageFetched();

        if (getAgeLimit() != NO_AGE_LIMIT) {
            return null;
        }

        try {
            final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

            final JsonArray results = nextResponse
                    .getObject("contents")
                    .getObject("twoColumnWatchNextResults")
                    .getObject("secondaryResults")
                    .getObject("secondaryResults")
                    .getArray("results");

            final TimeAgoParser timeAgoParser = getTimeAgoParser();
            results.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(result -> {
                        if (result.has("compactVideoRenderer")) {
                            return new YoutubeStreamInfoItemExtractor(
                                    result.getObject("compactVideoRenderer"), timeAgoParser);
                        } else if (result.has("compactRadioRenderer")) {
                            return new YoutubeMixOrPlaylistInfoItemExtractor(
                                    result.getObject("compactRadioRenderer"));
                        } else if (result.has("compactPlaylistRenderer")) {
                            return new YoutubeMixOrPlaylistInfoItemExtractor(
                                    result.getObject("compactPlaylistRenderer"));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(collector::commit);

            return collector;
        } catch (final Exception e) {
            throw new ParsingException("Could not get related videos", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        try {
            return getTextFromObject(playerResponse.getObject("playabilityStatus")
                    .getObject("errorScreen").getObject("playerErrorMessageRenderer")
                    .getObject("reason"));
        } catch (final NullPointerException e) {
            return null; // No error message
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    private static final String FORMATS = "formats";
    private static final String ADAPTIVE_FORMATS = "adaptiveFormats";
    private static final String DEOBFUSCATION_FUNC_NAME = "deobfuscate";
    private static final String STREAMING_DATA = "streamingData";
    private static final String PLAYER = "player";
    private static final String NEXT = "next";
    private static final String SIGNATURE_CIPHER = "signatureCipher";
    private static final String CIPHER = "cipher";

    private static final String[] REGEXES = {
            "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2,})\\s*=\\s*function\\(\\s*a\\s*\\)"
                    + "\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            "\\bm=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(h\\.s\\)\\)",
            "\\bc&&\\(c=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(c\\)\\)",
            "([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;",
            "\\b([\\w$]{2,})\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;",
            "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\("
    };
    private static final String STS_REGEX = "signatureTimestamp[=:](\\d+)";

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        initStsFromPlayerJsIfNeeded();

        final String videoId = getId();
        final Localization localization = getExtractorLocalization();
        final ContentCountry contentCountry = getExtractorContentCountry();
        html5Cpn = generateContentPlaybackNonce();

        playerResponse = getJsonPostResponse(PLAYER,
                createDesktopPlayerBody(localization, contentCountry, videoId, sts, false,
                        html5Cpn),
                localization);

        // Save the playerResponse from the player endpoint of the desktop internal API because
        // there can be restrictions on the embedded player.
        // E.g. if a video is age-restricted, the embedded player's playabilityStatus says that
        // the video cannot be played outside of YouTube, but does not show the original message.
        final JsonObject youtubePlayerResponse = playerResponse;

        if (playerResponse == null) {
            throw new ExtractionException("Could not get playerResponse");
        }

        final JsonObject playabilityStatus = playerResponse.getObject("playabilityStatus");

        final boolean isAgeRestricted = playabilityStatus.getString("reason", "")
                .contains("age");

        setStreamType();

        if (!playerResponse.has(STREAMING_DATA)) {
            try {
                fetchTvHtml5EmbedJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
            }
        }

        // Refresh the stream type because the stream type may be not properly known for
        // age-restricted videos
        setStreamType();

        if (html5StreamingData == null && playerResponse.has(STREAMING_DATA)) {
            html5StreamingData = playerResponse.getObject(STREAMING_DATA);
        }

        if (html5StreamingData == null) {
            checkPlayabilityStatus(youtubePlayerResponse, playabilityStatus);
        }

        // The microformat JSON object of the content is not returned on the client we use to
        // try to get streams of unavailable contents but is still returned on the WEB client,
        // so we need to store it instead of getting it directly from the playerResponse
        playerMicroFormatRenderer = youtubePlayerResponse.getObject("microformat")
                .getObject("playerMicroformatRenderer");

        if (isPlayerResponseNotValid(playerResponse, videoId)) {
            throw new ExtractionException("Initial player response is not valid");
        }

        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, contentCountry)
                        .value(VIDEO_ID, videoId)
                        .value(CONTENT_CHECK_OK, true)
                        .value(RACY_CHECK_OK, true)
                        .done())
                .getBytes(StandardCharsets.UTF_8);
        nextResponse = getJsonPostResponse(NEXT, body, localization);

        // streamType can only have LIVE_STREAM, POST_LIVE_STREAM and VIDEO_STREAM values (see
        // setStreamType()), so this block will be run only for POST_LIVE_STREAM and VIDEO_STREAM
        // values if fetching of the ANDROID client is not forced
        if ((!isAgeRestricted && streamType != StreamType.LIVE_STREAM)
                || isAndroidClientFetchForced) {
            try {
                fetchAndroidMobileJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
                // Ignore exceptions related to ANDROID client fetch or parsing, as it is not
                // compulsory to play contents
            }
        }

        if ((!isAgeRestricted && streamType == StreamType.LIVE_STREAM)
                || isIosClientFetchForced) {
            try {
                fetchIosMobileJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
                // Ignore exceptions related to IOS client fetch or parsing, as it is not
                // compulsory to play contents
            }
        }
    }

    private void checkPlayabilityStatus(final JsonObject youtubePlayerResponse,
                                        @Nonnull final JsonObject playabilityStatus)
            throws ParsingException {
        String status = playabilityStatus.getString("status");
        if (status == null || status.equalsIgnoreCase("ok")) {
            return;
        }

        // If status exist, and is not "OK", throw the specific exception based on error message
        // or a ContentNotAvailableException with the reason text if it's an unknown reason.
        final JsonObject newPlayabilityStatus =
                youtubePlayerResponse.getObject("playabilityStatus");
        status = newPlayabilityStatus.getString("status");
        final String reason = newPlayabilityStatus.getString("reason");

        if (status.equalsIgnoreCase("login_required")) {
            if (reason == null) {
                final String message = newPlayabilityStatus.getArray("messages").getString(0);
                if (message != null && message.contains("private")) {
                    throw new PrivateContentException("This video is private.");
                }
            } else if (reason.contains("age")) {
                // No streams can be fetched, therefore throw an AgeRestrictedContentException
                // explicitly.
                throw new AgeRestrictedContentException(
                        "This age-restricted video cannot be watched.");
            }
        }

        if ((status.equalsIgnoreCase("unplayable") || status.equalsIgnoreCase("error"))
                && reason != null) {
            if (reason.contains("Music Premium")) {
                throw new YoutubeMusicPremiumContentException();
            }
            if (reason.contains("payment")) {
                throw new PaidContentException("This video is a paid video");
            }
            if (reason.contains("members-only")) {
                throw new PaidContentException("This video is only available"
                        + " for members of the channel of this video");
            }

            if (reason.contains("unavailable")) {
                final String detailedErrorMessage = getTextFromObject(newPlayabilityStatus
                        .getObject("errorScreen")
                        .getObject("playerErrorMessageRenderer")
                        .getObject("subreason"));
                if (detailedErrorMessage != null && detailedErrorMessage.contains("country")) {
                    throw new GeographicRestrictionException(
                            "This video is not available in client's country.");
                } else if (detailedErrorMessage != null) {
                    throw new ContentNotAvailableException(detailedErrorMessage);
                } else {
                    throw new ContentNotAvailableException(reason);
                }
            }
        }

        throw new ContentNotAvailableException("Got error: \"" + reason + "\"");
    }

    /**
     * Fetch the Android Mobile API and assign the streaming data to the androidStreamingData JSON
     * object.
     */
    private void fetchAndroidMobileJsonPlayer(@Nonnull final ContentCountry contentCountry,
                                              @Nonnull final Localization localization,
                                              @Nonnull final String videoId)
            throws IOException, ExtractionException {
        androidCpn = generateContentPlaybackNonce();
        final byte[] mobileBody = JsonWriter.string(
                prepareAndroidMobileJsonBuilder(localization, contentCountry)
                        .value(VIDEO_ID, videoId)
                        .value(CPN, androidCpn)
                        .value(CONTENT_CHECK_OK, true)
                        .value(RACY_CHECK_OK, true)
                        // Workaround getting streaming URLs which can return 403 HTTP response
                        // codes by using stories parameter for Android client requests
                        // This behavior only happen in certain countries such as UK as of
                        // 10.29.2022
                        .value("params", "8AEB")
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        final JsonObject androidPlayerResponse = getJsonAndroidPostResponse(PLAYER,
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId);

        if (isPlayerResponseNotValid(androidPlayerResponse, videoId)) {
            return;
        }

        final JsonObject streamingData = androidPlayerResponse.getObject(STREAMING_DATA);
        if (!isNullOrEmpty(streamingData)) {
            androidStreamingData = streamingData;
            if (html5StreamingData == null) {
                playerResponse = androidPlayerResponse;
            }
        }
    }

    /**
     * Fetch the iOS Mobile API and assign the streaming data to the iosStreamingData JSON
     * object.
     */
    private void fetchIosMobileJsonPlayer(@Nonnull final ContentCountry contentCountry,
                                          @Nonnull final Localization localization,
                                          @Nonnull final String videoId)
            throws IOException, ExtractionException {
        iosCpn = generateContentPlaybackNonce();
        final byte[] mobileBody = JsonWriter.string(
                prepareIosMobileJsonBuilder(localization, contentCountry)
                        .value(VIDEO_ID, videoId)
                        .value(CPN, iosCpn)
                        .value(CONTENT_CHECK_OK, true)
                        .value(RACY_CHECK_OK, true)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        final JsonObject iosPlayerResponse = getJsonIosPostResponse(PLAYER,
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId);

        if (isPlayerResponseNotValid(iosPlayerResponse, videoId)) {
            return;
        }

        final JsonObject streamingData = iosPlayerResponse.getObject(STREAMING_DATA);
        if (!isNullOrEmpty(streamingData)) {
            iosStreamingData = streamingData;
            if (html5StreamingData == null) {
                playerResponse = iosPlayerResponse;
            }
        }
    }

    /**
     * Download the {@code TVHTML5_SIMPLY_EMBEDDED_PLAYER} JSON player as an embed client to bypass
     * some age-restrictions and assign the streaming data to the {@code html5StreamingData} JSON
     * object.
     *
     * @param contentCountry the content country to use
     * @param localization   the localization to use
     * @param videoId        the video id
     */
    private void fetchTvHtml5EmbedJsonPlayer(@Nonnull final ContentCountry contentCountry,
                                             @Nonnull final Localization localization,
                                             @Nonnull final String videoId)
            throws IOException, ExtractionException {
        initStsFromPlayerJsIfNeeded();

        // Because a cpn is unique to each request, we need to generate it again
        html5Cpn = generateContentPlaybackNonce();

        final JsonObject tvHtml5EmbedPlayerResponse = getJsonPostResponse(PLAYER,
                createDesktopPlayerBody(localization, contentCountry, videoId, sts, true,
                        html5Cpn), localization);

        if (isPlayerResponseNotValid(tvHtml5EmbedPlayerResponse, videoId)) {
            return;
        }

        final JsonObject streamingData = tvHtml5EmbedPlayerResponse.getObject(
                STREAMING_DATA);
        if (!isNullOrEmpty(streamingData)) {
            playerResponse = tvHtml5EmbedPlayerResponse;
            html5StreamingData = streamingData;
        }
    }

    /**
     * Checks whether a player response is invalid.
     *
     * <p>
     * If YouTube detect that requests come from a third party client, they may replace the real
     * player response by another one of a video saying that this content is not available on this
     * app and to watch it on the latest version of YouTube. This behavior has been observed on the
     * {@code ANDROID} client, see
     * <a href="https://github.com/TeamNewPipe/NewPipe/issues/8713">
     *     https://github.com/TeamNewPipe/NewPipe/issues/8713</a>.
     * </p>
     *
     * <p>
     * YouTube may also sometimes for currently unknown reasons rate-limit an IP, and replace the
     * real one by a player response with a video that says that the requested video is
     * unavailable. This behaviour has been observed in Piped on the InnerTube clients used by the
     * extractor ({@code ANDROID} and {@code WEB} clients) which should apply for all clients, see
     * <a href="https://github.com/TeamPiped/Piped/issues/2487">
     *     https://github.com/TeamPiped/Piped/issues/2487</a>.
     * </p>
     *
     * <p>
     * We can detect this by checking whether the video ID of the player response returned is the
     * same as the one requested by the extractor.
     * </p>
     *
     * @param playerResponse a player response from any client
     * @param videoId        the video ID of the content requested
     * @return whether the video ID of the player response is not equal to the one requested
     */
    private static boolean isPlayerResponseNotValid(
            @Nonnull final JsonObject playerResponse,
            @Nonnull final String videoId) {
        return !videoId.equals(playerResponse.getObject("videoDetails")
                .getString("videoId"));
    }

    private static void storePlayerJs() throws ParsingException {
        try {
            playerCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();
        } catch (final Exception e) {
            throw new ParsingException("Could not store JavaScript player", e);
        }
    }

    private static String getDeobfuscationFuncName(final String thePlayerCode)
            throws DeobfuscateException {
        Parser.RegexException exception = null;
        for (final String regex : REGEXES) {
            try {
                return Parser.matchGroup1(regex, thePlayerCode);
            } catch (final Parser.RegexException re) {
                if (exception == null) {
                    exception = re;
                }
            }
        }
        throw new DeobfuscateException(
                "Could not find deobfuscate function with any of the given patterns.", exception);
    }

    @Nonnull
    private static String loadDeobfuscationCode() throws DeobfuscateException {
        try {
            final String deobfuscationFunctionName = getDeobfuscationFuncName(playerCode);

            final String functionPattern = "("
                    + deobfuscationFunctionName.replace("$", "\\$")
                    + "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})";
            final String deobfuscateFunction = "var " + Parser.matchGroup1(functionPattern,
                    playerCode) + ";";

            final String helperObjectName =
                    Parser.matchGroup1(";([A-Za-z0-9_\\$]{2})\\...\\(",
                            deobfuscateFunction);
            final String helperPattern =
                    "(var " + helperObjectName.replace("$", "\\$")
                            + "=\\{.+?\\}\\};)";
            final String helperObject =
                    Parser.matchGroup1(helperPattern, Objects.requireNonNull(playerCode).replace(
                            "\n", ""));

            final String callerFunction =
                    "function " + DEOBFUSCATION_FUNC_NAME + "(a){return "
                            + deobfuscationFunctionName + "(a);}";

            return helperObject + deobfuscateFunction + callerFunction;
        } catch (final Exception e) {
            throw new DeobfuscateException("Could not parse deobfuscate function ", e);
        }
    }

    @Nonnull
    private static String getDeobfuscationCode() throws ParsingException {
        if (cachedDeobfuscationCode == null) {
            if (isNullOrEmpty(playerCode)) {
                throw new ParsingException("playerCode is null");
            }

            cachedDeobfuscationCode = loadDeobfuscationCode();
        }
        return cachedDeobfuscationCode;
    }

    private static void initStsFromPlayerJsIfNeeded() throws ParsingException {
        if (!isNullOrEmpty(sts)) {
            return;
        }
        if (playerCode == null) {
            storePlayerJs();
            if (playerCode == null) {
                throw new ParsingException("playerCode is null");
            }
        }
        sts = Parser.matchGroup1(STS_REGEX, playerCode);
    }

    private String deobfuscateSignature(final String obfuscatedSig) throws ParsingException {
        final String deobfuscationCode = getDeobfuscationCode();

        final Context context = Context.enter();
        context.setOptimizationLevel(-1);
        final Object result;
        try {
            final ScriptableObject scope = context.initSafeStandardObjects();
            context.evaluateString(scope, deobfuscationCode, "deobfuscationCode", 1, null);
            final Function deobfuscateFunc = (Function) scope.get(DEOBFUSCATION_FUNC_NAME, scope);
            result = deobfuscateFunc.call(context, scope, scope, new Object[]{obfuscatedSig});
        } catch (final Exception e) {
            throw new DeobfuscateException("Could not get deobfuscate signature", e);
        } finally {
            Context.exit();
        }
        return Objects.toString(result, "");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Nonnull
    private JsonObject getVideoPrimaryInfoRenderer() {
        if (videoPrimaryInfoRenderer != null) {
            return videoPrimaryInfoRenderer;
        }

        videoPrimaryInfoRenderer = getVideoInfoRenderer("videoPrimaryInfoRenderer");
        return videoPrimaryInfoRenderer;
    }

    @Nonnull
    private JsonObject getVideoSecondaryInfoRenderer() {
        if (videoSecondaryInfoRenderer != null) {
            return videoSecondaryInfoRenderer;
        }

        videoSecondaryInfoRenderer = getVideoInfoRenderer("videoSecondaryInfoRenderer");
        return videoSecondaryInfoRenderer;
    }

    @Nonnull
    private JsonObject getVideoInfoRenderer(@Nonnull final String videoRendererName) {
        return nextResponse.getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("results")
                .getObject("results")
                .getArray("contents")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(content -> content.has(videoRendererName))
                .map(content -> content.getObject(videoRendererName))
                .findFirst()
                .orElse(new JsonObject());
    }

    @Nonnull
    private <T extends Stream> List<T> getItags(
            final String streamingDataKey,
            final ItagItem.ItagType itagTypeWanted,
            final java.util.function.Function<ItagInfo, T> streamBuilderHelper,
            final String streamTypeExceptionMessage) throws ParsingException {
        try {
            final String videoId = getId();
            final List<T> streamList = new ArrayList<>();

            java.util.stream.Stream.of(
                    // Use the androidStreamingData object first because there is no n param and no
                    // signatureCiphers in streaming URLs of the Android client
                    new Pair<>(androidStreamingData, androidCpn),
                    new Pair<>(html5StreamingData, html5Cpn),
                    // Use the iosStreamingData object in the last position because most of the
                    // available streams can be extracted with the Android and web clients and also
                    // because the iOS client is only enabled by default on livestreams
                    new Pair<>(iosStreamingData, iosCpn)
            )
                    .flatMap(pair -> getStreamsFromStreamingDataKey(videoId, pair.getFirst(),
                            streamingDataKey, itagTypeWanted, pair.getSecond()))
                    .map(streamBuilderHelper)
                    .forEachOrdered(stream -> {
                        if (!Stream.containSimilarStream(stream, streamList)) {
                            streamList.add(stream);
                        }
                    });

            return streamList;
        } catch (final Exception e) {
            throw new ParsingException(
                    "Could not get " + streamTypeExceptionMessage + " streams", e);
        }
    }

    /**
     * Get the stream builder helper which will be used to build {@link AudioStream}s in
     * {@link #getItags(String, ItagItem.ItagType, java.util.function.Function, String)}
     *
     * <p>
     * The {@code StreamBuilderHelper} will set the following attributes in the
     * {@link AudioStream}s built:
     * <ul>
     *     <li>the {@link ItagItem}'s id of the stream as its id;</li>
     *     <li>{@link ItagInfo#getContent()} and {@link ItagInfo#getIsUrl()} as its content and
     *     and as the value of {@code isUrl};</li>
     *     <li>the media format returned by the {@link ItagItem} as its media format;</li>
     *     <li>its average bitrate with the value returned by {@link
     *     ItagItem#getAverageBitrate()};</li>
     *     <li>the {@link ItagItem};</li>
     *     <li>the {@link DeliveryMethod#DASH DASH delivery method}, for OTF streams, live streams
     *     and ended streams.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Note that the {@link ItagItem} comes from an {@link ItagInfo} instance.
     * </p>
     *
     * @return a stream builder helper to build {@link AudioStream}s
     */
    @Nonnull
    private java.util.function.Function<ItagInfo, AudioStream> getAudioStreamBuilderHelper() {
        return (itagInfo) -> {
            final ItagItem itagItem = itagInfo.getItagItem();
            final AudioStream.Builder builder = new AudioStream.Builder()
                    .setId(String.valueOf(itagItem.id))
                    .setContent(itagInfo.getContent(), itagInfo.getIsUrl())
                    .setMediaFormat(itagItem.getMediaFormat())
                    .setAverageBitrate(itagItem.getAverageBitrate())
                    .setAudioTrackId(itagItem.getAudioTrackId())
                    .setAudioTrackName(itagItem.getAudioTrackName())
                    .setAudioLocale(itagItem.getAudioLocale())
                    .setAudioTrackType(itagItem.getAudioTrackType())
                    .setItagItem(itagItem);

            if (streamType == StreamType.LIVE_STREAM
                    || streamType == StreamType.POST_LIVE_STREAM
                    || !itagInfo.getIsUrl()) {
                // For YouTube videos on OTF streams and for all streams of post-live streams
                // and live streams, only the DASH delivery method can be used.
                builder.setDeliveryMethod(DeliveryMethod.DASH);
            }

            return builder.build();
        };
    }

    /**
     * Get the stream builder helper which will be used to build {@link VideoStream}s in
     * {@link #getItags(String, ItagItem.ItagType, java.util.function.Function, String)}
     *
     * <p>
     * The {@code StreamBuilderHelper} will set the following attributes in the
     * {@link VideoStream}s built:
     * <ul>
     *     <li>the {@link ItagItem}'s id of the stream as its id;</li>
     *     <li>{@link ItagInfo#getContent()} and {@link ItagInfo#getIsUrl()} as its content and
     *     and as the value of {@code isUrl};</li>
     *     <li>the media format returned by the {@link ItagItem} as its media format;</li>
     *     <li>whether it is video-only with the {@code areStreamsVideoOnly} parameter</li>
     *     <li>the {@link ItagItem};</li>
     *     <li>the resolution, by trying to use, in this order:
     *         <ol>
     *             <li>the height returned by the {@link ItagItem} + {@code p} + the frame rate if
     *             it is more than 30;</li>
     *             <li>the default resolution string from the {@link ItagItem};</li>
     *             <li>an empty string.</li>
     *         </ol>
     *     </li>
     *     <li>the {@link DeliveryMethod#DASH DASH delivery method}, for OTF streams, live streams
     *     and ended streams.</li>
     * </ul>
     *
     * <p>
     * Note that the {@link ItagItem} comes from an {@link ItagInfo} instance.
     * </p>
     *
     * @param areStreamsVideoOnly whether the stream builder helper will set the video
     *                            streams as video-only streams
     * @return a stream builder helper to build {@link VideoStream}s
     */
    @Nonnull
    private java.util.function.Function<ItagInfo, VideoStream> getVideoStreamBuilderHelper(
            final boolean areStreamsVideoOnly) {
        return (itagInfo) -> {
            final ItagItem itagItem = itagInfo.getItagItem();
            final VideoStream.Builder builder = new VideoStream.Builder()
                    .setId(String.valueOf(itagItem.id))
                    .setContent(itagInfo.getContent(), itagInfo.getIsUrl())
                    .setMediaFormat(itagItem.getMediaFormat())
                    .setIsVideoOnly(areStreamsVideoOnly)
                    .setItagItem(itagItem);

            final String resolutionString = itagItem.getResolutionString();
            builder.setResolution(resolutionString != null ? resolutionString
                    : "");

            if (streamType != StreamType.VIDEO_STREAM || !itagInfo.getIsUrl()) {
                // For YouTube videos on OTF streams and for all streams of post-live streams
                // and live streams, only the DASH delivery method can be used.
                builder.setDeliveryMethod(DeliveryMethod.DASH);
            }

            return builder.build();
        };
    }

    @Nonnull
    private java.util.stream.Stream<ItagInfo> getStreamsFromStreamingDataKey(
            final String videoId,
            final JsonObject streamingData,
            final String streamingDataKey,
            @Nonnull final ItagItem.ItagType itagTypeWanted,
            @Nonnull final String contentPlaybackNonce) {
        if (streamingData == null || !streamingData.has(streamingDataKey)) {
            return java.util.stream.Stream.empty();
        }

        return streamingData.getArray(streamingDataKey).stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(formatData -> {
                    try {
                        final ItagItem itagItem = ItagItem.getItag(formatData.getInt("itag"));
                        if (itagItem.itagType == itagTypeWanted) {
                            return buildAndAddItagInfoToList(videoId, formatData, itagItem,
                                    itagItem.itagType, contentPlaybackNonce);
                        }
                    } catch (final IOException | ExtractionException ignored) {
                        // if the itag is not supported and getItag fails, we end up here
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }

    private ItagInfo buildAndAddItagInfoToList(
            @Nonnull final String videoId,
            @Nonnull final JsonObject formatData,
            @Nonnull final ItagItem itagItem,
            @Nonnull final ItagItem.ItagType itagType,
            @Nonnull final String contentPlaybackNonce) throws IOException, ExtractionException {
        String streamUrl;
        if (formatData.has("url")) {
            streamUrl = formatData.getString("url");
        } else {
            // This url has an obfuscated signature
            final String cipherString = formatData.has(CIPHER)
                    ? formatData.getString(CIPHER)
                    : formatData.getString(SIGNATURE_CIPHER);
            final Map<String, String> cipher = Parser.compatParseMap(
                    cipherString);
            streamUrl = cipher.get("url") + "&" + cipher.get("sp") + "="
                    + deobfuscateSignature(cipher.get("s"));
        }

        // Add the content playback nonce to the stream URL
        streamUrl += "&" + CPN + "=" + contentPlaybackNonce;

        // Decrypt the n parameter if it is present
        streamUrl = tryDecryptUrl(streamUrl, videoId);

        final JsonObject initRange = formatData.getObject("initRange");
        final JsonObject indexRange = formatData.getObject("indexRange");
        final String mimeType = formatData.getString("mimeType", "");
        final String codec = mimeType.contains("codecs")
                ? mimeType.split("\"")[1] : "";

        itagItem.setBitrate(formatData.getInt("bitrate"));
        itagItem.setWidth(formatData.getInt("width"));
        itagItem.setHeight(formatData.getInt("height"));
        itagItem.setInitStart(Integer.parseInt(initRange.getString("start", "-1")));
        itagItem.setInitEnd(Integer.parseInt(initRange.getString("end", "-1")));
        itagItem.setIndexStart(Integer.parseInt(indexRange.getString("start", "-1")));
        itagItem.setIndexEnd(Integer.parseInt(indexRange.getString("end", "-1")));
        itagItem.setQuality(formatData.getString("quality"));
        itagItem.setCodec(codec);

        if (streamType == StreamType.LIVE_STREAM || streamType == StreamType.POST_LIVE_STREAM) {
            itagItem.setTargetDurationSec(formatData.getInt("targetDurationSec"));
        }

        if (itagType == ItagItem.ItagType.VIDEO || itagType == ItagItem.ItagType.VIDEO_ONLY) {
            itagItem.setFps(formatData.getInt("fps"));
        } else if (itagType == ItagItem.ItagType.AUDIO) {
            // YouTube return the audio sample rate as a string
            itagItem.setSampleRate(Integer.parseInt(formatData.getString("audioSampleRate")));
            itagItem.setAudioChannels(formatData.getInt("audioChannels",
                    // Most audio streams have two audio channels, so use this value if the real
                    // count cannot be extracted
                    // Doing this prevents an exception when generating the
                    // AudioChannelConfiguration element of DASH manifests of audio streams in
                    // YoutubeDashManifestCreatorUtils
                    2));

            final String audioTrackId = formatData.getObject("audioTrack")
                    .getString("id");
            if (!isNullOrEmpty(audioTrackId)) {
                itagItem.setAudioTrackId(audioTrackId);
                final int audioTrackIdLastLocaleCharacter = audioTrackId.indexOf(".");
                if (audioTrackIdLastLocaleCharacter != -1) {
                    // Audio tracks IDs are in the form LANGUAGE_CODE.TRACK_NUMBER
                    itagItem.setAudioLocale(LocaleCompat.forLanguageTag(
                            audioTrackId.substring(0, audioTrackIdLastLocaleCharacter)));
                }
                itagItem.setAudioTrackType(YoutubeParsingHelper.extractAudioTrackType(streamUrl));
            }

            itagItem.setAudioTrackName(formatData.getObject("audioTrack")
                    .getString("displayName"));
        }

        // YouTube return the content length and the approximate duration as strings
        itagItem.setContentLength(Long.parseLong(formatData.getString("contentLength",
                String.valueOf(CONTENT_LENGTH_UNKNOWN))));
        itagItem.setApproxDurationMs(Long.parseLong(formatData.getString("approxDurationMs",
                String.valueOf(APPROX_DURATION_MS_UNKNOWN))));

        final ItagInfo itagInfo = new ItagInfo(streamUrl, itagItem);

        if (streamType == StreamType.VIDEO_STREAM) {
            itagInfo.setIsUrl(!formatData.getString("type", "")
                    .equalsIgnoreCase("FORMAT_STREAM_TYPE_OTF"));
        } else {
            // We are currently not able to generate DASH manifests for running
            // livestreams, so because of the requirements of StreamInfo
            // objects, return these streams as DASH URL streams (even if they
            // are not playable).
            // Ended livestreams are returned as non URL streams
            itagInfo.setIsUrl(streamType != StreamType.POST_LIVE_STREAM);
        }

        return itagInfo;
    }

    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        try {
            final JsonObject storyboards = playerResponse.getObject("storyboards");
            final JsonObject storyboardsRenderer = storyboards.getObject(
                    storyboards.has("playerLiveStoryboardSpecRenderer")
                            ? "playerLiveStoryboardSpecRenderer"
                            : "playerStoryboardSpecRenderer"
            );

            if (storyboardsRenderer == null) {
                return Collections.emptyList();
            }

            final String storyboardsRendererSpec = storyboardsRenderer.getString("spec");
            if (storyboardsRendererSpec == null) {
                return Collections.emptyList();
            }

            final String[] spec = storyboardsRendererSpec.split("\\|");
            final String url = spec[0];
            final List<Frameset> result = new ArrayList<>(spec.length - 1);

            for (int i = 1; i < spec.length; ++i) {
                final String[] parts = spec[i].split("#");
                if (parts.length != 8 || Integer.parseInt(parts[5]) == 0) {
                    continue;
                }
                final int totalCount = Integer.parseInt(parts[2]);
                final int framesPerPageX = Integer.parseInt(parts[3]);
                final int framesPerPageY = Integer.parseInt(parts[4]);
                final String baseUrl = url.replace("$L", String.valueOf(i - 1))
                        .replace("$N", parts[6]) + "&sigh=" + parts[7];
                final List<String> urls;
                if (baseUrl.contains("$M")) {
                    final int totalPages = (int) Math.ceil(totalCount / (double)
                            (framesPerPageX * framesPerPageY));
                    urls = new ArrayList<>(totalPages);
                    for (int j = 0; j < totalPages; j++) {
                        urls.add(baseUrl.replace("$M", String.valueOf(j)));
                    }
                } else {
                    urls = Collections.singletonList(baseUrl);
                }
                result.add(new Frameset(
                        urls,
                        /*frameWidth=*/Integer.parseInt(parts[0]),
                        /*frameHeight=*/Integer.parseInt(parts[1]),
                        totalCount,
                        /*durationPerFrame=*/Integer.parseInt(parts[5]),
                        framesPerPageX,
                        framesPerPageY
                ));
            }
            return result;
        } catch (final Exception e) {
            throw new ExtractionException("Could not get frames", e);
        }
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        return playerMicroFormatRenderer.getBoolean("isUnlisted")
                ? Privacy.UNLISTED
                : Privacy.PUBLIC;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return playerMicroFormatRenderer.getString("category", "");
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        final JsonObject metadataRowRenderer = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer")
                .getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .getObject(0)
                .getObject("metadataRowRenderer");

        final JsonArray contents = metadataRowRenderer.getArray("contents");
        final String license = getTextFromObject(contents.getObject(0));
        return license != null
                && "Licence".equals(getTextFromObject(metadataRowRenderer.getObject("title")))
                ? license
                : "YouTube licence";
    }

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(playerResponse.getObject("videoDetails")
                .getArray("keywords"));
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {

        if (!nextResponse.has("engagementPanels")) {
            return Collections.emptyList();
        }

        final JsonArray segmentsArray = nextResponse.getArray("engagementPanels")
                .stream()
                // Check if object is a JsonObject
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                // Check if the panel is the correct one
                .filter(panel -> "engagement-panel-macro-markers-description-chapters".equals(
                        panel
                                .getObject("engagementPanelSectionListRenderer")
                                .getString("panelIdentifier")))
                // Extract the data
                .map(panel -> panel
                        .getObject("engagementPanelSectionListRenderer")
                        .getObject("content")
                        .getObject("macroMarkersListRenderer")
                        .getArray("contents"))
                .findFirst()
                .orElse(null);

        // If no data was found exit
        if (segmentsArray == null) {
            return Collections.emptyList();
        }

        final long duration = getLength();
        final List<StreamSegment> segments = new ArrayList<>();
        for (final JsonObject segmentJson : segmentsArray.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(object -> object.getObject("macroMarkersListItemRenderer"))
                .collect(Collectors.toList())
        ) {
            final int startTimeSeconds = segmentJson.getObject("onTap")
                    .getObject("watchEndpoint").getInt("startTimeSeconds", -1);

            if (startTimeSeconds == -1) {
                throw new ParsingException("Could not get stream segment start time.");
            }
            if (startTimeSeconds > duration) {
                break;
            }

            final String title = getTextFromObject(segmentJson.getObject("title"));
            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get stream segment title.");
            }

            final StreamSegment segment = new StreamSegment(title, startTimeSeconds);
            segment.setUrl(getUrl() + "?t=" + startTimeSeconds);
            if (segmentJson.has("thumbnail")) {
                final JsonArray previewsArray = segmentJson
                        .getObject("thumbnail")
                        .getArray("thumbnails");
                if (!previewsArray.isEmpty()) {
                    // Assume that the thumbnail with the highest resolution is at the last position
                    final String url = previewsArray
                            .getObject(previewsArray.size() - 1)
                            .getString("url");
                    segment.setPreviewUrl(fixThumbnailUrl(url));
                }
            }
            segments.add(segment);
        }
        return segments;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return YoutubeParsingHelper.getMetaInfo(nextResponse
                .getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("results")
                .getObject("results")
                .getArray("contents"));
    }

    /**
     * Reset YouTube's deobfuscation code.
     *
     * <p>
     * This is needed for mocks in YouTube stream tests, because when they are ran, the
     * {@code signatureTimestamp} is known (the {@code sts} string) so a different body than the
     * body present in the mocks is send by the extractor instance. As a result, running all
     * YouTube stream tests with the MockDownloader (like the CI does) will fail if this method is
     * not called before fetching the page of a test.
     * </p>
     */
    public static void resetDeobfuscationCode() {
        cachedDeobfuscationCode = null;
        playerCode = null;
        sts = null;
        YoutubeJavaScriptExtractor.resetJavaScriptCode();
    }

    /**
     * Enable or disable the fetch of the Android client for all stream types.
     *
     * <p>
     * By default, the fetch of the Android client will be made only on videos, in order to reduce
     * data usage, because available streams of the Android client will be almost equal to the ones
     * available on the {@code WEB} client: you can get exclusively a 48kbps audio stream and a
     * 3GPP very low stream (which is, most of times, a 144p8 stream).
     * </p>
     *
     * @param forceFetchAndroidClientValue whether to always fetch the Android client and not only
     *                                     for videos
     */
    public static void forceFetchAndroidClient(final boolean forceFetchAndroidClientValue) {
        isAndroidClientFetchForced = forceFetchAndroidClientValue;
    }

    /**
     * Enable or disable the fetch of the iOS client for all stream types.
     *
     * <p>
     * By default, the fetch of the iOS client will be made only on livestreams, in order to get an
     * HLS manifest with separated audio and video which has also an higher replay time (up to one
     * hour, depending of the content instead of 30 seconds with non-iOS clients).
     * </p>
     *
     * <p>
     * Enabling this option will allow you to get an HLS manifest also for regular videos, which
     * contains resolutions up to 1080p60.
     * </p>
     *
     * @param forceFetchIosClientValue whether to always fetch the iOS client and not only for
     *                                 livestreams
     */
    public static void forceFetchIosClient(final boolean forceFetchIosClientValue) {
        isIosClientFetchForced = forceFetchIosClientValue;
    }
}
