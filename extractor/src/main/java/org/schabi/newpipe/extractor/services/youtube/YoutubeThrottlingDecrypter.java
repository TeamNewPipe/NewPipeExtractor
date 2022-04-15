package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
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

    private static final Pattern N_PARAM_PATTERN = Pattern.compile("[&?]n=([^&]+)");
    private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile(
            "\\.get\\(\"n\"\\)\\)&&\\(b=([a-zA-Z0-9$]+)(?:\\[(\\d+)])?\\([a-zA-Z0-9]\\)");

    private static final Map<String, String> N_PARAMS_CACHE = new HashMap<>();
    @SuppressWarnings("StaticVariableName") private static String FUNCTION;
    @SuppressWarnings("StaticVariableName") private static String FUNCTION_NAME;

    private final String functionName;
    private final String function;

    /**
     * <p>
     * Use this if you care about the off chance that YouTube tracks with which videoId the cipher
     * is requested.
     * </p>
     * Otherwise use the no-arg constructor which uses a constant value.
     *
     * @deprecated Use static function instead
     */
    public YoutubeThrottlingDecrypter(final String videoId) throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    /**
     * @deprecated Use static function instead
     */
    public YoutubeThrottlingDecrypter() throws ParsingException {
        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();

        functionName = parseDecodeFunctionName(playerJsCode);
        function = parseDecodeFunction(playerJsCode, functionName);
    }

    /**
     * <p>
     * The videoId is only used to fetch the decryption function.
     * It can be a constant value of any existing video.
     * A constant value is discouraged, because it could allow tracking.
     */
    public static String apply(final String url, final String videoId) throws ParsingException {
        if (containsNParam(url)) {
            if (FUNCTION == null) {
                final String playerJsCode
                        = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

                FUNCTION_NAME = parseDecodeFunctionName(playerJsCode);
                FUNCTION = parseDecodeFunction(playerJsCode, FUNCTION_NAME);
            }

            final String oldNParam = parseNParam(url);
            final String newNParam = decryptNParam(FUNCTION, FUNCTION_NAME, oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private static String parseDecodeFunctionName(final String playerJsCode)
            throws Parser.RegexException {
        final Matcher matcher = FUNCTION_NAME_PATTERN.matcher(playerJsCode);
        final boolean foundMatch = matcher.find();
        if (!foundMatch) {
            throw new Parser.RegexException("Failed to find pattern \""
                    + FUNCTION_NAME_PATTERN + "\"");
        }

        final String functionName = matcher.group(1);
        if (matcher.groupCount() == 1) {
            return functionName;
        }

        final int arrayNum = Integer.parseInt(matcher.group(2));
        final Pattern arrayPattern = Pattern.compile(
                "var " + Pattern.quote(functionName) + "\\s*=\\s*\\[(.+?)];");
        final String arrayStr = Parser.matchGroup1(arrayPattern, playerJsCode);
        final String[] names = arrayStr.split(",");
        return names[arrayNum];
    }

    @Nonnull
    private static String parseDecodeFunction(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        try {
            return parseWithParenthesisMatching(playerJsCode, functionName);
        } catch (final Exception e) {
            return parseWithRegex(playerJsCode, functionName);
        }
    }

    @Nonnull
    private static String parseWithParenthesisMatching(final String playerJsCode,
                                                       final String functionName) {
        final String functionBase = functionName + "=function";
        return functionBase + StringUtils.matchToClosingParenthesis(playerJsCode, functionBase)
                + ";";
    }

    @Nonnull
    private static String parseWithRegex(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        final Pattern functionPattern = Pattern.compile(functionName + "=function(.*?}};)\n",
                Pattern.DOTALL);
        return "function " + functionName + Parser.matchGroup1(functionPattern, playerJsCode);
    }

    @Deprecated
    public String apply(final String url) throws ParsingException {
        if (containsNParam(url)) {
            final String oldNParam = parseNParam(url);
            final String newNParam = decryptNParam(function, functionName, oldNParam);
            return replaceNParam(url, oldNParam, newNParam);
        } else {
            return url;
        }
    }

    private static boolean containsNParam(final String url) {
        return Parser.isMatch(N_PARAM_PATTERN, url);
    }

    private static String parseNParam(final String url) throws Parser.RegexException {
        return Parser.matchGroup1(N_PARAM_PATTERN, url);
    }

    private static String decryptNParam(final String function,
                                        final String functionName,
                                        final String nParam) {
        if (N_PARAMS_CACHE.containsKey(nParam)) {
            return N_PARAMS_CACHE.get(nParam);
        }
        final String decryptedNParam = JavaScript.run(function, functionName, nParam);
        N_PARAMS_CACHE.put(nParam, decryptedNParam);
        return decryptedNParam;
    }

    @Nonnull
    private static String replaceNParam(@Nonnull final String url,
                                        final String oldValue,
                                        final String newValue) {
        return url.replace(oldValue, newValue);
    }

    /**
     * @return the number of the cached "n" query parameters.
     */
    public static int getCacheSize() {
        return N_PARAMS_CACHE.size();
    }

    /**
     * Clears all stored "n" query parameters.
     */
    public static void clearCache() {
        N_PARAMS_CACHE.clear();
    }
}
