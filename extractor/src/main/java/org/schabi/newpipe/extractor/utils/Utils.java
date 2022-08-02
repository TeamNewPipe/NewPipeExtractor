package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Utils {

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    /**
     * @deprecated Use {@link java.nio.charset.StandardCharsets#UTF_8}
     */
    @Deprecated
    public static final String UTF_8 = "UTF-8";
    public static final String EMPTY_STRING = "";
    private static final Pattern M_PATTERN = Pattern.compile("(https?)?://m\\.");
    private static final Pattern WWW_PATTERN = Pattern.compile("(https?)?://www\\.");

    private Utils() {
        // no instance
    }

    /**
     * Remove all non-digit characters from a string.
     *
     * <p>
     * Examples:
     * </p>
     *
     * <ul>
     *     <li>1 234 567 views -&gt; 1234567</li>
     *     <li>$31,133.124 -&gt; 31133124</li>
     * </ul>
     *
     * @param toRemove string to remove non-digit chars
     * @return a string that contains only digits
     */
    @Nonnull
    public static String removeNonDigitCharacters(@Nonnull final String toRemove) {
        return toRemove.replaceAll("\\D+", "");
    }

    /**
     * Convert a mixed number word to a long.
     *
     * <p>
     * Examples:
     * </p>
     *
     * <ul>
     *     <li>123 -&gt; 123</li>
     *     <li>1.23K -&gt; 1230</li>
     *     <li>1.23M -&gt; 1230000</li>
     * </ul>
     *
     * @param numberWord string to be converted to a long
     * @return a long
     */
    public static long mixedNumberWordToLong(final String numberWord)
            throws NumberFormatException, ParsingException {
        String multiplier = "";
        try {
            multiplier = Parser.matchGroup("[\\d]+([\\.,][\\d]+)?([KMBkmb])+", numberWord, 2);
        } catch (final ParsingException ignored) {
        }
        final double count = Double.parseDouble(
                Parser.matchGroup1("([\\d]+([\\.,][\\d]+)?)", numberWord).replace(",", "."));
        switch (multiplier.toUpperCase()) {
            case "K":
                return (long) (count * 1e3);
            case "M":
                return (long) (count * 1e6);
            case "B":
                return (long) (count * 1e9);
            default:
                return (long) (count);
        }
    }

    /**
     * Check if the url matches the pattern.
     *
     * @param pattern the pattern that will be used to check the url
     * @param url     the url to be tested
     */
    public static void checkUrl(final String pattern, final String url) throws ParsingException {
        if (isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Url can't be null or empty");
        }

        if (!Parser.isMatch(pattern, url.toLowerCase())) {
            throw new ParsingException("Url don't match the pattern");
        }
    }

    public static String replaceHttpWithHttps(final String url) {
        if (url == null) {
            return null;
        }

        if (url.startsWith(HTTP)) {
            return HTTPS + url.substring(HTTP.length());
        }
        return url;
    }

    /**
     * Get the value of a URL-query by name.
     *
     * <p>
     * If an url-query is give multiple times, only the value of the first query is returned.
     * </p>
     *
     * @param url           the url to be used
     * @param parameterName the pattern that will be used to check the url
     * @return a string that contains the value of the query parameter or {@code null} if nothing
     * was found
     */
    @Nullable
    public static String getQueryValue(@Nonnull final URL url,
                                       final String parameterName) {
        final String urlQuery = url.getQuery();

        if (urlQuery != null) {
            for (final String param : urlQuery.split("&")) {
                final String[] params = param.split("=", 2);

                String query;
                try {
                    query = URLDecoder.decode(params[0], UTF_8);
                } catch (final UnsupportedEncodingException e) {
                    // Cannot decode string with UTF-8, using the string without decoding
                    query = params[0];
                }

                if (query.equals(parameterName)) {
                    try {
                        return URLDecoder.decode(params[1], UTF_8);
                    } catch (final UnsupportedEncodingException e) {
                        // Cannot decode string with UTF-8, using the string without decoding
                        return params[1];
                    }
                }
            }
        }

        return null;
    }

    /**
     * Convert a string to a {@link URL URL object}.
     *
     * <p>
     * Defaults to HTTP if no protocol is given.
     * </p>
     *
     * @param url the string to be converted to a URL-Object
     * @return a {@link URL URL object} containing the url
     */
    @Nonnull
    public static URL stringToURL(final String url) throws MalformedURLException {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            // If no protocol is given try prepending "https://"
            if (e.getMessage().equals("no protocol: " + url)) {
                return new URL(HTTPS + url);
            }

            throw e;
        }
    }

    public static boolean isHTTP(@Nonnull final URL url) {
        // Make sure it's HTTP or HTTPS
        final String protocol = url.getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https")) {
            return false;
        }

        final boolean usesDefaultPort = url.getPort() == url.getDefaultPort();
        final boolean setsNoPort = url.getPort() == -1;

        return setsNoPort || usesDefaultPort;
    }

    public static String removeMAndWWWFromUrl(final String url) {
        if (M_PATTERN.matcher(url).find()) {
            return url.replace("m.", "");
        }
        if (WWW_PATTERN.matcher(url).find()) {
            return url.replace("www.", "");
        }
        return url;
    }

    @Nonnull
    public static String removeUTF8BOM(@Nonnull final String s) {
        String result = s;
        if (result.startsWith("\uFEFF")) {
            result = result.substring(1);
        }
        if (result.endsWith("\uFEFF")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @Nonnull
    public static String getBaseUrl(final String url) throws ParsingException {
        try {
            final URL uri = stringToURL(url);
            return uri.getProtocol() + "://" + uri.getAuthority();
        } catch (final MalformedURLException e) {
            final String message = e.getMessage();
            if (message.startsWith("unknown protocol: ")) {
                // Return just the protocol (e.g. vnd.youtube)
                return message.substring("unknown protocol: ".length());
            }

            throw new ParsingException("Malformed url: " + url, e);
        }
    }

    /**
     * If the provided url is a Google search redirect, then the actual url is extracted from the
     * {@code url=} query value and returned, otherwise the original url is returned.
     *
     * @param url the url which can possibly be a Google search redirect
     * @return an url with no Google search redirects
     */
    public static String followGoogleRedirectIfNeeded(final String url) {
        // If the url is a redirect from a Google search, extract the actual URL
        try {
            final URL decoded = Utils.stringToURL(url);
            if (decoded.getHost().contains("google") && decoded.getPath().equals("/url")) {
                return URLDecoder.decode(Parser.matchGroup1("&url=([^&]+)(?:&|$)", url), UTF_8);
            }
        } catch (final Exception ignored) {
        }

        // URL is not a Google search redirect
        return url;
    }

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a collection is null or empty.
     *
     * <p>
     * This method can be also used for {@link com.grack.nanojson.JsonArray JsonArray}s.
     * </p>
     *
     * @param collection the collection on which check if it's null or empty
     * @return whether the collection is null or empty
     */
    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a {@link Map map} is null or empty.
     *
     * <p>
     * This method can be also used for {@link com.grack.nanojson.JsonObject JsonObject}s.
     * </p>
     *
     * @param map the {@link Map map} on which check if it's null or empty
     * @return whether the {@link Map map} is null or empty
     */
    public static <K, V> boolean isNullOrEmpty(final Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isWhitespace(final int c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r';
    }

    public static boolean isBlank(final String string) {
        if (isNullOrEmpty(string)) {
            return true;
        }

        final int length = string.length();
        for (int i = 0; i < length; i++) {
            if (!isWhitespace(string.codePointAt(i))) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    public static String join(final CharSequence delimiter,
                              @Nonnull final Iterable<? extends CharSequence> elements) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<? extends CharSequence> iterator = elements.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
            if (iterator.hasNext()) {
                stringBuilder.append(delimiter);
            }
        }
        return stringBuilder.toString();
    }

    @Nonnull
    public static String join(
            final String delimiter,
            final String mapJoin,
            @Nonnull final Map<? extends CharSequence, ? extends CharSequence> elements) {
        final List<String> list = new LinkedList<>();
        for (final Map.Entry<? extends CharSequence, ? extends CharSequence> entry
                : elements.entrySet()) {
            list.add(entry.getKey() + mapJoin + entry.getValue());
        }
        return join(delimiter, list);
    }

    /**
     * Concatenate all non-null, non-empty and strings which are not equal to <code>"null"</code>.
     */
    @Nonnull
    public static String nonEmptyAndNullJoin(final CharSequence delimiter,
                                             final String[] elements) {
        final List<String> list = new ArrayList<>(Arrays.asList(elements));
        list.removeIf(s -> isNullOrEmpty(s) || s.equals("null"));
        return join(delimiter, list);
    }

    /**
     * Find the result of an array of string regular expressions inside an input on the first
     * group ({@code 0}).
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the string array of regular expressions
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input
     */
    @Nonnull
    public static String getStringResultFromRegexArray(@Nonnull final String input,
                                                       @Nonnull final String[] regexes)
            throws Parser.RegexException {
        return getStringResultFromRegexArray(input, regexes, 0);
    }

    /**
     * Find the result of an array of {@link Pattern}s inside an input on the first group
     * ({@code 0}).
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the {@link Pattern} array
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input
     */
    @Nonnull
    public static String getStringResultFromRegexArray(@Nonnull final String input,
                                                       @Nonnull final Pattern[] regexes)
            throws Parser.RegexException {
        return getStringResultFromRegexArray(input, regexes, 0);
    }

    /**
     * Find the result of an array of string regular expressions inside an input on a specific
     * group.
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the string array of regular expressions
     * @param group   the group to match
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input, or at least in the
     * specified group
     */
    @Nonnull
    public static String getStringResultFromRegexArray(@Nonnull final String input,
                                                       @Nonnull final String[] regexes,
                                                       final int group)
            throws Parser.RegexException {
        return getStringResultFromRegexArray(input,
                Arrays.stream(regexes)
                        .filter(Objects::nonNull)
                        .map(Pattern::compile)
                        .toArray(Pattern[]::new),
                group);
    }

    /**
     * Find the result of an array of {@link Pattern}s inside an input on a specific
     * group.
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the {@link Pattern} array
     * @param group   the group to match
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input, or at least in the
     * specified group
     */
    @Nonnull
    public static String getStringResultFromRegexArray(@Nonnull final String input,
                                                       @Nonnull final Pattern[] regexes,
                                                       final int group)
            throws Parser.RegexException {
        for (final Pattern regex : regexes) {
            try {
                final String result = Parser.matchGroup(regex, input, group);
                if (result != null) {
                    return result;
                }

                // Continue if the result is null
            } catch (final Parser.RegexException ignored) {
            }
        }

        throw new Parser.RegexException("No regex matched the input on group " + group);
    }
}
