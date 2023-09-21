package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * Utility class to get the signature timestamp of YouTube's base JavaScript player and deobfuscate
 * signature of streaming URLs from HTML5 clients.
 */
final class YoutubeSignatureUtils {

    /**
     * The name of the deobfuscation function which needs to be called inside the deobfuscation
     * code.
     */
    static final String DEOBFUSCATION_FUNCTION_NAME = "deobfuscate";

    private static final String[] FUNCTION_REGEXES = {
            "\\bm=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(h\\.s\\)\\)",
            "\\bc&&\\(c=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(c\\)\\)",
            // CHECKSTYLE:OFF
            "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2,})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            // CHECKSTYLE:ON
            "([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;"
    };

    private static final String STS_REGEX = "signatureTimestamp[=:](\\d+)";

    private static final String DEOBF_FUNC_REGEX_START = "(";
    private static final String DEOBF_FUNC_REGEX_END = "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})";

    private static final String SIG_DEOBF_HELPER_OBJ_NAME_REGEX = ";([A-Za-z0-9_\\$]{2,})\\...\\(";
    private static final String SIG_DEOBF_HELPER_OBJ_REGEX_START = "(var ";
    private static final String SIG_DEOBF_HELPER_OBJ_REGEX_END = "=\\{(?>.|\\n)+?\\}\\};)";

    private YoutubeSignatureUtils() {
    }

    /**
     * Get the signature timestamp property of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature timestamp
     * @throws ParsingException if the signature timestamp couldn't be extracted
     */
    @Nonnull
    static String getSignatureTimestamp(@Nonnull final String javaScriptPlayerCode)
            throws ParsingException {
        try {
            return Parser.matchGroup1(STS_REGEX, javaScriptPlayerCode);
        } catch (final ParsingException e) {
            throw new ParsingException(
                    "Could not extract signature timestamp from JavaScript code", e);
        }
    }

    /**
     * Get the signature deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature deobfuscation code
     * @throws ParsingException if the signature deobfuscation code couldn't be extracted
     */
    @Nonnull
    static String getDeobfuscationCode(@Nonnull final String javaScriptPlayerCode)
            throws ParsingException {
        try {
            final String deobfuscationFunctionName = getDeobfuscationFunctionName(
                    javaScriptPlayerCode);

            String deobfuscationFunction;
            try {
                deobfuscationFunction = getDeobfuscateFunctionWithLexer(
                        javaScriptPlayerCode, deobfuscationFunctionName);
            } catch (final Exception e) {
                deobfuscationFunction = getDeobfuscateFunctionWithRegex(
                        javaScriptPlayerCode, deobfuscationFunctionName);
            }

            // Assert the extracted deobfuscation function is valid
            JavaScript.compileOrThrow(deobfuscationFunction);

            final String helperObjectName =
                    Parser.matchGroup1(SIG_DEOBF_HELPER_OBJ_NAME_REGEX, deobfuscationFunction);

            final String helperObject = getHelperObject(javaScriptPlayerCode, helperObjectName);

            final String callerFunction = "function " + DEOBFUSCATION_FUNCTION_NAME
                    + "(a){return "
                    + deobfuscationFunctionName
                    + "(a);}";

            return helperObject + deobfuscationFunction + ";" + callerFunction;
        } catch (final Exception e) {
            throw new ParsingException("Could not parse deobfuscation function", e);
        }
    }

    @Nonnull
    private static String getDeobfuscationFunctionName(@Nonnull final String javaScriptPlayerCode)
            throws ParsingException {
        Parser.RegexException exception = null;
        for (final String regex : FUNCTION_REGEXES) {
            try {
                return Parser.matchGroup1(regex, javaScriptPlayerCode);
            } catch (final Parser.RegexException e) {
                if (exception == null) {
                    exception = e;
                }
            }
        }

        throw new ParsingException(
                "Could not find deobfuscation function with any of the known patterns", exception);
    }

    @Nonnull
    private static String getDeobfuscateFunctionWithLexer(
            @Nonnull final String javaScriptPlayerCode,
            @Nonnull final String deobfuscationFunctionName) throws ParsingException {
        final String functionBase = deobfuscationFunctionName + "=function";
        return functionBase + JavaScriptExtractor.matchToClosingBrace(
                javaScriptPlayerCode, functionBase);
    }

    @Nonnull
    private static String getDeobfuscateFunctionWithRegex(
            @Nonnull final String javaScriptPlayerCode,
            @Nonnull final String deobfuscationFunctionName) throws ParsingException {
        final String functionPattern = DEOBF_FUNC_REGEX_START
                + Pattern.quote(deobfuscationFunctionName)
                + DEOBF_FUNC_REGEX_END;
        return "var " + Parser.matchGroup1(functionPattern, javaScriptPlayerCode);
    }

    @Nonnull
    private static String getHelperObject(@Nonnull final String javaScriptPlayerCode,
                                          @Nonnull final String helperObjectName)
            throws ParsingException {
        final String helperPattern = SIG_DEOBF_HELPER_OBJ_REGEX_START
                + Pattern.quote(helperObjectName)
                + SIG_DEOBF_HELPER_OBJ_REGEX_END;
        return Parser.matchGroup1(helperPattern, javaScriptPlayerCode)
                .replace("\n", "");
    }
}
