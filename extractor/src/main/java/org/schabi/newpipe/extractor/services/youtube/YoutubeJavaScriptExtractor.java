package org.schabi.newpipe.extractor.services.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * The extractor of YouTube's base JavaScript player file.
 *
 * <p>
 * YouTube restrict streaming their media in multiple ways by requiring their HTML5 clients to use
 * a signature timestamp, and on streaming URLs a signature deobfuscation function for some
 * contents and a throttling parameter deobfuscation one for all contents.
 * </p>
 *
 * <p>
 * This class handles fetching of this base JavaScript player file in order to allow other classes
 * to extract the needed data.
 * </p>
 *
 * <p>
 * It will try to get the player URL from YouTube's IFrame resource first, and from a YouTube embed
 * watch page as a fallback.
 * </p>
 */
public final class YoutubeJavaScriptExtractor {

    private static final String HTTPS = "https:";
    private static final String BASE_JS_PLAYER_URL_FORMAT =
            "https://www.youtube.com/s/player/%s/player_ias.vflset/en_GB/base.js";
    private static final Pattern IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN = Pattern.compile(
            "player\\\\/([a-z0-9]{8})\\\\/");
    private static final Pattern EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN = Pattern.compile(
            "\"jsUrl\":\"(/s/player/[A-Za-z0-9]+/player_ias\\.vflset/[A-Za-z_-]+/base\\.js)\"");
    private static String cachedJavaScriptCode;

    private YoutubeJavaScriptExtractor() {
    }

    /**
     * Extracts the JavaScript file.
     *
     * <p>
     * The result is cached, so subsequent calls use the result of previous calls.
     * </p>
     *
     * @param videoId a YouTube video ID, which doesn't influence the result, but it may help in
     *                the chance that YouTube track it
     * @return the whole JavaScript file as a string
     * @throws ParsingException if the extraction failed
     */
    @Nonnull
    public static String extractJavaScriptCode(@Nonnull final String videoId)
            throws ParsingException {
        if (cachedJavaScriptCode == null) {
            String url;
            try {
                url = YoutubeJavaScriptExtractor.extractJavaScriptUrlWithIframeResource();
            } catch (final Exception e) {
                url = YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage(videoId);
            }
            final String playerJsUrl = YoutubeJavaScriptExtractor.cleanJavaScriptUrl(url);
            cachedJavaScriptCode = YoutubeJavaScriptExtractor.downloadJavaScriptCode(playerJsUrl);
        }

        return cachedJavaScriptCode;
    }

    /**
     * Reset the cached JavaScript code.
     *
     * <p>
     * It will be fetched again the next time {@link #extractJavaScriptCode(String)} is called.
     * </p>
     */
    public static void resetJavaScriptCode() {
        cachedJavaScriptCode = null;
    }

    @Nonnull
    static String extractJavaScriptUrlWithIframeResource() throws ParsingException {
        final String iframeUrl;
        final String iframeContent;
        try {
            iframeUrl = "https://www.youtube.com/iframe_api";
            iframeContent = NewPipe.getDownloader()
                    .get(iframeUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not fetch IFrame resource", e);
        }

        try {
            final String hash = Parser.matchGroup1(
                    IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN, iframeContent);
            return String.format(BASE_JS_PLAYER_URL_FORMAT, hash);
        } catch (final Parser.RegexException e) {
            throw new ParsingException(
                    "IFrame resource didn't provide JavaScript base player's hash", e);
        }
    }

    @Nonnull
    static String extractJavaScriptUrlWithEmbedWatchPage(@Nonnull final String videoId)
            throws ParsingException {
        final String embedUrl;
        final String embedPageContent;
        try {
            embedUrl = "https://www.youtube.com/embed/" + videoId;
            embedPageContent = NewPipe.getDownloader()
                    .get(embedUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not fetch embedded watch page", e);
        }

        // Parse HTML response with jsoup and look at script elements first
        final Document doc = Jsoup.parse(embedPageContent);
        final Elements elems = doc.select("script")
                .attr("name", "player/base");
        for (final Element elem : elems) {
            // Script URLs should be relative and not absolute
            final String playerUrl = elem.attr("src");
            if (playerUrl.contains("base.js")) {
                return playerUrl;
            }
        }

        // Use regexes to match the URL in a JavaScript embedded script of the HTML page
        try {
            return Parser.matchGroup1(
                    EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN, embedPageContent);
        } catch (final Parser.RegexException e) {
            throw new ParsingException(
                    "Embedded watch page didn't provide JavaScript base player's URL", e);
        }
    }

    @Nonnull
    private static String cleanJavaScriptUrl(@Nonnull final String playerJsUrl) {
        if (playerJsUrl.startsWith("//")) {
            // https part has to be added manually if the URL is protocol-relative
            return HTTPS + playerJsUrl;
        } else if (playerJsUrl.startsWith("/")) {
            // https://www.youtube.com part has to be added manually if the URL is relative to
            // YouTube's domain
            return HTTPS + "//www.youtube.com" + playerJsUrl;
        } else {
            return playerJsUrl;
        }
    }

    @Nonnull
    private static String downloadJavaScriptCode(@Nonnull final String playerJsUrl)
            throws ParsingException {
        try {
            return NewPipe.getDownloader()
                    .get(playerJsUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException(
                    "Could not get JavaScript base player's code from URL: " + playerJsUrl, e);
        }
    }
}
