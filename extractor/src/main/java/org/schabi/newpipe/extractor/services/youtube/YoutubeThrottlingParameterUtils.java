package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.utils.Parser.matchMultiplePatterns;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to get the throttling parameter decryption code and check if a streaming has the
 * throttling parameter.
 */
final class YoutubeThrottlingParameterUtils {

    /**
     * The name of the deobfuscation function which needs to be called inside the deobfuscation
     * code.
     */
    static final String DEOBFUSCATION_FUNCTION_NAME = "deobfuscate_n_param";

    // NOTE: When changing this you should also change the quick exit/shortcut
    // in getThrottlingParameterFromStreamingUrl
    private static final Pattern THROTTLING_PARAM_PATTERN = Pattern.compile("[&?]n=([^&]+)");

    private static final String SINGLE_CHAR_VARIABLE_REGEX = "[a-zA-Z0-9$_]";

    private static final String MULTIPLE_CHARS_REGEX = SINGLE_CHAR_VARIABLE_REGEX + "+";

    // CHECKSTYLE:OFF
    private static final Pattern[] DEOBFUSCATION_FUNCTION_NAME_REGEXES = {
            /*
             * Matches the following text, where we want QO:
             *
             * QO=function(... F.set("alr","yes" ...)
             */
            Pattern.compile("(\\w*)=function\\(.*\\)\\{.*set\\(\\\"alr\\\",\\\"yes\\\"\\);\\w&&"),
    };
    // CHECKSTYLE:ON


    // Escape the curly end brace to allow compatibility with Android's regex engine
    // See https://stackoverflow.com/q/45074813
    @SuppressWarnings("RegExpRedundantEscape")
    private static final String DEOBFUSCATION_FUNCTION_BODY_REGEX =
            "=\\s*function([\\S\\s]*?\\}\\s*return [\\w$]+?\\.join\\(\"\"\\)\\s*\\};)";

    private static final String DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX = "var ";

    private static final String FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX =
            "\\s*=\\s*\\[(.+?)][;,]";

    private YoutubeThrottlingParameterUtils() {
    }

    /**
     * Get the throttling parameter deobfuscation function name of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the name of the throttling parameter deobfuscation function
     * @throws ParsingException if the name of the throttling parameter deobfuscation function
     * could not be extracted
     */
    @Nonnull
    static String getDeobfuscationFunctionName(@Nonnull final String javaScriptPlayerCode)
            throws ParsingException {
        final Matcher matcher;
        try {
            matcher = matchMultiplePatterns(DEOBFUSCATION_FUNCTION_NAME_REGEXES,
                    javaScriptPlayerCode);
        } catch (final Parser.RegexException e) {
            throw new ParsingException("Could not find deobfuscation function with any of the "
                    + "known patterns in the base JavaScript player code", e);
        }

        final String functionName = matcher.group(1);
        if (matcher.groupCount() == 1) {
            return functionName;
        }

        final int arrayNum = Integer.parseInt(matcher.group(2));
        final Pattern arrayPattern = Pattern.compile(
                DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX
                        + Pattern.quote(functionName)
                        + FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX);
        final String arrayStr = Parser.matchGroup1(arrayPattern, javaScriptPlayerCode);
        final String[] names = arrayStr.split(",");
        return names[arrayNum];
    }

    /**
     * Get the throttling parameter deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the throttling parameter deobfuscation function code
     * @throws ParsingException if the throttling parameter deobfuscation code couldn't be
     * extracted
     */
    @Nonnull
    static String getDeobfuscationFunction(@Nonnull final String javaScriptPlayerCode,
                                           @Nonnull final String functionName)
            throws ParsingException {
        String function;
        try {
            function = parseFunctionWithLexer(javaScriptPlayerCode, functionName);
        } catch (final Exception e) {
            function = parseFunctionWithRegex(javaScriptPlayerCode, functionName);
        }
        return function;
    }


    /**
     * Get the throttling parameter deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the throttling parameter deobfuscation function code
     * @throws ParsingException if the throttling parameter deobfuscation code couldn't be
     * extracted
     */
    @Nonnull
    static String getDeobfuscationCode(@Nonnull final String javaScriptPlayerCode)
            throws ParsingException {
        final String functionName = getDeobfuscationFunctionName(javaScriptPlayerCode);
        final String deobfuscationFunction = getDeobfuscationFunction(javaScriptPlayerCode,
                functionName);

        // Assert the extracted deobfuscation function is valid
        JavaScript.compileOrThrow(deobfuscationFunction);

        return buildHelperFunction(javaScriptPlayerCode, deobfuscationFunction);
    }

