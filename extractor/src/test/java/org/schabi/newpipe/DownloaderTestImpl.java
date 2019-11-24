package org.schabi.newpipe;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class DownloaderTestImpl extends Downloader {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
    private static final String DEFAULT_HTTP_ACCEPT_LANGUAGE = "en";

    private static DownloaderTestImpl instance = null;

    private DownloaderTestImpl() {
    }

    public static DownloaderTestImpl getInstance() {
        if (instance == null) {
            synchronized (DownloaderTestImpl.class) {
                if (instance == null) {
                    instance = new DownloaderTestImpl();
                }
            }
        }
        return instance;
    }

    private void setDefaultHeaders(URLConnection connection) {
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", DEFAULT_HTTP_ACCEPT_LANGUAGE);
    }

    @Override
    public Response execute(@Nonnull Request request) throws IOException, ReCaptchaException {
        final String httpMethod = request.httpMethod();
        final String url = request.url();
        final Map<String, List<String>> headers = request.headers();
        @Nullable final byte[] dataToSend = request.dataToSend();
        @Nullable final Localization localization = request.localization();

        final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

        connection.setConnectTimeout(30 * 1000); // 30s
        connection.setReadTimeout(30 * 1000); // 30s
        connection.setRequestMethod(httpMethod);

        setDefaultHeaders(connection);

        for (Map.Entry<String, List<String>> pair : headers.entrySet()) {
            final String headerName = pair.getKey();
            final List<String> headerValueList = pair.getValue();

            if (headerValueList.size() > 1) {
                connection.setRequestProperty(headerName, null);
                for (String headerValue : headerValueList) {
                    connection.addRequestProperty(headerName, headerValue);
                }
            } else if (headerValueList.size() == 1) {
                connection.setRequestProperty(headerName, headerValueList.get(0));
            }
        }

        @Nullable OutputStream outputStream = null;
        @Nullable InputStreamReader input = null;
        try {
            if (dataToSend != null && dataToSend.length > 0) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", dataToSend.length + "");
                outputStream = connection.getOutputStream();
                outputStream.write(dataToSend);
            }

            final InputStream inputStream = connection.getInputStream();
            final StringBuilder response = new StringBuilder();

            // Not passing any charset for decoding here... something to keep in mind.
            input = new InputStreamReader(inputStream);

            int readCount;
            char[] buffer = new char[32 * 1024];
            while ((readCount = input.read(buffer)) != -1) {
                response.append(buffer, 0, readCount);
            }

            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            final Map<String, List<String>> responseHeaders = connection.getHeaderFields();

            return new Response(responseCode, responseMessage, responseHeaders, response.toString());
        } catch (Exception e) {
            /*
             * HTTP 429 == Too Many Request
             * Receive from Youtube.com = ReCaptcha challenge request
             * See : https://github.com/rg3/youtube-dl/issues/5138
             */
            if (connection.getResponseCode() == 429) {
                throw new ReCaptchaException("reCaptcha Challenge requested", url);
            }

            throw new IOException(connection.getResponseCode() + " " + connection.getResponseMessage(), e);
        } finally {
            if (outputStream != null) outputStream.close();
            if (input != null) input.close();
        }
    }
}
