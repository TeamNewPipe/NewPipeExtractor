package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.*;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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
    private String playerJsUrl = null;

    private JsonArray initialAjaxJson;
    private JsonObject initialData;
    @Nonnull
    private final Map<String, String> videoInfoPage = new HashMap<>();
    private JsonObject playerResponse;
    private JsonObject videoPrimaryInfoRenderer;
    private JsonObject videoSecondaryInfoRenderer;
    private int ageLimit = -1;
    @Nullable
    private List<SubtitlesStream> subtitles = null;

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
            // age-restricted videos cause a ParsingException here
        }

        if (isNullOrEmpty(title)) {
            title = playerResponse.getObject("videoDetails").getString("title");

            if (isNullOrEmpty(title)) throw new ParsingException("Could not get name");
        }

        return title;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        final JsonObject micro =
                playerResponse.getObject("microformat").getObject("playerMicroformatRenderer");
        if (!micro.getString("uploadDate", EMPTY_STRING).isEmpty()) {
            return micro.getString("uploadDate");
        } else if (!micro.getString("publishDate", EMPTY_STRING).isEmpty()) {
            return micro.getString("publishDate");
        } else {
            final JsonObject liveDetails = micro.getObject("liveBroadcastDetails");
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

        if (getTextFromObject(getVideoPrimaryInfoRenderer().getObject("dateText")).startsWith("Premiered")) {
            String time = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("dateText")).substring(10);

            try { // Premiered 20 hours ago
                TimeAgoParser timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.fromLocalizationCode("en"));
                OffsetDateTime parsedTime = timeAgoParser.parse(time).offsetDateTime();
                return DateTimeFormatter.ISO_LOCAL_DATE.format(parsedTime);
            } catch (final Exception ignored) {
            }

            try { // Premiered Feb 21, 2020
                final LocalDate localDate = LocalDate.parse(time,
                        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH));
                return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
            } catch (final Exception ignored) {
            }
        }

        try {
            // TODO: this parses English formatted dates only, we need a better approach to parse the textual date
            LocalDate localDate = LocalDate.parse(getTextFromObject(getVideoPrimaryInfoRenderer().getObject("dateText")),
                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
            return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
        } catch (final Exception ignored) {
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
            JsonArray thumbnails = playerResponse.getObject("videoDetails").getObject("thumbnail").getArray("thumbnails");
            // the last thumbnail is the one with the highest resolution
            String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnail url");
        }

    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        assertPageFetched();
        // description with more info on links
        try {
            String description = getTextFromObject(getVideoSecondaryInfoRenderer().getObject("description"), true);
            if (!isNullOrEmpty(description)) return new Description(description, Description.HTML);
        } catch (final ParsingException ignored) {
            // age-restricted videos cause a ParsingException here
        }

        String description = playerResponse.getObject("videoDetails").getString("shortDescription");
        if (description == null) {
            final JsonObject descriptionObject = playerResponse.getObject("microformat")
                    .getObject("playerMicroformatRenderer").getObject("description");
            description = getTextFromObject(descriptionObject);
        }

        // raw non-html description
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
            String duration = playerResponse
                    .getObject("videoDetails")
                    .getString("lengthSeconds");
            return Long.parseLong(duration);
        } catch (final Exception e) {
            try {
                String durationMs = playerResponse
                        .getObject("streamingData")
                        .getArray("formats")
                        .getObject(0)
                        .getString("approxDurationMs");
                return Math.round(Long.parseLong(durationMs) / 1000f);
            } catch (final Exception ignored) {
                throw new ParsingException("Could not get duration", e);
            }
        }
    }

    /**
     * Attempts to parse (and return) the offset to start playing the video from.
     *
     * @return the offset (in seconds), or 0 if no timestamp is found.
     */
    @Override
    public long getTimeStamp() throws ParsingException {
        final long timestamp =
                getTimestampSeconds("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");

        if (timestamp == -2) {
            // regex for timestamp was not found
            return 0;
        } else {
            return timestamp;
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        assertPageFetched();
        String views = null;

        try {
            views = getTextFromObject(getVideoPrimaryInfoRenderer().getObject("viewCount")
                    .getObject("videoViewCountRenderer").getObject("viewCount"));
        } catch (final ParsingException ignored) {
            // age-restricted videos cause a ParsingException here
        }

        if (isNullOrEmpty(views)) {
            views = playerResponse.getObject("videoDetails").getString("viewCount");

            if (isNullOrEmpty(views)) throw new ParsingException("Could not get view count");
        }

        if (views.toLowerCase().contains("no views")) return 0;

        return Long.parseLong(Utils.removeNonDigitCharacters(views));
    }

    @Override
    public long getLikeCount() throws ParsingException {
        assertPageFetched();
        String likesString = "";
        try {
            try {
                likesString = getVideoPrimaryInfoRenderer().getObject("sentimentBar")
                        .getObject("sentimentBarRenderer").getString("tooltip").split("/")[0];
            } catch (final NullPointerException e) {
                // if this kicks in our button has no content and therefore ratings must be disabled
                if (playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
                    throw new ParsingException("Ratings are enabled even though the like button is missing", e);
                }
                return -1;
            }
            return Integer.parseInt(Utils.removeNonDigitCharacters(likesString));
        } catch (final NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + likesString + "\" as an Integer", nfe);
        } catch (final Exception e) {
            if (getAgeLimit() == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get like count", e);
            }
            return -1;
        }
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        assertPageFetched();
        String dislikesString = "";
        try {
            try {
                dislikesString = getVideoPrimaryInfoRenderer().getObject("sentimentBar")
                        .getObject("sentimentBarRenderer").getString("tooltip").split("/")[1];
            } catch (final NullPointerException e) {
                // if this kicks in our button has no content and therefore ratings must be disabled
                if (playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
                    throw new ParsingException("Ratings are enabled even though the dislike button is missing", e);
                }
                return -1;
            }
            return Integer.parseInt(Utils.removeNonDigitCharacters(dislikesString));
        } catch (final NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + dislikesString + "\" as an Integer", nfe);
        } catch (final Exception e) {
            if (getAgeLimit() == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get dislike count", e);
            }
            return -1;
        }
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        assertPageFetched();

        try {
            final String uploaderUrl = getUrlFromNavigationEndpoint(getVideoSecondaryInfoRenderer()
                    .getObject("owner").getObject("videoOwnerRenderer").getObject("navigationEndpoint"));
            if (!isNullOrEmpty(uploaderUrl)) {
                return uploaderUrl;
            }
        } catch (final ParsingException ignored) {
            // age-restricted videos cause a ParsingException here
        }

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

        String uploaderName = null;

        try {
            uploaderName = getTextFromObject(getVideoSecondaryInfoRenderer().getObject("owner")
                    .getObject("videoOwnerRenderer").getObject("title"));
        } catch (final ParsingException ignored) {
        }

        if (isNullOrEmpty(uploaderName)) {
            uploaderName = playerResponse.getObject("videoDetails").getString("author");

            if (isNullOrEmpty(uploaderName)) throw new ParsingException("Could not get uploader name");
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
            url = getVideoSecondaryInfoRenderer().getObject("owner").getObject("videoOwnerRenderer")
                    .getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");
        } catch (final ParsingException ignored) {
            // age-restricted videos cause a ParsingException here
        }

        if (isNullOrEmpty(url)) {
            if (ageLimit == NO_AGE_LIMIT) {
                throw new ParsingException("Could not get uploader avatar URL");
            }
            return "";
        }

        return fixThumbnailUrl(url);
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        assertPageFetched();
        try {
            String dashManifestUrl;
            if (playerResponse.getObject("streamingData").isString("dashManifestUrl")) {
                return playerResponse.getObject("streamingData").getString("dashManifestUrl");
            } else if (videoInfoPage.containsKey("dashmpd")) {
                dashManifestUrl = videoInfoPage.get("dashmpd");
            } else {
                return "";
            }

            if (!dashManifestUrl.contains("/signature/")) {
                String obfuscatedSig = Parser.matchGroup1("/s/([a-fA-F0-9\\.]+)", dashManifestUrl);
                final String deobfuscatedSig;

                deobfuscatedSig = deobfuscateSignature(obfuscatedSig);
                dashManifestUrl = dashManifestUrl.replace("/s/" + obfuscatedSig, "/signature/" + deobfuscatedSig);
            }

            return dashManifestUrl;
        } catch (final Exception e) {
            throw new ParsingException("Could not get dash manifest url", e);
        }
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();

        try {
            return playerResponse.getObject("streamingData").getString("hlsManifestUrl");
        } catch (final Exception e) {
            throw new ParsingException("Could not get hls manifest url", e);
        }
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        assertPageFetched();
        final List<AudioStream> audioStreams = new ArrayList<>();

        try {
            for (final Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.AUDIO).entrySet()) {
                final ItagItem itag = entry.getValue();
                final AudioStream audioStream = new AudioStream(entry.getKey(), itag);
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
            for (final Map.Entry<String, ItagItem> entry : getItags(FORMATS, ItagItem.ItagType.VIDEO).entrySet()) {
                final ItagItem itag = entry.getValue();
                final VideoStream videoStream = new VideoStream(entry.getKey(), false, itag);
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
            for (final Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
                final ItagItem itag = entry.getValue();

                final VideoStream videoStream = new VideoStream(entry.getKey(), true, itag);
                if (!Stream.containSimilarStream(videoStream, videoOnlyStreams)) {
                    videoOnlyStreams.add(videoStream);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return videoOnlyStreams;
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
        // if the video is age restricted getPlayerConfig will fail
        if (getAgeLimit() != NO_AGE_LIMIT) {
            return Collections.emptyList();
        }
        if (subtitles != null) {
            // already calculated
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
        return playerResponse.getObject("streamingData").has(FORMATS)
                ? StreamType.VIDEO_STREAM : StreamType.LIVE_STREAM;
    }

    @Nullable
    private StreamInfoItemExtractor getNextStream() throws ExtractionException {
        try {
            final JsonObject firstWatchNextItem = initialData.getObject("contents")
                    .getObject("twoColumnWatchNextResults").getObject("secondaryResults")
                    .getObject("secondaryResults").getArray("results").getObject(0);

            if (!firstWatchNextItem.has("compactAutoplayRenderer")) {
                // there is no "next" stream
                return null;
            }

            final JsonObject videoInfo = firstWatchNextItem.getObject("compactAutoplayRenderer")
                    .getArray("contents").getObject(0).getObject("compactVideoRenderer");

            return new YoutubeStreamInfoItemExtractor(videoInfo, getTimeAgoParser());
        } catch (final Exception e) {
            throw new ParsingException("Could not get next video", e);
        }
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() throws ExtractionException {
        assertPageFetched();

        if (getAgeLimit() != NO_AGE_LIMIT) {
            return null;
        }

        try {
            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

            final StreamInfoItemExtractor nextStream = getNextStream();
            if (nextStream != null) {
                collector.commit(nextStream);
            }

            final JsonArray results = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                    .getObject("secondaryResults").getObject("secondaryResults").getArray("results");

            final TimeAgoParser timeAgoParser = getTimeAgoParser();

            for (final Object ul : results) {
                if (((JsonObject) ul).has("compactVideoRenderer")) {
                    collector.commit(new YoutubeStreamInfoItemExtractor(((JsonObject) ul).getObject("compactVideoRenderer"), timeAgoParser));
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
            return getTextFromObject(initialAjaxJson.getObject(2).getObject("playerResponse")
                    .getObject("playabilityStatus").getObject("errorScreen")
                    .getObject("playerErrorMessageRenderer").getObject("reason"));
        } catch (final ParsingException | NullPointerException e) {
            return null; // no error message
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    private static final String FORMATS = "formats";
    private static final String ADAPTIVE_FORMATS = "adaptiveFormats";
    private static final String HTTPS = "https:";
    private static final String DEOBFUSCATION_FUNC_NAME = "deobfuscate";

    private final static String[] REGEXES = {
            "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            "([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;",
            "\\b([\\w$]{2})\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;",
            "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*c\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\("
    };

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        initialAjaxJson = getJsonResponse(getUrl() + "&pbj=1", getExtractorLocalization());

        initialData = initialAjaxJson.getObject(3).getObject("response", null);
        if (initialData == null) {
            initialData = initialAjaxJson.getObject(2).getObject("response", null);
            if (initialData == null) {
                throw new ParsingException("Could not get initial data");
            }
        }

        playerResponse = initialAjaxJson.getObject(2).getObject("playerResponse", null);
        // Save the playerResponse from the youtube.com website,
        // because there can be restrictions on the embedded player.
        // E.g. if a video is age-restricted, the embedded player's playabilityStatus says,
        // that the video cannot be played outside of YouTube,
        // but does not show the original message.
        JsonObject youtubePlayerResponse = playerResponse;

        if (playerResponse == null || !playerResponse.has("streamingData")) {
            // try to get player response by fetching video info page
            fetchVideoInfoPage();
        }

        if (playerResponse == null && youtubePlayerResponse == null) {
            throw new ExtractionException("Could not get playerResponse");
        } else if (youtubePlayerResponse == null) {
            youtubePlayerResponse = playerResponse;
        }

        JsonObject playabilityStatus = (playerResponse == null ? youtubePlayerResponse : playerResponse)
                .getObject("playabilityStatus");
        String status = playabilityStatus.getString("status");
        // If status exist, and is not "OK", throw the specific exception based on error message
        // or a ContentNotAvailableException with the reason text if it's an unknown reason.
        if (status != null && !status.equalsIgnoreCase("ok")) {
            playabilityStatus = youtubePlayerResponse.getObject("playabilityStatus");
            status = playabilityStatus.getString("status");

            final String reason = playabilityStatus.getString("reason");

            if (status.equalsIgnoreCase("login_required")) {
                if (reason == null) {
                    final String message = playabilityStatus.getArray("messages").getString(0);
                    if (message != null && message.equals("This is a private video. Please sign in to verify that you may see it.")) {
                        throw new PrivateContentException("This video is private.");
                    }
                } else if (reason.equals("Sign in to confirm your age")) {
                    // No streams can be fetched, therefore thrown an AgeRestrictedContentException explicitly.
                    throw new AgeRestrictedContentException("This age-restricted video cannot be watched.");
                }
            }
            if (status.equalsIgnoreCase("unplayable")) {
                if (reason != null) {
                    if (reason.equals("This video is only available to Music Premium members")) {
                        throw new YoutubeMusicPremiumContentException();
                    }
                    if (reason.equals("This video requires payment to watch.")) {
                        throw new PaidContentException("This video is a paid video");
                    }
                    if (reason.equals("Join this channel to get access to members-only content like this video, and other exclusive perks.") ||
                            reason.equals("Join this channel to get access to members-only content like this video and other exclusive perks.")) {
                        throw new PaidContentException("This video is only available for members of the channel of this video");
                    }
                    if (reason.equals("Video unavailable")) {
                        final String detailedErrorMessage = playabilityStatus.getObject("errorScreen")
                                .getObject("playerErrorMessageRenderer")
                                .getObject("subreason")
                                .getArray("runs")
                                .getObject(0)
                                .getString("text");
                        if (detailedErrorMessage != null) {
                            if (detailedErrorMessage.equals("The uploader has not made this video available in your country.")) {
                                throw new GeographicRestrictionException("This video is not available in user's country.");
                            }
                        }
                    }
                }
            }

            throw new ContentNotAvailableException("Got error: \"" + reason + "\"");
        }
    }

    private void fetchVideoInfoPage() throws ParsingException, ReCaptchaException, IOException {
        final String videoInfoUrl = getVideoInfoUrl(getId());
        final Response videoInfoResponse = NewPipe.getDownloader().get(videoInfoUrl, getExtractorLocalization());
        videoInfoPage.putAll(Parser.compatParseMap(videoInfoResponse.responseBody()));

        try {
            playerResponse = JsonParser.object().from(videoInfoPage.get("player_response"));
        } catch (final JsonParserException e) {
            throw new ParsingException(
                    "Could not parse YouTube player response from video info page", e);
        }
    }

    @Nonnull
    private String getEmbeddedInfoStsAndStorePlayerJsUrl() {
        try {
            final String embedUrl = "https://www.youtube.com/embed/" + getId();
            final String embedPageContent = NewPipe.getDownloader()
                    .get(embedUrl, getExtractorLocalization()).responseBody();

            try {
                final String assetsPattern = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
                playerJsUrl = Parser.matchGroup1(assetsPattern, embedPageContent)
                        .replace("\\", "").replace("\"", "");
            } catch (final Parser.RegexException ex) {
                // playerJsUrl is still available in the file, just somewhere else TODO
                // it is ok not to find it, see how that's handled in getDeobfuscationCode()
                final Document doc = Jsoup.parse(embedPageContent);
                final Elements elems = doc.select("script").attr("name", "player_ias/base");
                for (final Element elem : elems) {
                    if (elem.attr("src").contains("base.js")) {
                        playerJsUrl = elem.attr("src");
                        break;
                    }
                }
            }

            // Get embed sts
            return Parser.matchGroup1("\"sts\"\\s*:\\s*(\\d+)", embedPageContent);
        } catch (final Exception i) {
            // if it fails we simply reply with no sts as then it does not seem to be necessary
            return "";
        }
    }

    private String getDeobfuscationFuncName(final String playerCode) throws DeobfuscateException {
        Parser.RegexException exception = null;
        for (final String regex : REGEXES) {
            try {
                return Parser.matchGroup1(regex, playerCode);
            } catch (final Parser.RegexException re) {
                if (exception == null) {
                    exception = re;
                }
            }
        }
        throw new DeobfuscateException("Could not find deobfuscate function with any of the given patterns.", exception);
    }

    private String loadDeobfuscationCode(@Nonnull final String playerJsUrl)
            throws DeobfuscateException {
        try {
            final String playerCode = NewPipe.getDownloader()
                    .get(playerJsUrl, getExtractorLocalization()).responseBody();
            final String deobfuscationFunctionName = getDeobfuscationFuncName(playerCode);

            final String functionPattern = "("
                    + deobfuscationFunctionName.replace("$", "\\$")
                    + "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})";
            final String deobfuscateFunction = "var " + Parser.matchGroup1(functionPattern, playerCode) + ";";

            final String helperObjectName =
                    Parser.matchGroup1(";([A-Za-z0-9_\\$]{2})\\...\\(", deobfuscateFunction);
            final String helperPattern =
                    "(var " + helperObjectName.replace("$", "\\$") + "=\\{.+?\\}\\};)";
            final String helperObject =
                    Parser.matchGroup1(helperPattern, playerCode.replace("\n", ""));

            final String callerFunction =
                    "function " + DEOBFUSCATION_FUNC_NAME + "(a){return " + deobfuscationFunctionName + "(a);}";

            return helperObject + deobfuscateFunction + callerFunction;
        } catch (final IOException ioe) {
            throw new DeobfuscateException("Could not load deobfuscate function", ioe);
        } catch (final Exception e) {
            throw new DeobfuscateException("Could not parse deobfuscate function ", e);
        }
    }

    @Nonnull
    private String getDeobfuscationCode() throws ParsingException {
        if (cachedDeobfuscationCode == null) {
            if (playerJsUrl == null) {
                // the currentPlayerJsUrl was not found in any page fetched so far and there is
                // nothing cached, so try fetching embedded info
                getEmbeddedInfoStsAndStorePlayerJsUrl();
                if (playerJsUrl == null) {
                    throw new ParsingException(
                            "Embedded info did not provide YouTube player js url");
                }
            }

            if (playerJsUrl.startsWith("//")) {
                playerJsUrl = HTTPS + playerJsUrl;
            } else if (playerJsUrl.startsWith("/")) {
                // sometimes https://www.youtube.com part has to be added manually
                playerJsUrl = HTTPS + "//www.youtube.com" + playerJsUrl;
            }

            cachedDeobfuscationCode = loadDeobfuscationCode(playerJsUrl);
        }
        return cachedDeobfuscationCode;
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
        if (this.videoPrimaryInfoRenderer != null) return this.videoPrimaryInfoRenderer;

        JsonArray contents = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                .getObject("results").getObject("results").getArray("contents");
        JsonObject videoPrimaryInfoRenderer = null;

        for (final Object content : contents) {
            if (((JsonObject) content).has("videoPrimaryInfoRenderer")) {
                videoPrimaryInfoRenderer = ((JsonObject) content).getObject("videoPrimaryInfoRenderer");
                break;
            }
        }

        if (isNullOrEmpty(videoPrimaryInfoRenderer)) {
            throw new ParsingException("Could not find videoPrimaryInfoRenderer");
        }

        this.videoPrimaryInfoRenderer = videoPrimaryInfoRenderer;
        return videoPrimaryInfoRenderer;
    }

    private JsonObject getVideoSecondaryInfoRenderer() throws ParsingException {
        if (this.videoSecondaryInfoRenderer != null) return this.videoSecondaryInfoRenderer;

        JsonArray contents = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                .getObject("results").getObject("results").getArray("contents");
        JsonObject videoSecondaryInfoRenderer = null;

        for (final Object content : contents) {
            if (((JsonObject) content).has("videoSecondaryInfoRenderer")) {
                videoSecondaryInfoRenderer = ((JsonObject) content).getObject("videoSecondaryInfoRenderer");
                break;
            }
        }

        if (isNullOrEmpty(videoSecondaryInfoRenderer)) {
            throw new ParsingException("Could not find videoSecondaryInfoRenderer");
        }

        this.videoSecondaryInfoRenderer = videoSecondaryInfoRenderer;
        return videoSecondaryInfoRenderer;
    }

    @Nonnull
    private static String getVideoInfoUrl(final String id) {
        // TODO: Try parsing embedded_player_response first
        return "https://www.youtube.com/get_video_info?video_id=" + id +
                "&eurl=https://youtube.googleapis.com/v/" + id +
                "&html5=1&c=TVHTML5&cver=6.20180913&gl=US&hl=en";
    }

    private Map<String, ItagItem> getItags(final String streamingDataKey,
                                           final ItagItem.ItagType itagTypeWanted)
            throws ParsingException {
        final Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();
        final JsonObject streamingData = playerResponse.getObject("streamingData");
        if (!streamingData.has(streamingDataKey)) {
            return urlAndItags;
        }

        final JsonArray formats = streamingData.getArray(streamingDataKey);
        for (int i = 0; i != formats.size(); ++i) {
            JsonObject formatData = formats.getObject(i);
            int itag = formatData.getInt("itag");

            if (ItagItem.isSupported(itag)) {
                try {
                    ItagItem itagItem = ItagItem.getItag(itag);
                    if (itagItem.itagType == itagTypeWanted) {
                        // Ignore streams that are delivered using YouTube's OTF format,
                        // as those only work with DASH and not with progressive HTTP.
                        if (formatData.getString("type", EMPTY_STRING)
                                .equalsIgnoreCase("FORMAT_STREAM_TYPE_OTF")) {
                            continue;
                        }

                        String streamUrl;
                        if (formatData.has("url")) {
                            streamUrl = formatData.getString("url");
                        } else {
                            // this url has an obfuscated signature
                            final String cipherString = formatData.has("cipher")
                                    ? formatData.getString("cipher")
                                    : formatData.getString("signatureCipher");
                            final Map<String, String> cipher = Parser.compatParseMap(cipherString);
                            streamUrl = cipher.get("url") + "&" + cipher.get("sp") + "="
                                    + deobfuscateSignature(cipher.get("s"));
                        }

                        JsonObject initRange = formatData.getObject("initRange");
                        JsonObject indexRange = formatData.getObject("indexRange");
                        String mimeType = formatData.getString("mimeType", EMPTY_STRING);
                        String codec = mimeType.contains("codecs") ? mimeType.split("\"")[1] : EMPTY_STRING;

                        itagItem.setBitrate(formatData.getInt("bitrate"));
                        itagItem.setWidth(formatData.getInt("width"));
                        itagItem.setHeight(formatData.getInt("height"));
                        itagItem.setInitStart(Integer.parseInt(initRange.getString("start", "-1")));
                        itagItem.setInitEnd(Integer.parseInt(initRange.getString("end", "-1")));
                        itagItem.setIndexStart(Integer.parseInt(indexRange.getString("start", "-1")));
                        itagItem.setIndexEnd(Integer.parseInt(indexRange.getString("end", "-1")));
                        itagItem.fps = formatData.getInt("fps");
                        itagItem.setQuality(formatData.getString("quality"));
                        itagItem.setCodec(codec);

                        urlAndItags.put(streamUrl, itagItem);
                    }
                } catch (final UnsupportedEncodingException ignored) {
                }
            }
        }

        return urlAndItags;
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
                final String baseUrl = url.replace("$L", String.valueOf(i - 1)).replace("$N", parts[6]) + "&sigh=" + parts[7];
                final List<String> urls;
                if (baseUrl.contains("$M")) {
                    final int totalPages = (int) Math.ceil(totalCount / (double) (framesPerPageX * framesPerPageY));
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
            throw new ExtractionException(e);
        }
    }

    @Nonnull
    @Override
    public String getHost() {
        return "";
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        final boolean isUnlisted = playerResponse
                .getObject("microformat")
                .getObject("playerMicroformatRenderer")
                .getBoolean("isUnlisted");
        return isUnlisted ? Privacy.UNLISTED : Privacy.PUBLIC;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return playerResponse.getObject("microformat")
                .getObject("playerMicroformatRenderer")
                .getString("category", EMPTY_STRING);
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        final JsonObject metadataRowRenderer = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer").getObject("metadataRowContainerRenderer").getArray("rows")
                .getObject(0).getObject("metadataRowRenderer");

        final JsonArray contents = metadataRowRenderer.getArray("contents");
        final String license = getTextFromObject(contents.getObject(0));
        return license != null && "Licence".equals(getTextFromObject(metadataRowRenderer.getObject("title"))) ? license : "YouTube licence";
    }

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(playerResponse.getObject("videoDetails").getArray("keywords"));
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "";
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {
        final ArrayList<StreamSegment> segments = new ArrayList<>();
        if (initialData.has("engagementPanels")) {
            final JsonArray panels = initialData.getArray("engagementPanels");
            JsonArray segmentsArray = null;

            // Search for correct panel containing the data
            for (int i = 0; i < panels.size(); i++) {
                final String panelIdentifier = panels.getObject(i).getObject("engagementPanelSectionListRenderer")
                        .getString("panelIdentifier");
                if (panelIdentifier.equals("engagement-panel-macro-markers-description-chapters")
                        || panelIdentifier.equals("engagement-panel-macro-markers")) {
                    segmentsArray = panels.getObject(i).getObject("engagementPanelSectionListRenderer")
                            .getObject("content").getObject("macroMarkersListRenderer").getArray("contents");
                    break;
                }
            }

            if (segmentsArray != null) {
                final long duration = getLength();
                for (final Object object : segmentsArray) {
                    final JsonObject segmentJson = ((JsonObject) object).getObject("macroMarkersListItemRenderer");

                    final int startTimeSeconds = segmentJson.getObject("onTap").getObject("watchEndpoint")
                            .getInt("startTimeSeconds", -1);

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
                        final JsonArray previewsArray = segmentJson.getObject("thumbnail").getArray("thumbnails");
                        if (!previewsArray.isEmpty()) {
                            // Assume that the thumbnail with the highest resolution is at the last position
                            final String url = previewsArray.getObject(previewsArray.size() - 1).getString("url");
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
                initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                        .getObject("results").getObject("results").getArray("contents"));
    }

    @Nullable
    @Override
    public String getMixUrl(final MixType mixType) throws ParsingException {
        switch (mixType) {
            case VIDEO:
                return getUrl() + "&list=RD" + getId();
            case MUSIC:
                return "https://music.youtube.com/watch?v=" + getId() + "&list=RDAMVM" + getId();
            case CHANNEL:
                final String channelId;
                try {
                    channelId = YoutubeChannelLinkHandlerFactory.getInstance()
                            .getId(getUploaderUrl());
                } catch (final ParsingException e) {
                    return null; // apparently the uploader could not be parsed
                }
                return getUrl() + "&list=RDCM" + channelId;
        }
        return null;
    }
}