    /**
     * Get the throttling parameter of a streaming URL if it exists.
     *
     * @param streamingUrl a streaming URL
     * @return the throttling parameter of the streaming URL or {@code null} if no parameter has
     * been found
     */
    @Nullable
    static String getThrottlingParameterFromStreamingUrl(@Nonnull final String streamingUrl) {
        // Do a quick check if the n parameter is even present, if not abort
        // This improves performance by 60-900x
        if (!streamingUrl.contains("&n=") && !streamingUrl.contains("?n=")) {
            return null;
        }
        try {
            return Parser.matchGroup1(THROTTLING_PARAM_PATTERN, streamingUrl);
        } catch (final Parser.RegexException e) {
            // If the throttling parameter could not be parsed from the URL, it means that there is
            // no throttling parameter
            // Return null in this case
            return null;
        }
    }

    @Nonnull
    private static String parseFunctionWithLexer(@Nonnull final String javaScriptPlayerCode,
                                                 @Nonnull final String functionName)
            throws ParsingException {
        final String functionBase = functionName + "=function";
        return functionBase + JavaScriptExtractor.matchToClosingBrace(
                javaScriptPlayerCode, functionBase) + ";";
    }

    @Nonnull
    private static String parseFunctionWithRegex(@Nonnull final String javaScriptPlayerCode,
                                                 @Nonnull final String functionName)
            throws Parser.RegexException {
        // Quote the function name, as it may contain special regex characters such as dollar
        final Pattern functionPattern = Pattern.compile(
                Pattern.quote(functionName) + DEOBFUSCATION_FUNCTION_BODY_REGEX,
                Pattern.DOTALL);
        return validateFunction("function " + functionName
                + Parser.matchGroup1(functionPattern, javaScriptPlayerCode));
    }

    @Nonnull
    private static String validateFunction(@Nonnull final String function) {
        JavaScript.compileOrThrow(function);
        return function;
    }

    /**
     * Builds a helper function to execute the deobfuscation function.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @param deobfuscationFunctionName the name of the throttling parameter deobfuscation function
     * @return code needed to execute the deobfuscation function
     */
    @Nonnull
    private static String buildHelperFunction(@Nonnull final String javaScriptPlayerCode,
                                              @Nonnull final String deobfuscationFunctionName) {
        final String strippedJavaScriptPlayerCode = javaScriptPlayerCode
                .replace("var _yt_player={};(function(g){var window=this;", "")
                .replace("})(_yt_player);", "");
        return """
                var g = {};
                if (typeof globalThis.XMLHttpRequest === "undefined") {
                    globalThis.XMLHttpRequest = { prototype: {} };
                }
                if (typeof URL === "undefined") {
                    globalThis.location = {
                        hash: "",
                        host: "www.youtube.com",
                        hostname: "www.youtube.com",
                        href: "https://www.youtube.com/watch?v=yt-dlp-wins",
                        origin: "https://www.youtube.com",
                        password: "",
                        pathname: "/watch",
                        port: "",
                        protocol: "https:",
                        search: "?v=yt-dlp-wins",
                        username: "",
                    };
                } else {
                    globalThis.location = new URL("https://www.youtube.com/watch?v=yt-dlp-wins");
                }
                if (typeof globalThis.document === "undefined") {
                    globalThis.document = Object.create(null);
                }
                if (typeof globalThis.navigator === "undefined") {
                    globalThis.navigator = Object.create(null);
                }
                if (typeof globalThis.self === "undefined") {
                    globalThis.self = globalThis;
                }
                if (typeof globalThis.window === "undefined") {
                    globalThis.window = globalThis;
                }
                """ +
                strippedJavaScriptPlayerCode +
                String.format("""
                          function %s(n){
                          const url = %s("https://youtube.com/watch?v=yt-dlp-wins", "s", undefined);
                          url.set("n", n);
                          const proto = Object.getPrototypeOf(url);
                          const keys = Object.keys(proto).concat(Object.getOwnPropertyNames(proto));
                          for (let i = 0; i < keys.length; i++) {
                            const key = keys[i];
                            if (!["constructor", "set", "get", "clone"].includes(key)) {
                              url[key]();
                              break;
                            }
                          }
                          return url.get("n");
                        }""", DEOBFUSCATION_FUNCTION_NAME, deobfuscationFunctionName);
    }
}
