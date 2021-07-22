package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * YouTube's media is protected with a cipher, which modifies the "n" query parameter of it's video playback urls.
 * This class handles extracting that "n" query parameter, applying the cipher on it and returning the resulting url
 * which is not throttled.
 * </p>
 *
 * <p>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=VVF2xyZLVRZZxHXZ&other=other
 * </p>
 * becomes
 * <p>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=iHywZkMipkszqA&other=other
 * </p>
 */
public class YoutubeThrottlingDecrypter {

    private static final String N_PARAM_REGEX = "[&?]n=([^&]+)";
    private static final Map<String, String> nParams = new HashMap<>();

    private final String functionName;
    private final String function;

    /**
     * <p>
     * Use this if you care about the off chance that YouTube tracks with which videoId the cipher is requested.
     * </p>
     * Otherwise use the no-arg constructor which uses a constant value.
     */
    public YoutubeThrottlingDecrypter(String videoId) throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    public YoutubeThrottlingDecrypter() throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    private String parseDecodeFunctionName(String playerJsCode) throws Parser.RegexException {
        Pattern pattern = Pattern.compile("b=a\\.get\\(\"n\"\\)\\)&&\\(b=(\\w+)\\(b\\),a\\.set\\(\"n\",b\\)");
        return Parser.matchGroup1(pattern, playerJsCode);
    }

    private String parseDecodeFunction(String playerJsCode, String functionName) throws Parser.RegexException {
        Pattern functionPattern = Pattern.compile(functionName + "=function(.*?;)\n", Pattern.DOTALL);
        return  "function " + functionName + Parser.matchGroup1(functionPattern, playerJsCode);
    }

    public String apply(String url) throws Parser.RegexException {
        if (containsNParam(url)) {
            String oldNParam = parseNParam(url);
            String newNParam = decryptNParam(oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private boolean containsNParam(String url) {
        return Parser.isMatch(N_PARAM_REGEX, url);
    }

    private String parseNParam(String url) throws Parser.RegexException {
        Pattern nValuePattern = Pattern.compile(N_PARAM_REGEX);
        return Parser.matchGroup1(nValuePattern, url);
    }

    private String decryptNParam(String nParam) {
        if (nParams.containsKey(nParam)) {
            return nParams.get(nParam);
        }
        final String decryptedNParam = JavaScript.run(function, functionName, nParam);
        nParams.put(nParam, decryptedNParam);
        return decryptedNParam;
    }

    private String replaceNParam(String url, String oldValue, String newValue) {
        return url.replace(oldValue, newValue);
    }
}
