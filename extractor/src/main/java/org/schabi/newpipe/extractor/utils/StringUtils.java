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
        int endIndex = startIndex;
        while (string.charAt(endIndex) != '{') {
            ++endIndex;
        }
        ++endIndex;

        int openParenthesis = 1;
        while (openParenthesis > 0) {
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
}
