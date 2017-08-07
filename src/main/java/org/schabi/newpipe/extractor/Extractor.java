package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

public abstract class Extractor {
    /**
     * {@link StreamingService} currently related to this extractor.<br/>
     * Useful for getting other things from a service (like the url handlers for cleaning/accepting/get id from urls).
     */
    private final StreamingService service;

    /**
     * Dirty/original url that was passed in the constructor.
     * <p>
     * What makes a url "dirty" or not is, for example, the additional parameters
     * (not important as—in this case—the id):
     * <pre>
     *     https://www.youtube.com/watch?v=a9Zf_258aTI<i>&t=4s</i>  →  <i><b>&t=4s</b></i>
     * </pre>
     * But as you can imagine, the time parameter is very important when calling, for example, {@link org.schabi.newpipe.extractor.stream.StreamExtractor#getTimeStamp()}.
     */
    private final String originalUrl;

    /**
     * The cleaned url, result of passing the {@link #originalUrl} to the associated urlIdHandler ({@link #getUrlIdHandler()}).
     * <p>
     * Is lazily-cleaned by calling {@link #getCleanUrl()}
     */
    private String cleanUrl;

    public Extractor(StreamingService service, String url) throws ExtractionException {
        this.service = service;
        this.originalUrl = url;
    }

    /**
     * @return a {@link UrlIdHandler} of the current extractor type (e.g. a UserExtractor should return a user url handler).
     */
    protected abstract UrlIdHandler getUrlIdHandler() throws ParsingException;

    /**
     * Fetch the current page.
     */
    public abstract void fetchPage() throws IOException, ExtractionException;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getCleanUrl() {
        if (cleanUrl != null && !cleanUrl.isEmpty()) return cleanUrl;

        try {
            cleanUrl = getUrlIdHandler().cleanUrl(originalUrl);
        } catch (Exception e) {
            cleanUrl = null;
            // Fallback to the original url
            return originalUrl;
        }
        return cleanUrl;
    }

    public StreamingService getService() {
        return service;
    }

    public int getServiceId() {
        return service.getServiceId();
    }
}
