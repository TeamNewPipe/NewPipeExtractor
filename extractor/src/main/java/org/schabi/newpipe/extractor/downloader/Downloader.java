package org.schabi.newpipe.extractor.downloader;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A base for downloader implementations that NewPipe will use
 * to download needed resources during extraction.
 */
public abstract class Downloader {

    /**
     * Do a GET request to get the resource that the url is pointing to.<br>
     * <br>
     * This method calls {@link #get(String, Map, Locale)} with the default preferred
     * localization. It should only be used when the resource that will be fetched won't be affected
     * by the localization.
     *
     * @param url the URL that is pointing to the wanted resource
     * @return the result of the GET request
     */
    public Response get(final String url) throws IOException, ReCaptchaException {
        return get(url, null, NewPipe.getPreferredLocale());
    }

    /**
     * Do a GET request to get the resource that the url is pointing to.<br>
     * <br>
     * It will set the {@code Accept-Language} header to the language of the locale parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param locale the source of the value of the {@code Accept-Language} header
     * @return the result of the GET request
     */
    public Response get(final String url, final Locale locale)
            throws IOException, ReCaptchaException {
        return get(url, null, locale);
    }

    /**
     * Do a GET request with the specified headers.
     *
     * @param url     the URL that is pointing to the wanted resource
     * @param headers a list of headers that will be used in the request.
     *                Any default headers <b>should</b> be overridden by these.
     * @return the result of the GET request
     */
    public Response get(final String url, @Nullable final Map<String, List<String>> headers)
            throws IOException, ReCaptchaException {
        return get(url, headers, NewPipe.getPreferredLocale());
    }

    /**
     * Do a GET request with the specified headers.<br>
     * <br>
     * It will set the {@code Accept-Language} header to the language of the localization parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     *                     Any default headers <b>should</b> be overridden by these.
     * @param locale       the source of the value of the {@code Accept-Language} header
     * @return the result of the GET request
     */
    public Response get(final String url,
                        @Nullable final Map<String, List<String>> headers,
                        final Locale locale)
            throws IOException, ReCaptchaException {
        return execute(Request.newBuilder()
                .get(url)
                .headers(headers)
                .locale(locale)
                .build());
    }

    /**
     * Do a HEAD request.
     *
     * @param url the URL that is pointing to the wanted resource
     * @return the result of the HEAD request
     */
    public Response head(final String url) throws IOException, ReCaptchaException {
        return head(url, null);
    }

    /**
     * Do a HEAD request with the specified headers.
     *
     * @param url     the URL that is pointing to the wanted resource
     * @param headers a list of headers that will be used in the request.
     *                Any default headers <b>should</b> be overridden by these.
     * @return the result of the HEAD request
     */
    public Response head(final String url, @Nullable final Map<String, List<String>> headers)
            throws IOException, ReCaptchaException {
        return execute(Request.newBuilder()
                .head(url)
                .headers(headers)
                .build());
    }

    /**
     * Do a POST request with the specified headers, sending the data array.
     *
     * @param url        the URL that is pointing to the wanted resource
     * @param headers    a list of headers that will be used in the request.
     *                   Any default headers <b>should</b> be overridden by these.
     * @param dataToSend byte array that will be sent when doing the request.
     * @return the result of the POST request
     */
    public Response post(final String url,
                         @Nullable final Map<String, List<String>> headers,
                         @Nullable final byte[] dataToSend)
            throws IOException, ReCaptchaException {
        return post(url, headers, dataToSend, NewPipe.getPreferredLocale());
    }

    /**
     * Do a POST request with the specified headers, sending the data array.
     * <br>
     * It will set the {@code Accept-Language} header to the language of the localization parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     *                     Any default headers <b>should</b> be overridden by these.
     * @param dataToSend   byte array that will be sent when doing the request.
     * @param locale       the source of the value of the {@code Accept-Language} header
     * @return the result of the POST request
     */
    public Response post(final String url,
                         @Nullable final Map<String, List<String>> headers,
                         @Nullable final byte[] dataToSend,
                         final Locale locale)
            throws IOException, ReCaptchaException {
        return execute(Request.newBuilder()
                .post(url, dataToSend)
                .headers(headers)
                .locale(locale)
                .build());
    }

