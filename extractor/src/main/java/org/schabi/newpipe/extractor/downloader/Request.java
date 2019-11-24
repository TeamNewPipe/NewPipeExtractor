package org.schabi.newpipe.extractor.downloader;

import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * An object that holds request information used when {@link Downloader#execute(Request) executing} a request.
 */
public class Request {
    private final String httpMethod;
    private final String url;
    private final Map<String, List<String>> headers;
    @Nullable private final byte[] dataToSend;
    @Nullable private final Localization localization;

    public Request(String httpMethod, String url, Map<String, List<String>> headers, @Nullable byte[] dataToSend,
                   @Nullable Localization localization, boolean automaticLocalizationHeader) {
        if (httpMethod == null) throw new IllegalArgumentException("Request's httpMethod is null");
        if (url == null) throw new IllegalArgumentException("Request's url is null");

        this.httpMethod = httpMethod;
        this.url = url;
        this.dataToSend = dataToSend;
        this.localization = localization;

        Map<String, List<String>> headersToSet = null;
        if (headers == null) headers = Collections.emptyMap();

        if (automaticLocalizationHeader && localization != null) {
            headersToSet = new LinkedHashMap<>(headersFromLocalization(localization));
            headersToSet.putAll(headers);
        }

        this.headers = Collections.unmodifiableMap(headersToSet == null ? headers : headersToSet);
    }

    private Request(Builder builder) {
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
     * method to do this easily: {@link Request#headersFromLocalization(Localization)}).
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
        private Map<String, List<String>> headers = new LinkedHashMap<>();
        private byte[] dataToSend;
        private Localization localization;
        private boolean automaticLocalizationHeader = true;

        public Builder() {
        }

        /**
         * A http method (i.e. {@code GET, POST, HEAD}).
         */
        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        /**
         * The URL that is pointing to the wanted resource.
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * A list of headers that will be used in the request.<br>
         * Any default headers that the implementation may have, <b>should</b> be overridden by these.
         */
        public Builder headers(@Nullable Map<String, List<String>> headers) {
            if (headers == null) {
                this.headers.clear();
                return this;
            }
            this.headers.clear();
            this.headers.putAll(headers);
            return this;
        }

        /**
         * An optional byte array that will be sent when doing the request, very commonly used in
         * {@code POST} requests.<br>
         * <br>
         * The implementation should make note of some recommended headers
         * (for example, {@code Content-Length} in a post request).
         */
        public Builder dataToSend(byte[] dataToSend) {
            this.dataToSend = dataToSend;
            return this;
        }

        /**
         * A localization object that should be used when executing a request.<br>
         * <br>
         * Usually the {@code Accept-Language} will be set to this value (a helper
         * method to do this easily: {@link Request#headersFromLocalization(Localization)}).
         */
        public Builder localization(Localization localization) {
            this.localization = localization;
            return this;
        }

        /**
         * If localization headers should automatically be included in the request.
         */
        public Builder automaticLocalizationHeader(boolean automaticLocalizationHeader) {
            this.automaticLocalizationHeader = automaticLocalizationHeader;
            return this;
        }


        public Request build() {
            return new Request(this);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Http Methods Utils
        //////////////////////////////////////////////////////////////////////////*/

        public Builder get(String url) {
            this.httpMethod = "GET";
            this.url = url;
            return this;
        }

        public Builder head(String url) {
            this.httpMethod = "HEAD";
            this.url = url;
            return this;
        }

        public Builder post(String url, @Nullable byte[] dataToSend) {
            this.httpMethod = "POST";
            this.url = url;
            this.dataToSend = dataToSend;
            return this;
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Headers Utils
        //////////////////////////////////////////////////////////////////////////*/

        public Builder setHeaders(String headerName, List<String> headerValueList) {
            this.headers.remove(headerName);
            this.headers.put(headerName, headerValueList);
            return this;
        }

        public Builder addHeaders(String headerName, List<String> headerValueList) {
            @Nullable List<String> currentHeaderValueList = this.headers.get(headerName);
            if (currentHeaderValueList == null) {
                currentHeaderValueList = new ArrayList<>();
            }

            currentHeaderValueList.addAll(headerValueList);
            this.headers.put(headerName, headerValueList);
            return this;
        }

        public Builder setHeader(String headerName, String headerValue) {
            return setHeaders(headerName, Collections.singletonList(headerValue));
        }

        public Builder addHeader(String headerName, String headerValue) {
            return addHeaders(headerName, Collections.singletonList(headerValue));
        }

    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public static Map<String, List<String>> headersFromLocalization(@Nullable Localization localization) {
        if (localization == null) return Collections.emptyMap();

        final Map<String, List<String>> headers = new LinkedHashMap<>();
        if (!localization.getCountryCode().isEmpty()) {
            headers.put("Accept-Language", Collections.singletonList(localization.getLocalizationCode() +
                    ", " + localization.getLanguageCode() + ";q=0.9"));
        } else {
            headers.put("Accept-Language", Collections.singletonList(localization.getLanguageCode()));
        }

        return headers;
    }
}
