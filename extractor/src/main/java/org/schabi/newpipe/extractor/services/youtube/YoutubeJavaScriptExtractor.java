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

/**
 * YouTube restricts streaming their media in multiple ways by requiring clients to apply a cipher
 * function on parameters of requests.
 * The cipher function is sent alongside as a JavaScript function.
 * <p>
 * This class handling fetching the JavaScript file in order to allow other classes to extract the
 * needed functions.
 */
public final class YoutubeJavaScriptExtractor {

    private static final String HTTPS = "https:";
    private static String cachedJavaScriptCode;

    private YoutubeJavaScriptExtractor() {
    }

    /**
     * Extracts the JavaScript file. The result is cached, so subsequent calls use the result of
     * previous calls.
     *
     * @param videoId Does not influence the result, but a valid video id may help in the chance
     *                that YouTube tracks it.
     * @return The whole JavaScript file as a string.
     * @throws ParsingException If the extraction failed.
     */
    @Nonnull
    public static String extractJavaScriptCode(final String videoId) throws ParsingException {
        if (cachedJavaScriptCode == null) {
            String url;
            try {
                url = YoutubeJavaScriptExtractor.extractJavaScriptUrl();
            } catch (final Exception i) {
                url = YoutubeJavaScriptExtractor.extractJavaScriptUrl(videoId);
            }
            final String playerJsUrl = YoutubeJavaScriptExtractor.cleanJavaScriptUrl(url);
            cachedJavaScriptCode = YoutubeJavaScriptExtractor.downloadJavaScriptCode(playerJsUrl);
        }

        return cachedJavaScriptCode;
    }

    /**
     * Same as {@link YoutubeJavaScriptExtractor#extractJavaScriptCode(String)} but with a constant
     * value for videoId.
     * Possible because the videoId has no influence on the result.
     * <p>
     * In the off chance that YouTube tracks with which video id the request is made, it may make
     * sense to pass in video ids.
     */
    @Nonnull
    public static String extractJavaScriptCode() throws ParsingException {
        return extractJavaScriptCode("d4IGg5dqeO8");
    }

    /**
     * Reset the JavaScript code. It will be fetched again the next time
     * {@link #extractJavaScriptCode()} or {@link #extractJavaScriptCode(String)} is called.
     */
    public static void resetJavaScriptCode() {
        cachedJavaScriptCode = null;
    }

    public static String extractJavaScriptUrl() throws ParsingException {
        try {
            final String iframeUrl = "https://www.youtube.com/iframe_api";
            final String iframeContent = NewPipe.getDownloader()
                    .get(iframeUrl, Localization.DEFAULT).responseBody();
            final String hashPattern = "player\\\\\\/([a-z0-9]{8})\\\\\\/";
            final String hash = Parser.matchGroup1(hashPattern, iframeContent);

            return String.format(
                    "https://www.youtube.com/s/player/%s/player_ias.vflset/en_US/base.js", hash);
        } catch (final Exception ignored) {
        }

        throw new ParsingException("Iframe API did not provide YouTube player js url");
    }

    public static String extractJavaScriptUrl(final String videoId) throws ParsingException {
        try {
            final String embedUrl = "https://www.youtube.com/embed/" + videoId;
            final String embedPageContent = NewPipe.getDownloader()
                    .get(embedUrl, Localization.DEFAULT).responseBody();

            try {
                final String assetsPattern = "\"assets\":.+?\"js\":\\s*(\"[^\"]+\")";
                return Parser.matchGroup1(assetsPattern, embedPageContent)
                        .replace("\\", "").replace("\"", "");
            } catch (final Parser.RegexException ex) {
                // playerJsUrl is still available in the file, just somewhere else TODO
                // it is ok not to find it, see how that's handled in getDeobfuscationCode()
                final Document doc = Jsoup.parse(embedPageContent);
                final Elements elems = doc.select("script").attr("name", "player_ias/base");
                for (final Element elem : elems) {
                    if (elem.attr("src").contains("base.js")) {
                        return elem.attr("src");
                    }
                }
            }
        } catch (final Exception ignored) {
        }

        throw new ParsingException("Embedded info did not provide YouTube player js url");
    }

    @Nonnull
    private static String cleanJavaScriptUrl(@Nonnull final String playerJsUrl) {
        if (playerJsUrl.startsWith("//")) {
            return HTTPS + playerJsUrl;
        } else if (playerJsUrl.startsWith("/")) {
            // sometimes https://www.youtube.com part has to be added manually
            return HTTPS + "//www.youtube.com" + playerJsUrl;
        } else {
            return playerJsUrl;
        }
    }

    @Nonnull
    private static String downloadJavaScriptCode(final String playerJsUrl)
            throws ParsingException {
        try {
            return NewPipe.getDownloader().get(playerJsUrl, Localization.DEFAULT).responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not get player js code from url: " + playerJsUrl);
        }
    }
}
