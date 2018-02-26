package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * But as you can imagine, the time parameter is very important when calling {@link org.schabi.newpipe.extractor.stream.StreamExtractor#getTimeStamp()}.
     */
    private final String originalUrl;

    /**
     * The cleaned url, result of passing the {@link #originalUrl} to the associated urlIdHandler ({@link #getUrlIdHandler()}).
     * <p>
     * Is lazily-cleaned by calling {@link #getCleanUrl()}
     */
    @Nullable
    private String cleanUrl;
    private boolean pageFetched = false;
    private final Downloader downloader;

    public Extractor(final StreamingService service, final String url) {
        if(service == null) throw new NullPointerException("service is null");
        if(url == null) throw new NullPointerException("url is null");
        this.service = service;
        this.originalUrl = url;
        this.downloader = NewPipe.getDownloader();
        if(downloader == null) throw new NullPointerException("downloader is null");
    }

    /**
     * @return a {@link UrlIdHandler} of the current extractor type (e.g. a ChannelExtractor should return a channel url handler).
     */
    @Nonnull
    protected abstract UrlIdHandler getUrlIdHandler() throws ParsingException;

    /**
     * Fetch the current page.
     * @throws IOException if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    public void fetchPage() throws IOException, ExtractionException {
        if(pageFetched) return;
        onFetchPage(downloader);
        pageFetched = true;
    }

    protected void assertPageFetched() {
        if(!pageFetched) throw new IllegalStateException("Page is not fetched. Make sure you call fetchPage()");
    }

    protected boolean isPageFetched() {
        return pageFetched;
    }

    /**
     * Fetch the current page.
     * @param downloader the download to use
     * @throws IOException if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    public abstract void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException;

    @Nonnull
    public abstract String getId() throws ParsingException;

    /**
     * Get the name
     * @return the name
     * @throws ParsingException if the name cannot be extracted
     */
    @Nonnull
    public abstract String getName() throws ParsingException;

    @Nonnull
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * Get a clean url and as a fallback the original url.
     * @return the clean url or the original url
     */
    @Nonnull
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

    @Nonnull
    public StreamingService getService() {
        return service;
    }

    public int getServiceId() {
        return service.getServiceId();
    }
}
