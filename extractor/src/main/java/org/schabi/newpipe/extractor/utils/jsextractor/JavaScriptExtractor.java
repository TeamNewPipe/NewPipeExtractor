package org.schabi.newpipe.extractor.utils.jsextractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

/**
 * Utility class for extracting functions from JavaScript code.
 */
public final class JavaScriptExtractor {
    private JavaScriptExtractor() {

    }

    /**
     * Searches the given JavaScript code for the identifier of a function
     * and returns its body.
     *
     * @param jsCode JavaScript code
     * @param start start of the function (without the opening brace)
     * @return extracted code (opening brace + function + closing brace)
     * @throws ParsingException
     */
    @Nonnull
    public static String matchToClosingBrace(final String jsCode, final String start)
            throws ParsingException {
        int startIndex = jsCode.indexOf(start);
        if (startIndex < 0) {
            throw new ParsingException("Start not found");
        }
        startIndex += start.length();
        final String js = jsCode.substring(startIndex);

        final Lexer lexer = new Lexer(js);
        boolean visitedOpenBrace = false;

        while (true) {
            final Lexer.ParsedToken parsedToken = lexer.getNextToken();
            final Token t = parsedToken.token;

            if (t == Token.LC) {
                visitedOpenBrace = true;
            } else if (visitedOpenBrace && lexer.isBalanced()) {
                return js.substring(0, parsedToken.end);
            } else if (t == Token.EOF) {
                throw new ParsingException("Could not find matching braces");
            }
        }
    }
}
