package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;
import java.util.List;

/**
 * Base class to extractors that have a list (e.g. playlists, channels).
 */
public abstract class ListExtractor extends Extractor {
    protected String nextStreamsUrl;

    /**
     * Get a new ListExtractor with the given nextStreamsUrl set.
     * <p>
     * The extractor <b>WILL</b> fetch the page if {@link #fetchPageUponCreation()} return true, otherwise, it will <b>NOT</b>.
     * <p>
     * You can call {@link #fetchPage()} later, but this is mainly used just to get more items, so we don't waste bandwidth
     * downloading the whole page, but if the service that is being implemented need it, just do its own logic in {@link #fetchPageUponCreation()}.
     */
    public ListExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url);
        setNextStreamsUrl(nextStreamsUrl);

        if (fetchPageUponCreation()) {
            fetchPage();
        }
    }

    /**
     * Decide if the page will be fetched upon creation.
     * <p>
     * The default implementation checks if the nextStreamsUrl is null or empty (indication that the caller
     * don't need or know what is the next page, thus, fetch the page).
     */
    protected boolean fetchPageUponCreation() {
        return nextStreamsUrl == null || nextStreamsUrl.isEmpty();
    }

    public abstract StreamInfoItemCollector getStreams() throws IOException, ExtractionException;
    public abstract NextItemsResult getNextStreams() throws IOException, ExtractionException;

    public boolean hasMoreStreams() {
        return nextStreamsUrl != null && !nextStreamsUrl.isEmpty();
    }

    public String getNextStreamsUrl() {
        return nextStreamsUrl;
    }

    public void setNextStreamsUrl(String nextStreamsUrl) {
        this.nextStreamsUrl = nextStreamsUrl;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    //////////////////////////////////////////////////////////////////////////*/

    public static class NextItemsResult {
        /**
         * The current list of items to this result
         */
        public final List<InfoItem> nextItemsList;

        /**
         * Next url to fetch more items
         */
        public final String nextItemsUrl;

        public NextItemsResult(List<InfoItem> nextItemsList, String nextItemsUrl) {
            this.nextItemsList = nextItemsList;
            this.nextItemsUrl = nextItemsUrl;
        }

        public boolean hasMoreStreams() {
            return nextItemsUrl != null && !nextItemsUrl.isEmpty();
        }
    }

}
