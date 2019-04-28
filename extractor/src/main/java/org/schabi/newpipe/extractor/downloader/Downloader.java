package org.schabi.newpipe.extractor.downloader;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A base for downloader implementations that NewPipe will use
 * to download needed resources during extraction.
 */
public abstract class Downloader {

    /**
     * Do a GET request to get the resource that the url is pointing to.<br>
     * <br>
     * This method calls {@link #get(String, Map, Localization)} with the default preferred localization. It should only be
     * used when the resource that will be fetched won't be affected by the localization.
     *
     * @param url the URL that is pointing to the wanted resource
     * @return the result of the GET request
     */
    public Response get(String url) throws IOException, ReCaptchaException {
        return get(url, null, NewPipe.getPreferredLocalization());
    }

    /**
     * Do a GET request to get the resource that the url is pointing to.<br>
     * <br>
     * It will set the {@code Accept-Language} header to the language of the localization parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param localization the source of the value of the {@code Accept-Language} header
     * @return the result of the GET request
     */
    public Response get(String url, @Nullable Localization localization) throws IOException, ReCaptchaException {
        return get(url, null, localization);
    }

    /**
     * Do a GET request with the specified headers.
     *
     * @param url     the URL that is pointing to the wanted resource
     * @param headers a list of headers that will be used in the request.
     *                Any default headers <b>should</b> be overridden by these.
     * @return the result of the GET request
     */
    public Response get(String url, @Nullable Map<String, List<String>> headers) throws IOException, ReCaptchaException {
        return get(url, headers, NewPipe.getPreferredLocalization());
    }

    /**
     * Do a GET request with the specified headers.<br>
     * <br>
     * It will set the {@code Accept-Language} header to the language of the localization parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     *                     Any default headers <b>should</b> be overridden by these.
     * @param localization the source of the value of the {@code Accept-Language} header
     * @return the result of the GET request
     */
    public Response get(String url, @Nullable Map<String, List<String>> headers, @Nullable Localization localization)
            throws IOException, ReCaptchaException {
        return execute(Request.newBuilder()
                .get(url)
                .headers(headers)
                .localization(localization)
                .build());
    }

    /**
     * Do a HEAD request.
     *
     * @param url the URL that is pointing to the wanted resource
     * @return the result of the HEAD request
     */
    public Response head(String url) throws IOException, ReCaptchaException {
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
    public Response head(String url, @Nullable Map<String, List<String>> headers)
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
     * @return the result of the GET request
     */
    public Response post(String url, @Nullable Map<String, List<String>> headers, @Nullable byte[] dataToSend)
            throws IOException, ReCaptchaException {
        return post(url, headers, dataToSend, NewPipe.getPreferredLocalization());
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
     * @param localization the source of the value of the {@code Accept-Language} header
     * @return the result of the GET request
     */
    public Response post(String url, @Nullable Map<String, List<String>> headers, @Nullable byte[] dataToSend, @Nullable Localization localization)
            throws IOException, ReCaptchaException {
        return execute(Request.newBuilder()
                .post(url, dataToSend)
                .headers(headers)
                .localization(localization)
                .build());
    }

    /**
     * Do a request using the specified {@link Request} object.
     *
     * @return the result of the request
     */
    public abstract Response execute(@Nonnull Request request) throws IOException, ReCaptchaException;
}
