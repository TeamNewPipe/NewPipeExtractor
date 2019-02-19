package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.utils.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;

public abstract class Extractor{ 
    /**
     * {@link StreamingService} currently related to this extractor.<br>
     * Useful for getting other things from a service (like the url handlers for cleaning/accepting/get id from urls).
     */
    private final StreamingService service;

    private final LinkHandler linkHandler;
    private final Localization localization;

    @Nullable
    private boolean pageFetched = false;
    private final Downloader downloader;

    public Extractor(final StreamingService service, final LinkHandler linkHandler, final Localization localization) {
        if(service == null) throw new NullPointerException("service is null");
        if(linkHandler == null) throw new NullPointerException("LinkHandler is null");
        this.service = service;
        this.linkHandler = linkHandler;
        this.downloader = NewPipe.getDownloader();
        this.localization = localization;
        if(downloader == null) throw new NullPointerException("downloader is null");
    }

    /**
     * @return The {@link LinkHandler} of the current extractor object (e.g. a ChannelExtractor should return a channel url handler).
     */
    @Nonnull
    public LinkHandler getLinkHandler() {
        return linkHandler;
    }

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
    public String getId() throws ParsingException {
        return linkHandler.getId();
    }

    /**
     * Get the name
     * @return the name
     * @throws ParsingException if the name cannot be extracted
     */
    @Nonnull
    public abstract String getName() throws ParsingException;

    @Nonnull
    public String getOriginalUrl() throws ParsingException {
        return linkHandler.getOriginalUrl();
    }

    @Nonnull
    public String getUrl() throws ParsingException {
        return linkHandler.getUrl();
    }

    @Nonnull
    public StreamingService getService() {
        return service;
    }

    public int getServiceId() {
        return service.getServiceId();
    }

    public Downloader getDownloader() {
        return downloader;
    }

    @Nonnull
    public Localization getLocalization() {
        return localization;
    }
}
