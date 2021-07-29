package org.schabi.newpipe.extractor.utils;

import edu.umd.cs.findbugs.annotations.NonNull;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * @param string The string to search in
     * @param start A string from which to start searching.
     * @return A substring where each '{' matches a '}'
     * @throws IndexOutOfBoundsException If {@ string} does not contain {@code start}
     */
    @NonNull
    public static String matchToClosingParenthesis(@NonNull final String string, @NonNull final String start) {
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
