package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.Localization;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import static org.schabi.newpipe.extractor.localization.AbbreviationHelper.abbreviationSubscribersCount;

public class Utils {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private Utils() {
        //no instance
    }

    /**
     * Remove all non-digit characters from a string.<p>
     * Examples:<p>
     * <ul><li>1 234 567 views -&gt; 1234567</li>
     * <li>$31,133.124 -&gt; 31133124</li></ul>
     *
     * @param toRemove string to remove non-digit chars
     * @return a string that contains only digits
     */
    public static String removeNonDigitCharacters(String toRemove) {
        return toRemove.replaceAll("\\D+", "");
    }

    /**
     * <p>Remove a number from a string.</p>
     * <p>Examples:</p>
     * <ul>
     *     <li>"123" -&gt; ""</li>
     *     <li>"1.23K" -&gt; "K"</li>
     *     <li>"1.23 M" -&gt; " M"</li>
     * </ul>
     * Pay attention, it may remove the final dot.
     * eg: "8,93 хил." -> " хил"
     *
     * @param toRemove string to remove a number
     * @return a string that contains only not a number
     */
    public static String removeNumber(String toRemove) {
        return toRemove.replaceAll("[0-9,.]", "");
    }

    /**
     * <p>Convert a mixed number word to a long.</p>
     * <p>Examples:</p>
     * <ul>
     *     <li>123 -&gt; 123</li>
     *     <li>1.23K -&gt; 1230</li>
     *     <li>1.23M -&gt; 1230000</li>
     * </ul>
     *
     * @param numberWord string to be converted to a long
     * @return a long
     * @throws NumberFormatException
     * @throws ParsingException
     */
    public static long mixedNumberWordToLong(String numberWord) throws NumberFormatException, ParsingException {
        String multiplier = "";
        try {
            multiplier = Parser.matchGroup("[\\d]+([\\.,][\\d]+)?([KMBkmb万লক億])+", numberWord, 2);
        } catch (ParsingException ignored) {
        }
        double count = Double.parseDouble(Parser.matchGroup1("([\\d]+([\\.,][\\d]+)?)", numberWord)
                .replace(",", "."));
        switch (multiplier.toUpperCase()) {
            case "K":
                return (long) (count * 1e3);
            case "万": //10K, used by east-asian languages
                return (long) (count * 1e4);
            case "ল": //100K, used by indo-arabic languages
                return (long) (count * 1e5);
            case "M":
                return (long) (count * 1e6);
            case "ক": //10M, used by indo-arabic languages
                return (long) (count * 1e7);
            case "億": //100M, used by east-asian languages
                return (long) (count * 1e8);
            case "B":
                return (long) (count * 1e9);
            default:
                return (long) (count);
        }
    }

    public static String cleanWhiteSpaces(String s) {
        return s.replaceAll("(\\s| | )", "");
    }

    //does the same as the function above, but for the 80 languages supported by YouTube.
    public static long mixedNumberWordToLong(String numberWord, Localization loc) throws ParsingException {
        numberWord = cleanWhiteSpaces(numberWord);
        String langCode = loc.getLanguageCode();
        String abbreviation = removeNumber(numberWord);

        //special case for portugal, "mil" is the abbreviation for thousand, but is Million for many other languages
        if (langCode.equals("pt") && abbreviation.equals("mil")) {
            numberWord = numberWord.replace("mil", "K");
        } else if (langCode.equals("ca") && abbreviation.equals("m")) { //same for catalan but for "m"
            numberWord = numberWord.replace("m", "K");
        }
        //special case for languages written right to left
        else if (langCode.equals("sw") && abbreviation.equals("elfu")) {
            numberWord = moveAtRight("elfu", numberWord);
        } else if (langCode.equals("si")) {
            numberWord = moveAtRight(abbreviation, numberWord);
        }

        try { //special cases where it gives a number directly for some languages, or with a dot or a comma
            String maybeAlreadyNumber = numberWord.replaceAll("([.,])", "");
            return Long.parseLong(maybeAlreadyNumber);
        } catch (NumberFormatException e) {
            //the number had an abbreviation, so it will be handled below
        }

        if (!langCode.equals("en")) {
            try {
                numberWord = numberWord.replace(abbreviation, abbreviationSubscribersCount.get(abbreviation));
            } catch (NullPointerException e) {
                throw new ParsingException("The abbreviation \"" + abbreviation + "\" is missing in AbbreviationHelper map");
            }
        }
        return mixedNumberWordToLong(numberWord);
    }

    public static String moveAtRight(String toMove, String whole) {
        whole = whole.replace(toMove, "");
        whole += toMove;
        return whole;
    }

    /**
     * Check if the url matches the pattern.
     *
     * @param pattern the pattern that will be used to check the url
     * @param url     the url to be tested
     */
    public static void checkUrl(String pattern, String url) throws ParsingException {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Url can't be null or empty");
        }

        if (!Parser.isMatch(pattern, url.toLowerCase())) {
            throw new ParsingException("Url don't match the pattern");
        }
    }

    public static void printErrors(List<Throwable> errors) {
        for (Throwable e : errors) {
            e.printStackTrace();
            System.err.println("----------------");
        }
    }

    public static String replaceHttpWithHttps(final String url) {
        if (url == null) return null;

        if (!url.isEmpty() && url.startsWith(HTTP)) {
            return HTTPS + url.substring(HTTP.length());
        }
        return url;
    }

    /**
     * get the value of a URL-query by name.
     * if a url-query is give multiple times, only the value of the first query is returned
     *
     * @param url           the url to be used
     * @param parameterName the pattern that will be used to check the url
     * @return a string that contains the value of the query parameter or null if nothing was found
     */
    public static String getQueryValue(URL url, String parameterName) {
        String urlQuery = url.getQuery();

        if (urlQuery != null) {
            for (String param : urlQuery.split("&")) {
                String[] params = param.split("=", 2);

                String query;
                try {
                    query = URLDecoder.decode(params[0], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Cannot decode string with UTF-8. using the string without decoding");
                    e.printStackTrace();
                    query = params[0];
                }

                if (query.equals(parameterName)) {
                    try {
                        return URLDecoder.decode(params[1], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("Cannot decode string with UTF-8. using the string without decoding");
                        e.printStackTrace();
                        return params[1];
                    }
                }
            }
        }

        return null;
    }

    /**
     * converts a string to a URL-Object.
     * defaults to HTTP if no protocol is given
     *
     * @param url the string to be converted to a URL-Object
     * @return a URL-Object containing the url
     */
    public static URL stringToURL(String url) throws MalformedURLException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            // if no protocol is given try prepending "https://"
            if (e.getMessage().equals("no protocol: " + url)) {
                return new URL(HTTPS + url);
            }

            throw e;
        }
    }

    public static boolean isHTTP(URL url) {
        // make sure its http or https
        String protocol = url.getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https")) {
            return false;
        }

        boolean usesDefaultPort = url.getPort() == url.getDefaultPort();
        boolean setsNoPort = url.getPort() == -1;

        return setsNoPort || usesDefaultPort;
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        if (s.endsWith("\uFEFF")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String getBaseUrl(String url) throws ParsingException {
        URL uri;
        try {
            uri = stringToURL(url);
        } catch (MalformedURLException e) {
            throw new ParsingException("Malformed url: " + url, e);
        }
        return uri.getProtocol() + "://" + uri.getAuthority();
    }
}