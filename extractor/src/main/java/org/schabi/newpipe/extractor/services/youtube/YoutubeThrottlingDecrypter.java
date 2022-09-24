package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * YouTube's streaming URLs of HTML5 clients are protected with a cipher, which modifies their
 * {@code n} query parameter.
 *
 * <p>
 * This class handles extracting that {@code n} query parameter, applying the cipher on it and
 * returning the resulting URL which is not throttled.
 * </p>
 *
 * <p>
 * For instance,
 * {@code https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=VVF2xyZLVRZZxHXZ&other=other}
 * becomes
 * {@code https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?n=iHywZkMipkszqA&other=other}.
 * </p>
 *
 * <p>
 * Decoding the {@code n} parameter is time intensive. For this reason, the results are cached.
 * The cache can be cleared using {@link #clearCache()}.
 * </p>
 *
 */
public final class YoutubeThrottlingDecrypter {

    private static final Pattern N_PARAM_PATTERN = Pattern.compile("[&?]n=([^&]+)");
    private static final Pattern DECRYPT_FUNCTION_NAME_PATTERN = Pattern.compile(
            // CHECKSTYLE:OFF
            "\\.get\\(\"n\"\\)\\)&&\\([a-zA-Z0-9$_]=([a-zA-Z0-9$_]+)(?:\\[(\\d+)])?\\([a-zA-Z0-9$_]\\)");
            // CHECKSTYLE:ON

    // Escape the curly end brace to allow compatibility with Android's regex engine
    // See https://stackoverflow.com/q/45074813
    @SuppressWarnings("RegExpRedundantEscape")
    private static final String DECRYPT_FUNCTION_BODY_REGEX =
            "=\\s*function([\\S\\s]*?\\}\\s*return [\\w$]+?\\.join\\(\"\"\\)\\s*\\};)";

    private static final Map<String, String> N_PARAMS_CACHE = new HashMap<>();
    private static String decryptFunction;
    private static String decryptFunctionName;

    private YoutubeThrottlingDecrypter() {
        // No implementation
    }

    /**
     * Try to decrypt a YouTube streaming URL protected with a throttling parameter.
     *
     * <p>
     * If the streaming URL provided doesn't contain a throttling parameter, it is returned as it
     * is; otherwise, the encrypted value is decrypted and this value is replaced by the decrypted
     * one.
     * </p>
     *
     * <p>
     * If the JavaScript code has been not extracted, it is extracted with the given video ID using
     * {@link YoutubeJavaScriptExtractor#extractJavaScriptCode(String)}.
     * </p>
     *
     * @param streamingUrl The streaming URL to decrypt, if needed.
     * @param videoId      A video ID, used to fetch the JavaScript code to get the decryption
     *                     function. It can be a constant value of any existing video, but a
     *                     constant value is discouraged, because it could allow tracking.
     * @return A streaming URL with the decrypted parameter or the streaming URL itself if no
     * throttling parameter has been found.
     * @throws ParsingException If the streaming URL contains a throttling parameter and its
     *                          decryption failed
     */
    public static String apply(@Nonnull final String streamingUrl,
                               @Nonnull final String videoId) throws ParsingException {
        if (!containsNParam(streamingUrl)) {
            return streamingUrl;
        }

        try {
            if (decryptFunction == null) {
                final String playerJsCode
                        = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);

                decryptFunctionName = parseDecodeFunctionName(playerJsCode);
                decryptFunction = parseDecodeFunction(playerJsCode, decryptFunctionName);
            }

            final String oldNParam = parseNParam(streamingUrl);
            final String newNParam = decryptNParam(decryptFunction, decryptFunctionName, oldNParam);
            return replaceNParam(streamingUrl, oldNParam, newNParam);
        } catch (final Exception e) {
            throw new ParsingException("Could not parse, decrypt or replace n parameter", e);
        }
    }

    private static String parseDecodeFunctionName(final String playerJsCode)
            throws Parser.RegexException {
        final Matcher matcher = DECRYPT_FUNCTION_NAME_PATTERN.matcher(playerJsCode);
        if (!matcher.find()) {
            throw new Parser.RegexException("Failed to find pattern \""
                    + DECRYPT_FUNCTION_NAME_PATTERN + "\"");
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
            return parseWithLexer(playerJsCode, functionName);
        } catch (final Exception e) {
            return parseWithRegex(playerJsCode, functionName);
        }
    }

    @Nonnull
    private static String parseWithRegex(final String playerJsCode, final String functionName)
            throws Parser.RegexException {
        // Quote the function name, as it may contain special regex characters such as dollar
        final Pattern functionPattern = Pattern.compile(
                Pattern.quote(functionName) + DECRYPT_FUNCTION_BODY_REGEX, Pattern.DOTALL);
        return validateFunction("function "
                + functionName
                + Parser.matchGroup1(functionPattern, playerJsCode));
    }

    @Nonnull
    private static String validateFunction(@Nonnull final String function) {
        JavaScript.compileOrThrow(function);
        return function;
    }

    @Nonnull
    private static String parseWithLexer(final String playerJsCode, final String functionName)
            throws ParsingException {
        final String functionBase = functionName + "=function";
        return functionBase + JavaScriptExtractor.matchToClosingBrace(playerJsCode, functionBase)
                + ";";
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
     * @return The number of the cached {@code n} query parameters.
     */
    public static int getCacheSize() {
        return N_PARAMS_CACHE.size();
    }

    /**
     * Clears all stored {@code n} query parameters.
     */
    public static void clearCache() {
        N_PARAMS_CACHE.clear();
    }
}
