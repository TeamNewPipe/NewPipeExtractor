package org.schabi.newpipe.extractor.utils.jsextractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

public final class JavaScriptExtractor {
    private JavaScriptExtractor() {

    }

    @Nonnull
    public static String matchToClosingBrace(final String playerJsCode, final String start)
            throws ParsingException {
        int startIndex = playerJsCode.indexOf(start);
        if (startIndex < 0) {
            throw new ParsingException("start not found");
        }
        startIndex += start.length();
        final String js = playerJsCode.substring(startIndex);

        final JSParser parser = new JSParser(js);
        boolean visitedOpenBrace = false;

        while (true) {
            final JSParser.Item item = parser.getNextToken();
            final Token t = item.token;

            if (t == Token.LC) {
                visitedOpenBrace = true;
            } else if (visitedOpenBrace && parser.isBalanced()) {
                return js.substring(0, item.end);
            } else if (t == Token.EOF) {
                throw new ParsingException("could not find matching braces");
            }
        }
    }
}
