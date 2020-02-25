package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public class DecryptException extends ParsingException {
        DecryptException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////*/

    private Document doc;
    @Nullable
    private JsonObject playerArgs;
    @Nonnull
    private final Map<String, String> videoInfoPage = new HashMap<>();
    private JsonObject playerResponse;
    private JsonObject initialData;

    @Nonnull
    private List<SubtitlesInfo> subtitlesInfos = new ArrayList<>();

    private boolean isAgeRestricted;

    public YoutubeStreamExtractor(StreamingService service, LinkHandler linkHandler) {
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
            title = getVideoPrimaryInfoRenderer().getObject("title").getArray("runs").getObject(0).getString("text");
        } catch (Exception ignored) {}
        if (title == null) {
            try {
                title = playerResponse.getObject("videoDetails").getString("title");
            } catch (Exception ignored) {}
        }
        if (title != null) return title;
        throw new ParsingException("Could not get name");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        try {
            //return playerResponse.getObject("microformat").getObject("playerMicroformatRenderer").getString("publishDate");
        } catch (Exception ignored) {}

        try {
            // TODO this parses English formatted dates only, we need a better approach to parse the textual date
            Date d = new SimpleDateFormat("dd MMM yyy").parse(getVideoPrimaryInfoRenderer()
                    .getObject("dateText").getString("simpleText"));
            return new SimpleDateFormat("yyyy-MM-dd").format(d);
        } catch (Exception ignored) {}
        throw new ParsingException("Could not get upload date");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();

        if (textualUploadDate == null) {
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
            return thumbnails.getObject(thumbnails.size() - 1).getString("url");

        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url");
        }

    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        assertPageFetched();
        // raw non-html description
        try {
            return new Description(playerResponse.getObject("videoDetails").getString("shortDescription"), Description.PLAIN_TEXT);
        } catch (Exception ignored) { }
        try {
            JsonArray descriptions = getVideoSecondaryInfoRenderer().getObject("description").getArray("runs");
            StringBuilder descriptionBuilder = new StringBuilder(descriptions.size());
            for (Object textObjectHolder : descriptions) {
                JsonObject textHolder = (JsonObject) textObjectHolder;
                String text = textHolder.getString("text");
                if (text != null) descriptionBuilder.append(text);
            }
            String description = descriptionBuilder.toString();
            if (!description.isEmpty()) return new Description(description, Description.PLAIN_TEXT);
        } catch (Exception ignored) { }
        throw new ParsingException("Could not get description");
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        assertPageFetched();
        if (!isAgeRestricted) {
            return NO_AGE_LIMIT;
        }
        try {
            return Integer.valueOf(doc.select("meta[property=\"og:restrictions:age\"]")
                    .attr(CONTENT).replace("+", ""));
        } catch (Exception e) {
            throw new ParsingException("Could not get age restriction");
        }
    }

    @Override
    public long getLength() throws ParsingException {
        assertPageFetched();

        try {
            String duration = playerResponse
                    .getObject("videoDetails")
                    .getString("lengthSeconds");
            return Long.parseLong(duration);
        } catch (Exception e) {
            try {
                String durationMs = playerResponse
                        .getObject("streamingData")
                        .getArray("formats")
                        .getObject(0)
                        .getString("approxDurationMs");
                return Math.round(Long.parseLong(durationMs) / 1000f);
            } catch (Exception ignored) {
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
        return getTimestampSeconds("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
    }

    @Override
    public long getViewCount() throws ParsingException {
        assertPageFetched();
        String views = null;
        try {
            views = getVideoPrimaryInfoRenderer().getObject("viewCount")
                    .getObject("videoViewCountRenderer").getObject("viewCount")
                    .getArray("runs").getObject(0).getString("text");
        } catch (Exception ignored) {}
        if (views == null) {
            try {
                views = getVideoPrimaryInfoRenderer().getObject("viewCount")
                        .getObject("videoViewCountRenderer").getObject("viewCount").getString("simpleText");
            } catch (Exception ignored) {}
        }
        if (views == null) {
            try {
                views = playerResponse.getObject("videoDetails").getString("viewCount");
            } catch (Exception ignored) {}
        }
        if (views != null) return Long.parseLong(Utils.removeNonDigitCharacters(views));
        throw new ParsingException("Could not get view count");
    }

    @Override
    public long getLikeCount() throws ParsingException {
        assertPageFetched();
        String likesString = "";
        try {
            try {
                likesString = getVideoPrimaryInfoRenderer().getObject("sentimentBar")
                        .getObject("sentimentBarRenderer").getString("tooltip").split("/")[0];
            } catch (NullPointerException e) {
                //if this kicks in our button has no content and therefore ratings must be disabled
                if (playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
                    throw new ParsingException("Ratings are enabled even though the like button is missing", e);
                }
                return -1;
            }
            return Integer.parseInt(Utils.removeNonDigitCharacters(likesString));
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + likesString + "\" as an Integer", nfe);
        } catch (Exception e) {
            throw new ParsingException("Could not get like count", e);
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
            } catch (NullPointerException e) {
                //if this kicks in our button has no content and therefore ratings must be disabled
                if (playerResponse.getObject("videoDetails").getBoolean("allowRatings")) {
                    throw new ParsingException("Ratings are enabled even though the dislike button is missing", e);
                }
                return -1;
            }
            return Integer.parseInt(Utils.removeNonDigitCharacters(dislikesString));
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Could not parse \"" + dislikesString + "\" as an Integer", nfe);
        } catch (Exception e) {
            throw new ParsingException("Could not get dislike count", e);
        }
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        assertPageFetched();
        String uploaderId = null;
        try {
            uploaderId = getVideoSecondaryInfoRenderer().getObject("owner").getObject("videoOwnerRenderer")
                    .getObject("navigationEndpoint").getObject("browseEndpoint").getString("browseId");
        } catch (Exception ignored) {}
        if (uploaderId == null) {
            try {
                uploaderId = playerResponse.getObject("videoDetails").getString("channelId");
            } catch (Exception ignored) {}
        }
        if (uploaderId != null) return "https://www.youtube.com/channel/" + uploaderId;
        throw new ParsingException("Could not get uploader url");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        assertPageFetched();
        String uploaderName = null;
        try {
            uploaderName = getVideoSecondaryInfoRenderer().getObject("owner").getObject("videoOwnerRenderer")
                    .getObject("title").getArray("runs").getObject(0).getString("text");
        } catch (Exception ignored) {}
        if (uploaderName == null) {
            try {
                uploaderName = playerResponse.getObject("videoDetails").getString("author");
            } catch (Exception ignored) {}
        }
        if (uploaderName != null) return uploaderName;
        throw new ParsingException("Could not get uploader name");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        assertPageFetched();

        String uploaderAvatarUrl = null;
        try {
            uploaderAvatarUrl = initialData.getObject("contents").getObject("twoColumnWatchNextResults").getObject("secondaryResults")
                    .getObject("secondaryResults").getArray("results").getObject(0).getObject("compactAutoplayRenderer")
                    .getArray("contents").getObject(0).getObject("compactVideoRenderer").getObject("channelThumbnail")
                    .getArray("thumbnails").getObject(0).getString("url");
            if (uploaderAvatarUrl != null && !uploaderAvatarUrl.isEmpty()) {
                return uploaderAvatarUrl;
            }
        } catch (Exception ignored) {}

        try {
            uploaderAvatarUrl = getVideoSecondaryInfoRenderer().getObject("owner").getObject("videoOwnerRenderer")
                    .getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");
        } catch (Exception ignored) {}

        if (uploaderAvatarUrl == null) {
            throw new ParsingException("Could not get uploader avatar url");
        }
        return uploaderAvatarUrl;
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        assertPageFetched();
        try {
            String dashManifestUrl;
            if (videoInfoPage.containsKey("dashmpd")) {
                dashManifestUrl = videoInfoPage.get("dashmpd");
            } else if (playerArgs != null && playerArgs.isString("dashmpd")) {
                dashManifestUrl = playerArgs.getString("dashmpd", "");
            } else {
                return "";
            }

            if (!dashManifestUrl.contains("/signature/")) {
                String encryptedSig = Parser.matchGroup1("/s/([a-fA-F0-9\\.]+)", dashManifestUrl);
                String decryptedSig;

                decryptedSig = decryptSignature(encryptedSig, decryptionCode);
                dashManifestUrl = dashManifestUrl.replace("/s/" + encryptedSig, "/signature/" + decryptedSig);
            }

            return dashManifestUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get dash manifest url", e);
        }
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();

        try {
            return playerResponse.getObject("streamingData").getString("hlsManifestUrl");
        } catch (Exception e) {
            if (playerArgs != null && playerArgs.isString("hlsvp")) {
                return playerArgs.getString("hlsvp");
            } else {
                throw new ParsingException("Could not get hls manifest url", e);
            }
        }
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        assertPageFetched();
        List<AudioStream> audioStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.AUDIO).entrySet()) {
                ItagItem itag = entry.getValue();

                AudioStream audioStream = new AudioStream(entry.getKey(), itag.getMediaFormat(), itag.avgBitrate);
                if (!Stream.containSimilarStream(audioStream, audioStreams)) {
                    audioStreams.add(audioStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get audio streams", e);
        }

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();
        List<VideoStream> videoStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(FORMATS, ItagItem.ItagType.VIDEO).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.getMediaFormat(), itag.resolutionString);
                if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                    videoStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }

        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        assertPageFetched();
        List<VideoStream> videoOnlyStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FORMATS, ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.getMediaFormat(), itag.resolutionString, true);
                if (!Stream.containSimilarStream(videoStream, videoOnlyStreams)) {
                    videoOnlyStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return videoOnlyStreams;
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitlesDefault() {
        return getSubtitles(MediaFormat.TTML);
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) {
        assertPageFetched();
        List<SubtitlesStream> subtitles = new ArrayList<>();
        for (final SubtitlesInfo subtitlesInfo : subtitlesInfos) {
            subtitles.add(subtitlesInfo.getSubtitle(format));
        }
        return subtitles;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        assertPageFetched();
        try {
            if (!playerResponse.getObject("streamingData").has(FORMATS) ||
                    (playerArgs != null && playerArgs.has("ps") && playerArgs.get("ps").toString().equals("live"))) {
                return StreamType.LIVE_STREAM;
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get stream type", e);
        }
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws ExtractionException {
        assertPageFetched();
        if (isAgeRestricted) {
            return null;
        }
        try {
            final JsonObject videoInfo = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                    .getObject("secondaryResults").getObject("secondaryResults").getArray("results")
                    .getObject(0).getObject("compactAutoplayRenderer").getArray("contents")
                    .getObject(0).getObject("compactVideoRenderer");
            final TimeAgoParser timeAgoParser = getTimeAgoParser();
            StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

            collector.commit(new YoutubeStreamInfoItemExtractor(videoInfo, timeAgoParser));
            return collector.getItems().get(0);
        } catch (Exception e) {
            throw new ParsingException("Could not get next video", e);
        }
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws ExtractionException {
        assertPageFetched();
        if (isAgeRestricted) {
            return null;
        }
        try {
            StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            JsonArray results = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                    .getObject("secondaryResults").getObject("secondaryResults").getArray("results");

            final TimeAgoParser timeAgoParser = getTimeAgoParser();

            for (Object ul : results) {
                final JsonObject videoInfo = ((JsonObject) ul).getObject("compactVideoRenderer");

                if (videoInfo != null) collector.commit(new YoutubeStreamInfoItemExtractor(videoInfo, timeAgoParser));
            }
            return collector;
        } catch (Exception e) {
            throw new ParsingException("Could not get related videos", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        StringBuilder errorReason;
        Element errorElement = doc.select("h1[id=\"unavailable-message\"]").first();

        if (errorElement == null) {
            errorReason = null;
        } else {
            String errorMessage = errorElement.text();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorReason = null;
            } else {
                errorReason = new StringBuilder(errorMessage);
                errorReason.append("  ");
                errorReason.append(doc.select("[id=\"unavailable-submessage\"]").first().text());
            }
        }

        return errorReason != null ? errorReason.toString() : "";
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    private static final String FORMATS = "formats";
    private static final String ADAPTIVE_FORMATS = "adaptiveFormats";
    private static final String HTTPS = "https:";
    private static final String CONTENT = "content";
    private static final String DECRYPTION_FUNC_NAME = "decrypt";

    private static final String VERIFIED_URL_PARAMS = "&has_verified=1&bpctr=9999999999";

    private final static String DECRYPTION_SIGNATURE_FUNCTION_REGEX =
            "([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;";
    private final static String DECRYPTION_SIGNATURE_FUNCTION_REGEX_2 =
            "\\b([\\w$]{2})\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;";
    private final static String DECRYPTION_AKAMAIZED_STRING_REGEX =
            "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*c\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\(";
    private final static String DECRYPTION_AKAMAIZED_SHORT_STRING_REGEX =
            "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\(";

    private volatile String decryptionCode = "";

    private String pageHtml = null;

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String verifiedUrl = getUrl() + VERIFIED_URL_PARAMS;
        final Response response = downloader.get(verifiedUrl, getExtractorLocalization());
        pageHtml = response.responseBody();
        doc = YoutubeParsingHelper.parseAndCheckPage(verifiedUrl, response);

        final String playerUrl;
        // Check if the video is age restricted
        if (!doc.select("meta[property=\"og:restrictions:age\"]").isEmpty()) {
            final EmbeddedInfo info = getEmbeddedInfo();
            final String videoInfoUrl = getVideoInfoUrl(getId(), info.sts);
            final String infoPageResponse = downloader.get(videoInfoUrl, getExtractorLocalization()).responseBody();
            videoInfoPage.putAll(Parser.compatParseMap(infoPageResponse));
            playerUrl = info.url;
            isAgeRestricted = true;
        } else {
            final JsonObject ytPlayerConfig = getPlayerConfig();
            playerArgs = getPlayerArgs(ytPlayerConfig);
            playerUrl = getPlayerUrl(ytPlayerConfig);
            isAgeRestricted = false;
        }
        playerResponse = getPlayerResponse();
        initialData = YoutubeParsingHelper.getInitialData(pageHtml);

        if (decryptionCode.isEmpty()) {
            decryptionCode = loadDecryptionCode(playerUrl);
        }

        if (subtitlesInfos.isEmpty()) {
            subtitlesInfos.addAll(getAvailableSubtitlesInfo());
        }
    }

    private JsonObject getPlayerConfig() throws ParsingException {
        try {
            String ytPlayerConfigRaw = Parser.matchGroup1("ytplayer.config\\s*=\\s*(\\{.*?\\});", pageHtml);
            return JsonParser.object().from(ytPlayerConfigRaw);
        } catch (Parser.RegexException e) {
            String errorReason = getErrorMessage();
            if (errorReason.isEmpty()) {
                throw new ContentNotAvailableException("Content not available: player config empty", e);
            }
            throw new ContentNotAvailableException("Content not available", e);
        } catch (Exception e) {
            throw new ParsingException("Could not parse yt player config", e);
        }
    }

    private JsonObject getPlayerArgs(JsonObject playerConfig) throws ParsingException {
        JsonObject playerArgs;

        //attempt to load the youtube js player JSON arguments
        try {
            playerArgs = playerConfig.getObject("args");
        } catch (Exception e) {
            throw new ParsingException("Could not parse yt player config", e);
        }

        return playerArgs;
    }

    private String getPlayerUrl(JsonObject playerConfig) throws ParsingException {
        try {
            // The Youtube service needs to be initialized by downloading the
            // js-Youtube-player. This is done in order to get the algorithm
            // for decrypting cryptic signatures inside certain stream urls.
            String playerUrl;

            JsonObject ytAssets = playerConfig.getObject("assets");
            playerUrl = ytAssets.getString("js");

            if (playerUrl.startsWith("//")) {
                playerUrl = HTTPS + playerUrl;
            }
            return playerUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not load decryption code for the Youtube service.", e);
        }
    }

    private JsonObject getPlayerResponse() throws ParsingException {
        try {
            String playerResponseStr;
            if (playerArgs != null) {
                playerResponseStr = playerArgs.getString("player_response");
            } else {
                playerResponseStr = videoInfoPage.get("player_response");
            }
            return JsonParser.object().from(playerResponseStr);
        } catch (Exception e) {
            throw new ParsingException("Could not parse yt player response", e);
        }
    }

    @Nonnull
    private EmbeddedInfo getEmbeddedInfo() throws ParsingException, ReCaptchaException {
        try {
            final Downloader downloader = NewPipe.getDownloader();
            final String embedUrl = "https://www.youtube.com/embed/" + getId();
            final String embedPageContent = downloader.get(embedUrl, getExtractorLocalization()).responseBody();

            // Get player url
            final String assetsPattern = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
            String playerUrl = Parser.matchGroup1(assetsPattern, embedPageContent)
                    .replace("\\", "").replace("\"", "");
            if (playerUrl.startsWith("//")) {
                playerUrl = HTTPS + playerUrl;
            }

            try {
                // Get embed sts
                final String stsPattern = "\"sts\"\\s*:\\s*(\\d+)";
                final String sts = Parser.matchGroup1(stsPattern, embedPageContent);
                return new EmbeddedInfo(playerUrl, sts);
            } catch (Exception i) {
                // if it fails we simply reply with no sts as then it does not seem to be necessary
                return new EmbeddedInfo(playerUrl, "");
            }

        } catch (IOException e) {
            throw new ParsingException(
                    "Could load decryption code form restricted video for the Youtube service.", e);
        }
    }

    private String loadDecryptionCode(String playerUrl) throws DecryptException {
        try {
            Downloader downloader = NewPipe.getDownloader();
            if (!playerUrl.contains("https://youtube.com")) {
                //sometimes the https://youtube.com part does not get send with
                //than we have to add it by hand
                playerUrl = "https://youtube.com" + playerUrl;
            }

            final String playerCode = downloader.get(playerUrl, getExtractorLocalization()).responseBody();
            final String decryptionFunctionName = getDecryptionFuncName(playerCode);

            final String functionPattern = "("
                    + decryptionFunctionName.replace("$", "\\$")
                    + "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})";
            final String decryptionFunction = "var " + Parser.matchGroup1(functionPattern, playerCode) + ";";

            final String helperObjectName =
                    Parser.matchGroup1(";([A-Za-z0-9_\\$]{2})\\...\\(", decryptionFunction);
            final String helperPattern =
                    "(var " + helperObjectName.replace("$", "\\$") + "=\\{.+?\\}\\};)";
            final String helperObject =
                    Parser.matchGroup1(helperPattern, playerCode.replace("\n", ""));

            final String callerFunction =
                    "function " + DECRYPTION_FUNC_NAME + "(a){return " + decryptionFunctionName + "(a);}";

            return helperObject + decryptionFunction + callerFunction;
        } catch (IOException ioe) {
            throw new DecryptException("Could not load decrypt function", ioe);
        } catch (Exception e) {
            throw new DecryptException("Could not parse decrypt function ", e);
        }
    }

    private String decryptSignature(String encryptedSig, String decryptionCode) throws DecryptException {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        Object result;
        try {
            ScriptableObject scope = context.initStandardObjects();
            context.evaluateString(scope, decryptionCode, "decryptionCode", 1, null);
            Function decryptionFunc = (Function) scope.get("decrypt", scope);
            result = decryptionFunc.call(context, scope, scope, new Object[]{encryptedSig});
        } catch (Exception e) {
            throw new DecryptException("could not get decrypt signature", e);
        } finally {
            Context.exit();
        }
        return result == null ? "" : result.toString();
    }

    private String getDecryptionFuncName(String playerCode) throws DecryptException {
        String[] decryptionFuncNameRegexes = {
                DECRYPTION_SIGNATURE_FUNCTION_REGEX_2,
                DECRYPTION_SIGNATURE_FUNCTION_REGEX,
                DECRYPTION_AKAMAIZED_SHORT_STRING_REGEX,
                DECRYPTION_AKAMAIZED_STRING_REGEX
        };
        Parser.RegexException exception = null;
        for (String regex : decryptionFuncNameRegexes) {
            try {
                return Parser.matchGroup1(regex, playerCode);
            } catch (Parser.RegexException re) {
                if (exception == null)
                    exception = re;
            }
        }
        throw new DecryptException("Could not find decrypt function with any of the given patterns.", exception);
    }

    @Nonnull
    private List<SubtitlesInfo> getAvailableSubtitlesInfo() {
        // If the video is age restricted getPlayerConfig will fail
        if (isAgeRestricted) return Collections.emptyList();

        final JsonObject captions;
        if (!playerResponse.has("captions")) {
            // Captions does not exist
            return Collections.emptyList();
        }
        captions = playerResponse.getObject("captions");

        final JsonObject renderer = captions.getObject("playerCaptionsTracklistRenderer", new JsonObject());
        final JsonArray captionsArray = renderer.getArray("captionTracks", new JsonArray());
        // todo: use this to apply auto translation to different language from a source language
//        final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages", new JsonArray());

        // This check is necessary since there may be cases where subtitles metadata do not contain caption track info
        // e.g. https://www.youtube.com/watch?v=-Vpwatutnko
        final int captionsSize = captionsArray.size();
        if (captionsSize == 0) return Collections.emptyList();

        List<SubtitlesInfo> result = new ArrayList<>();
        for (int i = 0; i < captionsSize; i++) {
            final String languageCode = captionsArray.getObject(i).getString("languageCode");
            final String baseUrl = captionsArray.getObject(i).getString("baseUrl");
            final String vssId = captionsArray.getObject(i).getString("vssId");

            if (languageCode != null && baseUrl != null && vssId != null) {
                final boolean isAutoGenerated = vssId.startsWith("a.");
                result.add(new SubtitlesInfo(baseUrl, languageCode, isAutoGenerated));
            }
        }

        return result;
    }
    /*//////////////////////////////////////////////////////////////////////////
    // Data Class
    //////////////////////////////////////////////////////////////////////////*/

    private class EmbeddedInfo {
        final String url;
        final String sts;

        EmbeddedInfo(final String url, final String sts) {
            this.url = url;
            this.sts = sts;
        }
    }

    private class SubtitlesInfo {
        final String cleanUrl;
        final String languageCode;
        final boolean isGenerated;

        public SubtitlesInfo(final String baseUrl, final String languageCode, final boolean isGenerated) {
            this.cleanUrl = baseUrl
                    .replaceAll("&fmt=[^&]*", "") // Remove preexisting format if exists
                    .replaceAll("&tlang=[^&]*", ""); // Remove translation language
            this.languageCode = languageCode;
            this.isGenerated = isGenerated;
        }

        public SubtitlesStream getSubtitle(final MediaFormat format) {
            return new SubtitlesStream(format, languageCode, cleanUrl + "&fmt=" + format.getSuffix(), isGenerated);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private JsonObject getVideoPrimaryInfoRenderer() throws ParsingException {
        JsonArray contents = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                .getObject("results").getObject("results").getArray("contents");
        JsonObject videoPrimaryInfoRenderer = null;

        for (Object content : contents) {
            if (((JsonObject) content).getObject("videoPrimaryInfoRenderer") != null) {
                videoPrimaryInfoRenderer = ((JsonObject) content).getObject("videoPrimaryInfoRenderer");
                break;
            }
        }

        if (videoPrimaryInfoRenderer == null) {
            throw new ParsingException("Could not find videoPrimaryInfoRenderer");
        }

        return videoPrimaryInfoRenderer;
    }

    private JsonObject getVideoSecondaryInfoRenderer() throws ParsingException {
        JsonArray contents = initialData.getObject("contents").getObject("twoColumnWatchNextResults")
                .getObject("results").getObject("results").getArray("contents");
        JsonObject videoSecondaryInfoRenderer = null;

        for (Object content : contents) {
            if (((JsonObject) content).getObject("videoSecondaryInfoRenderer") != null) {
                videoSecondaryInfoRenderer = ((JsonObject) content).getObject("videoSecondaryInfoRenderer");
                break;
            }
        }

        if (videoSecondaryInfoRenderer == null) {
            throw new ParsingException("Could not find videoSecondaryInfoRenderer");
        }

        return videoSecondaryInfoRenderer;
    }

    @Nonnull
    private static String getVideoInfoUrl(final String id, final String sts) {
        return "https://www.youtube.com/get_video_info?" + "video_id=" + id +
                "&eurl=https://youtube.googleapis.com/v/" + id +
                "&sts=" + sts + "&ps=default&gl=US&hl=en";
    }

    private Map<String, ItagItem> getItags(String streamingDataKey, ItagItem.ItagType itagTypeWanted) throws ParsingException {
        Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();
        JsonObject streamingData = playerResponse.getObject("streamingData");
        if (!streamingData.has(streamingDataKey)) {
            return urlAndItags;
        }

        JsonArray formats = streamingData.getArray(streamingDataKey);
        for (int i = 0; i != formats.size(); ++i) {
            JsonObject formatData = formats.getObject(i);
            int itag = formatData.getInt("itag");

            if (ItagItem.isSupported(itag)) {
                try {
                    ItagItem itagItem = ItagItem.getItag(itag);
                    if (itagItem.itagType == itagTypeWanted) {
                        String streamUrl;
                        if (formatData.has("url")) {
                            streamUrl = formatData.getString("url");
                        } else {
                            // this url has an encrypted signature
                            Map<String, String> cipher = Parser.compatParseMap(formatData.getString("cipher"));
                            streamUrl = cipher.get("url") + "&" + cipher.get("sp") + "=" + decryptSignature(cipher.get("s"), decryptionCode);
                        }

                        urlAndItags.put(streamUrl, itagItem);
                    }
                } catch (UnsupportedEncodingException ignored) {

                }
            }
        }

        return urlAndItags;
    }

    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        try {
            final String script = doc.select("#player-api").first().siblingElements().select("script").html();
            int p = script.indexOf("ytplayer.config");
            if (p == -1) {
                return Collections.emptyList();
            }
            p = script.indexOf('{', p);
            int e = script.indexOf("ytplayer.load", p);
            if (e == -1) {
                return Collections.emptyList();
            }
            JsonObject jo = JsonParser.object().from(script.substring(p, e - 1));
            final String resp = jo.getObject("args").getString("player_response");
            jo = JsonParser.object().from(resp);
            final String[] spec = jo.getObject("storyboards").getObject("playerStoryboardSpecRenderer").getString("spec").split("\\|");
            final String url = spec[0];
            final ArrayList<Frameset> result = new ArrayList<>(spec.length - 1);
            for (int i = 1; i < spec.length; ++i) {
                final String[] parts = spec[i].split("#");
                if (parts.length != 8) {
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
                        framesPerPageX,
                        framesPerPageY
                ));
            }
            result.trimToSize();
            return result;
        } catch (Exception e) {
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
    public String getPrivacy() {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() {
        return "";
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "";
    }
}
