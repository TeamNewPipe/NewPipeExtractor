package org.schabi.newpipe.extractor.services.youtube.extractors;

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
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareAndroidMobileJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareIosMobileJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
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
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) Christian Schabesberger 2019 <chris.schabesberger@mailbox.org>
 * YoutubeStreamExtractor.java is part of NewPipe.
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

public class YoutubeStreamExtractor extends StreamExtractor {
    /*//////////////////////////////////////////////////////////////////////////
    // Exceptions
    //////////////////////////////////////////////////////////////////////////*/

    public static class DeobfuscateException extends ParsingException {
        DeobfuscateException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////*/

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
    @Nullable
    private List<SubtitlesStream> subtitles = null;

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
        String title = null;

        try {
            title = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("title"));
        } catch (final ParsingException ignored) {
            // Age-restricted videos cause a ParsingException here
        }

        if (isNullOrEmpty(title)) {
            title = playerResponse.getObject("videoDetails").getString("title");

            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get name");
            }
        }

        return title;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (!playerMicroFormatRenderer.getString("uploadDate", EMPTY_STRING).isEmpty()) {
            return playerMicroFormatRenderer.getString("uploadDate");
        } else if (!playerMicroFormatRenderer.getString("publishDate", EMPTY_STRING).isEmpty()) {
            return playerMicroFormatRenderer.getString("publishDate");
        } else {
            final JsonObject liveDetails = playerMicroFormatRenderer.getObject(
                    "liveBroadcastDetails");
            if (!liveDetails.getString("endTimestamp", EMPTY_STRING).isEmpty()) {
                // an ended live stream
                return liveDetails.getString("endTimestamp");
            } else if (!liveDetails.getString("startTimestamp", EMPTY_STRING).isEmpty()) {
                // a running live stream
                return liveDetails.getString("startTimestamp");
            } else if (getStreamType() == StreamType.LIVE_STREAM) {
                // this should never be reached, but a live stream without upload date is valid
                return null;
            }
        }

        if (getTextFromObject(getVideoPrimaryInfoRenderer().getObject("dateText"))
                .startsWith("Premiered")) {
            final String time = getTextFromObject(
                    getVideoPrimaryInfoRenderer().getObject("dateText")).substring(13);

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
            // TODO: this parses English formatted dates only, we need a better approach to parse
            //  the textual date
            final LocalDate localDate = LocalDate.parse(getTextFromObject(
                    getVideoPrimaryInfoRenderer().getObject("dateText")),
                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
            return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
        } catch (final Exception e) {
            throw new ParsingException("Could not get upload date", e);
        }

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
            final JsonArray thumbnails = playerResponse.getObject("videoDetails")
                    .getObject("thumbnail").getArray("thumbnails");
            // the last thumbnail is the one with the highest resolution
            final String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

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
        try {
            final String description = getTextFromObject(getVideoSecondaryInfoRenderer()
                    .getObject("description"), true);
            if (!isNullOrEmpty(description)) {
                return new Description(description, Description.HTML);
            }
        } catch (final ParsingException ignored) {
            // Age-restricted videos cause a ParsingException here
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
        if (ageLimit == -1) {
            ageLimit = NO_AGE_LIMIT;

            final JsonArray metadataRows = getVideoSecondaryInfoRenderer()
                    .getObject("metadataRowContainer").getObject("metadataRowContainerRenderer")
                    .getArray("rows");
            for (final Object metadataRow : metadataRows) {
                final JsonArray contents = ((JsonObject) metadataRow)
                        .getObject("metadataRowRenderer").getArray("contents");
                for (final Object content : contents) {
                    final JsonArray runs = ((JsonObject) content).getArray("runs");
                    for (final Object run : runs) {
                        final String rowText = ((JsonObject) run).getString("text", EMPTY_STRING);
                        if (rowText.contains("Age-restricted")) {
                            ageLimit = 18;
                            return ageLimit;
                        }
                    }
                }
            }
        }
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
        } else {
            return timestamp;
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        String views = null;

        try {
            views = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("viewCount")
                    .getObject("videoViewCountRenderer").getObject("viewCount"));
        } catch (final ParsingException ignored) {
            // Age-restricted videos cause a ParsingException here
        }

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
        String likesString = "";
        try {
            likesString = getVideoPrimaryInfoRenderer()
                    .getObject("videoActions")
                    .getObject("menuRenderer")
                    .getArray("topLevelButtons")
                    .getObject(0)
                    .getObject("toggleButtonRenderer")
                    .getObject("defaultText")
                    .getObject("accessibility")
                    .getObject("accessibilityData")
                    .getString("label");

            if (likesString == null) {
                // If this kicks in our button has no content and therefore ratings must be disabled
                if (playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
                    throw new ParsingException(
                            "Ratings are enabled even though the like button is missing");
                }
                return -1;
            }

            if (likesString.toLowerCase().contains("no likes")) {
                return 0;
            }

            return Integer.parseInt(Utils.removeNonDigitCharacters(likesString));
        } catch (final NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + likesString + "\" as an Integer",
                    nfe);
        } catch (final Exception e) {
            if (getAgeLimit() == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get like count", e);
            }
            return -1;
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
        final JsonArray badges = getVideoSecondaryInfoRenderer().getObject("owner")
                .getObject("videoOwnerRenderer").getArray("badges");

        return YoutubeParsingHelper.isVerified(badges);
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        assertPageFetched();

        String url = null;

        try {
            url = getVideoSecondaryInfoRenderer().getObject("owner")
                    .getObject("videoOwnerRenderer").getObject("thumbnail")
                    .getArray("thumbnails").getObject(0).getString("url");
        } catch (final ParsingException ignored) {
            // Age-restricted videos cause a ParsingException here
        }

        if (isNullOrEmpty(url)) {
            if (ageLimit == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get uploader avatar URL");
            }

            return EMPTY_STRING;
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
        return getManifestUrl("dash", Arrays.asList(html5StreamingData,
                androidStreamingData));
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();

        // Return HLS manifest of the iOS client first because on livestreams, the HLS manifest
        // returned has separated audio and video streams
        // Also, on videos, non-iOS clients don't have an HLS manifest URL in their player response
        return getManifestUrl("hls", Arrays.asList(iosStreamingData, html5StreamingData,
                androidStreamingData));
    }

    @Nonnull
    private static String getManifestUrl(@Nonnull final String manifestType,
                                         @Nonnull final List<JsonObject> streamingDataObjects) {
        final String manifestKey = manifestType + "ManifestUrl";
        for (final JsonObject streamingDataObject : streamingDataObjects) {
            if (streamingDataObject != null) {
                final String manifestKeyValue = streamingDataObject.getString(manifestKey);
                if (manifestKeyValue != null) {
                    return manifestKeyValue;
                }
            }
        }

        return EMPTY_STRING;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        assertPageFetched();
        final List<AudioStream> audioStreams = new ArrayList<>();

        try {
            for (final Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS,
                    ItagItem.ItagType.AUDIO).entrySet()) {
                final ItagItem itag = entry.getValue();
                final String url = tryDecryption(entry.getKey(), getId());

                final AudioStream audioStream = new AudioStream(url, itag);
                if (!Stream.containSimilarStream(audioStream, audioStreams)) {
                    audioStreams.add(audioStream);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get audio streams", e);
        }

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();
        final List<VideoStream> videoStreams = new ArrayList<>();

        try {
            for (final Map.Entry<String, ItagItem> entry : getItags(FORMATS,
                    ItagItem.ItagType.VIDEO).entrySet()) {
                final ItagItem itag = entry.getValue();
                final String url = tryDecryption(entry.getKey(), getId());

                final VideoStream videoStream = new VideoStream(url, false, itag);
                if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                    videoStreams.add(videoStream);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }

        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        assertPageFetched();
        final List<VideoStream> videoOnlyStreams = new ArrayList<>();

        try {
            for (final Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS,
                    ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
                final ItagItem itag = entry.getValue();
                final String url = tryDecryption(entry.getKey(), getId());

                final VideoStream videoStream = new VideoStream(url, true, itag);
                if (!Stream.containSimilarStream(videoStream, videoOnlyStreams)) {
                    videoOnlyStreams.add(videoStream);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return videoOnlyStreams;
    }

    /**
     * Try to decrypt url and fallback to given url, because decryption is not
     * always needed.
     * This way a breaking change from YouTube does not result in a broken extractor.
     */
    private String tryDecryption(final String url, final String videoId) {
        try {
            return YoutubeThrottlingDecrypter.apply(url, videoId);
        } catch (final ParsingException e) {
            return url;
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
        if (subtitles != null) {
            // Already calculated
            return subtitles;
        }

        final JsonObject renderer = playerResponse.getObject("captions")
                .getObject("playerCaptionsTracklistRenderer");
        final JsonArray captionsArray = renderer.getArray("captionTracks");
        // TODO: use this to apply auto translation to different language from a source language
        // final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages");

        subtitles = new ArrayList<>();
        for (int i = 0; i < captionsArray.size(); i++) {
            final String languageCode = captionsArray.getObject(i).getString("languageCode");
            final String baseUrl = captionsArray.getObject(i).getString("baseUrl");
            final String vssId = captionsArray.getObject(i).getString("vssId");

            if (languageCode != null && baseUrl != null && vssId != null) {
                final boolean isAutoGenerated = vssId.startsWith("a.");
                final String cleanUrl = baseUrl
                        .replaceAll("&fmt=[^&]*", "") // Remove preexisting format if exists
                        .replaceAll("&tlang=[^&]*", ""); // Remove translation language

                subtitles.add(new SubtitlesStream(format, languageCode,
                        cleanUrl + "&fmt=" + format.getSuffix(), isAutoGenerated));
            }
        }

        return subtitles;
    }

    @Override
    public StreamType getStreamType() {
        assertPageFetched();

        return streamType;
    }

    private void setStreamType() {
        if (playerResponse.getObject("playabilityStatus").has("liveStreamability")
                || playerResponse.getObject("videoDetails").getBoolean("isPostLiveDvr", false)) {
            streamType = StreamType.LIVE_STREAM;
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

            final JsonArray results = nextResponse.getObject("contents")
                    .getObject("twoColumnWatchNextResults").getObject("secondaryResults")
                    .getObject("secondaryResults").getArray("results");

            final TimeAgoParser timeAgoParser = getTimeAgoParser();

            for (final Object resultObject : results) {
                final JsonObject result = (JsonObject) resultObject;
                if (result.has("compactVideoRenderer")) {
                    collector.commit(new YoutubeStreamInfoItemExtractor(
                            result.getObject("compactVideoRenderer"), timeAgoParser));
                } else if (result.has("compactRadioRenderer")) {
                    collector.commit(new YoutubeMixOrPlaylistInfoItemExtractor(
                            result.getObject("compactRadioRenderer")));
                } else if (result.has("compactPlaylistRenderer")) {
                    collector.commit(new YoutubeMixOrPlaylistInfoItemExtractor(
                            result.getObject("compactPlaylistRenderer")));
                }
            }
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
        } catch (final ParsingException | NullPointerException e) {
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
        if (sts == null) {
            getStsFromPlayerJs();
        }

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

        final boolean ageRestricted = playabilityStatus.getString("reason", EMPTY_STRING)
                .contains("age");

        setStreamType();

        if (!playerResponse.has(STREAMING_DATA)) {
            try {
                fetchTvHtml5EmbedJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
            }

            // Refresh the stream type because the stream type may be not properly known for
            // age-restricted videos
            setStreamType();
        }

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

        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                contentCountry)
                .value(VIDEO_ID, videoId)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
                .getBytes(UTF_8);
        nextResponse = getJsonPostResponse(NEXT, body, localization);

        if ((!ageRestricted && streamType == StreamType.VIDEO_STREAM)
                || isAndroidClientFetchForced) {
            try {
                fetchAndroidMobileJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
            }
        }

        if ((!ageRestricted && streamType == StreamType.LIVE_STREAM)
                || isIosClientFetchForced) {
            try {
                fetchIosMobileJsonPlayer(contentCountry, localization, videoId);
            } catch (final Exception ignored) {
            }
        }
    }

    private void checkPlayabilityStatus(final JsonObject youtubePlayerResponse,
                                        @Nonnull final JsonObject playabilityStatus)
            throws ParsingException {
        String status = playabilityStatus.getString("status");
        // If status exist, and is not "OK", throw the specific exception based on error message
        // or a ContentNotAvailableException with the reason text if it's an unknown reason.
        if (status != null && !status.equalsIgnoreCase("ok")) {
            final JsonObject newPlayabilityStatus
                    = youtubePlayerResponse.getObject("playabilityStatus");
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

            if (status.equalsIgnoreCase("unplayable") && reason != null) {
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
                            .getObject("errorScreen").getObject("playerErrorMessageRenderer")
                            .getObject("subreason"));
                    if (detailedErrorMessage != null && detailedErrorMessage.contains("country")) {
                        throw new GeographicRestrictionException(
                                "This video is not available in client's country.");
                    }
                }
            }

            throw new ContentNotAvailableException("Got error: \"" + reason + "\"");
        }
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
        final byte[] mobileBody = JsonWriter.string(prepareAndroidMobileJsonBuilder(
                localization, contentCountry)
                .value(VIDEO_ID, videoId)
                .value(CPN, androidCpn)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
                .getBytes(UTF_8);

        final JsonObject androidPlayerResponse = getJsonAndroidPostResponse(PLAYER,
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId);

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
        final byte[] mobileBody = JsonWriter.string(prepareIosMobileJsonBuilder(
                        localization, contentCountry)
                        .value(VIDEO_ID, videoId)
                        .value(CPN, iosCpn)
                        .value(CONTENT_CHECK_OK, true)
                        .value(RACY_CHECK_OK, true)
                        .done())
                .getBytes(UTF_8);

        final JsonObject iosPlayerResponse = getJsonIosPostResponse(PLAYER,
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId);

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
        if (sts == null) {
            getStsFromPlayerJs();
        }

        // Because a cpn is unique to each request, we need to generate it again
        html5Cpn = generateContentPlaybackNonce();

        final JsonObject tvHtml5EmbedPlayerResponse = getJsonPostResponse(PLAYER,
                createDesktopPlayerBody(localization, contentCountry, videoId, sts, true,
                        html5Cpn), localization);
        final JsonObject streamingData = tvHtml5EmbedPlayerResponse.getObject(
                STREAMING_DATA);
        if (!isNullOrEmpty(streamingData)) {
            playerResponse = tvHtml5EmbedPlayerResponse;
            html5StreamingData = streamingData;
        }
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

    private static void getStsFromPlayerJs() throws ParsingException {
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

    private JsonObject getVideoPrimaryInfoRenderer() throws ParsingException {
        if (videoPrimaryInfoRenderer != null) {
            return videoPrimaryInfoRenderer;
        }

        final JsonArray contents = nextResponse.getObject("contents")
                .getObject("twoColumnWatchNextResults").getObject("results").getObject("results")
                .getArray("contents");
        JsonObject theVideoPrimaryInfoRenderer = null;

        for (final Object content : contents) {
            if (((JsonObject) content).has("videoPrimaryInfoRenderer")) {
                theVideoPrimaryInfoRenderer = ((JsonObject) content)
                        .getObject("videoPrimaryInfoRenderer");
                break;
            }
        }

        if (isNullOrEmpty(theVideoPrimaryInfoRenderer)) {
            throw new ParsingException("Could not find videoPrimaryInfoRenderer");
        }

        videoPrimaryInfoRenderer = theVideoPrimaryInfoRenderer;
        return theVideoPrimaryInfoRenderer;
    }

    private JsonObject getVideoSecondaryInfoRenderer() throws ParsingException {
        if (videoSecondaryInfoRenderer != null) {
            return videoSecondaryInfoRenderer;
        }

        final JsonArray contents = nextResponse.getObject("contents")
                .getObject("twoColumnWatchNextResults").getObject("results").getObject("results")
                .getArray("contents");

        JsonObject theVideoSecondaryInfoRenderer = null;

        for (final Object content : contents) {
            if (((JsonObject) content).has("videoSecondaryInfoRenderer")) {
                theVideoSecondaryInfoRenderer = ((JsonObject) content)
                        .getObject("videoSecondaryInfoRenderer");
                break;
            }
        }

        if (isNullOrEmpty(theVideoSecondaryInfoRenderer)) {
            throw new ParsingException("Could not find videoSecondaryInfoRenderer");
        }

        videoSecondaryInfoRenderer = theVideoSecondaryInfoRenderer;
        return theVideoSecondaryInfoRenderer;
    }

    @Nonnull
    private Map<String, ItagItem> getItags(@Nonnull final String streamingDataKey,
                                           @Nonnull final ItagItem.ItagType itagTypeWanted) {
        final Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();
        if (html5StreamingData == null && androidStreamingData == null
                && iosStreamingData == null) {
            return urlAndItags;
        }

        final Map<String, JsonObject> streamingDataAndCpnLoopMap = new HashMap<>();
        // Use the androidStreamingData object first because there is no n param and no
        // signatureCiphers in streaming URLs of the Android client
        streamingDataAndCpnLoopMap.put(androidCpn, androidStreamingData);
        streamingDataAndCpnLoopMap.put(html5Cpn, html5StreamingData);
        // Use the iosStreamingData object in the last position because most of the available
        // streams can be extracted with the Android and web clients and also because the iOS
        // client is only enabled by default on livestreams
        streamingDataAndCpnLoopMap.put(iosCpn, iosStreamingData);

        for (final Map.Entry<String, JsonObject> entry : streamingDataAndCpnLoopMap.entrySet()) {
            urlAndItags.putAll(getStreamsFromStreamingDataKey(entry.getValue(), streamingDataKey,
                    itagTypeWanted, entry.getKey()));
        }

        return urlAndItags;
    }

    @Nonnull
    private Map<String, ItagItem> getStreamsFromStreamingDataKey(
            final JsonObject streamingData,
            @Nonnull final String streamingDataKey,
            @Nonnull final ItagItem.ItagType itagTypeWanted,
            @Nonnull final String contentPlaybackNonce) {

        final Map<String, ItagItem> urlAndItagsFromStreamingDataObject = new LinkedHashMap<>();
        if (streamingData != null && streamingData.has(streamingDataKey)) {
            final JsonArray formats = streamingData.getArray(streamingDataKey);
            for (int i = 0; i != formats.size(); ++i) {
                final JsonObject formatData = formats.getObject(i);
                final int itag = formatData.getInt("itag");

                if (ItagItem.isSupported(itag)) {
                    try {
                        final ItagItem itagItem = ItagItem.getItag(itag);
                        if (itagItem.itagType == itagTypeWanted) {
                            // Ignore streams that are delivered using YouTube's OTF format,
                            // as those only work with DASH and not with progressive HTTP.
                            if (formatData.getString("type", EMPTY_STRING)
                                    .equalsIgnoreCase("FORMAT_STREAM_TYPE_OTF")) {
                                continue;
                            }

                            final String streamUrl;
                            if (formatData.has("url")) {
                                streamUrl = formatData.getString("url") + "&cpn="
                                        + contentPlaybackNonce;
                            } else {
                                // This url has an obfuscated signature
                                final String cipherString = formatData.has("cipher")
                                        ? formatData.getString("cipher")
                                        : formatData.getString("signatureCipher");
                                final Map<String, String> cipher = Parser.compatParseMap(
                                        cipherString);
                                streamUrl = cipher.get("url") + "&" + cipher.get("sp") + "="
                                        + deobfuscateSignature(cipher.get("s"));
                            }

                            final JsonObject initRange = formatData.getObject("initRange");
                            final JsonObject indexRange = formatData.getObject("indexRange");
                            final String mimeType = formatData.getString("mimeType", EMPTY_STRING);
                            final String codec = mimeType.contains("codecs")
                                    ? mimeType.split("\"")[1] : EMPTY_STRING;

                            itagItem.setBitrate(formatData.getInt("bitrate"));
                            itagItem.setWidth(formatData.getInt("width"));
                            itagItem.setHeight(formatData.getInt("height"));
                            itagItem.setInitStart(Integer.parseInt(initRange.getString("start",
                                    "-1")));
                            itagItem.setInitEnd(Integer.parseInt(initRange.getString("end",
                                    "-1")));
                            itagItem.setIndexStart(Integer.parseInt(indexRange.getString("start",
                                    "-1")));
                            itagItem.setIndexEnd(Integer.parseInt(indexRange.getString("end",
                                    "-1")));
                            itagItem.fps = formatData.getInt("fps");
                            itagItem.setQuality(formatData.getString("quality"));
                            itagItem.setCodec(codec);

                            urlAndItagsFromStreamingDataObject.put(streamUrl, itagItem);
                        }
                    } catch (final UnsupportedEncodingException | ParsingException ignored) {
                    }
                }
            }
        }

        return urlAndItagsFromStreamingDataObject;
    }

    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        try {
            final JsonObject storyboards = playerResponse.getObject("storyboards");
            final JsonObject storyboardsRenderer;
            if (storyboards.has("playerLiveStoryboardSpecRenderer")) {
                storyboardsRenderer = storyboards.getObject("playerLiveStoryboardSpecRenderer");
            } else {
                storyboardsRenderer = storyboards.getObject("playerStoryboardSpecRenderer");
            }

            if (storyboardsRenderer == null) {
                return Collections.emptyList();
            }

            final String storyboardsRendererSpec = storyboardsRenderer.getString("spec");
            if (storyboardsRendererSpec == null) {
                return Collections.emptyList();
            }

            final String[] spec = storyboardsRendererSpec.split("\\|");
            final String url = spec[0];
            final ArrayList<Frameset> result = new ArrayList<>(spec.length - 1);

            for (int i = 1; i < spec.length; ++i) {
                final String[] parts = spec[i].split("#");
                if (parts.length != 8 || Integer.parseInt(parts[5]) == 0) {
                    continue;
                }
                final int frameWidth = Integer.parseInt(parts[0]);
                final int frameHeight = Integer.parseInt(parts[1]);
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
                        frameWidth,
                        frameHeight,
                        totalCount,
                        Integer.parseInt(parts[5]),
                        framesPerPageX,
                        framesPerPageY
                ));
            }
            result.trimToSize();
            return result;
        } catch (final Exception e) {
            throw new ExtractionException("Could not get frames", e);
        }
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        final boolean isUnlisted = playerMicroFormatRenderer.getBoolean("isUnlisted");
        return isUnlisted ? Privacy.UNLISTED : Privacy.PUBLIC;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return playerMicroFormatRenderer.getString("category", EMPTY_STRING);
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        final JsonObject metadataRowRenderer = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer").getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .getObject(0).getObject("metadataRowRenderer");

        final JsonArray contents = metadataRowRenderer.getArray("contents");
        final String license = getTextFromObject(contents.getObject(0));
        return license != null && "Licence".equals(getTextFromObject(metadataRowRenderer
                .getObject("title"))) ? license : "YouTube licence";
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
        final ArrayList<StreamSegment> segments = new ArrayList<>();
        if (nextResponse.has("engagementPanels")) {
            final JsonArray panels = nextResponse.getArray("engagementPanels");
            JsonArray segmentsArray = null;

            // Search for correct panel containing the data
            for (int i = 0; i < panels.size(); i++) {
                final String panelIdentifier = panels.getObject(i)
                        .getObject("engagementPanelSectionListRenderer")
                        .getString("panelIdentifier");
                // panelIdentifier might be null if the panel has something to do with ads
                // See https://github.com/TeamNewPipe/NewPipe/issues/7792#issuecomment-1030900188
                if ("engagement-panel-macro-markers-description-chapters".equals(panelIdentifier)) {
                    segmentsArray = panels.getObject(i)
                            .getObject("engagementPanelSectionListRenderer").getObject("content")
                            .getObject("macroMarkersListRenderer").getArray("contents");
                    break;
                }
            }

            if (segmentsArray != null) {
                final long duration = getLength();
                for (final Object object : segmentsArray) {
                    final JsonObject segmentJson = ((JsonObject) object)
                            .getObject("macroMarkersListItemRenderer");

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
                        final JsonArray previewsArray = segmentJson.getObject("thumbnail")
                                .getArray("thumbnails");
                        if (!previewsArray.isEmpty()) {
                            // Assume that the thumbnail with the highest resolution is at the
                            // last position
                            final String url = previewsArray
                                    .getObject(previewsArray.size() - 1).getString("url");
                            segment.setPreviewUrl(fixThumbnailUrl(url));
                        }
                    }
                    segments.add(segment);
                }
            }
        }
        return segments;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return YoutubeParsingHelper.getMetaInfo(
                nextResponse.getObject("contents").getObject("twoColumnWatchNextResults")
                        .getObject("results").getObject("results").getArray("contents"));
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
