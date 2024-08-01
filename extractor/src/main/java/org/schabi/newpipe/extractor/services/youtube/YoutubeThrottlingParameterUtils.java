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

    private static final Pattern THROTTLING_PARAM_PATTERN = Pattern.compile("[&?]n=([^&]+)");

    private static final String SINGLE_CHAR_VARIABLE_REGEX = "[a-zA-Z0-9$_]";

    private static final String FUNCTION_NAME_REGEX = SINGLE_CHAR_VARIABLE_REGEX + "+";

    private static final String ARRAY_ACCESS_REGEX = "\\[(\\d+)]";

    // CHECKSTYLE:OFF
    private static final Pattern[] DEOBFUSCATION_FUNCTION_NAME_REGEXES = {

            /*
             * The first regex matches the following text, where we want rDa and the array index
             * accessed:
             *
             * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
             */
            Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "+=\"nn\"\\[\\+"
                    + SINGLE_CHAR_VARIABLE_REGEX + "+\\." + SINGLE_CHAR_VARIABLE_REGEX + "+],"
                    + SINGLE_CHAR_VARIABLE_REGEX + "+=" + SINGLE_CHAR_VARIABLE_REGEX
                    + "+\\.get\\(" + SINGLE_CHAR_VARIABLE_REGEX + "+\\)\\)&&\\("
                    + SINGLE_CHAR_VARIABLE_REGEX + "+=(" + SINGLE_CHAR_VARIABLE_REGEX
                    + "+)\\[(\\d+)]"),

            /*
             * The second regex matches the following text, where we want rma:
             *
             * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
             */
            Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "+=\"nn\"\\[\\+"
                    + SINGLE_CHAR_VARIABLE_REGEX + "+\\." + SINGLE_CHAR_VARIABLE_REGEX + "+],"
                    + SINGLE_CHAR_VARIABLE_REGEX + "+=" + SINGLE_CHAR_VARIABLE_REGEX + "+\\.get\\("
                    + SINGLE_CHAR_VARIABLE_REGEX + "+\\)\\).+\\|\\|(" + SINGLE_CHAR_VARIABLE_REGEX
                    + "+)\\(\"\"\\)"),

            /*
             * The third regex matches the following text, where we want BDa and the array index
             * accessed:
             *
             * (b=String.fromCharCode(110),c=a.get(b))&&(c=BDa[0](c)
             */
            Pattern.compile("\\(" + SINGLE_CHAR_VARIABLE_REGEX + "=String\\.fromCharCode\\(110\\),"
                    + SINGLE_CHAR_VARIABLE_REGEX + "=" + SINGLE_CHAR_VARIABLE_REGEX + "\\.get\\("
                    + SINGLE_CHAR_VARIABLE_REGEX + "\\)\\)" + "&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                    + "=(" + FUNCTION_NAME_REGEX + ")" + "(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                    + SINGLE_CHAR_VARIABLE_REGEX + "\\)"),

            /*
             * The fourth regex matches the following text, where we want Yva and the array index
             * accessed:
             *
             * .get("n"))&&(b=Yva[0](b)
             */
            Pattern.compile("\\.get\\(\"n\"\\)\\)&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                    + "=(" + FUNCTION_NAME_REGEX + ")(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                    + SINGLE_CHAR_VARIABLE_REGEX + "\\)"),

            /*
             * The fifth regex matches the following text, where we want oDa and the array index
             * accessed:
             *
             * ;a.D&&(PL(a),b=a.j.n||null)&&(b=oDa[0](b)
             */
            Pattern.compile("(?x)" +
                "(?:\\.get\\(\"n\"\\)\\)&&\\(b=|" +
                "(?:b=String\\.fromCharCode\\(110\\)|" +
                "(" + SINGLE_CHAR_VARIABLE_REGEX + "+)&&\\(b=\"nn\"\\[\\+\\1\\]" +
                "),c=a\\.get\\(b\\)\\)&&\\(c=|" +
                "\\b(" + SINGLE_CHAR_VARIABLE_REGEX + "+)=" +
                ")(" + SINGLE_CHAR_VARIABLE_REGEX + "+)(?:\\[(\\d+)\\])?\\" +
                "(" + SINGLE_CHAR_VARIABLE_REGEX + "\\).*?(?=,a\\.set\\(\"n\",\\2\\))")
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

        if (matcher.groupCount() == 1) {
            return matcher.group(1);
        }

        final int funcNameIndex = matcher.groupCount() - 1;
        final String functionName = matcher.group(funcNameIndex);

        final int arrayNumIndex = matcher.groupCount();
        final int arrayNum = Integer.parseInt(matcher.group(arrayNumIndex));
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
     * @return the throttling parameter deobfuscation function name
     * @throws ParsingException if the throttling parameter deobfuscation code couldn't be
     * extracted
     */
    @Nonnull
    static String getDeobfuscationFunction(@Nonnull final String javaScriptPlayerCode,
                                           @Nonnull final String functionName)
            throws ParsingException {
        try {
            return parseFunctionWithLexer(javaScriptPlayerCode, functionName);
        } catch (final Exception e) {
            return parseFunctionWithRegex(javaScriptPlayerCode, functionName);
        }
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
}
