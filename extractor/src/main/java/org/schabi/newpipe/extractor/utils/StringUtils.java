package org.schabi.newpipe.extractor.utils;

import javax.annotation.Nonnull;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * @param string The string to search in.
     * @param start A string from which to start searching.
     * @return A substring where each '{' matches a '}'.
     * @throws IndexOutOfBoundsException If {@code string} does not contain {@code start}
     * or parenthesis could not be matched .
     */
    @Nonnull
    public static String matchToClosingParenthesis(@Nonnull final String string,
                                                   @Nonnull final String start) {
        int startIndex = string.indexOf(start);
        if (startIndex < 0) {
            throw new IndexOutOfBoundsException();
        }

        startIndex += start.length();
        int endIndex = findNextParenthesis(string, startIndex, true);
        ++endIndex;

        int openParenthesis = 1;
        while (openParenthesis > 0) {
            endIndex = findNextParenthesis(string, endIndex, false);

            switch (string.charAt(endIndex)) {
                case '{':
                    ++openParenthesis;
                    break;
                case '}':
                    --openParenthesis;
                    break;
                default:
                    break;
            }
            ++endIndex;
        }

        return string.substring(startIndex, endIndex);
    }

    private static int findNextParenthesis(@Nonnull final String string,
                                           final int offset,
                                           final boolean onlyOpen) {
        boolean lastEscaped = false;
        char quote = ' ';

        for (int i = offset; i < string.length(); i++) {
            boolean thisEscaped = false;
            final char c = string.charAt(i);

            switch (c) {
                case '{':
                    if (quote == ' ') {
                        return i;
                    }
                    break;
                case '}':
                    if (!onlyOpen && quote == ' ') {
                        return i;
                    }
                    break;
                case '\\':
                    if (!lastEscaped) {
                        thisEscaped = true;
                    }
                    break;
                case '\'':
                case '"':
                    if (!lastEscaped) {
                        if (quote == ' ') {
                            quote = c;
                        } else if (quote == c) {
                            quote = ' ';
                        }
                    }
            }

            lastEscaped = thisEscaped;
        }

        return -1;
    }
}
