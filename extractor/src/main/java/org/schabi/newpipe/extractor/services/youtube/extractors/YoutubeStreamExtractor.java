/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) 2019 Christian Schabesberger <chris.schabesberger@mailbox.org>
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

import static org.schabi.newpipe.extractor.services.youtube.YoutubeDescriptionHelper.attributedDescriptionToHtml;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.BADGES;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CPN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.LABEL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.METADATA_BADGE_RENDERER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.generateContentPlaybackNonce;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getVisionOsUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.extractors.ItagUtils.getItagDataFromAudioFormat;
import static org.schabi.newpipe.extractor.services.youtube.extractors.ItagUtils.getItagDataFromVideoFormat;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException;
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.PrivateContentException;
import org.schabi.newpipe.extractor.exceptions.YoutubeMusicPremiumContentException;
import org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;
import org.schabi.newpipe.extractor.services.youtube.InnertubeClientRequestInfo;
import org.schabi.newpipe.extractor.services.youtube.PoTokenProvider;
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptPlayerManager;
import org.schabi.newpipe.extractor.services.youtube.YoutubeMetaInfoHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeStreamHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.stream.SabrData;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseAudioDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseLiveSubtitlesDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseVideoDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeSabrStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.impl.UriManifestStream;
import org.schabi.newpipe.extractor.stream.impl.UriMuxedStream;
import org.schabi.newpipe.extractor.stream.impl.UriSubtitlesStream;
import org.schabi.newpipe.extractor.stream.impl.base.BaseAudioStreamImpl;
import org.schabi.newpipe.extractor.stream.impl.base.BaseVideoStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.LocaleCompat;
import org.schabi.newpipe.extractor.utils.Pair;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeStreamExtractor extends StreamExtractor {

    private static final String PREMIERED = "Premiered ";
    private static final String PREMIERED_ON = "Premiered on ";
    private static final String FORMATS = "formats";
    private static final String ADAPTIVE_FORMATS = "adaptiveFormats";
    private static final String STREAMING_DATA = "streamingData";
    private static final String NEXT = "next";
    private static final String PLAYABILITY_STATUS = "playabilityStatus";
    private static final String THUMBNAIL = "thumbnail";
    private static final String THUMBNAILS = "thumbnails";
    private static final String VIDEO_DETAILS = "videoDetails";
    private static final String TITLE = "title";

    /**
     * List of supported subtitles media formats by YouTube and the extractor with the
     * corresponding fmt value to use in the caption URLs
     */
    private static final List<Pair<SubtitlesMediaFormat, String>> SUPPORTED_SUBTITLES_MEDIAFORMATS
            = List.of(new Pair<>(SubtitlesMediaFormat.WEBVTT, "vtt"),
            new Pair<>(SubtitlesMediaFormat.TTML, "ttml"),
            new Pair<>(SubtitlesMediaFormat.SRT, "srt"),
            new Pair<>(SubtitlesMediaFormat.TRANSCRIPT1, "srt1"),
            new Pair<>(SubtitlesMediaFormat.TRANSCRIPT2, "srt2"),
            new Pair<>(SubtitlesMediaFormat.TRANSCRIPT3, "srt3"));

    // Regex to match parameter for alternative URL host of streaming URLs as a query parameter
    private static final Pattern MN_PARAM_QUERY_PATTERN = Pattern.compile("[&?]mn=([^&]+)");

    // Regex to match parameter for expiration timestamp of streaming URLs as a query parameter
    // Works for YouTube captions on regular videos and for Googlevideo streams (manifests, Itags)
    private static final Pattern EXPIRE_STREAMING_URL_QUERY_PATTERN =
            Pattern.compile("[&?]expire=([^&]+)");
    private static final Pattern EXPIRE_GOOGLEVIDEO_URL_PATH_PATTERN =
            Pattern.compile("/expire/([^/]+)");

    private JsonObject mainPlayerResponse;
    private JsonObject nextResponse;

    private final List<PlayerResponseAndClientInfo> playerResponseAndClientInfos =
            new ArrayList<>();

    private JsonObject videoPrimaryInfoRenderer;
    private JsonObject videoSecondaryInfoRenderer;
    private JsonObject playerMicroFormatRenderer;
    private JsonArray thumbnailsArray;
    private int ageLimit = -1;
    private StreamType streamType;

    @Nullable
    private String liveChatContinuation;

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
        title = mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getString(TITLE);

        if (isNullOrEmpty(title)) {
            title = getTextFromObject(getVideoPrimaryInfoRenderer().getObject(TITLE));

            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get name");
            }
        }

        return title;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        String timestamp = playerMicroFormatRenderer.getString("uploadDate", "");
        if (timestamp.isEmpty()) {
            timestamp = playerMicroFormatRenderer.getString("publishDate", "");
        }
        if (!timestamp.isEmpty()) {
            return timestamp;
        }

        final var liveDetails = playerMicroFormatRenderer.getObject("liveBroadcastDetails");
        timestamp = liveDetails.getString("endTimestamp", ""); // an ended live stream
        if (timestamp.isEmpty()) {
            // a running live stream
            timestamp = liveDetails.getString("startTimestamp", "");
        }
        if (!timestamp.isEmpty()) {
            return timestamp;
        } else if (getStreamType() == StreamType.LIVE_STREAM) {
            // this should never be reached, but a live stream without upload date is valid
            return null;
        }

        final var textObject = getVideoPrimaryInfoRenderer().getObject("dateText");
        final String rendererDateText = getTextFromObject(textObject);
        if (rendererDateText == null) {
            return null;
        } else if (rendererDateText.startsWith(PREMIERED_ON)) { // Premiered on 21 Feb 2020
            return rendererDateText.substring(PREMIERED_ON.length());
        } else if (rendererDateText.startsWith(PREMIERED)) {
            // Premiered 20 hours ago / Premiered Feb 21, 2020
            return rendererDateText.substring(PREMIERED.length());
        } else {
            return rendererDateText;
        }
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String dateText = getTextualUploadDate();
        try {
            return DateWrapper.fromOffsetDateTime(dateText);
        } catch (final ParsingException e) {
            // Try other patterns first
        }

        try { // Premiered 20 hours ago
            final var localization = new Localization("en");
            return TimeAgoPatternsManager.getTimeAgoParserFor(localization).parse(dateText);
        } catch (final ParsingException e) {
            // Try other patterns first
        }

        return parseOptionalDate(dateText, "MMM dd, yyyy")
                .or(() -> parseOptionalDate(dateText, "dd MMM yyyy"))
                .map(date -> new DateWrapper(date.atStartOfDay(), true))
                .orElseThrow(() ->
                    new ParsingException("Could not parse upload date \"" + dateText + "\""));
    }

    private Optional<LocalDate> parseOptionalDate(final String date, final String pattern) {
        try {
            // TODO: this parses English formatted dates only, we need a better approach to parse
            // the textual date
            final var formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            return Optional.of(LocalDate.parse(date, formatter));
        } catch (final DateTimeParseException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        assertPageFetched();
        try {
            return getImagesFromThumbnailsArray(thumbnailsArray);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails");
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
            return new Description(videoSecondaryInfoRendererDescription, Description.Type.HTML);
        }

        final String attributedDescription = attributedDescriptionToHtml(
                getVideoSecondaryInfoRenderer().getObject("attributedDescription"));
        if (!isNullOrEmpty(attributedDescription)) {
            return new Description(attributedDescription, Description.Type.HTML);
        }

        String description = mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getString("shortDescription");
        if (description == null) {
            final JsonObject descriptionObject = playerMicroFormatRenderer.getObject("description");
            description = getTextFromObject(descriptionObject);
        }

        // Raw non-html description
        return Description.of(description, Description.Type.PLAIN_TEXT);
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
                .streamAsJsonObjects()
                .flatMap(metadataRow -> metadataRow
                        .getObject("metadataRowRenderer")
                        .getArray("contents")
                        .streamAsJsonObjects())
                .flatMap(content -> content
                        .getArray("runs")
                        .streamAsJsonObjects())
                .map(run -> run.getString("text", ""))
                .anyMatch(rowText -> rowText.contains("Age-restricted"));

        ageLimit = ageRestricted ? 18 : NO_AGE_LIMIT;
        return ageLimit;
    }

    @Override
    public long getLength() throws ParsingException {
        assertPageFetched();

        try {
            final String duration = mainPlayerResponse.getObject(VIDEO_DETAILS)
                    .getString("lengthSeconds");
            return Long.parseLong(duration);
        } catch (final Exception e) {
            return getDurationFromFirstAdaptiveFormat();
        }
    }

    private int getDurationFromFirstAdaptiveFormat()
            throws ParsingException {
        for (final PlayerResponseAndClientInfo playerResponseAndClientInfo
                : playerResponseAndClientInfos) {
            final JsonArray adaptiveFormats = playerResponseAndClientInfo.playerResponse()
                    .getObject(STREAMING_DATA)
                    .getArray(ADAPTIVE_FORMATS);
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
            views = mainPlayerResponse.getObject(VIDEO_DETAILS)
                    .getString("viewCount");

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
        if (!mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getBoolean("allowRatings")) {
            return -1L;
        }

        final JsonArray topLevelButtons = getVideoPrimaryInfoRenderer()
                .getObject("videoActions")
                .getObject("menuRenderer")
                .getArray("topLevelButtons");

        try {
            return parseLikeCountFromLikeButtonViewModel(topLevelButtons);
        } catch (final ParsingException ignored) {
            // A segmentedLikeDislikeButtonRenderer could be returned instead of a
            // segmentedLikeDislikeButtonViewModel, so ignore extraction errors relative to
            // segmentedLikeDislikeButtonViewModel object
        }

        try {
            return parseLikeCountFromLikeButtonRenderer(topLevelButtons);
        } catch (final ParsingException e) {
            throw new ParsingException("Could not get like count", e);
        }
    }

    private static long parseLikeCountFromLikeButtonRenderer(
            @Nonnull final JsonArray topLevelButtons) throws ParsingException {
        String likesString = null;
        final JsonObject likeToggleButtonRenderer = topLevelButtons.streamAsJsonObjects()
                .map(button -> button.getObject("segmentedLikeDislikeButtonRenderer")
                        .getObject("likeButton")
                        .getObject("toggleButtonRenderer"))
                .filter(toggleButtonRenderer -> !isNullOrEmpty(toggleButtonRenderer))
                .findFirst()
                .orElse(null);

        if (likeToggleButtonRenderer != null) {
            // Use one of the accessibility strings available (this one has the same path as the
            // one used for comments' like count extraction)
            likesString = likeToggleButtonRenderer.getObject("accessibilityData")
                    .getObject("accessibilityData")
                    .getString(LABEL);

            // Use the other accessibility string available which contains the exact like count
            if (likesString == null) {
                likesString = likeToggleButtonRenderer.getObject("accessibility")
                        .getString(LABEL);
            }

            // Last method: use the defaultText's accessibility data, which contains the exact like
            // count too, except when it is equal to 0, where a localized string is returned instead
            if (likesString == null) {
                likesString = likeToggleButtonRenderer.getObject("defaultText")
                        .getObject("accessibility")
                        .getObject("accessibilityData")
                        .getString(LABEL);
            }

            // This check only works with English localizations!
            if (likesString != null && likesString.toLowerCase().contains("no likes")) {
                return 0;
            }
        }

        // If ratings are allowed and the likes string is null, it means that we couldn't extract
        // the full like count from accessibility data
        if (likesString == null) {
            throw new ParsingException("Could not get like count from accessibility data");
        }

        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(likesString));
        } catch (final NumberFormatException e) {
            throw new ParsingException("Could not parse \"" + likesString + "\" as a long", e);
        }
    }

    private static long parseLikeCountFromLikeButtonViewModel(
            @Nonnull final JsonArray topLevelButtons) throws ParsingException {
        // Try first with the current video actions buttons data structure
        final JsonObject likeToggleButtonViewModel = topLevelButtons.streamAsJsonObjects()
                .map(button -> button.getObject("segmentedLikeDislikeButtonViewModel")
                        .getObject("likeButtonViewModel")
                        .getObject("likeButtonViewModel")
                        .getObject("toggleButtonViewModel")
                        .getObject("toggleButtonViewModel")
                        .getObject("defaultButtonViewModel")
                        .getObject("buttonViewModel"))
                .filter(buttonViewModel -> !isNullOrEmpty(buttonViewModel))
                .findFirst()
                .orElse(null);

        if (likeToggleButtonViewModel == null) {
            throw new ParsingException("Could not find buttonViewModel object");
        }

        final String accessibilityText = likeToggleButtonViewModel.getString("accessibilityText");
        if (accessibilityText == null) {
            throw new ParsingException("Could not find buttonViewModel's accessibilityText string");
        }

        // The like count is always returned as a number in this element, even for videos with no
        // likes
        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(accessibilityText));
        } catch (final NumberFormatException e) {
            throw new ParsingException(
                    "Could not parse \"" + accessibilityText + "\" as a long", e);
        }
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        assertPageFetched();

        // Don't use the id in the videoSecondaryRenderer object to get real id of the uploader
        // The difference between the real id of the channel and the displayed id is especially
        // visible for music channels and autogenerated channels.
        final String uploaderId = mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getString("channelId");
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
        final String uploaderName = mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getString("author");
        if (isNullOrEmpty(uploaderName)) {
            throw new ParsingException("Could not get uploader name");
        }

        return uploaderName;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        final JsonObject videoOwnerRenderer = getVideoSecondaryInfoRenderer()
                        .getObject("owner")
                        .getObject("videoOwnerRenderer");

        if (videoOwnerRenderer.has(BADGES)) {
            return YoutubeParsingHelper.isVerified(videoOwnerRenderer
                .getArray(BADGES));
        }


        final JsonObject channel = YoutubeParsingHelper.getFirstCollaborator(
            videoOwnerRenderer.getObject("navigationEndpoint"));
        if (channel == null) {
            return false;
        }

        return YoutubeParsingHelper.hasArtistOrVerifiedIconBadgeAttachment(
            channel.getObject(TITLE)
                    .getArray("attachmentRuns"));
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        assertPageFetched();
        final JsonObject owner = getVideoSecondaryInfoRenderer().getObject("owner")
                        .getObject("videoOwnerRenderer");

        final List<Image> imageList;
        if (owner.has("avatarStack")) {
            imageList = getImagesFromThumbnailsArray(
                owner.getObject("avatarStack").getObject("avatarStackViewModel")
                    .getArray("avatars")
                    // only consider the first collaborator, which is the video owner
                    .getObject(0)
                    .getObject("avatarViewModel")
                    .getObject("image")
                    .getArray("sources"));
        } else {
            imageList = getImagesFromThumbnailsArray(owner.getObject(THUMBNAIL)
                    .getArray(THUMBNAILS));
        }

        if (imageList.isEmpty() && ageLimit == NO_AGE_LIMIT) {
            throw new ParsingException("Could not get uploader avatars");
        }

        return imageList;
    }

    @Override
    public long getUploaderSubscriberCount() throws ParsingException {
        final JsonObject videoOwnerRenderer = JsonUtils.getObject(videoSecondaryInfoRenderer,
                "owner.videoOwnerRenderer");

        String subscriberCountText = null;
        if (videoOwnerRenderer.has("subscriberCountText")) {
            subscriberCountText = getTextFromObject(videoOwnerRenderer
                .getObject("subscriberCountText"));
        } else {
            final String content = YoutubeParsingHelper.getFirstCollaborator(
                videoOwnerRenderer.getObject("navigationEndpoint")
            ).getObject("subtitle").getString("content");
            subscriberCountText = content.split("•")[1];
        }

        if (isNullOrEmpty(subscriberCountText)) {
            return UNKNOWN_SUBSCRIBER_COUNT;
        }

        try {
            return Utils.mixedNumberWordToLong(subscriberCountText);
        } catch (final NumberFormatException e) {
            throw new ParsingException("Could not get uploader subscriber count", e);
        }
    }

    static long getExpirationTimestampFromStreamingUrl(@Nonnull final String streamingUrl,
                                                       final boolean isPathUrl) {
        try {
            if (isPathUrl) {
                return Long.parseLong(
                        Parser.matchGroup1(EXPIRE_GOOGLEVIDEO_URL_PATH_PATTERN, streamingUrl));
            } else {
                return Long.parseLong(
                        Parser.matchGroup1(EXPIRE_STREAMING_URL_QUERY_PATTERN, streamingUrl));
            }
        } catch (final Exception e) {
            return UriObject.EXPIRATION_TIMESTAMP_UNKNOWN;
        }
    }

    private void buildCaptionsTranslations(
            @Nonnull final List<Pair<String, String>> translations,
            @Nonnull final List<BaseSubtitlesStream> translationsFromLanguage,
            @Nonnull final Map<String, List<String>> httpHeaders,
            @Nullable final String vssId,
            @Nonnull final String baseUrl,
            final long expirationTimestamp) {
        translations.forEach(translation -> {
            // Similar to what YouTube does on vssIDs, add "a." characters to know with the
            // subtitles stream ID that it is autogenerated
            // Distinguish the translation from the original language with a "_" character, even
            // when we don't know the original ID
            final String idPartToAdd = "_a." + translation.getFirst();
            SUPPORTED_SUBTITLES_MEDIAFORMATS.forEach(subtitleMediaFormatAndExt ->
                    translationsFromLanguage.add(new UriSubtitlesStream(
                            vssId == null ? idPartToAdd : vssId + idPartToAdd,
                            translation.getSecond(),
                            LocaleCompat.forLanguageTag(translation.getFirst())
                                    .orElse(null),
                            // Translations from subtitles are automatically generated
                            Boolean.TRUE,
                            subtitleMediaFormatAndExt.getFirst(),
                            // No translation from a translation
                            List.of(),
                            new HttpDeliverySource(new UriObject(baseUrl + "&tlang="
                                    + translation.getFirst() + "&fmt="
                                    + subtitleMediaFormatAndExt.getSecond(), expirationTimestamp,
                                    null), List.of(), httpHeaders,
                                    HttpDeliverySource.HttpMethod.GET, null),
                            StreamingProtocol.PROGRESSIVE)));
        });
    }

    private void addCaptions(@Nonnull final List<Stream> streams) {
        playerResponseAndClientInfos.forEach(playerResponseAndClientInfo -> {
            final JsonObject playerCaptionsTracklistRenderer =
                    playerResponseAndClientInfo.playerResponse.getObject("captions")
                            .getObject("playerCaptionsTracklistRenderer");
            final List<Pair<String, String>> translations =
                    playerCaptionsTracklistRenderer.getArray("translationLanguages")
                            .streamAsJsonObjects()
                            .map(translationLanguage -> {
                                final String languageCode = translationLanguage.getString(
                                        "languageCode");
                                if (isNullOrEmpty(languageCode)) {
                                    return null;
                                }
                                return new Pair<>(languageCode, getTextFromObject(
                                        translationLanguage.getObject("languageName")));
                            })
                            .filter(Objects::nonNull)
                            .toList();

            playerCaptionsTracklistRenderer.getArray("captionTracks")
                    .streamAsJsonObjects()
                    .forEach(captionTrack -> {
                        // Remove preexisting format if it exists
                        final String baseUrl = captionTrack.getString("baseUrl", "")
                                .replaceAll("&fmt=[^&]*", "");

                        if (baseUrl.isEmpty()) {
                            return;
                        }

                        final String vssId = captionTrack.getString("vssId");
                        final Boolean isAutoGenerated = vssId == null ? null
                                : vssId.startsWith("a.");

                        final List<BaseSubtitlesStream> translationsFromLanguage =
                                new ArrayList<>();

                        final Map<String, List<String>> httpHeaders =
                                playerResponseAndClientInfo.httpHeaders();
                        final long expirationTimestamp = getExpirationTimestampFromStreamingUrl(
                                baseUrl, false);

                        if (captionTrack.getBoolean("isTranslatable")) {
                            buildCaptionsTranslations(translations, translationsFromLanguage,
                                    httpHeaders, vssId, baseUrl, expirationTimestamp);
                        }

                        SUPPORTED_SUBTITLES_MEDIAFORMATS.forEach(subtitleMediaFormatAndExt ->
                                streams.add(new UriSubtitlesStream(
                                        vssId,
                                        getTextFromObject(captionTrack.getObject("name")),
                                        LocaleCompat.forLanguageTag(
                                                        captionTrack.getString("languageCode"))
                                                .orElse(null),
                                        isAutoGenerated,
                                        subtitleMediaFormatAndExt.getFirst(),
                                        translationsFromLanguage,
                                        new HttpDeliverySource(new UriObject(baseUrl
                                                + "&fmt=" + subtitleMediaFormatAndExt.getSecond(),
                                                expirationTimestamp, null), List.of(), httpHeaders,
                                                HttpDeliverySource.HttpMethod.GET, null),
                                        StreamingProtocol.PROGRESSIVE)));
                    });
        });
    }

    @Nullable
    private String buildFinalFormatStreamingUrl(@Nonnull final JsonObject formatData,
                                                @Nonnull final String videoId,
                                                @Nonnull final String contentPlaybackNonce,
                                                @Nullable final String poToken)
            throws ExtractionException {
        String streamUrl;
        if (formatData.has("url")) {
            streamUrl = formatData.getString("url");
        } else {
            // This url has an obfuscated signature
            final String cipherString = formatData.getString("signatureCipher");

            if (isNullOrEmpty(cipherString)) {
                return null;
            }

            final Map<String, String> cipher = Parser.compatParseMap(cipherString);
            final String signature = YoutubeJavaScriptPlayerManager.deobfuscateSignature(videoId,
                    cipher.getOrDefault("s", ""));
            streamUrl = cipher.get("url") + "&" + cipher.get("sp") + "=" + signature;
        }

        // Decode the n parameter if it is present
        // If it cannot be decoded, the stream cannot be used as streaming URLs return HTTP 403
        // responses if it has not the right value
        streamUrl = YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated(
                videoId, streamUrl);

        // Add the content playback nonce to the stream URL
        streamUrl += "&" + CPN + "=" + contentPlaybackNonce;

        // Add the poToken, if there is one
        if (poToken != null) {
            streamUrl += "&pot=" + poToken;
        }

        return streamUrl;
    }

    @Nonnull
    static List<UriObject> getAlternateStreamingUrls(@Nonnull final String streamingUrl,
                                                     final long expirationTimestamp) {
        final URL streamingUrlObj;
        try {
            streamingUrlObj = new URL(streamingUrl);
        } catch (final MalformedURLException e) {
            return List.of();
        }

        String mnValue = null;

        final List<UriObject> alternateUris = new ArrayList<>();
        try {
            mnValue = Parser.matchGroup1(MN_PARAM_QUERY_PATTERN, streamingUrlObj.getPath());
        } catch (final Parser.RegexException ignored) {
            // No mn query parameter
        }

        if (mnValue != null) {
            // The mnValue is URL encoded, so the "," separator is "%2C"
            final String[] mnValues = mnValue.split("%2C");
            if (mnValues.length == 2) {
                // The first value is the original host, the second one is the alternate one
                final String hostPartToReplace = mnValues[0];
                final String alternateHostEnd = mnValues[1];
                if (!alternateHostEnd.isEmpty()) {
                    alternateUris.add(new UriObject(streamingUrl.replace(hostPartToReplace,
                            alternateHostEnd), expirationTimestamp, null));
                }
            }
        }

        // Add Google video redirector as a last URL
        alternateUris.add(new UriObject(streamingUrl.replace(streamingUrlObj.getHost(),
                "redirector.googlevideo.com"), expirationTimestamp, null));
        return alternateUris;
    }

    private void addLegacyMuxedVideoFormats(@Nonnull final List<Stream> streams,
                                            @Nonnull final String videoId) {
        playerResponseAndClientInfos.forEach(playerResponseAndClientInfo ->
                playerResponseAndClientInfo.playerResponse.getObject(STREAMING_DATA)
                        .getArray(FORMATS)
                        .streamAsJsonObjects()
                        .forEach(format -> {
                            if (format.has("drmFamilies")) {
                                // Ignore DRM protected formats, as we do not support them
                                return;
                            }

                            final int itagId = format.getInt("itag", -1);
                            final AudioMediaFormat audioMediaFormat =
                                    ItagUtils.getAudioMediaFormatFromItagId(itagId);
                            if (audioMediaFormat == null) {
                                return;
                            }

                            final VideoMediaFormat videoMediaFormat =
                                    ItagUtils.getVideoMediaFormatFromItagId(itagId);
                            if (videoMediaFormat == null) {
                                return;
                            }

                            final String primaryStreamingUrl;
                            try {
                                primaryStreamingUrl = buildFinalFormatStreamingUrl(format,
                                        videoId, playerResponseAndClientInfo.contentPlaybackNonce,
                                        playerResponseAndClientInfo.poToken);
                                if (primaryStreamingUrl == null) {
                                    return;
                                }
                            } catch (final ExtractionException e) {
                                // Unable to get decoded throttling parameter for this stream,
                                // ignore it
                                return;
                            }

                            final String[] codecs = format.getString("mimeType")
                                    .split("\"");

                            String videoCodec = null;
                            String audioCodec = null;

                            if (codecs.length == 2) {
                                final String[] codecsArray = codecs[1].split(", ");
                                if (codecsArray.length == 2) {
                                    videoCodec = codecsArray[0];
                                    audioCodec = codecsArray[1];
                                }
                            }

                            Integer averageBitrate = format.getInt("averageBitrate");
                            if (averageBitrate <= 0) {
                                // Bad value returned by YouTube or data missing, return
                                // unknown in this case
                                averageBitrate = null;
                            }

                            final ItagUtils.AudioItagData audioItagData =
                                    getItagDataFromAudioFormat(format);
                            final ItagUtils.VideoItagData videoItagData =
                                    getItagDataFromVideoFormat(format);

                            final long expirationTimestamp = getExpirationTimestampFromStreamingUrl(
                                    primaryStreamingUrl, false);

                            streams.add(new UriMuxedStream(
                                    playerResponseAndClientInfo.clientInfo.clientName
                                            + "-" + itagId,
                                    format.getString("qualityLabel"),
                                    // We don't have the average bitrate of each track, so use null
                                    // The audio muxed is always the original one
                                    List.of(new BaseAudioStreamImpl(null, null, null,
                                            audioItagData.isAutoGenerated(), audioMediaFormat, null,
                                            audioItagData.sampleRate(),
                                            audioItagData.channelsCount(),
                                            audioItagData.trackType(), Boolean.FALSE, audioCodec)),
                                    // We don't have the average bitrate of each track, so use null
                                    // The projection type
                                    List.of(new BaseVideoStreamImpl(null, null, null,
                                            videoItagData.isAutoGenerated(), videoMediaFormat,
                                            videoItagData.projectionType(), null,
                                            videoItagData.width(), videoItagData.height(),
                                            videoItagData.fps(), videoCodec,
                                            videoItagData.isHdr())),
                                    List.of(), List.of(), averageBitrate, new HttpDeliverySource(
                                            new UriObject(primaryStreamingUrl, expirationTimestamp,
                                                    null), getAlternateStreamingUrls(
                                                            primaryStreamingUrl,
                                    expirationTimestamp), playerResponseAndClientInfo.httpHeaders,
                                    HttpDeliverySource.HttpMethod.GET, null),
                                    StreamingProtocol.PROGRESSIVE));
                        }));
    }

    private void buildAndAddManifestFormat(
            @Nonnull final List<Stream> streams,
            @Nonnull final String manifestKey,
            @Nonnull final String formatIdEndPart,
            @Nonnull final StreamingProtocol streamingProtocol,
            @Nonnull final String videoId,
            @Nonnull final PlayerResponseAndClientInfo playerResponseAndClientInfo) {
        final String originalUrl =
                playerResponseAndClientInfo.playerResponse.getObject(STREAMING_DATA)
                        .getString(manifestKey);

        if (!isNullOrEmpty(originalUrl)) {
            String finalUrl;
            try {
                // Decode the n parameter if it is present
                // If it cannot be decoded, the streaming URLs and captions will return HTTP 403
                // responses if it has not the right value
                finalUrl = YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated(
                        videoId, originalUrl);
            } catch (final ExtractionException ignored) {
                // Unable to add this stream due to throttling parameter deobfuscation issues,
                // ignore this manifest
                return;
            }

            // Unlike format streaming URL, manifest URLs uses paths to separate parameters' name
            // and value
            // Add the content playback nonce to the stream URL
            finalUrl += "/" + CPN + "/" + playerResponseAndClientInfo.contentPlaybackNonce();

            // Add the poToken, if there is one
            if (playerResponseAndClientInfo.poToken != null) {
                finalUrl += "/pot/" + playerResponseAndClientInfo.poToken;
            }

            streams.add(new UriManifestStream(playerResponseAndClientInfo.clientInfo.clientName
                    + formatIdEndPart, null, new HttpDeliverySource(
                            new UriObject(finalUrl, getExpirationTimestampFromStreamingUrl(finalUrl,
                                    true), null), List.of(),
                    playerResponseAndClientInfo.httpHeaders, HttpDeliverySource.HttpMethod.GET,
                    null), streamingProtocol));
        }
    }

    private void addManifests(@Nonnull final List<Stream> streams, @Nonnull final String videoId) {
        playerResponseAndClientInfos.forEach(playerResponseAndClientInfo -> {
            buildAndAddManifestFormat(streams, "hlsManifestUrl", "_HLS-Manifest",
                    StreamingProtocol.HLS, videoId, playerResponseAndClientInfo);
            // DASH manifests seem to be not returned anymore
            buildAndAddManifestFormat(streams, "dashManifestUrl", "_DASH-Manifest",
                    StreamingProtocol.DASH, videoId, playerResponseAndClientInfo);
        });
    }

    @Nullable
    private SabrData buildSabrData(
            @Nonnull final PlayerResponseAndClientInfo playerResponseAndClientInfo) {
        final JsonObject playerResponse = playerResponseAndClientInfo.playerResponse;
        final String ustreamerConfig = playerResponse.getObject("playerConfig")
                .getObject("mediaCommonConfig")
                .getObject("mediaUstreamerRequestConfig")
                .getString("videoPlaybackUstreamerConfig");
        if (ustreamerConfig == null) {
            return null;
        }

        return new SabrData(ustreamerConfig,
                Integer.parseInt(playerResponseAndClientInfo.clientInfo.clientId),
                playerResponseAndClientInfo.clientInfo.clientVersion);
    }

    private void addAdaptiveStreams(@Nonnull final List<Stream> streams,
                                    @Nonnull final String videoId) {
        playerResponseAndClientInfos.forEach(playerResponseAndClientInfo -> {
            final JsonObject streamingData =
                    playerResponseAndClientInfo.playerResponse.getObject(STREAMING_DATA);
            final String sabrStreamingUrl = streamingData.getString("serverAbrStreamingUrl");

            // These variables can't be initialized with null and modified to non-null values if
            // SABR data is not null due to the lambdas below, to avoid creating new variables, use
            // if and else conditions to initialize everything once
            final SabrData sabrData;
            final List<UriObject> sabrAlternateUrls;
            final List<YoutubeBaseDemuxedStream> sabrAudioStreams;
            final List<YoutubeBaseDemuxedStream> sabrVideoStreams;
            final List<YoutubeBaseDemuxedStream> sabrLiveSubtitlesStreams;

            Long sabrExpiryTimestamp = null;

            if (isNullOrEmpty(sabrStreamingUrl)) {
                sabrData = null;
                sabrAlternateUrls = null;
                sabrAudioStreams = null;
                sabrVideoStreams = null;
                sabrLiveSubtitlesStreams = null;
            } else {
                sabrData = buildSabrData(playerResponseAndClientInfo);
                if (sabrData == null) {
                    sabrAlternateUrls = null;
                    sabrAudioStreams = null;
                    sabrVideoStreams = null;
                    sabrLiveSubtitlesStreams = null;
                } else {
                    sabrExpiryTimestamp = getExpirationTimestampFromStreamingUrl(
                            sabrStreamingUrl, false);
                    sabrAlternateUrls = getAlternateStreamingUrls(
                            sabrStreamingUrl, sabrExpiryTimestamp);
                    sabrAudioStreams = new ArrayList<>();
                    sabrVideoStreams = new ArrayList<>();
                    sabrLiveSubtitlesStreams = new ArrayList<>();
                }
            }

            streamingData.getArray(ADAPTIVE_FORMATS)
                    .streamAsJsonObjects()
                    .forEach(format -> {
                        final int itagId = format.getInt("itag", -1);
                        if (itagId == -1) {
                            // No ID found, so we are unable to distinguish its type, ignore this
                            // stream
                            return;
                        }

                        if ("FORMAT_STREAM_TYPE_OTF".equalsIgnoreCase(format.getString("type"))
                                || format.has("drmFamilies")) {
                            // OTF streams seem to have been removed, so ignore them if we get them,
                            // as they may not work with SABR
                            // Ignore DRM protected formats, as we do not support them
                            return;
                        }

                        String primaryStreamingUrl;
                        try {
                            primaryStreamingUrl = buildFinalFormatStreamingUrl(format, videoId,
                                    playerResponseAndClientInfo.contentPlaybackNonce,
                                    playerResponseAndClientInfo.poToken);
                        } catch (final ExtractionException e) {
                            // Unable to get decoded throttling parameter for this stream, ignore
                            // it, as we may be able to get it with SABR
                            primaryStreamingUrl = null;
                        }

                        final String id = playerResponseAndClientInfo.clientInfo.clientName
                                + "-" + itagId;
                        final ItagUtils.ItagParseResult parseResult =
                                ItagUtils.parseItag(format, itagId);
                        switch (parseResult.itagType()) {
                            case AUDIO -> parseResult.itagToStreamBuilder()
                                    .buildAndAddStreamFormats(id, playerResponseAndClientInfo,
                                            primaryStreamingUrl, sabrData, streams,
                                            sabrAudioStreams);
                            case VIDEO -> parseResult.itagToStreamBuilder()
                                    .buildAndAddStreamFormats(id, playerResponseAndClientInfo,
                                            primaryStreamingUrl, sabrData, streams,
                                            sabrVideoStreams);
                            case SUBTITLES -> parseResult.itagToStreamBuilder()
                                    .buildAndAddStreamFormats(id, playerResponseAndClientInfo,
                                            primaryStreamingUrl, sabrData, streams,
                                            sabrLiveSubtitlesStreams);
                            case UNKNOWN -> {
                                // Nothing to do, unsupported stream type
                            }
                        }
                    });

            if (sabrData != null) {
                // Castings are safe here, as we only add objects of these types in these lists
                // We can't use directly the types as the lists ones (i.e. by wrapping the original
                // lists into unmodifiable ones) due to the generics usage in the
                // itagToStreamBuilder method above
                streams.add(new YoutubeSabrStream(
                        playerResponseAndClientInfo.clientInfo.clientName + "-SABR",
                        sabrAudioStreams.stream()
                                .map(YoutubeBaseAudioDemuxedStream.class::cast)
                                .toList(),
                        sabrVideoStreams.stream()
                                .map(YoutubeBaseVideoDemuxedStream.class::cast)
                                .toList(),
                        sabrLiveSubtitlesStreams.stream()
                                .map(YoutubeBaseLiveSubtitlesDemuxedStream.class::cast)
                                .toList(),
                        sabrData,
                        playerResponseAndClientInfo.poToken,
                        new HttpDeliverySource(new UriObject(
                                sabrStreamingUrl, sabrExpiryTimestamp, null),
                                sabrAlternateUrls, playerResponseAndClientInfo.httpHeaders,
                                HttpDeliverySource.HttpMethod.POST, null)));
            }
        });
    }

    @Nonnull
    @Override
    public List<Stream> getStreams() throws IOException, ParsingException {
        assertPageFetched();

        final List<Stream> streams = new ArrayList<>();
        final String videoId = getId();

        if (streamType == StreamType.VIDEO_STREAM) {
            addCaptions(streams);
            addLegacyMuxedVideoFormats(streams, videoId);
        }

        addManifests(streams, videoId);
        addAdaptiveStreams(streams, videoId);
        return Collections.unmodifiableList(streams);
    }

    @Override
    public StreamType getStreamType() {
        assertPageFetched();

        return streamType;
    }

    @Override
    public boolean hasLiveChat() {
        return liveChatContinuation != null;
    }

    @Override
    @Nullable
    public String getLiveChatContinuation() {
        return liveChatContinuation;
    }

    private void setStreamType() {
        if (mainPlayerResponse.getObject(PLAYABILITY_STATUS).has("liveStreamability")) {
            streamType = StreamType.LIVE_STREAM;
        } else if (mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getBoolean("isPostLiveDvr", false)) {
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
            results.streamAsJsonObjects()
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
                        } else if (result.has("lockupViewModel")) {
                            final JsonObject lockupViewModel = result.getObject("lockupViewModel");
                            final String contentType = lockupViewModel.getString("contentType");
                            if ("LOCKUP_CONTENT_TYPE_PLAYLIST".equals(contentType)
                                    || "LOCKUP_CONTENT_TYPE_PODCAST".equals(contentType)) {
                                return new YoutubeMixOrPlaylistLockupInfoItemExtractor(
                                        lockupViewModel);
                            } else if ("LOCKUP_CONTENT_TYPE_VIDEO".equals(contentType)) {
                                return new YoutubeStreamInfoItemLockupExtractor(
                                        lockupViewModel, timeAgoParser);
                            }
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
            return getTextFromObject(mainPlayerResponse.getObject(PLAYABILITY_STATUS)
                    .getObject("errorScreen").getObject("playerErrorMessageRenderer")
                    .getObject("reason"));
        } catch (final NullPointerException e) {
            return null; // No error message
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String videoId = getId();

        final Localization localization = getExtractorLocalization();
        final ContentCountry contentCountry = getExtractorContentCountry();

        fetchVisionOsClient(localization, contentCountry, videoId);
        setStreamType();

        fetchWebClientMetadataAndSetThumbnails(localization, contentCountry, videoId);

        final byte[] nextBody = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, contentCountry)
                        .value(VIDEO_ID, videoId)
                        .value(CONTENT_CHECK_OK, true)
                        .value(RACY_CHECK_OK, true)
                        .done())
                .getBytes(StandardCharsets.UTF_8);
        nextResponse = getJsonPostResponse(NEXT, nextBody, localization);

        // Check for live chat availability
        findLiveChatContinuation(nextResponse);
    }

    private void findLiveChatContinuation(final JsonObject response) {
        try {
            final JsonObject liveChatRenderer = response
                    .getObject("contents")
                    .getObject("twoColumnWatchNextResults")
                    .getObject("conversationBar")
                    .getObject("liveChatRenderer");
            liveChatContinuation = liveChatRenderer
                    .getArray("continuations")
                    .getObject(0)
                    .getObject("reloadContinuationData")
                    .getString("continuation");
        } catch (final Exception e) {
            liveChatContinuation = null;
        }
    }

    private static void checkPlayabilityStatus(@Nonnull final JsonObject playabilityStatus)
            throws ParsingException {
        final String status = playabilityStatus.getString("status");
        if (status == null || status.equalsIgnoreCase("ok")) {
            return;
        }

        final String reason = playabilityStatus.getString("reason");

        if (reason != null) {
            if (status.equalsIgnoreCase("login_required")) {
                if (reason.contains("inappropriate for some users")) {
                    throw new AgeRestrictedContentException(
                            "This age-restricted video cannot be watched anonymously");
                }

                if (playabilityStatus.getArray("messages")
                        .stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .anyMatch(message -> !isNullOrEmpty(message)
                                && message.contains("private"))) {
                    throw new PrivateContentException("This video is private");
                }

                if (reason.contains("a bot")) {
                    throw new SignInConfirmNotBotException(
                            "YouTube probably temporarily blocked anonymous watch access with this"
                                    + " IP , got error " + status + ": \"" + reason + "\"");
                }
            }

            if (status.equalsIgnoreCase("unplayable") || status.equalsIgnoreCase("error")) {
                if (reason.contains("Music Premium")) {
                    throw new YoutubeMusicPremiumContentException();
                }

                if (reason.contains("payment")) {
                    throw new PaidContentException("This video is a paid video");
                }

                if (reason.contains("members")) {
                    throw new PaidContentException("This video is only available for members of "
                            + "the channel of this video");
                }

                if (reason.contains("country")) {
                    throw new GeographicRestrictionException(
                            "This video is not available in client's country.");
                }

                if (reason.contains("closed") || reason.contains("terminated")) {
                    throw new AccountTerminatedException(reason);
                }
            }
        }

        throw new ContentNotAvailableException("Got error " + status + ": \"" + reason + "\"");
    }

    private void fetchVisionOsClient(@Nonnull final Localization localization,
                                     @Nonnull final ContentCountry contentCountry,
                                     @Nonnull final String videoId) throws IOException,
            ExtractionException {
        final String visionOsCpn = generateContentPlaybackNonce();

        mainPlayerResponse = YoutubeStreamHelper.getVisionOsPlayerResponse(contentCountry,
                localization, videoId, visionOsCpn);

        checkPlayabilityStatus(mainPlayerResponse.getObject(PLAYABILITY_STATUS));
        if (isPlayerResponseNotValid(mainPlayerResponse, videoId)) {
            throw new ExtractionException("VISIONOS player response is not valid");
        }

        final Map<String, List<String>> headers = Map.of("User-Agent",
                List.of(getVisionOsUserAgent(localization)));
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofVisionOsClient();

        playerResponseAndClientInfos.add(new PlayerResponseAndClientInfo(mainPlayerResponse,
                innertubeClientRequestInfo.clientInfo, innertubeClientRequestInfo.deviceInfo,
                headers, visionOsCpn, null));
    }

    private void fetchWebClientMetadataAndSetThumbnails(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId) {
        try {
            final JsonObject webPlayerResponse = YoutubeStreamHelper.getWebMetadataPlayerResponse(
                    localization, contentCountry, videoId);

            // Important note: we don't checkPlayabilityStatus() here, because we use this request
            // exclusively for metadata, not for extracting streams. It turns out that when
            // YouTube returns a playability status error, the metadata may still be there.

            if (!isPlayerResponseNotValid(webPlayerResponse, videoId)) {
                // The microformat JSON object of the content is only returned on the WEB client,
                // so we need to store it instead of getting it directly from the playerResponse
                playerMicroFormatRenderer = webPlayerResponse.getObject("microformat")
                        .getObject("playerMicroformatRenderer");

                // Try to use web player response thumbnails first, as they should contain higher
                // quality ones than mobile clients
                final JsonObject thumbnailWebJsonObj = webPlayerResponse.getObject(VIDEO_DETAILS)
                        .getObject(THUMBNAIL);
                if (thumbnailWebJsonObj.containsKey(THUMBNAILS)) {
                    thumbnailsArray = thumbnailWebJsonObj.getArray(THUMBNAILS);
                } else {
                    thumbnailsArray = mainPlayerResponse.getObject(VIDEO_DETAILS)
                            .getObject(THUMBNAIL)
                            .getArray(THUMBNAILS);
                }
            }
        } catch (final Exception e) {
            // Ignore exceptions related to WEB client fetching or parsing, as it is not
            // compulsory to play contents
            // Set thumbnails from playerResponse
            playerMicroFormatRenderer = new JsonObject();
            thumbnailsArray = mainPlayerResponse.getObject(VIDEO_DETAILS)
                    .getObject(THUMBNAIL)
                    .getArray(THUMBNAILS);
        }
    }

    /**
     * Checks whether a player response is invalid.
     *
     * <p>
     * If YouTube detects that requests come from a third party client, they may replace the real
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
        return !videoId.equals(playerResponse.getObject(VIDEO_DETAILS)
                .getString("videoId"));
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
                .streamAsJsonObjects()
                .filter(content -> content.has(videoRendererName))
                .map(content -> content.getObject(videoRendererName))
                .findFirst()
                .orElse(new JsonObject());
    }

    /**
     * {@inheritDoc}
     * Should return a list of Frameset object that contains preview of stream frames
     *
     * <p><b>Warning:</b> When using this method be aware
     * that the YouTube API very rarely returns framesets,
     * that are slightly too small e.g. framesPerPageX = 5, frameWidth = 160, but the url contains
     * a storyboard that is only 795 pixels wide (5*160 &gt; 795). You will need to handle this
     * "manually" to avoid errors.</p>
     *
     * @see <a href="https://github.com/TeamNewPipe/NewPipe/pull/11596">
     *     TeamNewPipe/NewPipe#11596</a>
     */
    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        try {
            final JsonObject storyboards = mainPlayerResponse.getObject("storyboards");
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
                || getVideoPrimaryInfoRenderer().getArray(BADGES)
                .streamAsJsonObjects()
                .anyMatch(badge ->
                        "PRIVACY_UNLISTED".equals(badge.getObject(METADATA_BADGE_RENDERER)
                                .getObject("icon")
                                .getString("iconType")))
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
                && "Licence".equals(getTextFromObject(metadataRowRenderer.getObject(TITLE)))
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
        return JsonUtils.getStringListFromJsonArray(mainPlayerResponse.getObject(VIDEO_DETAILS)
                .getArray("keywords"));
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {

        if (!nextResponse.has("engagementPanels")) {
            return Collections.emptyList();
        }

        final JsonArray segmentsArray = nextResponse.getArray("engagementPanels")
                .streamAsJsonObjects()
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
        final var segmentStream = segmentsArray.streamAsJsonObjects()
                .map(object -> object.getObject("macroMarkersListItemRenderer"));
        final var it = segmentStream.iterator();

        while (it.hasNext()) {
            final var segmentJson = it.next();
            final int startTimeSeconds = segmentJson.getObject("onTap")
                    .getObject("watchEndpoint").getInt("startTimeSeconds", -1);

            if (startTimeSeconds == -1) {
                throw new ParsingException("Could not get stream segment start time.");
            }
            if (startTimeSeconds > duration) {
                break;
            }

            final String title = getTextFromObject(segmentJson.getObject(TITLE));
            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get stream segment title.");
            }

            final StreamSegment segment = new StreamSegment(title, startTimeSeconds);
            segment.setUrl(getUrl() + "?t=" + startTimeSeconds);
            if (segmentJson.has(THUMBNAIL)) {
                final JsonArray previewsArray = segmentJson.getObject(THUMBNAIL)
                        .getArray(THUMBNAILS);
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
        return YoutubeMetaInfoHelper.getMetaInfo(nextResponse
                .getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("results")
                .getObject("results")
                .getArray("contents"));
    }

    /**
     * Set the {@link PoTokenProvider} instance to be used for fetching {@code poToken}s.
     *
     * <p>
     * <b>This method currently doesn't do anything, as the extractor doesn't use any client
     * supporting poTokens until SABR support is added to the extractor.</b>
     * </p>
     *
     * <p>
     * This method allows setting an implementation of {@link PoTokenProvider} which will be used
     * to obtain poTokens required for YouTube player requests and streaming URLs. These tokens
     * are used by YouTube to verify the integrity of the user's device or browser and are required
     * for playback with several clients.
     * </p>
     *
     * <p>
     * Without a {@link PoTokenProvider}, the extractor makes its best effort to fetch as many
     * streams as possible, but without {@code poToken}s, some formats may be not available or
     * fetching may be slower due to additional requests done to get streams.
     * </p>
     *
     * <p>
     * Note that any provider change will be only applied on the next {@link #fetchPage()} request.
     * </p>
     *
     * @param poTokenProvider the {@link PoTokenProvider} instance to set, which can be null to
     *                        remove a provider already passed
     * @see PoTokenProvider
     */
    @SuppressWarnings("unused")
    public static void setPoTokenProvider(@Nullable final PoTokenProvider poTokenProvider) {
        // Nothing to do for now, see why in the Javadoc
    }

    record PlayerResponseAndClientInfo(
            @Nonnull JsonObject playerResponse,
            @Nonnull InnertubeClientRequestInfo.ClientInfo clientInfo,
            @Nonnull InnertubeClientRequestInfo.DeviceInfo deviceInfo,
            @Nonnull Map<String, List<String>> httpHeaders,
            @Nonnull String contentPlaybackNonce,
            @Nullable String poToken) {
    }
}