    /**
     * Convenient method to send a POST request using the specified value of the
     * {@code Content-Type} header with a given {@link Locale}.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     *                     Any default headers <b>should</b> be overridden by these.
     * @param dataToSend   byte array that will be sent when doing the request.
     * @param locale       the source of the value of the {@code Accept-Language} header
     * @param contentType  the mime type of the body sent, which will be set as the value of the
     *                     {@code Content-Type} header
     * @return the result of the POST request
     * @see #post(String, Map, byte[], Locale)
     */
    public Response postWithContentType(final String url,
                                        @Nullable final Map<String, List<String>> headers,
                                        @Nullable final byte[] dataToSend,
                                        final Locale locale,
                                        final String contentType)
            throws IOException, ReCaptchaException {
        final Map<String, List<String>> actualHeaders = new HashMap<>();
        if (headers != null) {
            actualHeaders.putAll(headers);
        }
        actualHeaders.put("Content-Type", List.of(contentType));
        return post(url, actualHeaders, dataToSend, locale);
    }

    /**
     * Convenient method to send a POST request using the specified value of the
     * {@code Content-Type} header.
     *
     * @param url         the URL that is pointing to the wanted resource
     * @param headers     a list of headers that will be used in the request.
     *                    Any default headers <b>should</b> be overridden by these.
     * @param dataToSend  byte array that will be sent when doing the request.
     * @param contentType the mime type of the body sent, which will be set as the value of the
     *                    {@code Content-Type} header
     * @return the result of the POST request
     * @see #post(String, Map, byte[], Locale)
     */
    public Response postWithContentType(final String url,
                                        @Nullable final Map<String, List<String>> headers,
                                        @Nullable final byte[] dataToSend,
                                        final String contentType)
            throws IOException, ReCaptchaException {
        return postWithContentType(url, headers, dataToSend, NewPipe.getPreferredLocale(),
                contentType);
    }

    /**
     * Convenient method to send a POST request the JSON mime type as the value of the
     * {@code Content-Type} header with a given {@link Locale}.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     *                     Any default headers <b>should</b> be overridden by these.
     * @param dataToSend   byte array that will be sent when doing the request.
     * @param locale       the source of the value of the {@code Accept-Language} header
     * @return the result of the POST request
     * @see #post(String, Map, byte[], Locale)
     */
    public Response postWithContentTypeJson(final String url,
                                            @Nullable final Map<String, List<String>> headers,
                                            @Nullable final byte[] dataToSend,
                                            final Locale locale)
            throws IOException, ReCaptchaException {
        return postWithContentType(url, headers, dataToSend, locale, "application/json");
    }

    /**
     * Convenient method to send a POST request the JSON mime type as the value of the
     * {@code Content-Type} header.
     *
     * @param url         the URL that is pointing to the wanted resource
     * @param headers     a list of headers that will be used in the request.
     *                    Any default headers <b>should</b> be overridden by these.
     * @param dataToSend  byte array that will be sent when doing the request.
     * @return the result of the POST request
     * @see #post(String, Map, byte[], Locale)
     */
    public Response postWithContentTypeJson(final String url,
                                            @Nullable final Map<String, List<String>> headers,
                                            @Nullable final byte[] dataToSend)
            throws IOException, ReCaptchaException {
        return postWithContentTypeJson(url, headers, dataToSend, NewPipe.getPreferredLocale());
    }

    /**
     * Do a request using the specified {@link Request} object.
     *
     * @return the result of the request
     */
    public abstract Response execute(@Nonnull Request request)
            throws IOException, ReCaptchaException;
}
