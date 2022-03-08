package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * YouTube's media is protected with a cipher,
 * which modifies the "n" query parameter of it's video playback urls.
 * This class handles extracting that "n" query parameter,
 * applying the cipher on it and returning the resulting url which is not throttled.
 * </p>
 *
 * <pre>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=VVF2xyZLVRZZxHXZ&amp;other=other
 * </pre>
 * becomes
 * <pre>
 * https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=iHywZkMipkszqA&amp;other=other
 * </pre>
 * <br>
 * <p>
 * Decoding the "n" parameter is time intensive. For this reason, the results are cached.
 * The cache can be cleared using {@link #clearCache()}
 * </p>
 *
 */
public class YoutubeThrottlingDecrypter {

    private static Pattern nParamPattern = Pattern.compile("[&?]n=([^&]+)");
    private static Pattern functionNamePattern = Pattern.compile(
            "b=a\\.get\\(\"n\"\\)\\)&&\\(b=(\\S+)\\(b\\),a\\.set\\(\"n\",b\\)");

    private static final Map<String, String> nParams = new HashMap<>();

    private final String functionName;
    private final String function;

    public static void setnParamPattern(String nParamPattern) {
        YoutubeThrottlingDecrypter.nParamPattern = Pattern.compile(nParamPattern);
    }

    public static void setFunctionNamePattern(String functionNamePattern) {
        YoutubeThrottlingDecrypter.functionNamePattern = Pattern.compile(functionNamePattern);
    }

    /**
     * <p>
     * Use this if you care about the off chance that YouTube tracks with which videoId the cipher
     * is requested.
     * </p>
     * Otherwise use the no-arg constructor which uses a constant value.
     */
    public YoutubeThrottlingDecrypter(final String videoId) throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    public YoutubeThrottlingDecrypter() throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    private String parseDecodeFunctionName(final String playerJsCode)
            throws Parser.RegexException {
            String functionName = Parser.matchGroup1(functionNamePattern, playerJsCode);
            final int arrayStartBrace = functionName.indexOf("[");
            
            if (arrayStartBrace > 0) {
                final String arrayVarName = functionName.substring(0, arrayStartBrace);
                final String order = functionName.substring(
                        arrayStartBrace + 1, functionName.indexOf("]"));
                final int arrayNum = Integer.parseInt(order);
                final Pattern arrayPattern = Pattern.compile(
                        String.format("var %s=\\[(.+?)\\];", arrayVarName));
                final String arrayStr = Parser.matchGroup1(arrayPattern, playerJsCode);
                final String[] names = arrayStr.split(",");
                functionName = names[arrayNum];
            }
            return functionName;
    }

    @Nonnull
    private String parseDecodeFunction(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        try {
            return parseWithParenthesisMatching(playerJsCode, functionName);
        } catch (Exception e) {
            return parseWithRegex(playerJsCode, functionName);
        }
    }

    @Nonnull
    private String parseWithParenthesisMatching(final String playerJsCode, final String functionName) {
        final String functionBase = functionName + "=function";
        return functionBase + StringUtils.matchToClosingParenthesis(playerJsCode, functionBase) + ";";
    }

    @Nonnull
    private String parseWithRegex(final String playerJsCode, final String functionName) throws Parser.RegexException {
        final Pattern functionPattern = Pattern.compile(functionName + "=function(.*?}};)\n",
                Pattern.DOTALL);
        return "function " + functionName + Parser.matchGroup1(functionPattern, playerJsCode);
    }

    public String apply(final String url) throws Parser.RegexException {
        if (containsNParam(url)) {
            final String oldNParam = parseNParam(url);
            final String newNParam = decryptNParam(oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private boolean containsNParam(final String url) {
        return Parser.isMatch(nParamPattern, url);
    }

    private String parseNParam(final String url) throws Parser.RegexException {
        return Parser.matchGroup1(nParamPattern, url);
    }

    private String decryptNParam(final String nParam) {
        if (nParams.containsKey(nParam)) {
            return nParams.get(nParam);
        }
        final String decryptedNParam = JavaScript.run(function, functionName, nParam);
        nParams.put(nParam, decryptedNParam);
        return decryptedNParam;
    }

    @Nonnull
    private String replaceNParam(@Nonnull final String url,
                                 final String oldValue,
                                 final String newValue) {
        return url.replace(oldValue, newValue);
    }

    /**
     * @return the number of the cached "n" query parameters.
     */
    public static int getCacheSize() {
        return nParams.size();
    }

    /**
     * Clears all stored "n" query parameters.
     */
    public static void clearCache() {
        nParams.clear();
    }
}
