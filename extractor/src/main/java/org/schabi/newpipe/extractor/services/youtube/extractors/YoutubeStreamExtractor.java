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
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.Localization;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
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
    private static final String TAG = YoutubeStreamExtractor.class.getSimpleName();

    /*//////////////////////////////////////////////////////////////////////////
    // Exceptions
    //////////////////////////////////////////////////////////////////////////*/

    public class DecryptException extends ParsingException {
        DecryptException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public class GemaException extends ContentNotAvailableException {
        GemaException(String message) {
            super(message);
        }
    }

    public class SubtitlesException extends ContentNotAvailableException {
        SubtitlesException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////*/

    private Document doc;
    @Nullable
    private JsonObject playerArgs;
    @Nonnull
    private final Map<String, String> videoInfoPage = new HashMap<>();

    @Nonnull
    private List<SubtitlesInfo> subtitlesInfos = new ArrayList<>();

    private boolean isAgeRestricted;

    public YoutubeStreamExtractor(StreamingService service, LinkHandler linkHandler, Localization localization) {
        super(service, linkHandler, localization);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Impl
    //////////////////////////////////////////////////////////////////////////*/

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        String name = getStringFromMetaData("title");
        if(name == null) {
            // Fallback to HTML method
            try {
                name = doc.select("meta[name=title]").attr(CONTENT);
            } catch (Exception e) {
                throw new ParsingException("Could not get the title", e);
            }
        }
        if(name == null || name.isEmpty()) {
            throw new ParsingException("Could not get the title");
        }
        return name;
    }

    @Nonnull
    @Override
    public String getUploadDate() throws ParsingException {
        assertPageFetched();
        try {
            return doc.select("meta[itemprop=datePublished]").attr(CONTENT);
        } catch (Exception e) {//todo: add fallback method
            throw new ParsingException("Could not get upload date", e);
        }
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        assertPageFetched();
        // Try to get high resolution thumbnail first, if it fails, use low res from the player instead
        try {
            return doc.select("link[itemprop=\"thumbnailUrl\"]").first().attr("abs:href");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            if (playerArgs != null && playerArgs.isString("thumbnail_url")) return playerArgs.getString("thumbnail_url");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            return videoInfoPage.get("thumbnail_url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Nonnull
    @Override
    public String getDescription() throws ParsingException {
        assertPageFetched();
        try {
            return parseHtmlAndGetFullLinks(doc.select("p[id=\"eow-description\"]").first().html());
        } catch (Exception e) {
            throw new ParsingException("Could not get the description", e);
        }
    }

    private String parseHtmlAndGetFullLinks(String descriptionHtml)
            throws MalformedURLException, UnsupportedEncodingException, ParsingException {
        final Document description = Jsoup.parse(descriptionHtml, getUrl());
        for(Element a : description.select("a")) {
            final URL redirectLink = new URL(
                    a.attr("abs:href"));
            final String queryString = redirectLink.getQuery();
            if(queryString != null) {
                // if the query string is null we are not dealing with a redirect link,
                // so we don't need to override it.
                final String link =
                        Parser.compatParseMap(queryString).get("q");

                if(link != null) {
                    // if link is null the a tag is a hashtag.
                    // They refer to the youtube search. We do not handle them.
                    a.text(link);
                } else if(redirectLink.toString().contains("https://www.youtube.com/")) {
                    a.text(redirectLink.toString());
                }
            } else if(redirectLink.toString().contains("https://www.youtube.com/")) {
                a.text(redirectLink.toString());
            }
        }
        return description.select("body").first().html();
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
        if(playerArgs != null) {
            try {
                long returnValue = Long.parseLong(playerArgs.get("length_seconds") + "");
                if (returnValue >= 0) return returnValue;
            } catch (Exception ignored) {
                // Try other method...
            }
        }

        String lengthString = videoInfoPage.get("length_seconds");
        try {
            return Long.parseLong(lengthString);
        } catch (Exception ignored) {
            // Try other method...
        }

        // TODO: 25.11.17 Implement a way to get the length for age restricted videos #44
        try {
            // Fallback to HTML method
            return Long.parseLong(doc.select("div[class~=\"ytp-progress-bar\"][role=\"slider\"]").first()
                    .attr("aria-valuemax"));
        } catch (Exception e) {
            throw new ParsingException("Could not get video length", e);
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
        try {
            return Long.parseLong(doc.select("meta[itemprop=interactionCount]").attr(CONTENT));
        } catch (Exception e) {//todo: find fallback method
            throw new ParsingException("Could not get number of views", e);
        }
    }

    @Override
    public long getLikeCount() throws ParsingException {
        assertPageFetched();
        String likesString = "";
        try {
            Element button = doc.select("button.like-button-renderer-like-button").first();
            try {
                likesString = button.select("span.yt-uix-button-content").first().text();
            } catch (NullPointerException e) {
                //if this kicks in our button has no content and therefore likes/dislikes are disabled
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
            Element button = doc.select("button.like-button-renderer-dislike-button").first();
            try {
                dislikesString = button.select("span.yt-uix-button-content").first().text();
            } catch (NullPointerException e) {
                //if this kicks in our button has no content and therefore likes/dislikes are disabled
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
        try {
            return doc.select("div[class=\"yt-user-info\"]").first().children()
                    .select("a").first().attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel link", e);
        }
    }


    @Nullable
    private String getStringFromMetaData(String field) {
        assertPageFetched();
        String value = null;
        if(playerArgs != null) {
            // This can not fail
            value = playerArgs.getString(field);
        }
        if(value == null) {
            // This can not fail too
            value = videoInfoPage.get(field);
        }
        return value;
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        assertPageFetched();
        String name = getStringFromMetaData("author");

        if(name == null) {
            try {
                // Fallback to HTML method
                name = doc.select("div.yt-user-info").first().text();
            } catch (Exception e) {
                throw new ParsingException("Could not get uploader name", e);
            }
        }
        if(name == null || name.isEmpty()) {
            throw new ParsingException("Could not get uploader name");
        }
        return name;
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        assertPageFetched();
        try {
            return doc.select("a[class*=\"yt-user-photo\"]").first()
                    .select("img").first()
                    .attr("abs:data-thumb");
        } catch (Exception e) {//todo: add fallback method
            throw new ParsingException("Could not get uploader thumbnail URL.", e);
        }
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
            String hlsvp = "";
            if (playerArgs != null) {
                if( playerArgs.isString("hlsvp") ) {
                    hlsvp = playerArgs.getString("hlsvp", "");
                }else {
                    hlsvp = JsonParser.object()
                            .from(playerArgs.getString("player_response", "{}"))
                            .getObject("streamingData", new JsonObject())
                            .getString("hlsManifestUrl", "");
                }
            }

            return hlsvp;
        } catch (Exception e) {
            throw new ParsingException("Could not get hls manifest url", e);
        }
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        assertPageFetched();
        List<AudioStream> audioStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FMTS, ItagItem.ItagType.AUDIO).entrySet()) {
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
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        assertPageFetched();
        List<VideoStream> videoStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(URL_ENCODED_FMT_STREAM_MAP, ItagItem.ItagType.VIDEO).entrySet()) {
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
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FMTS, ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
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
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return getSubtitles(MediaFormat.TTML);
    }

    @Override
    @Nonnull
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) throws IOException, ExtractionException {
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
            if (playerArgs != null && (playerArgs.has("ps") && playerArgs.get("ps").toString().equals("live") ||
                    playerArgs.get(URL_ENCODED_FMT_STREAM_MAP).toString().isEmpty())) {
                return StreamType.LIVE_STREAM;
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get hls manifest url", e);
        }
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        assertPageFetched();
        try {
            StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

            Elements watch = doc.select("div[class=\"watch-sidebar-section\"]");
            if (watch.size() < 1) {
                return null;// prevent the snackbar notification "report error" on age-restricted videos
            }
            
            collector.commit(extractVideoPreviewInfo(watch.first().select("li").first()));
            return collector.getItems().get(0);
        } catch (Exception e) {
            throw new ParsingException("Could not get next video", e);
        }
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        assertPageFetched();
        try {
            StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            Element ul = doc.select("ul[id=\"watch-related\"]").first();
            if (ul != null) {
                for (Element li : ul.children()) {
                    // first check if we have a playlist. If so leave them out
                    if (li.select("a[class*=\"content-link\"]").first() != null) {
                        collector.commit(extractVideoPreviewInfo(li));
                    }
                }
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
        String errorMessage = doc.select("h1[id=\"unavailable-message\"]").first().text();
        StringBuilder errorReason;

        if (errorMessage == null || errorMessage.isEmpty()) {
            errorReason = null;
        } else if (errorMessage.contains("GEMA")) {
            // Gema sometimes blocks youtube music content in germany:
            // https://www.gema.de/en/
            // Detailed description:
            // https://en.wikipedia.org/wiki/GEMA_%28German_organization%29
            errorReason = new StringBuilder("GEMA");
        } else {
            errorReason = new StringBuilder(errorMessage);
            errorReason.append("  ");
            errorReason.append(doc.select("[id=\"unavailable-submessage\"]").first().text());
        }

        return errorReason != null ? errorReason.toString() : null;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    private static final String URL_ENCODED_FMT_STREAM_MAP = "url_encoded_fmt_stream_map";
    private static final String ADAPTIVE_FMTS = "adaptive_fmts";
    private static final String HTTPS = "https:";
    private static final String CONTENT = "content";
    private static final String DECRYPTION_FUNC_NAME = "decrypt";

    private static final String VERIFIED_URL_PARAMS = "&has_verified=1&bpctr=9999999999";

    private final static String DECYRYPTION_SIGNATURE_FUNCTION_REGEX =
            "(\\w+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;";
    private final static String DECRYPTION_AKAMAIZED_STRING_REGEX =
            "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*c\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\(";
    private final static String DECRYPTION_AKAMAIZED_SHORT_STRING_REGEX =
            "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(:encodeURIComponent\\s*\\()([a-zA-Z0-9$]+)\\(";

    private volatile String decryptionCode = "";

    private String pageHtml = null;

    private String getPageHtml(Downloader downloader) throws IOException, ExtractionException {
        final String verifiedUrl = getUrl() + VERIFIED_URL_PARAMS;
        if (pageHtml == null) {
            pageHtml = downloader.download(verifiedUrl);
        }
        return pageHtml;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String pageContent = getPageHtml(downloader);
        doc = Jsoup.parse(pageContent, getUrl());

        final String playerUrl;
        // Check if the video is age restricted
        if (pageContent.contains("<meta property=\"og:restrictions:age")) {
            final EmbeddedInfo info = getEmbeddedInfo();
            final String videoInfoUrl = getVideoInfoUrl(getId(), info.sts);
            final String infoPageResponse = downloader.download(videoInfoUrl);
            videoInfoPage.putAll(Parser.compatParseMap(infoPageResponse));
            playerUrl = info.url;
            isAgeRestricted = true;
        } else {
            final JsonObject ytPlayerConfig = getPlayerConfig(pageContent);
            playerArgs = getPlayerArgs(ytPlayerConfig);
            playerUrl = getPlayerUrl(ytPlayerConfig);
            isAgeRestricted = false;
        }

        if (decryptionCode.isEmpty()) {
            decryptionCode = loadDecryptionCode(playerUrl);
        }

        if (subtitlesInfos.isEmpty()) {
            subtitlesInfos.addAll(getAvailableSubtitlesInfo());
        }
    }

    private JsonObject getPlayerConfig(String pageContent) throws ParsingException {
        try {
            String ytPlayerConfigRaw = Parser.matchGroup1("ytplayer.config\\s*=\\s*(\\{.*?\\});", pageContent);
            return JsonParser.object().from(ytPlayerConfigRaw);
        } catch (Parser.RegexException e) {
            String errorReason = getErrorMessage();
            switch (errorReason) {
                case "GEMA":
                    throw new GemaException(errorReason);
                case "":
                    throw new ContentNotAvailableException("Content not available: player config empty", e);
                default:
                    throw new ContentNotAvailableException("Content not available", e);
            }
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

    @Nonnull
    private EmbeddedInfo getEmbeddedInfo() throws ParsingException, ReCaptchaException {
        try {
            final Downloader downloader = NewPipe.getDownloader();
            final String embedUrl = "https://www.youtube.com/embed/" + getId();
            final String embedPageContent = downloader.download(embedUrl);

            // Get player url
            final String assetsPattern = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
            String playerUrl = Parser.matchGroup1(assetsPattern, embedPageContent)
                    .replace("\\", "").replace("\"", "");
            if (playerUrl.startsWith("//")) {
                playerUrl = HTTPS + playerUrl;
            }

            // Get embed sts
            final String stsPattern = "\"sts\"\\s*:\\s*(\\d+)";
            final String sts = Parser.matchGroup1(stsPattern, embedPageContent);

            return new EmbeddedInfo(playerUrl, sts);
        } catch (IOException e) {
            throw new ParsingException(
                    "Could load decryption code form restricted video for the Youtube service.", e);
        } catch (ReCaptchaException e) {
            throw new ReCaptchaException("reCaptcha Challenge requested");
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

            final String playerCode = downloader.download(playerUrl);

            final String decryptionFunctionName;
            if (Parser.isMatch(DECRYPTION_AKAMAIZED_SHORT_STRING_REGEX, playerCode)) {
                decryptionFunctionName = Parser.matchGroup1(DECRYPTION_AKAMAIZED_SHORT_STRING_REGEX, playerCode);
            } else if (Parser.isMatch(DECRYPTION_AKAMAIZED_STRING_REGEX, playerCode)) {
                decryptionFunctionName = Parser.matchGroup1(DECRYPTION_AKAMAIZED_STRING_REGEX, playerCode);
            } else {
                decryptionFunctionName = Parser.matchGroup1(DECYRYPTION_SIGNATURE_FUNCTION_REGEX, playerCode);
            }
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

    @Nonnull
    private List<SubtitlesInfo> getAvailableSubtitlesInfo() throws SubtitlesException {
        // If the video is age restricted getPlayerConfig will fail
        if(isAgeRestricted) return Collections.emptyList();

        final JsonObject playerConfig;
        try {
            playerConfig = getPlayerConfig(getPageHtml(NewPipe.getDownloader()));
        } catch (IOException | ExtractionException e) {
            throw new SubtitlesException("Unable to download player configs", e);
        }
        final String playerResponse = playerConfig.getObject("args", new JsonObject())
                .getString("player_response");

        final JsonObject captions;
        try {
            if (playerResponse == null || !JsonParser.object().from(playerResponse).has("captions")) {
                // Captions does not exist
                return Collections.emptyList();
            }
            captions = JsonParser.object().from(playerResponse).getObject("captions");
        } catch (JsonParserException e) {
            throw new SubtitlesException("Unable to parse subtitles listing", e);
        }

        final JsonObject renderer = captions.getObject("playerCaptionsTracklistRenderer", new JsonObject());
        final JsonArray captionsArray = renderer.getArray("captionTracks", new JsonArray());
        // todo: use this to apply auto translation to different language from a source language
        final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages", new JsonArray());

        // This check is necessary since there may be cases where subtitles metadata do not contain caption track info
        // e.g. https://www.youtube.com/watch?v=-Vpwatutnko
        final int captionsSize = captionsArray.size();
        if(captionsSize == 0) return Collections.emptyList();

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

    @Nonnull
    private static String getVideoInfoUrl(final String id, final String sts) {
        return "https://www.youtube.com/get_video_info?" + "video_id=" + id +
                "&eurl=https://youtube.googleapis.com/v/" + id +
                "&sts=" + sts + "&ps=default&gl=US&hl=en";
    }

    private Map<String, ItagItem> getItags(String encodedUrlMapKey, ItagItem.ItagType itagTypeWanted) throws ParsingException {
        Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();

        String encodedUrlMap = "";
        if (playerArgs != null && playerArgs.isString(encodedUrlMapKey)) {
            encodedUrlMap = playerArgs.getString(encodedUrlMapKey, "");
        } else if (videoInfoPage.containsKey(encodedUrlMapKey)) {
            encodedUrlMap = videoInfoPage.get(encodedUrlMapKey);
        }

        for (String url_data_str : encodedUrlMap.split(",")) {
            try {
                // This loop iterates through multiple streams, therefore tags
                // is related to one and the same stream at a time.
                Map<String, String> tags = Parser.compatParseMap(
                        org.jsoup.parser.Parser.unescapeEntities(url_data_str, true));

                int itag = Integer.parseInt(tags.get("itag"));

                if (ItagItem.isSupported(itag)) {
                    ItagItem itagItem = ItagItem.getItag(itag);
                    if (itagItem.itagType == itagTypeWanted) {
                        String streamUrl = tags.get("url");
                        // if video has a signature: decrypt it and add it to the url
                        if (tags.get("s") != null) {
                            streamUrl = streamUrl + "&signature=" + decryptSignature(tags.get("s"), decryptionCode);
                        }
                        urlAndItags.put(streamUrl, itagItem);
                    }
                }
            } catch (DecryptException e) {
                throw e;
            } catch (Exception ignored) {
            }
        }

        return urlAndItags;
    }

    /**
     * Provides information about links to other videos on the video page, such as related videos.
     * This is encapsulated in a StreamInfoItem object, which is a subset of the fields in a full StreamInfo.
     */
    private StreamInfoItemExtractor extractVideoPreviewInfo(final Element li) {
        return new YoutubeStreamInfoItemExtractor(li) {

            @Override
            public String getUrl() throws ParsingException {
                return li.select("a.content-link").first().attr("abs:href");
            }

            @Override
            public String getName() throws ParsingException {
                //todo: check NullPointerException causing
                return li.select("span.title").first().text();
                //this page causes the NullPointerException, after finding it by searching for "tjvg":
                //https://www.youtube.com/watch?v=Uqg0aEhLFAg
            }

            @Override
            public String getUploaderName() throws ParsingException {
                return li.select("span[class*=\"attribution\"").first()
                        .select("span").first().text();
            }

            @Override
            public String getUploaderUrl() throws ParsingException {
                return ""; // The uploader is not linked
            }

            @Override
            public String getUploadDate() throws ParsingException {
                return "";
            }

            @Override
            public long getViewCount() throws ParsingException {
                try {
                    if (getStreamType() == StreamType.LIVE_STREAM) return -1;

                    return Long.parseLong(Utils.removeNonDigitCharacters(
                            li.select("span.view-count").first().text()));
                } catch (Exception e) {
                    //related videos sometimes have no view count
                    return 0;
                }
            }

            @Override
            public String getThumbnailUrl() throws ParsingException {
                Element img = li.select("img").first();
                String thumbnailUrl = img.attr("abs:src");
                // Sometimes youtube sends links to gif files which somehow seem to not exist
                // anymore. Items with such gif also offer a secondary image source. So we are going
                // to use that if we caught such an item.
                if (thumbnailUrl.contains(".gif")) {
                    thumbnailUrl = img.attr("data-thumb");
                }
                if (thumbnailUrl.startsWith("//")) {
                    thumbnailUrl = HTTPS + thumbnailUrl;
                }
                return thumbnailUrl;
            }
        };
    }
}
