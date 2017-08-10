package org.schabi.newpipe.extractor.services.youtube;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
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

    public class LiveStreamException extends ContentNotAvailableException {
        LiveStreamException(String message) {
            super(message);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////*/

    private Document doc;
    private JSONObject playerArgs;
    private Map<String, String> videoInfoPage;

    private boolean isAgeRestricted;

    public YoutubeStreamExtractor(StreamingService service, String url) throws IOException, ExtractionException {
        super(service, url);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Impl
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public String getId() throws ParsingException {
        try {
            return getUrlIdHandler().getId(getCleanUrl());
        } catch (Exception e) {
            throw new ParsingException("Could not get stream id");
        }
    }

    @Override
    public String getTitle() throws ParsingException {
        try {
            if (playerArgs == null) {
                return videoInfoPage.get("title");
            }
            //json player args method
            return playerArgs.getString("title");
        } catch (Exception je) {
            System.err.println("failed to load title from JSON args; trying to extract it from HTML");
            try { // fall-back to html
                return doc.select("meta[name=title]").attr(CONTENT);
            } catch (Exception e) {
                throw new ParsingException("Could not get the title", e);
            }
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return doc.select("p[id=\"eow-description\"]").first().html();
        } catch (Exception e) {//todo: add fallback method <-- there is no ... as long as i know
            throw new ParsingException("Could not get the description", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return playerArgs.getString("author");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            return videoInfoPage.get("author");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            // Fallback to HTML method
            return doc.select("div.yt-user-info").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader name", e);
        }
    }

    @Override
    public long getLength() throws ParsingException {
        try {
            return playerArgs.getLong("length_seconds");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            return Long.parseLong(videoInfoPage.get("length_seconds"));
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            // Fallback to HTML method
            return Long.parseLong(doc.select("div[class~=\"ytp-progress-bar\"][role=\"slider\"]")
                    .first().attr("aria-valuemax"));
        } catch (Exception e) {
            throw new ParsingException("Could not get video length", e);
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            return Long.parseLong(doc.select("meta[itemprop=interactionCount]").attr(CONTENT));
        } catch (Exception e) {//todo: find fallback method
            throw new ParsingException("Could not get number of views", e);
        }
    }

    @Override
    public String getUploadDate() throws ParsingException {
        try {
            return doc.select("meta[itemprop=datePublished]").attr(CONTENT);
        } catch (Exception e) {//todo: add fallback method
            throw new ParsingException("Could not get upload date", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        // Try to get high resolution thumbnail first, if it fails, use low res from the player instead
        try {
            return doc.select("link[itemprop=\"thumbnailUrl\"]").first().attr("abs:href");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            return playerArgs.getString("thumbnail_url");
        } catch (Exception ignored) {
            // Try other method...
        }

        try {
            return videoInfoPage.get("thumbnail_url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            return doc.select("a[class*=\"yt-user-photo\"]").first()
                    .select("img").first()
                    .attr("abs:data-thumb");
        } catch (Exception e) {//todo: add fallback method
            throw new ParsingException("Could not get uploader thumbnail URL.", e);
        }
    }

    @Override
    public String getDashMpdUrl() throws ParsingException {
        try {
            String dashManifestUrl;
            if (videoInfoPage != null && videoInfoPage.containsKey("dashmpd")) {
                dashManifestUrl = videoInfoPage.get("dashmpd");
            } else if (playerArgs.get("dashmpd") != null) {
                dashManifestUrl = playerArgs.optString("dashmpd");
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

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        List<AudioStream> audioStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FMTS, ItagItem.ItagType.AUDIO).entrySet()) {
                ItagItem itag = entry.getValue();

                AudioStream audioStream = new AudioStream(entry.getKey(), itag.mediaFormatId, itag.avgBitrate);
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
        List<VideoStream> videoStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(URL_ENCODED_FMT_STREAM_MAP, ItagItem.ItagType.VIDEO).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.mediaFormatId, itag.resolutionString);
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
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        List<VideoStream> videoOnlyStreams = new ArrayList<>();
        try {
            for (Map.Entry<String, ItagItem> entry : getItags(ADAPTIVE_FMTS, ItagItem.ItagType.VIDEO_ONLY).entrySet()) {
                ItagItem itag = entry.getValue();

                VideoStream videoStream = new VideoStream(entry.getKey(), itag.mediaFormatId, itag.resolutionString, true);
                if (!Stream.containSimilarStream(videoStream, videoOnlyStreams)) {
                    videoOnlyStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return videoOnlyStreams;
    }

    /**
     * Attempts to parse (and return) the offset to start playing the video from.
     *
     * @return the offset (in seconds), or 0 if no timestamp is found.
     */
    @Override
    public int getTimeStamp() throws ParsingException {
        String timeStamp;
        try {
            timeStamp = Parser.matchGroup1("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)", getOriginalUrl());
        } catch (Parser.RegexException e) {
            // catch this instantly since an url does not necessarily have to have a time stamp

            // -2 because well the testing system will then know its the regex that failed :/
            // not good i know
            return -2;
        }

        if (!timeStamp.isEmpty()) {
            try {
                String secondsString = "";
                String minutesString = "";
                String hoursString = "";
                try {
                    secondsString = Parser.matchGroup1("(\\d{1,3})s", timeStamp);
                    minutesString = Parser.matchGroup1("(\\d{1,3})m", timeStamp);
                    hoursString = Parser.matchGroup1("(\\d{1,3})h", timeStamp);
                } catch (Exception e) {
                    //it could be that time is given in another method
                    if (secondsString.isEmpty() //if nothing was got,
                            && minutesString.isEmpty()//treat as unlabelled seconds
                            && hoursString.isEmpty()) {
                        secondsString = Parser.matchGroup1("t=(\\d+)", timeStamp);
                    }
                }

                int seconds = secondsString.isEmpty() ? 0 : Integer.parseInt(secondsString);
                int minutes = minutesString.isEmpty() ? 0 : Integer.parseInt(minutesString);
                int hours = hoursString.isEmpty() ? 0 : Integer.parseInt(hoursString);

                //don't trust BODMAS!
                return seconds + (60 * minutes) + (3600 * hours);
                //Log.d(TAG, "derived timestamp value:"+ret);
                //the ordering varies internationally
            } catch (ParsingException e) {
                throw new ParsingException("Could not get timestamp.", e);
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        if (!isAgeRestricted) {
            return 0;
        }
        try {
            return Integer.valueOf(doc.head()
                    .getElementsByAttributeValue("property", "og:restrictions:age")
                    .attr(CONTENT).replace("+", ""));
        } catch (Exception e) {
            throw new ParsingException("Could not get age restriction");
        }
    }

    @Override
    public long getLikeCount() throws ParsingException {
        String likesString = "";
        try {
            Element button = doc.select("button.like-button-renderer-like-button").first();
            try {
                likesString = button.select("span.yt-uix-button-content").first().text();
            } catch (NullPointerException e) {
                //if this ckicks in our button has no content and thefore likes/dislikes are disabled
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

    @Override
    public StreamInfoItem getNextVideo() throws IOException, ExtractionException {
        try {
            StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
            collector.commit(extractVideoPreviewInfo(doc.select("div[class=\"watch-sidebar-section\"]")
                    .first().select("li").first()));

            return ((StreamInfoItem) collector.getItemList().get(0));
        } catch (Exception e) {
            throw new ParsingException("Could not get next video", e);
        }
    }

    @Override
    public StreamInfoItemCollector getRelatedVideos() throws IOException, ExtractionException {
        try {
            StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
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

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return doc.select("div[class=\"yt-user-info\"]").first().children()
                    .select("a").first().attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel link", e);
        }
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        //todo: if implementing livestream support this value should be generated dynamically
        return StreamType.VIDEO_STREAM;
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
    private static final String GET_VIDEO_INFO_URL = "https://www.youtube.com/get_video_info?video_id=" + "%s" +
            "&el=info&ps=default&eurl=&gl=US&hl=en";

    private static volatile String decryptionCode = "";

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();

        String pageContent = dl.download(getCleanUrl());
        doc = Jsoup.parse(pageContent, getCleanUrl());
        String infoPageResponse = dl.download(String.format(GET_VIDEO_INFO_URL, getId()));
        videoInfoPage = Parser.compatParseMap(infoPageResponse);


        String playerUrl;

        // Check if the video is age restricted
        if (pageContent.contains("<meta property=\"og:restrictions:age")) {
            playerUrl = getPlayerUrlFromRestrictedVideo();
            isAgeRestricted = true;
        } else {
            JSONObject ytPlayerConfig = getPlayerConfig(pageContent);
            playerArgs = getPlayerArgs(ytPlayerConfig);
            playerUrl = getPlayerUrl(ytPlayerConfig);
            isAgeRestricted = false;
        }

        if (decryptionCode.isEmpty()) {
            decryptionCode = loadDecryptionCode(playerUrl);
        }
    }

    private JSONObject getPlayerConfig(String pageContent) throws ParsingException {
        try {
            String ytPlayerConfigRaw =
                    Parser.matchGroup1("ytplayer.config\\s*=\\s*(\\{.*?\\});", pageContent);
            return new JSONObject(ytPlayerConfigRaw);
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

    private JSONObject getPlayerArgs(JSONObject playerConfig) throws ParsingException {
        JSONObject playerArgs;

        //attempt to load the youtube js player JSON arguments
        boolean isLiveStream = false; //used to determine if this is a livestream or not
        try {
            playerArgs = playerConfig.getJSONObject("args");

            // check if we have a live stream. We need to filter it, since its not yet supported.
            if ((playerArgs.has("ps") && playerArgs.get("ps").toString().equals("live"))
                    || (playerArgs.get(URL_ENCODED_FMT_STREAM_MAP).toString().isEmpty())) {
                isLiveStream = true;
            }
        } catch (Exception e) {
            throw new ParsingException("Could not parse yt player config", e);
        }
        if (isLiveStream) {
            throw new LiveStreamException("This is a Live stream. Can't use those right now.");
        }

        return playerArgs;
    }

    private String getPlayerUrl(JSONObject playerConfig) throws ParsingException {
        try {
            // The Youtube service needs to be initialized by downloading the
            // js-Youtube-player. This is done in order to get the algorithm
            // for decrypting cryptic signatures inside certain stream urls.
            String playerUrl;

            JSONObject ytAssets = playerConfig.getJSONObject("assets");
            playerUrl = ytAssets.getString("js");

            if (playerUrl.startsWith("//")) {
                playerUrl = HTTPS + playerUrl;
            }
            return playerUrl;
        } catch (Exception e) {
            throw new ParsingException(
                    "Could not load decryption code for the Youtube service.", e);
        }
    }

    private String getPlayerUrlFromRestrictedVideo() throws ParsingException, ReCaptchaException {
        try {
            Downloader downloader = NewPipe.getDownloader();
            String playerUrl = "";
            String embedUrl = "https://www.youtube.com/embed/" + getId();
            String embedPageContent = downloader.download(embedUrl);
            //todo: find out if this can be reapaced by Parser.matchGroup1()
            Pattern assetsPattern = Pattern.compile("\"assets\":.+?\"js\":\\s*(\"[^\"]+\")");
            Matcher patternMatcher = assetsPattern.matcher(embedPageContent);
            while (patternMatcher.find()) {
                playerUrl = patternMatcher.group(1);
            }
            playerUrl = playerUrl.replace("\\", "").replace("\"", "");

            if (playerUrl.startsWith("//")) {
                playerUrl = HTTPS + playerUrl;
            }
            return playerUrl;
        } catch (IOException e) {
            throw new ParsingException(
                    "Could load decryption code form restricted video for the Youtube service.", e);
        } catch (ReCaptchaException e) {
            throw new ReCaptchaException("reCaptcha Challenge requested");
        }
    }

    private String loadDecryptionCode(String playerUrl) throws DecryptException {
        String decryptionFuncName;
        String decryptionFunc;
        String helperObjectName;
        String helperObject;
        String callerFunc = "function " + DECRYPTION_FUNC_NAME + "(a){return %%(a);}";
        String decryptionCode;

        try {
            Downloader downloader = NewPipe.getDownloader();
            if (!playerUrl.contains("https://youtube.com")) {
                //sometimes the https://youtube.com part does not get send with
                //than we have to add it by hand
                playerUrl = "https://youtube.com" + playerUrl;
            }
            String playerCode = downloader.download(playerUrl);

            decryptionFuncName =
                    Parser.matchGroup("([\"\\'])signature\\1\\s*,\\s*([a-zA-Z0-9$]+)\\(", playerCode, 2);

            String functionPattern = "("
                    + decryptionFuncName.replace("$", "\\$")
                    + "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})";
            decryptionFunc = "var " + Parser.matchGroup1(functionPattern, playerCode) + ";";

            helperObjectName = Parser
                    .matchGroup1(";([A-Za-z0-9_\\$]{2})\\...\\(", decryptionFunc);

            String helperPattern = "(var "
                    + helperObjectName.replace("$", "\\$") + "=\\{.+?\\}\\};)";
            helperObject = Parser.matchGroup1(helperPattern, playerCode);


            callerFunc = callerFunc.replace("%%", decryptionFuncName);
            decryptionCode = helperObject + decryptionFunc + callerFunc;
        } catch (IOException ioe) {
            throw new DecryptException("Could not load decrypt function", ioe);
        } catch (Exception e) {
            throw new DecryptException("Could not parse decrypt function ", e);
        }

        return decryptionCode;
    }

    private String decryptSignature(String encryptedSig, String decryptionCode) throws DecryptException {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        Object result = null;
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

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private Map<String, ItagItem> getItags(String encodedUrlMapKey, ItagItem.ItagType itagTypeWanted) throws ParsingException {
        Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();

        String encodedUrlMap = "";
        if (videoInfoPage != null && videoInfoPage.containsKey(encodedUrlMapKey)) {
            encodedUrlMap = videoInfoPage.get(encodedUrlMapKey);
        } else if (playerArgs != null && playerArgs.get(encodedUrlMapKey) != null) {
            encodedUrlMap = playerArgs.optString(encodedUrlMapKey);
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
            public String getWebPageUrl() throws ParsingException {
                return li.select("a.content-link").first().attr("abs:href");
            }

            @Override
            public String getTitle() throws ParsingException {
                //todo: check NullPointerException causing
                return li.select("span.title").first().text();
                //this page causes the NullPointerException, after finding it by searching for "tjvg":
                //https://www.youtube.com/watch?v=Uqg0aEhLFAg
            }

            @Override
            public String getUploaderName() throws ParsingException {
                return li.select("span.g-hovercard").first().text();
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
