package org.schabi.newpipe.extractor.downloader;

import org.schabi.newpipe.extractor.localization.Localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An object that holds request information used when {@link Downloader#execute(Request) executing}
 * a request.
 */
public class Request {
    private final String httpMethod;
    private final String url;
    private final Map<String, List<String>> headers;
    @Nullable
    private final byte[] dataToSend;
    @Nullable
    private final Localization localization;

    public Request(final String httpMethod,
                   final String url,
                   @Nullable final Map<String, List<String>> headers,
                   @Nullable final byte[] dataToSend,
                   @Nullable final Localization localization,
                   final boolean automaticLocalizationHeader) {
        this.httpMethod = Objects.requireNonNull(httpMethod, "Request's httpMethod is null");
        this.url = Objects.requireNonNull(url, "Request's url is null");
        this.dataToSend = dataToSend;
        this.localization = localization;

        final Map<String, List<String>> actualHeaders = new LinkedHashMap<>();
        if (headers != null) {
            actualHeaders.putAll(headers);
        }
        if (automaticLocalizationHeader && localization != null) {
            actualHeaders.putAll(getHeadersFromLocalization(localization));
        }

        this.headers = Collections.unmodifiableMap(actualHeaders);
    }

    private Request(final Builder builder) {
        this(builder.httpMethod, builder.url, builder.headers, builder.dataToSend,
                builder.localization, builder.automaticLocalizationHeader);
    }

    /**
     * A http method (i.e. {@code GET, POST, HEAD}).
     */
    public String httpMethod() {
        return httpMethod;
    }

    /**
     * The URL that is pointing to the wanted resource.
     */
    public String url() {
        return url;
    }

    /**
     * A list of headers that will be used in the request.<br>
     * Any default headers that the implementation may have, <b>should</b> be overridden by these.
     */
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * An optional byte array that will be sent when doing the request, very commonly used in
     * {@code POST} requests.<br>
     * <br>
     * The implementation should make note of some recommended headers
     * (for example, {@code Content-Length} in a post request).
     */
    @Nullable
    public byte[] dataToSend() {
        return dataToSend;
    }

    /**
     * A localization object that should be used when executing a request.<br>
     * <br>
     * Usually the {@code Accept-Language} will be set to this value (a helper
     * method to do this easily: {@link Request#getHeadersFromLocalization(Localization)}).
     */
    @Nullable
    public Localization localization() {
        return localization;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String httpMethod;
        private String url;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();
        private byte[] dataToSend;
        private Localization localization;
        private boolean automaticLocalizationHeader = true;

        public Builder() {
        }

        /**
         * A http method (i.e. {@code GET, POST, HEAD}).
         */
        public Builder httpMethod(final String httpMethodToSet) {
            this.httpMethod = httpMethodToSet;
            return this;
        }

        /**
         * The URL that is pointing to the wanted resource.
         */
        public Builder url(final String urlToSet) {
            this.url = urlToSet;
            return this;
        }

        /**
         * A list of headers that will be used in the request.<br>
         * Any default headers that the implementation may have, <b>should</b> be overridden by
         * these.
         */
        public Builder headers(@Nullable final Map<String, List<String>> headersToSet) {
            this.headers.clear();
            if (headersToSet != null) {
                this.headers.putAll(headersToSet);
            }
            return this;
        }

        /**
         * An optional byte array that will be sent when doing the request, very commonly used in
         * {@code POST} requests.<br>
         * <br>
         * The implementation should make note of some recommended headers
         * (for example, {@code Content-Length} in a post request).
         */
        public Builder dataToSend(final byte[] dataToSendToSet) {
            this.dataToSend = dataToSendToSet;
            return this;
        }

        /**
         * A localization object that should be used when executing a request.<br>
         * <br>
         * Usually the {@code Accept-Language} will be set to this value (a helper
         * method to do this easily: {@link Request#getHeadersFromLocalization(Localization)}).
         */
        public Builder localization(final Localization localizationToSet) {
            this.localization = localizationToSet;
            return this;
        }

        /**
         * If localization headers should automatically be included in the request.
         */
        public Builder automaticLocalizationHeader(final boolean automaticLocalizationHeaderToSet) {
            this.automaticLocalizationHeader = automaticLocalizationHeaderToSet;
            return this;
        }


        public Request build() {
            return new Request(this);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Http Methods Utils
        //////////////////////////////////////////////////////////////////////////*/

        public Builder get(final String urlToSet) {
            this.httpMethod = "GET";
            this.url = urlToSet;
            return this;
        }

        public Builder head(final String urlToSet) {
            this.httpMethod = "HEAD";
            this.url = urlToSet;
            return this;
        }

        public Builder post(final String urlToSet, @Nullable final byte[] dataToSendToSet) {
            this.httpMethod = "POST";
            this.url = urlToSet;
            this.dataToSend = dataToSendToSet;
            return this;
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Headers Utils
        //////////////////////////////////////////////////////////////////////////*/

        public Builder setHeaders(final String headerName, final List<String> headerValueList) {
            this.headers.remove(headerName);
            this.headers.put(headerName, headerValueList);
            return this;
        }

        public Builder addHeaders(final String headerName, final List<String> headerValueList) {
            @Nullable List<String> currentHeaderValueList = this.headers.get(headerName);
            if (currentHeaderValueList == null) {
                currentHeaderValueList = new ArrayList<>();
            }

            currentHeaderValueList.addAll(headerValueList);
            this.headers.put(headerName, headerValueList);
            return this;
        }

        public Builder setHeader(final String headerName, final String headerValue) {
            return setHeaders(headerName, Collections.singletonList(headerValue));
        }

        public Builder addHeader(final String headerName, final String headerValue) {
            return addHeaders(headerName, Collections.singletonList(headerValue));
        }

    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public static Map<String, List<String>> getHeadersFromLocalization(
            @Nullable final Localization localization) {
        if (localization == null) {
            return Collections.emptyMap();
        }

        final String languageCode = localization.getLanguageCode();
        final List<String> languageCodeList = Collections.singletonList(
                localization.getCountryCode().isEmpty() ? languageCode
                        : localization.getLocalizationCode() + ", " + languageCode + ";q=0.9");
        return Collections.singletonMap("Accept-Language", languageCodeList);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Generated
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Request request = (Request) o;
        return httpMethod.equals(request.httpMethod)
                && url.equals(request.url)
                && headers.equals(request.headers)
                && Arrays.equals(dataToSend, request.dataToSend)
                && Objects.equals(localization, request.localization);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(httpMethod, url, headers, localization);
        result = 31 * result + Arrays.hashCode(dataToSend);
        return result;
    }
}
