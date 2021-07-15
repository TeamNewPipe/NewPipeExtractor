package org.schabi.newpipe.extractor.services.youtube;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.Javascript;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.regex.Pattern;

public class YoutubeThrottlingDecoder {

    private static final String HTTPS = "https:";
    private static final String N_PARAM_REGEX = "[&?]n=([^&]+)";

    private final String functionName;
    private final String function;

    public YoutubeThrottlingDecoder(String videoId, Localization localization) throws ParsingException {
        String playerJsUrl = cleanPlayerJsUrl(extractPlayerJsUrl(videoId, localization));
        String playerJsCode = downloadPlayerJsCode(localization, playerJsUrl);

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    private String extractPlayerJsUrl(String videoId, Localization localization) throws ParsingException {
        try {
            final String embedUrl = "https://www.youtube.com/embed/" + videoId;
            final String embedPageContent = NewPipe.getDownloader()
                    .get(embedUrl, localization).responseBody();

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

    private String cleanPlayerJsUrl(String playerJsUrl) {
        if (playerJsUrl.startsWith("//")) {
            return HTTPS + playerJsUrl;
        } else if (playerJsUrl.startsWith("/")) {
            // sometimes https://www.youtube.com part has to be added manually
            return HTTPS + "//www.youtube.com" + playerJsUrl;
        } else {
            return playerJsUrl;
        }
    }

    private String downloadPlayerJsCode(Localization localization, String playerJsUrl) throws ParsingException {
        try {
            return NewPipe.getDownloader().get(playerJsUrl, localization).responseBody();
        } catch (Exception e) {
            throw new ParsingException("Could not get player js code from url: " + playerJsUrl);
        }
    }

    private String parseDecodeFunctionName(String playerJsCode) throws Parser.RegexException {
        Pattern pattern = Pattern.compile("b=a\\.get\\(\"n\"\\)\\)&&\\(b=(\\w+)\\(b\\),a\\.set\\(\"n\",b\\)");
        return Parser.matchGroup1(pattern, playerJsCode);
    }

    private String parseDecodeFunction(String playerJsCode, String functionName) throws Parser.RegexException {
        Pattern functionPattern = Pattern.compile(functionName + "=function(.*?;)\n", Pattern.DOTALL);
        return  "function " + functionName + Parser.matchGroup1(functionPattern, playerJsCode);
    }

    public String parseNParam(String url) throws Parser.RegexException {
        Pattern nValuePattern = Pattern.compile(N_PARAM_REGEX);
        return Parser.matchGroup1(nValuePattern, url);
    }

    public String decodeNParam(String nParam) {
        Javascript javascript = new Javascript();
        return javascript.run(function, functionName, nParam);
    }

    public String replaceNParam(String url, String newValue) {
        Pattern nValuePattern = Pattern.compile(N_PARAM_REGEX);
        return nValuePattern.matcher(url).replaceFirst(newValue);
    }
}
