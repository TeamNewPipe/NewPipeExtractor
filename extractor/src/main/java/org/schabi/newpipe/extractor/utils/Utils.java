package org.schabi.newpipe.extractor.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class Utils {

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

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

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
            // if no protocol is given try prepending "http://"
            if (e.getMessage().equals("no protocol: " + url)) {
                return new URL(HTTP + url);
            }

            throw e;
        }
    }
    
    public static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        if (s.endsWith("\uFEFF")) {
            s = s.substring(0,  s.length()-1);
        }
        return s;
    }
}