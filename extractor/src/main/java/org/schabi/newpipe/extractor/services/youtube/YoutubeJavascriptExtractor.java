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
 * Youtube restricts streaming their media in multiple ways by requiring clients to apply a cipher function
 * on parameters of requests.
 * The cipher function is sent alongside as a JavaScript function.
 * <p>
 * This class handling fetching the JavaScript file in order to allow other classes to extract the needed functions.
 */
public class YoutubeJavascriptExtractor {

    private static final String HTTPS = "https:";
    private static String cachedJavascriptCode;

    /**
     * Extracts the JavaScript file. The result is cached, so subsequent calls use the result of previous calls.
     *
     * @param videoId Does not influence the result, but a valid video id can prevent tracking
     * @return The whole javascript file as a string.
     * @throws ParsingException If the extraction failed.
     */
    @Nonnull
    public static String extractJavascriptCode(String videoId) throws ParsingException {
        if (cachedJavascriptCode == null) {
            final YoutubeJavascriptExtractor extractor = new YoutubeJavascriptExtractor();
            String playerJsUrl = extractor.cleanJavascriptUrl(extractor.extractJavascriptUrl(videoId));
            cachedJavascriptCode = extractor.downloadJavascriptCode(playerJsUrl);
        }

        return cachedJavascriptCode;
    }

    /**
     * Same as {@link YoutubeJavascriptExtractor#extractJavascriptCode(String)} but with a constant value for videoId.
     * Possible because the videoId has no influence on the result.
     *
     * For tracking avoidance purposes it may make sense to pass in valid video ids.
     */
    @Nonnull
    public static String extractJavascriptCode() throws ParsingException {
        return extractJavascriptCode("d4IGg5dqeO8");
    }

    private String extractJavascriptUrl(String videoId) throws ParsingException {
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

        } catch (final Exception i) {
            throw new ParsingException("Embedded info did not provide YouTube player js url");
        }
        throw new ParsingException("Embedded info did not provide YouTube player js url");
    }

    private String cleanJavascriptUrl(String playerJsUrl) {
        if (playerJsUrl.startsWith("//")) {
            return HTTPS + playerJsUrl;
        } else if (playerJsUrl.startsWith("/")) {
            // sometimes https://www.youtube.com part has to be added manually
            return HTTPS + "//www.youtube.com" + playerJsUrl;
        } else {
            return playerJsUrl;
        }
    }

    private String downloadJavascriptCode(String playerJsUrl) throws ParsingException {
        try {
            return NewPipe.getDownloader().get(playerJsUrl, Localization.DEFAULT).responseBody();
        } catch (Exception e) {
            throw new ParsingException("Could not get player js code from url: " + playerJsUrl);
        }
    }
}
